-- ================================================
-- 数据验证和测试SQL脚本
-- ================================================
-- 用途：验证字典数据完整性和一致性，提供测试查询
-- 包含：数据完整性检查、性能测试、功能验证等
-- 执行顺序：在07_multi_tenant_examples.sql之后执行
-- ================================================

-- 设置SQL执行参数
SET @current_timestamp = UNIX_TIMESTAMP(NOW()) * 1000;

-- ================================================
-- 1. 数据完整性验证
-- ================================================

-- 验证所有字典类型是否有数据
SELECT '=== 字典类型数据覆盖情况 ===' as validation_section;

SELECT 
    dictionary_type,
    COUNT(*) as total_entries,
    COUNT(DISTINCT tenant) as tenant_count,
    COUNT(DISTINCT channel) as channel_count,
    COUNT(DISTINCT domain) as domain_count,
    COUNT(CASE WHEN status = 'ACTIVE' THEN 1 END) as active_entries,
    ROUND(COUNT(CASE WHEN status = 'ACTIVE' THEN 1 END) * 100.0 / COUNT(*), 2) as active_percentage
FROM t_dictionary_entry
GROUP BY dictionary_type
ORDER BY dictionary_type;

-- 验证必要的字典类型是否存在
SELECT '=== 必要字典类型检查 ===' as validation_section;

WITH required_types AS (
    SELECT 'normalization_rules' as required_type
    UNION SELECT 'phonetic_corrections'
    UNION SELECT 'stop_words'
    UNION SELECT 'prefix_words'
    UNION SELECT 'synonym_sets'
    UNION SELECT 'semantic_alignment_rules'
    UNION SELECT 'semantic_keywords'
    UNION SELECT 'intent_catalog'
    UNION SELECT 'intent_patterns'
    UNION SELECT 'rewrite_rules'
    UNION SELECT 'expansion_strategies'
)
SELECT 
    rt.required_type,
    CASE 
        WHEN de.dictionary_type IS NOT NULL THEN 'EXISTS'
        ELSE 'MISSING'
    END as status,
    COALESCE(COUNT(de.entry_id), 0) as entry_count
FROM required_types rt
LEFT JOIN t_dictionary_entry de ON rt.required_type = de.dictionary_type
GROUP BY rt.required_type, de.dictionary_type
ORDER BY rt.required_type;

-- 验证多租户数据完整性
SELECT '=== 多租户数据完整性检查 ===' as validation_section;

SELECT 
    tenant,
    COUNT(DISTINCT dictionary_type) as type_coverage,
    COUNT(DISTINCT channel) as channel_coverage,
    COUNT(DISTINCT domain) as domain_coverage,
    COUNT(*) as total_entries
FROM t_dictionary_entry
WHERE tenant != 'default'
GROUP BY tenant
ORDER BY tenant;

-- 验证JSON格式数据
SELECT '=== JSON数据格式验证 ===' as validation_section;

SELECT 
    dictionary_type,
    entry_key,
    CASE 
        WHEN JSON_VALID(entry_value) THEN 'VALID'
        ELSE 'INVALID'
    END as json_status,
    CASE 
        WHEN JSON_VALID(entry_value) THEN 'OK'
        ELSE entry_value
    END as invalid_content
FROM t_dictionary_entry
WHERE entry_value LIKE '{%' OR entry_value LIKE '[%'
HAVING json_status = 'INVALID'
LIMIT 20;

-- ================================================
-- 2. 数据一致性验证
-- ================================================

-- 检查重复的entry_key
SELECT '=== 重复键值检查 ===' as validation_section;

SELECT 
    tenant, channel, domain, dictionary_type, entry_key,
    COUNT(*) as duplicate_count
FROM t_dictionary_entry
GROUP BY tenant, channel, domain, dictionary_type, entry_key
HAVING COUNT(*) > 1
ORDER BY duplicate_count DESC
LIMIT 20;

-- 检查空值数据
SELECT '=== 空值数据检查 ===' as validation_section;

