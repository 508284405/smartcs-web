-- 内容审核模块 - 审核策略表
CREATE TABLE IF NOT EXISTS `t_moderation_policy` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `name` VARCHAR(128) NOT NULL COMMENT '策略名称',
  `code` VARCHAR(64) NOT NULL COMMENT '策略编码，用于程序识别',
  `description` VARCHAR(512) COMMENT '策略描述',
  `scenario` VARCHAR(64) NOT NULL COMMENT '适用场景 USER_CHAT/BOT_REPLY/CONTENT_PUBLISH等',
  `policy_type` VARCHAR(32) DEFAULT 'STANDARD' COMMENT '策略类型 STANDARD/STRICT/LENIENT',
  `default_risk_level` VARCHAR(16) DEFAULT 'MEDIUM' COMMENT '默认风险等级 LOW/MEDIUM/HIGH/CRITICAL',
  `default_action` VARCHAR(32) DEFAULT 'REVIEW' COMMENT '默认处理动作 WARN/REVIEW/BLOCK/ESCALATE',
  `is_active` TINYINT DEFAULT 1 COMMENT '是否启用 1=启用 0=禁用',
  `priority` INT DEFAULT 100 COMMENT '优先级，数值越小优先级越高',
  `config_params` JSON COMMENT '策略配置参数（JSON格式）',
  `template_id` BIGINT COMMENT 'prompt模板ID',
  `created_by` VARCHAR(64) COMMENT '创建者',
  `updated_by` VARCHAR(64) COMMENT '更新者',
  `created_at` BIGINT NOT NULL COMMENT '创建时间（毫秒时间戳）',
  `updated_at` BIGINT NOT NULL COMMENT '更新时间（毫秒时间戳）',
  UNIQUE KEY uk_code (`code`),
  INDEX idx_scenario (`scenario`),
  INDEX idx_policy_type (`policy_type`),
  INDEX idx_is_active (`is_active`),
  INDEX idx_priority (`priority`),
  INDEX idx_template_id (`template_id`)
) COMMENT '内容审核策略配置表';

-- 内容审核模块 - 审核维度表
CREATE TABLE IF NOT EXISTS `t_moderation_dimension` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `name` VARCHAR(128) NOT NULL COMMENT '维度名称',
  `code` VARCHAR(64) NOT NULL COMMENT '维度编码，用于程序识别',
  `description` VARCHAR(512) COMMENT '维度描述',
  `check_guideline` TEXT COMMENT '详细的检查指南',
  `severity_level` VARCHAR(16) DEFAULT 'MEDIUM' COMMENT '严重程度级别 LOW/MEDIUM/HIGH/CRITICAL',
  `action_type` VARCHAR(32) DEFAULT 'REVIEW' COMMENT '处理动作类型 WARN/REVIEW/BLOCK/ESCALATE',
  `confidence_threshold` DECIMAL(3,2) DEFAULT 0.50 COMMENT '置信度阈值（0.00-1.00）',
  `is_active` TINYINT DEFAULT 1 COMMENT '是否启用 1=启用 0=禁用',
  `sort_order` INT DEFAULT 0 COMMENT '排序权重',
  `category` VARCHAR(64) COMMENT '维度分类 CONTENT_SAFETY/PRIVACY_PROTECTION等',
  `config_params` JSON COMMENT '维度配置参数（JSON格式）',
  `category_id` BIGINT COMMENT '关联的审核分类ID',
  `created_by` VARCHAR(64) COMMENT '创建者',
  `updated_by` VARCHAR(64) COMMENT '更新者',
  `created_at` BIGINT NOT NULL COMMENT '创建时间（毫秒时间戳）',
  `updated_at` BIGINT NOT NULL COMMENT '更新时间（毫秒时间戳）',
  UNIQUE KEY uk_code (`code`),
  INDEX idx_severity_level (`severity_level`),
  INDEX idx_action_type (`action_type`),
  INDEX idx_is_active (`is_active`),
  INDEX idx_sort_order (`sort_order`),
  INDEX idx_category (`category`),
  INDEX idx_category_id (`category_id`)
) COMMENT '内容审核维度配置表';

