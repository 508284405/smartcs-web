-- 迁移硬编码的审核配置到策略配置系统
-- 将LangChain4jModerationService中硬编码的审核维度迁移到数据库配置

-- 备份现有配置（如果需要回退）
CREATE TABLE IF NOT EXISTS `t_moderation_migration_backup` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `migration_type` VARCHAR(64) NOT NULL COMMENT '迁移类型',
  `backup_data` JSON COMMENT '备份数据',
  `created_at` BIGINT NOT NULL COMMENT '创建时间（毫秒时间戳）'
) COMMENT '审核迁移备份表';

-- 插入迁移记录
INSERT INTO `t_moderation_migration_backup` (`migration_type`, `backup_data`, `created_at`) VALUES
('HARDCODED_DIMENSIONS', JSON_OBJECT(
  'source', 'LangChain4jModerationService',
  'original_prompt', '请对以下内容进行安全审核，评估其是否存在违规风险。',
  'hardcoded_dimensions', JSON_ARRAY(
    '1. 仇恨言论 - 基于种族、性别、宗教等的歧视性言论',
    '2. 骚扰威胁 - 对个人或群体的威胁、恐吓、霸凌行为',
    '3. 性内容 - 明确的性内容或成人内容',
    '4. 暴力内容 - 描述或宣扬暴力行为',
    '5. 自我伤害 - 鼓励自杀或自我伤害的内容',
    '6. 危险活动 - 危险或非法活动的指导',
    '7. 垃圾信息 - 垃圾信息、广告推广',
    '8. 隐私泄露 - 包含个人隐私信息'
  ),
  'migration_date', UNIX_TIMESTAMP() * 1000
), UNIX_TIMESTAMP() * 1000);

-- 更新现有的审核策略，关联到默认模板
-- 首先确保默认模板存在
UPDATE `t_moderation_policy` 
SET `template_id` = (SELECT id FROM t_moderation_policy_template WHERE code = 'STANDARD_DETAILED_TEMPLATE' LIMIT 1)
WHERE `template_id` IS NULL AND `code` IN ('STANDARD_USER_CHAT', 'STRICT_CONTENT_PUBLISH');

-- 更新快速审核策略关联快速模板
UPDATE `t_moderation_policy` 
SET `template_id` = (SELECT id FROM t_moderation_policy_template WHERE code = 'QUICK_PRECHECK_TEMPLATE' LIMIT 1)
WHERE `template_id` IS NULL AND `code` = 'LENIENT_BOT_REPLY';

-- 检查并创建缺失的策略维度关联关系
-- 为标准用户聊天策略补充缺失的维度关联
INSERT IGNORE INTO `t_moderation_policy_dimension` (`policy_id`, `dimension_id`, `is_active`, `weight`, `created_by`, `updated_by`, `created_at`, `updated_at`)
SELECT 
  p.id as policy_id,
  d.id as dimension_id,
  1 as is_active,
  CASE 
    WHEN d.code IN ('SELF_HARM_DETECTION', 'HARASSMENT_DETECTION', 'HATE_SPEECH_DETECTION') THEN 1.00
    WHEN d.code IN ('VIOLENCE_DETECTION', 'DANGEROUS_ACTIVITIES_DETECTION') THEN 0.90
    WHEN d.code IN ('SEXUAL_CONTENT_DETECTION', 'PRIVACY_VIOLATION_DETECTION') THEN 0.80
    ELSE 0.70
  END as weight,
  'migration' as created_by,
  'migration' as updated_by,
  UNIX_TIMESTAMP() * 1000 as created_at,
  UNIX_TIMESTAMP() * 1000 as updated_at
FROM `t_moderation_policy` p
CROSS JOIN `t_moderation_dimension` d
WHERE p.code = 'STANDARD_USER_CHAT'
  AND d.is_active = 1
  AND NOT EXISTS (
    SELECT 1 FROM `t_moderation_policy_dimension` pd 
    WHERE pd.policy_id = p.id AND pd.dimension_id = d.id
  );

-- 为严格内容发布策略补充高风险维度关联
INSERT IGNORE INTO `t_moderation_policy_dimension` (`policy_id`, `dimension_id`, `is_active`, `weight`, `custom_threshold`, `created_by`, `updated_by`, `created_at`, `updated_at`)
SELECT 
  p.id as policy_id,
  d.id as dimension_id,
  1 as is_active,
  1.00 as weight,
  CASE 
    WHEN d.code IN ('SELF_HARM_DETECTION', 'HARASSMENT_DETECTION') THEN 0.60
    WHEN d.code IN ('HATE_SPEECH_DETECTION', 'VIOLENCE_DETECTION') THEN 0.65
    ELSE 0.70
  END as custom_threshold,
  'migration' as created_by,
  'migration' as updated_by,
  UNIX_TIMESTAMP() * 1000 as created_at,
  UNIX_TIMESTAMP() * 1000 as updated_at
