-- ================================================
-- 基础字典数据SQL脚本 - 查询转换器管线基础阶段
-- ================================================
-- 用途：为查询转换器管线的基础阶段提供字典数据
-- 包含：标准化规则、拼音纠错、停用词、前缀词汇、基础同义词
-- 执行顺序：在01_dictionary_entry.sql之后执行
-- ================================================

-- 设置SQL执行参数
SET @current_timestamp = UNIX_TIMESTAMP(NOW()) * 1000;
SET @creator = 'system_init';

-- ================================================
-- 1. 标准化规则字典 (normalization_rules)
-- 用于NormalizationStage：错别字纠正、语法规范化
-- ================================================
INSERT IGNORE INTO `t_dictionary_entry` (
    `dictionary_type`, `tenant`, `channel`, `domain`, 
    `entry_key`, `entry_value`, `description`, 
    `status`, `priority`, `created_at`, `updated_at`, 
    `created_by`, `updated_by`
) VALUES 

-- 常见错别字纠正
('normalization_rules', 'default', 'default', 'default', 
 '因该', '"应该"', '因该→应该 常见错别字纠正', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('normalization_rules', 'default', 'default', 'default', 
 '做为', '"作为"', '做为→作为 常见错别字纠正', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('normalization_rules', 'default', 'default', 'default', 
 '以至', '"以致"', '以至→以致 混淆词纠正', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('normalization_rules', 'default', 'default', 'default', 
 '在与', '"在于"', '在与→在于 常见错别字', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('normalization_rules', 'default', 'default', 'default', 
 '即然', '"既然"', '即然→既然 混淆词纠正', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

-- 标点符号规范化
('normalization_rules', 'default', 'default', 'default', 
 '？？', '"？"', '多问号规范化', 
 'ACTIVE', 90, @current_timestamp, @current_timestamp, @creator, @creator),

('normalization_rules', 'default', 'default', 'default', 
 '！！', '"！"', '多感叹号规范化', 
 'ACTIVE', 90, @current_timestamp, @current_timestamp, @creator, @creator),

-- 全角半角统一
('normalization_rules', 'default', 'default', 'default', 
 '（', '"("', '全角左括号转半角', 
 'ACTIVE', 95, @current_timestamp, @current_timestamp, @creator, @creator),

('normalization_rules', 'default', 'default', 'default', 
 '）', '")"', '全角右括号转半角', 
 'ACTIVE', 95, @current_timestamp, @current_timestamp, @creator, @creator),

('normalization_rules', 'default', 'default', 'default', 
 '，', '","', '全角逗号转半角', 
 'ACTIVE', 95, @current_timestamp, @current_timestamp, @creator, @creator);

-- ================================================
-- 2. 拼音纠错字典 (phonetic_corrections)
-- 用于PhoneticCorrectionStage：拼音错拼纠正
-- ================================================
INSERT IGNORE INTO `t_dictionary_entry` (
    `dictionary_type`, `tenant`, `channel`, `domain`, 
    `entry_key`, `entry_value`, `description`, 
    `status`, `priority`, `created_at`, `updated_at`, 
    `created_by`, `updated_by`
) VALUES 

-- 常见人名拼音错拼
('phonetic_corrections', 'default', 'default', 'default', 
 '朱丽业', '"朱丽叶"', '朱丽业→朱丽叶 人名拼音纠错', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('phonetic_corrections', 'default', 'default', 'default', 
 '莎士比亚', '"莎士比亚"', '已正确但用于示例', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

-- 地名拼音错拼
('phonetic_corrections', 'default', 'default', 'default', 
 '北经', '"北京"', '北经→北京 地名拼音纠错', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('phonetic_corrections', 'default', 'default', 'default', 
 '上海', '"上海"', '已正确地名', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

-- 技术术语拼音错拼
('phonetic_corrections', 'default', 'default', 'default', 
 '数聚库', '"数据库"', '数聚库→数据库 技术词汇纠错', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('phonetic_corrections', 'default', 'default', 'default', 
 '算发', '"算法"', '算发→算法 技术词汇纠错', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('phonetic_corrections', 'default', 'default', 'default', 
 '人功智能', '"人工智能"', '人功智能→人工智能 技术词汇纠错', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator);

-- ================================================
-- 3. 停用词字典 (stop_words)
-- 用于多个Stage：停用词过滤
-- ================================================
INSERT IGNORE INTO `t_dictionary_entry` (
    `dictionary_type`, `tenant`, `channel`, `domain`, 
    `entry_key`, `entry_value`, `description`, 
    `status`, `priority`, `created_at`, `updated_at`, 
    `created_by`, `updated_by`
) VALUES 

-- 中文停用词集合
('stop_words', 'default', 'default', 'default', 
 '基础停用词', '["的","了","在","是","我","有","和","就","不","人","都","一","上","也","很","到","说","要","去","你","会","着","没有","看","好","自己","这"]', '基础中文停用词集合', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

-- 英文停用词集合  
('stop_words', 'default', 'default', 'default', 
 '英文停用词', '["the","a","an","and","or","but","in","on","at","to","for","of","with","by","from","as","is","are","was","were","be","been","have","has","had","do","does","did","will","would","could","should","may","might","can","must","shall","this","that","these","those"]', '英文停用词集合', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

-- 查询相关停用词
('stop_words', 'default', 'default', 'default', 
 '查询停用词', '["请问","麻烦","谢谢","请","帮忙","能否","可以","可不可以","能不能","有没有","是否","是不是","对不对","好不好"]', '查询场景专用停用词', 
 'ACTIVE', 90, @current_timestamp, @current_timestamp, @creator, @creator);

-- ================================================
-- 4. 前缀补全词汇字典 (prefix_words)
-- 用于PrefixCompletionStage：查询前缀补全
-- ================================================
INSERT IGNORE INTO `t_dictionary_entry` (
    `dictionary_type`, `tenant`, `channel`, `domain`, 
    `entry_key`, `entry_value`, `description`, 
    `status`, `priority`, `created_at`, `updated_at`, 
    `created_by`, `updated_by`
) VALUES 

-- 中文疑问词前缀
('prefix_words', 'default', 'default', 'default', 
 '疑问前缀', '["如何","怎么","什么","为什么","哪里","谁","哪个","哪些","多少","几","何时","何地","何人","几时"]', '中文疑问词前缀集合', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

-- 英文疑问词前缀
('prefix_words', 'default', 'default', 'default', 
 '英文疑问前缀', '["how","what","why","where","who","when","which","whose","whom","how many","how much","how long","how often"]', '英文疑问词前缀集合', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

-- 动作指令前缀
('prefix_words', 'default', 'default', 'default', 
 '动作前缀', '["查询","搜索","寻找","获取","显示","列出","统计","分析","计算","检查","验证","测试","创建","建立","删除","更新","修改"]', '动作指令前缀词汇', 
 'ACTIVE', 95, @current_timestamp, @current_timestamp, @creator, @creator),

-- 比较关系前缀
('prefix_words', 'default', 'default', 'default', 
 '比较前缀', '["比较","对比","区别","差异","相同","类似","大于","小于","等于","高于","低于","超过","不足","最大","最小","最多","最少"]', '比较关系前缀词汇', 
 'ACTIVE', 90, @current_timestamp, @current_timestamp, @creator, @creator);

-- ================================================
-- 5. 基础同义词字典 (synonym_sets)
-- 用于SynonymRecallStage：同义词扩展
-- ================================================
INSERT IGNORE INTO `t_dictionary_entry` (
    `dictionary_type`, `tenant`, `channel`, `domain`, 
    `entry_key`, `entry_value`, `description`, 
    `status`, `priority`, `created_at`, `updated_at`, 
    `created_by`, `updated_by`
) VALUES 

-- 问题相关同义词
('synonym_sets', 'default', 'default', 'default', 
 '问题', '["问题","疑问","困惑","难题","疑惑","问题点","疑点","issue","problem","question","trouble","difficulty"]', '问题相关同义词组', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

-- 方法相关同义词
('synonym_sets', 'default', 'default', 'default', 
 '方法', '["方法","方式","途径","办法","手段","方案","措施","策略","路径","方法论","method","way","approach","solution","strategy","means"]', '方法相关同义词组', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

-- 查找相关同义词
('synonym_sets', 'default', 'default', 'default', 
 '查找', '["查找","搜索","寻找","检索","查询","搜寻","找寻","探寻","查看","search","find","look for","seek","query","retrieve"]', '查找相关同义词组', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

-- 获取相关同义词
('synonym_sets', 'default', 'default', 'default', 
 '获取', '["获取","取得","得到","拿到","取出","提取","获得","收集","采集","get","obtain","acquire","fetch","retrieve","collect"]', '获取相关同义词组', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

-- 显示相关同义词
('synonym_sets', 'default', 'default', 'default', 
 '显示', '["显示","展示","列出","展现","呈现","表示","show","display","list","present","exhibit","demonstrate"]', '显示相关同义词组', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

-- 创建相关同义词
('synonym_sets', 'default', 'default', 'default', 
 '创建', '["创建","建立","新建","创造","生成","制作","构建","create","build","make","generate","establish","construct"]', '创建相关同义词组', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

-- 删除相关同义词
('synonym_sets', 'default', 'default', 'default', 
 '删除', '["删除","移除","清除","去掉","删掉","剔除","delete","remove","clear","drop","eliminate"]', '删除相关同义词组', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

-- 修改相关同义词
('synonym_sets', 'default', 'default', 'default', 
 '修改', '["修改","更新","编辑","调整","变更","改动","update","modify","edit","change","alter","revise"]', '修改相关同义词组', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

-- 数据相关同义词
('synonym_sets', 'default', 'default', 'default', 
 '数据', '["数据","资料","信息","材料","内容","记录","data","information","content","records","materials"]', '数据相关同义词组', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

-- 系统相关同义词
('synonym_sets', 'default', 'default', 'default', 
 '系统', '["系统","平台","应用","程序","软件","工具","system","platform","application","program","software","tool"]', '系统相关同义词组', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator);

-- ================================================
-- 6. 标准化拼写纠错字典 (normalization_spelling_corrections) 
-- 用于NormalizationStage：高频拼写错误纠正
-- ================================================
INSERT IGNORE INTO `t_dictionary_entry` (
    `dictionary_type`, `tenant`, `channel`, `domain`, 
    `entry_key`, `entry_value`, `description`, 
    `status`, `priority`, `created_at`, `updated_at`, 
    `created_by`, `updated_by`
) VALUES 

-- 高频英文拼写错误
('normalization_spelling_corrections', 'default', 'default', 'default', 
 'recieve', '"receive"', 'recieve→receive 英文拼写纠错', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('normalization_spelling_corrections', 'default', 'default', 'default', 
 'seperate', '"separate"', 'seperate→separate 英文拼写纠错', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('normalization_spelling_corrections', 'default', 'default', 'default', 
 'definately', '"definitely"', 'definately→definitely 英文拼写纠错', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('normalization_spelling_corrections', 'default', 'default', 'default', 
 'occured', '"occurred"', 'occured→occurred 英文拼写纠错', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator);

-- ================================================
-- 执行统计信息更新
-- ================================================
-- 调用存储过程更新统计信息
CALL sp_refresh_dictionary_stats();

-- 输出执行结果
SELECT CONCAT('基础字典数据初始化完成 - 时间: ', FROM_UNIXTIME(@current_timestamp/1000)) AS result;

-- 显示插入的数据统计
SELECT 
    dictionary_type, 
    COUNT(*) as entry_count,
    COUNT(CASE WHEN status = 'ACTIVE' THEN 1 END) as active_count
FROM t_dictionary_entry 
WHERE tenant = 'default' 
    AND channel = 'default' 
    AND domain = 'default'
    AND dictionary_type IN (
        'normalization_rules', 
        'phonetic_corrections', 
        'stop_words', 
        'prefix_words', 
        'synonym_sets',
        'normalization_spelling_corrections'
    )
GROUP BY dictionary_type
ORDER BY dictionary_type;