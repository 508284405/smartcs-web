-- 知识模块新表结构DDL脚本

-- 1. 知识库表（租户级隔离）
CREATE TABLE `t_kb_knowledge_base` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `name` VARCHAR(128) NOT NULL COMMENT '知识库名称',
  `code` VARCHAR(64) UNIQUE NOT NULL COMMENT '知识库唯一编码',
  `description` TEXT COMMENT '描述信息',
  `owner_id` BIGINT NOT NULL COMMENT '创建者ID',
  `visibility` VARCHAR(16) DEFAULT 'private' COMMENT 'public/private',
  `is_deleted` TINYINT DEFAULT 0,
  `created_by` VARCHAR(64) COMMENT '创建者',
  `updated_by` VARCHAR(64) COMMENT '更新者',
  `created_at` BIGINT COMMENT '创建时间',
  `updated_at` BIGINT COMMENT '更新时间',
  INDEX idx_owner_id (`owner_id`),
  INDEX idx_visibility (`visibility`),
  INDEX idx_code (`code`)
) COMMENT '知识库表';

-- 2. 知识内容原始数据表（文档/音频/视频）
CREATE TABLE `t_kb_content` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `knowledge_base_id` BIGINT NOT NULL COMMENT '所属知识库ID',
  `title` VARCHAR(256) NOT NULL COMMENT '标题',
  `content_type` VARCHAR(32) NOT NULL COMMENT 'document/audio/video',
  `file_url` VARCHAR(512) COMMENT '原始文件地址',
  `file_type` VARCHAR(256) COMMENT '文件类型',
  `text_extracted` TEXT COMMENT '提取后的原始文本',
  `status` VARCHAR(32) DEFAULT 'uploaded' COMMENT 'uploaded/parsed/vectorized',
  `is_deleted` TINYINT DEFAULT 0,
  `created_by` VARCHAR(64) COMMENT '创建者',
  `updated_by` VARCHAR(64) COMMENT '更新者',
  `created_at` BIGINT COMMENT '创建时间',
  `updated_at` BIGINT COMMENT '更新时间',
  INDEX idx_knowledge_base_id (`knowledge_base_id`),
  INDEX idx_content_type (`content_type`),
  INDEX idx_status (`status`)
) COMMENT '知识内容表';

-- 3. 内容切片表（用于向量化）
CREATE TABLE `t_kb_chunk` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `content_id` BIGINT NOT NULL COMMENT '内容ID',
  `chunk_index` INT NOT NULL COMMENT '段落序号',
  `text` TEXT NOT NULL COMMENT '该段文本内容',
  `token_size` INT DEFAULT 0 COMMENT '切片token数',
  `vector_id` VARCHAR(64) COMMENT '向量数据库中的ID（如Milvus主键）',
  `metadata` JSON COMMENT '附加元信息，如页码、起止时间、原始位置等',
  `is_deleted` TINYINT DEFAULT 0,
  `created_by` VARCHAR(64) COMMENT '创建者',
  `updated_by` VARCHAR(64) COMMENT '更新者',
  `created_at` BIGINT COMMENT '创建时间',
  `updated_at` BIGINT COMMENT '更新时间',
  INDEX idx_content_id (`content_id`),
  INDEX idx_vector_id (`vector_id`)
) COMMENT '内容切片表';

-- 4. 向量表（可选：本地存储或做索引记录）
CREATE TABLE `t_kb_vector` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `chunk_id` BIGINT NOT NULL COMMENT '切片ID',
  `embedding` BLOB COMMENT '向量数据，float[]序列化后存储',
  `dim` INT DEFAULT 768 COMMENT '维度大小',
  `provider` VARCHAR(64) DEFAULT 'bge' COMMENT 'embedding提供方，如openai/bge',
  `is_deleted` TINYINT DEFAULT 0,
  `created_by` VARCHAR(64) COMMENT '创建者',
  `updated_by` VARCHAR(64) COMMENT '更新者',
  `created_at` BIGINT COMMENT '创建时间',
  `updated_at` BIGINT COMMENT '更新时间',
  UNIQUE INDEX idx_chunk_id (`chunk_id`)
) COMMENT '向量表';

-- 5. 用户-知识库权限表
CREATE TABLE `t_kb_user_kb_rel` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `knowledge_base_id` BIGINT NOT NULL COMMENT '知识库ID',
  `role` VARCHAR(32) DEFAULT 'reader' COMMENT 'reader/writer/admin',
  `is_deleted` TINYINT DEFAULT 0,
  `created_by` VARCHAR(64) COMMENT '创建者',
  `updated_by` VARCHAR(64) COMMENT '更新者',
  `created_at` BIGINT COMMENT '创建时间',
  `updated_at` BIGINT COMMENT '更新时间',
  UNIQUE INDEX idx_user_knowledge_base (`user_id`, `knowledge_base_id`)
) COMMENT '用户知识库权限关系表'; 