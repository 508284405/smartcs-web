-- 为消息表添加编辑相关字段
ALTER TABLE t_cs_message 
ADD COLUMN is_edited TINYINT DEFAULT 0 COMMENT '是否已编辑 0-未编辑 1-已编辑',
ADD COLUMN edited_at BIGINT NULL COMMENT '编辑时间戳',
ADD COLUMN original_content TEXT NULL COMMENT '原始内容（用于编辑历史）',
ADD COLUMN edit_count INT DEFAULT 0 COMMENT '编辑次数';

-- 为编辑相关字段添加索引，提高查询效率
CREATE INDEX idx_is_edited ON t_cs_message(is_edited);
CREATE INDEX idx_edited_at ON t_cs_message(edited_at);

-- 更新现有记录的默认值（确保已存在的消息的编辑状态为未编辑）
UPDATE t_cs_message SET is_edited = 0, edit_count = 0 WHERE is_edited IS NULL;