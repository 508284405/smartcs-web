# SmartCS Web — DDD 项目架构说明

本文从领域驱动设计（DDD）的视角，梳理 smartcs-web 的分层结构、边界上下文、核心名词与在代码中的落地方式，并给出端到端用例链路与扩展指引。

---

## 1. 分层与模块映射（Hexagonal/Onion）

- client（契约/DTO）
  - 定义对外服务接口与数据契约（命令/查询/DTO）。
  - 例：`ModelService`、`ModelDTO`、`ModelUpdateCmd`。
- domain（领域模型）
  - 实体/值对象/领域服务/领域网关（端口）。不依赖技术细节。
  - 例：`Model`、`DictionaryEntry`、`QueryExpansionConfig`、`ModelDomainService`、`ModelGateway`。
- infrastructure（基础设施）
  - 被驱动适配器：实现领域网关；持久化、消息、LLM、外部系统；转换器（MapStruct）。
  - 例：`ModelGatewayImpl`、`AiAppGatewayImpl`、`RagEventGatewayImpl`、`QueryExpansionGatewayImpl`、`SqlQueryContentRetriever`。
- app（应用层）
  - 用例编排、事务，`*CmdExe` / `*QryExe` 执行器；不承载核心业务规则。
  - 例：`ModelUpdateCmdExe`、`ModelPageQryExe`、`ModelServiceImpl`。
- adapter（驱动适配器）
  - Web/WS/SSE 控制器，将请求转换为命令/查询，调用应用服务。
  - 例：`PublicAiAppController`、`LTMMemoryController`。
- start（组合根）
  - Spring Boot 启动与装配，跨模块组件扫描。

---

## 2. 边界上下文（Bounded Contexts）

项目按照业务子域（上下文）组织包：model（模型编排）、app（AI 应用）、knowledge（知识库）、intent（意图）、rag（检索增强）、ltm（长期记忆）、eval（评估/观测）、dictionary（字典配置）、chat（会话）、moderation（内容审核）等。上下文之间通过应用层编排和领域网关解耦。

---

## 3. DDD 名词到项目代码的映射

下述均为真实代码示例（路径仅作定位，非穷尽）。

- 实体（Entity）
  - 模型实体：`domain/model/Model.java`（状态流转、可用性校验、特性映射等）
  - 客户实体：`domain/customer/Customer.java`（业务规则 `checkConflict()`、规模判断）
  - 语义记忆：`domain/ltm/entity/SemanticMemory.java`（置信度/遗忘/证据累积）
  - 字典条目（聚合根）：`domain/dictionary/entity/DictionaryEntry.java`（创建/更新/状态/版本）
- 值对象（Value Object）
  - 查询扩展配置：`domain/rag/transformer/valueobject/QueryExpansionConfig.java`（工厂 + 校验）
  - 字典配置：`domain/dictionary/valueobject/DictionaryConfig.java`（标识生成与格式校验）
- 聚合与聚合根（Aggregate & Root）
  - `DictionaryEntry` 为聚合根，封装规则与版本一致性；`AiApp`、`Model` 在各自上下文中亦承担聚合根职责。
- 领域服务（Domain Service）
  - 模型领域服务：`domain/model/domainservice/ModelDomainService.java`（创建/更新/启停校验、ID 生成、网关编排）
  - LTM 领域服务：`domain/ltm/domainservice/LTMDomainService.java`（长期记忆检索/形成的领域能力，供适配器调用）
- 领域网关（端口，Gateway/Repository Interface）
  - 模型网关：`domain/model/gateway/ModelGateway.java`
  - AI 应用网关：`domain/app/gateway/AiAppGateway.java`
  - 评估事件网关：`domain/eval/gateway/RagEventGateway.java`
- 基础设施实现（被驱动适配器）
  - MyBatis-Plus + MapStruct：`model/gatewayimpl/ModelGatewayImpl.java`、`app/gateway/AiAppGatewayImpl.java`
  - Kafka 事件发送：`eval/gatewayimpl/RagEventGatewayImpl.java` → `eval/producer/RagEventProducer.java`
  - LLM 查询扩展：`rag/transformer/gateway/QueryExpansionGatewayImpl.java`（LangChain4j）
  - SQL 内容检索：`rag/content/retriever/SqlQueryContentRetriever.java`（NLP→SQL、安全校验、元数据）
