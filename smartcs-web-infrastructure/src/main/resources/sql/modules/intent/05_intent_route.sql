-- 意图模块 - 意图路由表
CREATE TABLE IF NOT EXISTS `t_intent_route` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `version_id` BIGINT NOT NULL COMMENT '版本ID',
  `route_type` VARCHAR(32) NOT NULL COMMENT '路由类型: SMALL_MODEL/RULE/LLM/HYBRID',
  `route_conf` JSON COMMENT '路由配置',
  `created_by` VARCHAR(64) COMMENT '创建者',
  `updated_by` VARCHAR(64) COMMENT '更新者',
  `created_at` BIGINT COMMENT '创建时间',
  `updated_at` BIGINT COMMENT '更新时间',
  UNIQUE KEY uk_version_route (`version_id`),
  INDEX idx_route_type (`route_type`),
  CONSTRAINT fk_route_version FOREIGN KEY (`version_id`) REFERENCES `t_intent_version`(`id`) ON DELETE CASCADE
) COMMENT '意图路由表';