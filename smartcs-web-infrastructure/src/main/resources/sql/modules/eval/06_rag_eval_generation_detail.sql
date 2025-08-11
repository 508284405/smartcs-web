-- RAG评估生成详情表
-- 用于存储每个测试用例的生成详细结果
CREATE TABLE `t_rag_eval_generation_detail` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '详情记录ID',
  `run_id` VARCHAR(64) NOT NULL COMMENT '运行ID',
  `case_id` VARCHAR(64) NOT NULL COMMENT '测试用例ID',
  `query_original` TEXT NOT NULL COMMENT '原始查询',
  `injected_contexts` JSON COMMENT '注入的上下文信息',
  
  -- 提示构建
  `final_prompt` TEXT COMMENT '最终构建的提示词',
  `prompt_template` TEXT COMMENT '使用的提示模板',
  `prompt_tokens` INT COMMENT '提示Token数量',
  `context_injection_strategy` VARCHAR(64) COMMENT '上下文注入策略',
  
  -- 生成结果
  `generated_answer` TEXT COMMENT '生成的答案',
  `answer_tokens` INT COMMENT '答案Token数量',
  `generation_latency_ms` INT COMMENT '生成延迟（毫秒）',
  `model_used` VARCHAR(128) COMMENT '使用的生成模型',
  `model_params` JSON COMMENT '模型参数（温度、TopP等）',
  `streaming_enabled` TINYINT DEFAULT 0 COMMENT '是否启用流式生成：0-否，1-是',
  
  -- 引用和依据
  `citations` JSON COMMENT '引用信息（来源文档、位置等）',
  `supporting_evidence` JSON COMMENT '支持证据',
  `citation_accuracy` DECIMAL(5,4) COMMENT '引用准确性',
  `evidence_alignment` DECIMAL(5,4) COMMENT '证据对齐度',
  
  -- RAGAS生成指标
  `faithfulness_score` DECIMAL(5,4) COMMENT 'RAGAS Faithfulness分数',
  `answer_relevancy_score` DECIMAL(5,4) COMMENT 'RAGAS Answer Relevancy分数',
  `groundedness_score` DECIMAL(5,4) COMMENT '基于事实程度分数',
  
  -- 质量评估指标
  `factual_correctness_score` DECIMAL(5,4) COMMENT '事实正确性分数',
  `completeness_score` DECIMAL(5,4) COMMENT '完整性分数',
  `conciseness_score` DECIMAL(5,4) COMMENT '简洁性分数',
  `coherence_score` DECIMAL(5,4) COMMENT '连贯性分数',
  `fluency_score` DECIMAL(5,4) COMMENT '流畅性分数',
  
  -- 引用一致性检查
  `citation_consistency_score` DECIMAL(5,4) COMMENT '引用一致性分数',
  `hallucination_detected` TINYINT DEFAULT 0 COMMENT '是否检测到幻觉：0-否，1-是',
  `hallucination_severity` VARCHAR(16) COMMENT '幻觉严重程度：low, medium, high',
  `unsupported_claims` JSON COMMENT '不支持的声明列表',
  
  -- 与期望答案的比较
  `expected_answer` TEXT COMMENT '期望的答案（来自测试用例）',
  `semantic_similarity` DECIMAL(5,4) COMMENT '语义相似度',
  `bleu_score` DECIMAL(5,4) COMMENT 'BLEU分数',
  `rouge_l_score` DECIMAL(5,4) COMMENT 'ROUGE-L分数',
  `bertscore_f1` DECIMAL(5,4) COMMENT 'BERTScore F1分数',
  
  -- 成本分析
  `total_tokens` INT COMMENT '总Token数量',
  `cost_usd` DECIMAL(10,6) COMMENT '成本（美元）',
  `cost_breakdown` JSON COMMENT '成本分解（输入、输出Token成本）',
  
  -- 错误和异常
  `has_error` TINYINT DEFAULT 0 COMMENT '是否有错误：0-否，1-是',
  `error_message` TEXT COMMENT '错误信息',
  `error_stage` VARCHAR(32) COMMENT '错误阶段：prompt_build, generation, evaluation',
  
  -- 调试和分析
  `generation_steps` JSON COMMENT '生成步骤详情（流式生成时的中间结果）',
  `attention_weights` JSON COMMENT '注意力权重（如果可用）',
  `debug_info` JSON COMMENT '调试信息',
  `metadata` JSON COMMENT '扩展元数据',
  `created_at` BIGINT NOT NULL COMMENT '创建时间（毫秒时间戳）',
  
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_run_case` (`run_id`, `case_id`),
  KEY `idx_run_id` (`run_id`),
  KEY `idx_case_id` (`case_id`),
  KEY `idx_has_error` (`has_error`),
  KEY `idx_faithfulness` (`faithfulness_score`),
  KEY `idx_answer_relevancy` (`answer_relevancy_score`),
  KEY `idx_created_at` (`created_at`),
  CONSTRAINT `fk_generation_detail_run` FOREIGN KEY (`run_id`) REFERENCES `t_rag_eval_run` (`run_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_generation_detail_case` FOREIGN KEY (`case_id`) REFERENCES `t_rag_eval_case` (`case_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='RAG评估生成详情表';