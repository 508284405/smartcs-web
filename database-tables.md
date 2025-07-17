# SmartCS 数据库表结构文档

## 目录

- [1. 知识库模块](#1-知识库模块)
  - [1.1 知识库表 (t_kb_knowledge_base)](#11-知识库表-t_kb_knowledge_base)
  - [1.2 知识内容表 (t_kb_content)](#12-知识内容表-t_kb_content)
  - [1.3 内容切片表 (t_kb_chunk)](#13-内容切片表-t_kb_chunk)
  - [1.4 向量表 (t_kb_vector)](#14-向量表-t_kb_vector)
  - [1.5 用户知识库权限关系表 (t_kb_user_kb_rel)](#15-用户知识库权限关系表-t_kb_user_kb_rel)
- [2. 聊天模块](#2-聊天模块)
  - [2.1 用户表 (t_cs_user)](#21-用户表-t_cs_user)
  - [2.2 消息表 (t_cs_message)](#22-消息表-t_cs_message)
  - [2.3 会话表 (t_cs_session)](#23-会话表-t_cs_session)
- [3. 机器人模块](#3-机器人模块)
  - [3.1 机器人配置表 (t_cs_bot_profile)](#31-机器人配置表-t_cs_bot_profile)
  - [3.2 Bot Prompt模板表 (t_bot_prompt_template)](#32-bot-prompt模板表-t_bot_prompt_template)
- [4. FAQ模块](#4-faq模块)
  - [4.1 常见问题FAQ表 (t_cs_faq)](#41-常见问题faq表-t_cs_faq)

## 1. 知识库模块

### 1.1 知识库表 (t_kb_knowledge_base)

**表说明**: 存储知识库基本信息，租户级隔离

| 字段名 | 数据类型 | 约束 | 默认值 | 说明 |
| ------ | -------- | ---- | ------ | ---- |
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | | 主键ID |
| name | VARCHAR(128) | NOT NULL | | 知识库名称 |
| code | VARCHAR(64) | UNIQUE, NOT NULL | | 知识库唯一编码 |
| description | TEXT | | | 描述信息 |
| owner_id | BIGINT | NOT NULL | | 创建者ID |
| visibility | VARCHAR(16) | | 'private' | 可见性 public/private |
| is_deleted | TINYINT | | 0 | 是否删除 |
| created_by | VARCHAR(64) | | | 创建者 |
| updated_by | VARCHAR(64) | | | 更新者 |
| created_at | BIGINT | | | 创建时间 |
| updated_at | BIGINT | | | 更新时间 |

**索引**:
- idx_owner_id (owner_id)
- idx_visibility (visibility)
- idx_code (code)

### 1.2 知识内容表 (t_kb_content)

**表说明**: 存储知识内容原始数据，如文档/音频/视频

| 字段名 | 数据类型 | 约束 | 默认值 | 说明 |
| ------ | -------- | ---- | ------ | ---- |
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | | 主键ID |
| knowledge_base_id | BIGINT | NOT NULL | | 所属知识库ID |
| title | VARCHAR(256) | NOT NULL | | 标题 |
| content_type | VARCHAR(32) | NOT NULL | | 内容类型 document/audio/video |
| file_url | VARCHAR(512) | | | 原始文件地址 |
| file_type | VARCHAR(256) | | | 文件类型 |
| text_extracted | TEXT | | | 提取后的原始文本 |
| status | VARCHAR(32) | | 'uploaded' | 状态 uploaded/parsed/vectorized |
| segment_mode | VARCHAR(32) | | 'general' | 分段模式 general/parent_child |
| char_count | BIGINT | | 0 | 字符数 |
| recall_count | BIGINT | | 0 | 召回次数 |
| is_deleted | TINYINT | | 0 | 是否删除 |
| created_by | VARCHAR(64) | | | 创建者 |
| updated_by | VARCHAR(64) | | | 更新者 |
| created_at | BIGINT | | | 创建时间 |
| updated_at | BIGINT | | | 更新时间 |

**索引**:
- idx_knowledge_base_id (knowledge_base_id)
- idx_content_type (content_type)
- idx_status (status)
- idx_segment_mode (segment_mode)
- idx_char_count (char_count)
- idx_recall_count (recall_count)

### 1.3 内容切片表 (t_kb_chunk)

**表说明**: 存储内容切片数据，用于向量化

| 字段名 | 数据类型 | 约束 | 默认值 | 说明 |
| ------ | -------- | ---- | ------ | ---- |
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | | 主键ID |
| content_id | BIGINT | NOT NULL | | 内容ID |
| chunk_index | INT | NOT NULL | | 段落序号 |
| text | TEXT | NOT NULL | | 该段文本内容 |
| token_size | INT | | 0 | 切片token数 |
| vector_id | VARCHAR(64) | | | 向量数据库中的ID（如Milvus主键） |
| metadata | JSON | | | 附加元信息，如页码、起止时间、原始位置等 |
| is_deleted | TINYINT | | 0 | 是否删除 |
| created_by | VARCHAR(64) | | | 创建者 |
| updated_by | VARCHAR(64) | | | 更新者 |
| created_at | BIGINT | | | 创建时间 |
| updated_at | BIGINT | | | 更新时间 |

**索引**:
- idx_content_id (content_id)
- idx_vector_id (vector_id)

### 1.4 向量表 (t_kb_vector)

**表说明**: 存储向量数据，可选：本地存储或做索引记录

| 字段名 | 数据类型 | 约束 | 默认值 | 说明 |
| ------ | -------- | ---- | ------ | ---- |
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | | 主键ID |
| chunk_id | BIGINT | NOT NULL | | 切片ID |
| embedding | BLOB | | | 向量数据，float[]序列化后存储 |
| dim | INT | | 768 | 维度大小 |
| provider | VARCHAR(64) | | 'bge' | embedding提供方，如openai/bge |
| is_deleted | TINYINT | | 0 | 是否删除 |
| created_by | VARCHAR(64) | | | 创建者 |
| updated_by | VARCHAR(64) | | | 更新者 |
| created_at | BIGINT | | | 创建时间 |
| updated_at | BIGINT | | | 更新时间 |

**索引**:
- idx_chunk_id (chunk_id) - UNIQUE

### 1.5 用户知识库权限关系表 (t_kb_user_kb_rel)

**表说明**: 存储用户与知识库的权限关系

| 字段名 | 数据类型 | 约束 | 默认值 | 说明 |
| ------ | -------- | ---- | ------ | ---- |
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | | 主键ID |
| user_id | BIGINT | NOT NULL | | 用户ID |
| knowledge_base_id | BIGINT | NOT NULL | | 知识库ID |
| role | VARCHAR(32) | | 'reader' | 角色 reader/writer/admin |
| is_deleted | TINYINT | | 0 | 是否删除 |
| created_by | VARCHAR(64) | | | 创建者 |
| updated_by | VARCHAR(64) | | | 更新者 |
| created_at | BIGINT | | | 创建时间 |
| updated_at | BIGINT | | | 更新时间 |

**索引**:
- idx_user_knowledge_base (user_id, knowledge_base_id) - UNIQUE

## 2. 聊天模块

### 2.1 用户表 (t_cs_user)

**表说明**: 存储用户信息

| 字段名 | 数据类型 | 约束 | 默认值 | 说明 |
| ------ | -------- | ---- | ------ | ---- |
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | | 主键ID |
| user_id | BIGINT | | | 用户ID |
| nick_name | VARCHAR(255) | | | 昵称 |
| avatar_url | VARCHAR(255) | | | 头像URL |
| phone_mask | VARCHAR(255) | | | 手机号掩码 |
| user_type | INT | | | 用户类型 0=消费者 1=客服 |
| status | INT | | | 状态 1=正常 0=禁用 |
| is_deleted | INT | | 0 | 逻辑删除 |
| created_by | VARCHAR(255) | | | 创建人 |
| updated_by | VARCHAR(255) | | | 更新人 |
| created_at | BIGINT | | | 创建时间 |
| updated_at | BIGINT | | | 更新时间 |

### 2.2 消息表 (t_cs_message)

**表说明**: 存储消息数据

| 字段名 | 数据类型 | 约束 | 默认值 | 说明 |
| ------ | -------- | ---- | ------ | ---- |
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | | 主键ID |
| msg_id | BIGINT | | | 消息ID |
| session_id | BIGINT | | | 会话ID |
| sender_id | BIGINT | | | 发送者ID |
| sender_role | INT | | | 发送者角色 0=用户 1=客服 2=机器人 |
| msg_type | INT | | | 消息类型 0=text 1=image 2=order_card 3=system |
| content | VARCHAR(255) | | | 消息内容，JSON格式存储富文本 |
| at_list | JSON | | | @提及的用户列表 |
| is_deleted | INT | | 0 | 逻辑删除 |
| created_by | VARCHAR(255) | | | 创建人 |
| updated_by | VARCHAR(255) | | | 更新人 |
| created_at | BIGINT | | | 创建时间 |
| updated_at | BIGINT | | | 更新时间 |

### 2.3 会话表 (t_cs_session)

**表说明**: 存储会话数据

| 字段名 | 数据类型 | 约束 | 默认值 | 说明 |
| ------ | -------- | ---- | ------ | ---- |
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | | 主键ID |
| session_id | BIGINT | | | 会话ID |
| session_name | VARCHAR(50) | | | 会话名称 |
| customer_id | BIGINT | | | 客户ID |
| agent_id | BIGINT | | | 客服ID |
| session_state | INT | | | 会话状态 0=排队 1=进行中 2=已结束 |
| last_msg_time | BIGINT | | | 最后消息时间 |
| is_deleted | INT | | 0 | 逻辑删除 |
| created_by | VARCHAR(255) | | | 创建人 |
| updated_by | VARCHAR(255) | | | 更新人 |
| created_at | BIGINT | | | 创建时间 |
| updated_at | BIGINT | | | 更新时间 |

## 3. 机器人模块

### 3.1 机器人配置表 (t_cs_bot_profile)

**表说明**: 存储机器人配置信息，仅 Bot-Service 使用

| 字段名 | 数据类型 | 约束 | 默认值 | 说明 |
| ------ | -------- | ---- | ------ | ---- |
| id | BIGINT | PRIMARY KEY | | 主键，同时对应 cs_agent.agent_id |
| bot_name | VARCHAR(128) | NOT NULL | | 机器人名称 |
| model_name | VARCHAR(128) | NOT NULL | | 使用的 LLM / 模型标识，如 gpt-4o、bge-large |
| prompt_key | VARCHAR(64) | NOT NULL | | 默认 Prompt 模板 key，关联 bot_prompt_template |
| remark | VARCHAR(500) | | | 备注信息 |
| vendor | VARCHAR(32) | NOT NULL | | 模型厂商，如openai、deepseek等 |
| model_type | VARCHAR(32) | NOT NULL | | 模型类型，如chat、embedding、image、audio等 |
| api_key | VARCHAR(512) | NOT NULL | | API密钥 |
| base_url | VARCHAR(256) | NOT NULL | | API基础URL |
| options | JSON | | | 模型具体配置（JSON格式），如具体模型4o-mini等 |
| enabled | TINYINT(1) | NOT NULL | 1 | 是否启用 |
| is_deleted | TINYINT(1) | NOT NULL | 0 | 是否删除 |
| created_by | VARCHAR(64) | | | 创建者 |
| updated_by | VARCHAR(64) | | | 更新者 |
| created_at | BIGINT | NOT NULL | | 创建时间 ms |
| updated_at | BIGINT | NOT NULL | | 更新时间 ms |

**表引擎**: InnoDB
**字符集**: utf8mb4

### 3.2 Bot Prompt模板表 (t_bot_prompt_template)

**表说明**: 存储Bot Prompt模板数据

| 字段名 | 数据类型 | 约束 | 默认值 | 说明 |
| ------ | -------- | ---- | ------ | ---- |
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | | 主键ID |
| template_key | VARCHAR(255) | | | 模板标识 |
| template_content | TEXT | | | 模板内容 |
| is_deleted | TINYINT | | 0 | 逻辑删除标记（0=未删除 1=已删除） |
| created_by | VARCHAR(255) | | | 创建者ID |
| updated_by | VARCHAR(255) | | | 更新者ID |
| created_at | BIGINT | | | 创建时间（毫秒时间戳） |
| updated_at | BIGINT | | | 更新时间（毫秒时间戳） |

**索引**:
- uk_template_key (template_key) - UNIQUE

**表引擎**: InnoDB
**字符集**: utf8mb4

## 4. FAQ模块

### 4.1 常见问题FAQ表 (t_cs_faq)

**表说明**: 存储常见问题FAQ数据

| 字段名 | 数据类型 | 约束 | 默认值 | 说明 |
| ------ | -------- | ---- | ------ | ---- |
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | | 主键ID |
| is_deleted | TINYINT(1) | | 0 | 逻辑删除标记 |
| created_by | VARCHAR(64) | | | 创建者 |
| updated_by | VARCHAR(64) | | | 更新者 |
| created_at | BIGINT | | | 创建时间（毫秒时间戳） |
| updated_at | BIGINT | | | 更新时间（毫秒时间戳） |
| question | VARCHAR(255) | NOT NULL | | 问题文本 |
| answer_text | TEXT | | | 答案文本 |
| hit_count | BIGINT | | 0 | 命中次数 |
| version_no | INT | | 1 | 版本号 |
| enabled | TINYINT(1) | | 1 | 是否启用 |

**索引**:
- idx_question (question)
- ft_question_answer (question, answer_text) - FULLTEXT

**表引擎**: InnoDB
**字符集**: utf8mb4

## 表关系

1. **知识库模块表关系**:
   - t_kb_knowledge_base (1) ---> (n) t_kb_content: 一个知识库包含多个知识内容
   - t_kb_content (1) ---> (n) t_kb_chunk: 一个知识内容被切分为多个内容切片
   - t_kb_chunk (1) ---> (1) t_kb_vector: 一个内容切片对应一个向量
   - t_kb_knowledge_base (n) <---> (m) 用户: 多对多关系，通过t_kb_user_kb_rel表关联

2. **聊天模块表关系**:
   - t_cs_user (1) ---> (n) t_cs_session: 一个用户可以有多个会话
   - t_cs_session (1) ---> (n) t_cs_message: 一个会话包含多条消息

3. **机器人模块表关系**:
   - t_cs_bot_profile (n) ---> (1) t_bot_prompt_template: 多个机器人可以使用同一个Prompt模板