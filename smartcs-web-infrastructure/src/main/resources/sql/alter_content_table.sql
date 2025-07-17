-- 为知识内容表增加新字段
ALTER TABLE `t_kb_content` 
ADD COLUMN `segment_mode` VARCHAR(32) DEFAULT 'general' COMMENT '分段模式 general/parent_child' AFTER `status`,
ADD COLUMN `char_count` BIGINT DEFAULT 0 COMMENT '字符数' AFTER `segment_mode`,
ADD COLUMN `recall_count` BIGINT DEFAULT 0 COMMENT '召回次数' AFTER `char_count`;

-- 为字段添加索引
CREATE INDEX idx_segment_mode ON `t_kb_content`(`segment_mode`);
CREATE INDEX idx_char_count ON `t_kb_content`(`char_count`);
CREATE INDEX idx_recall_count ON `t_kb_content`(`recall_count`); 