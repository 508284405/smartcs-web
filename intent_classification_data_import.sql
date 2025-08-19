-- =====================================================
-- SmartCS-Web 意图分类数据导入脚本
-- 包含：农业、畜牧业、电商、客服系统的完整意图分类体系
-- 创建时间：2025-08-17
-- =====================================================

SET SESSION sql_mode = '';

-- 设置时间戳变量
SET @current_time = UNIX_TIMESTAMP() * 1000;
SET @creator_id = 1; -- 系统管理员ID
SET @creator_name = 'system';

-- =====================================================
-- 1. 清理现有数据（可选，仅在需要重新导入时使用）
-- =====================================================
-- DELETE FROM t_intent_sample WHERE 1=1;
-- DELETE FROM t_intent WHERE 1=1;
-- DELETE FROM t_intent_catalog WHERE 1=1;

-- =====================================================
-- 2. 导入意图目录结构
-- =====================================================

-- 2.1 农业领域根目录
INSERT INTO t_intent_catalog (name, code, description, parent_id, sort_order, creator_id, created_by, updated_by, created_at, updated_at) 
VALUES ('农业领域', 'AGRICULTURE', '农业相关的意图分类，包括种植管理、技术咨询、市场信息等', NULL, 1, @creator_id, @creator_name, @creator_name, @current_time, @current_time);
SET @agriculture_id = LAST_INSERT_ID();

-- 2.2 畜牧业领域根目录
INSERT INTO t_intent_catalog (name, code, description, parent_id, sort_order, creator_id, created_by, updated_by, created_at, updated_at) 
VALUES ('畜牧业领域', 'LIVESTOCK', '畜牧业相关的意图分类，包括养殖管理、疾病防控、生产经营等', NULL, 2, @creator_id, @creator_name, @creator_name, @current_time, @current_time);
SET @livestock_id = LAST_INSERT_ID();

-- 2.3 电商领域根目录
INSERT INTO t_intent_catalog (name, code, description, parent_id, sort_order, creator_id, created_by, updated_by, created_at, updated_at) 
VALUES ('电商领域', 'ECOMMERCE', '电商相关的意图分类，包括商品咨询、购买流程、售后服务等', NULL, 3, @creator_id, @creator_name, @creator_name, @current_time, @current_time);
SET @ecommerce_id = LAST_INSERT_ID();

-- 2.4 客服系统根目录
INSERT INTO t_intent_catalog (name, code, description, parent_id, sort_order, creator_id, created_by, updated_by, created_at, updated_at) 
VALUES ('客服系统', 'CUSTOMER_SERVICE', '客服系统通用意图分类，包括信息查询、业务办理、技术支持等', NULL, 4, @creator_id, @creator_name, @creator_name, @current_time, @current_time);
SET @customer_service_id = LAST_INSERT_ID();

-- =====================================================
-- 3. 农业领域子目录
-- =====================================================

-- 3.1 种植管理
INSERT INTO t_intent_catalog (name, code, description, parent_id, sort_order, creator_id, created_by, updated_by, created_at, updated_at) 
VALUES ('种植管理', 'AGRICULTURE_PLANTING', '农作物种植相关的管理指导', @agriculture_id, 1, @creator_id, @creator_name, @creator_name, @current_time, @current_time);
SET @agri_planting_id = LAST_INSERT_ID();

-- 3.2 技术咨询
INSERT INTO t_intent_catalog (name, code, description, parent_id, sort_order, creator_id, created_by, updated_by, created_at, updated_at) 
VALUES ('技术咨询', 'AGRICULTURE_TECH', '农业技术相关的咨询服务', @agriculture_id, 2, @creator_id, @creator_name, @creator_name, @current_time, @current_time);
SET @agri_tech_id = LAST_INSERT_ID();

-- 3.3 市场信息
INSERT INTO t_intent_catalog (name, code, description, parent_id, sort_order, creator_id, created_by, updated_by, created_at, updated_at) 
VALUES ('市场信息', 'AGRICULTURE_MARKET', '农产品市场信息和政策咨询', @agriculture_id, 3, @creator_id, @creator_name, @creator_name, @current_time, @current_time);
SET @agri_market_id = LAST_INSERT_ID();

-- =====================================================
-- 4. 畜牧业领域子目录
-- =====================================================

