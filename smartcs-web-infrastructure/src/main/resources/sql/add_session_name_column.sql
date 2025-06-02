-- 添加会话名称字段到会话表
ALTER TABLE t_cs_session ADD COLUMN session_name VARCHAR(50) COMMENT '会话名称';

-- 为现有数据设置默认会话名称
UPDATE t_cs_session SET session_name = CONCAT('会话 ', session_id) WHERE session_name IS NULL; 