-- 模型模块 - Prompt模板表
CREATE TABLE IF NOT EXISTS `t_model_prompt_template` (
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

-- 插入系统默认的Prompt模板
INSERT INTO `t_model_prompt_template` 
(`template_key`, `template_name`, `template_content`, `description`, `model_types`, `is_system`, `status`, `created_by`, `created_at`, `updated_at`)
VALUES 
('system_default', '系统默认模板', '你是一个有用的AI助手。请根据用户的问题提供准确、有帮助的回答。', '系统默认的通用助手模板', 'LLM', TRUE, 'ACTIVE', 'system', UNIX_TIMESTAMP()*1000, UNIX_TIMESTAMP()*1000),
('chat_assistant', '聊天助手模板', '你是一个友好的聊天助手。请用自然、亲切的语调与用户对话，理解用户的需求并提供有用的建议。', '用于日常聊天的助手模板', 'LLM', TRUE, 'ACTIVE', 'system', UNIX_TIMESTAMP()*1000, UNIX_TIMESTAMP()*1000),
('code_assistant', '代码助手模板', '你是一个专业的编程助手。请帮助用户解决编程问题，提供清晰的代码示例和详细的解释。', '用于编程和代码相关问题的模板', 'LLM', TRUE, 'ACTIVE', 'system', UNIX_TIMESTAMP()*1000, UNIX_TIMESTAMP()*1000),
('knowledge_qa', '知识问答模板', '基于提供的知识库内容，请准确回答用户的问题。如果知识库中没有相关信息，请诚实地说明。\n\n知识库内容：{{knowledge}}\n\n用户问题：{{question}}', '用于知识库问答的RAG模板', 'LLM', TRUE, 'ACTIVE', 'system', UNIX_TIMESTAMP()*1000, UNIX_TIMESTAMP()*1000);