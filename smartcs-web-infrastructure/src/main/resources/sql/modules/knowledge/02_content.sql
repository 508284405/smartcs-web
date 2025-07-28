-- 知识模块 - 内容表
CREATE TABLE IF NOT EXISTS `t_kb_content` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `knowledge_base_id` BIGINT NOT NULL COMMENT '所属知识库ID',
  `title` VARCHAR(256) NOT NULL COMMENT '标题',
  `content_type` VARCHAR(32) NOT NULL COMMENT 'document/audio/video',
  `file_url` VARCHAR(512) COMMENT '原始文件地址',
  `file_type` VARCHAR(256) COMMENT '文件扩展名',
  `status` VARCHAR(32) DEFAULT 'enabled' COMMENT 'enabled/disabled',
  `segment_mode` VARCHAR(32) DEFAULT 'general' COMMENT '分段模式 general/parent_child',
  `char_count` BIGINT DEFAULT 0 COMMENT '字符数',
  `recall_count` BIGINT DEFAULT 0 COMMENT '召回次数',
  `is_deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
  `created_by` VARCHAR(64) COMMENT '创建者',
  `updated_by` VARCHAR(64) COMMENT '更新者',
  `created_at` BIGINT COMMENT '创建时间',
  `updated_at` BIGINT COMMENT '更新时间',
  INDEX idx_knowledge_base_id (`knowledge_base_id`),
  INDEX idx_content_type (`content_type`),
  INDEX idx_status (`status`)
) COMMENT '知识内容表';