# SmartCS LTM 架构与 RAG 集成说明

## 文档范围
本文件基于 2025-02 版本的代码库，系统化梳理 SmartCS 长期记忆（Long-term Memory，LTM）在各分层中的职责、数据流与运行机制，并说明其与检索增强生成（RAG）的组合方式。文档定位为工程、测试与运维团队的架构参考，便于后续扩展、排错与治理。

## 分层拓扑
SmartCS 采用 DDD 风格的模块划分，LTM 能力贯穿所有层次：

| 分层 | 对应模块 | 关键职责 | 代表类 |
|------|----------|----------|--------|
| Client（DTO/API 合约） | `smartcs-web-client` | 对外/前端暴露的记忆查询与管理 DTO、OpenAPI 定义。 | `client/ltm/dto/memory/*` |
| Application（应用服务） | `smartcs-web-app` | 编排控制器请求，调用领域服务/网关完成查询、命令。 | `app/ltm/executor/MemoryQueryExe`、`MemoryManagementCmdExe` |
| Domain（领域） | `smartcs-web-domain` | 领域实体、网关接口、`LTMDomainService` 契约。 | `domain/ltm/domainservice/LTMDomainService.java`、`domain/ltm/entity/*`、`domain/ltm/gateway/*` |
| Infrastructure（基础设施） | `smartcs-web-infrastructure` | 网关实现、领域服务实现、记忆分析/巩固任务、安全组件、与 RAG 的胶水层。 | `ltm/service/LTMDomainServiceImpl.java`、`ltm/service/MemoryFormationService.java`、`ltm/service/MemoryConsolidationService.java`、`ltm/gatewayimpl/*`、`rag/memory/LTMEnhancedRedisChatMemoryStore.java`、`rag/retriever/LTMEnhancedContentRetriever.java` |
| Adapter（适配器） | `smartcs-web-adapter` | REST/SSE 等接口层，向业务系统开放 LTM 管理能力。 | `web/ltm/LTMMemoryController.java` |
| Bootstrap（启动配置） | `start` | Spring Boot 启动入口、`application.yaml` 中的 LTM/RAG 配置。 | — |

## 数据模型与持久化
- **数据库表设计**：采用 MyBatis-Plus 映射 `t_ltm_episodic_memory`、`t_ltm_semantic_memory`、`t_ltm_procedural_memory` 三张表，分别承载情景、语义、程序性记忆。
- **领域实体**：位于 `domain/ltm/entity/*`，封装记忆强化、遗忘、触发条件匹配等业务方法。
- **数据对象与转换**：`ltm/dataobject/*` 与领域实体之间通过 `ltm/convertor/LTMConvertor` 或专用转换逻辑互转。
- **领域网关**：`domain/ltm/gateway/*` 定义丰富的查询接口（重要度区间、语义检索、批量巩固游标等），在 `ltm/gatewayimpl/*` 中用 MyBatis-Plus 落地。当前向量检索 API 尚未接通 ANN 引擎，退化为基于 SQL 的排序过滤，但接口已预留。
- **批任务支撑**：`ltm/mapper/EpisodicMemoryMapper.xml` 额外提供待巩固用户分页 SQL，供批量任务游标式读取。

## 记忆生命周期
### 1. 采集与形成（Formation）
1. **对话采集**：短期对话记忆由 `rag/memory/FaultTolerantRedisChatMemoryStore` 存储在 Redis；当 `smartcs.ai.ltm.retrieval.enabled=true` 且 `smartcs.ai.ltm.context.chat-store.enabled=true` 时，`ChatMemoryStoreConfig` 会包装为 `LTMEnhancedRedisChatMemoryStore`，在 `getMessages`/`updateMessages` 入口挂载 LTM 逻辑。
2. **重要性评估**：`ltm/service/MemoryFormationService#doProcessMemoryFormation` 调用 `MemoryAnalyzer`，先用正则/启发式规则评估，再可选调用 LangChain4j `LanguageModel` 获得更精细打分。
3. **记忆落库**：通过 `EpisodicMemoryGatewayImpl#save` 写入情景记忆。若重要度 ≥ 阈值（默认 0.7）并开启对应开关，会进一步抽取语义记忆或学习程序性记忆。
4. **异步化**：`MemoryFormationService#processMemoryFormation` 支持异步提交，目前使用 `CompletableFuture.runAsync`（ForkJoinPool）。若需要更可控的线程池，可切换到 `ltm/config/LTMAsyncConfig` 暴露的 `ltmTaskExecutor` 配合 `TracingSupport` 传播 MDC。
5. **审计**：所有形成请求都会调用 `ltm/security/LTMAuditLogger` 记录审计事件，默认写入应用日志。

