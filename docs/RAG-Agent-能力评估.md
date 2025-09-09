# SmartCS RAG/Agent 能力评估

更新时间：自动生成于当前仓库代码基础（概览性评估）

## 结论
- 项目已具备“Agentic RAG + ReAct 工具调用”的核心能力（RAG、检索路由、记忆、工具、安全与观测），可覆盖多数问答与工具增强场景。
- 但尚未形成“完整通用 Agent 平台”：缺少通用的计划/编排（planner/supervisor）、健壮的并发工具执行与恢复、MCP 客户端生态对接、长任务与多回合策略、多代理协作等。

## 已具备的能力（要点）
- RAG 组装与路由
  - 工厂化装配 RetrievalAugmentor/QueryTransformer/ContentInjector/重排序聚合器；查询路由可在 知识库 / Web 搜索 / SQL 之间选择。
  - 参考：`smartcs-web-infrastructure/.../rag/factory/RagAugmentorFactory.java`。
- 查询转换器管线（可配置、可观测、可降级）
  - 阶段包含：标准化、语义对齐、意图抽取、拼音改写、前缀补全、近义召回、扩展等；带超时/降级与指标采集。
  - 参考：`smartcs-web-infrastructure/.../rag/query/pipeline/*`。
- 工具与 ReAct
  - 通过 `AiServices.builder(...).tools(...)` 将工具与 RAG、记忆一并注入到 `SmartChatService`，支持模型驱动的工具调用。
  - 已有示例工具：订单、SQL（MCP Server 侧）、天气等；带安全校验、审计与限流组件。
  - 参考：`smartcs-web-app/.../AiAppChatCmdExe.java`、`smartcs-web-infrastructure/src/main/java/com/leyue/smartcs/mcp/*`。
- 记忆与稳健性
  - Redis ChatMemoryStore + Resilience4j 降级至内存实现。
  - 参考：`smartcs-web-infrastructure/.../rag/memory/*`。
- 评估与可观测
  - AOP 采集 RAG 事件与检索耗时，配合基线评估脚本形成质量闸门。
  - 参考：`smartcs-web-app/.../RagEventCollectorAspect.java`、`scripts/baseline-eval.sh`。
- 多模态基础
  - 文本+图片流式对话已打通（图片路径当前直接走底层 StreamingChatModel）。

## 与“完整 Agent 平台”的差距
- 工具装配不够通用：`enabledTools` 主要包含订单工具，其他样例（如天气、SQL）未统一纳入装配清单。
- MCP 仅有 Server 侧（SQL 查询）可用；`DefaultMcpManager` 客户端为占位实现，未与外部 MCP 工具生态打通（资源/工具发现与调用为模拟）。
- Web 搜索默认关闭；不启用时查询路由的“实时信息”价值受限（`smartcs.ai.web-search.enabled`）。
- LangChain4j 1.4 的工具并发/错误处理/短路返回等新特性尚未落地到主路径。
- 缺少显式的计划/编排（planner/supervisor/任务图）、预算与多回合 Tool-Loop 治理、长任务控制与恢复策略。
- 多模态路径尚未与注解式 SmartChatService 的 RAG/工具/记忆装配完全统一。

## 建议与下一步
- 扩展工具装配
  - 将 `WeatherToolsService`、SQL 工具统一加入 `enabledTools` Bean，或改为基于注解自动收集 `@Tool` Bean 注入 `AiServices`。
- 打通 MCP 客户端
  - 为 `DefaultMcpManager` 接入真实 HTTP/SSE 传输与外部 MCP Server，启用资源/工具发现与调用；统一安全、审计与限流。
- 启用并验证查询路由
  - 在 `start/src/main/resources/application.yaml` 打开 `smartcs.ai.web-search.enabled: true`，配置 SearxNG，验证 KB/Web/SQL 路由与重排序聚合效果。
- 强化 Agentic 能力
  - 采用 LangChain4j 1.4 工具并发/错误处理/短路返回；增加 tool-loop 预算、重试与回退策略；必要时引入轻量 planner/supervisor 装饰器。
  - 将图片路径接入与文本一致的注解式服务/记忆/RAG，或在多模态流中追加检索增强。
- 观测与质量
  - 按“RAG 质量闸门”基线扩展自动化评估集，持续跟踪 Context Precision/Recall、Faithfulness、Answer Relevancy 等关键指标。

## 快速自测清单
- 工具链路
  - 运行 `smartcs-web-app/src/test/java/.../ReActIntegrationTest.java`；在有模型配置时，通过聊天触发“查询订单/取消订单”等语句，观察工具调用、安全与审计日志。
- RAG 路由
  - 提问带“需要最新信息/数据库数据”的问题，验证是否触发 Web/SQL 检索与重排序聚合。
- 记忆与稳健性
  - 多轮对话验证 Redis 记忆与降级逻辑（Redis 故障时回退内存）。
- 配置提示
  - Web 搜索：`smartcs.ai.web-search.enabled: true`（默认关闭）。
  - MCP SQL Server：`/mcp/sql/*` 路径已注册；客户端侧能力待打通后再测联调。

> 注：以上评估基于当前代码结构与配置，旨在为后续增强工作提供导航与优先级建议。

