# LangChain4j 1.4.0 升级分析与实施计划

更新时间：2025-09-01
适用范围：smartcs-web 全仓（client/domain/infrastructure/app/adapter/start）

---

## 1. 1.4.0 更新摘要（来源：官方 GitHub Releases 1.4.0）

以下为与本项目最相关的更新点，便于评估影响与收益：

- Agentic/代理编排
  - 增强声明式 API 能力；可在返回前操控 supervisor 输出。
- MCP（Model Context Protocol）
  - 新增可流式的 HTTP 传输。
- AI Service（声明式服务接口）
  - 支持并发执行工具（tools）。
  - 支持 tool 的参数与执行错误处理器。
  - 支持工具即时/直接返回（short-circuit）。
  - 新增多模态支持（图片/文本混合）。
- 结构化输出
  - 新增原生 JSON Schema 元素（用于更严格的结构化输出约束）。
- 新增/增强集成
  - 新增 watsonx.ai 模块；
  - Azure OpenAI 音频转写模型；
  - Anthropic 计费/分词统计。
- 重要变更（Breaking Changes）
  - AI Service tool 参数与错误处理相关的 API 变更；
  - Agentic 声明式 API 行为修正；
  - 默认内部执行器改为虚拟线程池（virtual threads）。
- 其他值得关注
  - OpenAI Java SDK 升级到 3.1.2；
  - OpenAI ChatModel 支持设置 JSON Schema；
  - 允许传入 null memory/memory provider；
  - 日志增强（可配置请求/响应日志）；
  - 多项 bugfix 与测试覆盖度提升。

参考：GitHub Releases Tag 1.4.0（已获取）。

---

## 2. 当前系统对 LangChain4j 1.1.0 的使用情况

- 依赖管理（根 POM）
  - `langchain4j-bom`: 1.1.0（属性：`<langchain4j.version>1.1.0</langchain4j.version>`）
  - `langchain4j-community-bom`: 1.1.0-beta7
  - `langchain4j-redis`: 1.0.0-alpha1（单独指定版本）
- 基础设施模块（smartcs-web-infrastructure/pom.xml）已用到的组件
  - 核心/Starter：`langchain4j`, `langchain4j-spring-boot-starter`, `langchain4j-open-ai-spring-boot-starter`
  - 文档解析：`langchain4j-document-parser-apache-pdfbox/tika/poi`
  - 向量存储：`langchain4j-redis`
  - RAG：`langchain4j-easy-rag`
  - 社区组件：`langchain4j-community-web-search-engine-searxng`（当前显式版本：1.0.0-beta4）
- 代码使用面（核心触点与典型文件）
  - 声明式 AI Service（AiServices）与提示注解
    - `dev.langchain4j.service.AiServices`、`@SystemMessage`、`@UserMessage`、`V`
    - 示例：`IntentClassificationAiService.java`、`LangChain4jModerationService.java`
  - Chat/Streaming Chat/Embedding 模型
    - `ChatModel`、`StreamingChatModel`、`EmbeddingModel`
    - OpenAI 实现：`OpenAiChatModel`、`OpenAiStreamingChatModel`、`OpenAiEmbeddingModel`
    - 示例：`DynamicModelManager.java`
  - 流式输出
    - `dev.langchain4j.service.TokenStream`（SSE 回传），示例：`AiAppChatCmdExe.java`
  - 记忆（Memory）
    - `MessageWindowChatMemory`、`ChatMemoryStore`（已注入/使用）
  - RAG
    - `dev.langchain4j.rag.RetrievalAugmentor`、`langchain4j-easy-rag`（SmartChatService 内部）
  - 文档解析/分块
    - `DocumentSplitters` 与 Apache PDFBox/Tika/POI 解析适配（如 `LangChain4jDocumentParserAdapter.java`、`LangChain4jChunkingStrategy.java`、`TextContentChunkingStrategy.java`）
  - 多模态
    - `dev.langchain4j.data.message.ImageContent/TextContent/UserMessage` 已在 PDF Parser 中引入
  - 向量检索
    - `EmbeddingStore`, `EmbeddingSearchRequest`, `EmbeddingMatch`（如 `TextSearchQryExe.java`、`FaqSearchQryExe.java`）

