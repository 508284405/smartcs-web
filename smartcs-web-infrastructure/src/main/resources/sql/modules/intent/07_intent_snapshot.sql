-- 意图模块 - 意图快照表
CREATE TABLE IF NOT EXISTS `t_intent_snapshot` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `name` VARCHAR(128) NOT NULL COMMENT '快照名称',
  `code` VARCHAR(64) UNIQUE NOT NULL COMMENT '快照编码',
  `scope` VARCHAR(64) DEFAULT 'global' COMMENT '作用域',
  `scope_selector` JSON COMMENT '作用域选择器',
  `status` VARCHAR(32) DEFAULT 'DRAFT' COMMENT 'DRAFT/PUBLISHED/ACTIVE/ROLLBACK/ARCHIVED',
  `etag` VARCHAR(128) COMMENT 'ETag',
  `created_by_id` BIGINT COMMENT '创建者ID',
  `published_by_id` BIGINT COMMENT '发布者ID',
  `published_at` BIGINT COMMENT '发布时间',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
  `created_by` VARCHAR(64) COMMENT '创建者',
  `updated_by` VARCHAR(64) COMMENT '更新者',
  `created_at` BIGINT COMMENT '创建时间',
  `updated_at` BIGINT COMMENT '更新时间',
  INDEX idx_status (`status`),
  INDEX idx_scope (`scope`),
  INDEX idx_created_by_id (`created_by_id`),
  INDEX idx_published_by_id (`published_by_id`),
  INDEX idx_code (`code`),
  INDEX idx_etag (`etag`)
) COMMENT '意图快照表';