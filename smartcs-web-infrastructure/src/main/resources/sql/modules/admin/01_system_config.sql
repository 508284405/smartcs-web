-- 管理模块 - 系统配置表
CREATE TABLE IF NOT EXISTS `t_system_config` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    `config_key` VARCHAR(128) NOT NULL UNIQUE COMMENT '配置键',
    `config_value` TEXT COMMENT '配置值',
    `config_type` VARCHAR(32) DEFAULT 'STRING' COMMENT '配置类型 STRING/NUMBER/BOOLEAN/JSON',
    `description` VARCHAR(512) COMMENT '配置描述',
    `category` VARCHAR(64) DEFAULT 'GENERAL' COMMENT '配置分类',
    `is_encrypted` BOOLEAN DEFAULT FALSE COMMENT '是否加密存储',
    `is_readonly` BOOLEAN DEFAULT FALSE COMMENT '是否只读',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '软删除标志',
    `created_by` VARCHAR(64) COMMENT '创建者',
    `updated_by` VARCHAR(64) COMMENT '更新者',
    `created_at` BIGINT COMMENT '创建时间（毫秒时间戳）',
    `updated_at` BIGINT COMMENT '更新时间（毫秒时间戳）',
    INDEX `idx_config_key` (`config_key`),
    INDEX `idx_category` (`category`),
    INDEX `idx_created_at` (`created_at`)
) COMMENT '系统配置表';

-- 插入系统默认配置
INSERT INTO `t_system_config` 
(`config_key`, `config_value`, `config_type`, `description`, `category`, `is_readonly`, `created_by`, `created_at`, `updated_at`)
VALUES 
('system.name', 'SmartCS', 'STRING', '系统名称', 'GENERAL', FALSE, 'system', UNIX_TIMESTAMP()*1000, UNIX_TIMESTAMP()*1000),
('system.version', '1.0.0', 'STRING', '系统版本', 'GENERAL', TRUE, 'system', UNIX_TIMESTAMP()*1000, UNIX_TIMESTAMP()*1000),
('chat.max_session_duration', '86400000', 'NUMBER', '最大会话持续时间（毫秒）', 'CHAT', FALSE, 'system', UNIX_TIMESTAMP()*1000, UNIX_TIMESTAMP()*1000),
('knowledge.max_file_size', '10485760', 'NUMBER', '知识库文件最大大小（字节）', 'KNOWLEDGE', FALSE, 'system', UNIX_TIMESTAMP()*1000, UNIX_TIMESTAMP()*1000),
('model.default_context_window', '4096', 'NUMBER', '默认上下文窗口大小', 'MODEL', FALSE, 'system', UNIX_TIMESTAMP()*1000, UNIX_TIMESTAMP()*1000);