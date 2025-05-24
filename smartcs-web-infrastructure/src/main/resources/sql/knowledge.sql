-- FAQ表
CREATE TABLE IF NOT EXISTS t_cs_faq
(
    id          BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    is_deleted  TINYINT(1) DEFAULT 0 COMMENT '逻辑删除标记',
    created_by  VARCHAR(64) COMMENT '创建者',
    updated_by  VARCHAR(64) COMMENT '更新者',
    created_at  BIGINT COMMENT '创建时间（毫秒时间戳）',
    updated_at  BIGINT COMMENT '更新时间（毫秒时间戳）',
    question    VARCHAR(255) NOT NULL COMMENT '问题文本',
    answer_text TEXT COMMENT '答案文本',
    hit_count   BIGINT     DEFAULT 0 COMMENT '命中次数',
    version_no  INT        DEFAULT 1 COMMENT '版本号',
    enabled     TINYINT(1) DEFAULT 1 COMMENT '是否启用',
    INDEX idx_question (question),
    FULLTEXT INDEX ft_question_answer (question, answer_text)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='常见问题FAQ表';

-- 文档表
CREATE TABLE IF NOT EXISTS t_cs_doc
(
    id         BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    is_deleted TINYINT(1) DEFAULT 0 COMMENT '逻辑删除标记',
    created_by VARCHAR(64) COMMENT '创建者',
    updated_by VARCHAR(64) COMMENT '更新者',
    created_at BIGINT COMMENT '创建时间（毫秒时间戳）',
    updated_at BIGINT COMMENT '更新时间（毫秒时间戳）',
    title      VARCHAR(255) NOT NULL COMMENT '文档标题',
    oss_url    VARCHAR(512) COMMENT 'OSS存储地址',
    file_type  VARCHAR(32) COMMENT '文件类型',
    version_no INT        DEFAULT 1 COMMENT '版本号',
    INDEX idx_title (title)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='文档表';

-- 文档段落向量表
CREATE TABLE IF NOT EXISTS t_cs_doc_embedding
(
    id           BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    is_deleted   TINYINT(1) DEFAULT 0 COMMENT '逻辑删除标记',
    created_by   VARCHAR(64) COMMENT '创建者',
    updated_by   VARCHAR(64) COMMENT '更新者',
    created_at   BIGINT COMMENT '创建时间（毫秒时间戳）',
    updated_at   BIGINT COMMENT '更新时间（毫秒时间戳）',
    doc_id       BIGINT NOT NULL COMMENT '文档ID',
    section_idx  INT    NOT NULL COMMENT '段落序号',
    content_snip TEXT COMMENT '文本片段',
    vector       BLOB COMMENT '向量数据',
    model_type   VARCHAR(50) COMMENT '模型类型',
    INDEX idx_doc_section (doc_id, section_idx)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='文档段落向量表';

CREATE TABLE IF NOT EXISTS `t_bot_prompt_template`
(
    `id`               BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    `template_key`     VARCHAR(255) COMMENT '模板标识',
    `template_content` TEXT COMMENT '模板内容',
    `is_deleted`       TINYINT DEFAULT 0 COMMENT '逻辑删除标记（0=未删除 1=已删除）',
    `created_by`       VARCHAR(255) COMMENT '创建者ID',
    `updated_by`       VARCHAR(255) COMMENT '更新者ID',
    `created_at`       BIGINT COMMENT '创建时间（毫秒时间戳）',
    `updated_at`       BIGINT COMMENT '更新时间（毫秒时间戳）'
) COMMENT ='Bot Prompt模板数据对象';

-- 插入一个基本的RAG查询模板
INSERT INTO `t_bot_prompt_template` (`is_deleted`, `created_by`, `created_at`, `updated_by`, `updated_at`,
                                     `template_key`, `template_content`)
VALUES (0, 'system', UNIX_TIMESTAMP() * 1000, 'system', UNIX_TIMESTAMP() * 1000, 'RAG_QUERY',
        '你是一个智能客服助手，根据以下知识信息回答用户问题，如果无法从知识中找到答案，请诚实地告知用户。
        历史对话：
        {{history}}

        相关知识：
        {{docs}}

        用户问题：{{question}}

        请根据以上知识和对话历史，以专业、友好的口吻回答用户问题。如果能够从知识中找到答案，请直接回答。如果知识中没有相关信息，请说明无法提供答案。不要编造信息。');