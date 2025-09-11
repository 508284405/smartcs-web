-- LTM (长期记忆) 模块初始化脚本
-- Long-Term Memory Module Initialization

-- 情景记忆表 (Episodic Memory)
-- 存储具体时间、地点的交互事件记录
CREATE TABLE IF NOT EXISTS t_ltm_episodic_memory (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    session_id BIGINT COMMENT '会话ID，可为空表示跨会话记忆',
    episode_id VARCHAR(64) NOT NULL COMMENT '情节ID，唯一标识符',
    content LONGTEXT NOT NULL COMMENT '情节内容，包含对话片段和上下文',
    embedding_vector BLOB COMMENT '向量嵌入，用于语义检索',
    context_metadata JSON COMMENT '上下文元数据：时间、地点、情绪、参与者等',
    timestamp BIGINT NOT NULL COMMENT '发生时间戳',
    importance_score DECIMAL(4,3) DEFAULT 0.500 COMMENT '重要性评分 0.000-1.000',
    access_count INT DEFAULT 0 COMMENT '访问次数，用于计算记忆强度',
    last_accessed_at BIGINT COMMENT '最后访问时间',
    consolidation_status TINYINT DEFAULT 0 COMMENT '巩固状态 0=新记忆 1=已巩固 2=已归档',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    created_at BIGINT NOT NULL COMMENT '创建时间',
    updated_at BIGINT NOT NULL COMMENT '更新时间',
    
    UNIQUE KEY uk_episode_id (episode_id),
    INDEX idx_user_session (user_id, session_id),
    INDEX idx_timestamp (timestamp),
    INDEX idx_importance (importance_score DESC),
    INDEX idx_access (access_count DESC),
    INDEX idx_consolidation (consolidation_status),
    INDEX idx_user_importance (user_id, importance_score DESC)
) COMMENT='情景记忆表 - 存储具体交互事件';

-- 语义记忆表 (Semantic Memory) 
-- 存储从情景记忆中提取的概念性知识
CREATE TABLE IF NOT EXISTS t_ltm_semantic_memory (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    concept VARCHAR(256) NOT NULL COMMENT '概念/知识点名称',
    knowledge LONGTEXT NOT NULL COMMENT '知识内容描述',
    embedding_vector BLOB COMMENT '向量嵌入，用于语义检索',
    confidence DECIMAL(4,3) DEFAULT 0.500 COMMENT '知识置信度 0.000-1.000',
    source_episodes JSON COMMENT '来源情景记忆ID列表，追溯知识来源',
    evidence_count INT DEFAULT 1 COMMENT '支持证据数量',
    contradiction_count INT DEFAULT 0 COMMENT '矛盾证据数量',
    last_reinforced_at BIGINT COMMENT '最后强化时间',
    decay_rate DECIMAL(5,4) DEFAULT 0.0100 COMMENT '遗忘衰减率',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    created_at BIGINT NOT NULL COMMENT '创建时间',
    updated_at BIGINT NOT NULL COMMENT '更新时间',
    
    UNIQUE KEY uk_user_concept (user_id, concept),
    INDEX idx_confidence (confidence DESC),
    INDEX idx_evidence (evidence_count DESC),
    INDEX idx_reinforced (last_reinforced_at DESC),
    INDEX idx_decay (decay_rate)
) COMMENT='语义记忆表 - 存储概念性知识';

-- 程序性记忆表 (Procedural Memory)
-- 存储用户行为模式、偏好和规则
CREATE TABLE IF NOT EXISTS t_ltm_procedural_memory (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    pattern_type VARCHAR(50) NOT NULL COMMENT '模式类型：preference/rule/habit/response_style',
    pattern_name VARCHAR(256) NOT NULL COMMENT '模式名称',
    pattern_description TEXT COMMENT '模式描述',
    trigger_conditions JSON COMMENT '触发条件，JSON格式存储',
    action_template TEXT COMMENT '行为模板或响应模式',
    success_count INT DEFAULT 0 COMMENT '成功执行次数',
    failure_count INT DEFAULT 0 COMMENT '失败执行次数',
    success_rate DECIMAL(5,4) DEFAULT 0.0000 COMMENT '成功率',
    last_triggered_at BIGINT COMMENT '最后触发时间',
    learning_rate DECIMAL(4,3) DEFAULT 0.100 COMMENT '学习率，影响模式更新速度',
    is_active TINYINT DEFAULT 1 COMMENT '是否活跃 1=活跃 0=休眠',
    is_deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    created_at BIGINT NOT NULL COMMENT '创建时间',
    updated_at BIGINT NOT NULL COMMENT '更新时间',
    
    UNIQUE KEY uk_user_pattern (user_id, pattern_type, pattern_name),
    INDEX idx_pattern_type (pattern_type),
    INDEX idx_success_rate (success_rate DESC),
    INDEX idx_triggered (last_triggered_at DESC),
    INDEX idx_active (is_active, success_rate DESC)
) COMMENT='程序性记忆表 - 存储行为模式和偏好';

-- 记忆关联表 (Memory Relations)
-- 存储不同记忆类型之间的关联关系
CREATE TABLE IF NOT EXISTS t_ltm_memory_relation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    source_type ENUM('episodic', 'semantic', 'procedural') NOT NULL COMMENT '源记忆类型',
    source_id BIGINT NOT NULL COMMENT '源记忆ID',
    target_type ENUM('episodic', 'semantic', 'procedural') NOT NULL COMMENT '目标记忆类型',
    target_id BIGINT NOT NULL COMMENT '目标记忆ID',
    relation_type VARCHAR(50) NOT NULL COMMENT '关系类型：derived_from/supports/conflicts/similar',
    relation_strength DECIMAL(4,3) DEFAULT 0.500 COMMENT '关联强度 0.000-1.000',
    created_at BIGINT NOT NULL COMMENT '创建时间',
    
    UNIQUE KEY uk_memory_relation (source_type, source_id, target_type, target_id),
    INDEX idx_source (source_type, source_id),
    INDEX idx_target (target_type, target_id),
    INDEX idx_relation_type (relation_type),
    INDEX idx_strength (relation_strength DESC)
) COMMENT='记忆关联表 - 存储记忆间关系';

-- 记忆访问日志表 (Memory Access Log)
-- 记录记忆访问情况，用于学习和遗忘算法
CREATE TABLE IF NOT EXISTS t_ltm_access_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    memory_type ENUM('episodic', 'semantic', 'procedural') NOT NULL COMMENT '记忆类型',
    memory_id BIGINT NOT NULL COMMENT '记忆ID',
    access_type VARCHAR(20) NOT NULL COMMENT '访问类型：retrieve/update/reinforce',
    context_info JSON COMMENT '访问上下文信息',
    relevance_score DECIMAL(4,3) COMMENT '相关性评分',
    accessed_at BIGINT NOT NULL COMMENT '访问时间',
    
    INDEX idx_memory (memory_type, memory_id),
    INDEX idx_user_access (user_id, accessed_at DESC),
    INDEX idx_access_type (access_type),
    INDEX idx_relevance (relevance_score DESC)
) COMMENT='记忆访问日志表 - 记录访问情况';