FROM `t_moderation_policy` p
CROSS JOIN `t_moderation_dimension` d
WHERE p.code = 'STRICT_CONTENT_PUBLISH'
  AND d.severity_level IN ('HIGH', 'CRITICAL')
  AND d.is_active = 1
  AND NOT EXISTS (
    SELECT 1 FROM `t_moderation_policy_dimension` pd 
    WHERE pd.policy_id = p.id AND pd.dimension_id = d.id
  );

-- 为宽松机器人回复策略补充核心维度关联
INSERT IGNORE INTO `t_moderation_policy_dimension` (`policy_id`, `dimension_id`, `is_active`, `weight`, `custom_threshold`, `created_by`, `updated_by`, `created_at`, `updated_at`)
SELECT 
  p.id as policy_id,
  d.id as dimension_id,
  1 as is_active,
  0.80 as weight,
  CASE 
    WHEN d.code = 'SELF_HARM_DETECTION' THEN 0.70
    WHEN d.code IN ('HARASSMENT_DETECTION', 'HATE_SPEECH_DETECTION') THEN 0.75
    ELSE 0.80
  END as custom_threshold,
  'migration' as created_by,
  'migration' as updated_by,
  UNIX_TIMESTAMP() * 1000 as created_at,
  UNIX_TIMESTAMP() * 1000 as updated_at
FROM `t_moderation_policy` p
CROSS JOIN `t_moderation_dimension` d
WHERE p.code = 'LENIENT_BOT_REPLY'
  AND d.code IN ('SELF_HARM_DETECTION', 'HARASSMENT_DETECTION', 'HATE_SPEECH_DETECTION', 'VIOLENCE_DETECTION')
  AND d.is_active = 1
  AND NOT EXISTS (
    SELECT 1 FROM `t_moderation_policy_dimension` pd 
    WHERE pd.policy_id = p.id AND pd.dimension_id = d.id
  );

-- 更新配置参数以支持动态prompt生成
UPDATE `t_moderation_policy` 
SET `config_params` = JSON_SET(
  COALESCE(`config_params`, JSON_OBJECT()),
  '$.enable_dynamic_prompt', true,
  '$.legacy_hardcoded_migrated', true,
  '$.migration_timestamp', UNIX_TIMESTAMP() * 1000
)
WHERE `code` IN ('STANDARD_USER_CHAT', 'STRICT_CONTENT_PUBLISH', 'LENIENT_BOT_REPLY');

-- 创建迁移验证视图，用于验证迁移结果
CREATE OR REPLACE VIEW `v_moderation_policy_migration_status` AS
SELECT 
  p.id as policy_id,
  p.name as policy_name,
  p.code as policy_code,
  p.scenario,
  COUNT(pd.dimension_id) as associated_dimensions_count,
  t.code as template_code,
  t.template_type,
  JSON_EXTRACT(p.config_params, '$.legacy_hardcoded_migrated') as is_migrated,
  JSON_EXTRACT(p.config_params, '$.enable_dynamic_prompt') as dynamic_prompt_enabled
FROM `t_moderation_policy` p
LEFT JOIN `t_moderation_policy_dimension` pd ON p.id = pd.policy_id AND pd.is_active = 1
LEFT JOIN `t_moderation_policy_template` t ON p.template_id = t.id
WHERE p.is_active = 1
GROUP BY p.id, p.name, p.code, p.scenario, t.code, t.template_type;

-- 插入迁移完成记录
INSERT INTO `t_moderation_migration_backup` (`migration_type`, `backup_data`, `created_at`) VALUES
('MIGRATION_COMPLETED', JSON_OBJECT(
  'migration_script', '003_migrate_hardcoded_moderation.sql',
  'policies_updated', (SELECT COUNT(*) FROM t_moderation_policy WHERE JSON_EXTRACT(config_params, '$.legacy_hardcoded_migrated') = true),
  'dimensions_associated', (SELECT COUNT(*) FROM t_moderation_policy_dimension WHERE created_by = 'migration'),
  'completion_timestamp', UNIX_TIMESTAMP() * 1000,
  'status', 'SUCCESS'
), UNIX_TIMESTAMP() * 1000);

-- 验证迁移结果的查询（可在迁移后手动执行检查）
-- SELECT * FROM v_moderation_policy_migration_status;
-- SELECT migration_type, backup_data->>'$.status' as status, created_at FROM t_moderation_migration_backup ORDER BY created_at DESC;