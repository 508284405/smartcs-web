-- ================================================
-- M3扩展策略字典数据SQL脚本 - ExpansionStrategyStage专用
-- ================================================
-- 用途：为M3里程碑的扩展策略阶段提供字典数据
-- 包含：扩展策略规则、扩展模板、查询增强策略等
-- 执行顺序：在05_rewrite_stage_data.sql之后执行
-- ================================================

-- 设置SQL执行参数
SET @current_timestamp = UNIX_TIMESTAMP(NOW()) * 1000;
SET @creator = 'system_m3_expansion';

-- ================================================
-- 1. 扩展策略字典 (expansion_strategies)
-- 用于ExpansionStrategyStage：查询扩展策略规则
-- ================================================
INSERT IGNORE INTO `t_dictionary_entry` (
    `dictionary_type`, `tenant`, `channel`, `domain`, 
    `entry_key`, `entry_value`, `description`, 
    `status`, `priority`, `created_at`, `updated_at`, 
    `created_by`, `updated_by`
) VALUES 

-- 通用查询扩展策略
('expansion_strategies', 'default', 'default', 'default', 
 '如何', '"{query} 方法", "{query} 步骤", "{query} 教程"', '如何类查询扩展策略', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('expansion_strategies', 'default', 'default', 'default', 
 '什么是', '"{query}", "{pattern}的定义", "{pattern}的概念", "{pattern}的含义"', '什么是类查询扩展策略', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('expansion_strategies', 'default', 'default', 'default', 
 '为什么', '"{query}", "{pattern}的原因", "{pattern}的机制", "{pattern}的背景"', '为什么类查询扩展策略', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('expansion_strategies', 'default', 'default', 'default', 
 '故障', '"{query}", "{query} 解决", "{query} 排查", "{query} 修复"', '故障类查询扩展策略', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('expansion_strategies', 'default', 'default', 'default', 
 '错误', '"{query}", "{query} 解决方案", "{query} 处理", "解决{query}"', '错误类查询扩展策略', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('expansion_strategies', 'default', 'default', 'default', 
 '安装', '"{query}", "{query} 配置", "{query} 部署", "{query} 设置"', '安装类查询扩展策略', 
 'ACTIVE', 95, @current_timestamp, @current_timestamp, @creator, @creator),

('expansion_strategies', 'default', 'default', 'default', 
 '配置', '"{query}", "{query} 设置", "{query} 参数", "{query} 选项"', '配置类查询扩展策略', 
 'ACTIVE', 95, @current_timestamp, @current_timestamp, @creator, @creator),

-- 技术领域扩展策略
('expansion_strategies', 'default', 'default', 'tech', 
 'API', '"{query}", "{query} 接口", "{query} 文档", "{query} 使用"', 'API相关查询扩展策略', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('expansion_strategies', 'default', 'default', 'tech', 
 '数据库', '"{query}", "{query} 操作", "{query} 查询", "{query} 优化"', '数据库相关查询扩展策略', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('expansion_strategies', 'default', 'default', 'tech', 
 '框架', '"{query}", "{query} 使用", "{query} 配置", "{query} 教程"', '框架相关查询扩展策略', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('expansion_strategies', 'default', 'default', 'tech', 
 '性能', '"{query}", "{query} 优化", "{query} 调优", "{query} 监控"', '性能相关查询扩展策略', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('expansion_strategies', 'default', 'default', 'tech', 
 '安全', '"{query}", "{query} 防护", "{query} 漏洞", "{query} 加固"', '安全相关查询扩展策略', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('expansion_strategies', 'default', 'default', 'tech', 
 '部署', '"{query}", "{query} 发布", "{query} 上线", "{query} 运维"', '部署相关查询扩展策略', 
 'ACTIVE', 95, @current_timestamp, @current_timestamp, @creator, @creator),

('expansion_strategies', 'default', 'default', 'tech', 
 '微服务', '"{query}", "{query} 架构", "{query} 治理", "{query} 监控"', '微服务相关查询扩展策略', 
 'ACTIVE', 95, @current_timestamp, @current_timestamp, @creator, @creator),

('expansion_strategies', 'default', 'default', 'tech', 
 '容器', '"{query}", "{query} Docker", "{query} Kubernetes", "{query} 编排"', '容器相关查询扩展策略', 
 'ACTIVE', 95, @current_timestamp, @current_timestamp, @creator, @creator),

-- 电商领域扩展策略
('expansion_strategies', 'default', 'default', 'ecommerce', 
 '订单', '"{query}", "{query} 处理", "{query} 管理", "{query} 状态"', '订单相关查询扩展策略', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('expansion_strategies', 'default', 'default', 'ecommerce', 
 '商品', '"{query}", "{query} 管理", "{query} 上架", "{query} 库存"', '商品相关查询扩展策略', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('expansion_strategies', 'default', 'default', 'ecommerce', 
 '支付', '"{query}", "{query} 处理", "{query} 异常", "{query} 对账"', '支付相关查询扩展策略', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('expansion_strategies', 'default', 'default', 'ecommerce', 
 '库存', '"{query}", "{query} 管理", "{query} 预警", "{query} 同步"', '库存相关查询扩展策略', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('expansion_strategies', 'default', 'default', 'ecommerce', 
 '促销', '"{query}", "{query} 活动", "{query} 规则", "{query} 策略"', '促销相关查询扩展策略', 
 'ACTIVE', 95, @current_timestamp, @current_timestamp, @creator, @creator),

('expansion_strategies', 'default', 'default', 'ecommerce', 
 '用户', '"{query}", "{query} 管理", "{query} 行为", "{query} 画像"', '用户相关查询扩展策略', 
 'ACTIVE', 95, @current_timestamp, @current_timestamp, @creator, @creator),

('expansion_strategies', 'default', 'default', 'ecommerce', 
 '数据分析', '"{query}", "{query} 报表", "{query} 统计", "{query} 指标"', '数据分析相关查询扩展策略', 
 'ACTIVE', 90, @current_timestamp, @current_timestamp, @creator, @creator),

-- 客服领域扩展策略
('expansion_strategies', 'default', 'default', 'customer_service', 
 '投诉', '"{query}", "{query} 处理", "{query} 解决", "{query} 反馈"', '投诉相关查询扩展策略', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('expansion_strategies', 'default', 'default', 'customer_service', 
 '咨询', '"{query}", "{query} 回复", "{query} 解答", "{query} 指导"', '咨询相关查询扩展策略', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('expansion_strategies', 'default', 'default', 'customer_service', 
 '退款', '"{query}", "{query} 流程", "{query} 审核", "{query} 处理"', '退款相关查询扩展策略', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('expansion_strategies', 'default', 'default', 'customer_service', 
 '售后', '"{query}", "{query} 服务", "{query} 支持", "{query} 维护"', '售后相关查询扩展策略', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('expansion_strategies', 'default', 'default', 'customer_service', 
 '工单', '"{query}", "{query} 流转", "{query} 处理", "{query} 跟踪"', '工单相关查询扩展策略', 
 'ACTIVE', 95, @current_timestamp, @current_timestamp, @creator, @creator);

-- ================================================
-- 2. 扩展模板字典 (expansion_templates)
-- 用于ExpansionStrategyStage：查询扩展模板
-- ================================================
INSERT IGNORE INTO `t_dictionary_entry` (
    `dictionary_type`, `tenant`, `channel`, `domain`, 
    `entry_key`, `entry_value`, `description`, 
    `status`, `priority`, `created_at`, `updated_at`, 
    `created_by`, `updated_by`
) VALUES 

-- 问题解决类模板
('expansion_templates', 'default', 'default', 'default', 
 '问题解决模板', '{"templates":["如何解决{query}","解决{query}的方法","{query}故障排除","处理{query}的步骤"]}', '问题解决类查询扩展模板', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

-- 信息查询类模板
('expansion_templates', 'default', 'default', 'default', 
 '信息查询模板', '{"templates":["什么是{query}","{query}的定义","{query}的概念","关于{query}的信息"]}', '信息查询类查询扩展模板', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

-- 操作指导类模板
('expansion_templates', 'default', 'default', 'default', 
 '操作指导模板', '{"templates":["如何使用{query}","{query}操作指南","{query}使用方法","{query}操作步骤"]}', '操作指导类查询扩展模板', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

-- 对比分析类模板
('expansion_templates', 'default', 'default', 'default', 
 '对比分析模板', '{"templates":["{query}对比分析","{query}的差异","比较{query}","分析{query}"]}', '对比分析类查询扩展模板', 
 'ACTIVE', 95, @current_timestamp, @current_timestamp, @creator, @creator),

-- 技术实现类模板
('expansion_templates', 'default', 'default', 'tech', 
 '技术实现模板', '{"templates":["实现{query}","开发{query}","{query}技术方案","{query}架构设计"]}', '技术实现类查询扩展模板', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

-- 配置管理类模板
('expansion_templates', 'default', 'default', 'tech', 
 '配置管理模板', '{"templates":["配置{query}","{query}参数设置","{query}环境配置","{query}系统设置"]}', '配置管理类查询扩展模板', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

-- 故障诊断类模板
('expansion_templates', 'default', 'default', 'tech', 
 '故障诊断模板', '{"templates":["诊断{query}","{query}故障分析","{query}问题排查","调试{query}"]}', '故障诊断类查询扩展模板', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

-- 业务流程类模板
('expansion_templates', 'default', 'default', 'ecommerce', 
 '业务流程模板', '{"templates":["{query}流程","{query}业务逻辑","{query}处理流程","{query}操作流程"]}', '业务流程类查询扩展模板', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

-- 数据分析类模板
('expansion_templates', 'default', 'default', 'ecommerce', 
 '数据分析模板', '{"templates":["{query}数据分析","{query}统计报告","{query}业务指标","分析{query}数据"]}', '数据分析类查询扩展模板', 
 'ACTIVE', 95, @current_timestamp, @current_timestamp, @creator, @creator),

-- 服务支持类模板
('expansion_templates', 'default', 'default', 'customer_service', 
 '服务支持模板', '{"templates":["{query}服务支持","{query}客户服务","处理{query}","解决{query}"]}', '服务支持类查询扩展模板', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator);

-- ================================================
-- 3. 查询增强规则字典 (query_enhancement_rules)
-- 用于ExpansionStrategyStage：查询增强规则
-- ================================================
INSERT IGNORE INTO `t_dictionary_entry` (
    `dictionary_type`, `tenant`, `channel`, `domain`, 
    `entry_key`, `entry_value`, `description`, 
    `status`, `priority`, `created_at`, `updated_at`, 
    `created_by`, `updated_by`
) VALUES 

-- 上下文增强规则
('query_enhancement_rules', 'default', 'default', 'default', 
 '上下文增强', '{"rules":[{"type":"tenant_context","pattern":".*","enhancement":"在{tenant}环境中{query}"},{"type":"channel_context","pattern":".*","enhancement":"通过{channel}渠道{query}"},{"type":"domain_context","pattern":".*","enhancement":"在{domain}领域{query}"}]}', '基于上下文的查询增强规则', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

-- 意图增强规则
('query_enhancement_rules', 'default', 'default', 'default', 
 '意图增强', '{"rules":[{"intent":"troubleshooting","enhancement":"{query} 故障排除 问题解决"},{"intent":"product_info","enhancement":"{query} 产品信息 功能特性"},{"intent":"installation","enhancement":"{query} 安装配置 部署设置"},{"intent":"consultation","enhancement":"{query} 咨询建议 专业指导"}]}', '基于意图的查询增强规则', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

-- 复杂查询增强规则
('query_enhancement_rules', 'default', 'default', 'default', 
 '复杂查询增强', '{"rules":[{"complexity":"high","pattern":".*和.*|.*或.*|.*对比.*","enhancement":"分解为多个子查询：{query}"},{"complexity":"medium","pattern":".*如何.*","enhancement":"提供方法指导：{query}"},{"complexity":"low","pattern":".*是什么.*","enhancement":"提供定义说明：{query}"}]}', '基于复杂度的查询增强规则', 
 'ACTIVE', 95, @current_timestamp, @current_timestamp, @creator, @creator),

-- 技术查询增强规则
('query_enhancement_rules', 'default', 'default', 'tech', 
 '技术查询增强', '{"rules":[{"type":"api_query","pattern":".*API.*","enhancement":"{query} 接口文档 使用示例"},{"type":"db_query","pattern":".*数据库.*","enhancement":"{query} 数据库操作 SQL语句"},{"type":"framework_query","pattern":".*框架.*","enhancement":"{query} 框架使用 最佳实践"}]}', '技术领域查询增强规则', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

-- 电商查询增强规则
('query_enhancement_rules', 'default', 'default', 'ecommerce', 
 '电商查询增强', '{"rules":[{"type":"order_query","pattern":".*订单.*","enhancement":"{query} 订单管理 业务流程"},{"type":"product_query","pattern":".*商品.*","enhancement":"{query} 商品管理 库存信息"},{"type":"payment_query","pattern":".*支付.*","enhancement":"{query} 支付处理 交易安全"}]}', '电商领域查询增强规则', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

-- 客服查询增强规则
('query_enhancement_rules', 'default', 'default', 'customer_service', 
 '客服查询增强', '{"rules":[{"type":"complaint_query","pattern":".*投诉.*","enhancement":"{query} 投诉处理 客户满意"},{"type":"consultation_query","pattern":".*咨询.*","enhancement":"{query} 专业解答 服务指导"},{"type":"aftersale_query","pattern":".*售后.*","enhancement":"{query} 售后服务 维护支持"}]}', '客服领域查询增强规则', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator);

-- ================================================
-- 4. 多路查询模式字典 (multi_query_patterns)
-- 用于ExpansionStrategyStage：多路查询生成模式
-- ================================================
INSERT IGNORE INTO `t_dictionary_entry` (
    `dictionary_type`, `tenant`, `channel`, `domain`, 
    `entry_key`, `entry_value`, `description`, 
    `status`, `priority`, `created_at`, `updated_at`, 
    `created_by`, `updated_by`
) VALUES 

-- 同义词替换模式
('multi_query_patterns', 'default', 'default', 'default', 
 '同义词替换', '{"patterns":[{"original":"如何","alternatives":["怎么","怎样","如何才能"]},{"original":"什么","alternatives":["哪些","什么样的","何种"]},{"original":"为什么","alternatives":["为何","什么原因","怎么回事"]},{"original":"解决","alternatives":["处理","解决","修复","排除"]}]}', '同义词替换生成多路查询', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

-- 表达方式变换模式
('multi_query_patterns', 'default', 'default', 'default', 
 '表达方式变换', '{"patterns":[{"pattern":"如何{action}","alternatives":["我想{action}","需要{action}","{action}的方法"]},{"pattern":"什么是{concept}","alternatives":["{concept}的定义","关于{concept}","解释{concept}"]},{"pattern":"为什么{reason}","alternatives":["{reason}的原因","导致{reason}的因素","{reason}是怎么回事"]}]}', '表达方式变换生成多路查询', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

-- 疑问词转换模式
('multi_query_patterns', 'default', 'default', 'default', 
 '疑问词转换', '{"patterns":[{"question_word":"如何","conversions":["怎么","怎样","什么方法","哪种方式"]},{"question_word":"什么","conversions":["哪个","哪些","何种","什么样"]},{"question_word":"为什么","conversions":["为何","什么原因","如何解释","怎么解释"]}]}', '疑问词转换生成多路查询', 
 'ACTIVE', 95, @current_timestamp, @current_timestamp, @creator, @creator),

-- 技术术语变换模式
('multi_query_patterns', 'default', 'default', 'tech', 
 '技术术语变换', '{"patterns":[{"term":"API","alternatives":["接口","应用程序接口","编程接口"]},{"term":"数据库","alternatives":["DB","database","数据存储"]},{"term":"框架","alternatives":["framework","开发框架","技术框架"]},{"term":"部署","alternatives":["deploy","发布","上线"]}]}', '技术术语变换生成多路查询', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

-- 业务术语变换模式
('multi_query_patterns', 'default', 'default', 'ecommerce', 
 '业务术语变换', '{"patterns":[{"term":"订单","alternatives":["order","购买记录","交易记录"]},{"term":"商品","alternatives":["产品","product","货品","商品信息"]},{"term":"库存","alternatives":["存货","inventory","stock","货存"]},{"term":"支付","alternatives":["付款","payment","结算","交易"]}]}', '业务术语变换生成多路查询', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator);

-- ================================================
-- 5. HyDE策略配置字典 (hyde_strategy_config)
-- 用于ExpansionStrategyStage：HyDE策略配置
-- ================================================
INSERT IGNORE INTO `t_dictionary_entry` (
    `dictionary_type`, `tenant`, `channel`, `domain`, 
    `entry_key`, `entry_value`, `description`, 
    `status`, `priority`, `created_at`, `updated_at`, 
    `created_by`, `updated_by`
) VALUES 

-- 抽象查询识别配置
('hyde_strategy_config', 'default', 'default', 'default', 
 '抽象查询关键词', '["原理","概念","理论","思想","方法","策略","模式","框架","机制","本质","规律","特点"]', '用于识别抽象查询的关键词列表', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

-- HyDE提示模板配置
('hyde_strategy_config', 'default', 'default', 'default', 
 'HyDE提示模板', '{"base_template":"请基于查询\'{query}\'生成一个详细、准确的假设性答案。答案应包含相关技术细节、关键信息和专业术语，结构清晰，长度适中（100-300字）。","domain_templates":{"tech":"请基于技术查询\'{query}\'生成一个专业的技术答案，包含实现细节、最佳实践和相关工具。","ecommerce":"请基于业务查询\'{query}\'生成一个详细的业务解答，包含流程步骤、关键要点和注意事项。","customer_service":"请基于服务查询\'{query}\'生成一个专业的服务解答，包含解决方案、操作指导和相关政策。"}}', 'HyDE策略提示模板配置', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

-- HyDE答案处理配置
('hyde_strategy_config', 'default', 'default', 'default', 
 '答案处理配置', '{"min_sentence_length":10,"max_sentence_length":100,"max_hyde_queries":3,"sentence_separators":["。","！","？",".","!","?"],"filter_keywords":["请","谢谢","如果","但是","不过","当然"]}', 'HyDE答案后处理配置参数', 
 'ACTIVE', 95, @current_timestamp, @current_timestamp, @creator, @creator);

-- ================================================
-- 6. Step-back策略配置字典 (stepback_strategy_config)
-- 用于ExpansionStrategyStage：Step-back策略配置
-- ================================================
INSERT IGNORE INTO `t_dictionary_entry` (
    `dictionary_type`, `tenant`, `channel`, `domain`, 
    `entry_key`, `entry_value`, `description`, 
    `status`, `priority`, `created_at`, `updated_at`, 
    `created_by`, `updated_by`
) VALUES 

-- 复杂查询识别配置
('stepback_strategy_config', 'default', 'default', 'default', 
 '复杂查询模式', '{"patterns":[".*和.*",".*或者.*",".*以及.*",".*同时.*",".*另外.*",".*对比.*",".*比较.*",".*分析.*",".*评估.*",".*总结.*",".*汇总.*"],"min_components":2,"min_length":50}', '用于识别复杂查询的模式配置', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

-- 主题抽象模式配置
('stepback_strategy_config', 'default', 'default', 'default', 
 '主题抽象模式', '{"如何.*":"方法和步骤","什么是.*":"概念和定义","为什么.*":"原因和机制",".*故障.*":"故障排除",".*对比.*|.*比较.*":"对比分析",".*配置.*|.*设置.*":"配置管理",".*性能.*":"性能优化",".*安全.*":"安全防护"}', '查询主题抽象模式映射', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

-- Step-back提示配置
('stepback_strategy_config', 'default', 'default', 'default', 
 'Step-back提示模板', '{"topic_extraction":"请分析查询\'{query}\'，提取其背后的主题或核心概念。用一个简洁的短语回答：","specific_generation":"基于主题\'{topic}\'和原始问题\'{query}\'，生成2-3个具体的检索查询，每行一个：","max_specific_queries":3}', 'Step-back策略提示模板配置', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator);

-- ================================================
-- 执行统计信息更新
-- ================================================
-- 调用存储过程更新统计信息
CALL sp_refresh_dictionary_stats();

-- 输出执行结果
SELECT CONCAT('M3扩展策略字典数据初始化完成 - 时间: ', FROM_UNIXTIME(@current_timestamp/1000)) AS result;

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
        'expansion_strategies',
        'expansion_templates',
        'query_enhancement_rules',
        'multi_query_patterns',
        'hyde_strategy_config',
        'stepback_strategy_config'
    )
GROUP BY dictionary_type, domain
ORDER BY dictionary_type, domain;