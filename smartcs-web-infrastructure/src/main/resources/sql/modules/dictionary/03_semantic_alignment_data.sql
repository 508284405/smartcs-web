-- ================================================
-- M2语义对齐字典数据SQL脚本 - SemanticAlignmentStage专用
-- ================================================
-- 用途：为M2里程碑的语义对齐阶段提供字典数据
-- 包含：语义对齐规则、语义关键词、语义分类、领域术语、单位映射等
-- 执行顺序：在02_basic_dictionary_data.sql之后执行
-- ================================================

-- 设置SQL执行参数
SET @current_timestamp = UNIX_TIMESTAMP(NOW()) * 1000;
SET @creator = 'system_m2_init';

-- ================================================
-- 1. 语义对齐规则字典 (semantic_alignment_rules)
-- 用于SemanticAlignmentStage：同义词归一化处理
-- ================================================
INSERT IGNORE INTO `t_dictionary_entry` (
    `dictionary_type`, `tenant`, `channel`, `domain`, 
    `entry_key`, `entry_value`, `description`, 
    `status`, `priority`, `created_at`, `updated_at`, 
    `created_by`, `updated_by`
) VALUES 

-- 技术领域语义对齐
('semantic_alignment_rules', 'default', 'default', 'tech', 
 'AI', '"人工智能"', 'AI→人工智能 技术术语对齐', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('semantic_alignment_rules', 'default', 'default', 'tech', 
 'ML', '"机器学习"', 'ML→机器学习 技术术语对齐', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('semantic_alignment_rules', 'default', 'default', 'tech', 
 'DB', '"数据库"', 'DB→数据库 技术术语对齐', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('semantic_alignment_rules', 'default', 'default', 'tech', 
 'API', '"应用程序接口"', 'API→应用程序接口 技术术语对齐', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

-- 电商领域语义对齐
('semantic_alignment_rules', 'default', 'default', 'ecommerce', 
 'SKU', '"商品"', 'SKU→商品 电商术语对齐', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('semantic_alignment_rules', 'default', 'default', 'ecommerce', 
 'SPU', '"商品单元"', 'SPU→商品单元 电商术语对齐', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('semantic_alignment_rules', 'default', 'default', 'ecommerce', 
 'GMV', '"成交总额"', 'GMV→成交总额 电商术语对齐', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

-- 客服领域语义对齐
('semantic_alignment_rules', 'default', 'default', 'customer_service', 
 'FAQ', '"常见问题"', 'FAQ→常见问题 客服术语对齐', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('semantic_alignment_rules', 'default', 'default', 'customer_service', 
 'CRM', '"客户关系管理"', 'CRM→客户关系管理 客服术语对齐', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator);

-- ================================================
-- 2. 语义关键词字典 (semantic_keywords)
-- 用于SemanticAlignmentStage：关键词标准化处理
-- ================================================
INSERT IGNORE INTO `t_dictionary_entry` (
    `dictionary_type`, `tenant`, `channel`, `domain`, 
    `entry_key`, `entry_value`, `description`, 
    `status`, `priority`, `created_at`, `updated_at`, 
    `created_by`, `updated_by`
) VALUES 

-- 技术关键词标准化
('semantic_keywords', 'default', 'default', 'tech', 
 '人工智能相关', '{"AI":"人工智能","artificial intelligence":"人工智能","machine learning":"机器学习","deep learning":"深度学习","neural network":"神经网络"}', '人工智能技术关键词标准化', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('semantic_keywords', 'default', 'default', 'tech', 
 '数据库相关', '{"database":"数据库","DB":"数据库","MySQL":"MySQL数据库","PostgreSQL":"PostgreSQL数据库","Redis":"Redis缓存","MongoDB":"MongoDB文档数据库"}', '数据库技术关键词标准化', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('semantic_keywords', 'default', 'default', 'tech', 
 '编程语言', '{"Java":"Java编程语言","Python":"Python编程语言","JavaScript":"JavaScript编程语言","TypeScript":"TypeScript编程语言","Go":"Go编程语言"}', '编程语言关键词标准化', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

-- 电商关键词标准化
('semantic_keywords', 'default', 'default', 'ecommerce', 
 '商品相关', '{"product":"商品","item":"商品","goods":"商品","commodity":"商品","merchandise":"商品"}', '商品相关关键词标准化', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('semantic_keywords', 'default', 'default', 'ecommerce', 
 '订单相关', '{"order":"订单","purchase":"购买","transaction":"交易","payment":"支付","checkout":"结算"}', '订单相关关键词标准化', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

-- 客服关键词标准化
('semantic_keywords', 'default', 'default', 'customer_service', 
 '服务相关', '{"support":"支持","help":"帮助","assistance":"协助","service":"服务","consultation":"咨询"}', '服务相关关键词标准化', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator);

-- ================================================
-- 3. 语义分类字典 (semantic_categories)
-- 用于SemanticAlignmentStage：分类标准化处理
-- ================================================
INSERT IGNORE INTO `t_dictionary_entry` (
    `dictionary_type`, `tenant`, `channel`, `domain`, 
    `entry_key`, `entry_value`, `description`, 
    `status`, `priority`, `created_at`, `updated_at`, 
    `created_by`, `updated_by`
) VALUES 

-- 查询意图分类
('semantic_categories', 'default', 'default', 'default', 
 '信息查询', '"INFORMATION_QUERY"', '信息查询类意图标准化', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('semantic_categories', 'default', 'default', 'default', 
 '操作执行', '"ACTION_EXECUTION"', '操作执行类意图标准化', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('semantic_categories', 'default', 'default', 'default', 
 '问题解决', '"PROBLEM_SOLVING"', '问题解决类意图标准化', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('semantic_categories', 'default', 'default', 'default', 
 '数据分析', '"DATA_ANALYSIS"', '数据分析类意图标准化', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

-- 技术分类标准化
('semantic_categories', 'default', 'default', 'tech', 
 '开发', '"DEVELOPMENT"', '开发相关技术分类', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('semantic_categories', 'default', 'default', 'tech', 
 '运维', '"OPERATIONS"', '运维相关技术分类', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('semantic_categories', 'default', 'default', 'tech', 
 '测试', '"TESTING"', '测试相关技术分类', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator);

-- ================================================
-- 4. 领域术语字典 (domain_terms)
-- 用于SemanticAlignmentStage：领域术语标准化处理
-- ================================================
INSERT IGNORE INTO `t_dictionary_entry` (
    `dictionary_type`, `tenant`, `channel`, `domain`, 
    `entry_key`, `entry_value`, `description`, 
    `status`, `priority`, `created_at`, `updated_at`, 
    `created_by`, `updated_by`
) VALUES 

-- 技术领域术语
('domain_terms', 'default', 'default', 'tech', 
 '微服务', '"微服务架构"', '微服务→微服务架构 领域术语标准化', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('domain_terms', 'default', 'default', 'tech', 
 '容器', '"容器化技术"', '容器→容器化技术 领域术语标准化', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('domain_terms', 'default', 'default', 'tech', 
 'DevOps', '"开发运维一体化"', 'DevOps→开发运维一体化 领域术语标准化', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('domain_terms', 'default', 'default', 'tech', 
 'CI/CD', '"持续集成持续部署"', 'CI/CD→持续集成持续部署 领域术语标准化', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

-- 电商领域术语
('domain_terms', 'default', 'default', 'ecommerce', 
 'B2B', '"企业对企业"', 'B2B→企业对企业 电商术语标准化', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('domain_terms', 'default', 'default', 'ecommerce', 
 'B2C', '"企业对消费者"', 'B2C→企业对消费者 电商术语标准化', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('domain_terms', 'default', 'default', 'ecommerce', 
 'C2C', '"消费者对消费者"', 'C2C→消费者对消费者 电商术语标准化', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('domain_terms', 'default', 'default', 'ecommerce', 
 'O2O', '"线上到线下"', 'O2O→线上到线下 电商术语标准化', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator);

-- ================================================
-- 5. 语义同义词字典 (semantic_synonyms)
-- 用于SemanticAlignmentStage：同义词处理（增强版）
-- ================================================
INSERT IGNORE INTO `t_dictionary_entry` (
    `dictionary_type`, `tenant`, `channel`, `domain`, 
    `entry_key`, `entry_value`, `description`, 
    `status`, `priority`, `created_at`, `updated_at`, 
    `created_by`, `updated_by`
) VALUES 

-- 技术语义同义词
('semantic_synonyms', 'default', 'default', 'tech', 
 '系统', '{"systems":["系统","平台","架构","框架"],"performance":["性能","效率","速度","响应时间"],"security":["安全","防护","保密","权限"]}', '技术领域语义同义词组', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

-- 业务语义同义词  
('semantic_synonyms', 'default', 'default', 'ecommerce', 
 '销售', '{"sales":["销售","售卖","营销","推广"],"customer":["客户","用户","消费者","买家"],"revenue":["收入","营收","盈利","利润"]}', '电商领域语义同义词组', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator);

-- ================================================
-- 6. 单位映射字典 (semantic_unit_mappings)
-- 用于SemanticAlignmentStage：单位标准化处理
-- ================================================
INSERT IGNORE INTO `t_dictionary_entry` (
    `dictionary_type`, `tenant`, `channel`, `domain`, 
    `entry_key`, `entry_value`, `description`, 
    `status`, `priority`, `created_at`, `updated_at`, 
    `created_by`, `updated_by`
) VALUES 

-- 重量单位映射
('semantic_unit_mappings', 'default', 'default', 'default', 
 '重量单位', '{"kg":"千克","g":"克","ton":"吨","lb":"磅","oz":"盎司","斤":"500克"}', '重量单位标准化映射', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

-- 长度单位映射
('semantic_unit_mappings', 'default', 'default', 'default', 
 '长度单位', '{"m":"米","cm":"厘米","mm":"毫米","km":"千米","ft":"英尺","inch":"英寸"}', '长度单位标准化映射', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

-- 时间单位映射
('semantic_unit_mappings', 'default', 'default', 'default', 
 '时间单位', '{"s":"秒","min":"分钟","h":"小时","d":"天","w":"周","m":"月","y":"年"}', '时间单位标准化映射', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

-- 存储单位映射
('semantic_unit_mappings', 'default', 'default', 'tech', 
 '存储单位', '{"B":"字节","KB":"千字节","MB":"兆字节","GB":"吉字节","TB":"太字节","PB":"拍字节"}', '存储单位标准化映射', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator);

-- ================================================
-- 7. 时间模式字典 (semantic_time_patterns)
-- 用于SemanticAlignmentStage：时间表达式识别与标准化
-- ================================================
INSERT IGNORE INTO `t_dictionary_entry` (
    `dictionary_type`, `tenant`, `channel`, `domain`, 
    `entry_key`, `entry_value`, `description`, 
    `status`, `priority`, `created_at`, `updated_at`, 
    `created_by`, `updated_by`
) VALUES 

-- 相对时间模式
('semantic_time_patterns', 'default', 'default', 'default', 
 '相对时间', '{"patterns":[{"pattern":"今天","replacement":"TODAY","type":"relative"},{"pattern":"昨天","replacement":"YESTERDAY","type":"relative"},{"pattern":"明天","replacement":"TOMORROW","type":"relative"},{"pattern":"上周","replacement":"LAST_WEEK","type":"relative"},{"pattern":"下周","replacement":"NEXT_WEEK","type":"relative"}]}', '相对时间表达式模式', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

-- 绝对时间模式
('semantic_time_patterns', 'default', 'default', 'default', 
 '绝对时间', '{"patterns":[{"pattern":"(\\d{4})年(\\d{1,2})月(\\d{1,2})日","replacement":"$1-$2-$3","type":"date"},{"pattern":"(\\d{1,2}):(\\d{2})","replacement":"$1:$2:00","type":"time"}]}', '绝对时间表达式模式', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator);

-- ================================================
-- 8. 实体别名字典 (semantic_entity_aliases)
-- 用于SemanticAlignmentStage：实体别名标准化处理
-- ================================================
INSERT IGNORE INTO `t_dictionary_entry` (
    `dictionary_type`, `tenant`, `channel`, `domain`, 
    `entry_key`, `entry_value`, `description`, 
    `status`, `priority`, `created_at`, `updated_at`, 
    `created_by`, `updated_by`
) VALUES 

-- 公司实体别名
('semantic_entity_aliases', 'default', 'default', 'default', 
 '苹果公司', '"Apple Inc."', '苹果公司实体别名标准化', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('semantic_entity_aliases', 'default', 'default', 'default', 
 '谷歌', '"Google LLC"', '谷歌公司实体别名标准化', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('semantic_entity_aliases', 'default', 'default', 'default', 
 '微软', '"Microsoft Corporation"', '微软公司实体别名标准化', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

-- 产品实体别名
('semantic_entity_aliases', 'default', 'default', 'tech', 
 'iPhone', '"苹果手机"', 'iPhone产品实体别名标准化', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('semantic_entity_aliases', 'default', 'default', 'tech', 
 'Android', '"安卓系统"', 'Android产品实体别名标准化', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

-- 地点实体别名
('semantic_entity_aliases', 'default', 'default', 'default', 
 '北京', '"北京市"', '北京地点实体别名标准化', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('semantic_entity_aliases', 'default', 'default', 'default', 
 '上海', '"上海市"', '上海地点实体别名标准化', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator);

-- ================================================
-- 执行统计信息更新
-- ================================================
-- 调用存储过程更新统计信息
CALL sp_refresh_dictionary_stats();

-- 输出执行结果
SELECT CONCAT('M2语义对齐字典数据初始化完成 - 时间: ', FROM_UNIXTIME(@current_timestamp/1000)) AS result;

-- 显示插入的数据统计
SELECT 
    dictionary_type, 
    domain,
    COUNT(*) as entry_count,
    COUNT(CASE WHEN status = 'ACTIVE' THEN 1 END) as active_count
FROM t_dictionary_entry 
WHERE tenant = 'default' 
    AND channel = 'default' 
    AND dictionary_type IN (
        'semantic_alignment_rules', 
        'semantic_keywords', 
        'semantic_categories', 
        'domain_terms',
        'semantic_synonyms',
        'semantic_unit_mappings',
        'semantic_time_patterns',
        'semantic_entity_aliases'
    )
GROUP BY dictionary_type, domain
ORDER BY dictionary_type, domain;