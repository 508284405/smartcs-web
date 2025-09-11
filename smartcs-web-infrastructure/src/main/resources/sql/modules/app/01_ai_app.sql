-- APP模块 - AI应用表
CREATE TABLE IF NOT EXISTS `t_ai_app` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `name` VARCHAR(128) NOT NULL COMMENT '应用名称',  
  `code` VARCHAR(64) UNIQUE NOT NULL COMMENT '应用唯一编码',
  `description` TEXT COMMENT '应用描述',
  `type` VARCHAR(32) NOT NULL COMMENT '应用类型: WORKFLOW/CHATFLOW/CHAT_ASSISTANT/AGENT',
  `config` JSON COMMENT '应用配置信息',
  `status` VARCHAR(16) DEFAULT 'DRAFT' COMMENT '应用状态: DRAFT/PUBLISHED/DISABLED',
  `icon` VARCHAR(255) COMMENT '应用图标',
  `tags` JSON COMMENT '应用标签',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
  `created_by` VARCHAR(64) COMMENT '创建者',
  `updated_by` VARCHAR(64) COMMENT '更新者', 
  `created_at` BIGINT COMMENT '创建时间',
  `updated_at` BIGINT COMMENT '更新时间',
  INDEX idx_creator_id (`creator_id`),
  INDEX idx_type (`type`),
  INDEX idx_status (`status`),
  INDEX idx_code (`code`),
  INDEX idx_created_at (`created_at`)
) COMMENT 'AI应用表';

