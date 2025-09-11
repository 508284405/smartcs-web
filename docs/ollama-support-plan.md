**Ollama 支持现状评估与升级实施计划**

**背景**
- 目标：在不破坏现有 OpenAI 兼容链路的前提下，为系统提供稳定、可观测、可配置的 Ollama 聊天与向量能力，覆盖同步/流式对话与嵌入向量的业务使用场景（RAG、NLP2SQL、工具调用等）。

**现状评估**
- 依赖与测试
  - 已引入 `langchain4j-ollama` 依赖（版本由 BOM 管理，当前为 1.4.0）。参见 `smartcs-web-infrastructure/pom.xml:152`、`smartcs-web-infrastructure/pom.xml:186`。
  - 存在基于 LangChain4j 的 Ollama 向量模型单测，验证 `OllamaEmbeddingModel` 可用，包含批量与不同模型的对比用例。参见 `smartcs-web-infrastructure/src/test/java/com/leyue/smartcs/model/ai/EmbeddingModelTest.java:127`、`smartcs-web-infrastructure/src/test/java/com/leyue/smartcs/model/ai/EmbeddingModelTest.java:214`。
- 核心能力覆盖
  - 模型获取统一由 `ModelProvider` 端口抽象，当前实现为 `DynamicModelManager`。参见 `smartcs-web-infrastructure/src/main/java/com/leyue/smartcs/model/gateway/ModelProvider.java:35` 与 `smartcs-web-infrastructure/src/main/java/com/leyue/smartcs/model/ai/DynamicModelManager.java:46`。
  - 现实现仅针对“OpenAI 兼容”提供商分支，分别构建 `OpenAiChatModel`、`OpenAiStreamingChatModel`、`OpenAiEmbeddingModel`。参见：
    - Chat 构建：`smartcs-web-infrastructure/src/main/java/com/leyue/smartcs/model/ai/DynamicModelManager.java:212`
    - Streaming 构建：`smartcs-web-infrastructure/src/main/java/com/leyue/smartcs/model/ai/DynamicModelManager.java:249`
    - Embedding 构建：`smartcs-web-infrastructure/src/main/java/com/leyue/smartcs/model/ai/DynamicModelManager.java:282`
  - RAG 组件通过 `ModelProvider` 获取 Chat/Embedding 实例，升级后可透明受益。参见 `smartcs-web-infrastructure/src/main/java/com/leyue/smartcs/rag/factory/RagAugmentorFactory.java:124`、`smartcs-web-infrastructure/src/main/java/com/leyue/smartcs/rag/factory/RagAugmentorFactory.java:210`。
  - SSE 流式对话通过 `ModelProvider#getStreamingChatModel` 获取模型。参见 `smartcs-web-app/src/main/java/com/leyue/smartcs/app/executor/AiAppChatCmdExe.java:226`。
- 配置与运行
  - 全局 `application.yaml` 未显式提供 Ollama 示例（默认演示以 OpenAI 路径为主）。参见 `start/src/main/resources/application.yaml:140`。
  - Provider 枚举未包含 Ollama；`DynamicModelManager` 未实现 Ollama 构建分支。
- 风险点
  - `Provider#isValid()` 校验要求存在 API Key，Ollama 场景通常无需密钥，可能导致前后台校验不通过。参见 `smartcs-web-domain/src/main/java/com/leyue/smartcs/domain/model/Provider.java:51`。
  - 现有实现依赖“OpenAI 兼容”分支和 API Key 解密逻辑，Ollama 需要走独立分支（不解密、不校验 API Key）。

**差距清单**
- ProviderType 枚举缺少 `OLLAMA`，且无“是否需要 API Key”的能力标识。
- `DynamicModelManager` 中未覆盖 Ollama 的 Chat/Streaming/Embedding 构建逻辑。
- Provider 有效性校验未针对无需密钥的提供商（如 Ollama）放行。
- 缺少远端/本地 Ollama 运行的配置示例与操作指引（端口、超时、http client 选择）。
- 缺少模型可用性探活与错误恢复（如：服务未启动/模型未拉取的指引与提示）。
- 缺少端到端回归与可选的集成测试开关（本地无 Ollama 时应跳过）。

**设计原则**
- 保持 `ModelProvider` 端口稳定，上层调用点零改动。
- 以 `ProviderType` 为路由键，在 `DynamicModelManager` 内分支构建不同厂商模型实例。
- 对 Ollama 采用 LangChain4j 官方实现（`OllamaChatModel`、`OllamaStreamingChatModel`、`OllamaEmbeddingModel`）。
- 安全默认：仅对需要 API Key 的厂商走解密流程；Ollama 不触发解密。
- 可观测：构建/调用处补充关键日志与可选健康探测，便于排障。