-- 4.1 养殖管理
INSERT INTO t_intent_catalog (name, code, description, parent_id, sort_order, creator_id, created_by, updated_by, created_at, updated_at) 
VALUES ('养殖管理', 'LIVESTOCK_BREEDING', '畜禽养殖管理相关指导', @livestock_id, 1, @creator_id, @creator_name, @creator_name, @current_time, @current_time);
SET @livestock_breeding_id = LAST_INSERT_ID();

-- 4.2 疾病防控
INSERT INTO t_intent_catalog (name, code, description, parent_id, sort_order, creator_id, created_by, updated_by, created_at, updated_at) 
VALUES ('疾病防控', 'LIVESTOCK_DISEASE', '畜禽疾病预防和治疗指导', @livestock_id, 2, @creator_id, @creator_name, @creator_name, @current_time, @current_time);
SET @livestock_disease_id = LAST_INSERT_ID();

-- 4.3 生产经营
INSERT INTO t_intent_catalog (name, code, description, parent_id, sort_order, creator_id, created_by, updated_by, created_at, updated_at) 
VALUES ('生产经营', 'LIVESTOCK_BUSINESS', '畜牧业生产经营管理咨询', @livestock_id, 3, @creator_id, @creator_name, @creator_name, @current_time, @current_time);
SET @livestock_business_id = LAST_INSERT_ID();

-- =====================================================
-- 5. 电商领域子目录
-- =====================================================

-- 5.1 商品咨询
INSERT INTO t_intent_catalog (name, code, description, parent_id, sort_order, creator_id, created_by, updated_by, created_at, updated_at) 
VALUES ('商品咨询', 'ECOMMERCE_PRODUCT', '商品信息咨询和选择指导', @ecommerce_id, 1, @creator_id, @creator_name, @creator_name, @current_time, @current_time);
SET @ecom_product_id = LAST_INSERT_ID();

-- 5.2 购买流程
INSERT INTO t_intent_catalog (name, code, description, parent_id, sort_order, creator_id, created_by, updated_by, created_at, updated_at) 
VALUES ('购买流程', 'ECOMMERCE_PURCHASE', '下单支付和配送相关咨询', @ecommerce_id, 2, @creator_id, @creator_name, @creator_name, @current_time, @current_time);
SET @ecom_purchase_id = LAST_INSERT_ID();

-- 5.3 售后服务
INSERT INTO t_intent_catalog (name, code, description, parent_id, sort_order, creator_id, created_by, updated_by, created_at, updated_at) 
VALUES ('售后服务', 'ECOMMERCE_AFTER_SALE', '退换货和质量问题处理', @ecommerce_id, 3, @creator_id, @creator_name, @creator_name, @current_time, @current_time);
SET @ecom_after_sale_id = LAST_INSERT_ID();

-- =====================================================
-- 6. 客服系统子目录
-- =====================================================

-- 6.1 信息查询
INSERT INTO t_intent_catalog (name, code, description, parent_id, sort_order, creator_id, created_by, updated_by, created_at, updated_at) 
VALUES ('信息查询', 'CS_QUERY', '各类状态和信息查询服务', @customer_service_id, 1, @creator_id, @creator_name, @creator_name, @current_time, @current_time);
SET @cs_query_id = LAST_INSERT_ID();

-- 6.2 业务办理
INSERT INTO t_intent_catalog (name, code, description, parent_id, sort_order, creator_id, created_by, updated_by, created_at, updated_at) 
VALUES ('业务办理', 'CS_BUSINESS', '各类业务申请和变更服务', @customer_service_id, 2, @creator_id, @creator_name, @creator_name, @current_time, @current_time);
SET @cs_business_id = LAST_INSERT_ID();

-- 6.3 技术支持
INSERT INTO t_intent_catalog (name, code, description, parent_id, sort_order, creator_id, created_by, updated_by, created_at, updated_at) 
VALUES ('技术支持', 'CS_TECH_SUPPORT', '技术问题诊断和解决方案', @customer_service_id, 3, @creator_id, @creator_name, @creator_name, @current_time, @current_time);
SET @cs_tech_support_id = LAST_INSERT_ID();

-- =====================================================
-- 7. 导入具体意图
-- =====================================================

