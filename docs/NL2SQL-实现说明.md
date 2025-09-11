# NL2SQL（NLP→SQL）设计与实现说明

本文档系统化说明 SmartCS 的 NL2SQL（文中也称 NPL2SQL/NLP2SQL）在 RAG 流程中的接入方式、执行链路、核心组件、配置项、安全控制与可改进点，便于工程协作、调参与排障。

## 总览

- 目标：将用户自然语言诉求转换为安全的 SQL 查询，并以检索源的方式融入 RAG。
- 位置：作为 `QueryRouter` 的一个检索源（数据库查询）与知识库/Web 搜索并行，按 LLM 路由决策选择。
- 关键能力：
  - 表结构向量化与召回（Redis 向量库）
  - 依据召回的表结构提示 LLM 生成 SQL（仅允许 SELECT）
  - SQL 解析、语法校验与危险关键字拦截
  - 置信度计算与阈值闸门（低于阈值不执行）
  - 执行结果结构化为 LangChain4j Content，附带审计元数据

## 路由与入口

- 路由注册：`QueryRouter` 在创建时按配置开启“数据库查询”检索源。
  - 文件：`smartcs-web-infrastructure/src/main/java/com/leyue/smartcs/rag/factory/RagAugmentorFactory.java`
  - 关键调用：`createQueryRouter(...)` 中当 `enableSqlQuery` 为 true 时注入 `SqlQueryContentRetriever`。
- 前端开关：`enableSqlQuery=false` 默认关闭，可在会话的 `RagComponentConfig` 里开启。
  - 文件：`smartcs-web-client/src/main/java/com/leyue/smartcs/dto/app/RagComponentConfig.java`

## 执行链路（分步）

1) 用户查询进入 SQL 检索器
- 入口：`SqlQueryContentRetriever.retrieve(Query)`
- 文件：`smartcs-web-infrastructure/src/main/java/com/leyue/smartcs/rag/content/retriever/SqlQueryContentRetriever.java`

2) NL2SQL 生成（核心逻辑）
- 调用：`nlpToSqlService.generateSql(nlpQuery, chatModelId, embeddingModelId)`
- 文件：`smartcs-web-infrastructure/src/main/java/com/leyue/smartcs/rag/database/service/NlpToSqlService.java`
- 流程：
  - a. 表结构召回：`SchemaRetrievalService.retrieveRelevantSchemas(...)`
    - 依据查询向量搜索 Redis 向量库（TopK+阈值），返回 `DatabaseTableSchema` 列表
    - 文件：`smartcs-web-infrastructure/src/main/java/com/leyue/smartcs/rag/database/service/SchemaRetrievalService.java`
  - b. 构造 LLM 提示：包含用户需求、相关表结构、表间关系等
  - c. LLM 生成 SQL：`ChatModel.chat(UserMessage.from(prompt))`
  - d. 解析 + 语法校验：从代码块/文本提取 SQL，仅允许 `SELECT ... FROM ...`
  - e. 安全校验：危险关键字黑名单（DROP/DELETE/INSERT/ALTER/EXEC/UNION/...）
  - f. 置信度计算：综合“召回相似度 + 语法有效性 + 表数量合理性 + 解释质量”

3) 置信度闸门
- 若 `confidence < min-confidence`（默认 0.6）则不执行 SQL，返回友好提示。
- 文件：`smartcs-web-infrastructure/src/main/java/com/leyue/smartcs/rag/content/retriever/SqlQueryContentRetriever.java`

4) 安全执行与结果包装
- 执行：`JdbcTemplate.queryForList(generatedSql)`（仅在通过校验与置信度闸门后）
- 文本化：限制输出前 10 行，构建 `Content` 返回
- 元数据：注入 `original_query/generated_sql/confidence/used_tables/...` 便于审计追踪

## 表结构向量化与召回

### 向量化入库（离线/增量）
- 服务：`TableSchemaVectorizationService`
- 文件：`smartcs-web-infrastructure/src/main/java/com/leyue/smartcs/rag/database/service/TableSchemaVectorizationService.java`
- 要点：
  - 读取当前库 `information_schema`，提取字段/索引/外键/统计/注释等，拼装 `DatabaseTableSchema`
  - 以 `generateVectorizationText()` 生成语义文本，调用 `EmbeddingModel` 生成向量
  - 写入向量库并缓存 schema（元数据含 `table_name/schema_name/table_description/...`）

### 语义召回（在线）
- 服务：`SchemaRetrievalService`
- 文件：`smartcs-web-infrastructure/src/main/java/com/leyue/smartcs/rag/database/service/SchemaRetrievalService.java`
- 要点：
  - 将查询向量化 → `EmbeddingStore.search`（TopK + minScore）→ 命中 item 的 `table_name` 反查缓存 schema
  - 返回 `SchemaRetrievalResult`（包含匹配分、匹配原因、表名/结构等）

