-- 添加消息表情反应表
-- 创建时间: 2024-08-29

-- 创建消息表情反应表
CREATE TABLE IF NOT EXISTS cs_message_reaction (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    msg_id VARCHAR(64) NOT NULL COMMENT '消息ID',
    session_id VARCHAR(64) NOT NULL COMMENT '会话ID',
    user_id VARCHAR(64) NOT NULL COMMENT '用户ID',
    reaction_emoji VARCHAR(10) NOT NULL COMMENT '表情符号',
    reaction_name VARCHAR(32) NOT NULL COMMENT '表情名称',
    created_at BIGINT NOT NULL COMMENT '添加时间戳',
    updated_at BIGINT NOT NULL COMMENT '更新时间戳',
    created_by VARCHAR(64) DEFAULT NULL COMMENT '创建者',
    updated_by VARCHAR(64) DEFAULT NULL COMMENT '更新者',
    
    -- 索引
    KEY idx_msg_id (msg_id),
    KEY idx_session_id (session_id),
    KEY idx_user_id (user_id),
    KEY idx_msg_emoji (msg_id, reaction_emoji),
    UNIQUE KEY uk_msg_user_reaction (msg_id, user_id, reaction_emoji)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息表情反应表';

-- 为现有cs_message表添加反应统计字段
ALTER TABLE cs_message 
ADD COLUMN reaction_count INT DEFAULT 0 COMMENT '表情反应总数',
ADD COLUMN reactions_summary JSON DEFAULT NULL COMMENT '表情反应摘要统计';

-- 添加索引优化查询
CREATE INDEX idx_cs_message_reaction_count ON cs_message(reaction_count);