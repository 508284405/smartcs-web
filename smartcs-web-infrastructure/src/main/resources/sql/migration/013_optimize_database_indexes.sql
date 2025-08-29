-- 数据库索引优化方案
-- 创建时间: 2024-08-29

-- ==================== 消息表索引优化 ====================

-- 1. 优化现有消息表的复合索引
DROP INDEX IF EXISTS idx_session_timestamp ON t_cs_message;
CREATE INDEX CONCURRENTLY idx_session_timestamp_optimized 
ON t_cs_message (session_id, created_at DESC, is_deleted) 
WHERE is_deleted = 0;

-- 2. 添加消息状态相关的复合索引
CREATE INDEX CONCURRENTLY idx_message_status_timeline
ON t_cs_message (send_status, created_at DESC, session_id)
WHERE is_deleted = 0 AND is_recalled = 0;

-- 3. 优化撤回消息查询索引
CREATE INDEX CONCURRENTLY idx_message_recall_check
ON t_cs_message (created_by, is_recalled, recalled_at, created_at)
WHERE is_deleted = 0;

-- 4. 已读状态索引优化
CREATE INDEX CONCURRENTLY idx_message_read_status
ON t_cs_message (session_id, is_read, read_at, created_at DESC)
WHERE is_deleted = 0;

-- 5. 分页查询优化索引
CREATE INDEX CONCURRENTLY idx_message_pagination
ON t_cs_message (session_id, id DESC)
WHERE is_deleted = 0 AND is_recalled = 0;

-- ==================== 会话表索引优化 ====================

-- 1. 用户会话查询优化
CREATE INDEX CONCURRENTLY idx_session_user_activity
ON cs_session (user_one_id, updated_at DESC, is_deleted)
WHERE is_deleted = 0;

CREATE INDEX CONCURRENTLY idx_session_user_two_activity  
ON cs_session (user_two_id, updated_at DESC, is_deleted)
WHERE is_deleted = 0;

-- 2. 会话状态复合索引
CREATE INDEX CONCURRENTLY idx_session_status_composite
ON cs_session (session_status, session_type, updated_at DESC)
WHERE is_deleted = 0;

-- ==================== 好友系统索引优化 ====================

-- 1. 好友关系查询优化
CREATE INDEX CONCURRENTLY idx_friend_relationship_optimized
ON cs_friend (user_id, friend_status, created_at DESC)
WHERE is_deleted = 0;

-- 2. 好友申请处理索引
CREATE INDEX CONCURRENTLY idx_friend_request_pending
ON cs_friend (friend_id, friend_status, created_at DESC)
WHERE friend_status = 0 AND is_deleted = 0;

-- 3. 黑名单查询优化
CREATE INDEX CONCURRENTLY idx_blacklist_check
ON cs_blacklist (user_id, blocked_user_id, is_deleted)
WHERE is_deleted = 0;

-- ==================== 多媒体消息索引优化 ====================

-- 1. 媒体消息类型查询
CREATE INDEX CONCURRENTLY idx_media_message_type
ON cs_media_message (msg_id, media_type, upload_status)
WHERE is_deleted = 0;

-- 2. 文件大小统计索引
CREATE INDEX CONCURRENTLY idx_media_file_stats
ON cs_media_message (created_by, file_size, media_type, created_at)
WHERE is_deleted = 0 AND upload_status = 1;

-- 3. 位置消息查询索引
CREATE INDEX CONCURRENTLY idx_media_location
ON cs_media_message (media_type, latitude, longitude)
WHERE media_type = 5 AND is_deleted = 0;

-- ==================== 消息搜索索引优化 ====================

-- 1. 全文搜索内容索引（如果支持）
ALTER TABLE t_cs_message ADD FULLTEXT(content);

-- 2. 搜索过滤复合索引
CREATE INDEX CONCURRENTLY idx_message_search_filter
ON t_cs_message (created_by, msg_type, created_at DESC, session_id)
WHERE is_deleted = 0 AND is_recalled = 0;

-- ==================== 会话设置索引优化 ====================

-- 1. 用户会话设置查询
CREATE INDEX CONCURRENTLY idx_conversation_settings_user
ON cs_conversation_settings (user_id, session_id, is_deleted)
WHERE is_deleted = 0;

-- 2. 置顶会话查询
CREATE INDEX CONCURRENTLY idx_conversation_pinned
ON cs_conversation_settings (user_id, is_pinned, pinned_at DESC)
WHERE is_pinned = 1 AND is_deleted = 0;

-- 3. 免打扰设置查询
CREATE INDEX CONCURRENTLY idx_conversation_muted
ON cs_conversation_settings (user_id, is_muted, mute_end_at)
WHERE is_muted = 1 AND is_deleted = 0;

-- ==================== 性能监控索引 ====================

-- 1. 创建性能监控视图
CREATE OR REPLACE VIEW v_message_performance_stats AS
SELECT 
    DATE(FROM_UNIXTIME(created_at/1000)) as date,
    COUNT(*) as total_messages,
    COUNT(CASE WHEN send_status = 1 THEN 1 END) as delivered_messages,
    COUNT(CASE WHEN send_status = 2 THEN 1 END) as failed_messages,
    COUNT(CASE WHEN is_recalled = 1 THEN 1 END) as recalled_messages,
    AVG(CASE WHEN send_status = 1 THEN TIMESTAMPDIFF(MICROSECOND, FROM_UNIXTIME(created_at/1000), FROM_UNIXTIME(updated_at/1000))/1000 END) as avg_delivery_time_ms
