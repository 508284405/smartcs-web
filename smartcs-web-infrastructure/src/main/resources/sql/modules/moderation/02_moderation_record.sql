-- 内容审核模块 - 审核记录表
CREATE TABLE IF NOT EXISTS `t_moderation_record` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `content_hash` VARCHAR(64) NOT NULL COMMENT '内容哈希值，用于去重和缓存',
  `original_content` TEXT NOT NULL COMMENT '原始待审核内容',
  `content_type` VARCHAR(32) NOT NULL COMMENT '内容类型 MESSAGE/KNOWLEDGE/DOCUMENT/FAQ',
  `source_id` VARCHAR(64) COMMENT '源ID，如消息ID、知识库ID等',
  `source_type` VARCHAR(32) COMMENT '来源类型 CHAT/KNOWLEDGE_BASE/RAG_QUERY/FILE_UPLOAD',
  `user_id` VARCHAR(64) COMMENT '用户ID',
  `session_id` VARCHAR(64) COMMENT '会话ID（如果适用）',
  
  -- 审核结果
  `moderation_result` VARCHAR(16) NOT NULL COMMENT '审核结果 APPROVED/REJECTED/NEEDS_REVIEW/PENDING',
  `risk_level` VARCHAR(16) COMMENT '风险等级 LOW/MEDIUM/HIGH/CRITICAL',
  `confidence_score` DECIMAL(5,4) COMMENT '置信度分数 0.0000-1.0000',
  `is_blocked` TINYINT DEFAULT 0 COMMENT '是否被阻断 1=被阻断 0=未阻断',
  
  -- 违规信息
  `violation_categories` JSON COMMENT '违规分类列表，JSON格式存储分类ID和分类名称',
  `ai_analysis_result` JSON COMMENT 'AI分析结果，包括详细的分析信息',
  `keyword_matches` JSON COMMENT '匹配的关键词列表',
  
  -- 审核方式和耗时
  `moderation_methods` VARCHAR(128) COMMENT '审核方式 AI,KEYWORD,MANUAL 多个用逗号分隔',
  `ai_model_used` VARCHAR(64) COMMENT '使用的AI模型名称',
  `processing_time_ms` BIGINT COMMENT '处理耗时（毫秒）',
  
  -- 人工审核
  `manual_review_status` VARCHAR(32) COMMENT '人工审核状态 PENDING/APPROVED/REJECTED/NOT_REQUIRED',
  `manual_reviewer_id` VARCHAR(64) COMMENT '人工审核员ID',
  `manual_review_notes` TEXT COMMENT '人工审核备注',
  `manual_reviewed_at` BIGINT COMMENT '人工审核时间',
  
  -- 后续处理
  `action_taken` VARCHAR(32) COMMENT '采取的行动 WARN/BLOCK/ESCALATE/LOG_ONLY',
  `escalated_to` VARCHAR(64) COMMENT '升级给谁处理',
  `escalated_at` BIGINT COMMENT '升级时间',
  
  -- 元数据
  `client_ip` VARCHAR(45) COMMENT '客户端IP地址',
  `user_agent` VARCHAR(256) COMMENT '用户代理信息',
  `request_id` VARCHAR(64) COMMENT '请求ID，用于追踪',
  `metadata` JSON COMMENT '扩展元数据',
  
  -- 系统字段
  `created_at` BIGINT NOT NULL COMMENT '创建时间（毫秒时间戳）',
  `updated_at` BIGINT NOT NULL COMMENT '更新时间（毫秒时间戳）',
  
  -- 索引
  UNIQUE KEY uk_content_hash (`content_hash`),
  INDEX idx_content_type (`content_type`),
  INDEX idx_source_type (`source_type`),
  INDEX idx_user_id (`user_id`),
  INDEX idx_session_id (`session_id`),
  INDEX idx_moderation_result (`moderation_result`),
  INDEX idx_risk_level (`risk_level`),
  INDEX idx_is_blocked (`is_blocked`),
  INDEX idx_manual_review_status (`manual_review_status`),
  INDEX idx_created_at (`created_at`),
  INDEX idx_source_id_type (`source_id`, `source_type`)
) COMMENT '内容审核记录表';

-- 创建分区表（按月分区，提高查询性能）
-- ALTER TABLE t_moderation_record PARTITION BY RANGE (created_at) (
--   PARTITION p202501 VALUES LESS THAN (1738339200000),  -- 2025-02-01
--   PARTITION p202502 VALUES LESS THAN (1740758400000),  -- 2025-03-01  
--   PARTITION p202503 VALUES LESS THAN (1743436800000),  -- 2025-04-01
--   PARTITION p_future VALUES LESS THAN MAXVALUE
-- );