-- 机器人配置表：仅 Bot-Service 使用
CREATE TABLE IF NOT EXISTS `t_cs_bot_profile` (
    `id`            BIGINT          NOT NULL COMMENT '主键，同时对应 cs_agent.agent_id',
    `bot_name`          VARCHAR(128)    NOT NULL COMMENT '机器人名称',
    `model_name`        VARCHAR(128)    NOT NULL COMMENT '使用的 LLM / 模型标识，如 gpt-4o、bge-large',
    `prompt_key`        VARCHAR(64)     NOT NULL COMMENT '默认 Prompt 模板 key，关联 bot_prompt_template',
    `max_qps`           INT             NOT NULL DEFAULT 10 COMMENT '该 Bot 对外允许的最大 QPS',
    `temperature`       DECIMAL(3,2)    NOT NULL DEFAULT 0.7 COMMENT 'LLM 采样温度',
    `extra_config`      JSON            DEFAULT NULL COMMENT '额外配置（如系统指令、插件开关等）',
    
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