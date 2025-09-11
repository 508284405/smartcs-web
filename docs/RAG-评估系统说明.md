# RAG 评估系统设计与实现说明

本文档介绍 SmartCS RAG 评估系统的关键组件、数据流、采样与事件模型、运行态指标以及 CI/CD 质量闸门方案。并记录本次优化点：富集在线事件（检索上下文）、用户 ID 匿名化与异常日志增强。

## 1. 架构与数据流

- 在线评估（采样/埋点）
  - AOP 切面拦截聊天与检索，构建 `RagEvent` 并异步发送至 Kafka。
  - 运行态指标通过 Micrometer 采集（请求数/时长/错误率/检索条数等）。
- 离线/CI 评估（质量闸门）
  - 通过 Feign 调用外部评估服务（FastAPI），对基准集进行评分，结果作为 CI 质量闸门。

## 2. 关键组件与文件

- 采样切面与事件收集
  - `smartcs-web-app/.../eval/aop/RagEventCollectorAspect.java`
    - 拦截 `AiAppChatCmdExe.execute/processChatStream` 与所有 `ContentRetriever.retrieve(...)`。
    - 构建并发送 `RagEvent`，记录总耗时、问题/答案等。
    - 优化：
      - 富集检索上下文（从返回的 `Content` 列表提取文本与元数据，生成 `retrievedContexts`）。
      - 用户 ID 匿名化（SHA-256 截断）防止明文外泄。
- 事件网关与 Producer
  - 接口：`smartcs-web-domain/.../RagEventGateway.java`
  - 实现：`smartcs-web-infrastructure/.../eval/gatewayimpl/RagEventGatewayImpl.java`
  - Kafka Producer：`smartcs-web-infrastructure/.../eval/producer/RagEventProducer.java`
    - 优化：补充异常日志，避免静默失败。
- 运行态观测
  - 监听器：`smartcs-web-infrastructure/.../rag/observability/ChatEventListener.java`
  - 指标收集：`smartcs-web-infrastructure/.../rag/observability/ChatMetricsCollector.java`
  - Trace 集成：`smartcs-web-app/.../eval/trace/SkyWalkingTraceContext.java`
- 简化评估（对接外部服务）
  - Feign Client：`smartcs-web-infrastructure/.../config/feign/SimpleEvalClient.java`
  - 网关：`smartcs-web-infrastructure/.../eval/gatewayimpl/SimpleEvalGatewayImpl.java`
  - DTO：`smartcs-web-client/.../dto/eval/SimpleEvalRequest.java`、`SimpleEvalResponse.java`
  - 管理端 API：`smartcs-web-adapter/.../web/eval/SimpleEvalController.java`

## 3. 在线采样与事件模型

- 采样策略：
  - 按 `eval.sampling.rate`（默认 5%）进行一致性哈希采样；
  - 或通过请求头 `X-RAG-EVAL: true` 强制采样。
- 事件模型（RagEvent）：
  - 基本字段：`eventId/traceId/ts/userId/question/answer/latencyMs/app`。
  - 检索上下文：`retrievedContexts[]`（docId、text、score、rank、source、chunkId）。
  - 检索器配置：`retriever.k`（观测到的返回条数作为近似 TopK）。
- 优化细节：
  - 在切面 `collectRetrieval` 中，识别 `List<Content>` 返回值，
    - 提取 `Content.textSegment().text()` 与 `metadata().toMap()`，
    - 解析 `docId/source/score/chunkId` 等常用键，构造 `RagEvent.RetrievedContext`。
  - 用户 ID 匿名化：读取请求头 `user-id` 后经 SHA-256→hex→截断 12 位。

## 4. 运行态指标（Micrometer）

- 聊天统计：总请求、成功/失败、激活会话数、平均时长。
- RAG 检索：检索时长、总召回条数。
- 工具使用：按工具/成功状态计数。
- 错误统计：按错误类型/组件计数。
- 输出位置：`smartcs-web-infrastructure/.../rag/observability/ChatMetricsCollector.java`

## 5. 离线评估与质量闸门（CI/CD）

- GitHub Actions 工作流：`.github/workflows/quality-gate.yml`
  - 启动评估服务+依赖（容器），执行 `scripts/baseline-eval.sh`。
  - 解析结果生成 HTML 报告与 PR 评论，失败时阻断流水线。
- 阈值与数据集：
  - `scripts/eval-thresholds.yaml`（环境/领域阈值与策略）
  - `scripts/baseline-dataset.json`（基准数据集）
- 评估服务 API：
  - POST `/eval`，请求 `SimpleEvalRequest`，响应 `SimpleEvalResponse`（含 `results/aggregate`）。

## 6. 配置总览

`start/src/main/resources/application.yaml`

```yaml
eval:
  enabled: true
  sampling:
    rate: 0.05
    header: "X-RAG-EVAL"
  kafka:
    topics:
      rag-events: "rag.events"
  simple-eval:
    base-url: "http://localhost:8088"
    timeout-ms: 120000
```

## 7. 风险与改进建议

- 事件语义对齐：不同检索器返回的元数据键不统一，已采用“尽力识别”策略；建议统一检索返回的标准键（docId/score/source/chunkId）。
- TopK/阈值观测：当前从实际返回条数估算 `k`；若需严格观测，可在检索器元数据中显式注入运行参数。
- 隐私保护：已匿名化 `userId`，如需增强可引入 HMAC salt 或全局 UID 映射策略。
- 评估降级：外部评估服务不可用时，质量闸门会失败；可在 CI 中增加本地兜底或缓存上次通过结果。
- 可视化：建议配套 Prometheus/Grafana 与 Kafka 消费侧看板，联动线上事件与离线评分。

## 8. 本次优化变更摘要

- 富集在线事件：在检索切面中解析 `Content` 返回值，生成 `retrievedContexts` 并写入 `RagEvent`。
- 用户 ID 匿名化：对 `user-id` 请求头进行 SHA-256 截断处理，默认匿名 `anonymous`。
- Producer 异常日志：`RagEventProducer.sendAsync` 捕获并记录发送失败异常，避免静默失败。

> 如需将更多运行参数（TopK、minScore、重排模型等）注入事件，可在 `RagAugmentorFactory` 注册检索器时统一封装元数据，或在检索返回的 `Content.metadata` 中标准化注入，切面将自动拾取。

