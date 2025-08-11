-- RAG评估检索详情表
-- 用于存储每个测试用例的检索详细结果
CREATE TABLE `t_rag_eval_retrieval_detail` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '详情记录ID',
  `run_id` VARCHAR(64) NOT NULL COMMENT '运行ID',
  `case_id` VARCHAR(64) NOT NULL COMMENT '测试用例ID',
  `query_original` TEXT NOT NULL COMMENT '原始查询',
  `query_rewritten` TEXT COMMENT '重写后的查询',
  `query_embedding_time_ms` INT COMMENT '查询向量化耗时（毫秒）',
  
  -- 检索阶段结果
  `retrieval_candidates` JSON COMMENT '检索候选结果（TopK文档，包含ID、文本片段、向量相似度分数）',
  `retrieval_scores` JSON COMMENT '检索分数详情',
  `retrieval_latency_ms` INT COMMENT '检索延迟（毫秒）',
  `retrieval_model_used` VARCHAR(128) COMMENT '使用的检索模型',
  `retrieval_params` JSON COMMENT '检索参数（TopK、阈值等）',
  
  -- 重排序阶段结果
  `rerank_candidates` JSON COMMENT '重排序后的候选结果',
  `rerank_scores` JSON COMMENT '重排序分数',
  `rerank_latency_ms` INT COMMENT '重排序延迟（毫秒）',
  `rerank_model_used` VARCHAR(128) COMMENT '使用的重排序模型',
  `rerank_params` JSON COMMENT '重排序参数',
  `rerank_improvement` DECIMAL(5,4) COMMENT '重排序改进程度',
  
  -- 过滤和处理
  `final_contexts` JSON COMMENT '最终选择的上下文',
  `filtered_candidates` JSON COMMENT '过滤掉的候选项',
  `context_compression_ratio` DECIMAL(5,4) COMMENT '上下文压缩比例',
  
  -- 评估指标（针对此用例）
  `precision_at_1` DECIMAL(5,4) COMMENT '单样本Precision@1',
  `precision_at_3` DECIMAL(5,4) COMMENT '单样本Precision@3',
  `precision_at_5` DECIMAL(5,4) COMMENT '单样本Precision@5',
  `recall_at_1` DECIMAL(5,4) COMMENT '单样本Recall@1',
  `recall_at_3` DECIMAL(5,4) COMMENT '单样本Recall@3',
  `recall_at_5` DECIMAL(5,4) COMMENT '单样本Recall@5',
  `reciprocal_rank` DECIMAL(5,4) COMMENT '倒数排名',
  `ndcg` DECIMAL(5,4) COMMENT 'NDCG分数',
  
  -- RAGAS特定指标
  `context_precision_score` DECIMAL(5,4) COMMENT 'RAGAS Context Precision分数',
  `context_recall_score` DECIMAL(5,4) COMMENT 'RAGAS Context Recall分数',
  
  -- 错误和异常
  `has_error` TINYINT DEFAULT 0 COMMENT '是否有错误：0-否，1-是',
  `error_message` TEXT COMMENT '错误信息',
  `error_stage` VARCHAR(32) COMMENT '错误阶段：embedding, retrieval, rerank, filter',
  
  -- 元数据和调试信息
  `debug_info` JSON COMMENT '调试信息',
  `metadata` JSON COMMENT '扩展元数据',
  `created_at` BIGINT NOT NULL COMMENT '创建时间（毫秒时间戳）',
  
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_run_case` (`run_id`, `case_id`),
  KEY `idx_run_id` (`run_id`),
  KEY `idx_case_id` (`case_id`),
  KEY `idx_has_error` (`has_error`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `fk_retrieval_detail_run` FOREIGN KEY (`run_id`) REFERENCES `t_rag_eval_run` (`run_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_retrieval_detail_case` FOREIGN KEY (`case_id`) REFERENCES `t_rag_eval_case` (`case_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='RAG评估检索详情表';