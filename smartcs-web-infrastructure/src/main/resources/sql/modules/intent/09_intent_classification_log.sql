-- 意图模块 - 意图分类日志表
CREATE TABLE IF NOT EXISTS `t_intent_classification_log` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `session_id` VARCHAR(128) COMMENT '会话ID',
  `snapshot_id` VARCHAR(64) NOT NULL COMMENT '快照ID',
  `input_text` TEXT NOT NULL COMMENT '输入文本',
  `intent_code` VARCHAR(64) COMMENT '识别的意图编码',
  `confidence_score` DOUBLE COMMENT '置信度分数',
  `channel` VARCHAR(32) COMMENT '渠道',
  `tenant` VARCHAR(64) COMMENT '租户',
  `user_id` BIGINT COMMENT '用户ID',
  `classification_time` BIGINT COMMENT '分类时间',
  `processing_time_ms` INT COMMENT '处理时间毫秒',
  `result_data` JSON COMMENT '完整结果数据',
  `created_at` BIGINT COMMENT '创建时间',
  INDEX idx_session_id (`session_id`),
  INDEX idx_snapshot_id (`snapshot_id`),
  INDEX idx_intent_code (`intent_code`),
  INDEX idx_channel (`channel`),
  INDEX idx_tenant (`tenant`),
  INDEX idx_user_id (`user_id`),
  INDEX idx_classification_time (`classification_time`)
) COMMENT '意图分类日志表';