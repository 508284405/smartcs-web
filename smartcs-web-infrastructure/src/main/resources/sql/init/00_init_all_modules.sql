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
-- 评估模块 - Evaluation Module
-- ================================================================

-- RAG评估数据集表
CREATE TABLE IF NOT EXISTS `t_rag_eval_dataset` (
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

-- RAG评估测试用例表
CREATE TABLE IF NOT EXISTS `t_rag_eval_case` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '测试用例ID',
  `case_id` VARCHAR(64) NOT NULL COMMENT '测试用例唯一标识符',
  `dataset_id` VARCHAR(64) NOT NULL COMMENT '所属数据集ID',
  `question` TEXT NOT NULL COMMENT '测试问题',
  `expected_summary` TEXT COMMENT '期望的回答摘要',
  `gold_evidence_refs` JSON COMMENT '标准证据引用（包含文档片段、FAQ等）',
  `ground_truth_contexts` JSON COMMENT '标准上下文（用于Context Precision/Recall计算）',
  `difficulty_tag` VARCHAR(32) COMMENT '难度标签：easy, medium, hard',
  `category` VARCHAR(64) COMMENT '类别标签（如：factual, reasoning, multi-hop等）',
  `query_type` VARCHAR(32) COMMENT '查询类型：simple, complex, ambiguous',
  `expected_retrieval_count` INT COMMENT '期望检索到的相关文档数量',
  `evaluation_notes` TEXT COMMENT '评估备注',
  `metadata` JSON COMMENT '扩展元数据（如原始数据源信息）',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除：0-否，1-是',
  `created_by` BIGINT COMMENT '创建人',
  `updated_by` BIGINT COMMENT '更新人',
  `created_at` BIGINT NOT NULL COMMENT '创建时间（毫秒时间戳）',
  `updated_at` BIGINT NOT NULL COMMENT '更新时间（毫秒时间戳）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_case_id` (`case_id`),
  KEY `idx_dataset_id` (`dataset_id`),
  KEY `idx_difficulty_tag` (`difficulty_tag`),
  KEY `idx_category` (`category`),
  KEY `idx_status_created` (`status`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='RAG评估测试用例表';

-- RAG评估运行记录表
CREATE TABLE IF NOT EXISTS `t_rag_eval_run` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '运行记录ID',
  `run_id` VARCHAR(64) NOT NULL COMMENT '运行唯一标识符',
  `dataset_id` VARCHAR(64) NOT NULL COMMENT '使用的数据集ID',
  `app_id` BIGINT COMMENT '关联的AI应用ID',
  `provider_id` BIGINT COMMENT '使用的模型提供商ID',
  `model_id` BIGINT COMMENT '使用的模型ID',
  `run_name` VARCHAR(128) COMMENT '运行名称',
  `run_description` TEXT COMMENT '运行描述',
  `run_type` VARCHAR(32) NOT NULL COMMENT '运行类型：retrieval, generation, e2e, ab_test',
  `evaluation_mode` VARCHAR(32) DEFAULT 'offline' COMMENT '评估模式：offline, online',
  `rag_config_snapshot` JSON COMMENT 'RAG配置快照（检索、重排序、生成参数）',
  `model_config_snapshot` JSON COMMENT '模型配置快照',
  `selected_metrics` JSON COMMENT '选择的评估指标列表',
  `total_cases` INT DEFAULT 0 COMMENT '总测试用例数',
  `completed_cases` INT DEFAULT 0 COMMENT '已完成用例数',
  `failed_cases` INT DEFAULT 0 COMMENT '失败用例数',
  `status` VARCHAR(32) DEFAULT 'pending' COMMENT '状态：pending, running, completed, failed, cancelled',
  `start_time` BIGINT COMMENT '开始时间（毫秒时间戳）',
  `end_time` BIGINT COMMENT '结束时间（毫秒时间戳）',
  `duration_ms` BIGINT COMMENT '运行时长（毫秒）',
  `error_message` TEXT COMMENT '错误信息',
  `progress_info` JSON COMMENT '进度信息',
  `comparison_baseline_run_id` VARCHAR(64) COMMENT 'A/B测试时的基准运行ID',
  `initiator_id` BIGINT COMMENT '发起人用户ID',
  `initiator_name` VARCHAR(64) COMMENT '发起人姓名',
  `metadata` JSON COMMENT '扩展元数据',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除：0-否，1-是',
  `created_by` BIGINT COMMENT '创建人',
  `updated_by` BIGINT COMMENT '更新人',
  `created_at` BIGINT NOT NULL COMMENT '创建时间（毫秒时间戳）',
  `updated_at` BIGINT NOT NULL COMMENT '更新时间（毫秒时间戳）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_run_id` (`run_id`),
  KEY `idx_dataset_id` (`dataset_id`),
  KEY `idx_app_id` (`app_id`),
  KEY `idx_run_type_status` (`run_type`, `status`),
  KEY `idx_initiator_created` (`initiator_id`, `created_at`),
  KEY `idx_start_time` (`start_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='RAG评估运行记录表';

-- RAG评估指标汇总表
CREATE TABLE IF NOT EXISTS `t_rag_eval_metric` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '指标记录ID',
  `run_id` VARCHAR(64) NOT NULL COMMENT '运行ID',
  `metric_category` VARCHAR(32) NOT NULL COMMENT '指标类别：retrieval, generation, efficiency, robustness',
  `precision_at_1` DECIMAL(5,4) COMMENT 'Precision@1',
  `precision_at_3` DECIMAL(5,4) COMMENT 'Precision@3',
  `precision_at_5` DECIMAL(5,4) COMMENT 'Precision@5',
  `recall_at_1` DECIMAL(5,4) COMMENT 'Recall@1',
  `recall_at_3` DECIMAL(5,4) COMMENT 'Recall@3',
  `recall_at_5` DECIMAL(5,4) COMMENT 'Recall@5',
  `mrr` DECIMAL(5,4) COMMENT '平均倒数排名（Mean Reciprocal Rank）',
  `ndcg_at_3` DECIMAL(5,4) COMMENT 'NDCG@3',
  `ndcg_at_5` DECIMAL(5,4) COMMENT 'NDCG@5',
  `context_precision` DECIMAL(5,4) COMMENT 'RAGAS Context Precision',
  `context_recall` DECIMAL(5,4) COMMENT 'RAGAS Context Recall',
  `rerank_improvement` DECIMAL(5,4) COMMENT '重排序改进率',
  `faithfulness` DECIMAL(5,4) COMMENT 'RAGAS Faithfulness（忠实度）',
  `answer_relevancy` DECIMAL(5,4) COMMENT 'RAGAS Answer Relevancy（答案相关性）',
  `citation_consistency` DECIMAL(5,4) COMMENT '引用一致性',
  `completeness` DECIMAL(5,4) COMMENT '完整性',
  `conciseness` DECIMAL(5,4) COMMENT '简洁性',
  `groundedness` DECIMAL(5,4) COMMENT '基于事实程度',
  `factual_correctness` DECIMAL(5,4) COMMENT '事实正确性',
  `avg_retrieval_latency_ms` DECIMAL(10,2) COMMENT '平均检索延迟（毫秒）',
  `avg_rerank_latency_ms` DECIMAL(10,2) COMMENT '平均重排序延迟（毫秒）',
  `avg_generation_latency_ms` DECIMAL(10,2) COMMENT '平均生成延迟（毫秒）',
  `avg_e2e_latency_ms` DECIMAL(10,2) COMMENT '平均端到端延迟（毫秒）',
  `avg_input_tokens` DECIMAL(10,2) COMMENT '平均输入Token数',
  `avg_output_tokens` DECIMAL(10,2) COMMENT '平均输出Token数',
  `avg_cost_usd` DECIMAL(10,6) COMMENT '平均成本（美元）',
  `robustness_score` DECIMAL(5,4) COMMENT '鲁棒性得分',
  `variance_threshold` DECIMAL(5,4) COMMENT '方差阈值',
  `perturbation_consistency` DECIMAL(5,4) COMMENT '扰动一致性',
  `sample_count` INT NOT NULL COMMENT '样本数量',
  `success_rate` DECIMAL(5,4) COMMENT '成功率',
  `error_rate` DECIMAL(5,4) COMMENT '错误率',
  `detailed_metrics` JSON COMMENT '详细指标数据（包含分布、百分位数等）',
  `confidence_intervals` JSON COMMENT '置信区间',
  `statistical_tests` JSON COMMENT '统计检验结果',
  `metadata` JSON COMMENT '扩展元数据',
  `created_at` BIGINT NOT NULL COMMENT '创建时间（毫秒时间戳）',
  `updated_at` BIGINT NOT NULL COMMENT '更新时间（毫秒时间戳）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_run_category` (`run_id`, `metric_category`),
  KEY `idx_run_id` (`run_id`),
  KEY `idx_category_created` (`metric_category`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='RAG评估指标汇总表';

-- RAG评估检索详情表
CREATE TABLE IF NOT EXISTS `t_rag_eval_retrieval_detail` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '详情记录ID',
  `run_id` VARCHAR(64) NOT NULL COMMENT '运行ID',
  `case_id` VARCHAR(64) NOT NULL COMMENT '测试用例ID',
  `query_original` TEXT NOT NULL COMMENT '原始查询',
  `query_rewritten` TEXT COMMENT '重写后的查询',
  `query_embedding_time_ms` INT COMMENT '查询向量化耗时（毫秒）',
  `retrieval_candidates` JSON COMMENT '检索候选结果（TopK文档，包含ID、文本片段、向量相似度分数）',
  `retrieval_scores` JSON COMMENT '检索分数详情',
  `retrieval_latency_ms` INT COMMENT '检索延迟（毫秒）',
  `retrieval_model_used` VARCHAR(128) COMMENT '使用的检索模型',
  `retrieval_params` JSON COMMENT '检索参数（TopK、阈值等）',
  `rerank_candidates` JSON COMMENT '重排序后的候选结果',
  `rerank_scores` JSON COMMENT '重排序分数',
  `rerank_latency_ms` INT COMMENT '重排序延迟（毫秒）',
  `rerank_model_used` VARCHAR(128) COMMENT '使用的重排序模型',
  `rerank_params` JSON COMMENT '重排序参数',
  `rerank_improvement` DECIMAL(5,4) COMMENT '重排序改进程度',
  `final_contexts` JSON COMMENT '最终选择的上下文',
  `filtered_candidates` JSON COMMENT '过滤掉的候选项',
  `context_compression_ratio` DECIMAL(5,4) COMMENT '上下文压缩比例',
  `precision_at_1` DECIMAL(5,4) COMMENT '单样本Precision@1',
  `precision_at_3` DECIMAL(5,4) COMMENT '单样本Precision@3',
  `precision_at_5` DECIMAL(5,4) COMMENT '单样本Precision@5',
  `recall_at_1` DECIMAL(5,4) COMMENT '单样本Recall@1',
  `recall_at_3` DECIMAL(5,4) COMMENT '单样本Recall@3',
  `recall_at_5` DECIMAL(5,4) COMMENT '单样本Recall@5',
  `reciprocal_rank` DECIMAL(5,4) COMMENT '倒数排名',
  `ndcg` DECIMAL(5,4) COMMENT 'NDCG分数',
  `context_precision_score` DECIMAL(5,4) COMMENT 'RAGAS Context Precision分数',
  `context_recall_score` DECIMAL(5,4) COMMENT 'RAGAS Context Recall分数',
  `has_error` TINYINT DEFAULT 0 COMMENT '是否有错误：0-否，1-是',
  `error_message` TEXT COMMENT '错误信息',
  `error_stage` VARCHAR(32) COMMENT '错误阶段：embedding, retrieval, rerank, filter',
  `debug_info` JSON COMMENT '调试信息',
  `metadata` JSON COMMENT '扩展元数据',
  `created_at` BIGINT NOT NULL COMMENT '创建时间（毫秒时间戳）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_run_case` (`run_id`, `case_id`),
  KEY `idx_run_id` (`run_id`),
  KEY `idx_case_id` (`case_id`),
  KEY `idx_has_error` (`has_error`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='RAG评估检索详情表';

-- RAG评估生成详情表
CREATE TABLE IF NOT EXISTS `t_rag_eval_generation_detail` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '详情记录ID',
  `run_id` VARCHAR(64) NOT NULL COMMENT '运行ID',
  `case_id` VARCHAR(64) NOT NULL COMMENT '测试用例ID',
  `query_original` TEXT NOT NULL COMMENT '原始查询',
  `injected_contexts` JSON COMMENT '注入的上下文信息',
  `final_prompt` TEXT COMMENT '最终构建的提示词',
  `prompt_template` TEXT COMMENT '使用的提示模板',
  `prompt_tokens` INT COMMENT '提示Token数量',
  `context_injection_strategy` VARCHAR(64) COMMENT '上下文注入策略',
  `generated_answer` TEXT COMMENT '生成的答案',
  `answer_tokens` INT COMMENT '答案Token数量',
  `generation_latency_ms` INT COMMENT '生成延迟（毫秒）',
  `model_used` VARCHAR(128) COMMENT '使用的生成模型',
  `model_params` JSON COMMENT '模型参数（温度、TopP等）',
  `streaming_enabled` TINYINT DEFAULT 0 COMMENT '是否启用流式生成：0-否，1-是',
  `citations` JSON COMMENT '引用信息（来源文档、位置等）',
  `supporting_evidence` JSON COMMENT '支持证据',
  `citation_accuracy` DECIMAL(5,4) COMMENT '引用准确性',
  `evidence_alignment` DECIMAL(5,4) COMMENT '证据对齐度',
  `faithfulness_score` DECIMAL(5,4) COMMENT 'RAGAS Faithfulness分数',
  `answer_relevancy_score` DECIMAL(5,4) COMMENT 'RAGAS Answer Relevancy分数',
  `groundedness_score` DECIMAL(5,4) COMMENT '基于事实程度分数',
  `factual_correctness_score` DECIMAL(5,4) COMMENT '事实正确性分数',
  `completeness_score` DECIMAL(5,4) COMMENT '完整性分数',
  `conciseness_score` DECIMAL(5,4) COMMENT '简洁性分数',
  `coherence_score` DECIMAL(5,4) COMMENT '连贯性分数',
  `fluency_score` DECIMAL(5,4) COMMENT '流畅性分数',
  `citation_consistency_score` DECIMAL(5,4) COMMENT '引用一致性分数',
  `hallucination_detected` TINYINT DEFAULT 0 COMMENT '是否检测到幻觉：0-否，1-是',
  `hallucination_severity` VARCHAR(16) COMMENT '幻觉严重程度：low, medium, high',
  `unsupported_claims` JSON COMMENT '不支持的声明列表',
  `expected_answer` TEXT COMMENT '期望的答案（来自测试用例）',
  `semantic_similarity` DECIMAL(5,4) COMMENT '语义相似度',
  `bleu_score` DECIMAL(5,4) COMMENT 'BLEU分数',
  `rouge_l_score` DECIMAL(5,4) COMMENT 'ROUGE-L分数',
  `bertscore_f1` DECIMAL(5,4) COMMENT 'BERTScore F1分数',
  `total_tokens` INT COMMENT '总Token数量',
  `cost_usd` DECIMAL(10,6) COMMENT '成本（美元）',
  `cost_breakdown` JSON COMMENT '成本分解（输入、输出Token成本）',
  `has_error` TINYINT DEFAULT 0 COMMENT '是否有错误：0-否，1-是',
  `error_message` TEXT COMMENT '错误信息',
  `error_stage` VARCHAR(32) COMMENT '错误阶段：prompt_build, generation, evaluation',
  `generation_steps` JSON COMMENT '生成步骤详情（流式生成时的中间结果）',
  `attention_weights` JSON COMMENT '注意力权重（如果可用）',
  `debug_info` JSON COMMENT '调试信息',
  `metadata` JSON COMMENT '扩展元数据',
  `created_at` BIGINT NOT NULL COMMENT '创建时间（毫秒时间戳）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_run_case` (`run_id`, `case_id`),
  KEY `idx_run_id` (`run_id`),
  KEY `idx_case_id` (`case_id`),
  KEY `idx_has_error` (`has_error`),
  KEY `idx_faithfulness` (`faithfulness_score`),
  KEY `idx_answer_relevancy` (`answer_relevancy_score`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='RAG评估生成详情表';

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

-- ================================================================
-- 意图模块 - Intent Management Module
-- ================================================================

-- 意图目录表
CREATE TABLE IF NOT EXISTS `t_intent_catalog` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `name` VARCHAR(128) NOT NULL COMMENT '目录名称',
  `code` VARCHAR(64) UNIQUE NOT NULL COMMENT '目录编码',
  `description` TEXT COMMENT '描述',
  `parent_id` BIGINT COMMENT '父目录ID',
  `sort_order` INT DEFAULT 0 COMMENT '排序',
  `creator_id` BIGINT NOT NULL COMMENT '创建者ID',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
  `created_by` VARCHAR(64) COMMENT '创建者',
  `updated_by` VARCHAR(64) COMMENT '更新者',
  `created_at` BIGINT COMMENT '创建时间',
  `updated_at` BIGINT COMMENT '更新时间',
  INDEX idx_parent_id (`parent_id`),
  INDEX idx_creator_id (`creator_id`),
  INDEX idx_code (`code`),
  INDEX idx_sort_order (`sort_order`)
) COMMENT '意图目录表';

-- 意图表
CREATE TABLE IF NOT EXISTS `t_intent` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `catalog_id` BIGINT NOT NULL COMMENT '目录ID',
  `name` VARCHAR(128) NOT NULL COMMENT '意图名称',
  `code` VARCHAR(64) UNIQUE NOT NULL COMMENT '意图编码',
  `description` TEXT COMMENT '意图描述',
  `labels` JSON COMMENT '标签数组',
  `boundaries` JSON COMMENT '边界定义',
  `current_version_id` BIGINT COMMENT '当前活跃版本ID',
  `status` VARCHAR(32) DEFAULT 'DRAFT' COMMENT 'DRAFT/ACTIVE/DEPRECATED',
  `creator_id` BIGINT NOT NULL COMMENT '创建者ID',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
  `created_by` VARCHAR(64) COMMENT '创建者',
  `updated_by` VARCHAR(64) COMMENT '更新者',
  `created_at` BIGINT COMMENT '创建时间',
  `updated_at` BIGINT COMMENT '更新时间',
  INDEX idx_catalog_id (`catalog_id`),
  INDEX idx_status (`status`),
  INDEX idx_creator_id (`creator_id`),
  INDEX idx_code (`code`),
  INDEX idx_current_version_id (`current_version_id`),
  CONSTRAINT fk_intent_catalog FOREIGN KEY (`catalog_id`) REFERENCES `t_intent_catalog`(`id`)
) COMMENT '意图表';

-- 意图版本表
CREATE TABLE IF NOT EXISTS `t_intent_version` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `intent_id` BIGINT NOT NULL COMMENT '意图ID',
  `version_number` VARCHAR(32) NOT NULL COMMENT '版本号',
  `version_name` VARCHAR(128) COMMENT '版本名称',
  `config_snapshot` JSON COMMENT '配置快照',
  `status` VARCHAR(32) DEFAULT 'DRAFT' COMMENT 'DRAFT/REVIEW/ACTIVE/DEPRECATED',
  `sample_count` INT DEFAULT 0 COMMENT '样本数量',
  `accuracy_score` DECIMAL(5,4) COMMENT '准确率',
  `change_note` TEXT COMMENT '变更说明',
  `created_by_id` BIGINT COMMENT '创建者ID',
  `approved_by_id` BIGINT COMMENT '审批者ID',
  `approved_at` BIGINT COMMENT '审批时间',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
  `created_by` VARCHAR(64) COMMENT '创建者',
  `updated_by` VARCHAR(64) COMMENT '更新者',
  `created_at` BIGINT COMMENT '创建时间',
  `updated_at` BIGINT COMMENT '更新时间',
  UNIQUE KEY uk_intent_version (`intent_id`, `version_number`),
  INDEX idx_intent_id (`intent_id`),
  INDEX idx_status (`status`),
  INDEX idx_created_by_id (`created_by_id`),
  INDEX idx_approved_by_id (`approved_by_id`),
  CONSTRAINT fk_version_intent FOREIGN KEY (`intent_id`) REFERENCES `t_intent`(`id`)
) COMMENT '意图版本表';

-- 意图策略表
CREATE TABLE IF NOT EXISTS `t_intent_policy` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `version_id` BIGINT NOT NULL COMMENT '版本ID',
  `threshold_tau` DECIMAL(5,4) COMMENT '阈值 tau',
  `margin_delta` DECIMAL(5,4) COMMENT '边际 delta',
  `temp_t` DECIMAL(5,4) COMMENT '温度 T',
  `unknown_label` VARCHAR(128) COMMENT '未知标签',
  `channel_overrides` JSON COMMENT '渠道覆盖配置',
  `created_by` VARCHAR(64) COMMENT '创建者',
  `updated_by` VARCHAR(64) COMMENT '更新者',
  `created_at` BIGINT COMMENT '创建时间',
  `updated_at` BIGINT COMMENT '更新时间',
  UNIQUE KEY uk_version_policy (`version_id`),
  CONSTRAINT fk_policy_version FOREIGN KEY (`version_id`) REFERENCES `t_intent_version`(`id`) ON DELETE CASCADE
) COMMENT '意图策略表';

-- 意图路由表
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

-- 意图样本表
CREATE TABLE IF NOT EXISTS `t_intent_sample` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `version_id` BIGINT NOT NULL COMMENT '版本ID',
  `type` VARCHAR(32) NOT NULL COMMENT '样本类型: TRAIN/DEV/TEST/ONLINE_HARD_NEG/UNKNOWN',
  `text` TEXT NOT NULL COMMENT '文本内容',
  `slots` JSON COMMENT '插槽信息',
  `source` VARCHAR(64) DEFAULT 'manual' COMMENT '数据来源: manual/online/augment',
  `confidence_score` DOUBLE COMMENT '置信度分数',
  `annotator_id` BIGINT COMMENT '标注者ID',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
  `created_by` VARCHAR(64) COMMENT '创建者',
  `updated_by` VARCHAR(64) COMMENT '更新者',
  `created_at` BIGINT COMMENT '创建时间',
  `updated_at` BIGINT COMMENT '更新时间',
  INDEX idx_version_id (`version_id`),
  INDEX idx_type (`type`),
  INDEX idx_source (`source`),
  INDEX idx_annotator_id (`annotator_id`),
  INDEX idx_version_type (`version_id`, `type`),
  CONSTRAINT fk_sample_version FOREIGN KEY (`version_id`) REFERENCES `t_intent_version`(`id`) ON DELETE CASCADE
) COMMENT '意图样本表';

-- 意图快照表
CREATE TABLE IF NOT EXISTS `t_intent_snapshot` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `name` VARCHAR(128) NOT NULL COMMENT '快照名称',
  `code` VARCHAR(64) UNIQUE NOT NULL COMMENT '快照编码',
  `scope` VARCHAR(64) DEFAULT 'global' COMMENT '作用域',
  `scope_selector` JSON COMMENT '作用域选择器',
  `status` VARCHAR(32) DEFAULT 'DRAFT' COMMENT 'DRAFT/PUBLISHED/ACTIVE/ROLLBACK/ARCHIVED',
  `etag` VARCHAR(128) COMMENT 'ETag',
  `created_by_id` BIGINT COMMENT '创建者ID',
  `published_by_id` BIGINT COMMENT '发布者ID',
  `published_at` BIGINT COMMENT '发布时间',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
  `created_by` VARCHAR(64) COMMENT '创建者',
  `updated_by` VARCHAR(64) COMMENT '更新者',
  `created_at` BIGINT COMMENT '创建时间',
  `updated_at` BIGINT COMMENT '更新时间',
  INDEX idx_status (`status`),
  INDEX idx_scope (`scope`),
  INDEX idx_created_by_id (`created_by_id`),
  INDEX idx_published_by_id (`published_by_id`),
  INDEX idx_code (`code`),
  INDEX idx_etag (`etag`)
) COMMENT '意图快照表';

