-- SmartCS Web 统一初始化脚本
-- 按模块顺序执行所有表的创建
-- 版本: 1.0.0
-- 创建时间: 2024-12-01

-- ================================================================
-- 核心模块 - 基础表
-- ================================================================

-- 管理模块 - 系统配置表
CREATE TABLE IF NOT EXISTS `t_system_config` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    `config_key` VARCHAR(128) NOT NULL UNIQUE COMMENT '配置键',
    `config_value` TEXT COMMENT '配置值',
    `config_type` VARCHAR(32) DEFAULT 'STRING' COMMENT '配置类型 STRING/NUMBER/BOOLEAN/JSON',
    `description` VARCHAR(512) COMMENT '配置描述',
    `category` VARCHAR(64) DEFAULT 'GENERAL' COMMENT '配置分类',
    `is_encrypted` BOOLEAN DEFAULT FALSE COMMENT '是否加密存储',
    `is_readonly` BOOLEAN DEFAULT FALSE COMMENT '是否只读',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '软删除标志',
    `created_by` VARCHAR(64) COMMENT '创建者',
    `updated_by` VARCHAR(64) COMMENT '更新者',
    `created_at` BIGINT COMMENT '创建时间（毫秒时间戳）',
    `updated_at` BIGINT COMMENT '更新时间（毫秒时间戳）',
    INDEX `idx_config_key` (`config_key`),
    INDEX `idx_category` (`category`),
    INDEX `idx_created_at` (`created_at`)
) COMMENT '系统配置表';

-- ================================================================
-- 聊天模块 - Chat Module
-- ================================================================

-- 聊天用户表
CREATE TABLE IF NOT EXISTS t_cs_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    user_id BIGINT COMMENT '用户ID',
    nick_name VARCHAR(255) COMMENT '昵称',
    avatar_url VARCHAR(255) COMMENT '头像URL',
    phone_mask VARCHAR(255) COMMENT '手机号掩码',
    user_type INT COMMENT '用户类型 0=消费者 1=客服',
    status INT COMMENT '状态 1=正常 0=禁用',
    is_deleted INT DEFAULT 0 COMMENT '逻辑删除',
    created_by VARCHAR(255) COMMENT '创建人',
    updated_by VARCHAR(255) COMMENT '更新人',
    created_at BIGINT COMMENT '创建时间',
    updated_at BIGINT COMMENT '更新时间',
    INDEX idx_user_id (user_id),
    INDEX idx_status (status)
) COMMENT='用户数据对象';

-- 聊天会话表
CREATE TABLE IF NOT EXISTS t_cs_session (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    session_id BIGINT COMMENT '会话ID',
    session_name VARCHAR(50) COMMENT '会话名称',
    customer_id BIGINT COMMENT '客户ID',
    agent_id BIGINT COMMENT '客服ID',
    session_state INT COMMENT '会话状态 0=排队 1=进行中 2=已结束',
    last_msg_time BIGINT COMMENT '最后消息时间',
    is_deleted INT DEFAULT 0 COMMENT '逻辑删除',
    created_by VARCHAR(255) COMMENT '创建人',
    updated_by VARCHAR(255) COMMENT '更新人',
    created_at BIGINT COMMENT '创建时间',
    updated_at BIGINT COMMENT '更新时间',
    UNIQUE KEY uk_session_id (session_id),
    INDEX idx_customer_id (customer_id),
    INDEX idx_agent_id (agent_id),
    INDEX idx_session_state (session_state)
) COMMENT='会话数据对象';

-- 聊天消息表
CREATE TABLE IF NOT EXISTS t_cs_message (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    msg_id VARCHAR(255) COMMENT '消息ID',
    session_id BIGINT COMMENT '会话ID',
    msg_type INT COMMENT '消息类型 0=text 1=image 2=order_card 3=system',
    chat_type VARCHAR(50) COMMENT '消息种类 USER/ASSISTANT/SYSTEM/TOOL',
    content TEXT COMMENT '消息内容，JSON格式存储富文本',
    timestamp DATETIME COMMENT '时间戳',
    is_deleted INT DEFAULT 0 COMMENT '逻辑删除',
    created_by VARCHAR(255) COMMENT '创建人',
    updated_by VARCHAR(255) COMMENT '更新人',
    created_at BIGINT COMMENT '创建时间',
    updated_at BIGINT COMMENT '更新时间',
    UNIQUE KEY uk_msg_id (msg_id),
    INDEX idx_session_id (session_id),
    INDEX idx_chat_type (chat_type),
    INDEX idx_timestamp (timestamp)
) COMMENT='消息数据对象';

