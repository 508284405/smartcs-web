-- 意图管理模块初始化脚本
-- 按依赖顺序执行所有意图相关表的创建

-- 1. 意图目录表
SOURCE 01_intent_catalog.sql;

-- 2. 意图表
SOURCE 02_intent.sql;

-- 3. 意图版本表
SOURCE 03_intent_version.sql;

-- 4. 意图策略表
SOURCE 04_intent_policy.sql;

-- 5. 意图路由表
SOURCE 05_intent_route.sql;

-- 6. 意图样本表
SOURCE 06_intent_sample.sql;

-- 7. 意图快照表
SOURCE 07_intent_snapshot.sql;

-- 8. 意图快照项表
SOURCE 08_intent_snapshot_item.sql;

-- 9. 意图分类日志表
SOURCE 09_intent_classification_log.sql;

-- 插入初始数据
INSERT INTO `t_intent_catalog` (`name`, `code`, `description`, `parent_id`, `sort_order`, `creator_id`, `is_deleted`, `created_by`, `created_at`, `updated_at`) VALUES
('默认目录', 'default', '系统默认意图目录', NULL, 0, 1, 0, 'system', UNIX_TIMESTAMP(NOW()) * 1000, UNIX_TIMESTAMP(NOW()) * 1000),
('智慧农业', 'smart_agriculture', '智慧农业相关意图', NULL, 1, 1, 0, 'system', UNIX_TIMESTAMP(NOW()) * 1000, UNIX_TIMESTAMP(NOW()) * 1000),
('智慧畜牧', 'smart_livestock', '智慧畜牧相关意图', NULL, 2, 1, 0, 'system', UNIX_TIMESTAMP(NOW()) * 1000, UNIX_TIMESTAMP(NOW()) * 1000);

-- 插入示例意图
INSERT INTO `t_intent` (`catalog_id`, `name`, `code`, `description`, `status`, `creator_id`, `is_deleted`, `created_by`, `created_at`, `updated_at`) VALUES
(1, '问候', 'greeting', '用户问候相关意图', 'DRAFT', 1, 0, 'system', UNIX_TIMESTAMP(NOW()) * 1000, UNIX_TIMESTAMP(NOW()) * 1000),
(1, '告别', 'goodbye', '用户告别相关意图', 'DRAFT', 1, 0, 'system', UNIX_TIMESTAMP(NOW()) * 1000, UNIX_TIMESTAMP(NOW()) * 1000),
(2, '病虫害识别', 'pest_identification', '农作物病虫害识别', 'DRAFT', 1, 0, 'system', UNIX_TIMESTAMP(NOW()) * 1000, UNIX_TIMESTAMP(NOW()) * 1000),
(3, '健康异常告警', 'health_alert', '牲畜健康异常告警', 'DRAFT', 1, 0, 'system', UNIX_TIMESTAMP(NOW()) * 1000, UNIX_TIMESTAMP(NOW()) * 1000);