-- 7.1 农业领域 - 种植管理意图
INSERT INTO t_intent (catalog_id, name, code, description, labels, status, creator_id, created_by, updated_by, created_at, updated_at) VALUES
(@agri_planting_id, '种子选择', 'AGRI_SEED_SELECT', '农作物种子品种选择和推荐', '["种子", "品种", "选择", "推荐"]', 'ACTIVE', @creator_id, @creator_name, @creator_name, @current_time, @current_time),
(@agri_planting_id, '播种指导', 'AGRI_SOWING_GUIDE', '播种时间、深度、密度等技术指导', '["播种", "时间", "深度", "密度"]', 'ACTIVE', @creator_id, @creator_name, @creator_name, @current_time, @current_time),
(@agri_planting_id, '田间管理', 'AGRI_FIELD_MANAGE', '田间管理技术和操作指导', '["田间管理", "施肥", "除草", "灌溉"]', 'ACTIVE', @creator_id, @creator_name, @creator_name, @current_time, @current_time),
(@agri_planting_id, '病虫害防治', 'AGRI_PEST_CONTROL', '农作物病虫害识别和防治', '["病虫害", "防治", "农药", "诊断"]', 'ACTIVE', @creator_id, @creator_name, @creator_name, @current_time, @current_time),
(@agri_planting_id, '收获储存', 'AGRI_HARVEST_STORE', '收获时机判断和储存技术', '["收获", "储存", "时机", "技术"]', 'ACTIVE', @creator_id, @creator_name, @creator_name, @current_time, @current_time);

-- 7.2 农业领域 - 技术咨询意图
INSERT INTO t_intent (catalog_id, name, code, description, labels, status, creator_id, created_by, updated_by, created_at, updated_at) VALUES
(@agri_tech_id, '土壤管理', 'AGRI_SOIL_MANAGE', '土壤改良和管理技术咨询', '["土壤", "改良", "管理", "检测"]', 'ACTIVE', @creator_id, @creator_name, @creator_name, @current_time, @current_time),
(@agri_tech_id, '灌溉技术', 'AGRI_IRRIGATION', '灌溉系统设计和节水技术', '["灌溉", "节水", "系统", "技术"]', 'ACTIVE', @creator_id, @creator_name, @creator_name, @current_time, @current_time),
(@agri_tech_id, '设备使用', 'AGRI_EQUIPMENT', '农机设备使用和维护指导', '["设备", "农机", "使用", "维护"]', 'ACTIVE', @creator_id, @creator_name, @creator_name, @current_time, @current_time),
(@agri_tech_id, '新技术推广', 'AGRI_NEW_TECH', '智慧农业和新技术推广应用', '["智慧农业", "新技术", "推广", "应用"]', 'ACTIVE', @creator_id, @creator_name, @creator_name, @current_time, @current_time);

-- 7.3 农业领域 - 市场信息意图
INSERT INTO t_intent (catalog_id, name, code, description, labels, status, creator_id, created_by, updated_by, created_at, updated_at) VALUES
(@agri_market_id, '价格查询', 'AGRI_PRICE_QUERY', '农产品市场价格查询和趋势分析', '["价格", "查询", "市场", "趋势"]', 'ACTIVE', @creator_id, @creator_name, @creator_name, @current_time, @current_time),
(@agri_market_id, '销售渠道', 'AGRI_SALES_CHANNEL', '农产品销售渠道和平台推荐', '["销售", "渠道", "平台", "推荐"]', 'ACTIVE', @creator_id, @creator_name, @creator_name, @current_time, @current_time),
(@agri_market_id, '政策补贴', 'AGRI_POLICY_SUBSIDY', '农业政策解读和补贴申请指导', '["政策", "补贴", "申请", "指导"]', 'ACTIVE', @creator_id, @creator_name, @creator_name, @current_time, @current_time);

-- 7.4 畜牧业领域 - 养殖管理意图
INSERT INTO t_intent (catalog_id, name, code, description, labels, status, creator_id, created_by, updated_by, created_at, updated_at) VALUES
(@livestock_breeding_id, '品种选择', 'LIVE_BREED_SELECT', '畜禽品种选择和特点对比', '["品种", "选择", "对比", "特点"]', 'ACTIVE', @creator_id, @creator_name, @creator_name, @current_time, @current_time),
(@livestock_breeding_id, '饲养技术', 'LIVE_FEEDING_TECH', '饲养管理技术和营养配方', '["饲养", "技术", "营养", "配方"]', 'ACTIVE', @creator_id, @creator_name, @creator_name, @current_time, @current_time),
(@livestock_breeding_id, '环境控制', 'LIVE_ENV_CONTROL', '养殖环境控制和设施管理', '["环境", "控制", "设施", "管理"]', 'ACTIVE', @creator_id, @creator_name, @creator_name, @current_time, @current_time),
(@livestock_breeding_id, '繁殖育种', 'LIVE_REPRODUCTION', '畜禽繁殖和育种技术指导', '["繁殖", "育种", "技术", "指导"]', 'ACTIVE', @creator_id, @creator_name, @creator_name, @current_time, @current_time);

