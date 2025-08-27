-- 字典样本数据
-- 用于测试查询转换器的字典驱动功能

-- 标准化停用词数据
INSERT INTO dictionary_entry (id, dictionary_type, tenant, channel, domain, locale, entry_key, entry_value, status, effective_start_time, effective_end_time, version, created_by, created_time, updated_time) VALUES
(1001, 'normalization_stopwords', 'default', 'default', 'default', 'zh_CN', 'common_stopwords', '["的", "了", "在", "是", "我", "有", "和", "就", "不", "人", "都", "一", "一个", "上", "也", "很", "到", "说", "要", "去", "你", "会", "着", "没有", "看", "好", "自己", "这", "那", "个", "们", "这个", "那个", "什么", "怎么", "为什么", "哪里", "谁"]', 'ACTIVE', NOW(), '2099-12-31 23:59:59', 1, 'system', NOW(), NOW()),
(1002, 'normalization_stopwords', 'default', 'default', 'default', 'en_US', 'english_stopwords', '["the", "a", "an", "and", "or", "but", "in", "on", "at", "to", "for", "of", "with", "by", "is", "are", "was", "were", "be", "been", "being", "have", "has", "had", "do", "does", "did", "will", "would", "could", "should", "may", "might", "can", "shall", "must"]', 'ACTIVE', NOW(), '2099-12-31 23:59:59', 1, 'system', NOW(), NOW());

-- 标准化拼写纠错数据
INSERT INTO dictionary_entry (id, dictionary_type, tenant, channel, domain, locale, entry_key, entry_value, status, effective_start_time, effective_end_time, version, created_by, created_time, updated_time) VALUES
(1011, 'normalization_spell_corrections', 'default', 'default', 'default', 'zh_CN', '因该', '"应该"', 'ACTIVE', NOW(), '2099-12-31 23:59:59', 1, 'system', NOW(), NOW()),
(1012, 'normalization_spell_corrections', 'default', 'default', 'default', 'zh_CN', '做为', '"作为"', 'ACTIVE', NOW(), '2099-12-31 23:59:59', 1, 'system', NOW(), NOW()),
(1013, 'normalization_spell_corrections', 'default', 'default', 'default', 'zh_CN', '既使', '"即使"', 'ACTIVE', NOW(), '2099-12-31 23:59:59', 1, 'system', NOW(), NOW()),
(1014, 'normalization_spell_corrections', 'default', 'default', 'default', 'en_US', 'recieve', '"receive"', 'ACTIVE', NOW(), '2099-12-31 23:59:59', 1, 'system', NOW(), NOW()),
(1015, 'normalization_spell_corrections', 'default', 'default', 'default', 'en_US', 'seperate', '"separate"', 'ACTIVE', NOW(), '2099-12-31 23:59:59', 1, 'system', NOW(), NOW()),
(1016, 'normalization_spell_corrections', 'default', 'default', 'default', 'en_US', 'definately', '"definitely"', 'ACTIVE', NOW(), '2099-12-31 23:59:59', 1, 'system', NOW(), NOW());

-- 语义同义词数据
INSERT INTO dictionary_entry (id, dictionary_type, tenant, channel, domain, locale, entry_key, entry_value, status, effective_start_time, effective_end_time, version, created_by, created_time, updated_time) VALUES
(1021, 'semantic_synonyms', 'default', 'default', 'default', 'zh_CN', '人工智能', '["AI", "机器学习", "深度学习", "神经网络", "智能算法"]', 'ACTIVE', NOW(), '2099-12-31 23:59:59', 1, 'system', NOW(), NOW()),
(1022, 'semantic_synonyms', 'default', 'default', 'default', 'zh_CN', '数据库', '["DB", "数据存储", "数据仓库", "存储系统"]', 'ACTIVE', NOW(), '2099-12-31 23:59:59', 1, 'system', NOW(), NOW()),
(1023, 'semantic_synonyms', 'default', 'default', 'default', 'zh_CN', '用户', '["使用者", "客户", "终端用户", "用户端"]', 'ACTIVE', NOW(), '2099-12-31 23:59:59', 1, 'system', NOW(), NOW()),
(1024, 'semantic_synonyms', 'default', 'default', 'default', 'en_US', 'computer', '["PC", "laptop", "machine", "workstation", "desktop"]', 'ACTIVE', NOW(), '2099-12-31 23:59:59', 1, 'system', NOW(), NOW()),
(1025, 'semantic_synonyms', 'default', 'default', 'default', 'en_US', 'software', '["program", "application", "app", "system", "platform"]', 'ACTIVE', NOW(), '2099-12-31 23:59:59', 1, 'system', NOW(), NOW());

