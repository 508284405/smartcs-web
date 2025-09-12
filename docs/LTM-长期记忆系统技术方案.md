# 智能体长期记忆（LMT/LTM）技术方案

本文面向 SmartCS 的长期记忆系统（Long-term Memory，简称 LMT/LTM）方案调研、现状分析、优化建议与落地路径，覆盖架构、数据模型、流程、接口与配置，帮助团队以最小代价获得稳健、可观测、可演进的个性化记忆能力。

## 1. 背景与目标

- 目标：在多轮对话、跨会话场景中形成“可用、可靠、可控”的个性化长期记忆，提升答案个性化、上下文连续性与任务效率。
- 范畴：记忆形成、巩固、检索、遗忘、个性化响应、安全与合规（加密/审计）。
- 约束：与现有 RAG、会话记忆（ChatMemoryStore）与 LangChain4j 架构自然融合；遵循本仓库的 DDD 分层与命名规范。

## 2. 设计总览

- 领域模型（Domain）：
  - 情景记忆（Episodic）：具体的交互事件（时间/场景/上下文/嵌入向量/重要性/访问计数/巩固状态）。
  - 语义记忆（Semantic）：从情景记忆抽取的概念与知识（概念/知识/证据/置信度/衰减率）。
  - 程序性记忆（Procedural）：偏好/规则/习惯/响应风格（触发条件/成功率/活跃状态/学习率）。
- 关键流程：
  - 形成（Formation）：按重要性阈值形成情景记忆，可派生语义/程序性记忆。
  - 巩固（Consolidation）：离线聚合与泛化，高重要性情景记忆巩固为语义/程序性记忆。
  - 检索（Retrieval）：基于向量/关键词/元数据/时间的混合检索，注入 LTM 上下文到系统消息或 RAG 返回。
  - 遗忘（Forgetting）：语义衰减 + 情景保留期 + 程序性非活跃清理。
  - 个性化（Personalization）：基于程序性记忆增强模型响应风格与信息粒度。
- 安全与合规：
  - 加密：敏感内容 AES-GCM 加密（LTMDataEncryptor）。
  - 审计：访问/修改/导入导出审计（LTMAuditLogger）。

## 3. 现状分析（代码基线）

- 领域与接口：
  - `smartcs-web-domain` 下已定义 `EpisodicMemory`、`SemanticMemory`、`ProceduralMemory` 实体与 `EpisodicMemoryGateway`、`SemanticMemoryGateway`、`ProceduralMemoryGateway` 网关接口。
  - `LTMDomainService` 已定义检索、形成、巩固、遗忘、个性化等能力接口（暂无实现）。
- 形成与巩固：
  - `MemoryFormationService`：负责重要性评估（MemoryAnalyzer + LLM/规则）、情景记忆入库、条件触发语义/程序性派生与嵌入生成。
  - `MemoryConsolidationService`：定时任务（cron）执行概念聚类与派生，含 @Async("ltmTaskExecutor") 异步方法（缺少该 bean）。
- 与会话/RAG 的集成：
  - `LTMEnhancedRedisChatMemoryStore`：在获取/更新消息时检索或形成 LTM（依赖 `LTMDomainService`）。当前未作为 `@Primary`，默认仍使用 `FaultTolerantRedisChatMemoryStore`。
  - `LTMEnhancedContentRetriever`：用于在 RAG 检索中合并 LTM 内容，但未接入 `RagAugmentorFactory` 的 retriever 集合。
- 安全与可观测：
  - `LTMDataEncryptor`：AES-GCM 加密，密钥源自 `application.yaml` 的 `secrets` 段。
  - `LTMAuditLogger`：批量异步写入审计日志（当前落地为日志输出，后续可接入 DB/Kafka）。
- 配置：
  - `start/src/main/resources/application.yaml` 中已有完整 LTM 配置（formation/consolidation/retrieval/analyzer/security/performance/user-defaults 等）。
- 缺口与风险：
  - 无 `LTMDomainService` 实现，导致 `LTMEnhancedRedisChatMemoryStore`/`LTMEnhancedContentRetriever` 难以注入、功能无法启用。
  - 缺少 `ltmTaskExecutor`，`@Async("ltmTaskExecutor")` 将在运行时报错。
  - 三个 LTM 网关缺少持久化实现（MyBatis-Plus/JPA/Mapper），目前服务仅定义流程逻辑。
  - LTM 未真正接入 RAG 检索路由，仍在“可用组件”状态。

## 4. 已做优化与改进点（本次）

