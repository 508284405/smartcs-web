-- 知识模块 - 用户知识库权限关系表
CREATE TABLE IF NOT EXISTS `t_kb_user_kb_rel` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `knowledge_base_id` BIGINT NOT NULL COMMENT '知识库ID',
  `role` VARCHAR(32) DEFAULT 'reader' COMMENT 'reader/writer/admin',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
  `created_by` VARCHAR(64) COMMENT '创建者',
  `updated_by` VARCHAR(64) COMMENT '更新者',
  `created_at` BIGINT COMMENT '创建时间',
  `updated_at` BIGINT COMMENT '更新时间',
  UNIQUE INDEX idx_user_knowledge_base (`user_id`, `knowledge_base_id`)
) COMMENT '用户知识库权限关系表';