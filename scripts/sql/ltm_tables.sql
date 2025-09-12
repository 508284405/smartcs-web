-- LTM 基本表结构（MySQL示例）

CREATE TABLE IF NOT EXISTS `t_ltm_episodic_memory` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `session_id` BIGINT NULL,
  `episode_id` VARCHAR(64) NOT NULL UNIQUE,
  `content` MEDIUMTEXT,
  `embedding_vector` LONGBLOB,
  `context_json` JSON NULL,
  `timestamp` BIGINT,
  `importance_score` DOUBLE,
  `access_count` INT,
  `last_accessed_at` BIGINT,
  `consolidation_status` TINYINT,
  `created_at` BIGINT,
  `updated_at` BIGINT,
  KEY `idx_user_time` (`user_id`,`timestamp`),
  KEY `idx_user_importance` (`user_id`,`importance_score`)
);

CREATE TABLE IF NOT EXISTS `t_ltm_semantic_memory` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `concept` VARCHAR(255) NOT NULL,
  `knowledge` MEDIUMTEXT,
  `embedding_vector` LONGBLOB,
  `confidence` DOUBLE,
  `source_episodes_json` JSON,
  `evidence_count` INT,
  `contradiction_count` INT,
  `last_reinforced_at` BIGINT,
  `decay_rate` DOUBLE,
  `created_at` BIGINT,
  `updated_at` BIGINT,
  UNIQUE KEY `uk_user_concept` (`user_id`,`concept`),
  KEY `idx_user_confidence` (`user_id`,`confidence`)
);

CREATE TABLE IF NOT EXISTS `t_ltm_procedural_memory` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `pattern_type` VARCHAR(64) NOT NULL,
  `pattern_name` VARCHAR(128) NOT NULL,
  `pattern_description` VARCHAR(512),
  `trigger_conditions_json` JSON,
  `action_template` VARCHAR(1024),
  `success_count` INT,
  `failure_count` INT,
  `success_rate` DOUBLE,
  `last_triggered_at` BIGINT,
  `learning_rate` DOUBLE,
  `is_active` TINYINT,
  `created_at` BIGINT,
  `updated_at` BIGINT,
  UNIQUE KEY `uk_user_type_name` (`user_id`,`pattern_type`,`pattern_name`),
  KEY `idx_user_active` (`user_id`,`is_active`),
  KEY `idx_user_success` (`user_id`,`success_rate`)
);