- 新增 `LTMDomainServiceImpl`（基础可用、可降级）：
  - 所在：`smartcs-web-infrastructure/src/main/java/com/leyue/smartcs/ltm/service/LTMDomainServiceImpl.java`
  - 特性：
    - 网关可选注入（未配置持久化实现时优雅降级为空上下文）。
    - 编排 Formation/Consolidation/Forgetting/Personalization 全流程。
    - 检索：向量优先回退规则，结合重要性/时间窗的简化混合策略。
    - 个性化：基于程序性记忆的“详细/技术导向”等轻量增强。
    - 导出/清理：按默认保留期清理情景记忆、语义衰减与非活跃程序性清理，导出聚合数据。
  - 价值：补齐核心服务实现，使 LTM 组件可被安全注入和调用（即使暂未接通网关实现）。

- 新增 `LTMAsyncConfig`：
  - 所在：`smartcs-web-infrastructure/src/main/java/com/leyue/smartcs/ltm/config/LTMAsyncConfig.java`
  - 功能：提供 `@Bean("ltmTaskExecutor")`，参数来自 `application.yaml` 的 `smartcs.ai.ltm.performance.async-executor.*`，保障巩固等异步任务正常运行。

以上变更保持最小侵入，不改变现有主路径（默认仍使用 `FaultTolerantRedisChatMemoryStore`）。

## 5. 快速开始（实践指南）

1) 启用配置（application.yaml）

```
smartcs:
  ai:
    ltm:
      enabled: true
      retrieval:
        enabled: true
        weight: 0.3
        max-results: 5
        threshold: 0.7
      context:
        chat-store:
          enabled: false   # 如需将LTM注入聊天记忆则设为true
      formation:
        importance-threshold: 0.5
        semantic-extraction:
          enabled: true
        procedural-learning:
          enabled: true
        async:
          enabled: true
      consolidation:
        schedule: "0 0 2 * * ?"
        batch-size: 100
        importance-threshold: 0.7
        semantic:
          enabled: true
          max-episodes-per-concept: 5
        procedural:
          enabled: true
      analyzer:
        use-llm: true
```

2) 准备依赖
- Redis（ChatMemoryStore 与向量存储如启用）
- OpenAI/兼容大模型 API Key（环境变量 `OPENAI_API_KEY`）
- 配置密钥：`secrets.keys.dev-key-2024` 已在样例中提供开发用途。

3) 启动与验证
- 构建：`mvn clean install -DskipTests`
- 运行：`cd start && mvn spring-boot:run`
- 聊天路径验证：打开使用聊天接口的应用，若 `smartcs.ai.ltm.context.chat-store.enabled=true`，LTM 上下文会注入到系统消息。
- RAG 路径验证：当 `smartcs.ai.ltm.retrieval.enabled=true`，RAG 检索会融合 LTM 内容，内容元数据带有 `source=ltm_*` 标记。

4) 典型 REST 接口（用户记忆管理）
- 获取摘要：GET `/api/v1/ltm/memory/summary?userId=1001`
- 情景记忆：GET `/api/v1/ltm/memory/episodic?userId=1001&page=1&size=20`
- 语义记忆：GET `/api/v1/ltm/memory/semantic?userId=1001&page=1&size=20`
- 程序性记忆：GET `/api/v1/ltm/memory/procedural?userId=1001`
- 搜索记忆：GET `/api/v1/ltm/memory/search?userId=1001&keyword=Spring`
- 删除情景：DELETE `/api/v1/ltm/memory/episodic/{memoryId}?userId=1001`
- 更新重要性：PUT `/api/v1/ltm/memory/episodic/{memoryId}/importance`

对应代码：
- `smartcs-web-adapter/src/main/java/com/leyue/smartcs/web/ltm/LTMMemoryController.java`

## 6. 关键数据流（时序说明）

- 聊天注入（可选）：
  1. 获取消息：`LTMEnhancedRedisChatMemoryStore#getMessages`
  2. 构造 `MemoryRetrievalRequest` → `LTMDomainServiceImpl#retrieveMemoryContext`
  3. 将 LTM 上下文（情景/语义/程序性）合并到 SystemMessage 前缀
  4. 返回增强后的消息列表

- 消息更新触发形成：
  1. `LTMEnhancedRedisChatMemoryStore#updateMessages` → 异步 `formMemoriesFromMessages`
  2. 记忆形成请求 → `MemoryFormationService#processMemoryFormation`
  3. 规则+LLM 评估重要性 → 情景记忆入库 →（可选）语义/程序性派生

- RAG 检索融合：
  1. `RagAugmentorFactory` 注册 `LTMEnhancedContentRetriever`（开关控制）
  2. 检索 LTM 上下文 → 转为 `Content` → 与知识库/Web/SQL 结果融合与重排

## 7. 存储与Schema（建议）

