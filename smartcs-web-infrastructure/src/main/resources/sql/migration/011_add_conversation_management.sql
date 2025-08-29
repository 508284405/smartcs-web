-- 添加会话管理功能相关表
-- 创建时间: 2024-08-29

-- 创建会话设置表
CREATE TABLE IF NOT EXISTS cs_conversation_settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id VARCHAR(64) NOT NULL COMMENT '用户ID',
    session_id BIGINT NOT NULL COMMENT '会话ID',
    is_pinned INT NOT NULL DEFAULT 0 COMMENT '是否置顶 0-否 1-是',
    pinned_at BIGINT DEFAULT NULL COMMENT '置顶时间',
    is_muted INT NOT NULL DEFAULT 0 COMMENT '是否免打扰 0-否 1-是',
    muted_at BIGINT DEFAULT NULL COMMENT '免打扰开始时间',
    mute_end_at BIGINT DEFAULT NULL COMMENT '免打扰结束时间（null表示永久）',
    is_archived INT NOT NULL DEFAULT 0 COMMENT '是否归档 0-否 1-是',
    archived_at BIGINT DEFAULT NULL COMMENT '归档时间',
    custom_background VARCHAR(255) DEFAULT NULL COMMENT '自定义背景',
    notification_sound VARCHAR(100) DEFAULT NULL COMMENT '自定义通知声音',
    created_at BIGINT NOT NULL COMMENT '创建时间',
    updated_at BIGINT NOT NULL COMMENT '更新时间',
    created_by VARCHAR(64) DEFAULT NULL COMMENT '创建者',
    updated_by VARCHAR(64) DEFAULT NULL COMMENT '更新者',
    
    -- 索引
    KEY idx_user_id (user_id),
    KEY idx_session_id (session_id),
    KEY idx_is_pinned (is_pinned),
    KEY idx_is_muted (is_muted),
    KEY idx_is_archived (is_archived),
    KEY idx_pinned_at (pinned_at),
    KEY idx_muted_at (muted_at),
    KEY idx_archived_at (archived_at),
    KEY idx_mute_end_at (mute_end_at),
    
    -- 唯一约束：同一用户对同一会话只能有一条设置记录
    UNIQUE KEY uk_user_session (user_id, session_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会话设置表';

-- 创建消息搜索索引表（用于全文搜索优化）
CREATE TABLE IF NOT EXISTS cs_message_search_index (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    msg_id VARCHAR(64) NOT NULL COMMENT '消息ID',
    session_id BIGINT NOT NULL COMMENT '会话ID',
    user_id VARCHAR(64) NOT NULL COMMENT '用户ID（消息可见用户）',
    content_text TEXT NOT NULL COMMENT '消息文本内容',
    msg_type INT NOT NULL COMMENT '消息类型',
    created_at BIGINT NOT NULL COMMENT '消息创建时间',
    updated_at BIGINT NOT NULL COMMENT '索引更新时间',
    
    -- 索引
    KEY idx_msg_id (msg_id),
    KEY idx_session_id (session_id),
    KEY idx_user_id (user_id),
    KEY idx_created_at (created_at),
    KEY idx_msg_type (msg_type),
    
    -- 全文搜索索引
    FULLTEXT KEY idx_content_text (content_text),
    
    -- 唯一约束
    UNIQUE KEY uk_msg_user (msg_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息搜索索引表';

-- 创建会话标签表
CREATE TABLE IF NOT EXISTS cs_conversation_tag (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id VARCHAR(64) NOT NULL COMMENT '用户ID',
    tag_name VARCHAR(32) NOT NULL COMMENT '标签名称',
    tag_color VARCHAR(7) DEFAULT NULL COMMENT '标签颜色',
    tag_order INT DEFAULT 0 COMMENT '标签排序',
    created_at BIGINT NOT NULL COMMENT '创建时间',
    updated_at BIGINT NOT NULL COMMENT '更新时间',
    created_by VARCHAR(64) DEFAULT NULL COMMENT '创建者',
    updated_by VARCHAR(64) DEFAULT NULL COMMENT '更新者',
    
    -- 索引
    KEY idx_user_id (user_id),
    KEY idx_tag_order (tag_order),
    
    -- 唯一约束：同一用户的标签名不能重复
    UNIQUE KEY uk_user_tag (user_id, tag_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会话标签表';

-- 创建会话标签关联表
CREATE TABLE IF NOT EXISTS cs_conversation_tag_rel (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id VARCHAR(64) NOT NULL COMMENT '用户ID',
    session_id BIGINT NOT NULL COMMENT '会话ID',
    tag_id BIGINT NOT NULL COMMENT '标签ID',
    created_at BIGINT NOT NULL COMMENT '创建时间',
    created_by VARCHAR(64) DEFAULT NULL COMMENT '创建者',
    
    -- 索引
    KEY idx_user_id (user_id),
    KEY idx_session_id (session_id),
    KEY idx_tag_id (tag_id),
    
    -- 唯一约束：同一用户的同一会话不能关联同一标签多次
    UNIQUE KEY uk_user_session_tag (user_id, session_id, tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会话标签关联表';

-- 插入默认标签
INSERT INTO cs_conversation_tag (user_id, tag_name, tag_color, tag_order, created_at, updated_at, created_by, updated_by)
VALUES 
    ('system', '重要', '#f56c6c', 1, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'system', 'system'),
    ('system', '工作', '#409eff', 2, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'system', 'system'),
    ('system', '学习', '#67c23a', 3, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'system', 'system'),
    ('system', '娱乐', '#e6a23c', 4, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'system', 'system');

-- 为现有会话表添加扩展字段
ALTER TABLE cs_session 
ADD COLUMN last_message_preview VARCHAR(500) DEFAULT NULL COMMENT '最后消息预览',
ADD COLUMN participant_count INT DEFAULT 2 COMMENT '参与者数量',
ADD COLUMN session_type INT DEFAULT 1 COMMENT '会话类型 1-私聊 2-群聊 3-系统',
ADD COLUMN session_avatar VARCHAR(255) DEFAULT NULL COMMENT '会话头像';

-- 优化索引
CREATE INDEX idx_cs_conversation_settings_composite ON cs_conversation_settings(user_id, is_archived, is_pinned, updated_at);
CREATE INDEX idx_cs_message_search_composite ON cs_message_search_index(user_id, session_id, created_at);

-- 创建定时任务表（用于清理过期数据）
CREATE EVENT IF NOT EXISTS cleanup_expired_mute_settings
ON SCHEDULE EVERY 1 HOUR
DO
  UPDATE cs_conversation_settings 
  SET is_muted = 0, updated_at = UNIX_TIMESTAMP() * 1000 
  WHERE is_muted = 1 AND mute_end_at IS NOT NULL AND mute_end_at <= UNIX_TIMESTAMP() * 1000;