-- 模型扩展功能表结构
-- 包含推理任务、上下文管理、Prompt模板等

-- 模型Prompt模板表
CREATE TABLE `t_model_prompt_template` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    `template_key` VARCHAR(255) NOT NULL UNIQUE COMMENT '模板键（唯一标识）',
    `template_name` VARCHAR(255) NOT NULL COMMENT '模板名称',
    `template_content` TEXT NOT NULL COMMENT '模板内容',
    `description` TEXT COMMENT '模板描述',
    `model_types` VARCHAR(256) COMMENT '支持的模型类型（逗号分隔）',
    `variables` TEXT COMMENT '模板变量（JSON格式）',
    `is_system` BOOLEAN DEFAULT FALSE COMMENT '是否为系统内置模板',
    `status` VARCHAR(32) DEFAULT 'ACTIVE' COMMENT '状态（ACTIVE/INACTIVE）',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '软删除标志',
    `created_by` VARCHAR(64) COMMENT '创建者',
    `updated_by` VARCHAR(64) COMMENT '更新者',
    `created_at` BIGINT COMMENT '创建时间（毫秒时间戳）',
    `updated_at` BIGINT COMMENT '更新时间（毫秒时间戳）',
    INDEX `idx_template_key` (`template_key`),
    INDEX `idx_template_name` (`template_name`),
    INDEX `idx_model_types` (`model_types`),
    INDEX `idx_is_system` (`is_system`),
    INDEX `idx_status` (`status`),
    INDEX `idx_created_at` (`created_at`)
) COMMENT '模型Prompt模板表';

-- 模型任务表
CREATE TABLE `t_model_task` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    `task_id` VARCHAR(64) NOT NULL UNIQUE COMMENT '任务ID',
    `model_id` BIGINT NOT NULL COMMENT '模型ID',
    `task_type` VARCHAR(32) NOT NULL COMMENT '任务类型（INFERENCE/TRAINING/EVALUATION/FINE_TUNING）',
    `status` VARCHAR(32) NOT NULL DEFAULT 'PENDING' COMMENT '任务状态（PENDING/RUNNING/COMPLETED/FAILED/CANCELLED）',
    `input_data` TEXT COMMENT '输入数据（JSON格式）',
    `output_data` TEXT COMMENT '输出数据（JSON格式）',
    `progress` INT DEFAULT 0 COMMENT '进度百分比（0-100）',
    `error_message` TEXT COMMENT '错误信息',
    `priority` INT DEFAULT 0 COMMENT '任务优先级',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '软删除标志',
    `created_at` BIGINT COMMENT '创建时间（毫秒时间戳）',
    `started_at` BIGINT COMMENT '开始时间（毫秒时间戳）',
    `completed_at` BIGINT COMMENT '完成时间（毫秒时间戳）',
    `created_by` VARCHAR(64) COMMENT '创建者',
    INDEX `idx_task_id` (`task_id`),
    INDEX `idx_model_id` (`model_id`),
    INDEX `idx_task_type` (`task_type`),
    INDEX `idx_status` (`status`),
    INDEX `idx_priority` (`priority`),
    INDEX `idx_created_at` (`created_at`),
    INDEX `idx_started_at` (`started_at`),
    INDEX `idx_completed_at` (`completed_at`),
    CONSTRAINT `fk_task_model_id` FOREIGN KEY (`model_id`) REFERENCES `t_model`(`id`)
) COMMENT '模型任务表';

-- 模型上下文表
CREATE TABLE `t_model_context` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    `session_id` VARCHAR(128) NOT NULL UNIQUE COMMENT '会话ID',
    `model_id` BIGINT NOT NULL COMMENT '模型ID',
    `messages` LONGTEXT COMMENT '上下文消息列表（JSON格式）',
    `context_window` INT COMMENT '上下文窗口大小',
    `current_length` INT DEFAULT 0 COMMENT '当前上下文长度',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '软删除标志',
    `created_at` BIGINT COMMENT '创建时间（毫秒时间戳）',
    `updated_at` BIGINT COMMENT '最后更新时间（毫秒时间戳）',
    INDEX `idx_session_id` (`session_id`),
    INDEX `idx_model_id` (`model_id`),
    INDEX `idx_created_at` (`created_at`),
    INDEX `idx_updated_at` (`updated_at`),
    CONSTRAINT `fk_context_model_id` FOREIGN KEY (`model_id`) REFERENCES `t_model`(`id`)
) COMMENT '模型上下文表';

-- 扩展现有t_model表，添加推理相关字段
ALTER TABLE `t_model` 
ADD COLUMN `inference_config` TEXT COMMENT '推理配置（JSON格式）',
ADD COLUMN `context_window` INT COMMENT '上下文窗口大小',
ADD COLUMN `supports_streaming` BOOLEAN DEFAULT FALSE COMMENT '是否支持流式推理';

-- 为新增字段添加索引
ALTER TABLE `t_model` ADD INDEX `idx_supports_streaming` (`supports_streaming`);

-- 插入一些系统默认的Prompt模板
INSERT INTO `t_model_prompt_template` 
(`template_key`, `template_name`, `template_content`, `description`, `model_types`, `is_system`, `status`, `created_by`, `created_at`, `updated_at`)
VALUES 
('system_default', '系统默认模板', '你是一个有用的AI助手。请根据用户的问题提供准确、有帮助的回答。', '系统默认的通用助手模板', 'LLM', TRUE, 'ACTIVE', 'system', UNIX_TIMESTAMP()*1000, UNIX_TIMESTAMP()*1000),
('chat_assistant', '聊天助手模板', '你是一个友好的聊天助手。请用自然、亲切的语调与用户对话，理解用户的需求并提供有用的建议。', '用于日常聊天的助手模板', 'LLM', TRUE, 'ACTIVE', 'system', UNIX_TIMESTAMP()*1000, UNIX_TIMESTAMP()*1000),
('code_assistant', '代码助手模板', '你是一个专业的编程助手。请帮助用户解决编程问题，提供清晰的代码示例和详细的解释。', '用于编程和代码相关问题的模板', 'LLM', TRUE, 'ACTIVE', 'system', UNIX_TIMESTAMP()*1000, UNIX_TIMESTAMP()*1000),
('knowledge_qa', '知识问答模板', '基于提供的知识库内容，请准确回答用户的问题。如果知识库中没有相关信息，请诚实地说明。\n\n知识库内容：{{knowledge}}\n\n用户问题：{{question}}', '用于知识库问答的RAG模板', 'LLM', TRUE, 'ACTIVE', 'system', UNIX_TIMESTAMP()*1000, UNIX_TIMESTAMP()*1000);