-- LTM配置相关表

-- 用户LTM配置表
-- 存储每个用户的LTM系统个性化配置
CREATE TABLE IF NOT EXISTS t_ltm_user_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    ltm_enabled TINYINT DEFAULT 1 COMMENT 'LTM功能是否启用',
    max_episodic_memories INT DEFAULT 10000 COMMENT '最大情景记忆数量',
    max_semantic_concepts INT DEFAULT 1000 COMMENT '最大语义概念数量',
    max_procedural_patterns INT DEFAULT 500 COMMENT '最大程序性模式数量',
    episodic_retention_days INT DEFAULT 90 COMMENT '情景记忆保留天数',
    semantic_decay_enabled TINYINT DEFAULT 1 COMMENT '是否启用语义遗忘',
    procedural_learning_enabled TINYINT DEFAULT 1 COMMENT '是否启用程序性学习',
    privacy_level TINYINT DEFAULT 1 COMMENT '隐私级别 1=普通 2=敏感 3=严格',
    auto_consolidation TINYINT DEFAULT 1 COMMENT '是否启用自动巩固',
    personalization_level DECIMAL(3,2) DEFAULT 0.70 COMMENT '个性化强度 0.00-1.00',
    memory_sharing_enabled TINYINT DEFAULT 0 COMMENT '是否允许记忆共享',
    created_at BIGINT NOT NULL COMMENT '创建时间',
    updated_at BIGINT NOT NULL COMMENT '更新时间',
    
    UNIQUE KEY uk_user_id (user_id),
    INDEX idx_ltm_enabled (ltm_enabled),
    INDEX idx_privacy_level (privacy_level)
) COMMENT='用户LTM配置表';

-- LTM系统配置表
-- 存储全局LTM系统配置参数
CREATE TABLE IF NOT EXISTS t_ltm_system_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    config_key VARCHAR(100) NOT NULL COMMENT '配置键名',
    config_value TEXT NOT NULL COMMENT '配置值',
    config_type VARCHAR(20) DEFAULT 'string' COMMENT '配置类型：string/number/boolean/json',
    description TEXT COMMENT '配置说明',
    is_active TINYINT DEFAULT 1 COMMENT '是否生效',
    updated_by VARCHAR(100) COMMENT '更新人',
    created_at BIGINT NOT NULL COMMENT '创建时间',
    updated_at BIGINT NOT NULL COMMENT '更新时间',
    
    UNIQUE KEY uk_config_key (config_key),
    INDEX idx_active (is_active)
) COMMENT='LTM系统配置表';

-- 插入默认系统配置
INSERT INTO t_ltm_system_config (config_key, config_value, config_type, description, created_at, updated_at) VALUES
('ltm.consolidation.schedule', '0 0 2 * * ?', 'string', 'LTM记忆巩固定时任务执行时间', UNIX_TIMESTAMP(NOW()) * 1000, UNIX_TIMESTAMP(NOW()) * 1000),
('ltm.consolidation.batch_size', '1000', 'number', '每次巩固处理的记忆数量', UNIX_TIMESTAMP(NOW()) * 1000, UNIX_TIMESTAMP(NOW()) * 1000),
('ltm.episodic.importance_threshold', '0.3', 'number', '情景记忆重要性阈值', UNIX_TIMESTAMP(NOW()) * 1000, UNIX_TIMESTAMP(NOW()) * 1000),
('ltm.semantic.extraction_threshold', '0.7', 'number', '语义知识提取置信度阈值', UNIX_TIMESTAMP(NOW()) * 1000, UNIX_TIMESTAMP(NOW()) * 1000),
('ltm.procedural.learning_rate', '0.1', 'number', '程序性记忆学习率', UNIX_TIMESTAMP(NOW()) * 1000, UNIX_TIMESTAMP(NOW()) * 1000),
('ltm.vector.dimension', '1536', 'number', '向量维度（与embedding模型匹配）', UNIX_TIMESTAMP(NOW()) * 1000, UNIX_TIMESTAMP(NOW()) * 1000),
('ltm.retrieval.max_results', '10', 'number', '记忆检索最大返回数量', UNIX_TIMESTAMP(NOW()) * 1000, UNIX_TIMESTAMP(NOW()) * 1000),
('ltm.retrieval.similarity_threshold', '0.8', 'number', '记忆检索相似度阈值', UNIX_TIMESTAMP(NOW()) * 1000, UNIX_TIMESTAMP(NOW()) * 1000);

-- LTM统计信息表
-- 存储LTM系统的统计和监控数据
CREATE TABLE IF NOT EXISTS t_ltm_statistics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    user_id BIGINT COMMENT '用户ID，为空表示系统级统计',
    stat_date DATE NOT NULL COMMENT '统计日期',
    total_episodic_memories BIGINT DEFAULT 0 COMMENT '情景记忆总数',
    total_semantic_concepts BIGINT DEFAULT 0 COMMENT '语义概念总数',
    total_procedural_patterns BIGINT DEFAULT 0 COMMENT '程序性模式总数',
    memory_retrieval_count BIGINT DEFAULT 0 COMMENT '记忆检索次数',
    memory_formation_count BIGINT DEFAULT 0 COMMENT '记忆形成次数',
    memory_consolidation_count BIGINT DEFAULT 0 COMMENT '记忆巩固次数',
    average_importance_score DECIMAL(4,3) DEFAULT 0 COMMENT '平均重要性评分',
    active_procedural_patterns BIGINT DEFAULT 0 COMMENT '活跃程序性模式数',
    storage_size_mb DECIMAL(10,2) DEFAULT 0 COMMENT '存储空间占用(MB)',
    created_at BIGINT NOT NULL COMMENT '创建时间',
    
    UNIQUE KEY uk_user_date (IFNULL(user_id, 0), stat_date),
    INDEX idx_stat_date (stat_date DESC),
    INDEX idx_user_id (user_id)
) COMMENT='LTM统计信息表';