- 应用层（Application Service + 执行器）
  - 服务实现：`model/serviceimpl/ModelServiceImpl.java`
  - 执行器：`model/executor/ModelUpdateCmdExe.java`、`model/executor/ModelPageQryExe.java` 等
- 驱动适配器（Controller/SSE/WS）
  - 公开应用：`web/app/PublicAiAppController.java`（状态校验 + SSE）
  - LTM 管理：`web/ltm/LTMMemoryController.java`
- 反腐层（ACL）与转换器（MapStruct）
  - 领域↔DO：`model/convertor/ModelConvertor.java`、`app/convertor/AiAppConvertor.java`
  - 目的：隔离数据库/外部模型的变化对领域层的侵入

---

## 4. 端到端用例链路（示例）

### 用例 A：更新模型实例（Model.Update）

1) 适配器接收请求（REST，略）→ 调用 client 契约 `ModelService`
2) 应用层路由到命令执行器 `ModelUpdateCmdExe`，将 `ModelUpdateCmd` 转为领域模型 `Model`
3) 调用领域服务 `ModelDomainService.updateModel()`：
   - 校验模型存在、未删除
   - 校验提供商存在（通过 `ProviderGateway`）
   - 更新时间/保留创建信息
   - 委托 `ModelGateway.updateModel()`
4) 基础设施 `ModelGatewayImpl` 使用 MyBatis-Plus 更新 `ModelDO`；`ModelConvertor` 做 DO↔领域对象映射
5) 应用层将领域对象转换回 `ModelDTO`，返回响应

典型路径：
- 应用服务：`smartcs-web-app/src/main/java/com/leyue/smartcs/model/serviceimpl/ModelServiceImpl.java`
- 执行器：`smartcs-web-app/src/main/java/com/leyue/smartcs/model/executor/ModelUpdateCmdExe.java`
- 领域服务：`smartcs-web-domain/src/main/java/com/leyue/smartcs/domain/model/domainservice/ModelDomainService.java`
- 端口：`smartcs-web-domain/src/main/java/com/leyue/smartcs/domain/model/gateway/ModelGateway.java`
- 实现：`smartcs-web-infrastructure/src/main/java/com/leyue/smartcs/model/gatewayimpl/ModelGatewayImpl.java`
- 转换器：`smartcs-web-infrastructure/src/main/java/com/leyue/smartcs/model/convertor/ModelConvertor.java`

### 用例 B：RAG 查询扩展（Query Expansion）

- client 中的 RAG 组件配置 `RagComponentConfig` 提供可配参数
- 领域端口 `QueryExpansionGateway` 的基础设施实现 `QueryExpansionGatewayImpl` 基于 LangChain4j 调用动态模型、解析扩展查询、集成 Sentinel 限流/降级

典型路径：
- 配置 DTO：`smartcs-web-client/src/main/java/com/leyue/smartcs/dto/app/RagComponentConfig.java`
- 基础设施：`smartcs-web-infrastructure/src/main/java/com/leyue/smartcs/rag/transformer/gateway/QueryExpansionGatewayImpl.java`

### 用例 C：长期记忆（LTM）增强的聊天记忆

- 基础设施适配器 `LTMEnhancedRedisChatMemoryStore` 包装底层 `ChatMemoryStore`，在 get/update 消息时调用领域服务 `LTMDomainService`：
  - getMessages：根据用户上下文检索 Episodic/Semantic/Procedural 记忆，注入系统消息
  - updateMessages：异步将最近对话片段形成记忆

路径：`smartcs-web-infrastructure/src/main/java/com/leyue/smartcs/rag/memory/LTMEnhancedRedisChatMemoryStore.java`

### 用例 D：评估与观测链路（RAG 事件上报）

- AOP 采集：`RagEventCollectorAspect` 拦截聊天执行，构建 `RagEvent` 指标
- 领域端口 `RagEventGateway` 实现 `RagEventGatewayImpl` 调用 `RagEventProducer` 异步写入 Kafka