-- 意图快照项表
CREATE TABLE IF NOT EXISTS `t_intent_snapshot_item` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `snapshot_id` BIGINT NOT NULL COMMENT '快照ID',
  `version_id` BIGINT NOT NULL COMMENT '版本ID',
  `created_at` BIGINT COMMENT '创建时间',
  UNIQUE KEY uk_snapshot_version (`snapshot_id`, `version_id`),
  INDEX idx_snapshot_id (`snapshot_id`),
  INDEX idx_version_id (`version_id`),
  CONSTRAINT fk_snapshot_item_snapshot FOREIGN KEY (`snapshot_id`) REFERENCES `t_intent_snapshot`(`id`) ON DELETE CASCADE,
  CONSTRAINT fk_snapshot_item_version FOREIGN KEY (`version_id`) REFERENCES `t_intent_version`(`id`)
) COMMENT '意图快照项表';

-- 意图分类日志表
CREATE TABLE IF NOT EXISTS `t_intent_classification_log` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `session_id` VARCHAR(128) COMMENT '会话ID',
  `snapshot_id` VARCHAR(64) NOT NULL COMMENT '快照ID',
  `input_text` TEXT NOT NULL COMMENT '输入文本',
  `intent_code` VARCHAR(64) COMMENT '识别的意图编码',
  `confidence_score` DOUBLE COMMENT '置信度分数',
  `channel` VARCHAR(32) COMMENT '渠道',
  `tenant` VARCHAR(64) COMMENT '租户',
  `user_id` BIGINT COMMENT '用户ID',
  `classification_time` BIGINT COMMENT '分类时间',
  `processing_time_ms` INT COMMENT '处理时间毫秒',
  `result_data` JSON COMMENT '完整结果数据',
  `created_at` BIGINT COMMENT '创建时间',
  INDEX idx_session_id (`session_id`),
  INDEX idx_snapshot_id (`snapshot_id`),
  INDEX idx_intent_code (`intent_code`),
  INDEX idx_channel (`channel`),
  INDEX idx_tenant (`tenant`),
  INDEX idx_user_id (`user_id`),
  INDEX idx_classification_time (`classification_time`)
) COMMENT '意图分类日志表';