## 组件协作图（文字版）

- Router（LM 路由）
  - → SqlQueryContentRetriever（检索器）
    - → NlpToSqlService（生成 SQL）
      - → SchemaRetrievalService（向量召回）
        - → EmbeddingStore（Redis 向量存储）
      - → ChatModel（LLM 生成）
      - → 解析/校验/置信度评估
    - → JdbcTemplate（执行 SELECT）
    - → Content（结果 + 审计元数据）

## 配置项与默认值

- 总配置：`start/src/main/resources/application.yaml`
  - `smartcs.nlp2sql.enable-nlp-mode`（默认 true）：启用 NL2SQL 流程
  - `smartcs.nlp2sql.min-confidence`（默认 0.6）：执行闸门阈值
  - `smartcs.nlp2sql.similarity-threshold`（默认 0.6）：schema 召回相似度阈值
  - `smartcs.nlp2sql.max-tables-for-sql`（默认 5）：拼装 Prompt 的最大表数
  - `smartcs.nlp2sql.enable-complex-queries`（默认 true）：允许复杂查询（可按需限制）
  - `smartcs.nlp2sql.schema-index-prefix`、`schema-search-max-results`：schema 索引前缀与召回数量
- 向量库：Redis EmbeddingStore 见
  - `smartcs-web-infrastructure/src/main/java/com/leyue/smartcs/rag/config/RedisEmbeddingStoreConfig.java`
  - 维度默认 1536（与 `langchain4j.embedding-store.dimension` 对齐）
- 路由开关：`RagComponentConfig.QueryRouterConfig.enableSqlQuery`
  - `smartcs-web-client/src/main/java/com/leyue/smartcs/dto/app/RagComponentConfig.java`

## 安全与稳态保障

- 仅允许 `SELECT`：解析后用正则校验 `^\s*SELECT\s+.*?\s+FROM\s+.*`
- 危险关键字黑名单：`DROP/DELETE/INSERT/UPDATE/ALTER/CREATE/TRUNCATE/EXEC/UNION/INFORMATION_SCHEMA` 等
- 低置信度不执行：`confidence < min-confidence` 直接提示用户，附生成 SQL 预览与置信度
- 错误分支：
  - schema 召回为空 → 友好报错
  - 解析/语法校验失败 → 报错并附原因
  - 安全校验失败 → 报错并附原因
- 元数据审计：
  - 每条返回 `Content` 附带 `source/original_query/generated_sql/confidence/used_tables/result_count/sql_explanation` 等，用于追踪与复现

## 关键代码位置（便于排障）

- Router 装配：`smartcs-web-infrastructure/src/main/java/com/leyue/smartcs/rag/factory/RagAugmentorFactory.java`
- 检索器：`smartcs-web-infrastructure/src/main/java/com/leyue/smartcs/rag/content/retriever/SqlQueryContentRetriever.java`
- NL2SQL 服务：`smartcs-web-infrastructure/src/main/java/com/leyue/smartcs/rag/database/service/NlpToSqlService.java`
- Schema 召回：`smartcs-web-infrastructure/src/main/java/com/leyue/smartcs/rag/database/service/SchemaRetrievalService.java`
- Schema 向量化：`smartcs-web-infrastructure/src/main/java/com/leyue/smartcs/rag/database/service/TableSchemaVectorizationService.java`
- Redis 向量库配置：`smartcs-web-infrastructure/src/main/java/com/leyue/smartcs/rag/config/RedisEmbeddingStoreConfig.java`
- 应用配置：`start/src/main/resources/application.yaml`

## 已知空白与改进建议

- 直通 SQL 支持：`SqlQueryContentRetriever` 已定义 `SELECT_PATTERN/DANGEROUS_KEYWORDS`，可增加“当输入本身是安全 SELECT 时直接执行”的快捷分支（目前统一走 NL2SQL）。
- 二次防御：在 JdbcTemplate 执行前再进行一次轻量校验（regex + 黑名单），形成双层保险。
- 数据最小化：增加列白名单/敏感字段脱敏；建议通过只读视图暴露数据，或在执行层做白名单过滤。
- Prompt 体积控制：对超大 schema 文本进行字段筛选/摘要，仅保留关键列与约束，降低 LLM 成本。
- 结果分页：当前仅格式化前 10 行，可扩展为分页查询或 LIMIT 控制，并通过元数据返回分页提示。
- 缓存与热点：对常见 NL2SQL 请求与生成 SQL 做缓存（结合语义哈希），下降延迟与成本。

## 小结

NL2SQL 以“schema 语义召回 → LLM 生成 → 解析/安全/置信度 → 安全执行 → 结果审计”的闭环方式集成到 RAG Router 中，既提升了 Agent 对结构化数据的覆盖，又在工程上确保稳态与可观测。按本文档位置与配置，可快速完成问题定位与场景调参。