-- 7.5 畜牧业领域 - 疾病防控意图
INSERT INTO t_intent (catalog_id, name, code, description, labels, status, creator_id, created_by, updated_by, created_at, updated_at) VALUES
(@livestock_disease_id, '疫病预防', 'LIVE_DISEASE_PREVENT', '畜禽疫病预防和免疫程序', '["疫病", "预防", "免疫", "程序"]', 'ACTIVE', @creator_id, @creator_name, @creator_name, @current_time, @current_time),
(@livestock_disease_id, '疾病诊断', 'LIVE_DISEASE_DIAGNOSE', '畜禽疾病症状诊断和识别', '["疾病", "诊断", "症状", "识别"]', 'ACTIVE', @creator_id, @creator_name, @creator_name, @current_time, @current_time),
(@livestock_disease_id, '治疗方案', 'LIVE_TREATMENT', '疾病治疗方案和用药指导', '["治疗", "方案", "用药", "指导"]', 'ACTIVE', @creator_id, @creator_name, @creator_name, @current_time, @current_time),
(@livestock_disease_id, '兽药使用', 'LIVE_MEDICINE_USE', '兽药使用规范和注意事项', '["兽药", "使用", "规范", "注意事项"]', 'ACTIVE', @creator_id, @creator_name, @creator_name, @current_time, @current_time);

-- 7.6 畜牧业领域 - 生产经营意图
INSERT INTO t_intent (catalog_id, name, code, description, labels, status, creator_id, created_by, updated_by, created_at, updated_at) VALUES
(@livestock_business_id, '成本核算', 'LIVE_COST_CALC', '养殖成本计算和控制方法', '["成本", "核算", "计算", "控制"]', 'ACTIVE', @creator_id, @creator_name, @creator_name, @current_time, @current_time),
(@livestock_business_id, '设备投资', 'LIVE_EQUIP_INVEST', '养殖设备选择和投资建议', '["设备", "投资", "选择", "建议"]', 'ACTIVE', @creator_id, @creator_name, @creator_name, @current_time, @current_time),
(@livestock_business_id, '市场行情', 'LIVE_MARKET_INFO', '畜产品市场行情和价格走势', '["市场", "行情", "价格", "走势"]', 'ACTIVE', @creator_id, @creator_name, @creator_name, @current_time, @current_time);

-- 7.7 电商领域 - 商品咨询意图
INSERT INTO t_intent (catalog_id, name, code, description, labels, status, creator_id, created_by, updated_by, created_at, updated_at) VALUES
(@ecom_product_id, '产品信息', 'ECOM_PRODUCT_INFO', '商品基本信息和详细介绍', '["产品", "信息", "介绍", "详情"]', 'ACTIVE', @creator_id, @creator_name, @creator_name, @current_time, @current_time),
(@ecom_product_id, '规格参数', 'ECOM_PRODUCT_SPEC', '商品规格参数和技术指标', '["规格", "参数", "技术", "指标"]', 'ACTIVE', @creator_id, @creator_name, @creator_name, @current_time, @current_time),
(@ecom_product_id, '使用方法', 'ECOM_PRODUCT_USAGE', '商品使用方法和操作指南', '["使用", "方法", "操作", "指南"]', 'ACTIVE', @creator_id, @creator_name, @creator_name, @current_time, @current_time),
(@ecom_product_id, '适用场景', 'ECOM_PRODUCT_SCENE', '商品适用场景和选择建议', '["适用", "场景", "选择", "建议"]', 'ACTIVE', @creator_id, @creator_name, @creator_name, @current_time, @current_time);

