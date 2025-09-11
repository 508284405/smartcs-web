-- 聊天模块 - 消息表
-- cs_message表
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