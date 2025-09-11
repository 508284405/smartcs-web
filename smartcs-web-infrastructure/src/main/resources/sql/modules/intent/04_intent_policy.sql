-- 意图模块 - 意图策略表
CREATE TABLE IF NOT EXISTS `t_intent_policy` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `version_id` BIGINT NOT NULL COMMENT '版本ID',
  `threshold_tau` DECIMAL(5,4) COMMENT '阈值 tau',
  `margin_delta` DECIMAL(5,4) COMMENT '边际 delta',
  `temp_t` DECIMAL(5,4) COMMENT '温度 T',
  `unknown_label` VARCHAR(128) COMMENT '未知标签',
  `channel_overrides` JSON COMMENT '渠道覆盖配置',
  `created_by` VARCHAR(64) COMMENT '创建者',
  `updated_by` VARCHAR(64) COMMENT '更新者',
  `created_at` BIGINT COMMENT '创建时间',
  `updated_at` BIGINT COMMENT '更新时间',
  UNIQUE KEY uk_version_policy (`version_id`),
  CONSTRAINT fk_policy_version FOREIGN KEY (`version_id`) REFERENCES `t_intent_version`(`id`) ON DELETE CASCADE
) COMMENT '意图策略表';