-- 7.8 电商领域 - 购买流程意图
INSERT INTO t_intent (catalog_id, name, code, description, labels, status, creator_id, created_by, updated_by, created_at, updated_at) VALUES
(@ecom_purchase_id, '下单支付', 'ECOM_ORDER_PAY', '下单流程和支付方式咨询', '["下单", "支付", "流程", "方式"]', 'ACTIVE', @creator_id, @creator_name, @creator_name, @current_time, @current_time),
(@ecom_purchase_id, '配送物流', 'ECOM_DELIVERY', '配送时间和物流信息查询', '["配送", "物流", "时间", "查询"]', 'ACTIVE', @creator_id, @creator_name, @creator_name, @current_time, @current_time),
(@ecom_purchase_id, '库存查询', 'ECOM_STOCK_QUERY', '商品库存状态和补货信息', '["库存", "查询", "状态", "补货"]', 'ACTIVE', @creator_id, @creator_name, @creator_name, @current_time, @current_time),
(@ecom_purchase_id, '价格优惠', 'ECOM_PRICE_DISCOUNT', '价格优惠和促销活动信息', '["价格", "优惠", "促销", "活动"]', 'ACTIVE', @creator_id, @creator_name, @creator_name, @current_time, @current_time);

-- 7.9 电商领域 - 售后服务意图
INSERT INTO t_intent (catalog_id, name, code, description, labels, status, creator_id, created_by, updated_by, created_at, updated_at) VALUES
(@ecom_after_sale_id, '退换货', 'ECOM_RETURN_EXCHANGE', '退换货政策和处理流程', '["退换货", "政策", "处理", "流程"]', 'ACTIVE', @creator_id, @creator_name, @creator_name, @current_time, @current_time),
(@ecom_after_sale_id, '质量问题', 'ECOM_QUALITY_ISSUE', '商品质量问题处理和保修', '["质量", "问题", "处理", "保修"]', 'ACTIVE', @creator_id, @creator_name, @creator_name, @current_time, @current_time),
(@ecom_after_sale_id, '投诉建议', 'ECOM_COMPLAINT', '客户投诉处理和建议反馈', '["投诉", "建议", "处理", "反馈"]', 'ACTIVE', @creator_id, @creator_name, @creator_name, @current_time, @current_time),
(@ecom_after_sale_id, '账户管理', 'ECOM_ACCOUNT_MANAGE', '账户信息管理和安全设置', '["账户", "管理", "信息", "安全"]', 'ACTIVE', @creator_id, @creator_name, @creator_name, @current_time, @current_time);

-- 7.10 客服系统 - 信息查询意图
INSERT INTO t_intent (catalog_id, name, code, description, labels, status, creator_id, created_by, updated_by, created_at, updated_at) VALUES
(@cs_query_id, '状态查询', 'CS_STATUS_QUERY', '订单状态和账户信息查询', '["状态", "查询", "订单", "账户"]', 'ACTIVE', @creator_id, @creator_name, @creator_name, @current_time, @current_time),
(@cs_query_id, '进度跟踪', 'CS_PROGRESS_TRACK', '申请进度和处理状态跟踪', '["进度", "跟踪", "申请", "处理"]', 'ACTIVE', @creator_id, @creator_name, @creator_name, @current_time, @current_time),
(@cs_query_id, '历史记录', 'CS_HISTORY_RECORD', '消费记录和服务历史查询', '["历史", "记录", "消费", "服务"]', 'ACTIVE', @creator_id, @creator_name, @creator_name, @current_time, @current_time),
(@cs_query_id, '政策说明', 'CS_POLICY_EXPLAIN', '服务条款和政策说明解释', '["政策", "说明", "条款", "解释"]', 'ACTIVE', @creator_id, @creator_name, @creator_name, @current_time, @current_time);

-- 7.11 客服系统 - 业务办理意图
INSERT INTO t_intent (catalog_id, name, code, description, labels, status, creator_id, created_by, updated_by, created_at, updated_at) VALUES
(@cs_business_id, '开通服务', 'CS_SERVICE_OPEN', '新服务开通和业务申请', '["开通", "服务", "业务", "申请"]', 'ACTIVE', @creator_id, @creator_name, @creator_name, @current_time, @current_time),
(@cs_business_id, '变更业务', 'CS_SERVICE_CHANGE', '业务变更和信息修改', '["变更", "业务", "信息", "修改"]', 'ACTIVE', @creator_id, @creator_name, @creator_name, @current_time, @current_time),
(@cs_business_id, '暂停取消', 'CS_SERVICE_SUSPEND', '服务暂停和账户注销', '["暂停", "取消", "服务", "注销"]', 'ACTIVE', @creator_id, @creator_name, @creator_name, @current_time, @current_time),
(@cs_business_id, '续费充值', 'CS_RECHARGE_RENEW', '账户充值和服务续费', '["续费", "充值", "账户", "服务"]', 'ACTIVE', @creator_id, @creator_name, @creator_name, @current_time, @current_time);