-- ================================================================
-- 模型模块 - Model Module  
-- ================================================================

-- 模型提供商表
CREATE TABLE IF NOT EXISTS `t_model_provider` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    `provider_type` VARCHAR(128) NOT NULL COMMENT '提供商类型枚举',
    `label` VARCHAR(128) NOT NULL COMMENT '名称',
    `icon_small` VARCHAR(256) COMMENT '小图标URL',
    `icon_large` VARCHAR(256) COMMENT '大图标URL',
    `api_key` VARCHAR(256) COMMENT 'API Key（全局）',
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

-- 模型实例表
CREATE TABLE IF NOT EXISTS `t_model` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    `provider_id` BIGINT NOT NULL COMMENT '关联provider.id',
    `label` VARCHAR(128) NOT NULL COMMENT '名称',
    `model_type` VARCHAR(256) NOT NULL COMMENT '模型类型（多个类型用逗号分隔）',
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
    INDEX `idx_model_type` (`model_type`),
    INDEX `idx_status` (`status`),
    INDEX `idx_deprecated` (`deprecated`),
    INDEX `idx_created_at` (`created_at`),
    CONSTRAINT `fk_model_provider_id` FOREIGN KEY (`provider_id`) REFERENCES `t_model_provider`(`id`)
) COMMENT '模型实例表';

-- 模型Prompt模板表
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

-- 模型任务表
CREATE TABLE IF NOT EXISTS `t_model_task` (
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
    `started_at` BIGINT COMMENT '开始时间（毫秒时间戳）',
    `completed_at` BIGINT COMMENT '完成时间（毫秒时间戳）',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '软删除标志',
    `created_by` VARCHAR(64) COMMENT '创建者',
    `updated_by` VARCHAR(64) COMMENT '更新者',
    `created_at` BIGINT COMMENT '创建时间（毫秒时间戳）',
    `updated_at` BIGINT COMMENT '更新时间（毫秒时间戳）',
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
CREATE TABLE IF NOT EXISTS `t_model_context` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    `session_id` VARCHAR(128) NOT NULL UNIQUE COMMENT '会话ID',
    `model_id` BIGINT NOT NULL COMMENT '模型ID',
    `messages` LONGTEXT COMMENT '上下文消息列表（JSON格式）',
    `context_window` INT COMMENT '上下文窗口大小',
    `current_length` INT DEFAULT 0 COMMENT '当前上下文长度',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '软删除标志',
    `created_by` VARCHAR(64) COMMENT '创建者',
    `updated_by` VARCHAR(64) COMMENT '更新者',
    `created_at` BIGINT COMMENT '创建时间（毫秒时间戳）',
    `updated_at` BIGINT COMMENT '最后更新时间（毫秒时间戳）',
    INDEX `idx_session_id` (`session_id`),
    INDEX `idx_model_id` (`model_id`),
    INDEX `idx_created_at` (`created_at`),
    INDEX `idx_updated_at` (`updated_at`),
    CONSTRAINT `fk_context_model_id` FOREIGN KEY (`model_id`) REFERENCES `t_model`(`id`)
) COMMENT '模型上下文表';

-- ================================================================
-- 知识模块 - Knowledge Module
-- ================================================================

-- 知识库表
CREATE TABLE IF NOT EXISTS `t_kb_knowledge_base` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `name` VARCHAR(128) NOT NULL COMMENT '知识库名称',
  `code` VARCHAR(64) UNIQUE NOT NULL COMMENT '知识库唯一编码',
  `description` TEXT COMMENT '描述信息',
  `owner_id` BIGINT NOT NULL COMMENT '创建者ID',
  `visibility` VARCHAR(16) DEFAULT 'private' COMMENT 'public/private',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
  `created_by` VARCHAR(64) COMMENT '创建者',
  `updated_by` VARCHAR(64) COMMENT '更新者',
  `created_at` BIGINT COMMENT '创建时间',
  `updated_at` BIGINT COMMENT '更新时间',
  INDEX idx_owner_id (`owner_id`),
  INDEX idx_visibility (`visibility`),
  INDEX idx_code (`code`)
) COMMENT '知识库表';

