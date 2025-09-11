-- 知识库设置表迁移脚本
-- 为知识库增加详细的设置选项，支持索引模式、嵌入模型和检索配置

CREATE TABLE IF NOT EXISTS `t_kb_knowledge_base_settings` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  `knowledge_base_id` BIGINT UNIQUE NOT NULL COMMENT '知识库ID',
  `indexing_mode` VARCHAR(32) NOT NULL DEFAULT 'high_quality' COMMENT '索引模式：high_quality(高质量)、economy(经济模式)',
  `embedding_model` VARCHAR(128) NOT NULL DEFAULT '' COMMENT '嵌入模型名称',
  `vector_enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '向量搜索是否启用：0-禁用，1-启用',
  `vector_top_k` INT NOT NULL DEFAULT 10 COMMENT '向量搜索返回条数',
  `vector_score_threshold` DECIMAL(10,6) NOT NULL DEFAULT 0.0 COMMENT '向量搜索相似度阈值',
  `full_text_enabled` TINYINT NOT NULL DEFAULT 0 COMMENT '全文搜索是否启用：0-禁用，1-启用',
  `hybrid_enabled` TINYINT NOT NULL DEFAULT 0 COMMENT '混合搜索是否启用：0-禁用，1-启用',
  `hybrid_rerank_enabled` TINYINT NOT NULL DEFAULT 0 COMMENT '混合搜索重排是否启用：0-禁用，1-启用',
  `created_by` VARCHAR(64) COMMENT '创建者',
  `updated_by` VARCHAR(64) COMMENT '更新者',
  `created_at` BIGINT COMMENT '创建时间（毫秒时间戳）',
  `updated_at` BIGINT COMMENT '更新时间（毫秒时间戳）',
  UNIQUE INDEX unq_knowledge_base_id (`knowledge_base_id`),
  FOREIGN KEY (`knowledge_base_id`) REFERENCES `t_kb_knowledge_base`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='知识库设置表';