-- 7.12 客服系统 - 技术支持意图
INSERT INTO t_intent (catalog_id, name, code, description, labels, status, creator_id, created_by, updated_by, created_at, updated_at) VALUES
(@cs_tech_support_id, '故障报修', 'CS_FAULT_REPAIR', '设备故障报修和问题反馈', '["故障", "报修", "设备", "问题"]', 'ACTIVE', @creator_id, @creator_name, @creator_name, @current_time, @current_time),
(@cs_tech_support_id, '使用指导', 'CS_USAGE_GUIDE', '功能使用教程和操作指导', '["使用", "指导", "教程", "操作"]', 'ACTIVE', @creator_id, @creator_name, @creator_name, @current_time, @current_time),
(@cs_tech_support_id, '问题诊断', 'CS_ISSUE_DIAGNOSE', '技术问题诊断和解决方案', '["问题", "诊断", "技术", "解决"]', 'ACTIVE', @creator_id, @creator_name, @creator_name, @current_time, @current_time),
(@cs_tech_support_id, '优化建议', 'CS_OPTIMIZE_SUGGEST', '系统优化和使用效果提升', '["优化", "建议", "系统", "提升"]', 'ACTIVE', @creator_id, @creator_name, @creator_name, @current_time, @current_time);

-- =====================================================
-- 8. 插入意图样本数据（示例语料）
-- =====================================================

-- 为每个意图添加训练样本
-- 注意：这里只添加部分示例，实际使用时需要根据 t_intent_version 表的 ID 来插入

-- 获取种子选择意图ID
SET @seed_select_intent_id = (SELECT id FROM t_intent WHERE code = 'AGRI_SEED_SELECT');
-- 由于版本表数据需要先创建，这里暂时注释示例样本插入代码
/*
INSERT INTO t_intent_sample (version_id, type, text, source, created_by, updated_by, created_at, updated_at) VALUES
(1, 'TRAIN', '推荐适合这个地区的玉米品种', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(1, 'TRAIN', '什么品种的小麦抗旱性好', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(1, 'TRAIN', '我想种植水稻，选什么品种比较好', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(1, 'TRAIN', '这个地方适合种什么蔬菜品种', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(1, 'TRAIN', '有没有产量高的大豆品种推荐', 'manual', @creator_name, @creator_name, @current_time, @current_time);
*/

-- =====================================================
-- 9. 设置自增ID起始值（可选）
-- =====================================================
-- ALTER TABLE t_intent_catalog AUTO_INCREMENT = 1000;
-- ALTER TABLE t_intent AUTO_INCREMENT = 10000;

-- =====================================================
-- 10. 验证导入结果
-- =====================================================
-- =====================================================
-- 11. 增量完善：新增更细分的意图（不与现有编码重复）
-- =====================================================

-- 11.1 农业：水肥管理/保险理赔/设备与物联
-- 水肥管理-施肥方案（归入“种植管理”子目录）
INSERT INTO t_intent (catalog_id, name, code, description, labels, status, creator_id, created_by, updated_by, created_at, updated_at)
VALUES (@agri_planting_id, '施肥方案', 'AGRI_FERT_PLAN', '生育期与土壤指标驱动的施肥方案', '["施肥", "配方", "比例", "生育期"]', 'ACTIVE', @creator_id, @creator_name, @creator_name, @current_time, @current_time);

-- 市场与政策-农业保险理赔
INSERT INTO t_intent (catalog_id, name, code, description, labels, status, creator_id, created_by, updated_by, created_at, updated_at)
VALUES (@agri_market_id, '农业保险理赔', 'AGRI_INSURANCE_CLAIM', '农业保险报案、理赔材料与流程', '["保险", "理赔", "报案", "材料"]', 'ACTIVE', @creator_id, @creator_name, @creator_name, @current_time, @current_time);

