-- 字典模块 - 字典条目表
-- 用于存储查询转换器的字典数据，支持多租户、多渠道、多领域配置
CREATE TABLE IF NOT EXISTS `t_dictionary_entry` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
  `dictionary_type` VARCHAR(64) NOT NULL COMMENT '字典类型：normalization_rules, phonetic_corrections, prefix_words, synonym_sets, stop_words等',
  `tenant` VARCHAR(50) NOT NULL COMMENT '租户标识',
  `channel` VARCHAR(50) NOT NULL COMMENT '渠道标识',
  `domain` VARCHAR(50) NOT NULL COMMENT '领域标识',
  `entry_key` VARCHAR(200) NOT NULL COMMENT '条目键，在同一配置同一类型下唯一',
  `entry_value` TEXT NOT NULL COMMENT '条目值，JSON格式存储',
  `description` VARCHAR(500) COMMENT '描述说明',
  `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：DRAFT-草稿，ACTIVE-生效，INACTIVE-失效',
  `priority` INT NOT NULL DEFAULT 100 COMMENT '优先级，数值越大优先级越高',
  `version` BIGINT NOT NULL DEFAULT 1 COMMENT '版本号，用于乐观锁控制',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0-未删除，1-已删除',
  `created_at` BIGINT NOT NULL COMMENT '创建时间戳（毫秒）',
  `updated_at` BIGINT NOT NULL COMMENT '更新时间戳（毫秒）',
  `created_by` VARCHAR(64) NOT NULL COMMENT '创建人',
  `updated_by` VARCHAR(64) NOT NULL COMMENT '更新人',
  
  -- 唯一约束：同一配置下的同一字典类型中条目键必须唯一
  UNIQUE KEY `uk_dict_business` (`dictionary_type`, `tenant`, `channel`, `domain`, `entry_key`),
  
  -- 复合索引：按配置和类型查询（最常用）
  INDEX `idx_config_type` (`tenant`, `channel`, `domain`, `dictionary_type`, `status`),
  
  -- 单字段索引
  INDEX `idx_dictionary_type` (`dictionary_type`),
  INDEX `idx_tenant` (`tenant`),
  INDEX `idx_status` (`status`),
  INDEX `idx_created_at` (`created_at`),
  INDEX `idx_updated_at` (`updated_at`),
  INDEX `idx_created_by` (`created_by`),
  
  -- 优化查询性能的复合索引
  INDEX `idx_type_status_priority` (`dictionary_type`, `status`, `priority` DESC),
  INDEX `idx_config_updated` (`tenant`, `channel`, `domain`, `updated_at` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci 
COMMENT='字典条目表 - 存储查询转换器的多种字典数据';

-- 创建分区表（可选，适用于大数据量场景）
-- 按字典类型进行HASH分区，提升查询性能
-- ALTER TABLE `t_dictionary_entry` PARTITION BY HASH(`dictionary_type`) PARTITIONS 8;

-- 创建视图：活跃字典条目视图（便于业务查询）
CREATE OR REPLACE VIEW `v_active_dictionary_entries` AS
SELECT 
    `id`,
    `dictionary_type`,
    `tenant`,
    `channel`,
    `domain`,
    `entry_key`,
    `entry_value`,
    `description`,
    `priority`,
    `version`,
    `created_at`,
    `updated_at`,
    `created_by`,
    `updated_by`
FROM `t_dictionary_entry`
WHERE `status` = 'ACTIVE' 
    AND `is_deleted` = 0
ORDER BY `priority` DESC, `created_at` ASC;

-- 插入默认配置的初始数据
INSERT IGNORE INTO `t_dictionary_entry` (
    `dictionary_type`, `tenant`, `channel`, `domain`, 
    `entry_key`, `entry_value`, `description`, 
    `status`, `priority`, `created_at`, `updated_at`, 
    `created_by`, `updated_by`
) VALUES 

-- 默认标准化规则
('normalization_rules', 'default', 'default', 'default', 
 '因该', '"应该"', '常见错别字纠正', 
 'ACTIVE', 100, UNIX_TIMESTAMP(NOW()) * 1000, UNIX_TIMESTAMP(NOW()) * 1000, 
 'system', 'system'),

('normalization_rules', 'default', 'default', 'default', 
 '做为', '"作为"', '常见错别字纠正', 
 'ACTIVE', 100, UNIX_TIMESTAMP(NOW()) * 1000, UNIX_TIMESTAMP(NOW()) * 1000, 
 'system', 'system'),

-- 默认拼音纠错
('phonetic_corrections', 'default', 'default', 'default', 
 '朱丽业', '"朱丽叶"', '拼音错拼纠正', 
 'ACTIVE', 100, UNIX_TIMESTAMP(NOW()) * 1000, UNIX_TIMESTAMP(NOW()) * 1000, 
 'system', 'system'),

-- 默认停用词
('stop_words', 'default', 'default', 'default', 
 '停用词集合', '["的","了","在","是","我","有","和","就","不","人","都","一","上","也","很"]', '常见中文停用词', 
 'ACTIVE', 100, UNIX_TIMESTAMP(NOW()) * 1000, UNIX_TIMESTAMP(NOW()) * 1000, 
 'system', 'system'),

-- 默认前缀词汇
('prefix_words', 'default', 'default', 'default', 
 '常用前缀', '["如何","怎么","什么","为什么","哪里","谁","when","where","what","how","why"]', '常用查询前缀词汇', 
 'ACTIVE', 100, UNIX_TIMESTAMP(NOW()) * 1000, UNIX_TIMESTAMP(NOW()) * 1000, 
 'system', 'system'),

-- 默认同义词组
('synonym_sets', 'default', 'default', 'default', 
 '问题', '["问题","疑问","困惑","难题","issue","problem"]', '问题相关同义词', 
 'ACTIVE', 100, UNIX_TIMESTAMP(NOW()) * 1000, UNIX_TIMESTAMP(NOW()) * 1000, 
 'system', 'system'),

('synonym_sets', 'default', 'default', 'default', 
 '方法', '["方法","方式","途径","办法","手段","方案","method","way","approach"]', '方法相关同义词', 
 'ACTIVE', 100, UNIX_TIMESTAMP(NOW()) * 1000, UNIX_TIMESTAMP(NOW()) * 1000, 
 'system', 'system');