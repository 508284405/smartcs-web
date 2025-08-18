-- 意图模块 - 意图表
CREATE TABLE IF NOT EXISTS `t_intent` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `catalog_id` BIGINT NOT NULL COMMENT '目录ID',
  `name` VARCHAR(128) NOT NULL COMMENT '意图名称',
  `code` VARCHAR(64) UNIQUE NOT NULL COMMENT '意图编码',
  `description` TEXT COMMENT '意图描述',
  `labels` JSON COMMENT '标签数组',
  `boundaries` JSON COMMENT '边界定义',
  `current_version_id` BIGINT COMMENT '当前活跃版本ID',
  `status` VARCHAR(32) DEFAULT 'DRAFT' COMMENT 'DRAFT/ACTIVE/DEPRECATED',
  `creator_id` BIGINT NOT NULL COMMENT '创建者ID',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
  `created_by` VARCHAR(64) COMMENT '创建者',
  `updated_by` VARCHAR(64) COMMENT '更新者',
  `created_at` BIGINT COMMENT '创建时间',
  `updated_at` BIGINT COMMENT '更新时间',
  INDEX idx_catalog_id (`catalog_id`),
  INDEX idx_status (`status`),
  INDEX idx_creator_id (`creator_id`),
  INDEX idx_code (`code`),
  INDEX idx_current_version_id (`current_version_id`),
  CONSTRAINT fk_intent_catalog FOREIGN KEY (`catalog_id`) REFERENCES `t_intent_catalog`(`id`)
) COMMENT '意图表';