- 初步兼容性评估
  - 我们未使用 tools（工具调用），因此 AI Service 的 tool 相关破坏性变更对现有代码影响有限。
  - Chat/Embedding API 使用均为主路径（Builder/基本方法），预期与 1.4.0 兼容。
  - 默认执行器切换至虚拟线程：需关注 ThreadLocal/上下文传播（本项目有 `transmittable-thread-local` 依赖，但库内部虚拟线程不一定承载上下文）。
  - `langchain4j-redis` 仍为 `1.0.0-alpha1`，需要在 1.4.0 下验证二进制兼容；如有问题需切换到其他受支持的 store 或使用 community-bom 管理的实现。

---

## 3. 升级实施计划（分步详解）

### A. 准备与基线
- 建立分支：`feat/langchain4j-1.4.0-upgrade`。
- 标记里程碑：编译通过、冒烟通过、RAG 质量回归、性能回归、灰度完成。
- 环境变量确认：`OPENAI_API_KEY`（可选 `OPENAI_BASE_URL`）。

### B. 版本对齐与依赖调整
- 根 `pom.xml`
  - 将 `langchain4j.version` 更新为 `1.4.0`：
    - `<langchain4j.version>1.4.0</langchain4j.version>`
  - 将 `langchain4j-community-bom` 更新为 `1.4.0-beta10`（与 1.4.0 同步发布节奏）：
    - `<artifactId>langchain4j-community-bom</artifactId>` → 版本 `1.4.0-beta10`
  - `langchain4j-redis`：保留 `1.0.0-alpha1` 先行验证；若不兼容：
    - 方案1：查验是否已有与 1.4.0 对齐的新版本；
    - 方案2：短期改用内存/文件/PGVector 等其它 store（由 community-bom 管理的实现）。
- `smartcs-web-infrastructure/pom.xml`
  - 移除能被 BOM 管理的组件的显式版本，或将 `searxng` 对齐到 `1.4.0-beta10`（若不使用 BOM 管理）。
  - 保持：`langchain4j`, `langchain4j-spring-boot-starter`, `langchain4j-open-ai-spring-boot-starter`, 文档解析模块。

### C. 编译与静态检查
- 执行：`mvn -q -DskipTests compile`
- 若编译失败，按以下优先级修复：
  - `AiServices` 接口：保持 `@SystemMessage/@UserMessage/@V` 用法；如有方法签名/包路径变更，按官方迁移说明调整。
  - `ChatResponse`/`StreamingChatResponseHandler` 包或方法变动：调整导入与调用。
  - 文档解析模块包名/工厂方法若有变动：按新 API 调整。
  - 向量检索 `EmbeddingStore`/`EmbeddingSearchRequest`：校对构建器与返回值类型是否一致。

### D. 冒烟与功能验证
- 核心路径
  - 普通对话（`ChatModel.chat`）与流式输出（`TokenStream` + SSE）。
  - 意图分类（`IntentClassificationAiService`）：JSON 解析/置信度计算稳定。
  - RAG 对话（SmartChatService + easy-rag）：召回/重排序/引用输出正确。
  - 知识库检索：`EmbeddingStore.search` 返回稳定，打分阈值可用。
  - 文档解析：PDF/TXT/HTML/DOCX 等常见格式解析与分块。
- 多模态（若启用）：
  - PDF 图片提取 + 图像描述提示词；验证大图片/多图片页的稳定性与吞吐。

### E. 性能与线程模型验证
- 重点关注：虚拟线程默认执行器对以下方面的影响：
  - SSE 推送回压/并发稳定性；
  - 日志上下文/租户上下文的 ThreadLocal 传播（必要时避免依赖 ThreadLocal，或改为参数传递）。