-- 内容审核模块 - 策略维度关联表
CREATE TABLE IF NOT EXISTS `t_moderation_policy_dimension` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `policy_id` BIGINT NOT NULL COMMENT '策略ID',
  `dimension_id` BIGINT NOT NULL COMMENT '维度ID',
  `is_active` TINYINT DEFAULT 1 COMMENT '在该策略中是否启用 1=启用 0=禁用',
  `weight` DECIMAL(3,2) DEFAULT 1.00 COMMENT '在该策略中的权重（0.00-1.00）',
  `custom_threshold` DECIMAL(3,2) COMMENT '在该策略中的自定义阈值',
  `custom_action` VARCHAR(32) COMMENT '在该策略中的自定义动作',
  `created_by` VARCHAR(64) COMMENT '创建者',
  `updated_by` VARCHAR(64) COMMENT '更新者',
  `created_at` BIGINT NOT NULL COMMENT '创建时间（毫秒时间戳）',
  `updated_at` BIGINT NOT NULL COMMENT '更新时间（毫秒时间戳）',
  UNIQUE KEY uk_policy_dimension (`policy_id`, `dimension_id`),
  INDEX idx_policy_id (`policy_id`),
  INDEX idx_dimension_id (`dimension_id`),
  INDEX idx_is_active (`is_active`),
  FOREIGN KEY fk_policy (`policy_id`) REFERENCES `t_moderation_policy` (`id`) ON DELETE CASCADE,
  FOREIGN KEY fk_dimension (`dimension_id`) REFERENCES `t_moderation_dimension` (`id`) ON DELETE CASCADE
) COMMENT '审核策略维度关联表';

-- 内容审核模块 - 策略模板表
CREATE TABLE IF NOT EXISTS `t_moderation_policy_template` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `name` VARCHAR(128) NOT NULL COMMENT '模板名称',
  `code` VARCHAR(64) NOT NULL COMMENT '模板编码，用于程序识别',
  `description` VARCHAR(512) COMMENT '模板描述',
  `template_type` VARCHAR(32) DEFAULT 'DETAILED' COMMENT '模板类型 DETAILED/QUICK/BATCH',
  `prompt_template` TEXT NOT NULL COMMENT '基础prompt模板内容',
  `dimension_template` TEXT COMMENT '维度列表模板',
  `response_template` TEXT COMMENT '响应格式模板',
  `language` VARCHAR(16) DEFAULT 'zh-CN' COMMENT '支持的语言 zh-CN/en-US等',
  `variables` JSON COMMENT '模板变量定义（JSON格式）',
  `default_values` JSON COMMENT '默认变量值（JSON格式）',
  `is_active` TINYINT DEFAULT 1 COMMENT '是否启用 1=启用 0=禁用',
  `version` VARCHAR(16) DEFAULT '1.0' COMMENT '版本号',
  `created_by` VARCHAR(64) COMMENT '创建者',
  `updated_by` VARCHAR(64) COMMENT '更新者',
  `created_at` BIGINT NOT NULL COMMENT '创建时间（毫秒时间戳）',
  `updated_at` BIGINT NOT NULL COMMENT '更新时间（毫秒时间戳）',
  UNIQUE KEY uk_code_version (`code`, `version`),
  INDEX idx_template_type (`template_type`),
  INDEX idx_language (`language`),
  INDEX idx_is_active (`is_active`),
  INDEX idx_version (`version`)
) COMMENT '审核策略模板表';

