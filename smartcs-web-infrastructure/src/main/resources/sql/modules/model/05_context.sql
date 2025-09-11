-- 模型模块 - 上下文表
CREATE TABLE IF NOT EXISTS `t_model_context` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    `session_id` VARCHAR(128) NOT NULL UNIQUE COMMENT '会话ID',
    `model_id` BIGINT NOT NULL COMMENT '模型ID',
    `messages` LONGTEXT COMMENT '上下文消息列表（JSON格式）',
    `context_window` INT COMMENT '上下文窗口大小',
    `current_length` INT DEFAULT 0 COMMENT '当前上下文长度',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '软删除标志',
    `created_by` VARCHAR(64) COMMENT '创建者',
    `updated_by` VARCHAR(64) COMMENT '更新者',
    `created_at` BIGINT COMMENT '创建时间（毫秒时间戳）',
    `updated_at` BIGINT COMMENT '最后更新时间（毫秒时间戳）',
    INDEX `idx_session_id` (`session_id`),
    INDEX `idx_model_id` (`model_id`),
    INDEX `idx_created_at` (`created_at`),
    INDEX `idx_updated_at` (`updated_at`),
    CONSTRAINT `fk_context_model_id` FOREIGN KEY (`model_id`) REFERENCES `t_model`(`id`)
) COMMENT '模型上下文表';