- 指标观察：接口 P99、内存占用、GC、线程数、向量检索耗时。

### F. 观测性与诊断
- 如需：启用模型请求/响应日志（1.4.0 新增可配置 logger）。
- 对 OpenAI ChatModel 的结构化输出场景：尝试设置 JSON Schema（更严格的输出约束）。

### G. 安全与配置
- 校对 `OpenAI SDK Java 3.1.2` 升级后的代理/超时/重试配置项；
- 不在仓库提交任何密钥（遵循现有安全模板与加载流程）。

### H. 回滚与降级预案
- 若遇重大不兼容：
  - 立即回滚分支；
  - 或将 `langchain4j.version` 回退至 1.1.0，保留对代码的兼容性修正（前向兼容）。
- 向量存储不兼容：临时切换为内存/文件存储，保障核心对话链路。

---

## 4. 代码变更建议（示例片段）

- 根 `pom.xml`
  - 属性：
    - `langchain4j.version`: `1.1.0` → `1.4.0`
  - 依赖管理：
    - `langchain4j-community-bom`: `1.1.0-beta7` → `1.4.0-beta10`
  - 若使用 BOM 管理 `searxng`，可去除其显式版本；否则更新到 `1.4.0-beta10`。

- `smartcs-web-infrastructure/pom.xml`
  - 由 BOM 管理的 LangChain4j 组件去除显式版本。
  - 暂留 `langchain4j-redis` 为 `1.0.0-alpha1`，等待兼容性验证结果。

---

## 5. 验收与里程碑

- M1 编译通过：`mvn -q -DskipTests compile`
- M2 冒烟通过：对话/流式/RAG/检索/解析
- M3 回归通过：关键业务链路 + 指标恢复
- M4 可观测性增强项落地（可选）：结构化输出 JSON Schema、请求/响应日志
- M5 灰度与稳定性观察（1-3 天），全量发布

---

## 6. 附录：与本项目强相关的 1.4.0 变化映射

- 我们当前使用的能力与 1.4.0 对照：
  - AiServices 注解式对话：继续可用；如后续引入工具调用，可直接使用 1.4.0 新特性（并发、错误处理、直接返回）。
  - OpenAI Chat/Streaming/Embedding：Builder 与常用参数保持；可按需启用 JSON Schema/日志等增强。
  - RAG（easy-rag）：与 1.4.0 生态兼容，按 BOM 升级并实测。
  - 文档解析与分块：解析模块名与 API 需在编译期校验（预期兼容）。
  - 向量存储（Redis）：当前版本较旧，重点做二进制兼容验证；必要时切换到其他 store。
  - 线程模型：虚拟线程对 ThreadLocal 传播有潜在影响，避免依赖 ThreadLocal。

---

## 7. 执行清单（Checklist）

- 依赖升级
  - [ ] 更新 `langchain4j.version` 到 `1.4.0`
  - [ ] 更新 `langchain4j-community-bom` 到 `1.4.0-beta10`
  - [ ] 校验 `langchain4j-redis` 兼容性（必要时替代方案）
  - [ ] 对齐 `searxng` 至与 BOM 一致的版本
- 编译与修复
  - [ ] 编译通过，无弃用/破坏性 API 问题
  - [ ] 修正因包名/签名变化导致的编译错误
- 功能冒烟
  - [ ] 对话/流式输出
  - [ ] 意图分类
  - [ ] RAG 对话
  - [ ] 向量检索
  - [ ] 文档解析/分块
- 非功能验证
  - [ ] 性能/并发（SSE + 虚拟线程）
  - [ ] 日志/观测（可选）
  - [ ] 配置/安全
- 灰度与回滚
  - [ ] 小流量灰度与指标观察
  - [ ] 回滚预案演练

---

备注：如需采纳 1.4.0 新特性（tools 并发/错误处理、JSON Schema 结构化输出、可配置日志、MCP 流式 HTTP 等），建议在主升级完成后开独立任务完成增量改造与验收。

