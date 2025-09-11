-- 知识模块 - 向量表
CREATE TABLE IF NOT EXISTS `t_kb_vector` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `chunk_id` BIGINT NOT NULL COMMENT '切片ID',
  `embedding` BLOB COMMENT '向量数据，float[]序列化后存储',
  `dim` INT DEFAULT 768 COMMENT '维度大小',
  `provider` VARCHAR(64) DEFAULT 'bge' COMMENT 'embedding提供方，如openai/bge',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
  `created_by` VARCHAR(64) COMMENT '创建者',
  `updated_by` VARCHAR(64) COMMENT '更新者',
  `created_at` BIGINT COMMENT '创建时间',
  `updated_at` BIGINT COMMENT '更新时间',
  UNIQUE INDEX idx_chunk_id (`chunk_id`)
) COMMENT '向量表';