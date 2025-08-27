-- ================================================
-- 字典模块初始化脚本
-- ================================================
-- 用途：初始化字典模块的所有数据库对象和基础数据
-- 执行顺序：在其他字典模块SQL文件之前执行
-- ================================================

-- 1. 创建字典模块相关的序列或函数（如果需要）

-- 2. 创建字典模块特有的数据类型或枚举（如果数据库支持）

-- 3. 设置字典模块相关的变量或配置
SET @dictionary_module_version = '1.0.0';
SET @dictionary_init_timestamp = UNIX_TIMESTAMP(NOW()) * 1000;

-- 4. 创建字典模块的元数据表（可选，用于版本管理和模块信息）
CREATE TABLE IF NOT EXISTS `t_dictionary_module_info` (
    `module_name` VARCHAR(64) PRIMARY KEY COMMENT '模块名称',
    `version` VARCHAR(32) NOT NULL COMMENT '模块版本',
    `description` TEXT COMMENT '模块描述',
    `initialized_at` BIGINT NOT NULL COMMENT '初始化时间戳',
    `updated_at` BIGINT NOT NULL COMMENT '更新时间戳',
    `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '模块状态'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='字典模块信息表';

-- 5. 插入字典模块信息
INSERT INTO `t_dictionary_module_info` (
    `module_name`, `version`, `description`, 
    `initialized_at`, `updated_at`, `status`
) VALUES (
    'dictionary', @dictionary_module_version, '字典模块 - 查询转换器字典数据管理',
    @dictionary_init_timestamp, @dictionary_init_timestamp, 'ACTIVE'
) ON DUPLICATE KEY UPDATE
    `version` = VALUES(`version`),
    `description` = VALUES(`description`),
    `updated_at` = VALUES(`updated_at`);

-- 6. 创建字典数据统计表（可选，用于缓存统计信息）
CREATE TABLE IF NOT EXISTS `t_dictionary_stats` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `dictionary_type` VARCHAR(64) NOT NULL COMMENT '字典类型',
    `tenant` VARCHAR(50) NOT NULL COMMENT '租户标识',  
    `channel` VARCHAR(50) NOT NULL COMMENT '渠道标识',
    `domain` VARCHAR(50) NOT NULL COMMENT '领域标识',
    `entry_count` INT NOT NULL DEFAULT 0 COMMENT '条目数量',
    `active_count` INT NOT NULL DEFAULT 0 COMMENT '活跃条目数量',
    `last_updated` BIGINT NOT NULL COMMENT '最后更新时间戳',
    `data_version` BIGINT NOT NULL DEFAULT 1 COMMENT '数据版本号',
    
    UNIQUE KEY `uk_stats_config` (`dictionary_type`, `tenant`, `channel`, `domain`),
    INDEX `idx_dict_type` (`dictionary_type`),
    INDEX `idx_last_updated` (`last_updated`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='字典数据统计表';

-- 7. 创建用于触发缓存刷新的事件表（可选）
CREATE TABLE IF NOT EXISTS `t_dictionary_cache_events` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `event_type` VARCHAR(32) NOT NULL COMMENT '事件类型：CREATE, UPDATE, DELETE, REFRESH',
    `dictionary_type` VARCHAR(64) COMMENT '字典类型',
    `tenant` VARCHAR(50) COMMENT '租户标识',
    `channel` VARCHAR(50) COMMENT '渠道标识', 
    `domain` VARCHAR(50) COMMENT '领域标识',
    `entry_id` BIGINT COMMENT '条目ID',
    `event_data` JSON COMMENT '事件数据',
    `processed` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已处理',
    `created_at` BIGINT NOT NULL COMMENT '创建时间戳',
    
    INDEX `idx_event_type` (`event_type`),
    INDEX `idx_processed` (`processed`, `created_at`),
    INDEX `idx_config` (`tenant`, `channel`, `domain`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='字典缓存事件表 - 用于触发缓存刷新';

-- 8. 创建存储过程：刷新字典统计信息
DELIMITER //
CREATE PROCEDURE IF NOT EXISTS `sp_refresh_dictionary_stats`()
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE v_dict_type VARCHAR(64);
    DECLARE v_tenant VARCHAR(50);
    DECLARE v_channel VARCHAR(50);
    DECLARE v_domain VARCHAR(50);
    DECLARE v_total_count INT;
    DECLARE v_active_count INT;
    DECLARE v_last_updated BIGINT;
    
    -- 游标定义
    DECLARE config_cursor CURSOR FOR
        SELECT DISTINCT dictionary_type, tenant, channel, domain
        FROM t_dictionary_entry
        WHERE is_deleted = 0;
    
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    
    -- 开始事务
    START TRANSACTION;
    
    OPEN config_cursor;
    
    config_loop: LOOP
        FETCH config_cursor INTO v_dict_type, v_tenant, v_channel, v_domain;
        IF done THEN
            LEAVE config_loop;
        END IF;
        
        -- 统计总数和活跃数
        SELECT 
            COUNT(*) as total_count,
            SUM(CASE WHEN status = 'ACTIVE' THEN 1 ELSE 0 END) as active_count,
            MAX(updated_at) as last_updated
        INTO v_total_count, v_active_count, v_last_updated
        FROM t_dictionary_entry
        WHERE dictionary_type = v_dict_type
            AND tenant = v_tenant
            AND channel = v_channel
            AND domain = v_domain
            AND is_deleted = 0;
        
        -- 更新统计表
        INSERT INTO t_dictionary_stats (
            dictionary_type, tenant, channel, domain,
            entry_count, active_count, last_updated, data_version
        ) VALUES (
            v_dict_type, v_tenant, v_channel, v_domain,
            v_total_count, v_active_count, v_last_updated, 
            UNIX_TIMESTAMP(NOW()) * 1000
        ) ON DUPLICATE KEY UPDATE
            entry_count = VALUES(entry_count),
            active_count = VALUES(active_count),
            last_updated = VALUES(last_updated),
            data_version = UNIX_TIMESTAMP(NOW()) * 1000;
        
    END LOOP;
    
    CLOSE config_cursor;
    
    -- 提交事务
    COMMIT;
    
END //
DELIMITER ;

-- 9. 执行初始统计信息刷新
CALL sp_refresh_dictionary_stats();

-- 10. 输出初始化完成信息
SELECT CONCAT('字典模块初始化完成 - 版本: ', @dictionary_module_version, ', 时间: ', FROM_UNIXTIME(@dictionary_init_timestamp/1000)) AS init_result;