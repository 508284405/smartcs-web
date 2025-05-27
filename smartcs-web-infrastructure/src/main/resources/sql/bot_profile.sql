-- 机器人配置表：仅 Bot-Service 使用
CREATE TABLE IF NOT EXISTS `t_cs_bot_profile` (
    `id`            BIGINT          NOT NULL COMMENT '主键，同时对应 cs_agent.agent_id',
    `bot_name`          VARCHAR(128)    NOT NULL COMMENT '机器人名称',
    `model_name`        VARCHAR(128)    NOT NULL COMMENT '使用的 LLM / 模型标识，如 gpt-4o、bge-large',
    `prompt_key`        VARCHAR(64)     NOT NULL COMMENT '默认 Prompt 模板 key，关联 bot_prompt_template',
    `remark`            VARCHAR(500)    DEFAULT NULL COMMENT '备注信息',
    `vendor`            VARCHAR(32)     NOT NULL COMMENT '模型厂商，如openai、deepseek等',
    `model_type`        VARCHAR(32)     NOT NULL COMMENT '模型类型，如chat、embedding、image、audio等',
    `api_key`           VARCHAR(512)    NOT NULL COMMENT 'API密钥',
    `base_url`          VARCHAR(256)    NOT NULL COMMENT 'API基础URL',
    `options`           JSON            DEFAULT NULL COMMENT '模型具体配置（JSON格式），如具体模型4o-mini等',
    `enabled`           TINYINT(1)      NOT NULL DEFAULT 1 COMMENT '是否启用',
    
    -- 通用字段
    `is_deleted`        TINYINT(1)      NOT NULL DEFAULT 0,
    `created_by`        VARCHAR(64)     DEFAULT NULL,
    `updated_by`        VARCHAR(64)     DEFAULT NULL,
    `created_at`        BIGINT          NOT NULL COMMENT '创建时间 ms',
    `updated_at`        BIGINT          NOT NULL COMMENT '更新时间 ms',
    
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Bot Agent 详细配置表';

-- Bot Prompt 模板表
CREATE TABLE IF NOT EXISTS `t_bot_prompt_template` (
    `id`                BIGINT          NOT NULL COMMENT '主键',
    `template_key`      VARCHAR(255)    NOT NULL COMMENT '模板标识',
    `template_content`  TEXT            COMMENT '模板内容',

    -- 通用字段
    `is_deleted`        TINYINT(1)      NOT NULL DEFAULT 0,
    `created_by`        VARCHAR(64)     DEFAULT NULL,
    `updated_by`        VARCHAR(64)     DEFAULT NULL,
    `created_at`        BIGINT          NOT NULL COMMENT '创建时间 ms',
    `updated_at`        BIGINT          NOT NULL COMMENT '更新时间 ms',

    PRIMARY KEY (`id`),
    UNIQUE INDEX uk_template_key (`template_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Bot Prompt 模板表'; 