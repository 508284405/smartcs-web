下面是一份可直接执行的落地计划（以 Java / Spring Boot 3.x、Micrometer Tracing + OpenTelemetry、HTTP/gRPC/Kafka 为基线），目标是统一 traceId 并贯通跨服务与异步场景。你可以按阶段推进，也可并行两两实施。

目标与范围
	•	目标：全链路统一使用 W3C Trace Context（traceparent/tracestate），在 HTTP/gRPC/MQ/异步编程中保证 traceId 不丢、不改、不错。
	•	范围：API 网关、所有下游微服务、HTTP/gRPC 客户端与服务端、Kafka/RabbitMQ 生产与消费端、@Async/线程池/CompletableFuture/Reactor/定时任务、日志与采样策略。

⸻

路线图（8 个阶段）

P0 现状盘点（2–3 天）

产出：清单与基线
	•	盘点各服务使用的追踪协议（B3/sw8/W3C）、探针与依赖版本、日志模式、MQ 头白名单/大小限制。
	•	标注关键链路（入口→核心→出账/下单）与异步热点（批处理、扇出聚合）。
	•	确认可观测平台与 OTLP/Exporter/Collector 地址。

P1 协议统一与入口治理（3–5 天）

策略：对外/对下游统一 W3C；过渡期双写（W3C + 旧协议），在网关桥接转换。
任务
	•	API Gateway：启用/编写过滤器，提取(B3/sw8)→转写W3C；下游仅转发 W3C。
	•	入口采样策略：在网关做 head-based 采样，保留高价值交易/错误强制采样。
验收
	•	外部来流带 B3/sw8 时，下游服务能收到 W3C 且 traceId 保持不变。

P2 基础依赖与平台接入（1–2 天/服务，可并行）

依赖

implementation("org.springframework.boot:spring-boot-starter-actuator")
implementation("io.micrometer:micrometer-tracing-bridge-otel")
implementation("io.opentelemetry:opentelemetry-exporter-otlp")
implementation("io.micrometer:context-propagation")

配置

management.tracing.sampling.probability=1.0
management.otlp.tracing.endpoint=http://otel-collector:4318/v1/traces
logging.pattern.level=%5p [traceId=%X{traceId} spanId=%X{spanId}]

验收：本地调用能在平台看到 span，并在日志中输出 traceId/spanId。

P3 HTTP/gRPC 传播（2–3 天/域）

任务
	•	HTTP：启用客户端/服务端拦截器，自动 inject/extract traceparent。
	•	gRPC：注册 ClientInterceptor/ServerInterceptor，使用 metadata 传播。
验收
	•	A→B→C 链路中 UI 能看到单一 trace，父子关系正确；B 不生成新 traceId。

P4 MQ 传播（2–4 天/域）

任务
	•	生产者：在消息 header 注入 traceparent/tracestate；校验 header 白名单与大小。
	•	消费者：从 header 提取上下文作为父上下文创建消费 span；对广播/聚合使用 Span Links。
	•	重试/死信：沿用原 traceId；在日志打 retryAttempt。
验收
	•	HTTP→Kafka→消费者→下游HTTP 的同一 traceId 能贯通；重试场景可定位全链路。

P5 异步场景一致性（3–5 天）

统一做“捕获-恢复”（capture/restore），禁止裸 ThreadLocal。
	•	线程池 / @Async：为 ThreadPoolTaskExecutor 配置 TaskDecorator：

@Bean TaskDecorator tracingTaskDecorator() {
  return r -> ContextSnapshot.captureAll().wrap(r);
}


	•	CompletableFuture：对 supplyAsync/thenApply/... 使用 ContextSnapshot.captureAll().wrap(...)。
	•	Reactor（WebFlux）：注册 MDCThreadLocalAccessor() 并 Hooks.enableAutomaticContextPropagation()。
	•	定时任务/批处理：默认新根 Span；如需跨批次关联，用 baggage/事件字段存业务关联 ID。
验收
	•	@Async、CompletableFuture、Reactor、Scheduler 各场景日志与平台中 traceId 均不丢失。

P6 日志与采样治理（1–2 天）

任务
	•	统一 JSON 日志或模式串，确保 traceId/spanId 写入 MDC。
	•	采样：入口 head-based + Collector 端 tail-based（按错误/延迟回收）。
	•	Baggage 最小化（仅租户、渠道等少量安全字段）。
验收
	•	采样=0 时仍可通过日志用 traceId 全文检索定位问题。

P7 端到端测试集（3–5 天）

覆盖
	•	正常：A→B→C HTTP；A→Kafka→D；D→gRPC→E。
	•	异常/重试：下游 5xx、客户端重试、MQ 重试/死信。
	•	并发扇出/聚合：A 并发调用 B/C/D → A 聚合；批处理/多事件聚合用 Span Links。
	•	无入站头：Job/定时任务生成根 trace。
