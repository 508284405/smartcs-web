-- 内容审核模块 - 关键词规则表
CREATE TABLE IF NOT EXISTS `t_moderation_keyword_rule` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `rule_name` VARCHAR(128) NOT NULL COMMENT '规则名称',
  `keyword` VARCHAR(256) NOT NULL COMMENT '关键词或正则表达式',
  `category_id` BIGINT NOT NULL COMMENT '关联的违规分类ID',
  `rule_type` VARCHAR(32) NOT NULL COMMENT '规则类型 EXACT/FUZZY/REGEX/SUBSTRING',
  `match_mode` VARCHAR(32) NOT NULL COMMENT '匹配模式 FULL/PARTIAL/WORD_BOUNDARY',
  `case_sensitive` TINYINT DEFAULT 0 COMMENT '是否大小写敏感 1=敏感 0=不敏感',
  `severity_weight` DECIMAL(3,2) DEFAULT 1.00 COMMENT '严重程度权重 0.1-9.99',
  
  -- 匹配配置
  `similarity_threshold` DECIMAL(3,2) COMMENT '相似度阈值（用于模糊匹配）0.00-1.00',
  `context_window` INT DEFAULT 0 COMMENT '上下文窗口大小（字符数）',
  `whitelist_contexts` JSON COMMENT '白名单上下文，在这些上下文中不触发规则',
  
  -- 规则状态和优先级
  `is_active` TINYINT DEFAULT 1 COMMENT '是否启用 1=启用 0=禁用',
  `priority` INT DEFAULT 100 COMMENT '优先级，数字越小优先级越高',
  `action_override` VARCHAR(32) COMMENT '动作覆盖 WARN/REVIEW/BLOCK/ESCALATE，NULL使用分类默认动作',
  
  -- 统计信息
  `hit_count` BIGINT DEFAULT 0 COMMENT '命中次数',
  `last_hit_at` BIGINT COMMENT '最后命中时间',
  
  -- 规则元数据
  `description` VARCHAR(512) COMMENT '规则描述',
  `source` VARCHAR(64) COMMENT '规则来源 SYSTEM/MANUAL/IMPORT/AI_GENERATED',
  `language` VARCHAR(16) DEFAULT 'zh' COMMENT '适用语言 zh/en/auto',
  `tags` VARCHAR(256) COMMENT '标签，用逗号分隔',
  
  -- 有效期
  `effective_from` BIGINT COMMENT '生效时间',
  `effective_until` BIGINT COMMENT '失效时间',
  
  -- 系统字段
  `created_by` VARCHAR(64) COMMENT '创建者',
  `updated_by` VARCHAR(64) COMMENT '更新者',
  `created_at` BIGINT NOT NULL COMMENT '创建时间（毫秒时间戳）',
  `updated_at` BIGINT NOT NULL COMMENT '更新时间（毫秒时间戳）',
  
  -- 索引
  INDEX idx_category_id (`category_id`),
  INDEX idx_rule_type (`rule_type`),
  INDEX idx_is_active (`is_active`),
  INDEX idx_priority (`priority`),
  INDEX idx_language (`language`),
  INDEX idx_effective_time (`effective_from`, `effective_until`),
  INDEX idx_hit_count (`hit_count`),
  
  -- 外键约束
  FOREIGN KEY (category_id) REFERENCES t_moderation_category(id) ON DELETE CASCADE ON UPDATE CASCADE
) COMMENT '内容审核关键词规则表';

-- 插入默认关键词规则示例
INSERT INTO `t_moderation_keyword_rule` (`rule_name`, `keyword`, `category_id`, `rule_type`, `match_mode`, `case_sensitive`, `severity_weight`, `description`, `source`, `language`, `created_by`, `created_at`, `updated_at`) VALUES

