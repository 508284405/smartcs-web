# Sentinel 集成说明

## 背景调研
- Sentinel 提供限流、熔断降级、系统保护和热点参数控制，支持规则动态推送与控制台管理。
- 项目原先依赖 Resilience4j 提供熔断和隔离能力，主要通过注解驱动（`@CircuitBreaker`、`@Retry`、`@Bulkhead`）应用在 Feign、Redis 聊天记忆和模型推理等组件上。
- 结合现有基础设施（Nacos、Redis、Kafka、LangChain4j），Sentinel 可借助 Spring Cloud Alibaba 实现统一流控治理，并复用 Nacos 作为规则中心。

## 迁移范围
- 删除 Resilience4j 依赖与配置（父 POM、`start`、`smartcs-web-infrastructure` 模块）及相关配置类。
- 新增 Sentinel 依赖（`spring-cloud-starter-alibaba-sentinel`、`sentinel-datasource-nacos`）以及 `SentinelConfig`，统一注册 `SentinelResourceAspect` 与自定义 `BlockExceptionHandler`。
- 将 Resilience4j 注解迁移为 `@SentinelResource`，实现资源标记、Fallback 与限流 Block Handler：
  - ID 生成网关：`smartcs-web-infrastructure/src/main/java/com/leyue/smartcs/common/gateway/IdGeneratorGatewayImpl.java`
  - Redis 聊天记忆：`smartcs-web-infrastructure/src/main/java/com/leyue/smartcs/rag/memory/FaultTolerantRedisChatMemoryStore.java`、`.../LTMEnhancedRedisChatMemoryStore.java`
  - RAG 查询扩展与 NLP2SQL：`.../QueryExpansionGatewayImpl.java`、`.../NlpToSqlService.java`
  - SQL 检索：`.../rag/content/retriever/SqlQueryContentRetriever.java`
  - AI 应用聊天、模型推理 SSE：`smartcs-web-app/src/main/java/com/leyue/smartcs/app/executor/AiAppChatCmdExe.java`、`smartcs-web-app/src/main/java/com/leyue/smartcs/model/executor/ModelInferStreamCmdExe.java`
  - 对外接口：`smartcs-web-adapter/src/main/java/com/leyue/smartcs/web/app/PublicAiAppController.java`、`smartcs-web-adapter/src/main/java/com/leyue/smartcs/sse/ModelSSEController.java`
- 调整 Feign 客户端移除 Resilience4j 注解，限流熔断由上层资源管理器统一处理。
- `application.yaml` 中替换 Resilience4j 配置为 Sentinel Nacos 数据源示例，并开启 `feign.sentinel.enabled`。

## 资源命名与策略建议
- 网关/Feign：`gateway:id-generator:*`、`model:infer:*` 等遵循 `层级:模块:动作` 命名，控制台可据此定义流控（QPS、并发）与降级策略。
- RAG 流程：`rag:query-expansion:*`、`rag:nlp2sql:generate`、`rag:sql-retriever:retrieve`，建议按模型/应用维度配置热点参数限流。
- 会话接口：`app:ai-chat:execute`、`public-app:chat` 对应 SSE 通道，控制台可结合热点参数（sessionId/appId）设置链路限流。
- Redis 存储：`redis-memory-store:*` 与 `ltm-enhanced-memory:*` 负责聊天记忆读写，Block Handler 降级到内存存储。

## 后续工作
1. 在 Sentinel Dashboard 中创建上述资源的流控、降级、热点规则，并通过 Nacos dataId（`smartcs-web-sentinel-flow-rules` 等）下发。
2. 结合业务负载为 `AiAppChatCmdExe`、模型推理等热点通道设定合理阈值，并引入热点参数规则区分租户/会话。
3. 针对 `ToolRateLimitService` 自研限流逻辑，评估迁移到 Sentinel 热点参数统计或接入自定义 Slot 扩展。
4. 接入监控：通过 Sentinel 控制台或 Prometheus Exporter 观察各资源的 block 次数、降级情况，完善告警策略。

## 参考
- `start/src/main/resources/application.yaml` Sentinel 基础配置（Dashboard 地址、Nacos 规则数据源）。
- `smartcs-web-infrastructure/src/main/java/com/leyue/smartcs/config/SentinelConfig.java` 自定义 BlockException 响应与 URL 归一化。