-- 技术咨询-设备故障处理（设备与物联）
INSERT INTO t_intent (catalog_id, name, code, description, labels, status, creator_id, created_by, updated_by, created_at, updated_at)
VALUES (@agri_tech_id, '设备故障处理', 'AGRI_EQUIP_TROUBLE', '农机/灌溉/无人机等设备故障排查', '["设备", "故障", "排查", "维护"]', 'ACTIVE', @creator_id, @creator_name, @creator_name, @current_time, @current_time);

-- 技术咨询-传感器告警处理（设备与物联）
INSERT INTO t_intent (catalog_id, name, code, description, labels, status, creator_id, created_by, updated_by, created_at, updated_at)
VALUES (@agri_tech_id, '传感器告警处理', 'AGRI_SENSOR_ALERT', '温湿/土壤/电导率等传感异常处理', '["传感器", "告警", "土壤", "温湿"]', 'ACTIVE', @creator_id, @creator_name, @creator_name, @current_time, @current_time);

-- 11.2 畜牧业：繁育/饲料/环境与保险
-- 繁育排期
INSERT INTO t_intent (catalog_id, name, code, description, labels, status, creator_id, created_by, updated_by, created_at, updated_at)
VALUES (@livestock_breeding_id, '繁育排期', 'LIVE_BREED_SCH', '母畜配种与育肥周期安排', '["繁育", "配种", "排期", "育肥"]', 'ACTIVE', @creator_id, @creator_name, @creator_name, @current_time, @current_time);

-- 发情识别
INSERT INTO t_intent (catalog_id, name, code, description, labels, status, creator_id, created_by, updated_by, created_at, updated_at)
VALUES (@livestock_breeding_id, '发情识别', 'LIVE_ESTRUS_DET', '发情表现识别与监测手段', '["发情", "识别", "监测", "繁殖"]', 'ACTIVE', @creator_id, @creator_name, @creator_name, @current_time, @current_time);

-- 日粮配方
INSERT INTO t_intent (catalog_id, name, code, description, labels, status, creator_id, created_by, updated_by, created_at, updated_at)
VALUES (@livestock_breeding_id, '日粮配方', 'LIVE_FEED_FORM', '阶段性营养配比与饲喂方案', '["日粮", "配方", "营养", "阶段"]', 'ACTIVE', @creator_id, @creator_name, @creator_name, @current_time, @current_time);

-- 饲料供应采购（归入生产经营）
INSERT INTO t_intent (catalog_id, name, code, description, labels, status, creator_id, created_by, updated_by, created_at, updated_at)
VALUES (@livestock_business_id, '饲料供应采购', 'LIVE_FEED_SUP', '饲料/添加剂采购渠道与比价', '["饲料", "采购", "供应", "渠道"]', 'ACTIVE', @creator_id, @creator_name, @creator_name, @current_time, @current_time);

-- 粪污处理（环境与达标）
INSERT INTO t_intent (catalog_id, name, code, description, labels, status, creator_id, created_by, updated_by, created_at, updated_at)
VALUES (@livestock_breeding_id, '粪污处理', 'LIVE_WASTE_MGMT', '粪污达标排放与资源化利用', '["粪污", "达标", "资源化", "环保"]', 'ACTIVE', @creator_id, @creator_name, @creator_name, @current_time, @current_time);

-- 养殖保险理赔（归入生产经营）
INSERT INTO t_intent (catalog_id, name, code, description, labels, status, creator_id, created_by, updated_by, created_at, updated_at)
VALUES (@livestock_business_id, '养殖保险理赔', 'LIVE_INS_CLAIM', '养殖险报案、理赔资料与周期', '["保险", "理赔", "报案", "周期"]', 'ACTIVE', @creator_id, @creator_name, @creator_name, @current_time, @current_time);

-- 11.3 电商：搜索/订单/退款/地址变更
-- 商品搜索推荐
INSERT INTO t_intent (catalog_id, name, code, description, labels, status, creator_id, created_by, updated_by, created_at, updated_at)
VALUES (@ecom_product_id, '商品搜索推荐', 'ECOM_PROD_SEARCH', '按预算/用途/参数的商品推荐', '["搜索", "推荐", "预算", "参数"]', 'ACTIVE', @creator_id, @creator_name, @creator_name, @current_time, @current_time);