### 2. 巩固、强化与遗忘
- **批处理巩固**：`ltm/batch/MemoryConsolidationJobConfig` + `MemoryConsolidationJobLauncher` 定义了 Spring Batch 作业，根据 `smartcs.ai.ltm.consolidation.schedule` 的 CRON 触发。`ConsolidationUserItemReader` 分批拉取存在高重要度记忆的用户，逐个调用 `MemoryConsolidationService`：
  - 使用轻量 LLM 提示对情景记忆聚类并生成统一知识描述；
  - 将高价值情景记忆转化为语义或程序性记忆；
  - 批量更新巩固状态与置信度。
- **领域服务入口**：`LTMDomainServiceImpl` 暴露 `consolidateMemories`、`reinforceMemory`、`applyForgetting`、`learnUserPreference` 等能力，并在缺失网关实现时优雅降级（`@Autowired(required = false)`）。
- **遗忘策略**：`applyForgetting` 依据 `smartcs.ai.ltm.user-defaults.episodic-retention-days`（默认 90 天）清理久未访问的情景记忆，并调用 `SemanticMemoryGateway#applyDecayToAll`、`ProceduralMemoryGateway#deleteInactiveMemories` 实现语义衰减与习惯归档。

### 3. 检索与个性化
- **记忆检索**：`LTMDomainServiceImpl#retrieveMemoryContext` 按向量检索 → 关键词 → 重要度/时间窗口的顺序组合情景结果，并补充语义记忆（概念模糊匹配/置信度排序）与程序性记忆（活跃偏好）。
- **上下文注入**：`LTMEnhancedRedisChatMemoryStore#getMessages` 将检索结果组织成额外的 `SystemMessage`，包含：历史片段、知识概览、偏好提示，插入原消息序列前部而不破坏真实历史。
- **响应个性化**：`LTMDomainServiceImpl#personalizeResponse` 基于程序性记忆中的响应风格/偏好触发器对模型生成内容做后处理（如追加“更详细”“强调技术细节”等提示）。

### 4. 运维关注点
- 记忆管理接口由 `web/ltm/LTMMemoryController` 提供，支持摘要、分页查询、搜索、删除、手动调节重要度/状态等；请在接入 BFF 或管理后台前补齐鉴权与审计链路。
- 批处理、异步任务均依赖 Redis、关系型数据库与 LangChain4j 配置的 LLM/Embedding 模型，部署时需同步校验这些外部依赖。

## RAG 集成路径
### 对话记忆增强（Chat Memory Augmentation）
- **启用条件**：`smartcs.ai.ltm.retrieval.enabled=true` 且 `smartcs.ai.ltm.context.chat-store.enabled=true`。
- **消息更新**：`updateMessages` 会截取最近 4 条对话构造 `MemoryFormationRequest`，异步调用 `LTMDomainService#formMemory`；错误时自动回退到基础存储。
- **消息读取**：`getMessages` 构造 `MemoryRetrievalRequest`，利用最近一条用户消息作为查询，自定义上下文为会话统计信息，检索到的 LTM 结果以系统提示的形式注入下游。

### 检索增强（Retrieval Augmentation）
- `rag/retriever/LTMEnhancedContentRetriever` 包装默认 `ContentRetriever`，由 `RagAugmentorFactory` 在 `smartcs.ai.ltm.retrieval.enabled=true` 时注册进 LangChain4j `LanguageModelQueryRouter`。
- 每次 RAG 检索会并行命中：
  1. 向量知识库检索（`EmbeddingStoreContentRetriever`）；
  2. 可选的 Web Search / SQL Retriever；
  3. LTM 检索器（调用 `LTMDomainService#retrieveMemoryContext`）。返回内容以 `source=ltm_episodic`/`ltm_semantic` 等元数据区分来源。
- `mergeAndRerankResults` 根据 `smartcs.ai.ltm.retrieval.weight`（默认 0.3）控制 LTM 结果占比，并在文本前添加 `[个性化内容]` 提示，便于后续模板或 UI 特殊渲染。
- **注意事项**：`extractUserIdFromQuery` 留有 TODO，需要在构造 RAG `Query` 时写入 `userId` 或 `memoryId`，否则 LTM 检索会退化为基础结果。

