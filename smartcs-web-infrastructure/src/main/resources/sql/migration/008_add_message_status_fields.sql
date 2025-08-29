-- 为消息表添加消息状态相关字段
ALTER TABLE t_cs_message 
ADD COLUMN send_status TINYINT DEFAULT 0 COMMENT '消息发送状态 0-发送中 1-已送达 2-发送失败 3-已读',
ADD COLUMN send_fail_reason VARCHAR(500) NULL COMMENT '发送失败原因',
ADD COLUMN retry_count INT DEFAULT 0 COMMENT '重试次数';

-- 为消息状态相关字段添加索引，提高查询效率
CREATE INDEX idx_send_status ON t_cs_message(send_status);
CREATE INDEX idx_retry_count ON t_cs_message(retry_count);

-- 为失败消息查询添加复合索引
CREATE INDEX idx_failed_messages ON t_cs_message(session_id, send_status, created_at);

-- 为发送状态统计添加复合索引
CREATE INDEX idx_status_stats ON t_cs_message(from_user_id, send_status, created_at);

-- 更新现有记录的默认值（将已存在的消息状态设置为已送达）
UPDATE t_cs_message SET send_status = 1, retry_count = 0 WHERE send_status IS NULL;