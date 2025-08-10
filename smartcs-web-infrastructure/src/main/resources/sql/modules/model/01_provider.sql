-- 模型模块 - 提供商表
CREATE TABLE IF NOT EXISTS `t_model_provider` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    `provider_type` VARCHAR(128) NOT NULL COMMENT '提供商类型枚举',
    `label` VARCHAR(128) NOT NULL COMMENT '名称',
    `icon_small` VARCHAR(256) COMMENT '小图标URL',
    `icon_large` VARCHAR(256) COMMENT '大图标URL',
    `api_key` VARCHAR(256) COMMENT 'API Key（全局，待废弃）',
    `api_key_cipher` VARBINARY(1024) COMMENT 'API Key 密文（AES-GCM加密）',
    `api_key_iv` VARBINARY(32) COMMENT 'API Key 加密初始化向量',
    `api_key_kid` VARCHAR(64) COMMENT 'API Key 加密密钥ID',
    `endpoint` VARCHAR(256) COMMENT 'API Endpoint',
    `supported_model_types` VARCHAR(128) COMMENT '支持的模型类型（逗号分隔）',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '软删除标志',
    `created_by` VARCHAR(64) COMMENT '创建者',
    `updated_by` VARCHAR(64) COMMENT '更新者',
    `created_at` BIGINT COMMENT '创建时间（毫秒时间戳）',
    `updated_at` BIGINT COMMENT '更新时间（毫秒时间戳）',
    INDEX `idx_provider_type` (`provider_type`),
    INDEX `idx_supported_model_types` (`supported_model_types`),
    INDEX `idx_created_at` (`created_at`)
) COMMENT '模型提供商表';