-- 语义单位映射数据
INSERT INTO dictionary_entry (id, dictionary_type, tenant, channel, domain, locale, entry_key, entry_value, status, effective_start_time, effective_end_time, version, created_by, created_time, updated_time) VALUES
(1031, 'semantic_unit_mappings', 'default', 'default', 'default', 'zh_CN', '千字节', '"KB"', 'ACTIVE', NOW(), '2099-12-31 23:59:59', 1, 'system', NOW(), NOW()),
(1032, 'semantic_unit_mappings', 'default', 'default', 'default', 'zh_CN', '兆字节', '"MB"', 'ACTIVE', NOW(), '2099-12-31 23:59:59', 1, 'system', NOW(), NOW()),
(1033, 'semantic_unit_mappings', 'default', 'default', 'default', 'zh_CN', '吉字节', '"GB"', 'ACTIVE', NOW(), '2099-12-31 23:59:59', 1, 'system', NOW(), NOW()),
(1034, 'semantic_unit_mappings', 'default', 'default', 'default', 'zh_CN', '公里', '"km"', 'ACTIVE', NOW(), '2099-12-31 23:59:59', 1, 'system', NOW(), NOW()),
(1035, 'semantic_unit_mappings', 'default', 'default', 'default', 'zh_CN', '千米', '"km"', 'ACTIVE', NOW(), '2099-12-31 23:59:59', 1, 'system', NOW(), NOW());

-- 语义时间模式数据
INSERT INTO dictionary_entry (id, dictionary_type, tenant, channel, domain, locale, entry_key, entry_value, status, effective_start_time, effective_end_time, version, created_by, created_time, updated_time) VALUES
(1041, 'semantic_time_patterns', 'default', 'default', 'default', 'zh_CN', 'today_pattern', '{"name": "今天模式", "pattern": "(今天|今日)", "replacement": "TODAY", "description": "识别今天的时间表达", "enabled": true, "priority": 100, "ignoreCase": true, "multiline": false, "dotAll": false}', 'ACTIVE', NOW(), '2099-12-31 23:59:59', 1, 'system', NOW(), NOW()),
(1042, 'semantic_time_patterns', 'default', 'default', 'default', 'zh_CN', 'yesterday_pattern', '{"name": "昨天模式", "pattern": "(昨天|昨日)", "replacement": "YESTERDAY", "description": "识别昨天的时间表达", "enabled": true, "priority": 100, "ignoreCase": true, "multiline": false, "dotAll": false}', 'ACTIVE', NOW(), '2099-12-31 23:59:59', 1, 'system', NOW(), NOW()),
(1043, 'semantic_time_patterns', 'default', 'default', 'default', 'zh_CN', 'date_pattern', '{"name": "日期模式", "pattern": "(\\d{4})[年-](\\d{1,2})[月-](\\d{1,2})[日]?", "replacement": "$1-$2-$3", "description": "标准化日期格式", "enabled": true, "priority": 90, "ignoreCase": false, "multiline": false, "dotAll": false}', 'ACTIVE', NOW(), '2099-12-31 23:59:59', 1, 'system', NOW(), NOW());

