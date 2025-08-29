-- 消息撤回功能相关字段扩展
-- 为t_cs_message表添加撤回相关字段

ALTER TABLE t_cs_message 
ADD COLUMN is_recalled TINYINT(1) DEFAULT 0 COMMENT '是否已撤回 0-未撤回 1-已撤回',
ADD COLUMN recalled_at BIGINT COMMENT '撤回时间戳',
ADD COLUMN recalled_by VARCHAR(255) COMMENT '撤回操作者ID',
ADD COLUMN recall_reason VARCHAR(500) COMMENT '撤回原因',
ADD INDEX idx_is_recalled (is_recalled),
ADD INDEX idx_recalled_at (recalled_at);

-- 创建消息状态枚举说明
-- is_recalled: 0-正常消息, 1-已撤回消息
-- recalled_at: 撤回操作的时间戳
-- recalled_by: 执行撤回操作的用户ID
-- recall_reason: 撤回原因（可选，用于系统撤回等场景）