路径：
- `smartcs-web-app/src/main/java/com/leyue/smartcs/eval/aop/RagEventCollectorAspect.java`
- `smartcs-web-domain/src/main/java/com/leyue/smartcs/domain/eval/gateway/RagEventGateway.java`
- `smartcs-web-infrastructure/src/main/java/com/leyue/smartcs/eval/gatewayimpl/RagEventGatewayImpl.java`
- `smartcs-web-infrastructure/src/main/java/com/leyue/smartcs/eval/producer/RagEventProducer.java`

---

## 5. 设计原则与实践要点

- 规则归位：
  - 不变量与行为尽量落在聚合根/实体；参数组合与不可变对象使用值对象；跨实体规则落在领域服务。
- 端口隔离：
  - 领域层只定义 Gateway 接口；实现放在 infrastructure，利用 MapStruct 做 DO/DTO ↔ 领域对象的转换，避免技术细节“上浮”。
- 用例编排：
  - 应用层 `*CmdExe`/`*QryExe` 负责交易脚本式编排、事务控制与跨上下文调用，不写核心业务规则。
- 适配器分离：
  - Web/SSE/WS 控制器仅做入参校验、权限/限流、与应用层交互；不直接操作基础设施。
- 事件驱动：
  - 领域事件用于表达业务状态变化；集成事件用于跨系统传播和观测（Kafka）。
- 可观测与稳健性：
  - Sentinel 限流与 Fallback、AOP 采样、SkyWalking Trace 标签、结构化日志贯穿 RAG/LTM 等关键链路。

---

## 6. 新增用例/能力的推荐流程

1) 定义契约与 DTO（client 模块）
2) 补充领域模型：实体/值对象/领域服务/端口（domain 模块）
3) 实现端口（infrastructure 模块）：持久化/外部系统/转换器
4) 编排用例（app 模块）：新增 `*CmdExe`/`*QryExe` 并在应用服务中组合
5) 提供适配器（adapter 模块）：新增 REST/WS/SSE 控制器或集成入口
6) 验证链路：单元/集成测试、观测数据校验

---

## 7. 参考示例（代码定位）

- 实体：
  - `smartcs-web-domain/src/main/java/com/leyue/smartcs/domain/model/Model.java`
  - `smartcs-web-domain/src/main/java/com/leyue/smartcs/domain/dictionary/entity/DictionaryEntry.java`
- 值对象：
  - `smartcs-web-domain/src/main/java/com/leyue/smartcs/domain/rag/transformer/valueobject/QueryExpansionConfig.java`
  - `smartcs-web-domain/src/main/java/com/leyue/smartcs/domain/dictionary/valueobject/DictionaryConfig.java`
- 领域服务：
  - `smartcs-web-domain/src/main/java/com/leyue/smartcs/domain/model/domainservice/ModelDomainService.java`
- 端口与实现：
  - `smartcs-web-domain/src/main/java/com/leyue/smartcs/domain/model/gateway/ModelGateway.java`
  - `smartcs-web-infrastructure/src/main/java/com/leyue/smartcs/model/gatewayimpl/ModelGatewayImpl.java`
- 应用与适配器：
  - `smartcs-web-app/src/main/java/com/leyue/smartcs/model/executor/ModelUpdateCmdExe.java`
  - `smartcs-web-adapter/src/main/java/com/leyue/smartcs/web/app/PublicAiAppController.java`
- RAG/LTM：
  - `smartcs-web-infrastructure/src/main/java/com/leyue/smartcs/rag/transformer/gateway/QueryExpansionGatewayImpl.java`
  - `smartcs-web-infrastructure/src/main/java/com/leyue/smartcs/rag/memory/LTMEnhancedRedisChatMemoryStore.java`
- 评估事件：
  - `smartcs-web-app/src/main/java/com/leyue/smartcs/eval/aop/RagEventCollectorAspect.java`
  - `smartcs-web-infrastructure/src/main/java/com/leyue/smartcs/eval/gatewayimpl/RagEventGatewayImpl.java`

---

以上可作为团队在 smartcs-web 中进行 DDD 架构理解、代码走查与用例扩展的参考。