验收指标
	•	贯穿率≥99.5%（含异步/MQ），错误链路完整率≥99%，日志与平台 traceId 一致。

P8 渐进发布与回滚（1–2 周）

策略
	•	环境顺序：dev → test → pre → prod；每批次灰度 10% → 50% → 100%。
	•	观测指标：trace 入口数、span 丢失率、端到端时延 P95/99、Collector 压力、日志体积。
	•	回滚：开关采样比例、关闭桥接/双写、恢复旧日志模式（保持服务可用）。

⸻

服务侧落地清单（每个服务打勾即过关）
	1.	统一依赖与版本（Micrometer Tracing、OTLP、context-propagation）。
	2.	HTTP/gRPC 拦截器启用，禁止在中游重建 traceId。
	3.	Kafka/RabbitMQ 生产/消费注入与提取就绪；校验 header 白名单与大小。
	4.	线程池配置 TaskDecorator；@Async 指向已装饰的执行器。
	5.	Reactor 自动上下文传播启用；MDC 写入正常。
	6.	定时任务的根 Span 与业务关联字段约定清晰。
	7.	日志模式包含 traceId/spanId；JSON 或统一 pattern。
	8.	采样配置与 Collector 地址正确；错误/延迟策略在 Collector 生效。
	9.	E2E 用例通过（正常/异常/扇出/重试/批处理）。
	10.	文档与 Runbook 补齐（调试方法、常见故障、回滚步骤）。

⸻

网关桥接与多协议迁移
	•	入口只认 W3C：若上游带 B3/sw8，提取→转写为 W3C；对下游仅下发 W3C。
	•	过渡期双写（W3C + 旧协议），给存量依赖留切换窗口；明确截止日期，届时关闭双写。
	•	严禁中游服务覆盖已有 traceparent；如检测到，报警 + 失败构建。

⸻

风险与缓解
	•	MQ 头被剥离/超限：提前白名单、压缩/精简 baggage；必要时仅保留 traceparent。
	•	只复制 MDC 未复制 OTel 上下文：统一使用 ContextSnapshot.captureAll()。
	•	多协议混用：强制入口统一 + 静态分析/网关检测；灰度期间监控拓扑碎片化。
	•	成本上升：调低 head-based，Collector 启 tail-based 重点保留错误/慢调用；指标&日志不依赖采样。

⸻

指标与报警（建议）
	•	trace 丢失率（无父子关系或跨跳断裂）> 0.5% 报警。
	•	无 traceId 的日志比例 > 0.1% 报警。
	•	Collector 队列滞留、导出失败率、上报时延 P95 阈值报警。
	•	MQ 重试次数/死信率 与 异常链路占比 的联合报警。

⸻

交付物与“完成定义”（DoD）
	•	《追踪规范 v1》：W3C 为唯一协议，baggage 白名单，命名规则（service/span/attr）。
	•	《落地手册》：依赖版本、配置样例、代码片段（拦截器/TaskDecorator/Reactor 初始化）。
	•	《网关桥接与回滚方案》：双写开关、切换步骤、失败回退。
	•	《E2E 测试清单与脚本》：可在 CI/CD 中自动验收。
	•	DoD：P0–P8 全部通过，观测指标达标 2 周且无回退。

⸻

团队分工（RACI 简表）
	•	平台/中间件（R）：P1/P2/P6（协议/依赖/采样/Collector/日志规范）。
	•	各业务服务（R）：P3/P4/P5（传播与异步改造）、自测与联调。
	•	SRE/可观测（A）：指标、报警、容量评估与灰度发布。
	•	架构/技术委员会（C）：规范审阅与最终验收。
	•	QA（I）：E2E 测试、异常/重试/压测覆盖。

⸻

可复制的“服务就绪”YAML（放到仓库根）

tracing-readiness:
  protocol: W3C
  http: injected_and_extracted: true
  grpc: injected_and_extracted: true
  mq:
    producer_header: traceparent
    consumer_extract: true
    dlq_retry_kept_trace: true
  async:
    task_decorator: enabled
    completable_future: wrapped
    reactor: auto_ctx_propagation
    scheduler_root_span: true
  logging:
    mdc_fields: [traceId, spanId]
    json_or_pattern: unified
  sampling:
    head_based: configured
    tail_based: enabled_for_error_latency
  e2e_cases: [http_chain, http_kafka_http, retry, fanout_fanin, batch_links]
  status: green

按上述阶段推进，通常 2–4 周可以完成存量系统的统一化和异步一致性治理；关键是入口统一、上下文传播一把梭、以及用测试和指标把结论“钉住”。