-- 知识内容表
CREATE TABLE IF NOT EXISTS `t_kb_content` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `knowledge_base_id` BIGINT NOT NULL COMMENT '所属知识库ID',
  `title` VARCHAR(256) NOT NULL COMMENT '标题',
  `content_type` VARCHAR(32) NOT NULL COMMENT 'document/audio/video',
  `file_url` VARCHAR(512) COMMENT '原始文件地址',
  `file_type` VARCHAR(256) COMMENT '文件扩展名',
  `status` VARCHAR(32) DEFAULT 'enabled' COMMENT 'enabled/disabled',
  `segment_mode` VARCHAR(32) DEFAULT 'general' COMMENT '分段模式 general/parent_child',
  `char_count` BIGINT DEFAULT 0 COMMENT '字符数',
  `recall_count` BIGINT DEFAULT 0 COMMENT '召回次数',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
  `created_by` VARCHAR(64) COMMENT '创建者',
  `updated_by` VARCHAR(64) COMMENT '更新者',
  `created_at` BIGINT COMMENT '创建时间',
  `updated_at` BIGINT COMMENT '更新时间',
  INDEX idx_knowledge_base_id (`knowledge_base_id`),
  INDEX idx_content_type (`content_type`),
  INDEX idx_status (`status`)
) COMMENT '知识内容表';

-- 内容切片表
CREATE TABLE IF NOT EXISTS `t_kb_chunk` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `content_id` BIGINT NOT NULL COMMENT '内容ID',
  `chunk_index` VARCHAR(255) NOT NULL COMMENT '段落序号',
  `token_size` INT DEFAULT 0 COMMENT '切片token数',
  `content` TEXT NOT NULL COMMENT '切片内容文本',
  `metadata` JSON COMMENT '附加元信息，如页码、起止时间、原始位置等',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
  `created_by` VARCHAR(64) COMMENT '创建者',
  `updated_by` VARCHAR(64) COMMENT '更新者',
  `created_at` BIGINT COMMENT '创建时间',
  `updated_at` BIGINT COMMENT '更新时间',
  INDEX idx_content_id (`content_id`),
  INDEX idx_chunk_index (`chunk_index`)
) COMMENT '内容切片表';

-- 向量表
CREATE TABLE IF NOT EXISTS `t_kb_vector` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `chunk_id` BIGINT NOT NULL COMMENT '切片ID',
  `embedding` BLOB COMMENT '向量数据，float[]序列化后存储',
  `dim` INT DEFAULT 768 COMMENT '维度大小',
  `provider` VARCHAR(64) DEFAULT 'bge' COMMENT 'embedding提供方，如openai/bge',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
  `created_by` VARCHAR(64) COMMENT '创建者',
  `updated_by` VARCHAR(64) COMMENT '更新者',
  `created_at` BIGINT COMMENT '创建时间',
  `updated_at` BIGINT COMMENT '更新时间',
  UNIQUE INDEX idx_chunk_id (`chunk_id`)
) COMMENT '向量表';

-- 用户知识库权限关系表
CREATE TABLE IF NOT EXISTS `t_kb_user_kb_rel` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `knowledge_base_id` BIGINT NOT NULL COMMENT '知识库ID',
  `role` VARCHAR(32) DEFAULT 'reader' COMMENT 'reader/writer/admin',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
  `created_by` VARCHAR(64) COMMENT '创建者',
  `updated_by` VARCHAR(64) COMMENT '更新者',
  `created_at` BIGINT COMMENT '创建时间',
  `updated_at` BIGINT COMMENT '更新时间',
  UNIQUE INDEX idx_user_knowledge_base (`user_id`, `knowledge_base_id`)
) COMMENT '用户知识库权限关系表';

