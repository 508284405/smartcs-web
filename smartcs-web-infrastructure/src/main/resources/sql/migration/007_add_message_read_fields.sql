-- 为消息表添加已读回执相关字段
ALTER TABLE t_cs_message 
ADD COLUMN is_read TINYINT DEFAULT 0 COMMENT '是否已读 0-未读 1-已读',
ADD COLUMN read_at BIGINT NULL COMMENT '读取时间戳',
ADD COLUMN read_by VARCHAR(255) NULL COMMENT '读取者ID';

-- 为已读相关字段添加索引，提高查询效率
CREATE INDEX idx_is_read ON t_cs_message(is_read);
CREATE INDEX idx_read_at ON t_cs_message(read_at);
CREATE INDEX idx_read_by ON t_cs_message(read_by);

-- 为会话未读消息查询添加复合索引
CREATE INDEX idx_session_unread ON t_cs_message(session_id, is_read, is_deleted);

-- 更新现有记录的默认值（确保已存在的消息的已读状态为未读）
UPDATE t_cs_message SET is_read = 0 WHERE is_read IS NULL;