SELECT 
    dictionary_type,
    SUM(CASE WHEN entry_key IS NULL OR entry_key = '' THEN 1 ELSE 0 END) as empty_keys,
    SUM(CASE WHEN entry_value IS NULL OR entry_value = '' THEN 1 ELSE 0 END) as empty_values,
    SUM(CASE WHEN description IS NULL OR description = '' THEN 1 ELSE 0 END) as empty_descriptions
FROM t_dictionary_entry
GROUP BY dictionary_type
HAVING empty_keys > 0 OR empty_values > 0 OR empty_descriptions > 0
ORDER BY dictionary_type;

-- 检查优先级分布
SELECT '=== 优先级分布检查 ===' as validation_section;

SELECT 
    dictionary_type,
    priority,
    COUNT(*) as entry_count,
    ROUND(COUNT(*) * 100.0 / SUM(COUNT(*)) OVER (PARTITION BY dictionary_type), 2) as percentage
FROM t_dictionary_entry
GROUP BY dictionary_type, priority
ORDER BY dictionary_type, priority DESC;

-- ================================================
-- 3. 性能测试查询
-- ================================================

-- 测试基础查询性能
SELECT '=== 性能测试 - 基础查询 ===' as validation_section;

-- 记录开始时间
SET @start_time = NOW(3);

-- 执行典型查询场景
SELECT COUNT(*) as result_count
FROM t_dictionary_entry
WHERE tenant = 'default' 
    AND channel = 'default' 
    AND domain = 'default' 
    AND dictionary_type = 'normalization_rules'
    AND status = 'ACTIVE';

-- 计算查询耗时
SELECT CONCAT('基础查询耗时: ', TIMESTAMPDIFF(MICROSECOND, @start_time, NOW(3)) / 1000, ' ms') as performance_result;

-- 测试复杂查询性能
SET @start_time = NOW(3);

SELECT 
    dictionary_type,
    COUNT(*) as entry_count
FROM t_dictionary_entry
WHERE (tenant, channel, domain) IN (
    ('default', 'default', 'default'),
    ('automotive', 'web', 'manufacturing'),
    ('ecommerce_platform', 'app', 'marketing')
)
    AND status = 'ACTIVE'
    AND created_at > UNIX_TIMESTAMP(DATE_SUB(NOW(), INTERVAL 30 DAY)) * 1000
GROUP BY dictionary_type
ORDER BY entry_count DESC;

SELECT CONCAT('复杂查询耗时: ', TIMESTAMPDIFF(MICROSECOND, @start_time, NOW(3)) / 1000, ' ms') as performance_result;

-- 测试模糊匹配性能
SET @start_time = NOW(3);

SELECT entry_key, entry_value
FROM t_dictionary_entry
WHERE dictionary_type = 'normalization_rules'
    AND entry_key LIKE '%错%'
    AND status = 'ACTIVE'
LIMIT 10;

SELECT CONCAT('模糊匹配耗时: ', TIMESTAMPDIFF(MICROSECOND, @start_time, NOW(3)) / 1000, ' ms') as performance_result;

-- ================================================
-- 4. 功能验证测试
-- ================================================

-- 测试多租户隔离
SELECT '=== 功能验证 - 多租户隔离测试 ===' as validation_section;

-- 验证不同租户的同类型数据是否正确隔离
SELECT 
    'normalization_rules' as test_type,
    tenant,
    COUNT(DISTINCT entry_key) as unique_keys,
    GROUP_CONCAT(DISTINCT entry_key ORDER BY entry_key LIMIT 5) as sample_keys
FROM t_dictionary_entry
WHERE dictionary_type = 'normalization_rules'
GROUP BY tenant
ORDER BY tenant;

-- 测试渠道特定配置
SELECT '=== 功能验证 - 渠道特定配置测试 ===' as validation_section;

SELECT 
    channel,
    dictionary_type,
    COUNT(*) as entry_count,
    GROUP_CONCAT(DISTINCT domain ORDER BY domain) as domains
FROM t_dictionary_entry
WHERE channel != 'default'
GROUP BY channel, dictionary_type
ORDER BY channel, dictionary_type;

-- 测试领域特定配置
SELECT '=== 功能验证 - 领域特定配置测试 ===' as validation_section;