-- 意图模块初始化数据
INSERT INTO `t_intent_catalog` (`name`, `code`, `description`, `parent_id`, `sort_order`, `creator_id`, `is_deleted`, `created_by`, `created_at`, `updated_at`) VALUES
('默认目录', 'default', '系统默认意图目录', NULL, 0, 1, 0, 'system', UNIX_TIMESTAMP(NOW()) * 1000, UNIX_TIMESTAMP(NOW()) * 1000),
('智慧农业', 'smart_agriculture', '智慧农业相关意图', NULL, 1, 1, 0, 'system', UNIX_TIMESTAMP(NOW()) * 1000, UNIX_TIMESTAMP(NOW()) * 1000),
('智慧畜牧', 'smart_livestock', '智慧畜牧相关意图', NULL, 2, 1, 0, 'system', UNIX_TIMESTAMP(NOW()) * 1000, UNIX_TIMESTAMP(NOW()) * 1000);

-- 插入示例意图
INSERT INTO `t_intent` (`catalog_id`, `name`, `code`, `description`, `status`, `creator_id`, `is_deleted`, `created_by`, `created_at`, `updated_at`) VALUES
(1, '问候', 'greeting', '用户问候相关意图', 'DRAFT', 1, 0, 'system', UNIX_TIMESTAMP(NOW()) * 1000, UNIX_TIMESTAMP(NOW()) * 1000),
(1, '告别', 'goodbye', '用户告别相关意图', 'DRAFT', 1, 0, 'system', UNIX_TIMESTAMP(NOW()) * 1000, UNIX_TIMESTAMP(NOW()) * 1000),
(2, '病虫害识别', 'pest_identification', '农作物病虫害识别', 'DRAFT', 1, 0, 'system', UNIX_TIMESTAMP(NOW()) * 1000, UNIX_TIMESTAMP(NOW()) * 1000),
(3, '健康异常告警', 'health_alert', '牲畜健康异常告警', 'DRAFT', 1, 0, 'system', UNIX_TIMESTAMP(NOW()) * 1000, UNIX_TIMESTAMP(NOW()) * 1000);