-- 聊天模块 - 会话表
-- cs_session表
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