FROM t_cs_message 
WHERE is_deleted = 0 
    AND created_at >= UNIX_TIMESTAMP(DATE_SUB(NOW(), INTERVAL 30 DAY)) * 1000
GROUP BY DATE(FROM_UNIXTIME(created_at/1000))
ORDER BY date DESC;

-- 2. 用户活跃度统计视图
CREATE OR REPLACE VIEW v_user_activity_stats AS
SELECT 
    created_by as user_id,
    DATE(FROM_UNIXTIME(created_at/1000)) as date,
    COUNT(*) as messages_sent,
    COUNT(DISTINCT session_id) as sessions_active,
    MIN(created_at) as first_message_time,
    MAX(created_at) as last_message_time
FROM t_cs_message
WHERE is_deleted = 0 
    AND created_at >= UNIX_TIMESTAMP(DATE_SUB(NOW(), INTERVAL 7 DAY)) * 1000
GROUP BY created_by, DATE(FROM_UNIXTIME(created_at/1000));

-- ==================== 分区表准备（未来扩展） ====================

-- 为消息表创建分区准备（按月分区）
-- 注意：这需要在业务低峰期执行，且需要停机操作

-- 1. 创建新的分区消息表结构
CREATE TABLE t_cs_message_partitioned (
    LIKE t_cs_message INCLUDING ALL
) PARTITION BY RANGE (YEAR(FROM_UNIXTIME(created_at/1000)) * 100 + MONTH(FROM_UNIXTIME(created_at/1000)));

-- 2. 创建分区示例（需要根据实际数据分布调整）
-- CREATE TABLE t_cs_message_202501 PARTITION OF t_cs_message_partitioned
-- FOR VALUES FROM (202501) TO (202502);

-- ==================== 索引维护建议 ====================

-- 1. 创建索引监控存储过程
DELIMITER $$
CREATE PROCEDURE sp_analyze_index_usage()
BEGIN
    -- 分析索引使用情况
    SELECT 
        SCHEMA_NAME,
        TABLE_NAME,
        INDEX_NAME,
        COLUMN_NAME,
        CARDINALITY,
        CASE 
            WHEN CARDINALITY = 0 THEN 'UNUSED'
            WHEN CARDINALITY < 100 THEN 'LOW_SELECTIVITY' 
            ELSE 'GOOD'
        END as INDEX_STATUS
    FROM INFORMATION_SCHEMA.STATISTICS 
    WHERE SCHEMA_NAME = DATABASE()
        AND TABLE_NAME IN ('t_cs_message', 'cs_session', 'cs_friend', 'cs_media_message')
    ORDER BY TABLE_NAME, INDEX_NAME;
END$$
DELIMITER ;

-- 2. 索引重建建议（定期执行）
-- 每月执行一次索引优化
CREATE EVENT IF NOT EXISTS ev_monthly_index_maintenance
ON SCHEDULE EVERY 1 MONTH
STARTS '2025-01-01 02:00:00'
DO
  BEGIN
    -- 重建关键索引统计信息
    ANALYZE TABLE t_cs_message;
    ANALYZE TABLE cs_session;
    ANALYZE TABLE cs_friend;
    ANALYZE TABLE cs_media_message;
    ANALYZE TABLE cs_conversation_settings;
  END;

-- ==================== 查询性能优化建议 ====================

-- 1. 慢查询日志配置建议
-- SET GLOBAL slow_query_log = 'ON';
-- SET GLOBAL long_query_time = 1; -- 记录超过1秒的查询
-- SET GLOBAL log_queries_not_using_indexes = 'ON';

-- 2. 连接池优化建议
-- 在application.yaml中配置：
-- spring.datasource.hikari.maximum-pool-size=20
-- spring.datasource.hikari.minimum-idle=5
-- spring.datasource.hikari.idle-timeout=300000

-- 3. 查询缓存优化
-- 启用查询结果缓存，特别是对于频繁的只读查询

-- ==================== 数据清理策略 ====================

-- 1. 历史数据归档（超过1年的消息）
CREATE EVENT IF NOT EXISTS ev_archive_old_messages
ON SCHEDULE EVERY 1 MONTH  
STARTS '2025-01-15 03:00:00'
DO
  BEGIN
    -- 归档1年前的消息到历史表
    INSERT INTO t_cs_message_archive 
    SELECT * FROM t_cs_message 
    WHERE created_at < UNIX_TIMESTAMP(DATE_SUB(NOW(), INTERVAL 1 YEAR)) * 1000
      AND is_deleted = 0;
    
    -- 删除已归档的消息
    DELETE FROM t_cs_message 
    WHERE created_at < UNIX_TIMESTAMP(DATE_SUB(NOW(), INTERVAL 1 YEAR)) * 1000
    LIMIT 10000; -- 分批删除，避免长事务
  END;

-- 2. 临时文件清理
CREATE EVENT IF NOT EXISTS ev_cleanup_temp_files  
ON SCHEDULE EVERY 1 DAY
STARTS '2025-01-01 01:00:00'
DO
  UPDATE cs_file_upload 
  SET is_deleted = 1 
  WHERE expires_at < UNIX_TIMESTAMP() * 1000 
    AND is_deleted = 0;