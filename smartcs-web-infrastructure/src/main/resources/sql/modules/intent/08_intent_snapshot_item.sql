-- 意图模块 - 意图快照项表
CREATE TABLE IF NOT EXISTS `t_intent_snapshot_item` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `snapshot_id` BIGINT NOT NULL COMMENT '快照ID',
  `version_id` BIGINT NOT NULL COMMENT '版本ID',
  `created_at` BIGINT COMMENT '创建时间',
  UNIQUE KEY uk_snapshot_version (`snapshot_id`, `version_id`),
  INDEX idx_snapshot_id (`snapshot_id`),
  INDEX idx_version_id (`version_id`),
  CONSTRAINT fk_snapshot_item_snapshot FOREIGN KEY (`snapshot_id`) REFERENCES `t_intent_snapshot`(`id`) ON DELETE CASCADE,
  CONSTRAINT fk_snapshot_item_version FOREIGN KEY (`version_id`) REFERENCES `t_intent_version`(`id`)
) COMMENT '意图快照项表';