SELECT 
    domain,
    dictionary_type,
    COUNT(*) as entry_count,
    COUNT(DISTINCT tenant) as tenant_count
FROM t_dictionary_entry
WHERE domain != 'default'
GROUP BY domain, dictionary_type
ORDER BY domain, dictionary_type;

-- ================================================
-- 5. 业务场景验证
-- ================================================

-- 验证查询转换管线各阶段的字典数据
SELECT '=== 业务场景验证 - 管线阶段字典覆盖 ===' as validation_section;

WITH pipeline_stages AS (
    SELECT 'NormalizationStage' as stage, 'normalization_rules' as required_type
    UNION SELECT 'NormalizationStage', 'phonetic_corrections'
    UNION SELECT 'SemanticAlignmentStage', 'semantic_alignment_rules'
    UNION SELECT 'SemanticAlignmentStage', 'semantic_keywords'
    UNION SELECT 'IntentExtractionStage', 'intent_catalog'
    UNION SELECT 'IntentExtractionStage', 'intent_patterns'
    UNION SELECT 'RewriteStage', 'rewrite_rules'
    UNION SELECT 'ExpansionStrategyStage', 'expansion_strategies'
)
SELECT 
    ps.stage,
    ps.required_type,
    COUNT(de.entry_id) as available_entries,
    COUNT(CASE WHEN de.status = 'ACTIVE' THEN 1 END) as active_entries,
    CASE 
        WHEN COUNT(de.entry_id) > 0 THEN 'AVAILABLE'
        ELSE 'MISSING'
    END as availability_status
FROM pipeline_stages ps
LEFT JOIN t_dictionary_entry de ON ps.required_type = de.dictionary_type 
    AND de.tenant = 'default' 
    AND de.channel = 'default'
    AND de.domain = 'default'
GROUP BY ps.stage, ps.required_type
ORDER BY ps.stage, ps.required_type;

-- 验证不同行业租户的专业术语覆盖
SELECT '=== 业务场景验证 - 行业术语覆盖 ===' as validation_section;

WITH industry_tenants AS (
    SELECT 'automotive' as tenant, '汽车制造' as industry
    UNION SELECT 'ecommerce_platform', '电商平台'
    UNION SELECT 'financial_services', '金融服务'
    UNION SELECT 'healthcare', '医疗健康'
    UNION SELECT 'education', '教育培训'
)
SELECT 
    it.tenant,
    it.industry,
    COUNT(DISTINCT de.dictionary_type) as type_coverage,
    COUNT(de.entry_id) as total_entries,
    GROUP_CONCAT(DISTINCT de.dictionary_type ORDER BY de.dictionary_type) as covered_types
FROM industry_tenants it
LEFT JOIN t_dictionary_entry de ON it.tenant = de.tenant
GROUP BY it.tenant, it.industry
ORDER BY total_entries DESC;

-- ================================================
-- 6. 数据质量报告
-- ================================================

SELECT '=== 数据质量报告 ===' as validation_section;

-- 总体统计
SELECT 
    '总体统计' as metric_type,
    COUNT(*) as total_entries,
    COUNT(DISTINCT dictionary_type) as total_types,
    COUNT(DISTINCT tenant) as total_tenants,
    COUNT(DISTINCT channel) as total_channels,
    COUNT(DISTINCT domain) as total_domains,
    COUNT(CASE WHEN status = 'ACTIVE' THEN 1 END) as active_entries,
    ROUND(COUNT(CASE WHEN status = 'ACTIVE' THEN 1 END) * 100.0 / COUNT(*), 2) as active_percentage
FROM t_dictionary_entry;

-- 数据分布统计
SELECT 
    '数据分布' as metric_type,
    dictionary_type,
    COUNT(*) as entry_count,
    ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM t_dictionary_entry), 2) as distribution_percentage
FROM t_dictionary_entry
GROUP BY dictionary_type
ORDER BY entry_count DESC;

