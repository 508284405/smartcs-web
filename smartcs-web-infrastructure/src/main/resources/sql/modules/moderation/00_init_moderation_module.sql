-- 内容审核模块初始化脚本
-- 按顺序执行所有审核模块相关的SQL文件

-- 1. 创建违规分类表并插入默认数据
SOURCE modules/moderation/01_moderation_category.sql;

-- 2. 创建审核记录表
SOURCE modules/moderation/02_moderation_record.sql;

-- 3. 创建审核配置表并插入默认配置
SOURCE modules/moderation/03_moderation_config.sql;

-- 4. 创建关键词规则表并插入默认规则
SOURCE modules/moderation/04_moderation_keyword_rule.sql;

-- 审核模块初始化完成
SELECT 'Moderation module initialized successfully' as status;