-- 仇恨言论相关关键词
('种族歧视关键词-1', '(黑鬼|尼哥|猴子)', (SELECT id FROM t_moderation_category WHERE code = 'RACIAL_DISCRIMINATION'), 'REGEX', 'PARTIAL', 0, 3.0, '种族歧视性贬义词汇', 'SYSTEM', 'zh', 'system', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('性别歧视关键词-1', '(婊子|贱人|臭女人)', (SELECT id FROM t_moderation_category WHERE code = 'GENDER_DISCRIMINATION'), 'REGEX', 'PARTIAL', 0, 2.5, '性别歧视性贬义词汇', 'SYSTEM', 'zh', 'system', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),

-- 暴力威胁相关关键词  
('人身威胁关键词-1', '(杀死你|弄死你|干掉你)', (SELECT id FROM t_moderation_category WHERE code = 'PERSONAL_THREATS'), 'REGEX', 'PARTIAL', 0, 3.5, '人身威胁性词汇', 'SYSTEM', 'zh', 'system', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('暴力行为关键词-1', '(砍死|捅死|炸死)', (SELECT id FROM t_moderation_category WHERE code = 'VIOLENCE'), 'REGEX', 'PARTIAL', 0, 3.0, '暴力行为描述词汇', 'SYSTEM', 'zh', 'system', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),

-- 自我伤害相关关键词
('自杀关键词-1', '(想自杀|要自杀|自杀方法)', (SELECT id FROM t_moderation_category WHERE code = 'SELF_HARM'), 'REGEX', 'PARTIAL', 0, 4.0, '自杀相关表达', 'SYSTEM', 'zh', 'system', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('自伤关键词-1', '(割腕|跳楼|上吊)', (SELECT id FROM t_moderation_category WHERE code = 'SELF_HARM'), 'REGEX', 'PARTIAL', 0, 3.5, '自伤方式描述', 'SYSTEM', 'zh', 'system', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),

-- 性内容相关关键词
('明确性内容-1', '(做爱|性交|啪啪)', (SELECT id FROM t_moderation_category WHERE code = 'SEXUAL_CONTENT'), 'REGEX', 'PARTIAL', 0, 2.0, '明确性行为描述', 'SYSTEM', 'zh', 'system', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),

-- 垃圾信息关键词
('广告推广-1', '(加微信|扫码|优惠券)', (SELECT id FROM t_moderation_category WHERE code = 'SPAM'), 'REGEX', 'PARTIAL', 0, 1.5, '常见广告推广用词', 'SYSTEM', 'zh', 'system', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('联系方式-1', '([1-9]\d{10}|QQ:\d+)', (SELECT id FROM t_moderation_category WHERE code = 'SPAM'), 'REGEX', 'PARTIAL', 0, 1.0, '可疑联系方式', 'SYSTEM', 'zh', 'system', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),

-- 隐私信息关键词
('身份证号', '\d{17}[\dXx]', (SELECT id FROM t_moderation_category WHERE code = 'PRIVACY_VIOLATION'), 'REGEX', 'FULL', 0, 2.5, '身份证号码格式', 'SYSTEM', 'zh', 'system', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('银行卡号', '\d{16,19}', (SELECT id FROM t_moderation_category WHERE code = 'PRIVACY_VIOLATION'), 'REGEX', 'FULL', 0, 2.5, '银行卡号格式', 'SYSTEM', 'zh', 'system', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),

-- 英文关键词示例
('Hate Speech EN-1', '(nazi|kkk|white supremacy)', (SELECT id FROM t_moderation_category WHERE code = 'HATE_SPEECH'), 'REGEX', 'PARTIAL', 0, 3.0, 'English hate speech terms', 'SYSTEM', 'en', 'system', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('Threats EN-1', '(kill you|murder you|die)', (SELECT id FROM t_moderation_category WHERE code = 'PERSONAL_THREATS'), 'REGEX', 'PARTIAL', 0, 3.5, 'English threat expressions', 'SYSTEM', 'en', 'system', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000);

-- 创建keyword字段的全文索引（如果需要全文搜索）
-- ALTER TABLE t_moderation_keyword_rule ADD FULLTEXT(keyword);