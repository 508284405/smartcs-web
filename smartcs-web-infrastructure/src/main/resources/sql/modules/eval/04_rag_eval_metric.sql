-- RAG评估指标汇总表
-- 用于存储评估运行的聚合指标结果
CREATE TABLE `t_rag_eval_metric` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '指标记录ID',
  `run_id` VARCHAR(64) NOT NULL COMMENT '运行ID',
  `metric_category` VARCHAR(32) NOT NULL COMMENT '指标类别：retrieval, generation, efficiency, robustness',
  
  -- 检索指标
  `precision_at_1` DECIMAL(5,4) COMMENT 'Precision@1',
  `precision_at_3` DECIMAL(5,4) COMMENT 'Precision@3',
  `precision_at_5` DECIMAL(5,4) COMMENT 'Precision@5',
  `recall_at_1` DECIMAL(5,4) COMMENT 'Recall@1',
  `recall_at_3` DECIMAL(5,4) COMMENT 'Recall@3',
  `recall_at_5` DECIMAL(5,4) COMMENT 'Recall@5',
  `mrr` DECIMAL(5,4) COMMENT '平均倒数排名（Mean Reciprocal Rank）',
  `ndcg_at_3` DECIMAL(5,4) COMMENT 'NDCG@3',
  `ndcg_at_5` DECIMAL(5,4) COMMENT 'NDCG@5',
  `context_precision` DECIMAL(5,4) COMMENT 'RAGAS Context Precision',
  `context_recall` DECIMAL(5,4) COMMENT 'RAGAS Context Recall',
  `rerank_improvement` DECIMAL(5,4) COMMENT '重排序改进率',
  
  -- 生成指标
  `faithfulness` DECIMAL(5,4) COMMENT 'RAGAS Faithfulness（忠实度）',
  `answer_relevancy` DECIMAL(5,4) COMMENT 'RAGAS Answer Relevancy（答案相关性）',
  `citation_consistency` DECIMAL(5,4) COMMENT '引用一致性',
  `completeness` DECIMAL(5,4) COMMENT '完整性',
  `conciseness` DECIMAL(5,4) COMMENT '简洁性',
  `groundedness` DECIMAL(5,4) COMMENT '基于事实程度',
  `factual_correctness` DECIMAL(5,4) COMMENT '事实正确性',
  
  -- 效率指标
  `avg_retrieval_latency_ms` DECIMAL(10,2) COMMENT '平均检索延迟（毫秒）',
  `avg_rerank_latency_ms` DECIMAL(10,2) COMMENT '平均重排序延迟（毫秒）',
  `avg_generation_latency_ms` DECIMAL(10,2) COMMENT '平均生成延迟（毫秒）',
  `avg_e2e_latency_ms` DECIMAL(10,2) COMMENT '平均端到端延迟（毫秒）',
  `avg_input_tokens` DECIMAL(10,2) COMMENT '平均输入Token数',
  `avg_output_tokens` DECIMAL(10,2) COMMENT '平均输出Token数',
  `avg_cost_usd` DECIMAL(10,6) COMMENT '平均成本（美元）',
  
  -- 鲁棒性指标
  `robustness_score` DECIMAL(5,4) COMMENT '鲁棒性得分',
  `variance_threshold` DECIMAL(5,4) COMMENT '方差阈值',
  `perturbation_consistency` DECIMAL(5,4) COMMENT '扰动一致性',
  
  -- 聚合统计
  `sample_count` INT NOT NULL COMMENT '样本数量',
  `success_rate` DECIMAL(5,4) COMMENT '成功率',
  `error_rate` DECIMAL(5,4) COMMENT '错误率',
  
  -- 详细指标JSON
  `detailed_metrics` JSON COMMENT '详细指标数据（包含分布、百分位数等）',
  `confidence_intervals` JSON COMMENT '置信区间',
  `statistical_tests` JSON COMMENT '统计检验结果',
  
  `metadata` JSON COMMENT '扩展元数据',
  `created_at` BIGINT NOT NULL COMMENT '创建时间（毫秒时间戳）',
  `updated_at` BIGINT NOT NULL COMMENT '更新时间（毫秒时间戳）',
  
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_run_category` (`run_id`, `metric_category`),
  KEY `idx_run_id` (`run_id`),
  KEY `idx_category_created` (`metric_category`, `created_at`),
  CONSTRAINT `fk_eval_metric_run` FOREIGN KEY (`run_id`) REFERENCES `t_rag_eval_run` (`run_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='RAG评估指标汇总表';