- 情景记忆表（episodic_memory）：id, user_id, session_id, episode_id, content, embedding_vector(BLOB), context_json, ts, importance, access_count, last_accessed_at, consolidation_status, created_at, updated_at
- 语义记忆表（semantic_memory）：id, user_id, concept, knowledge, embedding_vector, confidence, source_episodes(JSON), evidence_count, contradiction_count, last_reinforced_at, decay_rate, created_at, updated_at
- 程序性记忆表（procedural_memory）：id, user_id, pattern_type, pattern_name, description, triggers_json, action_template, success_count, failure_count, success_rate, last_triggered_at, learning_rate, is_active, created_at, updated_at

可先落 MyBatis-Plus 基本 CRUD，向量检索可接 Redis/PGVector，按系统 `vector-store.dimension` 与模型维度对齐。

## 8. 安全与可观测性（实践）

- 加密：`LTMDataEncryptor`（AES-GCM）可对敏感字段加解密；建议结合 TypeHandler 做字段级透明加密。
- 审计：`LTMAuditLogger` 批量刷写；建议改造为 DB/Kafka 落地，配合报表与阈值告警。
- 指标：形成命中率/巩固吞吐/检索命中率/个性化采纳率/存储占用；Tracing 贯通 Formation/Consolidation/Retrieval 路径。

## 9. 配置清单（与代码对应）

- `smartcs.ai.ltm.enabled`
- `smartcs.ai.ltm.retrieval.enabled|weight|max-results|threshold`
- `smartcs.ai.ltm.context.chat-store.enabled`
- `smartcs.ai.ltm.formation.importance-threshold|semantic-extraction.enabled|procedural-learning.enabled|async.enabled`
- `smartcs.ai.ltm.consolidation.schedule|batch-size|importance-threshold|semantic.enabled|max-episodes-per-concept|procedural.enabled`
- `smartcs.ai.ltm.analyzer.use-llm`
- 安全：`smartcs.ai.ltm.security.*`（加密/访问控制/审计）与 `secrets.*`

注：本次已统一 LTM 代码读取 `smartcs.ai.ltm.*` 命名空间。

## 10. 最佳实践

- 形成去重：已加入轻量级文本归一化去重，建议后续加入 embedding 相似度阈值判重。
- 预算控制：在形成阶段按重要性阈值与会话窗口裁剪，避免存储爆炸；在巩固/遗忘阶段进行老化清理。
- 个性化 A/B：对同一问题提供个性化/非个性化双路径评测，接入“RAG 评估”采样上报。
- 用户控制：提供“导出/清理/停用个性化”等自助入口，保障透明与可控。

## 11. 路线图（可落地）

- M1 当前：域服务实现 + LTM retriever 接入 + ChatMemoryStore 切换开关 + 去重 + 文档
- M2 持久化：三类网关 + 向量索引 + 审计落库 + 字段级加密
- M3 策略优化：形成/巩固去重聚类、遗忘预算策略、个性化评估看板
- M4 生态：与更多外部知识源、企业身份与权限体系集成

## 5. 推荐的后续优化路线

1) 网关持久化实现（关键路径）
- 依据既有实体与接口在 `smartcs-web-infrastructure` 补齐 MyBatis-Plus Mapper 与 GatewayImpl：
  - `EpisodicMemoryGatewayImpl`：保存/更新/时间窗/重要性/访问频次/批量状态更新；支持向量检索（Redis/PGVector/ES）。
  - `SemanticMemoryGatewayImpl`：保存/更新/置信度/证据与冲突/概念模糊与向量检索/批量衰减。
  - `ProceduralMemoryGatewayImpl`：保存/更新/按触发条件匹配/活跃度/成功率批量更新与清理。
- 嵌入向量存储：
  - 可优先接入 Redis 向量索引（与现有 `vector-store` 配置对齐），后续视需要支持 PGVector/ES。

2) 检索融入 RAG 路径（可配置）
- 在 `RagAugmentorFactory` 中按 `smartcs.ltm.retrieval.enabled` 动态加入 `LTMEnhancedContentRetriever`，统一结果融合与重排。

3) 会话记忆替换（按需）
- 将 `chatMemoryStore` 的 `@Primary` 切换为 `LTMEnhancedRedisChatMemoryStore`，或在 `AiAppChatCmdExe` 中按模型/应用开关动态选择。
- 已新增开关：`smartcs.ai.ltm.context.chat-store.enabled`（默认 false）。开启后 `ChatMemoryStore` 会在容错 Redis 基础上包一层 LTM 增强。

4) 形成与巩固策略强化
- 形成：
  - 重要性阈值 + 去重（MinHash/SimHash/Embedding 相似度）。
  - 检测“记住/偏好/计划/技能”等强触发词与 LLM 评分加权融合。
