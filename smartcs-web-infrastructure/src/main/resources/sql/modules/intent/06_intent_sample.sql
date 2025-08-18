-- 意图模块 - 意图样本表
CREATE TABLE IF NOT EXISTS `t_intent_sample` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `version_id` BIGINT NOT NULL COMMENT '版本ID',
  `type` VARCHAR(32) NOT NULL COMMENT '样本类型: TRAIN/DEV/TEST/ONLINE_HARD_NEG/UNKNOWN',
  `text` TEXT NOT NULL COMMENT '文本内容',
  `slots` JSON COMMENT '插槽信息',
  `source` VARCHAR(64) DEFAULT 'manual' COMMENT '数据来源: manual/online/augment',
  `confidence_score` DOUBLE COMMENT '置信度分数',
  `annotator_id` BIGINT COMMENT '标注者ID',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
  `created_by` VARCHAR(64) COMMENT '创建者',
  `updated_by` VARCHAR(64) COMMENT '更新者',
  `created_at` BIGINT COMMENT '创建时间',
  `updated_at` BIGINT COMMENT '更新时间',
  INDEX idx_version_id (`version_id`),
  INDEX idx_type (`type`),
  INDEX idx_source (`source`),
  INDEX idx_annotator_id (`annotator_id`),
  INDEX idx_version_type (`version_id`, `type`),
  CONSTRAINT fk_sample_version FOREIGN KEY (`version_id`) REFERENCES `t_intent_version`(`id`) ON DELETE CASCADE
) COMMENT '意图样本表';