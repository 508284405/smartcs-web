-- 聊天模块 - 离线消息表 & 未读计数表（MVP）

-- 离线消息表：用户离线期间未送达的消息
CREATE TABLE IF NOT EXISTS t_im_offline_message (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    receiver_id BIGINT NOT NULL COMMENT '接收者用户ID',
    conversation_id VARCHAR(64) NOT NULL COMMENT '会话标识（私聊用sessionId；群聊用groupId）',
    msg_id VARCHAR(255) NOT NULL COMMENT '消息ID（引用消息表）',
    msg_brief VARCHAR(255) COMMENT '消息摘要（用于快速列表展示）',
    created_at BIGINT NOT NULL COMMENT '入库时间',
    is_deleted INT DEFAULT 0 COMMENT '逻辑删除',
    UNIQUE KEY uk_receiver_msg (receiver_id, msg_id),
    KEY idx_receiver (receiver_id),
    KEY idx_conversation (conversation_id)
) COMMENT='离线消息表';

-- 未读计数表：每个用户按会话维度的未读数
CREATE TABLE IF NOT EXISTS t_im_unread_counter (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    conversation_id VARCHAR(64) NOT NULL COMMENT '会话标识（sessionId/groupId）',
    unread_count INT NOT NULL DEFAULT 0 COMMENT '未读计数',
    updated_at BIGINT NOT NULL COMMENT '更新时间',
    UNIQUE KEY uk_user_conv (user_id, conversation_id),
    KEY idx_user (user_id)
) COMMENT='未读计数表（可选，Redis 为主 DB 兜底）';

