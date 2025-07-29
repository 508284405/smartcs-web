-- SmartCS Web Database Migrations
-- This script applies only the changes that are actually needed based on the current schema

-- Check if model_key column exists in t_model table and drop it if it does
SET @column_exists = (SELECT COUNT(*) 
                      FROM INFORMATION_SCHEMA.COLUMNS 
                      WHERE TABLE_SCHEMA = DATABASE() 
                      AND TABLE_NAME = 't_model' 
                      AND COLUMN_NAME = 'model_key');

SET @sql = IF(@column_exists > 0,
    'ALTER TABLE `t_model` DROP COLUMN `model_key`',
    'SELECT "Column model_key does not exist in t_model table" AS message');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Drop indexes if they exist
DROP INDEX IF EXISTS `idx_model_key` ON `t_model`;
DROP INDEX IF EXISTS `idx_provider_model` ON `t_model`;

-- Update existing t_kb_content records with default values where needed
UPDATE `t_kb_content` SET 
    `source` = 'upload',
    `processing_status` = 'success',
    `chunk_count` = COALESCE(`chunk_count`, 0),
    `average_chunk_length` = COALESCE(`average_chunk_length`, 0),
    `processing_time` = COALESCE(`processing_time`, 0),
    `embedding_time` = COALESCE(`embedding_time`, 0),
    `embedding_cost` = COALESCE(`embedding_cost`, 0)
WHERE `source` IS NULL OR `processing_status` IS NULL;

-- Ensure all new columns have proper defaults for future inserts
ALTER TABLE `t_kb_content` 
MODIFY COLUMN `metadata` JSON COMMENT '元数据信息',
MODIFY COLUMN `original_file_name` VARCHAR(256) COMMENT '原始文件名称',
MODIFY COLUMN `file_size` BIGINT COMMENT '文件大小（字节）',
MODIFY COLUMN `source` VARCHAR(64) COMMENT '来源 upload/api/import',
MODIFY COLUMN `processing_time` BIGINT COMMENT '处理时间（毫秒）',
MODIFY COLUMN `embedding_time` BIGINT COMMENT '向量化时间（毫秒）',
MODIFY COLUMN `embedding_cost` BIGINT COMMENT '嵌入成本（tokens）',
MODIFY COLUMN `average_chunk_length` INT COMMENT '平均段落长度',
MODIFY COLUMN `chunk_count` INT COMMENT '段落数量',
MODIFY COLUMN `processing_status` VARCHAR(32) COMMENT '处理状态 processing/success/failed',
MODIFY COLUMN `processing_error_message` TEXT COMMENT '处理错误信息';