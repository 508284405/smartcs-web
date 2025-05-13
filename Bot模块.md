
9. Bot-Service 模块详细设计

9.1 模块简介

Bot-Service 是智能问答的执行引擎，负责接收用户或 Session-Service 转发的“机器人接管”请求，调用知识域检索、拼装 Prompt，驱动 LLM（如 GPT）产生回答，并处理回复格式、上下文管理与缓存。使用spring-ai框架实现。

9.2 核心职责
	•	接收会话域或前端转发的机器人会话请求
	•	从 Knowledge-Service 获取 Top-K 检索结果与原始文本
	•	拼装多轮对话 Prompt 模板，支持上下文记忆
	•	调用 LLM 接口，处理流式/非流式输出
	•	格式化模型响应（文本、卡片、链接等）并推送给 Chat-Service
	•	将响应与用户问题一起缓存到 Redis，支持上下文续聊与降级重试

9.3 架构组件

flowchart TB
  subgraph Bot-Service
    API[REST / gRPC 接口]
    Core[业务逻辑层]
    PromptBuilder[Prompt 构建器]
    LLMClient[LLM 客户端]
    KSClient[Knowledge 客户端]
    SessionClient[Session 客户端]
    Cache[Redis 缓存层]
  end
  subgraph Infra
    Redis[(Redis Cluster)]
    KS[Knowledge-Service]
    SS[Session-Service]
  end

  API --> Core
  Core --> KSClient
  Core --> SessionClient
  Core --> PromptBuilder
  PromptBuilder --> LLMClient
  LLMClient --> Core
  Core --> Cache
  Cache --> Redis

	•	API：对外暴露 POST /api/bot/chat 接口，接收用户问题与会话上下文
	•	Core：协调检索、构建 Prompt、调用 LLM、结果后处理
	•	KSClient：调用 Knowledge-Service 提供的向量／全文检索接口
	•	SessionClient：获取会话上下文（历史消息）及用户画像
	•	PromptBuilder：根据 bot_prompt_template 表和多轮上下文模板拼接 Prompt
	•	LLMClient：封装对接 OpenAI／自部署模型的认证、限流、重试逻辑
	•	Cache：将用户提问 + 模型回答缓存至 Redis，Key 格式 bot:ctx:{sessionId}，TTL 可配置

9.4 数据模型

虽然 Bot-Service 不直接持久化业务数据，但可维护以下元数据表，支持 Prompt 与策略动态配置：

CREATE TABLE bot_prompt_template (
  id              BIGINT       PRIMARY KEY AUTO_INCREMENT,
  is_deleted      TINYINT(1)   DEFAULT 0,
  created_by      VARCHAR(64),
  updated_by      VARCHAR(64),
  created_at      BIGINT       COMMENT '毫秒时间戳',
  updated_at      BIGINT       COMMENT '毫秒时间戳',
  template_key    VARCHAR(64)  UNIQUE COMMENT '模板标识，如:RAG_QUERY_MULTI_ROUND',
  template_content TEXT         COMMENT 'Prompt 模板，支持变量占位，如{{history}}、{{docs}}、{{question}}'
);

9.5 接口设计

方法	路径	描述	参数示例
POST	/api/bot/chat	机器人问答入口，返回模型回答	{sessionId:12345, question:"如何退款？", history:true}
GET	/api/bot/context/{id}	获取会话上下文缓存（提问+回复）	sessionId
DELETE	/api/bot/context/{id}	删除会话上下文缓存	sessionId

9.6 业务流程

sequenceDiagram
  participant User
  participant BotAPI
  participant Core
  participant Session
  participant KS
  participant Builder
  participant LLM
  participant Cache
  participant Chat

  User->>BotAPI: POST /api/bot/chat {sessionId, question}
  BotAPI->>Core: handleRequest()
  Core->>Session: 获取历史上下文
  Session-->>Core: history[]
  Core->>KS: vectorSearch(question)
  KS-->>Core: docs[topK]
  Core->>Builder: buildPrompt(history, docs, question)
  Builder-->>Core: promptText
  Core->>LLM: generateAnswer(promptText)
  LLM-->>Core: answerText
  Core->>Cache: 保存 history + answer
  Core->>Chat: 推送消息(answerText)
  Chat-->>User: 显示机器人回答

9.7 性能与可用性
	•	并发控制：LLMClient 内置令牌桶限流（Token Bucket），防止突发调用超额
	•	超时与重试：对 LLM 调用设置超时（如 3s），失败时降级到 FAQ 缓存或全文检索回答
	•	多模型支持：可按场景动态切换 Embedding/LLM 模型，支持灰度与 AB 测试
	•	缓存优化：会话上下文和 Top-K 检索结果缓存，降低重复调用成本
	•	高可用：部署多实例，启用 Kubernetes HPA 自动扩缩容

9.8 安全与监控
	•	安全：调用 LLM API 时使用服务间 Token，避免暴露用户密钥；接口全部需 JWT 鉴权
	•	审计：对每次问答调用进行日志记录（包括 Prompt、模型 ID、用时、Token 消耗）
	•	指标：Prometheus 监控 QPS、RT、Error Rate，Grafana 展示模型调用耗时分布
	•	链路追踪：通过 Jaeger 采集 Bot-Service 到 Knowledge/Session/LLM 的调用链

⸻

上述内容可直接补充到文档第 9 章，若需调整层次或进一步示例，请告知！