-- 语义实体别名数据
INSERT INTO dictionary_entry (id, dictionary_type, tenant, channel, domain, locale, entry_key, entry_value, status, effective_start_time, effective_end_time, version, created_by, created_time, updated_time) VALUES
(1051, 'semantic_entity_aliases', 'default', 'default', 'default', 'zh_CN', '苹果公司', '"Apple Inc."', 'ACTIVE', NOW(), '2099-12-31 23:59:59', 1, 'system', NOW(), NOW()),
(1052, 'semantic_entity_aliases', 'default', 'default', 'default', 'zh_CN', '微软公司', '"Microsoft Corporation"', 'ACTIVE', NOW(), '2099-12-31 23:59:59', 1, 'system', NOW(), NOW()),
(1053, 'semantic_entity_aliases', 'default', 'default', 'default', 'zh_CN', '谷歌', '"Google"', 'ACTIVE', NOW(), '2099-12-31 23:59:59', 1, 'system', NOW(), NOW()),
(1054, 'semantic_entity_aliases', 'default', 'default', 'default', 'zh_CN', '腾讯', '"Tencent"', 'ACTIVE', NOW(), '2099-12-31 23:59:59', 1, 'system', NOW(), NOW()),
(1055, 'semantic_entity_aliases', 'default', 'default', 'default', 'zh_CN', '阿里巴巴', '"Alibaba"', 'ACTIVE', NOW(), '2099-12-31 23:59:59', 1, 'system', NOW(), NOW());

-- 意图目录数据
INSERT INTO dictionary_entry (id, dictionary_type, tenant, channel, domain, locale, entry_key, entry_value, status, effective_start_time, effective_end_time, version, created_by, created_time, updated_time) VALUES
(1061, 'intent_catalog', 'default', 'default', 'default', 'zh_CN', 'query_info', '{"intentId": "query_info", "intentName": "信息查询", "description": "用户查询信息类问题", "parentIntentId": null, "intentType": "QUERY", "weight": 1.0, "enabled": true, "keywords": ["查询", "查找", "搜索", "找", "问"], "entityTypes": ["PERSON", "ORGANIZATION", "LOCATION"], "queryPatterns": [".*查.*", ".*找.*", ".*搜索.*"], "handlerId": "info_query_handler", "properties": {"maxResults": 10, "timeout": 5000}}', 'ACTIVE', NOW(), '2099-12-31 23:59:59', 1, 'system', NOW(), NOW()),
(1062, 'intent_catalog', 'default', 'default', 'default', 'zh_CN', 'action_execute', '{"intentId": "action_execute", "intentName": "执行操作", "description": "用户要求执行某个操作", "parentIntentId": null, "intentType": "COMMAND", "weight": 1.5, "enabled": true, "keywords": ["执行", "运行", "启动", "开始", "创建"], "entityTypes": ["ACTION", "OBJECT"], "queryPatterns": [".*执行.*", ".*运行.*", ".*启动.*"], "handlerId": "action_handler", "properties": {"requireConfirm": true}}', 'ACTIVE', NOW(), '2099-12-31 23:59:59', 1, 'system', NOW(), NOW());