-- 插入默认的审核策略
INSERT INTO `t_moderation_policy` (`name`, `code`, `description`, `scenario`, `policy_type`, `default_risk_level`, `default_action`, `priority`, `config_params`, `created_by`, `created_at`, `updated_at`) VALUES
('标准用户聊天审核', 'STANDARD_USER_CHAT', '适用于用户聊天场景的标准审核策略', 'USER_CHAT', 'STANDARD', 'MEDIUM', 'REVIEW', 100, '{"timeout_seconds": 5, "enable_cache": true}', 'system', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('严格内容发布审核', 'STRICT_CONTENT_PUBLISH', '适用于内容发布场景的严格审核策略', 'CONTENT_PUBLISH', 'STRICT', 'HIGH', 'BLOCK', 50, '{"timeout_seconds": 10, "enable_cache": false}', 'system', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('宽松机器人回复审核', 'LENIENT_BOT_REPLY', '适用于机器人回复场景的宽松审核策略', 'BOT_REPLY', 'LENIENT', 'LOW', 'WARN', 200, '{"timeout_seconds": 3, "enable_cache": true}', 'system', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000);

-- 插入默认的审核维度（基于现有硬编码的维度）
INSERT INTO `t_moderation_dimension` (`name`, `code`, `description`, `check_guideline`, `severity_level`, `action_type`, `confidence_threshold`, `sort_order`, `category`, `category_id`, `created_by`, `created_at`, `updated_at`) VALUES
('仇恨言论检测', 'HATE_SPEECH_DETECTION', '检测基于种族、性别、宗教等的歧视性言论', '识别任何基于种族、民族、性别、性取向、宗教信仰、年龄、残疾状况等特征的仇恨、歧视或偏见性言论', 'HIGH', 'BLOCK', 0.70, 1, 'CONTENT_SAFETY', (SELECT id FROM t_moderation_category WHERE code = 'HATE_SPEECH' LIMIT 1), 'system', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('骚扰威胁检测', 'HARASSMENT_DETECTION', '检测对个人或群体的威胁、恐吓、霸凌行为', '识别威胁、恐吓、霸凌、骚扰或任何意图伤害他人的言论和行为', 'HIGH', 'BLOCK', 0.75, 2, 'CONTENT_SAFETY', (SELECT id FROM t_moderation_category WHERE code = 'HARASSMENT' LIMIT 1), 'system', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('性内容检测', 'SEXUAL_CONTENT_DETECTION', '检测明确的性内容或成人内容', '识别露骨的性描述、色情内容或不适当的成人内容', 'MEDIUM', 'BLOCK', 0.65, 3, 'CONTENT_SAFETY', (SELECT id FROM t_moderation_category WHERE code = 'SEXUAL_CONTENT' LIMIT 1), 'system', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('暴力内容检测', 'VIOLENCE_DETECTION', '检测描述或宣扬暴力行为的内容', '识别描述暴力、鼓励暴力行为或包含血腥暴力场面的内容', 'HIGH', 'BLOCK', 0.70, 4, 'CONTENT_SAFETY', (SELECT id FROM t_moderation_category WHERE code = 'VIOLENCE' LIMIT 1), 'system', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('自我伤害检测', 'SELF_HARM_DETECTION', '检测鼓励自杀或自我伤害的内容', '识别鼓励、美化或描述自杀、自残或其他自我伤害行为的内容', 'CRITICAL', 'ESCALATE', 0.80, 5, 'CONTENT_SAFETY', (SELECT id FROM t_moderation_category WHERE code = 'SELF_HARM' LIMIT 1), 'system', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('危险活动检测', 'DANGEROUS_ACTIVITIES_DETECTION', '检测危险或非法活动的指导内容', '识别提供危险、非法或有害活动指导的内容，包括制作危险物品的方法', 'HIGH', 'BLOCK', 0.70, 6, 'CONTENT_SAFETY', (SELECT id FROM t_moderation_category WHERE code = 'DANGEROUS_ACTIVITIES' LIMIT 1), 'system', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('垃圾信息检测', 'SPAM_DETECTION', '检测垃圾信息、广告推广等', '识别重复性信息、恶意链接、不当广告或其他垃圾内容', 'LOW', 'WARN', 0.60, 7, 'CONTENT_QUALITY', (SELECT id FROM t_moderation_category WHERE code = 'SPAM' LIMIT 1), 'system', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('隐私泄露检测', 'PRIVACY_VIOLATION_DETECTION', '检测包含个人隐私信息的内容', '识别身份证号、手机号、邮箱地址、家庭住址等个人敏感信息', 'MEDIUM', 'REVIEW', 0.65, 8, 'PRIVACY_PROTECTION', (SELECT id FROM t_moderation_category WHERE code = 'PRIVACY_VIOLATION' LIMIT 1), 'system', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000);

-- 插入默认的策略维度关联关系
-- 标准用户聊天审核策略关联所有维度
INSERT INTO `t_moderation_policy_dimension` (`policy_id`, `dimension_id`, `is_active`, `weight`, `created_by`, `created_at`, `updated_at`)
SELECT 
  (SELECT id FROM t_moderation_policy WHERE code = 'STANDARD_USER_CHAT'),
  d.id,
  1,
  CASE 
    WHEN d.code IN ('SELF_HARM_DETECTION', 'HARASSMENT_DETECTION', 'HATE_SPEECH_DETECTION') THEN 1.00
    WHEN d.code IN ('VIOLENCE_DETECTION', 'DANGEROUS_ACTIVITIES_DETECTION') THEN 0.90
    WHEN d.code IN ('SEXUAL_CONTENT_DETECTION', 'PRIVACY_VIOLATION_DETECTION') THEN 0.80
    ELSE 0.70
  END,
  'system',
  UNIX_TIMESTAMP() * 1000,
  UNIX_TIMESTAMP() * 1000
FROM t_moderation_dimension d;

-- 严格内容发布审核策略关联高风险维度
INSERT INTO `t_moderation_policy_dimension` (`policy_id`, `dimension_id`, `is_active`, `weight`, `custom_threshold`, `created_by`, `created_at`, `updated_at`)
SELECT 
  (SELECT id FROM t_moderation_policy WHERE code = 'STRICT_CONTENT_PUBLISH'),
  d.id,
  1,
  1.00,
  CASE 
    WHEN d.code IN ('SELF_HARM_DETECTION', 'HARASSMENT_DETECTION') THEN 0.60
    WHEN d.code IN ('HATE_SPEECH_DETECTION', 'VIOLENCE_DETECTION') THEN 0.65
    ELSE 0.70
  END,
  'system',
  UNIX_TIMESTAMP() * 1000,
  UNIX_TIMESTAMP() * 1000
FROM t_moderation_dimension d
WHERE d.severity_level IN ('HIGH', 'CRITICAL');

-- 宽松机器人回复审核策略关联部分维度
INSERT INTO `t_moderation_policy_dimension` (`policy_id`, `dimension_id`, `is_active`, `weight`, `custom_threshold`, `created_by`, `created_at`, `updated_at`)
SELECT 
  (SELECT id FROM t_moderation_policy WHERE code = 'LENIENT_BOT_REPLY'),
  d.id,
  1,
  0.80,
  CASE 
    WHEN d.code = 'SELF_HARM_DETECTION' THEN 0.70
    WHEN d.code IN ('HARASSMENT_DETECTION', 'HATE_SPEECH_DETECTION') THEN 0.75
    ELSE 0.80
  END,
  'system',
  UNIX_TIMESTAMP() * 1000,
  UNIX_TIMESTAMP() * 1000
FROM t_moderation_dimension d
WHERE d.code IN ('SELF_HARM_DETECTION', 'HARASSMENT_DETECTION', 'HATE_SPEECH_DETECTION', 'VIOLENCE_DETECTION');

-- 插入默认的prompt模板
INSERT INTO `t_moderation_policy_template` (`name`, `code`, `description`, `template_type`, `prompt_template`, `dimension_template`, `response_template`, `language`, `variables`, `default_values`, `created_by`, `created_at`, `updated_at`) VALUES
('标准详细审核模板', 'STANDARD_DETAILED_TEMPLATE', '用于详细审核分析的标准中文模板', 'DETAILED', 
'请对以下内容进行安全审核，评估其是否存在违规风险。

审核内容：
{{content}}

请从以下维度进行评估：
{{dimensions}}

请返回JSON格式的审核结果，包含：
{
  "result": "APPROVED/REJECTED/NEEDS_REVIEW",
  "riskLevel": "LOW/MEDIUM/HIGH/CRITICAL", 
  "confidence": 0.85,
  "violations": [
    {
      "category": "违规分类",
      "confidence": 0.9,
      "reason": "具体原因"
    }
  ],
  "reasoning": "详细分析说明"
}',
'{{index}}. {{name}} - {{description}} {{#if guideline}}({{guideline}}){{/if}}',
'{"result": "string", "riskLevel": "string", "confidence": "number", "violations": "array", "reasoning": "string"}',
'zh-CN',
'{"content": "待审核内容", "dimensions": "审核维度列表"}',
'{"language": "zh-CN", "response_format": "json"}',
'system', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),

('快速预检模板', 'QUICK_PRECHECK_TEMPLATE', '用于快速安全预检的简化模板', 'QUICK',
'请对以下内容进行快速安全检查，判断是否需要进一步审核。

内容：{{content}}

请只返回：
- "SAFE" - 内容安全，可以通过
- "UNSAFE" - 内容可能存在风险，需要详细审核  
- "BLOCKED" - 内容明显违规，直接阻断

只返回一个词，不要额外说明。',
'',
'"SAFE" | "UNSAFE" | "BLOCKED"',
'zh-CN',
'{"content": "待审核内容"}',
'{"response_format": "simple"}',
'system', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000);

-- 关联策略与模板
UPDATE `t_moderation_policy` SET `template_id` = (SELECT id FROM t_moderation_policy_template WHERE code = 'STANDARD_DETAILED_TEMPLATE' LIMIT 1) WHERE code IN ('STANDARD_USER_CHAT', 'STRICT_CONTENT_PUBLISH');
UPDATE `t_moderation_policy` SET `template_id` = (SELECT id FROM t_moderation_policy_template WHERE code = 'QUICK_PRECHECK_TEMPLATE' LIMIT 1) WHERE code = 'LENIENT_BOT_REPLY';