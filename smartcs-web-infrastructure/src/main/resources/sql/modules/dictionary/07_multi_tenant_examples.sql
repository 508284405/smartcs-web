-- ================================================
-- 多租户多领域示例数据SQL脚本
-- ================================================
-- 用途：为多租户SaaS架构提供不同租户、渠道、领域的字典示例数据
-- 包含：不同行业租户的专业术语、业务流程、查询模式等
-- 执行顺序：在06_expansion_strategy_data.sql之后执行
-- ================================================

-- 设置SQL执行参数
SET @current_timestamp = UNIX_TIMESTAMP(NOW()) * 1000;
SET @creator = 'system_multi_tenant';

-- ================================================
-- 租户1：汽车行业 (automotive)
-- ================================================

-- 汽车行业标准化规则
INSERT IGNORE INTO `t_dictionary_entry` (
    `dictionary_type`, `tenant`, `channel`, `domain`, 
    `entry_key`, `entry_value`, `description`, 
    `status`, `priority`, `created_at`, `updated_at`, 
    `created_by`, `updated_by`
) VALUES 

-- 汽车行业标准化规则
('normalization_rules', 'automotive', 'web', 'manufacturing', 
 '国VI', '"国六"', '国VI→国六 汽车排放标准规范化', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('normalization_rules', 'automotive', 'web', 'manufacturing', 
 'NEV', '"新能源汽车"', 'NEV→新能源汽车 术语规范化', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('normalization_rules', 'automotive', 'mobile', 'sales', 
 'OEM', '"主机厂"', 'OEM→主机厂 汽车行业术语', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

-- 汽车行业语义对齐
('semantic_alignment_rules', 'automotive', 'web', 'manufacturing', 
 'BEV', '"纯电动汽车"', 'BEV→纯电动汽车 新能源分类', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('semantic_alignment_rules', 'automotive', 'web', 'manufacturing', 
 'PHEV', '"插电式混合动力汽车"', 'PHEV→插电式混合动力汽车', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('semantic_alignment_rules', 'automotive', 'mobile', 'aftersales', 
 'TSB', '"技术服务公告"', 'TSB→技术服务公告 售后术语', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

-- 汽车行业意图目录
('intent_catalog', 'automotive', 'web', 'manufacturing', 
 'QUALITY_CONTROL', '{"code":"QUALITY_CONTROL","name":"质量控制","description":"生产质量管控相关意图","keywords":["质检","QC","质量","缺陷","不良率"],"examples":["质量检验流程","缺陷分析报告","生产质量监控"]}', '质量控制意图目录', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('intent_catalog', 'automotive', 'mobile', 'sales', 
 'VEHICLE_CONFIG', '{"code":"VEHICLE_CONFIG","name":"车辆配置","description":"车辆配置查询相关意图","keywords":["配置","选装","车型","参数"],"examples":["查询车辆配置","对比车型参数","选装包信息"]}', '车辆配置意图目录', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

-- 汽车行业扩展策略
('expansion_strategies', 'automotive', 'web', 'manufacturing', 
 '质量问题', '"{query}", "{query} 原因分析", "{query} 改进措施", "{query} 预防方案"', '质量问题类查询扩展策略', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('expansion_strategies', 'automotive', 'mobile', 'sales', 
 '车型对比', '"{query}", "{query} 参数对比", "{query} 价格差异", "{query} 性能分析"', '车型对比类查询扩展策略', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator);

-- ================================================
-- 租户2：电商平台 (ecommerce_platform)
-- ================================================

INSERT IGNORE INTO `t_dictionary_entry` (
    `dictionary_type`, `tenant`, `channel`, `domain`, 
    `entry_key`, `entry_value`, `description`, 
    `status`, `priority`, `created_at`, `updated_at`, 
    `created_by`, `updated_by`
) VALUES 

-- 电商平台标准化规则
('normalization_rules', 'ecommerce_platform', 'web', 'operation', 
 'ROI', '"投资回报率"', 'ROI→投资回报率 运营指标术语', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('normalization_rules', 'ecommerce_platform', 'app', 'marketing', 
 'CTR', '"点击率"', 'CTR→点击率 营销指标术语', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('normalization_rules', 'ecommerce_platform', 'mini_program', 'customer_service', 
 'CSAT', '"客户满意度"', 'CSAT→客户满意度 客服指标', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

-- 电商平台语义对齐
('semantic_alignment_rules', 'ecommerce_platform', 'web', 'operation', 
 'UV', '"独立访客数"', 'UV→独立访客数 流量指标', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('semantic_alignment_rules', 'ecommerce_platform', 'app', 'marketing', 
 'LTV', '"客户生命周期价值"', 'LTV→客户生命周期价值', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

-- 电商平台意图目录
('intent_catalog', 'ecommerce_platform', 'web', 'operation', 
 'DATA_ANALYSIS', '{"code":"DATA_ANALYSIS","name":"数据分析","description":"业务数据分析相关意图","keywords":["分析","统计","报表","趋势","指标"],"examples":["销售数据分析","用户行为统计","转化率报告"]}', '数据分析意图目录', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('intent_catalog', 'ecommerce_platform', 'app', 'marketing', 
 'CAMPAIGN_MANAGEMENT', '{"code":"CAMPAIGN_MANAGEMENT","name":"活动管理","description":"营销活动管理相关意图","keywords":["活动","促销","投放","效果","预算"],"examples":["创建促销活动","投放效果分析","活动预算管理"]}', '活动管理意图目录', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

-- 电商平台扩展策略
('expansion_strategies', 'ecommerce_platform', 'web', 'operation', 
 '转化率', '"{query}", "{query} 优化", "{query} 分析", "{query} 提升策略"', '转化率类查询扩展策略', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('expansion_strategies', 'ecommerce_platform', 'app', 'marketing', 
 '广告投放', '"{query}", "{query} 策略", "{query} 优化", "{query} 效果评估"', '广告投放类查询扩展策略', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator);

-- ================================================
-- 租户3：金融服务 (financial_services)
-- ================================================

INSERT IGNORE INTO `t_dictionary_entry` (
    `dictionary_type`, `tenant`, `channel`, `domain`, 
    `entry_key`, `entry_value`, `description`, 
    `status`, `priority`, `created_at`, `updated_at`, 
    `created_by`, `updated_by`
) VALUES 

-- 金融服务标准化规则
('normalization_rules', 'financial_services', 'web', 'banking', 
 'KYC', '"客户身份识别"', 'KYC→客户身份识别 合规术语', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('normalization_rules', 'financial_services', 'app', 'investment', 
 'AUM', '"资产管理规模"', 'AUM→资产管理规模 投资术语', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('normalization_rules', 'financial_services', 'web', 'risk_management', 
 'VaR', '"风险价值"', 'VaR→风险价值 风控术语', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

-- 金融服务语义对齐
('semantic_alignment_rules', 'financial_services', 'web', 'banking', 
 'NPL', '"不良贷款"', 'NPL→不良贷款 银行业务术语', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('semantic_alignment_rules', 'financial_services', 'app', 'investment', 
 'PE', '"市盈率"', 'PE→市盈率 投资分析指标', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

-- 金融服务意图目录
('intent_catalog', 'financial_services', 'web', 'banking', 
 'COMPLIANCE_CHECK', '{"code":"COMPLIANCE_CHECK","name":"合规检查","description":"合规性检查相关意图","keywords":["合规","监管","审核","风控","检查"],"examples":["反洗钱检查","合规审核流程","监管报告生成"]}', '合规检查意图目录', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('intent_catalog', 'financial_services', 'app', 'investment', 
 'PORTFOLIO_ANALYSIS', '{"code":"PORTFOLIO_ANALYSIS","name":"投资组合分析","description":"投资组合分析相关意图","keywords":["组合","分析","配置","收益","风险"],"examples":["投资组合优化","风险收益分析","资产配置建议"]}', '投资组合分析意图目录', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

-- 金融服务扩展策略
('expansion_strategies', 'financial_services', 'web', 'banking', 
 '风险控制', '"{query}", "{query} 机制", "{query} 措施", "{query} 评估"', '风险控制类查询扩展策略', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('expansion_strategies', 'financial_services', 'app', 'investment', 
 '投资策略', '"{query}", "{query} 分析", "{query} 建议", "{query} 风险评估"', '投资策略类查询扩展策略', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator);

-- ================================================
-- 租户4：医疗健康 (healthcare)
-- ================================================

INSERT IGNORE INTO `t_dictionary_entry` (
    `dictionary_type`, `tenant`, `channel`, `domain`, 
    `entry_key`, `entry_value`, `description`, 
    `status`, `priority`, `created_at`, `updated_at`, 
    `created_by`, `updated_by`
) VALUES 

-- 医疗健康标准化规则
('normalization_rules', 'healthcare', 'web', 'clinical', 
 'EMR', '"电子病历"', 'EMR→电子病历 医疗信息系统术语', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('normalization_rules', 'healthcare', 'app', 'pharmacy', 
 'OTC', '"非处方药"', 'OTC→非处方药 药品分类术语', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('normalization_rules', 'healthcare', 'web', 'telemedicine', 
 'HIS', '"医院信息系统"', 'HIS→医院信息系统 医疗IT术语', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

-- 医疗健康语义对齐
('semantic_alignment_rules', 'healthcare', 'web', 'clinical', 
 'ICU', '"重症监护病房"', 'ICU→重症监护病房 科室术语', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('semantic_alignment_rules', 'healthcare', 'app', 'pharmacy', 
 'GMP', '"药品生产质量管理规范"', 'GMP→药品生产质量管理规范', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

-- 医疗健康意图目录
('intent_catalog', 'healthcare', 'web', 'clinical', 
 'DIAGNOSIS_SUPPORT', '{"code":"DIAGNOSIS_SUPPORT","name":"诊断支持","description":"临床诊断辅助相关意图","keywords":["诊断","症状","检查","治疗","病历"],"examples":["疾病诊断建议","检查项目推荐","治疗方案制定"]}', '诊断支持意图目录', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('intent_catalog', 'healthcare', 'app', 'pharmacy', 
 'MEDICATION_MANAGEMENT', '{"code":"MEDICATION_MANAGEMENT","name":"药物管理","description":"药物管理相关意图","keywords":["药物","处方","剂量","禁忌","相互作用"],"examples":["药物配伍检查","剂量调整建议","药物不良反应监测"]}', '药物管理意图目录', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

-- 医疗健康扩展策略
('expansion_strategies', 'healthcare', 'web', 'clinical', 
 '临床指南', '"{query}", "{query} 标准", "{query} 规范", "{query} 最佳实践"', '临床指南类查询扩展策略', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('expansion_strategies', 'healthcare', 'app', 'pharmacy', 
 '药物相互作用', '"{query}", "{query} 机制", "{query} 预防", "{query} 处理"', '药物相互作用类查询扩展策略', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator);

-- ================================================
-- 租户5：教育培训 (education)
-- ================================================

INSERT IGNORE INTO `t_dictionary_entry` (
    `dictionary_type`, `tenant`, `channel`, `domain`, 
    `entry_key`, `entry_value`, `description`, 
    `status`, `priority`, `created_at`, `updated_at`, 
    `created_by`, `updated_by`
) VALUES 

-- 教育培训标准化规则
('normalization_rules', 'education', 'web', 'k12', 
 'STEM', '"科学技术工程数学"', 'STEM→科学技术工程数学 教育术语', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('normalization_rules', 'education', 'app', 'online_learning', 
 'LMS', '"学习管理系统"', 'LMS→学习管理系统 在线教育术语', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('normalization_rules', 'education', 'web', 'vocational', 
 '1+X证书', '"学历证书+职业技能等级证书"', '1+X证书→学历证书+职业技能等级证书', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

-- 教育培训语义对齐
('semantic_alignment_rules', 'education', 'web', 'k12', 
 'PBL', '"项目式学习"', 'PBL→项目式学习 教学方法', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('semantic_alignment_rules', 'education', 'app', 'online_learning', 
 'MOOC', '"大规模开放在线课程"', 'MOOC→大规模开放在线课程', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

-- 教育培训意图目录
('intent_catalog', 'education', 'web', 'k12', 
 'CURRICULUM_DESIGN', '{"code":"CURRICULUM_DESIGN","name":"课程设计","description":"课程设计相关意图","keywords":["课程","教学","设计","目标","评估"],"examples":["课程目标制定","教学活动设计","学习评估方案"]}', '课程设计意图目录', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('intent_catalog', 'education', 'app', 'online_learning', 
 'LEARNING_ANALYTICS', '{"code":"LEARNING_ANALYTICS","name":"学习分析","description":"学习数据分析相关意图","keywords":["学习","分析","数据","行为","效果"],"examples":["学习行为分析","学习效果评估","个性化推荐"]}', '学习分析意图目录', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

-- 教育培训扩展策略
('expansion_strategies', 'education', 'web', 'k12', 
 '教学方法', '"{query}", "{query} 应用", "{query} 实践", "{query} 效果"', '教学方法类查询扩展策略', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('expansion_strategies', 'education', 'app', 'online_learning', 
 '个性化学习', '"{query}", "{query} 算法", "{query} 策略", "{query} 实施"', '个性化学习类查询扩展策略', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator);

-- ================================================
-- 跨租户通用配置示例
-- ================================================

INSERT IGNORE INTO `t_dictionary_entry` (
    `dictionary_type`, `tenant`, `channel`, `domain`, 
    `entry_key`, `entry_value`, `description`, 
    `status`, `priority`, `created_at`, `updated_at`, 
    `created_by`, `updated_by`
) VALUES 

-- 多语言支持示例（中英文）
('normalization_rules', 'default', 'default', 'default', 
 'AI', '"人工智能"', 'AI→人工智能 多语言术语标准化', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('normalization_rules', 'automotive', 'web', 'manufacturing', 
 'AI', '"智能制造"', 'AI→智能制造 汽车行业特定含义', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('normalization_rules', 'healthcare', 'web', 'clinical', 
 'AI', '"辅助诊断"', 'AI→辅助诊断 医疗行业特定含义', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

-- 渠道特定配置示例
('semantic_keywords', 'default', 'mobile', 'default', 
 '移动端特定', '{"touch":"触摸","swipe":"滑动","tap":"点击","pinch":"缩放","gesture":"手势"}', '移动端交互术语', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

('semantic_keywords', 'default', 'mini_program', 'default', 
 '小程序特定', '{"onLoad":"页面加载","onShow":"页面显示","onHide":"页面隐藏","onUnload":"页面卸载"}', '小程序生命周期术语', 
 'ACTIVE', 100, @current_timestamp, @current_timestamp, @creator, @creator),

-- 区域本地化示例
('normalization_rules', 'default', 'default', 'default', 
 '色彩', '"颜色"', '色彩→颜色 术语本地化', 
 'ACTIVE', 90, @current_timestamp, @current_timestamp, @creator, @creator),

('normalization_rules', 'default', 'default', 'default', 
 '網路', '"网络"', '網路→网络 繁简转换', 
 'ACTIVE', 90, @current_timestamp, @current_timestamp, @creator, @creator);

-- ================================================
-- 执行统计信息更新
-- ================================================
-- 调用存储过程更新统计信息
CALL sp_refresh_dictionary_stats();

-- 输出执行结果
SELECT CONCAT('多租户多领域示例数据初始化完成 - 时间: ', FROM_UNIXTIME(@current_timestamp/1000)) AS result;

-- 显示多租户数据统计
SELECT 
    tenant,
    channel,
    domain,
    dictionary_type,
    COUNT(*) as entry_count,
    COUNT(CASE WHEN status = 'ACTIVE' THEN 1 END) as active_count
FROM t_dictionary_entry 
WHERE tenant != 'default' OR (tenant = 'default' AND channel != 'default') 
GROUP BY tenant, channel, domain, dictionary_type
ORDER BY tenant, channel, domain, dictionary_type;

-- 显示各租户覆盖的字典类型统计
SELECT 
    tenant,
    COUNT(DISTINCT dictionary_type) as type_count,
    GROUP_CONCAT(DISTINCT dictionary_type ORDER BY dictionary_type) as covered_types
FROM t_dictionary_entry 
WHERE tenant != 'default'
GROUP BY tenant
ORDER BY tenant;

-- 显示各渠道覆盖的字典类型统计
SELECT 
    channel,
    COUNT(DISTINCT dictionary_type) as type_count,
    COUNT(DISTINCT domain) as domain_count,
    COUNT(*) as total_entries
FROM t_dictionary_entry 
WHERE channel != 'default'
GROUP BY channel
ORDER BY channel;