-- 订单状态查询
INSERT INTO t_intent (catalog_id, name, code, description, labels, status, creator_id, created_by, updated_by, created_at, updated_at)
VALUES (@ecom_purchase_id, '订单状态查询', 'ECOM_ORDER_STATUS', '订单进度与预计送达', '["订单", "状态", "进度", "送达"]', 'ACTIVE', @creator_id, @creator_name, @creator_name, @current_time, @current_time);

-- 退款（售后细分）
INSERT INTO t_intent (catalog_id, name, code, description, labels, status, creator_id, created_by, updated_by, created_at, updated_at)
VALUES (@ecom_after_sale_id, '退款', 'ECOM_AFTER_REFUND', '退款规则、流程与到账时间', '["退款", "规则", "流程", "到账"]', 'ACTIVE', @creator_id, @creator_name, @creator_name, @current_time, @current_time);

-- 收货信息变更
INSERT INTO t_intent (catalog_id, name, code, description, labels, status, creator_id, created_by, updated_by, created_at, updated_at)
VALUES (@ecom_purchase_id, '收货信息变更', 'ECOM_ADDR_CHANGE', '地址/电话/自提点变更', '["地址", "变更", "电话", "自提"]', 'ACTIVE', @creator_id, @creator_name, @creator_name, @current_time, @current_time);

-- 11.4 客服系统：工单/账号/升级
-- 创建工单（业务办理）
INSERT INTO t_intent (catalog_id, name, code, description, labels, status, creator_id, created_by, updated_by, created_at, updated_at)
VALUES (@cs_business_id, '创建工单', 'CS_TICKET_CREATE', '系统/产品问题报障与提单', '["工单", "报障", "提单", "问题"]', 'ACTIVE', @creator_id, @creator_name, @creator_name, @current_time, @current_time);

-- 查询工单（信息查询）
INSERT INTO t_intent (catalog_id, name, code, description, labels, status, creator_id, created_by, updated_by, created_at, updated_at)
VALUES (@cs_query_id, '查询工单', 'CS_TICKET_QUERY', '工单进度/处理人/时限', '["工单", "查询", "进度", "处理人"]', 'ACTIVE', @creator_id, @creator_name, @creator_name, @current_time, @current_time);

-- 重置密码（业务办理）
INSERT INTO t_intent (catalog_id, name, code, description, labels, status, creator_id, created_by, updated_by, created_at, updated_at)
VALUES (@cs_business_id, '重置密码', 'CS_ACCOUNT_RESET_PWD', '忘记密码/验证码/强制下线重置', '["密码", "重置", "验证码", "下线"]', 'ACTIVE', @creator_id, @creator_name, @creator_name, @current_time, @current_time);

-- 账号解锁（业务办理）
INSERT INTO t_intent (catalog_id, name, code, description, labels, status, creator_id, created_by, updated_by, created_at, updated_at)
VALUES (@cs_business_id, '账号解锁', 'CS_ACCOUNT_UNLOCK', '多次失败被锁/人工解锁', '["账号", "解锁", "失败", "人工"]', 'ACTIVE', @creator_id, @creator_name, @creator_name, @current_time, @current_time);

-- 升级/转人工（技术支持）
INSERT INTO t_intent (catalog_id, name, code, description, labels, status, creator_id, created_by, updated_by, created_at, updated_at)
VALUES (@cs_tech_support_id, '升级/转人工', 'CS_ESCALATE_AGENT', 'SLA升级/转二线/转人工客服', '["升级", "转人工", "SLA", "二线"]', 'ACTIVE', @creator_id, @creator_name, @creator_name, @current_time, @current_time);

-- =====================================================
-- 12. 验证导入结果（增量后）
-- =====================================================
SELECT '意图目录导入完成' as status, COUNT(*) as catalog_count FROM t_intent_catalog;
SELECT '意图导入完成' as status, COUNT(*) as intent_count FROM t_intent;

-- 查看目录结构
SELECT 
    c1.name as root_catalog,
    c2.name as sub_catalog,
    COUNT(i.id) as intent_count
FROM t_intent_catalog c1
LEFT JOIN t_intent_catalog c2 ON c1.id = c2.parent_id
LEFT JOIN t_intent i ON c2.id = i.catalog_id
WHERE c1.parent_id IS NULL
GROUP BY c1.id, c1.name, c2.id, c2.name
ORDER BY c1.sort_order, c2.sort_order;

-- =====================================================
-- 脚本执行完成
-- =====================================================
SELECT '意图分类数据导入脚本执行完成！' as message;
