-- 内容审核模块 - 违规分类表（支持二级分类）
CREATE TABLE IF NOT EXISTS `t_moderation_category` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `parent_id` BIGINT COMMENT '父分类ID，NULL为一级分类',
  `name` VARCHAR(64) NOT NULL COMMENT '分类名称',
  `code` VARCHAR(32) NOT NULL COMMENT '分类编码，用于程序识别',
  `description` VARCHAR(256) COMMENT '分类描述',
  `severity_level` VARCHAR(16) DEFAULT 'MEDIUM' COMMENT '严重程度 LOW/MEDIUM/HIGH/CRITICAL',
  `action_type` VARCHAR(32) DEFAULT 'BLOCK' COMMENT '处理动作 WARN/REVIEW/BLOCK/ESCALATE',
  `is_active` TINYINT DEFAULT 1 COMMENT '是否启用 1=启用 0=禁用',
  `sort_order` INT DEFAULT 0 COMMENT '排序权重',
  `created_by` VARCHAR(64) COMMENT '创建者',
  `updated_by` VARCHAR(64) COMMENT '更新者',
  `created_at` BIGINT NOT NULL COMMENT '创建时间（毫秒时间戳）',
  `updated_at` BIGINT NOT NULL COMMENT '更新时间（毫秒时间戳）',
  UNIQUE KEY uk_code (`code`),
  INDEX idx_parent_id (`parent_id`),
  INDEX idx_severity_level (`severity_level`),
  INDEX idx_is_active (`is_active`),
  INDEX idx_sort_order (`sort_order`)
) COMMENT '内容审核违规分类表';

-- 插入默认的一级分类
INSERT INTO `t_moderation_category` (`parent_id`, `name`, `code`, `description`, `severity_level`, `action_type`, `sort_order`, `created_by`, `created_at`, `updated_at`) VALUES
(NULL, '仇恨言论', 'HATE_SPEECH', '基于种族、性别、宗教等特征的仇恨或歧视性言论', 'HIGH', 'BLOCK', 1, 'system', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
(NULL, '骚扰威胁', 'HARASSMENT', '对个人或群体的骚扰、威胁、恐吓行为', 'HIGH', 'BLOCK', 2, 'system', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
(NULL, '性内容', 'SEXUAL_CONTENT', '明确的性内容或成人内容', 'MEDIUM', 'BLOCK', 3, 'system', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
(NULL, '暴力内容', 'VIOLENCE', '描述或宣扬暴力行为的内容', 'HIGH', 'BLOCK', 4, 'system', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
(NULL, '自我伤害', 'SELF_HARM', '鼓励或描述自我伤害、自杀的内容', 'CRITICAL', 'ESCALATE', 5, 'system', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
(NULL, '危险活动', 'DANGEROUS_ACTIVITIES', '危险、非法或有害活动的指导', 'HIGH', 'BLOCK', 6, 'system', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
(NULL, '垃圾信息', 'SPAM', '垃圾信息、重复内容或恶意链接', 'LOW', 'WARN', 7, 'system', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
(NULL, '隐私信息', 'PRIVACY_VIOLATION', '包含个人隐私信息或敏感数据', 'MEDIUM', 'REVIEW', 8, 'system', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000);

-- 插入二级分类示例（仇恨言论子分类）
INSERT INTO `t_moderation_category` (`parent_id`, `name`, `code`, `description`, `severity_level`, `action_type`, `sort_order`, `created_by`, `created_at`, `updated_at`) VALUES
((SELECT id FROM t_moderation_category WHERE code = 'HATE_SPEECH'), '种族歧视', 'RACIAL_DISCRIMINATION', '基于种族或民族的歧视性言论', 'HIGH', 'BLOCK', 1, 'system', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
((SELECT id FROM t_moderation_category WHERE code = 'HATE_SPEECH'), '性别歧视', 'GENDER_DISCRIMINATION', '基于性别或性取向的歧视性言论', 'HIGH', 'BLOCK', 2, 'system', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
((SELECT id FROM t_moderation_category WHERE code = 'HATE_SPEECH'), '宗教歧视', 'RELIGIOUS_DISCRIMINATION', '基于宗教信仰的歧视性言论', 'HIGH', 'BLOCK', 3, 'system', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000);

-- 插入二级分类示例（骚扰威胁子分类）
INSERT INTO `t_moderation_category` (`parent_id`, `name`, `code`, `description`, `severity_level`, `action_type`, `sort_order`, `created_by`, `created_at`, `updated_at`) VALUES
((SELECT id FROM t_moderation_category WHERE code = 'HARASSMENT'), '网络霸凌', 'CYBERBULLYING', '在线欺凌、羞辱或恶意攻击他人', 'HIGH', 'BLOCK', 1, 'system', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
((SELECT id FROM t_moderation_category WHERE code = 'HARASSMENT'), '人身威胁', 'PERSONAL_THREATS', '对个人或其家人的威胁或恐吓', 'CRITICAL', 'ESCALATE', 2, 'system', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
((SELECT id FROM t_moderation_category WHERE code = 'HARASSMENT'), '跟踪骚扰', 'STALKING', '持续的跟踪或骚扰行为', 'HIGH', 'BLOCK', 3, 'system', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000);