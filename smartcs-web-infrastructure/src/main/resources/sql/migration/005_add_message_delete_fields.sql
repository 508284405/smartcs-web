-- 添加消息删除相关字段
ALTER TABLE t_cs_message 
ADD COLUMN is_deleted_by_sender TINYINT(1) DEFAULT 0 COMMENT '是否被发送者删除 0-未删除 1-已删除',
ADD COLUMN is_deleted_by_receiver TINYINT(1) DEFAULT 0 COMMENT '是否被接收者删除 0-未删除 1-已删除',
ADD COLUMN deleted_by_sender_at BIGINT COMMENT '发送者删除时间戳',
ADD COLUMN deleted_by_receiver_at BIGINT COMMENT '接收者删除时间戳',
ADD COLUMN delete_type TINYINT(1) DEFAULT 0 COMMENT '删除类型 0-仅自己可见删除 1-双方删除',
ADD COLUMN deleted_reason VARCHAR(500) COMMENT '删除原因';

-- 添加索引以优化删除状态查询
CREATE INDEX idx_message_deleted_sender ON t_cs_message(is_deleted_by_sender);
CREATE INDEX idx_message_deleted_receiver ON t_cs_message(is_deleted_by_receiver);