-- 知识模块数据迁移脚本
-- 从旧的 t_cs_doc, t_cs_doc_embedding, t_cs_faq 迁移到新的知识库架构

-- 1. 创建默认知识库（用于迁移现有数据）
INSERT INTO `t_kb_knowledge_base` (
    `name`, 
    `code`, 
    `description`, 
    `owner_id`, 
    `visibility`, 
    `created_by`, 
    `created_at`, 
    `updated_at`
) VALUES (
    '默认知识库', 
    'default_kb', 
    '系统默认知识库，用于迁移历史数据', 
    1, 
    'public', 
    'system', 
    UNIX_TIMESTAMP() * 1000, 
    UNIX_TIMESTAMP() * 1000
);

-- 获取默认知识库ID（假设为1，实际使用时需要动态获取）
SET @default_kb_id = LAST_INSERT_ID();

-- 2. 迁移文档数据到内容表
INSERT INTO `t_kb_content` (
    `knowledge_base_id`,
    `title`,
    `content_type`,
    `file_url`,
    `status`,
    `created_by`,
    `created_at`,
    `updated_at`
)
SELECT 
    @default_kb_id,
    COALESCE(d.title, '未命名文档'),
    'document',
    d.oss_url,
    'uploaded',
    COALESCE(d.created_by, 'system'),
    COALESCE(d.created_at, UNIX_TIMESTAMP() * 1000),
    COALESCE(d.updated_at, UNIX_TIMESTAMP() * 1000)
FROM `t_cs_doc` d
WHERE d.is_deleted = 0;

-- 3. 迁移向量数据到切片表和向量表
-- 首先创建切片记录
INSERT INTO `t_kb_chunk` (
    `content_id`,
    `chunk_index`,
    `text`,
    `token_size`,
    `created_by`,
    `created_at`,
    `updated_at`
)
SELECT 
    c.id,
    0, -- 假设每个文档只有一个切片
    COALESCE(e.text_content, ''),
    COALESCE(LENGTH(e.text_content), 0),
    COALESCE(e.created_by, 'system'),
    COALESCE(e.created_at, UNIX_TIMESTAMP() * 1000),
    COALESCE(e.updated_at, UNIX_TIMESTAMP() * 1000)
FROM `t_cs_doc_embedding` e
JOIN `t_kb_content` c ON c.file_url = (
    SELECT d.oss_url FROM `t_cs_doc` d WHERE d.id = e.doc_id AND d.is_deleted = 0
)
WHERE e.is_deleted = 0;

-- 然后创建向量记录
INSERT INTO `t_kb_vector` (
    `chunk_id`,
    `embedding`,
    `dim`,
    `provider`,
    `created_by`,
    `created_at`,
    `updated_at`
)
SELECT 
    ch.id,
    e.embedding_data,
    COALESCE(e.vector_dim, 768),
    COALESCE(e.provider, 'unknown'),
    COALESCE(e.created_by, 'system'),
    COALESCE(e.created_at, UNIX_TIMESTAMP() * 1000),
    COALESCE(e.updated_at, UNIX_TIMESTAMP() * 1000)
FROM `t_cs_doc_embedding` e
JOIN `t_cs_doc` d ON d.id = e.doc_id AND d.is_deleted = 0
JOIN `t_kb_content` c ON c.file_url = d.oss_url
JOIN `t_kb_chunk` ch ON ch.content_id = c.id
WHERE e.is_deleted = 0;

-- 4. 更新内容状态为已向量化
UPDATE `t_kb_content` c
SET c.status = 'vectorized'
WHERE EXISTS (
    SELECT 1 FROM `t_kb_chunk` ch 
    JOIN `t_kb_vector` v ON v.chunk_id = ch.id 
    WHERE ch.content_id = c.id
);

-- 5. 为默认知识库创建管理员权限（假设用户ID为1）
INSERT INTO `t_kb_user_kb_rel` (
    `user_id`,
    `knowledge_base_id`,
    `role`,
    `created_by`,
    `created_at`,
    `updated_at`
) VALUES (
    1,
    @default_kb_id,
    'admin',
    'system',
    UNIX_TIMESTAMP() * 1000,
    UNIX_TIMESTAMP() * 1000
);

-- 注意：FAQ数据保持不变，继续使用原有的 t_cs_faq 表
-- 迁移完成后，可以考虑备份并删除旧表：
-- RENAME TABLE t_cs_doc TO t_cs_doc_backup;
-- RENAME TABLE t_cs_doc_embedding TO t_cs_doc_embedding_backup; 