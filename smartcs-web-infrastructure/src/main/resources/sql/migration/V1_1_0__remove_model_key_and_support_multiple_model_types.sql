-- 模型管理功能优化：删除模型Key字段，支持多选模型类型
-- 版本：V1.1.0
-- 时间：2025-01-27

-- 1. 删除model_key相关的索引和约束
DROP INDEX IF EXISTS `idx_model_key` ON `t_model`;
DROP INDEX IF EXISTS `idx_provider_model` ON `t_model`;

-- 2. 删除model_key字段
ALTER TABLE `t_model` DROP COLUMN IF EXISTS `model_key`;

-- 3. 修改model_type字段长度，支持多个类型（逗号分隔）
ALTER TABLE `t_model` MODIFY COLUMN `model_type` VARCHAR(256) NOT NULL COMMENT '模型类型（多个类型用逗号分隔）';

-- 4. 为已有数据保持现有的model_type值不变（单一类型会自动兼容新的多选格式）

-- 注意：
-- 1. 删除model_key字段后，模型实例将不再有唯一标识限制
-- 2. model_type字段现在支持存储多个类型，用逗号分隔，如："LLM,TEXT_EMBEDDING"
-- 3. 前端和后端代码已经更新以支持模型类型多选功能
-- 4. 现有的单一类型数据会自动兼容新的多选格式