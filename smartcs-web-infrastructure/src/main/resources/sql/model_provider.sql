-- 模型提供商与模型实例表结构

-- 模型提供商表
CREATE TABLE `t_model_provider` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    `provider_key` VARCHAR(128) NOT NULL UNIQUE COMMENT '唯一标识（如 deepseek）',
    `label` VARCHAR(128) NOT NULL COMMENT '名称',
    `icon_small` VARCHAR(256) COMMENT '小图标URL',
    `icon_large` VARCHAR(256) COMMENT '大图标URL',
    `api_key` VARCHAR(256) NOT NULL COMMENT 'API Key（全局）',
    `endpoint` VARCHAR(256) COMMENT 'API Endpoint',
    `supported_model_types` VARCHAR(128) COMMENT '支持的模型类型（逗号分隔）',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '软删除标志',
    `created_by` VARCHAR(64) COMMENT '创建者',
    `updated_by` VARCHAR(64) COMMENT '更新者',
    `created_at` BIGINT COMMENT '创建时间（毫秒时间戳）',
    `updated_at` BIGINT COMMENT '更新时间（毫秒时间戳）',
    INDEX `idx_provider_key` (`provider_key`),
    INDEX `idx_supported_model_types` (`supported_model_types`),
    INDEX `idx_created_at` (`created_at`)
) COMMENT '模型提供商表';

-- 模型实例表
CREATE TABLE `t_model` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    `provider_id` BIGINT NOT NULL COMMENT '关联provider.id',
    `model_key` VARCHAR(128) NOT NULL COMMENT '模型唯一标识',
    `label` VARCHAR(128) NOT NULL COMMENT '名称',
    `model_type` VARCHAR(64) NOT NULL COMMENT '模型类型（llm/tts等）',
    `features` VARCHAR(256) COMMENT '能力标签（逗号分隔）',
    `fetch_from` VARCHAR(64) COMMENT '来源（如predefined-model）',
    `model_properties` TEXT COMMENT '其他属性（如context_size, mode等，JSON格式）',
    `deprecated` BOOLEAN DEFAULT FALSE COMMENT '是否废弃',
    `status` VARCHAR(32) DEFAULT 'active' COMMENT '状态（active/inactive）',
    `load_balancing_enabled` BOOLEAN DEFAULT FALSE COMMENT '是否负载均衡',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '软删除标志',
    `created_by` VARCHAR(64) COMMENT '创建者',
    `updated_by` VARCHAR(64) COMMENT '更新者',
    `created_at` BIGINT COMMENT '创建时间（毫秒时间戳）',
    `updated_at` BIGINT COMMENT '更新时间（毫秒时间戳）',
    INDEX `idx_provider_id` (`provider_id`),
    INDEX `idx_model_key` (`model_key`),
    INDEX `idx_model_type` (`model_type`),
    INDEX `idx_status` (`status`),
    INDEX `idx_deprecated` (`deprecated`),
    INDEX `idx_created_at` (`created_at`),
    CONSTRAINT `fk_model_provider_id` FOREIGN KEY (`provider_id`) REFERENCES `t_model_provider`(`id`)
) COMMENT '模型实例表';

-- 为provider_id和model_key组合创建唯一索引
ALTER TABLE `t_model` ADD UNIQUE INDEX `idx_provider_model` (`provider_id`, `model_key`);