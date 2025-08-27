-- 意图模块 - 意图目录表
CREATE TABLE IF NOT EXISTS `t_intent_catalog` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `name` VARCHAR(128) NOT NULL COMMENT '目录名称',
    `code` VARCHAR(64) UNIQUE NOT NULL COMMENT '目录编码',
    `description` TEXT COMMENT '描述',
    `parent_id` BIGINT COMMENT '父目录ID',
    `sort_order` INT DEFAULT 0 COMMENT '排序',
    `creator_id` BIGINT NOT NULL COMMENT '创建者ID',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    `created_by` VARCHAR(64) COMMENT '创建者',
    `updated_by` VARCHAR(64) COMMENT '更新者',
    `created_at` BIGINT COMMENT '创建时间',
    `updated_at` BIGINT COMMENT '更新时间',
    INDEX idx_parent_id (`parent_id`),
    INDEX idx_creator_id (`creator_id`),
    INDEX idx_code (`code`),
    INDEX idx_sort_order (`sort_order`)
) COMMENT '意图目录表';

SELECT * FROM t_intent_catalog;
SELECT * FROM t_intent where catalog_id = 12;

SELECT ti.code,ti.name,tic.name as catalog_name
FROM
    t_intent as ti
    join t_intent_catalog as tic on ti.catalog_id = tic.id;