**实施方案**
- Domain 层
  - 在 `ProviderType` 增加 `OLLAMA("ollama", "Ollama")`；新增 `requiresApiKey()` 方法，默认 `true`，对 `OLLAMA` 返回 `false`。参见 `smartcs-web-domain/src/main/java/com/leyue/smartcs/domain/model/enums/ProviderType.java:1`。
  - 调整 `Provider#isValid()`：若 `!providerType.requiresApiKey()` 则仅校验 `endpoint` 非空；否则沿用现有密钥校验。参见 `smartcs-web-domain/src/main/java/com/leyue/smartcs/domain/model/Provider.java:51`。
- Infrastructure 层
  - `DynamicModelManager` 新增 Ollama 分支：
    - Chat：构建 `OllamaChatModel.builder().baseUrl(endpoint).modelName(label)...build()`。
    - Streaming：构建 `OllamaStreamingChatModel`（或 LangChain4j 1.4.0 中对应的 Streaming 实现）。
    - Embedding：构建 `OllamaEmbeddingModel.builder().baseUrl(endpoint).modelName(label)...build()`。
    - 推荐为 Ollama 显式设置 `httpClientBuilder(new dev.langchain4j.http.client.jdk.JdkHttpClientBuilder())`，与现有单测保持一致，避免 http 客户端冲突。参考 `smartcs-web-infrastructure/src/test/java/com/leyue/smartcs/model/ai/EmbeddingModelTest.java:227`。
  - 构建分支内移除 API Key 解密逻辑；增加构建日志与异常描述（明确提示“服务未启动或模型未下载”）。
- App/Adapter 层
  - 无需代码改动；`AiAppChatCmdExe`、`RagAugmentorFactory` 等均通过 `ModelProvider` 取模型，自动兼容。
- 配置与文档
  - 在 `docs` 添加运行指引与配置示例（本文件附录）。
  - 可选：在 `application.yaml` 示例中加入注释块，指明如何以数据库 Provider/Model 方式配置 Ollama（`endpoint: http://localhost:11434`）。参见 `start/src/main/resources/application.yaml:1`。
- 测试
  - 单元测试：
    - 为 `DynamicModelManager` 增加针对 `OLLAMA` 的构建分支测试（Mock `ModelGateway`、`ProviderGateway`，断言返回类型为 Ollama 模型）。
    - Provider 校验：`ProviderType#requiresApiKey` 与 `Provider#isValid` 在 OLLAMA 下不要求密钥。
  - 集成测试（可选、带跳过）：
    - 若环境变量 `OLLAMA_BASE_URL` 未设置或连接失败，跳过相关 IT；否则执行对话与嵌入的端到端校验。

**改动细节（伪代码/要点）**
- ProviderType
  - 添加：`OLLAMA("ollama", "Ollama")`
  - 方法：
    - `public boolean requiresApiKey() { return this != OLLAMA; }`
    - `public boolean isOpenAiCompatible() { ... }` 保持不变或不含 OLLAMA（我们走 Ollama 专有实现）。参见 `smartcs-web-domain/src/main/java/com/leyue/smartcs/domain/model/enums/ProviderType.java:1`。
- Provider
  - `isValid()`：当 `!providerType.requiresApiKey()` 时，放宽为仅校验 `endpoint` 非空。参见 `smartcs-web-domain/src/main/java/com/leyue/smartcs/domain/model/Provider.java:51`。
- DynamicModelManager
  - 在 `buildChatModel`/`buildStreamingChatModel`/`buildEmbeddingModel` 里增加 `if (provider.getProviderType() == ProviderType.OLLAMA) { ... }` 分支，返回对应 Ollama 模型实例。参见 `smartcs-web-infrastructure/src/main/java/com/leyue/smartcs/model/ai/DynamicModelManager.java:212`、`:249`、`:282`。
  - 构建参数：`baseUrl = provider.getEndpoint()`；`modelName = model.getLabel()`；必要时设置 `timeout` 与 `httpClientBuilder`。
  - 日志：成功/失败均打印 providerId、modelId、端点与模型名，方便定位。

**配置与运行指引（示例）**
- 本地运行 Ollama
  - 安装并启动 Ollama 服务，默认监听 `http://localhost:11434`。
  - 拉取所需模型（示例）：
    - `ollama pull llama3`
    - `ollama pull qwen2`
    - `ollama pull nomic-embed-text`
- 在系统中新增 Provider/Model（通过管理界面或初始化脚本）：
  - Provider：`type=OLLAMA`，`endpoint=http://localhost:11434`，`supportedModelTypes=chat,embedding`，无需 API Key。
  - Model：
    - 聊天模型：`label=llama3`（示例）
    - 向量模型：`label=nomic-embed-text` 或 `mxbai-embed-large`
