# SmartCS Web 数据库表结构文档

## 目录结构

```
sql/
├── modules/           # 模块化SQL文件
│   ├── chat/         # 聊天模块
│   ├── knowledge/    # 知识模块  
│   ├── model/        # 模型模块
│   ├── admin/        # 管理模块
│   └── core/         # 核心模块
├── migration/        # 数据库迁移脚本
├── init/            # 初始化脚本
└── README.md        # 说明文档
```

## 模块说明

### 1. 聊天模块 (Chat)
- `01_cs_user.sql` - 用户表
- `02_cs_session.sql` - 会话表  
- `03_cs_message.sql` - 消息表
  
  分库分表物理表（如启用 ShardingSphere-JDBC）：
  - `02_cs_session_shards.sql` - 会话表分表 t_cs_session_0..3（每个分库执行）
  - `03_cs_message_shards.sql` - 消息表分表 t_cs_message_0..3（每个分库执行）

### 2. 知识模块 (Knowledge)
- `01_knowledge_base.sql` - 知识库表
- `02_content.sql` - 内容表
- `03_chunk.sql` - 切片表
- `04_vector.sql` - 向量表
- `05_user_kb_rel.sql` - 用户知识库权限关系表
- `06_faq.sql` - FAQ表

### 3. 模型模块 (Model)
- `01_provider.sql` - 提供商表
- `02_model.sql` - 模型实例表
- `03_prompt_template.sql` - Prompt模板表
- `04_task.sql` - 任务表
- `05_context.sql` - 上下文表

### 4. 管理模块 (Admin)
- `01_system_config.sql` - 系统配置表

## 使用说明

### 完整初始化
使用统一初始化脚本：
```sql
source init/00_init_all_modules.sql
```

### 模块化初始化
按模块分别执行：
```sql
source modules/chat/01_cs_user.sql
source modules/chat/02_cs_session.sql  
source modules/chat/03_cs_message.sql

-- 如启用分库分表（ShardingSphere-JDBC）
-- 请在每个物理库（例如 smartcs_0 与 smartcs_1）分别执行：
source modules/chat/02_cs_session_shards.sql
source modules/chat/03_cs_message_shards.sql

-- 知识模块
source modules/knowledge/01_knowledge_base.sql
source modules/knowledge/02_content.sql
source modules/knowledge/03_chunk.sql
source modules/knowledge/04_vector.sql
source modules/knowledge/05_user_kb_rel.sql
source modules/knowledge/06_faq.sql

-- 模型模块
source modules/model/01_provider.sql
source modules/model/02_model.sql
source modules/model/03_prompt_template.sql
source modules/model/04_task.sql
source modules/model/05_context.sql

-- 管理模块
source modules/admin/01_system_config.sql
```

## 表依赖关系

```
t_model_provider (提供商)
    ↓
t_model (模型实例)
    ↓
t_model_task (模型任务)
t_model_context (模型上下文)

t_kb_knowledge_base (知识库)
    ↓
t_kb_content (内容)
    ↓
t_kb_chunk (切片)
    ↓
t_kb_vector (向量)

t_cs_user (用户)
    ↓
t_cs_session (会话)
    ↓
t_cs_message (消息)
```

## 数据对象映射

| DO类 | 表名 | 模块 |
|------|------|------|
| CsUserDO | t_cs_user | Chat |
| CsSessionDO | t_cs_session | Chat |
| CsMessageDO | t_cs_message | Chat |
| KnowledgeBaseDO | t_kb_knowledge_base | Knowledge |
| ContentDO | t_kb_content | Knowledge |
| ChunkDO | t_kb_chunk | Knowledge |
| VectorDO | t_kb_vector | Knowledge |
| UserKnowledgeBaseRelDO | t_kb_user_kb_rel | Knowledge |
| FaqDO | t_cs_faq | Knowledge |
| ProviderDO | t_model_provider | Model |
| ModelDO | t_model | Model |
| ModelPromptTemplateDO | t_model_prompt_template | Model |
| ModelTaskDO | t_model_task | Model |
| ModelTaskContextDO | t_model_context | Model |

## 命名规范

- **表名**: `t_{模块前缀}_{功能名称}`
- **字段名**: `snake_case` 格式
- **索引名**: `idx_{表名}_{字段名}` 或 `uk_{表名}_{字段名}`
- **外键名**: `fk_{表名}_{引用字段}`

## 注意事项

1. 所有表都包含基础字段：`id`, `is_deleted`, `created_by`, `updated_by`, `created_at`, `updated_at`
2. 时间戳统一使用 `BIGINT` 类型存储毫秒时间戳
3. 逻辑删除使用 `is_deleted` 字段，0=未删除，1=已删除
4. 外键约束已设置，注意表的创建顺序
5. 已优化索引配置，提高查询性能
6. 分库分表环境中，请确保每个分库都存在对应的分表（如 t_cs_session_0..3、t_cs_message_0..3），并与逻辑表结构一致；逻辑表名在应用侧保持为 `t_cs_session`、`t_cs_message`。
7. Sharding 配置示例参考：`start/src/main/resources/application-sharding.yaml`（包含绑定表、复合分片算法等）。
