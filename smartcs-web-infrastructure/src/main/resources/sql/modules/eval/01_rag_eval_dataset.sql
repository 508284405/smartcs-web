-- RAG评估数据集表
-- 用于存储评估数据集的元数据信息
CREATE TABLE `t_rag_eval_dataset` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '数据集ID',
  `dataset_id` VARCHAR(64) NOT NULL COMMENT '数据集唯一标识符',
  `name` VARCHAR(128) NOT NULL COMMENT '数据集名称',
  `description` TEXT COMMENT '数据集描述',
  `domain` VARCHAR(64) COMMENT '领域类型（如：customer_service、knowledge_base等）',
  `language` VARCHAR(16) DEFAULT 'zh-CN' COMMENT '语言类型',
  `total_cases` INT DEFAULT 0 COMMENT '总测试用例数',
  `active_cases` INT DEFAULT 0 COMMENT '活跃测试用例数',
  `creator_id` BIGINT COMMENT '创建者用户ID',
  `creator_name` VARCHAR(64) COMMENT '创建者姓名',
  `tags` JSON COMMENT '标签信息',
  `metadata` JSON COMMENT '扩展元数据',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除：0-否，1-是',
  `created_by` BIGINT COMMENT '创建人',
  `updated_by` BIGINT COMMENT '更新人',
  `created_at` BIGINT NOT NULL COMMENT '创建时间（毫秒时间戳）',
  `updated_at` BIGINT NOT NULL COMMENT '更新时间（毫秒时间戳）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_dataset_id` (`dataset_id`),
  KEY `idx_creator_id` (`creator_id`),
  KEY `idx_domain_status` (`domain`, `status`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='RAG评估数据集表';