- 可选环境变量（用于 IT 或本地快捷配置）：
  - `export OLLAMA_BASE_URL=http://localhost:11434`

**验收标准**
- 支持通过 `ProviderType=OLLAMA` 配置出 Chat/Streaming/Embedding 模型并在以下路径稳定工作：
  - SSE 流式对话：`smartcs-web-app/src/main/java/com/leyue/smartcs/app/executor/AiAppChatCmdExe.java:226`
  - RAG 查询转换与检索：`smartcs-web-infrastructure/src/main/java/com/leyue/smartcs/rag/factory/RagAugmentorFactory.java:210`
  - 嵌入与相似检索：`smartcs-web-infrastructure/src/main/java/com/leyue/smartcs/rag/database/service/TableSchemaVectorizationService.java:98`
- 未配置/服务不可用时，具备清晰错误信息与可观测日志；单测通过、IT 可在本地跳过。

**交付物**
- 代码：ProviderType 扩展、DynamicModelManager 分支、Provider 校验放宽、必要的日志与异常处理。
- 文档：本实施计划 + 运行指南（附录）。
- 测试：单测 + 可选集成测试（带条件跳过）。

**里程碑与工期预估**
- M1（0.5d）：Domain 与 Manager 改造，基础单测覆盖。
- M2（0.5d）：运行指引与日志/错误信息优化，回归 RAG & SSE 流程。
- M3（可选 0.5d）：集成测试（本地有 Ollama 时启用），简单健康检查与告警接入。

**附录：模型与参数建议**
- 嵌入模型：`nomic-embed-text`（通用，性能均衡）、`mxbai-embed-large`（效果更好，资源更高）。
- 聊天模型：`llama3`、`qwen2`、`qwen2.5-coder`（代码/SQL 相关更优）。
- 连接参数建议：
  - `timeout`: 60s~120s（批量嵌入或长上下文）
  - `httpClientBuilder`: JDK HttpClient，保持与单测一致，减少冲突
  - `num_ctx`/`temperature` 等模型参数按需在 builder 中扩展

**实施完成状态（✅ 已完成）**

后端 Ollama 集成已全部完成，具体实现内容：

1. **域模型扩展**：
   - ✅ `ProviderType` 枚举添加 `OLLAMA` 支持
   - ✅ 添加 `requiresApiKey()` 方法，区分不同提供商的认证需求
   - ✅ `Provider.isValid()` 方法支持无需 API Key 的提供商验证

2. **基础设施层实现**：
   - ✅ `DynamicModelManager` 添加 Ollama 分支构建逻辑
   - ✅ 实现 `OllamaChatModel`、`OllamaStreamingChatModel`、`OllamaEmbeddingModel` 构建
   - ✅ 添加 `langchain4j-http-client-jdk` 依赖支持
   - ✅ 完善错误处理和日志记录

3. **配置和文档**：
   - ✅ 在 `application.yaml` 中添加 Ollama 配置示例和使用说明
   - ✅ 更新本实施计划文档，记录完成状态

4. **测试覆盖**：
   - ✅ 创建 `DynamicModelManagerTest` 单元测试，覆盖 Ollama 分支
   - ✅ 创建 `ProviderTest` 测试，验证 Ollama 提供商验证逻辑

**使用指南**

通过管理界面配置 Ollama：

1. **安装和启动 Ollama**
   ```bash
   # 安装 Ollama（根据操作系统）
   curl -fsSL https://ollama.ai/install.sh | sh
   
   # 启动服务
   ollama serve  # 默认监听 http://localhost:11434
   
   # 拉取模型
   ollama pull llama3          # 聊天模型
   ollama pull nomic-embed-text  # 嵌入模型
   ```

2. **在系统中配置 Provider**
   - 访问管理界面的提供商管理
   - 创建新提供商：
     - 类型：选择 "Ollama"
     - 名称：如 "本地 Ollama"
     - 端点：`http://localhost:11434`
     - 支持模型类型：选择 "大语言模型" 和/或 "文本向量化"
     - 无需设置 API Key

3. **配置 Model**
   - 在模型管理中创建新模型：
     - 关联到上述 Ollama 提供商
     - 模型名称：输入已拉取的模型名（如 `llama3`、`nomic-embed-text`）
     - 模型类型：根据模型选择对应类型

4. **验证集成**
   - 系统会自动使用配置的 Ollama 模型进行推理
   - 查看日志确认模型构建成功
   - 在 RAG、聊天等功能中测试模型响应

**技术要点**
- Ollama 集成使用 LangChain4j 官方实现，保持架构一致性
- 通过 `ModelProvider` 接口抽象，上层业务代码无需修改
- 支持同步和流式聊天，以及文本嵌入功能
- 包含完整的错误处理和可观测性支持

