-- 聊天模块 - 用户表
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
    updated_at BIGINT COMMENT '更新时间',
    INDEX idx_user_id (user_id),
    INDEX idx_status (status)
) COMMENT='用户数据对象';