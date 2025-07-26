-- 知识模块 - 切片表
CREATE TABLE IF NOT EXISTS `t_kb_chunk` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `content_id` BIGINT NOT NULL COMMENT '内容ID',
  `chunk_index` VARCHAR(255) NOT NULL COMMENT '段落序号',
  `token_size` INT DEFAULT 0 COMMENT '切片token数',
  `content` TEXT NOT NULL COMMENT '切片内容文本',
  `metadata` JSON COMMENT '附加元信息，如页码、起止时间、原始位置等',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
  `created_by` VARCHAR(64) COMMENT '创建者',
  `updated_by` VARCHAR(64) COMMENT '更新者',
  `created_at` BIGINT COMMENT '创建时间',
  `updated_at` BIGINT COMMENT '更新时间',
  INDEX idx_content_id (`content_id`),
  INDEX idx_chunk_index (`chunk_index`)
) COMMENT '内容切片表';