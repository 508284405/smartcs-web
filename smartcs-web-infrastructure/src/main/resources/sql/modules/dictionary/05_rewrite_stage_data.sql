-- ================================================
-- M3查询改写字典数据SQL脚本 - RewriteStage专用
-- ================================================  
-- 用途：为M3里程碑的查询改写阶段提供字典数据
-- 包含：改写规则、口语化模式、关键词权重、技术术语映射等
-- 执行顺序：在04_intent_extraction_data.sql之后执行
-- ================================================

-- 设置SQL执行参数
SET @current_timestamp = UNIX_TIMESTAMP(NOW()) * 1000;
SET @creator = 'system_m3_rewrite';

-- ================================================
-- 1. 改写规则字典 (rewrite_rules)
-- 用于RewriteStage：查询重构规则
-- ================================================
INSERT IGNORE INTO `t_dictionary_entry` (
    `dictionary_type`, `tenant`, `channel`, `domain`, 
    `entry_key`, `entry_value`, `description`, 
    `status`, `priority`, `created_at`, `updated_at`, 
    `created_by`, `updated_by`
) VALUES 

-- 通用改写规则
('rewrite_rules', 'default', 'default', 'default', 
 '口语转正式', '{"rules":[{"pattern":"咋样","replacement":"怎么样","type":"colloquial_to_formal"},{"pattern":"啥","replacement":"什么","type":"colloquial_to_formal"},{"pattern":"咋","replacement":"怎么","type":"colloquial_to_formal"},{"pattern":"整啥","replacement":"做什么","type":"colloquial_to_formal"}]}', '口语化表达转正式表达', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('rewrite_rules', 'default', 'default', 'default', 
 '简化复合查询', '{"rules":[{"pattern":"我想要查询关于(.+?)的(.+?)信息","replacement":"查询$2 $1","type":"query_simplification"},{"pattern":"请帮我找一下(.+?)相关的(.+?)数据","replacement":"查询$1 $2","type":"query_simplification"},{"pattern":"能否告诉我(.+?)的(.+?)情况","replacement":"查询$1 $2状态","type":"query_simplification"}]}', '复合查询简化规则', 
 'ACTIVE', 95, @current_timestamp, @current_timestamp, @creator, @creator),

('rewrite_rules', 'default', 'default', 'default', 
 '冗余词移除', '{"rules":[{"pattern":"请问","replacement":"","type":"redundancy_removal"},{"pattern":"麻烦","replacement":"","type":"redundancy_removal"},{"pattern":"可以帮我","replacement":"","type":"redundancy_removal"},{"pattern":"能否","replacement":"","type":"redundancy_removal"}]}', '移除查询中的冗余词汇', 
 'ACTIVE', 90, @current_timestamp, @current_timestamp, @creator, @creator),

-- 技术领域改写规则
('rewrite_rules', 'default', 'default', 'tech', 
 '技术术语标准化', '{"rules":[{"pattern":"程序猿","replacement":"程序员","type":"term_standardization"},{"pattern":"码农","replacement":"程序员","type":"term_standardization"},{"pattern":"攻城狮","replacement":"工程师","type":"term_standardization"},{"pattern":"产品狗","replacement":"产品经理","type":"term_standardization"}]}', '技术行业术语标准化', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('rewrite_rules', 'default', 'default', 'tech', 
 '代码相关改写', '{"rules":[{"pattern":"跑代码","replacement":"执行代码","type":"action_formalization"},{"pattern":"跑程序","replacement":"运行程序","type":"action_formalization"},{"pattern":"跑脚本","replacement":"执行脚本","type":"action_formalization"},{"pattern":"测试下","replacement":"执行测试","type":"action_formalization"}]}', '代码执行相关表达改写', 
 'ACTIVE', 95, @current_timestamp, @current_timestamp, @creator, @creator),

-- 电商领域改写规则
('rewrite_rules', 'default', 'default', 'ecommerce', 
 '购物表达改写', '{"rules":[{"pattern":"买东西","replacement":"购买商品","type":"action_formalization"},{"pattern":"下单","replacement":"创建订单","type":"action_formalization"},{"pattern":"砍价","replacement":"议价","type":"action_formalization"},{"pattern":"包邮","replacement":"免运费","type":"term_standardization"}]}', '购物相关表达改写', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

-- 客服领域改写规则
('rewrite_rules', 'default', 'default', 'customer_service', 
 '客服表达改写', '{"rules":[{"pattern":"投诉","replacement":"反馈问题","type":"tone_adjustment"},{"pattern":"催单","replacement":"查询订单状态","type":"intent_clarification"},{"pattern":"退货","replacement":"申请退货","type":"action_formalization"}]}', '客服场景表达改写', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator);

-- ================================================
-- 2. 口语化模式字典 (rewrite_colloquial_patterns)
-- 用于RewriteStage：口语化表达转换
-- ================================================
INSERT IGNORE INTO `t_dictionary_entry` (
    `dictionary_type`, `tenant`, `channel`, `domain`, 
    `entry_key`, `entry_value`, `description`, 
    `status`, `priority`, `created_at`, `updated_at`, 
    `created_by`, `updated_by`
) VALUES 

-- 中文口语化模式
('rewrite_colloquial_patterns', 'default', 'default', 'default', 
 '中文口语词汇', '{"patterns":[{"colloquial":"咋回事","formal":"什么情况","context":"询问"},{"colloquial":"搞定","formal":"完成","context":"动作"},{"colloquial":"整不明白","formal":"不理解","context":"状态"},{"colloquial":"弄啥","formal":"做什么","context":"询问"}]}', '中文口语化词汇标准化', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('rewrite_colloquial_patterns', 'default', 'default', 'default', 
 '网络流行语', '{"patterns":[{"colloquial":"666","formal":"很好","context":"评价"},{"colloquial":"yyds","formal":"永远的神","context":"赞美"},{"colloquial":"绝绝子","formal":"非常好","context":"评价"},{"colloquial":"拿捏","formal":"掌握","context":"动作"}]}', '网络流行语标准化', 
 'ACTIVE', 95, @current_timestamp, @current_timestamp, @creator, @creator),

-- 方言转普通话
('rewrite_colloquial_patterns', 'default', 'default', 'default', 
 '方言词汇', '{"patterns":[{"colloquial":"木有","formal":"没有","context":"否定","dialect":"网络语"},{"colloquial":"银家","formal":"人家","context":"称谓","dialect":"网络语"},{"colloquial":"酱紫","formal":"这样","context":"指代","dialect":"网络语"}]}', '方言词汇标准化', 
 'ACTIVE', 90, @current_timestamp, @current_timestamp, @creator, @creator),

-- 英文口语化模式
('rewrite_colloquial_patterns', 'default', 'default', 'tech', 
 '英文口语技术词', '{"patterns":[{"colloquial":"gonna","formal":"going to","context":"future"},{"colloquial":"wanna","formal":"want to","context":"desire"},{"colloquial":"dunno","formal":"do not know","context":"uncertainty"}]}', '英文口语技术表达标准化', 
 'ACTIVE', 85, @current_timestamp, @current_timestamp, @creator, @creator);

-- ================================================
-- 3. 关键词权重模式字典 (rewrite_keyword_weight_patterns)
-- 用于RewriteStage：关键词权重调整
-- ================================================
INSERT IGNORE INTO `t_dictionary_entry` (
    `dictionary_type`, `tenant`, `channel`, `domain`, 
    `entry_key`, `entry_value`, `description`, 
    `status`, `priority`, `created_at`, `updated_at`, 
    `created_by`, `updated_by`
) VALUES 

-- 通用关键词权重
('rewrite_keyword_weight_patterns', 'default', 'default', 'default', 
 '高权重词汇', '{"weight_rules":[{"keyword":"紧急","weight":2.0,"context":"优先级"},{"keyword":"重要","weight":1.8,"context":"优先级"},{"keyword":"关键","weight":1.5,"context":"优先级"},{"keyword":"核心","weight":1.5,"context":"重要性"}]}', '高权重关键词规则', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('rewrite_keyword_weight_patterns', 'default', 'default', 'default', 
 '低权重词汇', '{"weight_rules":[{"keyword":"可能","weight":0.7,"context":"不确定性"},{"keyword":"或许","weight":0.6,"context":"不确定性"},{"keyword":"大概","weight":0.6,"context":"模糊性"},{"keyword":"好像","weight":0.5,"context":"不确定性"}]}', '低权重关键词规则', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

-- 技术领域关键词权重
('rewrite_keyword_weight_patterns', 'default', 'default', 'tech', 
 '技术高权重词', '{"weight_rules":[{"keyword":"性能","weight":2.0,"context":"技术指标"},{"keyword":"安全","weight":2.0,"context":"技术要求"},{"keyword":"架构","weight":1.8,"context":"技术设计"},{"keyword":"优化","weight":1.5,"context":"技术改进"}]}', '技术领域高权重关键词', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

-- 电商领域关键词权重
('rewrite_keyword_weight_patterns', 'default', 'default', 'ecommerce', 
 '电商高权重词', '{"weight_rules":[{"keyword":"销量","weight":2.0,"context":"商业指标"},{"keyword":"转化率","weight":1.8,"context":"商业指标"},{"keyword":"用户体验","weight":1.5,"context":"产品质量"},{"keyword":"库存","weight":1.5,"context":"运营管理"}]}', '电商领域高权重关键词', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator);

-- ================================================
-- 4. 技术术语映射字典 (rewrite_tech_term_mappings)
-- 用于RewriteStage：技术术语标准化
-- ================================================
INSERT IGNORE INTO `t_dictionary_entry` (
    `dictionary_type`, `tenant`, `channel`, `domain`, 
    `entry_key`, `entry_value`, `description`, 
    `status`, `priority`, `created_at`, `updated_at`, 
    `created_by`, `updated_by`
) VALUES 

-- 编程术语映射
('rewrite_tech_term_mappings', 'default', 'default', 'tech', 
 '编程概念', '{"mappings":{"方法":"method","函数":"function","类":"class","对象":"object","变量":"variable","常量":"constant","数组":"array","列表":"list"}}', '编程概念术语映射', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('rewrite_tech_term_mappings', 'default', 'default', 'tech', 
 '数据库概念', '{"mappings":{"表":"table","字段":"field","索引":"index","主键":"primary key","外键":"foreign key","查询":"query","事务":"transaction","存储过程":"stored procedure"}}', '数据库概念术语映射', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('rewrite_tech_term_mappings', 'default', 'default', 'tech', 
 '系统架构', '{"mappings":{"微服务":"microservice","API":"interface","服务":"service","组件":"component","模块":"module","框架":"framework","中间件":"middleware","负载均衡":"load balancing"}}', '系统架构术语映射', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

-- 云计算术语映射
('rewrite_tech_term_mappings', 'default', 'default', 'tech', 
 '云计算概念', '{"mappings":{"容器":"container","镜像":"image","集群":"cluster","节点":"node","部署":"deployment","服务网格":"service mesh","监控":"monitoring","日志":"logging"}}', '云计算概念术语映射', 
 'ACTIVE', 95, @current_timestamp, @current_timestamp, @creator, @creator),

-- AI/ML术语映射
('rewrite_tech_term_mappings', 'default', 'default', 'tech', 
 'AI机器学习', '{"mappings":{"模型":"model","算法":"algorithm","训练":"training","推理":"inference","特征":"feature","数据集":"dataset","神经网络":"neural network","深度学习":"deep learning"}}', 'AI机器学习术语映射', 
 'ACTIVE', 95, @current_timestamp, @current_timestamp, @creator, @creator);

-- ================================================
-- 5. 改写停用词字典 (rewrite_stopwords)
-- 用于RewriteStage：停用词处理（改写阶段专用）
-- ================================================
INSERT IGNORE INTO `t_dictionary_entry` (
    `dictionary_type`, `tenant`, `channel`, `domain`, 
    `entry_key`, `entry_value`, `description`, 
    `status`, `priority`, `created_at`, `updated_at`, 
    `created_by`, `updated_by`
) VALUES 

-- 改写阶段停用词（保留语义但可简化的词汇）
('rewrite_stopwords', 'default', 'default', 'default', 
 '礼貌用词', '["请","谢谢","麻烦","劳烦","拜托","不好意思","打扰一下"]', '礼貌用词停用词（改写时可移除）', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('rewrite_stopwords', 'default', 'default', 'default', 
 '冗余修饰词', '["非常","特别","十分","极其","相当","比较","还是","应该","可能","或许","大概","好像"]', '冗余修饰词（改写时可简化）', 
 'ACTIVE', 90, @current_timestamp, @current_timestamp, @creator, @creator),

('rewrite_stopwords', 'default', 'default', 'default', 
 '填充词汇', '["这个","那个","这样","那样","这种","那种","这里","那里","然后","接着","另外","此外"]', '填充性词汇（改写时可优化）', 
 'ACTIVE', 85, @current_timestamp, @current_timestamp, @creator, @creator),

-- 技术领域改写停用词
('rewrite_stopwords', 'default', 'default', 'tech', 
 '技术填充词', '["这个功能","那个模块","这种方式","那种方法","这边","那边","目前","现在","当前"]', '技术讨论中的填充词汇', 
 'ACTIVE', 80, @current_timestamp, @current_timestamp, @creator, @creator),

-- 客服领域改写停用词
('rewrite_stopwords', 'default', 'default', 'customer_service', 
 '客服场景填充词', '["亲","亲爱的","您好","不好意思","真的是","实在是","确实","的确"]', '客服场景中的填充和礼貌用词', 
 'ACTIVE', 75, @current_timestamp, @current_timestamp, @creator, @creator);

-- ================================================
-- 6. 查询语气调整字典 (rewrite_tone_adjustment)
-- 用于RewriteStage：语气和情感调整
-- ================================================
INSERT IGNORE INTO `t_dictionary_entry` (
    `dictionary_type`, `tenant`, `channel`, `domain`, 
    `entry_key`, `entry_value`, `description`, 
    `status`, `priority`, `created_at`, `updated_at`, 
    `created_by`, `updated_by`
) VALUES 

-- 语气标准化
('rewrite_tone_adjustment', 'default', 'default', 'default', 
 '情绪词调整', '{"adjustments":[{"emotional":"超级生气","neutral":"不满意","intensity_reduction":true},{"emotional":"特别开心","neutral":"满意","intensity_reduction":true},{"emotional":"非常着急","neutral":"紧急","intensity_reduction":true}]}', '情绪词汇语气调整', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('rewrite_tone_adjustment', 'default', 'default', 'default', 
 '命令式转请求式', '{"adjustments":[{"command":"给我查一下","polite":"请帮我查询","tone_type":"politeness"},{"command":"快点处理","polite":"请尽快处理","tone_type":"urgency_polite"},{"command":"立即解决","polite":"请及时解决","tone_type":"urgency_polite"}]}', '命令式语气转请求式', 
 'ACTIVE', 95, @current_timestamp, @current_timestamp, @creator, @creator),

-- 客服场景语气调整
('rewrite_tone_adjustment', 'default', 'default', 'customer_service', 
 '客服语气标准化', '{"adjustments":[{"complaint":"这什么破系统","professional":"系统出现问题","tone_type":"professional"},{"complaint":"太垃圾了","professional":"体验不佳","tone_type":"constructive"},{"complaint":"简直无语","professional":"需要改进","tone_type":"constructive"}]}', '客服场景语气专业化', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator);

-- ================================================
-- 执行统计信息更新
-- ================================================
-- 调用存储过程更新统计信息
CALL sp_refresh_dictionary_stats();

-- 输出执行结果
SELECT CONCAT('M3查询改写字典数据初始化完成 - 时间: ', FROM_UNIXTIME(@current_timestamp/1000)) AS result;

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
        'rewrite_rules',
        'rewrite_colloquial_patterns',
        'rewrite_keyword_weight_patterns',
        'rewrite_tech_term_mappings',
        'rewrite_stopwords',
        'rewrite_tone_adjustment'
    )
GROUP BY dictionary_type, domain
ORDER BY dictionary_type, domain;