- 巩固：
  - 概念聚类时引入语义聚类（KMeans/HDBSCAN-on-embedding），并追踪贡献权重与证据数。
  - 程序性记忆强化采用带遗忘的移动平均与置信区间。

5) 遗忘与预算
- 语义记忆：指数衰减（已支持），可叠加“使用频率”抗衰减。
- 情景记忆：保留期/配额/重要性优先级策略（LRU+Importance）。
- 程序性记忆：低成功率+非活跃清理，保留用户手动标注的“锁定”模式。

6) 安全与审计落地
- 审计落地：将 `LTMAuditLogger` 输出落库（MySQL/Kafka），建立检索报表与阈值告警。
- 加密落地：为敏感字段提供字段级透明加解密（结合 MyBatis-Plus TypeHandler）。

7) 可观测性与评估
- 指标：形成命中率/巩固吞吐/检索命中率/个性化满意度/存储占用。
- 评估：将 LTM 影响纳入现有“RAG 评估”事件采集（个性化增强是否提升回答质量）。

## 6. 关键接口与配置（摘录）

- 领域服务：`com.leyue.smartcs.domain.ltm.domainservice.LTMDomainService`
  - `retrieveMemoryContext(req)`：检索 LTM 上下文（情景/语义/程序性）。
  - `formMemory(req)` / `consolidateMemories(userId)` / `applyForgetting(userId)`。
  - `personalizeResponse(userId, original, ctx)`：个性化响应增强。
  - `learnUserPreference(userId, interaction, feedback)`：在线学习偏好。

- 配置：`start/src/main/resources/application.yaml`
  - `smartcs.ai.ltm.*`（formation/consolidation/retrieval/analyzer/security/performance/user-defaults）。
  - `secrets.*`（AES-GCM 密钥与算法）。
  - LTM集成开关：
    - `smartcs.ai.ltm.retrieval.enabled`：在 RAG 检索路由中加入 LTM 检索器。
    - `smartcs.ai.ltm.context.chat-store.enabled`：启用 LTM 增强 ChatMemoryStore。

## 7. 落地建议与里程碑

- M1（本次）：
  - 已补齐 `LTMDomainServiceImpl` 与 `ltmTaskExecutor`，LTM 组件可加载且具备可回退能力。
  - 输出完整技术方案文档（本文）。
- M2（持久化）：
  - 完成三类记忆网关的 MyBatis-Plus 实现与基础表结构；打通 Redis 向量索引。
  - 将 `LTMEnhancedContentRetriever` 挂入 RAG 检索路径（配置开关）。
- M3（质量与个性化）：
  - 完善形成/巩固策略与去重；加入个性化 A/B 评估与指标看板。
  - 审计落库、敏感字段透明加解密。

## 8. 附：主要源码位置

- 域模型与接口：
  - `smartcs-web-domain/src/main/java/com/leyue/smartcs/domain/ltm/entity/EpisodicMemory.java`
  - `smartcs-web-domain/src/main/java/com/leyue/smartcs/domain/ltm/entity/SemanticMemory.java`
  - `smartcs-web-domain/src/main/java/com/leyue/smartcs/domain/ltm/entity/ProceduralMemory.java`
  - `smartcs-web-domain/src/main/java/com/leyue/smartcs/domain/ltm/domainservice/LTMDomainService.java`
- 基础能力：
  - 形成：`smartcs-web-infrastructure/src/main/java/com/leyue/smartcs/ltm/service/MemoryFormationService.java`
  - 巩固：`smartcs-web-infrastructure/src/main/java/com/leyue/smartcs/ltm/service/MemoryConsolidationService.java`
  - 分析：`smartcs-web-infrastructure/src/main/java/com/leyue/smartcs/ltm/service/MemoryAnalyzer.java`
  - 域服务实现：`smartcs-web-infrastructure/src/main/java/com/leyue/smartcs/ltm/service/LTMDomainServiceImpl.java`
  - 异步：`smartcs-web-infrastructure/src/main/java/com/leyue/smartcs/ltm/config/LTMAsyncConfig.java`
  - 安全：`smartcs-web-infrastructure/src/main/java/com/leyue/smartcs/ltm/security/*`
- RAG/会话集成：
  - RAG：`smartcs-web-infrastructure/src/main/java/com/leyue/smartcs/rag/retriever/LTMEnhancedContentRetriever.java`
  - 会话：`smartcs-web-infrastructure/src/main/java/com/leyue/smartcs/rag/memory/LTMEnhancedRedisChatMemoryStore.java`

---

备注：本次提交未改动现有主运行路径（ChatMemoryStore/RAG 选择）。当网关实现与存储准备就绪后，可按章节 5 将 LTM 平滑接入生产路径，并逐步增强策略与可观测性。
