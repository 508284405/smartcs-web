-- 内容审核模块 - 审核配置表
CREATE TABLE IF NOT EXISTS `t_moderation_config` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `config_key` VARCHAR(64) NOT NULL COMMENT '配置键，用于程序识别',
  `config_name` VARCHAR(128) NOT NULL COMMENT '配置名称',
  `config_value` TEXT COMMENT '配置值，可以是JSON格式的复杂配置',
  `config_type` VARCHAR(32) NOT NULL COMMENT '配置类型 BOOLEAN/INTEGER/STRING/JSON/DECIMAL',
  `description` VARCHAR(512) COMMENT '配置描述',
  `category` VARCHAR(64) COMMENT '配置分类 GENERAL/AI_MODEL/KEYWORD_FILTER/THRESHOLD/CACHE',
  `is_active` TINYINT DEFAULT 1 COMMENT '是否启用 1=启用 0=禁用',
  `is_system` TINYINT DEFAULT 0 COMMENT '是否系统配置 1=系统配置 0=用户配置',
  `validation_rule` VARCHAR(256) COMMENT '验证规则，如数值范围、正则表达式等',
  `default_value` TEXT COMMENT '默认值',
  `created_by` VARCHAR(64) COMMENT '创建者',
  `updated_by` VARCHAR(64) COMMENT '更新者',
  `created_at` BIGINT NOT NULL COMMENT '创建时间（毫秒时间戳）',
  `updated_at` BIGINT NOT NULL COMMENT '更新时间（毫秒时间戳）',
  
  UNIQUE KEY uk_config_key (`config_key`),
  INDEX idx_category (`category`),
  INDEX idx_is_active (`is_active`),
  INDEX idx_is_system (`is_system`)
) COMMENT '内容审核配置表';

-- 插入默认配置
INSERT INTO `t_moderation_config` (`config_key`, `config_name`, `config_value`, `config_type`, `description`, `category`, `is_system`, `validation_rule`, `default_value`, `created_by`, `created_at`, `updated_at`) VALUES

-- 通用配置
('moderation.enabled', '内容审核总开关', 'true', 'BOOLEAN', '是否启用内容审核功能', 'GENERAL', 1, NULL, 'true', 'system', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('moderation.default_action', '默认处理动作', 'BLOCK', 'STRING', '当审核结果为违规时的默认处理动作', 'GENERAL', 1, 'WARN|REVIEW|BLOCK|ESCALATE', 'BLOCK', 'system', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('moderation.max_content_length', '最大内容长度', '10000', 'INTEGER', '单次审核内容的最大字符数', 'GENERAL', 1, '100-50000', '10000', 'system', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),

-- AI模型配置
('moderation.ai.enabled', 'AI审核开关', 'true', 'BOOLEAN', '是否启用AI审核功能', 'AI_MODEL', 1, NULL, 'true', 'system', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('moderation.ai.model_name', 'AI审核模型', 'gpt-3.5-turbo', 'STRING', '用于内容审核的AI模型名称', 'AI_MODEL', 1, NULL, 'gpt-3.5-turbo', 'system', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('moderation.ai.timeout_ms', 'AI审核超时时间', '5000', 'INTEGER', 'AI审核请求的超时时间（毫秒）', 'AI_MODEL', 1, '1000-30000', '5000', 'system', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('moderation.ai.retry_times', 'AI审核重试次数', '2', 'INTEGER', 'AI审核失败时的重试次数', 'AI_MODEL', 1, '0-5', '2', 'system', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),

-- 关键词过滤配置
('moderation.keyword.enabled', '关键词过滤开关', 'true', 'BOOLEAN', '是否启用关键词过滤功能', 'KEYWORD_FILTER', 1, NULL, 'true', 'system', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('moderation.keyword.case_sensitive', '关键词大小写敏感', 'false', 'BOOLEAN', '关键词匹配是否大小写敏感', 'KEYWORD_FILTER', 1, NULL, 'false', 'system', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('moderation.keyword.fuzzy_match', '关键词模糊匹配', 'true', 'BOOLEAN', '是否启用关键词模糊匹配', 'KEYWORD_FILTER', 1, NULL, 'true', 'system', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('moderation.keyword.similarity_threshold', '相似度阈值', '0.8', 'DECIMAL', '模糊匹配的相似度阈值', 'KEYWORD_FILTER', 1, '0.1-1.0', '0.8', 'system', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),

-- 阈值配置
('moderation.threshold.low_risk', '低风险阈值', '0.3', 'DECIMAL', '低风险等级的置信度阈值', 'THRESHOLD', 1, '0.0-1.0', '0.3', 'system', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('moderation.threshold.medium_risk', '中风险阈值', '0.6', 'DECIMAL', '中风险等级的置信度阈值', 'THRESHOLD', 1, '0.0-1.0', '0.6', 'system', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('moderation.threshold.high_risk', '高风险阈值', '0.8', 'DECIMAL', '高风险等级的置信度阈值', 'THRESHOLD', 1, '0.0-1.0', '0.8', 'system', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('moderation.threshold.critical_risk', '极高风险阈值', '0.95', 'DECIMAL', '极高风险等级的置信度阈值', 'THRESHOLD', 1, '0.0-1.0', '0.95', 'system', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),

-- 缓存配置
('moderation.cache.enabled', '缓存开关', 'true', 'BOOLEAN', '是否启用审核结果缓存', 'CACHE', 1, NULL, 'true', 'system', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('moderation.cache.ttl_seconds', '缓存过期时间', '3600', 'INTEGER', '审核结果缓存的过期时间（秒）', 'CACHE', 1, '60-86400', '3600', 'system', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('moderation.cache.max_entries', '最大缓存条目数', '10000', 'INTEGER', '最大缓存条目数量', 'CACHE', 1, '1000-100000', '10000', 'system', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),

-- 异步处理配置  
('moderation.async.enabled', '异步处理开关', 'true', 'BOOLEAN', '是否启用异步处理模式', 'GENERAL', 1, NULL, 'true', 'system', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('moderation.async.thread_pool_size', '异步线程池大小', '10', 'INTEGER', '异步处理的线程池大小', 'GENERAL', 1, '1-50', '10', 'system', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('moderation.async.queue_capacity', '异步队列容量', '1000', 'INTEGER', '异步处理队列的容量', 'GENERAL', 1, '100-10000', '1000', 'system', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),

-- 监控和日志配置
('moderation.monitoring.enabled', '监控开关', 'true', 'BOOLEAN', '是否启用审核监控功能', 'GENERAL', 1, NULL, 'true', 'system', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('moderation.logging.level', '日志级别', 'INFO', 'STRING', '审核模块的日志级别', 'GENERAL', 1, 'ERROR|WARN|INFO|DEBUG', 'INFO', 'system', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000),
('moderation.logging.detailed', '详细日志', 'false', 'BOOLEAN', '是否记录详细的审核日志', 'GENERAL', 1, NULL, 'false', 'system', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000);