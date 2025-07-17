-- cs_user表
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
    updated_at BIGINT COMMENT '更新时间'
) COMMENT='用户数据对象';

-- cs_message表
CREATE TABLE IF NOT EXISTS t_cs_message (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    msg_id BIGINT COMMENT '消息ID',
    session_id BIGINT COMMENT '会话ID',
    sender_id BIGINT COMMENT '发送者ID',
    sender_role INT COMMENT '发送者角色 0=用户 1=客服 2=机器人',
    msg_type INT COMMENT '消息类型 0=text 1=image 2=order_card 3=system',
    content VARCHAR(255) COMMENT '消息内容，JSON格式存储富文本',
    at_list JSON COMMENT '@提及的用户列表',
    is_deleted INT DEFAULT 0 COMMENT '逻辑删除',
    created_by VARCHAR(255) COMMENT '创建人',
    updated_by VARCHAR(255) COMMENT '更新人',
    created_at BIGINT COMMENT '创建时间',
    updated_at BIGINT COMMENT '更新时间'
) COMMENT='消息数据对象';

-- cs_session表
CREATE TABLE IF NOT EXISTS t_cs_session (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    session_id BIGINT COMMENT '会话ID',
    t_cs_session ADD COLUMN session_name VARCHAR(50) COMMENT '会话名称',
    customer_id BIGINT COMMENT '客户ID',
    agent_id BIGINT COMMENT '客服ID',
    session_state INT COMMENT '会话状态 0=排队 1=进行中 2=已结束',
    last_msg_time BIGINT COMMENT '最后消息时间',
    is_deleted INT DEFAULT 0 COMMENT '逻辑删除',
    created_by VARCHAR(255) COMMENT '创建人',
    updated_by VARCHAR(255) COMMENT '更新人',
    created_at BIGINT COMMENT '创建时间',
    updated_at BIGINT COMMENT '更新时间'
) COMMENT='会话数据对象'; 