-- FAQ表
CREATE TABLE IF NOT EXISTS `t_cs_faq`
(
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `question` VARCHAR(255) NOT NULL COMMENT '问题文本',
    `answer_text` TEXT COMMENT '答案文本',
    `hit_count` BIGINT DEFAULT 0 COMMENT '命中次数',
    `version_no` INT DEFAULT 1 COMMENT '版本号',
    `enabled` TINYINT(1) DEFAULT 1 COMMENT '是否启用',
    `is_deleted` TINYINT(1) DEFAULT 0 COMMENT '逻辑删除标记',
    `created_by` VARCHAR(64) COMMENT '创建者',
    `updated_by` VARCHAR(64) COMMENT '更新者',
    `created_at` BIGINT COMMENT '创建时间（毫秒时间戳）',
    `updated_at` BIGINT COMMENT '更新时间（毫秒时间戳）',
    INDEX idx_question (question),
    FULLTEXT INDEX ft_question_answer (question, answer_text)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='常见问题FAQ表';

-- ================================================================
-- 应用模块 - Application Module
-- ================================================================

-- AI应用表
CREATE TABLE IF NOT EXISTS `t_ai_app` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `name` VARCHAR(128) NOT NULL COMMENT '应用名称',  
  `code` VARCHAR(64) UNIQUE NOT NULL COMMENT '应用唯一编码',
  `description` TEXT COMMENT '应用描述',
  `type` VARCHAR(32) NOT NULL COMMENT '应用类型: WORKFLOW/CHATFLOW/CHAT_ASSISTANT/AGENT',
  `config` JSON COMMENT '应用配置信息',
  `status` VARCHAR(16) DEFAULT 'DRAFT' COMMENT '应用状态: DRAFT/PUBLISHED/DISABLED',
  `icon` VARCHAR(255) COMMENT '应用图标',
  `tags` JSON COMMENT '应用标签',
  `creator_id` BIGINT NOT NULL COMMENT '创建者ID',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
  `created_by` VARCHAR(64) COMMENT '创建者',
  `updated_by` VARCHAR(64) COMMENT '更新者', 
  `created_at` BIGINT COMMENT '创建时间',
  `updated_at` BIGINT COMMENT '更新时间',
  INDEX idx_creator_id (`creator_id`),
  INDEX idx_type (`type`),
  INDEX idx_status (`status`),
  INDEX idx_code (`code`),
  INDEX idx_created_at (`created_at`)
) COMMENT 'AI应用表';

-- ================================================================
-- 初始化数据
-- ================================================================

-- 系统配置初始化数据
INSERT INTO `t_system_config` 
(`config_key`, `config_value`, `config_type`, `description`, `category`, `is_readonly`, `created_by`, `created_at`, `updated_at`)
VALUES 
('system.name', 'SmartCS', 'STRING', '系统名称', 'GENERAL', FALSE, 'system', UNIX_TIMESTAMP()*1000, UNIX_TIMESTAMP()*1000),
('system.version', '1.0.0', 'STRING', '系统版本', 'GENERAL', TRUE, 'system', UNIX_TIMESTAMP()*1000, UNIX_TIMESTAMP()*1000),
('chat.max_session_duration', '86400000', 'NUMBER', '最大会话持续时间（毫秒）', 'CHAT', FALSE, 'system', UNIX_TIMESTAMP()*1000, UNIX_TIMESTAMP()*1000),
('knowledge.max_file_size', '10485760', 'NUMBER', '知识库文件最大大小（字节）', 'KNOWLEDGE', FALSE, 'system', UNIX_TIMESTAMP()*1000, UNIX_TIMESTAMP()*1000),
('model.default_context_window', '4096', 'NUMBER', '默认上下文窗口大小', 'MODEL', FALSE, 'system', UNIX_TIMESTAMP()*1000, UNIX_TIMESTAMP()*1000);

-- Prompt模板初始化数据
INSERT INTO `t_model_prompt_template` 
(`template_key`, `template_name`, `template_content`, `description`, `model_types`, `is_system`, `status`, `created_by`, `created_at`, `updated_at`)
VALUES 
('system_default', '系统默认模板', '你是一个有用的AI助手。请根据用户的问题提供准确、有帮助的回答。', '系统默认的通用助手模板', 'LLM', TRUE, 'ACTIVE', 'system', UNIX_TIMESTAMP()*1000, UNIX_TIMESTAMP()*1000),
('chat_assistant', '聊天助手模板', '你是一个友好的聊天助手。请用自然、亲切的语调与用户对话，理解用户的需求并提供有用的建议。', '用于日常聊天的助手模板', 'LLM', TRUE, 'ACTIVE', 'system', UNIX_TIMESTAMP()*1000, UNIX_TIMESTAMP()*1000),
('code_assistant', '代码助手模板', '你是一个专业的编程助手。请帮助用户解决编程问题，提供清晰的代码示例和详细的解释。', '用于编程和代码相关问题的模板', 'LLM', TRUE, 'ACTIVE', 'system', UNIX_TIMESTAMP()*1000, UNIX_TIMESTAMP()*1000),
('knowledge_qa', '知识问答模板', '基于提供的知识库内容，请准确回答用户的问题。如果知识库中没有相关信息，请诚实地说明。\n\n知识库内容：{{knowledge}}\n\n用户问题：{{question}}', '用于知识库问答的RAG模板', 'LLM', TRUE, 'ACTIVE', 'system', UNIX_TIMESTAMP()*1000, UNIX_TIMESTAMP()*1000);