-- 意图实体模式数据
INSERT INTO dictionary_entry (id, dictionary_type, tenant, channel, domain, locale, entry_key, entry_value, status, effective_start_time, effective_end_time, version, created_by, created_time, updated_time) VALUES
(1071, 'intent_entity_patterns', 'default', 'default', 'default', 'zh_CN', 'person_name', '{"name": "人名识别", "pattern": "(张|李|王|刘|陈|杨|黄|赵|周|吴|徐|孙|胡|朱|高|林|何|郭|马|罗|梁|宋|郑|谢|韩|唐|冯|于|董|萧|程|曹|袁|邓|许|傅|沈|曾|彭|吕|苏|卢|蒋|蔡|贾|丁|魏|薛|叶|阎|余|潘|杜|戴|夏|钟|汪|田|任|姜|范|方|石|姚|谭|廖|邹|熊|金|陆|郝|孔|白|崔|康|毛|邱|秦|江|史|顾|侯|邵|孟|龙|万|段|漕|钱|汤|尹|黎|易|常|武|乔|贺|赖|龚|文)[\\u4e00-\\u9fa5]{1,3}", "replacement": "[PERSON:$0]", "description": "识别中文人名", "enabled": true, "priority": 100, "ignoreCase": false, "multiline": false, "dotAll": false}', 'ACTIVE', NOW(), '2099-12-31 23:59:59', 1, 'system', NOW(), NOW()),
(1072, 'intent_entity_patterns', 'default', 'default', 'default', 'zh_CN', 'phone_number', '{"name": "手机号识别", "pattern": "1[3-9]\\d{9}", "replacement": "[PHONE:$0]", "description": "识别手机号码", "enabled": true, "priority": 95, "ignoreCase": false, "multiline": false, "dotAll": false}', 'ACTIVE', NOW(), '2099-12-31 23:59:59', 1, 'system', NOW(), NOW());

-- 意图查询类型模式数据
INSERT INTO dictionary_entry (id, dictionary_type, tenant, channel, domain, locale, entry_key, entry_value, status, effective_start_time, effective_end_time, version, created_by, created_time, updated_time) VALUES
(1081, 'intent_query_type_patterns', 'default', 'default', 'default', 'zh_CN', 'question_wh', '{"name": "疑问词查询", "pattern": "(什么|怎么|为什么|哪里|谁|何时|如何)", "replacement": "[QUESTION_TYPE:WH]", "description": "识别疑问词类型查询", "enabled": true, "priority": 100, "ignoreCase": true, "multiline": false, "dotAll": false}', 'ACTIVE', NOW(), '2099-12-31 23:59:59', 1, 'system', NOW(), NOW()),
(1082, 'intent_query_type_patterns', 'default', 'default', 'default', 'zh_CN', 'question_yn', '{"name": "是否类查询", "pattern": "(是否|是不是|能不能|可不可以|会不会)", "replacement": "[QUESTION_TYPE:YN]", "description": "识别是否类型查询", "enabled": true, "priority": 90, "ignoreCase": true, "multiline": false, "dotAll": false}', 'ACTIVE', NOW(), '2099-12-31 23:59:59', 1, 'system', NOW(), NOW());

-- 意图比较模式数据
INSERT INTO dictionary_entry (id, dictionary_type, tenant, channel, domain, locale, entry_key, entry_value, status, effective_start_time, effective_end_time, version, created_by, created_time, updated_time) VALUES
(1091, 'intent_comparison_patterns', 'default', 'default', 'default', 'zh_CN', 'compare_more', '{"name": "比较级-更多", "pattern": "(更|更加|更多|更好|更大|更小|更高|更低)", "replacement": "[COMPARE:MORE]", "description": "识别比较级表达", "enabled": true, "priority": 100, "ignoreCase": true, "multiline": false, "dotAll": false}', 'ACTIVE', NOW(), '2099-12-31 23:59:59', 1, 'system', NOW(), NOW()),
(1092, 'intent_comparison_patterns', 'default', 'default', 'default', 'zh_CN', 'compare_than', '{"name": "比较级-比", "pattern": "(比.*更|比.*好|比.*差|比.*多|比.*少)", "replacement": "[COMPARE:THAN]", "description": "识别比字句比较", "enabled": true, "priority": 95, "ignoreCase": true, "multiline": false, "dotAll": false}', 'ACTIVE', NOW(), '2099-12-31 23:59:59', 1, 'system', NOW(), NOW());

