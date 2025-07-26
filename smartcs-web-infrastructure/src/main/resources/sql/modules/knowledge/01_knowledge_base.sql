-- 知识模块 - 知识库表
CREATE TABLE IF NOT EXISTS `t_kb_knowledge_base` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `name` VARCHAR(128) NOT NULL COMMENT '知识库名称',
  `code` VARCHAR(64) UNIQUE NOT NULL COMMENT '知识库唯一编码',
  `description` TEXT COMMENT '描述信息',
  `owner_id` BIGINT NOT NULL COMMENT '创建者ID',
  `visibility` VARCHAR(16) DEFAULT 'private' COMMENT 'public/private',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
  `created_by` VARCHAR(64) COMMENT '创建者',
  `updated_by` VARCHAR(64) COMMENT '更新者',
  `created_at` BIGINT COMMENT '创建时间',
  `updated_at` BIGINT COMMENT '更新时间',
  INDEX idx_owner_id (`owner_id`),
  INDEX idx_visibility (`visibility`),
  INDEX idx_code (`code`)
) COMMENT '知识库表';