### 查询与注入阶段协同
- `DefaultRetrievalAugmentor` 在聚合阶段调用 `ContentInjector` 把 LTM 与其他来源的片段拼接到 Prompt 中；
- `QueryTransformerPipeline`（若在配置中开启）可用于提前写入用户标识、意图信息，给 LTM 检索提供更稳定的上下文；
- 在生成链路中，可结合 `PromptTemplate` 或上下文注入策略，让模型区分“知识库事实”与“个性化记忆”，避免混淆。

## 运行时序示意
**对话更新流程**
1. 用户发送消息 → `ChatMemoryStore#updateMessages` 写入短期记忆；
2. LTM 包装器异步构造 `MemoryFormationRequest`；
3. `LTMDomainService#formMemory` → `MemoryFormationService` 评估重要度、持久化记忆；
4. `LTMAuditLogger` 记录形成事件，必要时触发巩固任务。

**检索问答流程**
1. 用户发起需要知识库的问答流程；
2. `RagAugmentorFactory` 创建的 `LanguageModelQueryRouter` 并行调用多路 `ContentRetriever`；
3. LTM 检索器通过 `LTMDomainService` 拉取记忆，合并为 `Content`；
4. `ReRankingContentAggregator` 曲线混排，`ContentInjector` 将片段注入提示词；
5. 最终模型响应经 `LTMDomainService#personalizeResponse` 做风格调整后返回。

## 配置与调优建议
- `smartcs.ai.ltm.retrieval.enabled`：总开关，关闭后 LTM 不再介入对话/RAG，但形成与巩固仍可独立运行。
- `smartcs.ai.ltm.context.*`：控制注入的上下文数量与阈值，避免对话提示过长导致 token 浪费。
- `smartcs.ai.ltm.formation.*`：决定异步策略、重要度阈值、是否自动抽取语义/程序性记忆，可视业务对模型配额与存储成本的要求调节。
- `smartcs.ai.ltm.consolidation.*`：批任务的排程、批大小、重试策略；当数据量增大时建议结合分片或限流机制。
- `smartcs.ai.ltm.security.*`：包括加密、访问频控、审计开关，生产环境务必开启。
- 向量检索落地后，可把 `queryVector` 传入 `MemoryRetrievalRequest`，并同步调高权重以提升相关度。

## 安全与合规
- **加密**：`ltm/security/LTMDataEncryptor` 实现 AES-GCM 加解密，密钥配置在 `smartcs.secrets.keys`，建议结合 TypeHandler 透明加密敏感字段。
- **访问控制**：`ltm/security/LTMSecurityManager` 提供用户隔离、敏感访问限流、内容脱敏、导出/删除权限校验。
- **审计**：`LTMAuditLogger` 默认写日志，可扩展 `persistAuditEvents` 输出至 Kafka、数据库或审计平台，配合 `LTMSecurityManager#logSecurityViolation` 形成闭环。
- **敏感内容治理**：在进行记忆导入/导出或对话提示注入时，请结合业务合规规则对包含 PII 的字段做脱敏或忽略。

## 观测与测试建议
- 接入 Micrometer 监控关键链路：记忆形成耗时、巩固批任务成功率、检索命中率、个性化命中率等。
- 针对 `LTMDomainService` 编写单元测试／契约测试，模拟网关返回空值、异常的降级场景。
- 端到端验证可通过 Mock Redis/数据库的集成测试或在沙箱环境跑 `mvn -pl smartcs-web-infrastructure test` 的定制用例。
- 为审计与安全逻辑增加回归测试，确保在开关关闭/开启时行为符合预期。

## 扩展计划与风险点
1. **Query 元数据补全**：需要前置流程把 `userId` 写入 RAG `Query`，否则 LTM 检索无法生效。
2. **向量检索接入**：计划引入 Redis Vector / PgVector 后端，替换当前 SQL 排序，提升查全率；同时需补充降级策略。
3. **线程池治理**：对 Formation/Consolidation 异步任务统一接入 `ltmTaskExecutor` 并结合 MDC/Tracing，避免 ForkJoinPool 被占满。
4. **可观测性**：目前缺乏专用指标及健康检查端点，需规划 Prometheus 指标与日志结构化输出。
5. **数据治理**：审计日志仅落地文件，存在合规风险；应当配置持久化与告警阈值。
6. **接口安全**：`LTMMemoryController` 暴露的管理接口需接入统一鉴权、租户隔离以及速率限制，再开放给前台或运营工具。

---
*文档维护者：AI Platform Team；最新更新：由 Codex agent 代表 wangyu 提交。*