-- 改写口语化模式数据  
INSERT INTO dictionary_entry (id, dictionary_type, tenant, channel, domain, locale, entry_key, entry_value, status, effective_start_time, effective_end_time, version, created_by, created_time, updated_time) VALUES
(1101, 'rewrite_colloquial_patterns', 'default', 'default', 'default', 'zh_CN', 'colloquial_question', '{"name": "口语化疑问", "pattern": "(咋样|咋办|咋搞|咋回事)", "replacement": "怎么样|怎么办|怎么搞|怎么回事", "description": "口语化疑问词转换", "enabled": true, "priority": 100, "ignoreCase": true, "multiline": false, "dotAll": false}', 'ACTIVE', NOW(), '2099-12-31 23:59:59', 1, 'system', NOW(), NOW()),
(1102, 'rewrite_colloquial_patterns', 'default', 'default', 'default', 'zh_CN', 'colloquial_modal', '{"name": "口语化语气", "pattern": "(木有|没木有)", "replacement": "没有", "description": "口语化语气词转换", "enabled": true, "priority": 90, "ignoreCase": true, "multiline": false, "dotAll": false}', 'ACTIVE', NOW(), '2099-12-31 23:59:59', 1, 'system', NOW(), NOW());

-- 改写关键词权重模式数据
INSERT INTO dictionary_entry (id, dictionary_type, tenant, channel, domain, locale, entry_key, entry_value, status, effective_start_time, effective_end_time, version, created_by, created_time, updated_time) VALUES
(1111, 'rewrite_keyword_weight_patterns', 'default', 'default', 'default', 'zh_CN', 'important_terms', '{"name": "重要术语", "pattern": "(人工智能|机器学习|深度学习|神经网络|数据挖掘)", "weight": 2.0, "handlerId": "tech_term_handler", "description": "技术术语高权重", "enabled": true, "priority": 100, "ignoreCase": true, "multiline": false, "weightMode": "MULTIPLY"}', 'ACTIVE', NOW(), '2099-12-31 23:59:59', 1, 'system', NOW(), NOW()),
(1112, 'rewrite_keyword_weight_patterns', 'default', 'default', 'default', 'zh_CN', 'brand_names', '{"name": "品牌名称", "pattern": "(苹果|华为|小米|OPPO|vivo|三星)", "weight": 1.5, "handlerId": "brand_handler", "description": "品牌名称中等权重", "enabled": true, "priority": 90, "ignoreCase": true, "multiline": false, "weightMode": "MULTIPLY"}', 'ACTIVE', NOW(), '2099-12-31 23:59:59', 1, 'system', NOW(), NOW());

-- 改写技术术语映射数据
INSERT INTO dictionary_entry (id, dictionary_type, tenant, channel, domain, locale, entry_key, entry_value, status, effective_start_time, effective_end_time, version, created_by, created_time, updated_time) VALUES
(1121, 'rewrite_tech_term_mappings', 'default', 'default', 'default', 'zh_CN', 'AI', '"人工智能"', 'ACTIVE', NOW(), '2099-12-31 23:59:59', 1, 'system', NOW(), NOW()),
(1122, 'rewrite_tech_term_mappings', 'default', 'default', 'default', 'zh_CN', 'ML', '"机器学习"', 'ACTIVE', NOW(), '2099-12-31 23:59:59', 1, 'system', NOW(), NOW()),
(1123, 'rewrite_tech_term_mappings', 'default', 'default', 'default', 'zh_CN', 'DL', '"深度学习"', 'ACTIVE', NOW(), '2099-12-31 23:59:59', 1, 'system', NOW(), NOW()),
(1124, 'rewrite_tech_term_mappings', 'default', 'default', 'default', 'zh_CN', 'NLP', '"自然语言处理"', 'ACTIVE', NOW(), '2099-12-31 23:59:59', 1, 'system', NOW(), NOW()),
(1125, 'rewrite_tech_term_mappings', 'default', 'default', 'default', 'zh_CN', 'CV', '"计算机视觉"', 'ACTIVE', NOW(), '2099-12-31 23:59:59', 1, 'system', NOW(), NOW());