-- 数据新鲜度统计
SELECT 
    '数据新鲜度' as metric_type,
    CASE 
        WHEN created_at > UNIX_TIMESTAMP(DATE_SUB(NOW(), INTERVAL 1 DAY)) * 1000 THEN '最近1天'
        WHEN created_at > UNIX_TIMESTAMP(DATE_SUB(NOW(), INTERVAL 7 DAY)) * 1000 THEN '最近7天'
        WHEN created_at > UNIX_TIMESTAMP(DATE_SUB(NOW(), INTERVAL 30 DAY)) * 1000 THEN '最近30天'
        ELSE '30天前'
    END as time_range,
    COUNT(*) as entry_count
FROM t_dictionary_entry
GROUP BY 
    CASE 
        WHEN created_at > UNIX_TIMESTAMP(DATE_SUB(NOW(), INTERVAL 1 DAY)) * 1000 THEN '最近1天'
        WHEN created_at > UNIX_TIMESTAMP(DATE_SUB(NOW(), INTERVAL 7 DAY)) * 1000 THEN '最近7天'
        WHEN created_at > UNIX_TIMESTAMP(DATE_SUB(NOW(), INTERVAL 30 DAY)) * 1000 THEN '最近30天'
        ELSE '30天前'
    END
ORDER BY entry_count DESC;

-- ================================================
-- 7. 测试数据样例查询
-- ================================================

SELECT '=== 测试数据样例 ===' as validation_section;

-- 每种字典类型的示例数据
SELECT 
    dictionary_type,
    tenant,
    channel,
    domain,
    entry_key,
    LEFT(entry_value, 100) as sample_value,
    description
FROM (
    SELECT 
        *,
        ROW_NUMBER() OVER (PARTITION BY dictionary_type ORDER BY priority DESC, created_at DESC) as rn
    FROM t_dictionary_entry
    WHERE status = 'ACTIVE'
) ranked
WHERE rn <= 2
ORDER BY dictionary_type, tenant, channel, domain;

-- ================================================
-- 8. 建议的优化查询
-- ================================================

SELECT '=== 建议的索引优化 ===' as validation_section;

-- 分析查询模式，建议索引
SELECT 
    '建议创建复合索引' as optimization_type,
    'CREATE INDEX idx_dict_lookup ON t_dictionary_entry(tenant, channel, domain, dictionary_type, status);' as suggestion
UNION
SELECT 
    '建议创建查询索引',
    'CREATE INDEX idx_dict_key_lookup ON t_dictionary_entry(dictionary_type, entry_key, status);'
UNION
SELECT 
    '建议创建时间索引',
    'CREATE INDEX idx_dict_time ON t_dictionary_entry(created_at, updated_at);'
UNION
SELECT 
    '建议创建优先级索引',
    'CREATE INDEX idx_dict_priority ON t_dictionary_entry(dictionary_type, priority, status);';

-- ================================================
-- 执行完成标记
-- ================================================

-- 更新统计信息
CALL sp_refresh_dictionary_stats();

-- 输出最终报告
SELECT CONCAT('数据验证和测试完成 - 时间: ', FROM_UNIXTIME(@current_timestamp/1000)) AS final_result;

SELECT 
    'VALIDATION_COMPLETE' as status,
    COUNT(*) as total_entries,
    COUNT(DISTINCT dictionary_type) as total_types,
    COUNT(DISTINCT CONCAT(tenant, '|', channel, '|', domain)) as total_contexts,
    COUNT(CASE WHEN status = 'ACTIVE' THEN 1 END) as active_entries,
    ROUND(AVG(priority), 2) as avg_priority
FROM t_dictionary_entry;

-- 生成验证报告摘要
SELECT '=== 验证报告摘要 ===' as summary_section;

SELECT 
    '字典系统验证完成' as message,
    CONCAT('共计 ', COUNT(*), ' 条数据记录') as data_count,
    CONCAT('覆盖 ', COUNT(DISTINCT dictionary_type), ' 种字典类型') as type_coverage,
    CONCAT('支持 ', COUNT(DISTINCT tenant), ' 个租户') as tenant_support,
    CONCAT('激活率 ', ROUND(COUNT(CASE WHEN status = 'ACTIVE' THEN 1 END) * 100.0 / COUNT(*), 1), '%') as activation_rate
FROM t_dictionary_entry;