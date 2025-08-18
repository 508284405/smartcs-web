-- =====================================================
-- SmartCS-Web 意图样本语料数据导入脚本
-- 为各个意图分类提供丰富的训练样本
-- 创建时间：2025-08-17
-- =====================================================

SET SESSION sql_mode = '';

-- 设置时间戳变量
SET @current_time = UNIX_TIMESTAMP() * 1000;
SET @creator_name = 'system';

-- =====================================================
-- 农业领域意图样本
-- =====================================================

-- 种子选择意图样本
INSERT INTO t_intent_sample (version_id, type, text, source, created_by, updated_by, created_at, updated_at) VALUES
-- 注意：version_id 需要根据实际的版本ID进行替换，这里使用占位符
(1, 'TRAIN', '推荐适合这个地区的玉米品种', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(1, 'TRAIN', '什么品种的小麦抗旱性好', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(1, 'TRAIN', '我想种植水稻，选什么品种比较好', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(1, 'TRAIN', '这个地方适合种什么蔬菜品种', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(1, 'TRAIN', '有没有产量高的大豆品种推荐', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(1, 'TRAIN', '哪个玉米品种抗倒伏能力强', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(1, 'TRAIN', '早熟的水稻品种有哪些', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(1, 'TRAIN', '选择什么品种的棉花好', 'manual', @creator_name, @creator_name, @current_time, @current_time);

-- 播种指导意图样本
INSERT INTO t_intent_sample (version_id, type, text, source, created_by, updated_by, created_at, updated_at) VALUES
(2, 'TRAIN', '小麦的最佳播种时间', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(2, 'TRAIN', '水稻播种深度多少合适', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(2, 'TRAIN', '玉米播种密度怎么控制', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(2, 'TRAIN', '春播应该注意什么', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(2, 'TRAIN', '大豆播种行距多少', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(2, 'TRAIN', '什么时候播种最好', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(2, 'TRAIN', '播种前需要做什么准备', 'manual', @creator_name, @creator_name, @current_time, @current_time);

-- 病虫害防治意图样本
INSERT INTO t_intent_sample (version_id, type, text, source, created_by, updated_by, created_at, updated_at) VALUES
(3, 'TRAIN', '番茄叶子发黄是什么病', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(3, 'TRAIN', '如何防治稻飞虱', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(3, 'TRAIN', '玉米螟虫怎么治', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(3, 'TRAIN', '小麦条纹花叶病防治', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(3, 'TRAIN', '蚜虫用什么农药', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(3, 'TRAIN', '果树病虫害预防措施', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(3, 'TRAIN', '作物叶片有斑点是什么病', 'manual', @creator_name, @creator_name, @current_time, @current_time);

-- =====================================================
-- 畜牧业领域意图样本
-- =====================================================

-- 品种选择意图样本
INSERT INTO t_intent_sample (version_id, type, text, source, created_by, updated_by, created_at, updated_at) VALUES
(4, 'TRAIN', '养什么品种的猪利润高', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(4, 'TRAIN', '奶牛品种特点对比', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(4, 'TRAIN', '肉鸡哪个品种长得快', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(4, 'TRAIN', '蛋鸡选什么品种产蛋多', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(4, 'TRAIN', '肉牛品种推荐', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(4, 'TRAIN', '山羊和绵羊哪个好养', 'manual', @creator_name, @creator_name, @current_time, @current_time);

-- 疾病诊断意图样本
INSERT INTO t_intent_sample (version_id, type, text, source, created_by, updated_by, created_at, updated_at) VALUES
(5, 'TRAIN', '牛腹泻的原因分析', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(5, 'TRAIN', '羊咳嗽是什么病', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(5, 'TRAIN', '猪不吃食怎么回事', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(5, 'TRAIN', '鸡拉稀是什么原因', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(5, 'TRAIN', '牛发烧症状表现', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(5, 'TRAIN', '母猪不发情的原因', 'manual', @creator_name, @creator_name, @current_time, @current_time);

-- =====================================================
-- 电商领域意图样本
-- =====================================================

-- 产品信息意图样本
INSERT INTO t_intent_sample (version_id, type, text, source, created_by, updated_by, created_at, updated_at) VALUES
(6, 'TRAIN', '这款手机有什么颜色', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(6, 'TRAIN', '衣服的尺码表', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(6, 'TRAIN', '产品详细介绍', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(6, 'TRAIN', '商品有哪些规格', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(6, 'TRAIN', '这个产品的材质是什么', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(6, 'TRAIN', '能介绍一下这个商品吗', 'manual', @creator_name, @creator_name, @current_time, @current_time);

-- 下单支付意图样本
INSERT INTO t_intent_sample (version_id, type, text, source, created_by, updated_by, created_at, updated_at) VALUES
(7, 'TRAIN', '怎么使用优惠券', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(7, 'TRAIN', '支付方式有哪些', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(7, 'TRAIN', '如何下单购买', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(7, 'TRAIN', '可以货到付款吗', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(7, 'TRAIN', '支付失败怎么办', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(7, 'TRAIN', '怎么修改订单', 'manual', @creator_name, @creator_name, @current_time, @current_time);

-- 退换货意图样本
INSERT INTO t_intent_sample (version_id, type, text, source, created_by, updated_by, created_at, updated_at) VALUES
(8, 'TRAIN', '不满意可以退货吗', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(8, 'TRAIN', '换货流程说明', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(8, 'TRAIN', '如何申请退款', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(8, 'TRAIN', '退货需要什么条件', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(8, 'TRAIN', '我要退货', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(8, 'TRAIN', '商品有问题想换', 'manual', @creator_name, @creator_name, @current_time, @current_time);

-- =====================================================
-- 客服系统意图样本
-- =====================================================

-- 状态查询意图样本
INSERT INTO t_intent_sample (version_id, type, text, source, created_by, updated_by, created_at, updated_at) VALUES
(9, 'TRAIN', '我的订单状态', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(9, 'TRAIN', '账户余额查询', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(9, 'TRAIN', '查看订单进度', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(9, 'TRAIN', '我的会员等级', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(9, 'TRAIN', '积分余额多少', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(9, 'TRAIN', '当前服务状态', 'manual', @creator_name, @creator_name, @current_time, @current_time);

-- 故障报修意图样本
INSERT INTO t_intent_sample (version_id, type, text, source, created_by, updated_by, created_at, updated_at) VALUES
(10, 'TRAIN', '设备不能正常使用', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(10, 'TRAIN', '系统登录不了', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(10, 'TRAIN', '网络连接有问题', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(10, 'TRAIN', '软件运行异常', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(10, 'TRAIN', '我要报修', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(10, 'TRAIN', '设备故障了', 'manual', @creator_name, @creator_name, @current_time, @current_time);

-- =====================================================
-- 更多领域扩展样本（可选）
-- =====================================================

-- 通用表达样本（示例，占位 version_id）
INSERT INTO t_intent_sample (version_id, type, text, source, created_by, updated_by, created_at, updated_at) VALUES
-- 肯定表达
(11, 'TRAIN', '好的', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(11, 'TRAIN', '可以', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(11, 'TRAIN', '是的', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(11, 'TRAIN', '同意', 'manual', @creator_name, @creator_name, @current_time, @current_time),
-- 否定表达
(12, 'TRAIN', '不要', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(12, 'TRAIN', '不是', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(12, 'TRAIN', '取消', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(12, 'TRAIN', '不需要', 'manual', @creator_name, @creator_name, @current_time, @current_time),
-- 疑问表达
(13, 'TRAIN', '可以帮我看看吗？', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(13, 'TRAIN', '这是什么意思？', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(13, 'TRAIN', '还有别的选择吗？', 'manual', @creator_name, @creator_name, @current_time, @current_time);

-- =====================================================
-- 增量完善：为新增意图自动创建版本并插入样本
-- 通过意图编码(code)解析 intent_id，若 v1 已存在则重用
-- =====================================================

/* 农业 AGRI_FERT_PLAN */
SET @intent_id := (SELECT id FROM t_intent WHERE code='AGRI_FERT_PLAN');
INSERT INTO t_intent_version (intent_id, version_number, version_name, status, created_by, created_at, updated_at)
VALUES (@intent_id, 'v1', '初始版本', 'ACTIVE', @creator_name, @current_time, @current_time)
ON DUPLICATE KEY UPDATE id=LAST_INSERT_ID(id), updated_at=VALUES(updated_at);
SET @ver_id := LAST_INSERT_ID();
INSERT INTO t_intent_sample (version_id, type, text, source, created_by, updated_by, created_at, updated_at) VALUES
(@ver_id, 'TRAIN', '水稻分蘖期氮磷钾比例怎么配？', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(@ver_id, 'TRAIN', '玉米拔节期追肥有什么建议？', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(@ver_id, 'TRAIN', '苹果树秋季施肥用量如何确定？', 'manual', @creator_name, @creator_name, @current_time, @current_time);

/* 农业 AGRI_INSURANCE_CLAIM */
SET @intent_id := (SELECT id FROM t_intent WHERE code='AGRI_INSURANCE_CLAIM');
INSERT INTO t_intent_version (intent_id, version_number, version_name, status, created_by, created_at, updated_at)
VALUES (@intent_id, 'v1', '初始版本', 'ACTIVE', @creator_name, @current_time, @current_time)
ON DUPLICATE KEY UPDATE id=LAST_INSERT_ID(id), updated_at=VALUES(updated_at);
SET @ver_id := LAST_INSERT_ID();
INSERT INTO t_intent_sample (version_id, type, text, source, created_by, updated_by, created_at, updated_at) VALUES
(@ver_id, 'TRAIN', '农机购置保险怎么报案？', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(@ver_id, 'TRAIN', '水稻保险理赔需要哪些材料？', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(@ver_id, 'TRAIN', '理赔一般多久到账？', 'manual', @creator_name, @creator_name, @current_time, @current_time);

/* 农业 AGRI_EQUIP_TROUBLE */
SET @intent_id := (SELECT id FROM t_intent WHERE code='AGRI_EQUIP_TROUBLE');
INSERT INTO t_intent_version (intent_id, version_number, version_name, status, created_by, created_at, updated_at)
VALUES (@intent_id, 'v1', '初始版本', 'ACTIVE', @creator_name, @current_time, @current_time)
ON DUPLICATE KEY UPDATE id=LAST_INSERT_ID(id), updated_at=VALUES(updated_at);
SET @ver_id := LAST_INSERT_ID();
INSERT INTO t_intent_sample (version_id, type, text, source, created_by, updated_by, created_at, updated_at) VALUES
(@ver_id, 'TRAIN', '喷灌机不出水怎么办？', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(@ver_id, 'TRAIN', '无人机电机发热如何处理？', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(@ver_id, 'TRAIN', '施肥机流量不均怎么排查？', 'manual', @creator_name, @creator_name, @current_time, @current_time);

/* 农业 AGRI_SENSOR_ALERT */
SET @intent_id := (SELECT id FROM t_intent WHERE code='AGRI_SENSOR_ALERT');
INSERT INTO t_intent_version (intent_id, version_number, version_name, status, created_by, created_at, updated_at)
VALUES (@intent_id, 'v1', '初始版本', 'ACTIVE', @creator_name, @current_time, @current_time)
ON DUPLICATE KEY UPDATE id=LAST_INSERT_ID(id), updated_at=VALUES(updated_at);
SET @ver_id := LAST_INSERT_ID();
INSERT INTO t_intent_sample (version_id, type, text, source, created_by, updated_by, created_at, updated_at) VALUES
(@ver_id, 'TRAIN', '土壤湿度异常如何处理？', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(@ver_id, 'TRAIN', '温室温度过高预警怎么降温？', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(@ver_id, 'TRAIN', 'EC值超标怎么调整？', 'manual', @creator_name, @creator_name, @current_time, @current_time);

/* 畜牧业 LIVE_BREED_SCH */
SET @intent_id := (SELECT id FROM t_intent WHERE code='LIVE_BREED_SCH');
INSERT INTO t_intent_version (intent_id, version_number, version_name, status, created_by, created_at, updated_at)
VALUES (@intent_id, 'v1', '初始版本', 'ACTIVE', @creator_name, @current_time, @current_time)
ON DUPLICATE KEY UPDATE id=LAST_INSERT_ID(id), updated_at=VALUES(updated_at);
SET @ver_id := LAST_INSERT_ID();
INSERT INTO t_intent_sample (version_id, type, text, source, created_by, updated_by, created_at, updated_at) VALUES
(@ver_id, 'TRAIN', '母猪配种最佳时间是什么时候？', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(@ver_id, 'TRAIN', '肉牛育肥一般多长时间出栏？', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(@ver_id, 'TRAIN', '成年母羊繁育间隔建议', 'manual', @creator_name, @creator_name, @current_time, @current_time);

/* 畜牧业 LIVE_ESTRUS_DET */
SET @intent_id := (SELECT id FROM t_intent WHERE code='LIVE_ESTRUS_DET');
INSERT INTO t_intent_version (intent_id, version_number, version_name, status, created_by, created_at, updated_at)
VALUES (@intent_id, 'v1', '初始版本', 'ACTIVE', @creator_name, @current_time, @current_time)
ON DUPLICATE KEY UPDATE id=LAST_INSERT_ID(id), updated_at=VALUES(updated_at);
SET @ver_id := LAST_INSERT_ID();
INSERT INTO t_intent_sample (version_id, type, text, source, created_by, updated_by, created_at, updated_at) VALUES
(@ver_id, 'TRAIN', '奶牛发情有哪些典型表现？', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(@ver_id, 'TRAIN', '母羊发情怎么判断？', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(@ver_id, 'TRAIN', '发情监测项圈如何使用？', 'manual', @creator_name, @creator_name, @current_time, @current_time);

/* 畜牧业 LIVE_FEED_FORM */
SET @intent_id := (SELECT id FROM t_intent WHERE code='LIVE_FEED_FORM');
INSERT INTO t_intent_version (intent_id, version_number, version_name, status, created_by, created_at, updated_at)
VALUES (@intent_id, 'v1', '初始版本', 'ACTIVE', @creator_name, @current_time, @current_time)
ON DUPLICATE KEY UPDATE id=LAST_INSERT_ID(id), updated_at=VALUES(updated_at);
SET @ver_id := LAST_INSERT_ID();
INSERT INTO t_intent_sample (version_id, type, text, source, created_by, updated_by, created_at, updated_at) VALUES
(@ver_id, 'TRAIN', '育肥猪日粮蛋白比例多少合适？', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(@ver_id, 'TRAIN', '犊牛断奶前的饲喂配方', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(@ver_id, 'TRAIN', '母牛泌乳期营养需求', 'manual', @creator_name, @creator_name, @current_time, @current_time);

/* 畜牧业 LIVE_FEED_SUP */
SET @intent_id := (SELECT id FROM t_intent WHERE code='LIVE_FEED_SUP');
INSERT INTO t_intent_version (intent_id, version_number, version_name, status, created_by, created_at, updated_at)
VALUES (@intent_id, 'v1', '初始版本', 'ACTIVE', @creator_name, @current_time, @current_time)
ON DUPLICATE KEY UPDATE id=LAST_INSERT_ID(id), updated_at=VALUES(updated_at);
SET @ver_id := LAST_INSERT_ID();
INSERT INTO t_intent_sample (version_id, type, text, source, created_by, updated_by, created_at, updated_at) VALUES
(@ver_id, 'TRAIN', '附近有优质苜蓿供应吗？', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(@ver_id, 'TRAIN', '玉米DDGS在哪里买划算？', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(@ver_id, 'TRAIN', '预混料采购渠道有哪些？', 'manual', @creator_name, @creator_name, @current_time, @current_time);

/* 畜牧业 LIVE_WASTE_MGMT */
SET @intent_id := (SELECT id FROM t_intent WHERE code='LIVE_WASTE_MGMT');
INSERT INTO t_intent_version (intent_id, version_number, version_name, status, created_by, created_at, updated_at)
VALUES (@intent_id, 'v1', '初始版本', 'ACTIVE', @creator_name, @current_time, @current_time)
ON DUPLICATE KEY UPDATE id=LAST_INSERT_ID(id), updated_at=VALUES(updated_at);
SET @ver_id := LAST_INSERT_ID();
INSERT INTO t_intent_sample (version_id, type, text, source, created_by, updated_by, created_at, updated_at) VALUES
(@ver_id, 'TRAIN', '沼液还田注意事项有哪些？', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(@ver_id, 'TRAIN', '固液分离后怎么处理？', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(@ver_id, 'TRAIN', '粪污达标排放标准咨询', 'manual', @creator_name, @creator_name, @current_time, @current_time);

/* 畜牧业 LIVE_INS_CLAIM */
SET @intent_id := (SELECT id FROM t_intent WHERE code='LIVE_INS_CLAIM');
INSERT INTO t_intent_version (intent_id, version_number, version_name, status, created_by, created_at, updated_at)
VALUES (@intent_id, 'v1', '初始版本', 'ACTIVE', @creator_name, @current_time, @current_time)
ON DUPLICATE KEY UPDATE id=LAST_INSERT_ID(id), updated_at=VALUES(updated_at);
SET @ver_id := LAST_INSERT_ID();
INSERT INTO t_intent_sample (version_id, type, text, source, created_by, updated_by, created_at, updated_at) VALUES
(@ver_id, 'TRAIN', '家禽疫情理赔需要哪些材料？', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(@ver_id, 'TRAIN', '养殖险如何报案？', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(@ver_id, 'TRAIN', '理赔周期一般多久？', 'manual', @creator_name, @creator_name, @current_time, @current_time);

/* 电商 ECOM_PROD_SEARCH */
SET @intent_id := (SELECT id FROM t_intent WHERE code='ECOM_PROD_SEARCH');
INSERT INTO t_intent_version (intent_id, version_number, version_name, status, created_by, created_at, updated_at)
VALUES (@intent_id, 'v1', '初始版本', 'ACTIVE', @creator_name, @current_time, @current_time)
ON DUPLICATE KEY UPDATE id=LAST_INSERT_ID(id), updated_at=VALUES(updated_at);
SET @ver_id := LAST_INSERT_ID();
INSERT INTO t_intent_sample (version_id, type, text, source, created_by, updated_by, created_at, updated_at) VALUES
(@ver_id, 'TRAIN', '预算500-1000元的蓝牙耳机推荐', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(@ver_id, 'TRAIN', '有什么人体工学办公椅？', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(@ver_id, 'TRAIN', '轻薄本电脑有哪些推荐？', 'manual', @creator_name, @creator_name, @current_time, @current_time);

/* 电商 ECOM_ORDER_STATUS */
SET @intent_id := (SELECT id FROM t_intent WHERE code='ECOM_ORDER_STATUS');
INSERT INTO t_intent_version (intent_id, version_number, version_name, status, created_by, created_at, updated_at)
VALUES (@intent_id, 'v1', '初始版本', 'ACTIVE', @creator_name, @current_time, @current_time)
ON DUPLICATE KEY UPDATE id=LAST_INSERT_ID(id), updated_at=VALUES(updated_at);
SET @ver_id := LAST_INSERT_ID();
INSERT INTO t_intent_sample (version_id, type, text, source, created_by, updated_by, created_at, updated_at) VALUES
(@ver_id, 'TRAIN', '订单12345现在到哪了？', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(@ver_id, 'TRAIN', '预计什么时候可以送到？', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(@ver_id, 'TRAIN', '这个订单能否加急？', 'manual', @creator_name, @creator_name, @current_time, @current_time);

/* 电商 ECOM_AFTER_REFUND */
SET @intent_id := (SELECT id FROM t_intent WHERE code='ECOM_AFTER_REFUND');
INSERT INTO t_intent_version (intent_id, version_number, version_name, status, created_by, created_at, updated_at)
VALUES (@intent_id, 'v1', '初始版本', 'ACTIVE', @creator_name, @current_time, @current_time)
ON DUPLICATE KEY UPDATE id=LAST_INSERT_ID(id), updated_at=VALUES(updated_at);
SET @ver_id := LAST_INSERT_ID();
INSERT INTO t_intent_sample (version_id, type, text, source, created_by, updated_by, created_at, updated_at) VALUES
(@ver_id, 'TRAIN', '七天无理由如何退款？', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(@ver_id, 'TRAIN', '已签收还能申请退款吗？', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(@ver_id, 'TRAIN', '退款一般多久到账？', 'manual', @creator_name, @creator_name, @current_time, @current_time);

/* 电商 ECOM_ADDR_CHANGE */
SET @intent_id := (SELECT id FROM t_intent WHERE code='ECOM_ADDR_CHANGE');
INSERT INTO t_intent_version (intent_id, version_number, version_name, status, created_by, created_at, updated_at)
VALUES (@intent_id, 'v1', '初始版本', 'ACTIVE', @creator_name, @current_time, @current_time)
ON DUPLICATE KEY UPDATE id=LAST_INSERT_ID(id), updated_at=VALUES(updated_at);
SET @ver_id := LAST_INSERT_ID();
INSERT INTO t_intent_sample (version_id, type, text, source, created_by, updated_by, created_at, updated_at) VALUES
(@ver_id, 'TRAIN', '下单后还能改地址吗？', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(@ver_id, 'TRAIN', '把收件人电话改一下', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(@ver_id, 'TRAIN', '可以改成到店自提吗？', 'manual', @creator_name, @creator_name, @current_time, @current_time);

/* 客服 CS_TICKET_CREATE */
SET @intent_id := (SELECT id FROM t_intent WHERE code='CS_TICKET_CREATE');
INSERT INTO t_intent_version (intent_id, version_number, version_name, status, created_by, created_at, updated_at)
VALUES (@intent_id, 'v1', '初始版本', 'ACTIVE', @creator_name, @current_time, @current_time)
ON DUPLICATE KEY UPDATE id=LAST_INSERT_ID(id), updated_at=VALUES(updated_at);
SET @ver_id := LAST_INSERT_ID();
INSERT INTO t_intent_sample (version_id, type, text, source, created_by, updated_by, created_at, updated_at) VALUES
(@ver_id, 'TRAIN', '系统登录不了，帮我提个工单', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(@ver_id, 'TRAIN', '接口超时想报障', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(@ver_id, 'TRAIN', '提交一个错误反馈工单', 'manual', @creator_name, @creator_name, @current_time, @current_time);

/* 客服 CS_TICKET_QUERY */
SET @intent_id := (SELECT id FROM t_intent WHERE code='CS_TICKET_QUERY');
INSERT INTO t_intent_version (intent_id, version_number, version_name, status, created_by, created_at, updated_at)
VALUES (@intent_id, 'v1', '初始版本', 'ACTIVE', @creator_name, @current_time, @current_time)
ON DUPLICATE KEY UPDATE id=LAST_INSERT_ID(id), updated_at=VALUES(updated_at);
SET @ver_id := LAST_INSERT_ID();
INSERT INTO t_intent_sample (version_id, type, text, source, created_by, updated_by, created_at, updated_at) VALUES
(@ver_id, 'TRAIN', '工单A123现在进展如何？', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(@ver_id, 'TRAIN', '预计什么时候能处理完？', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(@ver_id, 'TRAIN', '这个工单分配给谁了？', 'manual', @creator_name, @creator_name, @current_time, @current_time);

/* 客服 CS_ACCOUNT_RESET_PWD */
SET @intent_id := (SELECT id FROM t_intent WHERE code='CS_ACCOUNT_RESET_PWD');
INSERT INTO t_intent_version (intent_id, version_number, version_name, status, created_by, created_at, updated_at)
VALUES (@intent_id, 'v1', '初始版本', 'ACTIVE', @creator_name, @current_time, @current_time)
ON DUPLICATE KEY UPDATE id=LAST_INSERT_ID(id), updated_at=VALUES(updated_at);
SET @ver_id := LAST_INSERT_ID();
INSERT INTO t_intent_sample (version_id, type, text, source, created_by, updated_by, created_at, updated_at) VALUES
(@ver_id, 'TRAIN', '忘记密码怎么重置？', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(@ver_id, 'TRAIN', '手机收不到验证码怎么办？', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(@ver_id, 'TRAIN', '能否强制下线再重置密码？', 'manual', @creator_name, @creator_name, @current_time, @current_time);

/* 客服 CS_ACCOUNT_UNLOCK */
SET @intent_id := (SELECT id FROM t_intent WHERE code='CS_ACCOUNT_UNLOCK');
INSERT INTO t_intent_version (intent_id, version_number, version_name, status, created_by, created_at, updated_at)
VALUES (@intent_id, 'v1', '初始版本', 'ACTIVE', @creator_name, @current_time, @current_time)
ON DUPLICATE KEY UPDATE id=LAST_INSERT_ID(id), updated_at=VALUES(updated_at);
SET @ver_id := LAST_INSERT_ID();
INSERT INTO t_intent_sample (version_id, type, text, source, created_by, updated_by, created_at, updated_at) VALUES
(@ver_id, 'TRAIN', '多次登录失败账号被锁了', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(@ver_id, 'TRAIN', '如何解除账号锁定？', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(@ver_id, 'TRAIN', '可以人工帮我解锁吗？', 'manual', @creator_name, @creator_name, @current_time, @current_time);

/* 客服 CS_ESCALATE_AGENT */
SET @intent_id := (SELECT id FROM t_intent WHERE code='CS_ESCALATE_AGENT');
INSERT INTO t_intent_version (intent_id, version_number, version_name, status, created_by, created_at, updated_at)
VALUES (@intent_id, 'v1', '初始版本', 'ACTIVE', @creator_name, @current_time, @current_time)
ON DUPLICATE KEY UPDATE id=LAST_INSERT_ID(id), updated_at=VALUES(updated_at);
SET @ver_id := LAST_INSERT_ID();
INSERT INTO t_intent_sample (version_id, type, text, source, created_by, updated_by, created_at, updated_at) VALUES
(@ver_id, 'TRAIN', '问题拖太久了，帮我升级处理', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(@ver_id, 'TRAIN', '能不能转到二线支持？', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(@ver_id, 'TRAIN', '我想直接转人工客服', 'manual', @creator_name, @creator_name, @current_time, @current_time);

-- =====================================================
-- 为新增意图设置默认策略(t_intent_policy)与路由(t_intent_route)
-- 每个版本唯一：ON DUPLICATE KEY 覆盖更新配置
-- =====================================================

-- 工具说明：按意图code取v1版本ID
-- SET @intent_id := (SELECT id FROM t_intent WHERE code='...');
-- SET @ver_id := (SELECT id FROM t_intent_version WHERE intent_id=@intent_id AND version_number='v1' ORDER BY id DESC LIMIT 1);

/* 农业 AGRI_FERT_PLAN */
SET @intent_id := (SELECT id FROM t_intent WHERE code='AGRI_FERT_PLAN');
SET @ver_id := (SELECT id FROM t_intent_version WHERE intent_id=@intent_id AND version_number='v1' ORDER BY id DESC LIMIT 1);
INSERT INTO t_intent_policy (version_id, threshold_tau, margin_delta, temp_t, unknown_label, channel_overrides, created_by, updated_by, created_at, updated_at)
VALUES (@ver_id, 0.50, 0.10, 0.30, 'UNKNOWN', NULL, @creator_name, @creator_name, @current_time, @current_time)
ON DUPLICATE KEY UPDATE threshold_tau=VALUES(threshold_tau), margin_delta=VALUES(margin_delta), temp_t=VALUES(temp_t), updated_by=VALUES(updated_by), updated_at=VALUES(updated_at);
INSERT INTO t_intent_route (version_id, route_type, route_conf, created_by, updated_by, created_at, updated_at)
VALUES (@ver_id, 'LLM', JSON_OBJECT('model','default','top_k',3,'rerank',false), @creator_name, @creator_name, @current_time, @current_time)
ON DUPLICATE KEY UPDATE route_type=VALUES(route_type), route_conf=VALUES(route_conf), updated_by=VALUES(updated_by), updated_at=VALUES(updated_at);

/* 农业 AGRI_INSURANCE_CLAIM */
SET @intent_id := (SELECT id FROM t_intent WHERE code='AGRI_INSURANCE_CLAIM');
SET @ver_id := (SELECT id FROM t_intent_version WHERE intent_id=@intent_id AND version_number='v1' ORDER BY id DESC LIMIT 1);
INSERT INTO t_intent_policy VALUES (@ver_id, 0.55, 0.10, 0.30, 'UNKNOWN', NULL, @creator_name, @creator_name, @current_time, @current_time)
ON DUPLICATE KEY UPDATE threshold_tau=VALUES(threshold_tau), margin_delta=VALUES(margin_delta), temp_t=VALUES(temp_t), updated_by=VALUES(updated_by), updated_at=VALUES(updated_at);
INSERT INTO t_intent_route VALUES (@ver_id, 'LLM', JSON_OBJECT('model','default','top_k',3), @creator_name, @creator_name, @current_time, @current_time)
ON DUPLICATE KEY UPDATE route_type=VALUES(route_type), route_conf=VALUES(route_conf), updated_by=VALUES(updated_by), updated_at=VALUES(updated_at);

/* 农业 AGRI_EQUIP_TROUBLE */
SET @intent_id := (SELECT id FROM t_intent WHERE code='AGRI_EQUIP_TROUBLE');
SET @ver_id := (SELECT id FROM t_intent_version WHERE intent_id=@intent_id AND version_number='v1' ORDER BY id DESC LIMIT 1);
INSERT INTO t_intent_policy VALUES (@ver_id, 0.50, 0.10, 0.30, 'UNKNOWN', NULL, @creator_name, @creator_name, @current_time, @current_time)
ON DUPLICATE KEY UPDATE threshold_tau=VALUES(threshold_tau), margin_delta=VALUES(margin_delta), temp_t=VALUES(temp_t), updated_by=VALUES(updated_by), updated_at=VALUES(updated_at);
INSERT INTO t_intent_route VALUES (@ver_id, 'LLM', JSON_OBJECT('model','default'), @creator_name, @creator_name, @current_time, @current_time)
ON DUPLICATE KEY UPDATE route_type=VALUES(route_type), route_conf=VALUES(route_conf), updated_by=VALUES(updated_by), updated_at=VALUES(updated_at);

/* 农业 AGRI_SENSOR_ALERT */
SET @intent_id := (SELECT id FROM t_intent WHERE code='AGRI_SENSOR_ALERT');
SET @ver_id := (SELECT id FROM t_intent_version WHERE intent_id=@intent_id AND version_number='v1' ORDER BY id DESC LIMIT 1);
INSERT INTO t_intent_policy VALUES (@ver_id, 0.60, 0.10, 0.20, 'UNKNOWN', NULL, @creator_name, @creator_name, @current_time, @current_time)
ON DUPLICATE KEY UPDATE threshold_tau=VALUES(threshold_tau), margin_delta=VALUES(margin_delta), temp_t=VALUES(temp_t), updated_by=VALUES(updated_by), updated_at=VALUES(updated_at);
INSERT INTO t_intent_route VALUES (@ver_id, 'HYBRID', JSON_OBJECT('strategy','threshold_first','threshold',0.6,'fallback','LLM'), @creator_name, @creator_name, @current_time, @current_time)
ON DUPLICATE KEY UPDATE route_type=VALUES(route_type), route_conf=VALUES(route_conf), updated_by=VALUES(updated_by), updated_at=VALUES(updated_at);

/* 畜牧业 LIVE_BREED_SCH */
SET @intent_id := (SELECT id FROM t_intent WHERE code='LIVE_BREED_SCH');
SET @ver_id := (SELECT id FROM t_intent_version WHERE intent_id=@intent_id AND version_number='v1' ORDER BY id DESC LIMIT 1);
INSERT INTO t_intent_policy VALUES (@ver_id, 0.50, 0.10, 0.30, 'UNKNOWN', NULL, @creator_name, @creator_name, @current_time, @current_time)
ON DUPLICATE KEY UPDATE threshold_tau=VALUES(threshold_tau), margin_delta=VALUES(margin_delta), temp_t=VALUES(temp_t), updated_by=VALUES(updated_by), updated_at=VALUES(updated_at);
INSERT INTO t_intent_route VALUES (@ver_id, 'LLM', JSON_OBJECT('model','default'), @creator_name, @creator_name, @current_time, @current_time)
ON DUPLICATE KEY UPDATE route_type=VALUES(route_type), route_conf=VALUES(route_conf), updated_by=VALUES(updated_by), updated_at=VALUES(updated_at);

/* 畜牧业 LIVE_ESTRUS_DET */
SET @intent_id := (SELECT id FROM t_intent WHERE code='LIVE_ESTRUS_DET');
SET @ver_id := (SELECT id FROM t_intent_version WHERE intent_id=@intent_id AND version_number='v1' ORDER BY id DESC LIMIT 1);
INSERT INTO t_intent_policy VALUES (@ver_id, 0.50, 0.10, 0.30, 'UNKNOWN', NULL, @creator_name, @creator_name, @current_time, @current_time)
ON DUPLICATE KEY UPDATE threshold_tau=VALUES(threshold_tau), margin_delta=VALUES(margin_delta), temp_t=VALUES(temp_t), updated_by=VALUES(updated_by), updated_at=VALUES(updated_at);
INSERT INTO t_intent_route VALUES (@ver_id, 'LLM', JSON_OBJECT('model','default'), @creator_name, @creator_name, @current_time, @current_time)
ON DUPLICATE KEY UPDATE route_type=VALUES(route_type), route_conf=VALUES(route_conf), updated_by=VALUES(updated_by), updated_at=VALUES(updated_at);

/* 畜牧业 LIVE_FEED_FORM */
SET @intent_id := (SELECT id FROM t_intent WHERE code='LIVE_FEED_FORM');
SET @ver_id := (SELECT id FROM t_intent_version WHERE intent_id=@intent_id AND version_number='v1' ORDER BY id DESC LIMIT 1);
INSERT INTO t_intent_policy VALUES (@ver_id, 0.50, 0.10, 0.30, 'UNKNOWN', NULL, @creator_name, @creator_name, @current_time, @current_time)
ON DUPLICATE KEY UPDATE threshold_tau=VALUES(threshold_tau), margin_delta=VALUES(margin_delta), temp_t=VALUES(temp_t), updated_by=VALUES(updated_by), updated_at=VALUES(updated_at);
INSERT INTO t_intent_route VALUES (@ver_id, 'LLM', JSON_OBJECT('model','default'), @creator_name, @creator_name, @current_time, @current_time)
ON DUPLICATE KEY UPDATE route_type=VALUES(route_type), route_conf=VALUES(route_conf), updated_by=VALUES(updated_by), updated_at=VALUES(updated_at);

/* 畜牧业 LIVE_FEED_SUP */
SET @intent_id := (SELECT id FROM t_intent WHERE code='LIVE_FEED_SUP');
SET @ver_id := (SELECT id FROM t_intent_version WHERE intent_id=@intent_id AND version_number='v1' ORDER BY id DESC LIMIT 1);
INSERT INTO t_intent_policy VALUES (@ver_id, 0.55, 0.10, 0.30, 'UNKNOWN', NULL, @creator_name, @creator_name, @current_time, @current_time)
ON DUPLICATE KEY UPDATE threshold_tau=VALUES(threshold_tau), margin_delta=VALUES(margin_delta), temp_t=VALUES(temp_t), updated_by=VALUES(updated_by), updated_at=VALUES(updated_at);
INSERT INTO t_intent_route VALUES (@ver_id, 'RULE', JSON_OBJECT('keyword_rules',true), @creator_name, @creator_name, @current_time, @current_time)
ON DUPLICATE KEY UPDATE route_type=VALUES(route_type), route_conf=VALUES(route_conf), updated_by=VALUES(updated_by), updated_at=VALUES(updated_at);

/* 畜牧业 LIVE_WASTE_MGMT */
SET @intent_id := (SELECT id FROM t_intent WHERE code='LIVE_WASTE_MGMT');
SET @ver_id := (SELECT id FROM t_intent_version WHERE intent_id=@intent_id AND version_number='v1' ORDER BY id DESC LIMIT 1);
INSERT INTO t_intent_policy VALUES (@ver_id, 0.50, 0.10, 0.25, 'UNKNOWN', NULL, @creator_name, @creator_name, @current_time, @current_time)
ON DUPLICATE KEY UPDATE threshold_tau=VALUES(threshold_tau), margin_delta=VALUES(margin_delta), temp_t=VALUES(temp_t), updated_by=VALUES(updated_by), updated_at=VALUES(updated_at);
INSERT INTO t_intent_route VALUES (@ver_id, 'LLM', JSON_OBJECT('model','default'), @creator_name, @creator_name, @current_time, @current_time)
ON DUPLICATE KEY UPDATE route_type=VALUES(route_type), route_conf=VALUES(route_conf), updated_by=VALUES(updated_by), updated_at=VALUES(updated_at);

/* 畜牧业 LIVE_INS_CLAIM */
SET @intent_id := (SELECT id FROM t_intent WHERE code='LIVE_INS_CLAIM');
SET @ver_id := (SELECT id FROM t_intent_version WHERE intent_id=@intent_id AND version_number='v1' ORDER BY id DESC LIMIT 1);
INSERT INTO t_intent_policy VALUES (@ver_id, 0.55, 0.10, 0.30, 'UNKNOWN', NULL, @creator_name, @creator_name, @current_time, @current_time)
ON DUPLICATE KEY UPDATE threshold_tau=VALUES(threshold_tau), margin_delta=VALUES(margin_delta), temp_t=VALUES(temp_t), updated_by=VALUES(updated_by), updated_at=VALUES(updated_at);
INSERT INTO t_intent_route VALUES (@ver_id, 'LLM', JSON_OBJECT('model','default'), @creator_name, @creator_name, @current_time, @current_time)
ON DUPLICATE KEY UPDATE route_type=VALUES(route_type), route_conf=VALUES(route_conf), updated_by=VALUES(updated_by), updated_at=VALUES(updated_at);

/* 电商 ECOM_PROD_SEARCH */
SET @intent_id := (SELECT id FROM t_intent WHERE code='ECOM_PROD_SEARCH');
SET @ver_id := (SELECT id FROM t_intent_version WHERE intent_id=@intent_id AND version_number='v1' ORDER BY id DESC LIMIT 1);
INSERT INTO t_intent_policy VALUES (@ver_id, 0.50, 0.10, 0.35, 'UNKNOWN', JSON_OBJECT('web', JSON_OBJECT('tau',0.5)), @creator_name, @creator_name, @current_time, @current_time)
ON DUPLICATE KEY UPDATE threshold_tau=VALUES(threshold_tau), margin_delta=VALUES(margin_delta), temp_t=VALUES(temp_t), channel_overrides=VALUES(channel_overrides), updated_by=VALUES(updated_by), updated_at=VALUES(updated_at);
INSERT INTO t_intent_route VALUES (@ver_id, 'LLM', JSON_OBJECT('model','default','top_k',5), @creator_name, @creator_name, @current_time, @current_time)
ON DUPLICATE KEY UPDATE route_type=VALUES(route_type), route_conf=VALUES(route_conf), updated_by=VALUES(updated_by), updated_at=VALUES(updated_at);

/* 电商 ECOM_ORDER_STATUS */
SET @intent_id := (SELECT id FROM t_intent WHERE code='ECOM_ORDER_STATUS');
SET @ver_id := (SELECT id FROM t_intent_version WHERE intent_id=@intent_id AND version_number='v1' ORDER BY id DESC LIMIT 1);
INSERT INTO t_intent_policy VALUES (@ver_id, 0.55, 0.10, 0.30, 'UNKNOWN', JSON_OBJECT('web', JSON_OBJECT('tau',0.55)), @creator_name, @creator_name, @current_time, @current_time)
ON DUPLICATE KEY UPDATE threshold_tau=VALUES(threshold_tau), margin_delta=VALUES(margin_delta), temp_t=VALUES(temp_t), channel_overrides=VALUES(channel_overrides), updated_by=VALUES(updated_by), updated_at=VALUES(updated_at);
INSERT INTO t_intent_route VALUES (@ver_id, 'LLM', JSON_OBJECT('model','default'), @creator_name, @creator_name, @current_time, @current_time)
ON DUPLICATE KEY UPDATE route_type=VALUES(route_type), route_conf=VALUES(route_conf), updated_by=VALUES(updated_by), updated_at=VALUES(updated_at);

/* 电商 ECOM_AFTER_REFUND */
SET @intent_id := (SELECT id FROM t_intent WHERE code='ECOM_AFTER_REFUND');
SET @ver_id := (SELECT id FROM t_intent_version WHERE intent_id=@intent_id AND version_number='v1' ORDER BY id DESC LIMIT 1);
INSERT INTO t_intent_policy VALUES (@ver_id, 0.50, 0.10, 0.30, 'UNKNOWN', JSON_OBJECT('web', JSON_OBJECT('tau',0.5)), @creator_name, @creator_name, @current_time, @current_time)
ON DUPLICATE KEY UPDATE threshold_tau=VALUES(threshold_tau), margin_delta=VALUES(margin_delta), temp_t=VALUES(temp_t), channel_overrides=VALUES(channel_overrides), updated_by=VALUES(updated_by), updated_at=VALUES(updated_at);
INSERT INTO t_intent_route VALUES (@ver_id, 'LLM', JSON_OBJECT('model','default'), @creator_name, @creator_name, @current_time, @current_time)
ON DUPLICATE KEY UPDATE route_type=VALUES(route_type), route_conf=VALUES(route_conf), updated_by=VALUES(updated_by), updated_at=VALUES(updated_at);

/* 电商 ECOM_ADDR_CHANGE */
SET @intent_id := (SELECT id FROM t_intent WHERE code='ECOM_ADDR_CHANGE');
SET @ver_id := (SELECT id FROM t_intent_version WHERE intent_id=@intent_id AND version_number='v1' ORDER BY id DESC LIMIT 1);
INSERT INTO t_intent_policy VALUES (@ver_id, 0.65, 0.10, 0.20, 'UNKNOWN', JSON_OBJECT('web', JSON_OBJECT('tau',0.65)), @creator_name, @creator_name, @current_time, @current_time)
ON DUPLICATE KEY UPDATE threshold_tau=VALUES(threshold_tau), margin_delta=VALUES(margin_delta), temp_t=VALUES(temp_t), channel_overrides=VALUES(channel_overrides), updated_by=VALUES(updated_by), updated_at=VALUES(updated_at);
INSERT INTO t_intent_route VALUES (@ver_id, 'RULE', JSON_OBJECT('required_fields', JSON_ARRAY('orderNo','phone'),'allow_partial',false), @creator_name, @creator_name, @current_time, @current_time)
ON DUPLICATE KEY UPDATE route_type=VALUES(route_type), route_conf=VALUES(route_conf), updated_by=VALUES(updated_by), updated_at=VALUES(updated_at);

/* 客服 CS_TICKET_CREATE */
SET @intent_id := (SELECT id FROM t_intent WHERE code='CS_TICKET_CREATE');
SET @ver_id := (SELECT id FROM t_intent_version WHERE intent_id=@intent_id AND version_number='v1' ORDER BY id DESC LIMIT 1);
INSERT INTO t_intent_policy VALUES (@ver_id, 0.50, 0.10, 0.25, 'UNKNOWN', NULL, @creator_name, @creator_name, @current_time, @current_time)
ON DUPLICATE KEY UPDATE threshold_tau=VALUES(threshold_tau), margin_delta=VALUES(margin_delta), temp_t=VALUES(temp_t), updated_by=VALUES(updated_by), updated_at=VALUES(updated_at);
INSERT INTO t_intent_route VALUES (@ver_id, 'LLM', JSON_OBJECT('model','default','structure','form'), @creator_name, @creator_name, @current_time, @current_time)
ON DUPLICATE KEY UPDATE route_type=VALUES(route_type), route_conf=VALUES(route_conf), updated_by=VALUES(updated_by), updated_at=VALUES(updated_at);

/* 客服 CS_TICKET_QUERY */
SET @intent_id := (SELECT id FROM t_intent WHERE code='CS_TICKET_QUERY');
SET @ver_id := (SELECT id FROM t_intent_version WHERE intent_id=@intent_id AND version_number='v1' ORDER BY id DESC LIMIT 1);
INSERT INTO t_intent_policy VALUES (@ver_id, 0.55, 0.10, 0.25, 'UNKNOWN', NULL, @creator_name, @creator_name, @current_time, @current_time)
ON DUPLICATE KEY UPDATE threshold_tau=VALUES(threshold_tau), margin_delta=VALUES(margin_delta), temp_t=VALUES(temp_t), updated_by=VALUES(updated_by), updated_at=VALUES(updated_at);
INSERT INTO t_intent_route VALUES (@ver_id, 'LLM', JSON_OBJECT('model','default','tools', JSON_ARRAY('ticket.status.query')), @creator_name, @creator_name, @current_time, @current_time)
ON DUPLICATE KEY UPDATE route_type=VALUES(route_type), route_conf=VALUES(route_conf), updated_by=VALUES(updated_by), updated_at=VALUES(updated_at);

/* 客服 CS_ACCOUNT_RESET_PWD */
SET @intent_id := (SELECT id FROM t_intent WHERE code='CS_ACCOUNT_RESET_PWD');
SET @ver_id := (SELECT id FROM t_intent_version WHERE intent_id=@intent_id AND version_number='v1' ORDER BY id DESC LIMIT 1);
INSERT INTO t_intent_policy VALUES (@ver_id, 0.55, 0.10, 0.20, 'UNKNOWN', NULL, @creator_name, @creator_name, @current_time, @current_time)
ON DUPLICATE KEY UPDATE threshold_tau=VALUES(threshold_tau), margin_delta=VALUES(margin_delta), temp_t=VALUES(temp_t), updated_by=VALUES(updated_by), updated_at=VALUES(updated_at);
INSERT INTO t_intent_route VALUES (@ver_id, 'LLM', JSON_OBJECT('model','default','guardrails', JSON_ARRAY('security.reset-pwd')), @creator_name, @creator_name, @current_time, @current_time)
ON DUPLICATE KEY UPDATE route_type=VALUES(route_type), route_conf=VALUES(route_conf), updated_by=VALUES(updated_by), updated_at=VALUES(updated_at);

/* 客服 CS_ACCOUNT_UNLOCK */
SET @intent_id := (SELECT id FROM t_intent WHERE code='CS_ACCOUNT_UNLOCK');
SET @ver_id := (SELECT id FROM t_intent_version WHERE intent_id=@intent_id AND version_number='v1' ORDER BY id DESC LIMIT 1);
INSERT INTO t_intent_policy VALUES (@ver_id, 0.55, 0.10, 0.20, 'UNKNOWN', NULL, @creator_name, @creator_name, @current_time, @current_time)
ON DUPLICATE KEY UPDATE threshold_tau=VALUES(threshold_tau), margin_delta=VALUES(margin_delta), temp_t=VALUES(temp_t), updated_by=VALUES(updated_by), updated_at=VALUES(updated_at);
INSERT INTO t_intent_route VALUES (@ver_id, 'LLM', JSON_OBJECT('model','default','guardrails', JSON_ARRAY('security.unlock')), @creator_name, @creator_name, @current_time, @current_time)
ON DUPLICATE KEY UPDATE route_type=VALUES(route_type), route_conf=VALUES(route_conf), updated_by=VALUES(updated_by), updated_at=VALUES(updated_at);

/* 客服 CS_ESCALATE_AGENT */
SET @intent_id := (SELECT id FROM t_intent WHERE code='CS_ESCALATE_AGENT');
SET @ver_id := (SELECT id FROM t_intent_version WHERE intent_id=@intent_id AND version_number='v1' ORDER BY id DESC LIMIT 1);
INSERT INTO t_intent_policy VALUES (@ver_id, 0.50, 0.10, 0.25, 'UNKNOWN', NULL, @creator_name, @creator_name, @current_time, @current_time)
ON DUPLICATE KEY UPDATE threshold_tau=VALUES(threshold_tau), margin_delta=VALUES(margin_delta), temp_t=VALUES(temp_t), updated_by=VALUES(updated_by), updated_at=VALUES(updated_at);
INSERT INTO t_intent_route VALUES (@ver_id, 'LLM', JSON_OBJECT('model','default','handoff', true), @creator_name, @creator_name, @current_time, @current_time)
ON DUPLICATE KEY UPDATE route_type=VALUES(route_type), route_conf=VALUES(route_conf), updated_by=VALUES(updated_by), updated_at=VALUES(updated_at);

-- 问候表达
(13, 'TRAIN', '你好', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(13, 'TRAIN', '在吗', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(13, 'TRAIN', '有人吗', 'manual', @creator_name, @creator_name, @current_time, @current_time),
(13, 'TRAIN', '客服在吗', 'manual', @creator_name, @creator_name, @current_time, @current_time);

-- =====================================================
-- 验证导入结果
-- =====================================================
SELECT '意图样本导入完成' as status, COUNT(*) as sample_count FROM t_intent_sample;

-- 按意图统计样本数量
SELECT 
    i.name as intent_name,
    COUNT(s.id) as sample_count,
    GROUP_CONCAT(DISTINCT s.type) as sample_types
FROM t_intent i
LEFT JOIN t_intent_version v ON i.id = v.intent_id
LEFT JOIN t_intent_sample s ON v.id = s.version_id
GROUP BY i.id, i.name
ORDER BY i.id;

-- =====================================================
-- 脚本执行完成
-- =====================================================
SELECT '意图样本语料导入脚本执行完成！' as message;