-- 改写停用词数据
INSERT INTO dictionary_entry (id, dictionary_type, tenant, channel, domain, locale, entry_key, entry_value, status, effective_start_time, effective_end_time, version, created_by, created_time, updated_time) VALUES
(1131, 'rewrite_stopwords', 'default', 'default', 'default', 'zh_CN', 'rewrite_stopwords', '["嗯", "啊", "哦", "呃", "那个", "这个", "就是说", "也就是说", "换句话说", "总的来说", "基本上", "大概", "差不多", "应该说", "可以说"]', 'ACTIVE', NOW(), '2099-12-31 23:59:59', 1, 'system', NOW(), NOW());

-- 前缀源词数据
INSERT INTO dictionary_entry (id, dictionary_type, tenant, channel, domain, locale, entry_key, entry_value, status, effective_start_time, effective_end_time, version, created_by, created_time, updated_time) VALUES
(1141, 'prefix_source_words', 'default', 'default', 'default', 'zh_CN', 'tech_terms', '["人工智能", "机器学习", "深度学习", "神经网络", "自然语言处理", "计算机视觉", "数据挖掘", "大数据", "云计算", "物联网", "区块链", "量子计算"]', 'ACTIVE', NOW(), '2099-12-31 23:59:59', 1, 'system', NOW(), NOW()),
(1142, 'prefix_source_words', 'default', 'default', 'default', 'zh_CN', 'common_words', '["用户", "系统", "数据", "文件", "网络", "服务", "平台", "应用", "软件", "硬件", "程序", "算法", "接口", "协议", "架构", "框架", "模块", "组件"]', 'ACTIVE', NOW(), '2099-12-31 23:59:59', 1, 'system', NOW(), NOW());

-- 拼音纠错数据（兼容现有）
INSERT INTO dictionary_entry (id, dictionary_type, tenant, channel, domain, locale, entry_key, entry_value, status, effective_start_time, effective_end_time, version, created_by, created_time, updated_time) VALUES
(1151, 'phonetic_corrections', 'default', 'default', 'default', 'zh_CN', 'shouji', '"手机"', 'ACTIVE', NOW(), '2099-12-31 23:59:59', 1, 'system', NOW(), NOW()),
(1152, 'phonetic_corrections', 'default', 'default', 'default', 'zh_CN', 'diannao', '"电脑"', 'ACTIVE', NOW(), '2099-12-31 23:59:59', 1, 'system', NOW(), NOW()),
(1153, 'phonetic_corrections', 'default', 'default', 'default', 'zh_CN', 'wangluo', '"网络"', 'ACTIVE', NOW(), '2099-12-31 23:59:59', 1, 'system', NOW(), NOW()),
(1154, 'phonetic_corrections', 'default', 'default', 'default', 'zh_CN', 'shuju', '"数据"', 'ACTIVE', NOW(), '2099-12-31 23:59:59', 1, 'system', NOW(), NOW());

-- 同义词集合数据（兼容现有）
INSERT INTO dictionary_entry (id, dictionary_type, tenant, channel, domain, locale, entry_key, entry_value, status, effective_start_time, effective_end_time, version, created_by, created_time, updated_time) VALUES
(1161, 'synonym_sets', 'default', 'default', 'default', 'zh_CN', '手机', '["移动电话", "手提电话", "cellular phone", "mobile phone"]', 'ACTIVE', NOW(), '2099-12-31 23:59:59', 1, 'system', NOW(), NOW()),
(1162, 'synonym_sets', 'default', 'default', 'default', 'zh_CN', '电脑', '["计算机", "computer", "PC", "电子计算机"]', 'ACTIVE', NOW(), '2099-12-31 23:59:59', 1, 'system', NOW(), NOW()),
(1163, 'synonym_sets', 'default', 'default', 'default', 'zh_CN', '网络', '["网路", "network", "互联网", "因特网"]', 'ACTIVE', NOW(), '2099-12-31 23:59:59', 1, 'system', NOW(), NOW());

COMMIT;