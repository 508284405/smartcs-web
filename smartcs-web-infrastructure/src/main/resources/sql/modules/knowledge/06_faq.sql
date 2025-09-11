-- 知识模块 - FAQ表
CREATE TABLE IF NOT EXISTS `t_cs_faq`
(
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    `question` VARCHAR(255) NOT NULL COMMENT '问题文本',
    `answer_text` TEXT COMMENT '答案文本',
    `hit_count` BIGINT DEFAULT 0 COMMENT '命中次数',
    `version_no` INT DEFAULT 1 COMMENT '版本号',
    `enabled` TINYINT(1) DEFAULT 1 COMMENT '是否启用',
    `is_deleted` TINYINT(1) DEFAULT 0 COMMENT '逻辑删除标记',
    `created_by` VARCHAR(64) COMMENT '创建者',
    `updated_by` VARCHAR(64) COMMENT '更新者',
    `created_at` BIGINT COMMENT '创建时间（毫秒时间戳）',
    `updated_at` BIGINT COMMENT '更新时间（毫秒时间戳）',
    INDEX idx_question (question),
    FULLTEXT INDEX ft_question_answer (question, answer_text)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='常见问题FAQ表';