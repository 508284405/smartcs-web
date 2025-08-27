-- ================================================
-- M2意图识别字典数据SQL脚本 - IntentExtractionStage专用  
-- ================================================
-- 用途：为M2里程碑的意图提取阶段提供字典数据
-- 包含：意图目录、意图模式、意图关键词、实体模式、查询类型模式等
-- 执行顺序：在03_semantic_alignment_data.sql之后执行
-- ================================================

-- 设置SQL执行参数
SET @current_timestamp = UNIX_TIMESTAMP(NOW()) * 1000;
SET @creator = 'system_m2_intent';

-- ================================================
-- 1. 意图目录字典 (intent_catalog)
-- 用于IntentExtractionStage：意图分类目录
-- ================================================
INSERT IGNORE INTO `t_dictionary_entry` (
    `dictionary_type`, `tenant`, `channel`, `domain`, 
    `entry_key`, `entry_value`, `description`, 
    `status`, `priority`, `created_at`, `updated_at`, 
    `created_by`, `updated_by`
) VALUES 

-- 通用意图目录
('intent_catalog', 'default', 'default', 'default', 
 'INFORMATION_QUERY', '{"code":"INFORMATION_QUERY","name":"信息查询","description":"用户查询信息的意图","keywords":["查询","搜索","寻找","获取","显示","列出"],"examples":["查询用户信息","搜索订单","显示商品列表"]}', '信息查询意图目录', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('intent_catalog', 'default', 'default', 'default', 
 'ACTION_EXECUTION', '{"code":"ACTION_EXECUTION","name":"操作执行","description":"用户要求执行某种操作的意图","keywords":["创建","删除","修改","更新","执行","运行"],"examples":["创建用户","删除订单","修改商品信息"]}', '操作执行意图目录', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('intent_catalog', 'default', 'default', 'default', 
 'PROBLEM_SOLVING', '{"code":"PROBLEM_SOLVING","name":"问题解决","description":"用户遇到问题需要解决的意图","keywords":["帮助","解决","修复","排查","诊断","处理"],"examples":["解决登录问题","修复系统错误","排查性能问题"]}', '问题解决意图目录', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('intent_catalog', 'default', 'default', 'default', 
 'DATA_ANALYSIS', '{"code":"DATA_ANALYSIS","name":"数据分析","description":"用户需要进行数据分析的意图","keywords":["分析","统计","计算","对比","汇总","报告"],"examples":["分析销售数据","统计用户活跃度","生成业务报告"]}', '数据分析意图目录', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

-- 技术领域意图目录
('intent_catalog', 'default', 'default', 'tech', 
 'CODE_DEBUG', '{"code":"CODE_DEBUG","name":"代码调试","description":"开发者调试代码相关的意图","keywords":["调试","debug","错误","异常","bug","日志"],"examples":["调试Java程序","查看错误日志","排查异常"]}', '代码调试意图目录', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('intent_catalog', 'default', 'default', 'tech', 
 'SYSTEM_CONFIG', '{"code":"SYSTEM_CONFIG","name":"系统配置","description":"系统配置相关的意图","keywords":["配置","设置","环境","参数","安装","部署"],"examples":["配置数据库","设置环境变量","安装依赖"]}', '系统配置意图目录', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

-- 电商领域意图目录
('intent_catalog', 'default', 'default', 'ecommerce', 
 'ORDER_MANAGEMENT', '{"code":"ORDER_MANAGEMENT","name":"订单管理","description":"订单相关操作的意图","keywords":["订单","购买","支付","退款","发货","收货"],"examples":["查询订单状态","处理退款申请","安排发货"]}', '订单管理意图目录', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('intent_catalog', 'default', 'default', 'ecommerce', 
 'PRODUCT_MANAGEMENT', '{"code":"PRODUCT_MANAGEMENT","name":"商品管理","description":"商品相关操作的意图","keywords":["商品","产品","库存","价格","上架","下架"],"examples":["更新商品信息","调整商品价格","管理库存"]}', '商品管理意图目录', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

-- 客服领域意图目录
('intent_catalog', 'default', 'default', 'customer_service', 
 'CUSTOMER_SUPPORT', '{"code":"CUSTOMER_SUPPORT","name":"客户支持","description":"客户支持服务的意图","keywords":["帮助","支持","咨询","服务","问题","投诉"],"examples":["客户投诉处理","产品使用咨询","技术支持"]}', '客户支持意图目录', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator);

-- ================================================
-- 2. 意图模式字典 (intent_patterns) 
-- 用于IntentExtractionStage：意图识别模式匹配
-- ================================================
INSERT IGNORE INTO `t_dictionary_entry` (
    `dictionary_type`, `tenant`, `channel`, `domain`, 
    `entry_key`, `entry_value`, `description`, 
    `status`, `priority`, `created_at`, `updated_at`, 
    `created_by`, `updated_by`
) VALUES 

-- 信息查询模式
('intent_patterns', 'default', 'default', 'default', 
 '查询模式', '{"patterns":[{"regex":"^(查询|查找|搜索|寻找|获取|显示|列出|看一下).*","intent":"INFORMATION_QUERY","confidence":0.9},{"regex":".*?(什么|哪些|多少|几个).*","intent":"INFORMATION_QUERY","confidence":0.8},{"regex":"^(告诉我|我想知道|我需要了解).*","intent":"INFORMATION_QUERY","confidence":0.85}]}', '信息查询意图模式规则', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

-- 操作执行模式  
('intent_patterns', 'default', 'default', 'default', 
 '操作模式', '{"patterns":[{"regex":"^(创建|新建|添加|增加|建立).*","intent":"ACTION_EXECUTION","confidence":0.9},{"regex":"^(删除|移除|清除|去掉).*","intent":"ACTION_EXECUTION","confidence":0.9},{"regex":"^(修改|更新|编辑|调整|变更).*","intent":"ACTION_EXECUTION","confidence":0.9}]}', '操作执行意图模式规则', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

-- 问题解决模式
('intent_patterns', 'default', 'default', 'default', 
 '问题模式', '{"patterns":[{"regex":"^(帮助|帮我|解决|修复|排查).*","intent":"PROBLEM_SOLVING","confidence":0.9},{"regex":".*?(出错|错误|异常|问题|故障).*","intent":"PROBLEM_SOLVING","confidence":0.8},{"regex":"^(为什么|怎么办|如何处理).*","intent":"PROBLEM_SOLVING","confidence":0.85}]}', '问题解决意图模式规则', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

-- 数据分析模式
('intent_patterns', 'default', 'default', 'default', 
 '分析模式', '{"patterns":[{"regex":"^(分析|统计|计算|汇总).*","intent":"DATA_ANALYSIS","confidence":0.9},{"regex":".*?(对比|比较|趋势|报告).*","intent":"DATA_ANALYSIS","confidence":0.8},{"regex":"^(生成.*报告|导出.*数据).*","intent":"DATA_ANALYSIS","confidence":0.85}]}', '数据分析意图模式规则', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator);

-- ================================================
-- 3. 意图关键词字典 (intent_keywords)
-- 用于IntentExtractionStage：关键词基础意图识别
-- ================================================
INSERT IGNORE INTO `t_dictionary_entry` (
    `dictionary_type`, `tenant`, `channel`, `domain`, 
    `entry_key`, `entry_value`, `description`, 
    `status`, `priority`, `created_at`, `updated_at`, 
    `created_by`, `updated_by`
) VALUES 

-- 通用意图关键词
('intent_keywords', 'default', 'default', 'default', 
 'INFORMATION_QUERY', '{"primary":["查询","搜索","寻找","获取","显示","列出","查看","展示"],"secondary":["什么","哪些","多少","几个","告诉我","我想知道"]}', '信息查询意图关键词', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('intent_keywords', 'default', 'default', 'default', 
 'ACTION_EXECUTION', '{"primary":["创建","删除","修改","更新","执行","运行","新建","添加"],"secondary":["操作","处理","执行","实施","进行"]}', '操作执行意图关键词', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('intent_keywords', 'default', 'default', 'default', 
 'PROBLEM_SOLVING', '{"primary":["帮助","解决","修复","排查","诊断","处理"],"secondary":["问题","错误","异常","故障","bug","失败"]}', '问题解决意图关键词', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('intent_keywords', 'default', 'default', 'default', 
 'DATA_ANALYSIS', '{"primary":["分析","统计","计算","汇总","对比","报告"],"secondary":["数据","指标","趋势","图表","比较","总结"]}', '数据分析意图关键词', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

-- 技术领域意图关键词
('intent_keywords', 'default', 'default', 'tech', 
 'CODE_DEBUG', '{"primary":["调试","debug","排查","诊断","异常","错误"],"secondary":["代码","程序","函数","方法","日志","堆栈"]}', '代码调试意图关键词', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('intent_keywords', 'default', 'default', 'tech', 
 'SYSTEM_CONFIG', '{"primary":["配置","设置","安装","部署","环境","参数"],"secondary":["系统","服务","应用","数据库","网络","权限"]}', '系统配置意图关键词', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator);

-- ================================================
-- 4. 实体模式字典 (intent_entity_patterns)
-- 用于IntentExtractionStage：实体模式匹配
-- ================================================
INSERT IGNORE INTO `t_dictionary_entry` (
    `dictionary_type`, `tenant`, `channel`, `domain`, 
    `entry_key`, `entry_value`, `description`, 
    `status`, `priority`, `created_at`, `updated_at`, 
    `created_by`, `updated_by`
) VALUES 

-- 通用实体模式
('intent_entity_patterns', 'default', 'default', 'default', 
 '时间实体', '{"patterns":[{"regex":"(\\d{4}年\\d{1,2}月\\d{1,2}日)","entity_type":"DATE","extraction_group":1},{"regex":"(今天|昨天|明天|上周|下周|本月|上月|下月)","entity_type":"RELATIVE_DATE","extraction_group":1},{"regex":"(\\d{1,2}:\\d{2})","entity_type":"TIME","extraction_group":1}]}', '时间相关实体模式', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('intent_entity_patterns', 'default', 'default', 'default', 
 '数量实体', '{"patterns":[{"regex":"(\\d+(?:\\.\\d+)?)\\s*(个|件|台|套|批|次|人|天|小时)","entity_type":"QUANTITY","extraction_group":1},{"regex":"第(\\d+)","entity_type":"ORDINAL","extraction_group":1},{"regex":"(前|后)\\s*(\\d+)","entity_type":"POSITION","extraction_group":2}]}', '数量相关实体模式', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

-- 技术领域实体模式
('intent_entity_patterns', 'default', 'default', 'tech', 
 '技术实体', '{"patterns":[{"regex":"(Java|Python|JavaScript|Go|C\\+\\+|C#)","entity_type":"PROGRAMMING_LANGUAGE","extraction_group":1},{"regex":"(MySQL|PostgreSQL|MongoDB|Redis|Oracle)","entity_type":"DATABASE","extraction_group":1},{"regex":"(Spring|Django|Express|Gin|React|Vue)","entity_type":"FRAMEWORK","extraction_group":1}]}', '技术相关实体模式', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

-- 电商领域实体模式
('intent_entity_patterns', 'default', 'default', 'ecommerce', 
 '商品实体', '{"patterns":[{"regex":"订单号：?([A-Z0-9]+)","entity_type":"ORDER_ID","extraction_group":1},{"regex":"商品ID：?(\\d+)","entity_type":"PRODUCT_ID","extraction_group":1},{"regex":"用户ID：?(\\d+)","entity_type":"USER_ID","extraction_group":1}]}', '电商相关实体模式', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator);

-- ================================================
-- 5. 查询类型模式字典 (intent_query_type_patterns)
-- 用于IntentExtractionStage：查询类型判断
-- ================================================
INSERT IGNORE INTO `t_dictionary_entry` (
    `dictionary_type`, `tenant`, `channel`, `domain`, 
    `entry_key`, `entry_value`, `description`, 
    `status`, `priority`, `created_at`, `updated_at`, 
    `created_by`, `updated_by`
) VALUES 

-- 查询类型识别模式
('intent_query_type_patterns', 'default', 'default', 'default', 
 '选择查询', '{"patterns":[{"regex":"^(选择|筛选|过滤).*","query_type":"SELECT","confidence":0.9},{"regex":".*?(条件|符合|满足).*","query_type":"CONDITIONAL_SELECT","confidence":0.8}]}', '选择查询类型模式', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('intent_query_type_patterns', 'default', 'default', 'default', 
 '聚合查询', '{"patterns":[{"regex":"^(统计|计算|汇总|求和).*","query_type":"AGGREGATE","confidence":0.9},{"regex":".*?(平均|最大|最小|总计|数量).*","query_type":"AGGREGATE","confidence":0.8}]}', '聚合查询类型模式', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('intent_query_type_patterns', 'default', 'default', 'default', 
 '排序查询', '{"patterns":[{"regex":"^(排序|按.*排列).*","query_type":"ORDER_BY","confidence":0.9},{"regex":".*?(最新|最早|最高|最低).*","query_type":"ORDER_BY","confidence":0.8}]}', '排序查询类型模式', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('intent_query_type_patterns', 'default', 'default', 'default', 
 '分组查询', '{"patterns":[{"regex":"^(分组|按.*分类).*","query_type":"GROUP_BY","confidence":0.9},{"regex":".*?(各个|每个|分别).*","query_type":"GROUP_BY","confidence":0.7}]}', '分组查询类型模式', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator);

-- ================================================
-- 6. 比较模式字典 (intent_comparison_patterns)
-- 用于IntentExtractionStage：比较操作识别
-- ================================================
INSERT IGNORE INTO `t_dictionary_entry` (
    `dictionary_type`, `tenant`, `channel`, `domain`, 
    `entry_key`, `entry_value`, `description`, 
    `status`, `priority`, `created_at`, `updated_at`, 
    `created_by`, `updated_by`
) VALUES 

-- 比较操作模式
('intent_comparison_patterns', 'default', 'default', 'default', 
 '数值比较', '{"patterns":[{"regex":".*?(大于|超过|高于|多于)(\\d+(?:\\.\\d+)?).*","comparison_type":"GREATER_THAN","extraction_group":2},{"regex":".*?(小于|少于|低于|不足)(\\d+(?:\\.\\d+)?).*","comparison_type":"LESS_THAN","extraction_group":2},{"regex":".*?(等于|相等|一样)(\\d+(?:\\.\\d+)?).*","comparison_type":"EQUALS","extraction_group":2}]}', '数值比较操作模式', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('intent_comparison_patterns', 'default', 'default', 'default', 
 '范围比较', '{"patterns":[{"regex":".*?(\\d+(?:\\.\\d+)?)到(\\d+(?:\\.\\d+)?).*","comparison_type":"RANGE","extraction_group":"1,2"},{"regex":".*?在(\\d+(?:\\.\\d+)?)和(\\d+(?:\\.\\d+)?)之间.*","comparison_type":"BETWEEN","extraction_group":"1,2"}]}', '范围比较操作模式', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('intent_comparison_patterns', 'default', 'default', 'default', 
 '文本比较', '{"patterns":[{"regex":".*?(包含|含有|带有)(.+?)的.*","comparison_type":"CONTAINS","extraction_group":2},{"regex":".*?(以(.+?)开头|(.+?)开头).*","comparison_type":"STARTS_WITH","extraction_group":"2,3"},{"regex":".*?(以(.+?)结尾|(.+?)结尾).*","comparison_type":"ENDS_WITH","extraction_group":"2,3"}]}', '文本比较操作模式', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator);

-- ================================================
-- 执行统计信息更新
-- ================================================
-- 调用存储过程更新统计信息
CALL sp_refresh_dictionary_stats();

-- 输出执行结果
SELECT CONCAT('M2意图识别字典数据初始化完成 - 时间: ', FROM_UNIXTIME(@current_timestamp/1000)) AS result;

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
        'intent_catalog',
        'intent_patterns', 
        'intent_keywords',
        'intent_entity_patterns',
        'intent_query_type_patterns',
        'intent_comparison_patterns'
    )
GROUP BY dictionary_type, domain
ORDER BY dictionary_type, domain;