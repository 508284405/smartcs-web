-- 添加富媒体消息功能相关表
-- 创建时间: 2024-08-29

-- 创建多媒体消息表
CREATE TABLE IF NOT EXISTS cs_media_message (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    media_id VARCHAR(64) NOT NULL UNIQUE COMMENT '多媒体ID',
    msg_id VARCHAR(64) NOT NULL COMMENT '消息ID',
    media_type INT NOT NULL COMMENT '多媒体类型 1-图片 2-语音 3-视频 4-文件 5-位置',
    file_name VARCHAR(255) DEFAULT NULL COMMENT '文件名',
    file_size BIGINT DEFAULT NULL COMMENT '文件大小（字节）',
    mime_type VARCHAR(100) DEFAULT NULL COMMENT 'MIME类型',
    file_url VARCHAR(500) DEFAULT NULL COMMENT '文件URL',
    thumbnail_url VARCHAR(500) DEFAULT NULL COMMENT '缩略图URL',
    width INT DEFAULT NULL COMMENT '宽度（图片/视频）',
    height INT DEFAULT NULL COMMENT '高度（图片/视频）',
    duration INT DEFAULT NULL COMMENT '时长（语音/视频，单位秒）',
    latitude DECIMAL(10,7) DEFAULT NULL COMMENT '位置纬度',
    longitude DECIMAL(10,7) DEFAULT NULL COMMENT '位置经度',
    address VARCHAR(500) DEFAULT NULL COMMENT '位置地址',
    location_name VARCHAR(200) DEFAULT NULL COMMENT '位置名称',
    upload_status INT NOT NULL DEFAULT 0 COMMENT '上传状态 0-上传中 1-上传成功 2-上传失败',
    upload_progress INT DEFAULT 0 COMMENT '上传进度（0-100）',
    extra_data TEXT DEFAULT NULL COMMENT '扩展数据（JSON格式）',
    is_deleted INT NOT NULL DEFAULT 0 COMMENT '逻辑删除标识',
    created_by VARCHAR(64) DEFAULT NULL COMMENT '创建者',
    updated_by VARCHAR(64) DEFAULT NULL COMMENT '更新者',
    created_at BIGINT NOT NULL COMMENT '创建时间',
    updated_at BIGINT NOT NULL COMMENT '更新时间',
    
    -- 索引
    KEY idx_media_id (media_id),
    KEY idx_msg_id (msg_id),
    KEY idx_media_type (media_type),
    KEY idx_created_by (created_by),
    KEY idx_created_at (created_at),
    KEY idx_upload_status (upload_status),
    KEY idx_file_size (file_size),
    KEY idx_location (latitude, longitude)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='多媒体消息表';

-- 创建文件上传记录表
CREATE TABLE IF NOT EXISTS cs_file_upload (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    upload_id VARCHAR(64) NOT NULL UNIQUE COMMENT '上传ID',
    user_id VARCHAR(64) NOT NULL COMMENT '用户ID',
    file_name VARCHAR(255) NOT NULL COMMENT '原始文件名',
    file_size BIGINT NOT NULL COMMENT '文件大小（字节）',
    mime_type VARCHAR(100) NOT NULL COMMENT 'MIME类型',
    file_path VARCHAR(500) DEFAULT NULL COMMENT '服务器文件路径',
    file_url VARCHAR(500) DEFAULT NULL COMMENT '访问URL',
    thumbnail_path VARCHAR(500) DEFAULT NULL COMMENT '缩略图路径',
    thumbnail_url VARCHAR(500) DEFAULT NULL COMMENT '缩略图URL',
    upload_status INT NOT NULL DEFAULT 0 COMMENT '上传状态 0-上传中 1-上传成功 2-上传失败',
    upload_progress INT DEFAULT 0 COMMENT '上传进度（0-100）',
    error_message VARCHAR(500) DEFAULT NULL COMMENT '错误信息',
    chunk_size INT DEFAULT NULL COMMENT '分片大小（字节）',
    total_chunks INT DEFAULT NULL COMMENT '总分片数',
    uploaded_chunks INT DEFAULT 0 COMMENT '已上传分片数',
    file_hash VARCHAR(64) DEFAULT NULL COMMENT '文件哈希值（用于去重）',
    expires_at BIGINT DEFAULT NULL COMMENT '过期时间（临时文件）',
    is_deleted INT NOT NULL DEFAULT 0 COMMENT '逻辑删除标识',
    created_by VARCHAR(64) DEFAULT NULL COMMENT '创建者',
    updated_by VARCHAR(64) DEFAULT NULL COMMENT '更新者',
    created_at BIGINT NOT NULL COMMENT '创建时间',
    updated_at BIGINT NOT NULL COMMENT '更新时间',
    
    -- 索引
    KEY idx_upload_id (upload_id),
    KEY idx_user_id (user_id),
    KEY idx_upload_status (upload_status),
    KEY idx_file_hash (file_hash),
    KEY idx_created_at (created_at),
    KEY idx_expires_at (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件上传记录表';

-- 创建文件存储配置表
CREATE TABLE IF NOT EXISTS cs_file_storage_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    config_key VARCHAR(100) NOT NULL UNIQUE COMMENT '配置键',
    config_value TEXT NOT NULL COMMENT '配置值（JSON格式）',
    config_desc VARCHAR(255) DEFAULT NULL COMMENT '配置描述',
    is_enabled INT NOT NULL DEFAULT 1 COMMENT '是否启用',
    is_deleted INT NOT NULL DEFAULT 0 COMMENT '逻辑删除标识',
    created_by VARCHAR(64) DEFAULT NULL COMMENT '创建者',
    updated_by VARCHAR(64) DEFAULT NULL COMMENT '更新者',
    created_at BIGINT NOT NULL COMMENT '创建时间',
    updated_at BIGINT NOT NULL COMMENT '更新时间',
    
    -- 索引
    KEY idx_config_key (config_key),
    KEY idx_is_enabled (is_enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件存储配置表';

-- 插入默认存储配置
INSERT INTO cs_file_storage_config (config_key, config_value, config_desc, created_at, updated_at, created_by)
VALUES 
    ('local_storage', '{"path": "/data/files", "maxFileSize": 104857600, "allowedTypes": ["image/*", "audio/*", "video/*", "application/*"]}', '本地存储配置', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'system'),
    ('image_compress', '{"enabled": true, "quality": 85, "maxWidth": 1920, "maxHeight": 1080, "thumbnailWidth": 300, "thumbnailHeight": 300}', '图片压缩配置', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'system'),
    ('audio_transcode', '{"enabled": true, "format": "mp3", "bitrate": "128k", "maxDuration": 300}', '音频转码配置', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'system'),
    ('file_quota', '{"perUserLimit": 1073741824, "perFileLimit": 104857600, "allowedExtensions": [".jpg", ".png", ".gif", ".mp3", ".mp4", ".pdf", ".doc", ".docx", ".txt"]}', '文件配额配置', UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'system');

-- 为现有消息表添加多媒体相关字段
ALTER TABLE t_cs_message 
ADD COLUMN has_media INT DEFAULT 0 COMMENT '是否包含多媒体内容 0-否 1-是',
ADD COLUMN media_count INT DEFAULT 0 COMMENT '多媒体数量',
ADD INDEX idx_has_media (has_media);

-- 创建定时清理任务（清理过期的临时文件）
CREATE EVENT IF NOT EXISTS cleanup_expired_files
ON SCHEDULE EVERY 1 HOUR
DO
  UPDATE cs_file_upload 
  SET is_deleted = 1, updated_at = UNIX_TIMESTAMP() * 1000 
  WHERE expires_at IS NOT NULL 
  AND expires_at <= UNIX_TIMESTAMP() * 1000 
  AND is_deleted = 0;

-- 创建文件统计视图
CREATE VIEW v_file_usage_stats AS
SELECT 
    user_id,
    COUNT(*) as total_files,
    SUM(file_size) as total_size,
    SUM(CASE WHEN mime_type LIKE 'image/%' THEN 1 ELSE 0 END) as image_count,
    SUM(CASE WHEN mime_type LIKE 'audio/%' THEN 1 ELSE 0 END) as audio_count,
    SUM(CASE WHEN mime_type LIKE 'video/%' THEN 1 ELSE 0 END) as video_count,
    SUM(CASE WHEN mime_type NOT LIKE 'image/%' AND mime_type NOT LIKE 'audio/%' AND mime_type NOT LIKE 'video/%' THEN 1 ELSE 0 END) as other_count
FROM cs_file_upload 
WHERE is_deleted = 0 AND upload_status = 1
GROUP BY user_id;