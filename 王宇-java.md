# 王宇｜Java 后端｜大模型应用工程师

- 手机：178-5427-8830
- 邮箱：508284405@qq.com
- 微信：508284405
- 城市：南京（可全国远程/出差）
- 目标岗位：Java 后端｜大模型应用工程师（RAG/知识库/检索融合）

---

## 个人简介

- 7 年 Java 后端经验，熟悉 DDD 与 COLA 分层，主导企业 SaaS、IoT 与安全平台的后端架构与实现。
- 精通 Spring Boot/Cloud、MyBatis-Plus、Redis/Redisson、Kafka、Elasticsearch/ClickHouse，实践 AOP、限流熔断、任务调度与工程治理。
- 近年聚焦 RAG 与大模型应用：基于 LangChain4j 实现动态 RAG 组件工厂、RAG-Fusion、分块策略管道与 Web 搜索整合，落地可配、可观测、可扩展的检索增强能力。
- 注重可维护性与工程化：参数化配置、缓存与降级、规范与单测、日志与监控；推动团队知识沉淀与复用。

---

## 技能矩阵

- 后端：Java 17、Spring Boot/Cloud、MyBatis-Plus、AOP、WebSocket/SSE、JWT、Nacos
- 数据与消息：MySQL、Redis/Redisson（含向量检索）、Kafka、Elasticsearch/Logstash、ClickHouse
- 大模型/RAG：LangChain4j、RetrievalAugmentor、QueryRouter/Transformer、ReRanking、RAG-Fusion、SearxNG Web 搜索、知识库（分块/向量/检索）
- 工程化：DDD/COLA、分表分库、任务调度（XXL-Job）、鉴权与数据权限、CI/CD、单元测试（JUnit5/Mockito）、性能与稳定性治理
- 其他：Linux、Docker、Git/Maven；了解 Golang、脚本与网络流量解析

---

## 亮点项目｜SmartCS 智能检索增强与大模型应用平台（RAG）

- 背景：面向企业知识库问答与实时信息获取，统一支持知识库向量检索、Web 搜索与 SQL 检索，服务多 Bot 与多模型，强调动态可配与性能可控。
- 角色：Java 后端（RAG/LLM 应用），负责 RAG 架构、融合检索与工程化治理。
- 架构与实现：
  - 动态 RAG 工厂：基于 DynamicModelManager 为每个 modelId 按需创建并缓存 `QueryTransformer/QueryRouter/ContentInjector/ReRankingContentAggregator`；提供缓存统计与清理，支持模型热切换与 RAG 参数化。
  - 检索路由：使用 `LanguageModelQueryRouter` 统一路由知识库检索、SearxNG Web 搜索、SQL 检索；可按查询语义自动选择或组合；RAG 参数支持开启/关闭各检索器、配置 `topK/scoreThreshold/timeout` 等。
  - RAG-Fusion：多变体 Query 并发检索、去重融合排序、上下文构建，支持 `maxQueries/topK/similarityThreshold/maxContextTokens` 等配置，异步并发与异常隔离。
  - 知识库与分块：Redis 向量存储；重构分块策略（文本/表格/图像可组合管道），按文档类型自适配默认策略；支持 OCR、表格结构保留、句子边界对齐与块大小/重叠可配；提供上传/分块/向量化/向量检索/全文检索 REST 接口。
  - Web 搜索：集成 SearxNG（`langchain4j-community-web-search-engine-searxng`），支持公有/自托管实例、主备切换、结果量与超时配置；在 RAG 流程中自动启用。
  - 对话与工程化：`MessageWindowChatMemory` 会话记忆；SSE/WS 流式输出；配置验证与回退；关键路径日志与监控；Fusion 与配置校验单测覆盖。
- 价值与成果：
  - RAG 组件与模型解耦，灵活按调用定制 `aggregator/queryTransformer/queryRouter/web/knowledge` 等；新增模型/策略零侵入扩展。
  - 并发检索 + 融合显著提升答案相关性与上下文覆盖；Web 搜索增强事实时效，降低知识盲区。
  - 统一知识库 API 与分块管道，降低文档接入与治理成本，兼顾多模态材料（文本/表格/图像）。
- 技术栈：Java 17、Spring Boot 3.4、LangChain4j 1.1、Redis/Redisson、MyBatis-Plus、SSE/WS、MapStruct、JWT、SearxNG、Nacos（i18n/配置）

---

## 工作经历

### 丰疆智能软件科技有限公司｜Java 开发工程师｜2022.04 – 至今｜南京

- 畜牧云平台（2023.03 – 至今）
  - 背景：面向 ToB 代理商与 ToC 农场主的国际化微服务平台，管理硬件设备与牛群数据、实时告警。
  - 动作：设计用户中心与数据权限模型（组织/角色/资源层级）；落地 `MessageSource + Nacos` 动态国际化与 `Redis` 消息告警；统一 CSV 导入导出并封装组件；代码模块化与无效代码清理。
  - 价值：降低重复开发与维护成本；国际化与权限更易扩展；消息推送逻辑与框架统一，降低需求实现复杂度。
  - 技术：Spring Boot/Cloud、MyBatis-Plus、MySQL、Redis、Kafka、Nacos、AOP、i18n

- 农业 SaaS 平台（2022.04 – 2023.05）
  - 背景：面向海外企业/农场主的设备管理与任务编排 SaaS。
  - 动作：基于 `WebSocket + Redis + Kafka` 完成车机事件实时推送；用 MyBatis 拦截器自动记录历史操作；接入数仓清洗标记作业数据；`Kafka + 时间轮/XXL-Job` 实现工单过期与钉钉告警；访问统计用 bitmap 实时聚合。
  - 价值：实时性与可审计性增强；工单自动化降本提效；访问统计由延迟汇总优化为实时统计。
  - 技术：Spring Boot、MyBatis-Plus、MySQL、Redis、Kafka、WebSocket、数据仓库

### 江苏开情科技有限公司｜Java 开发工程师｜2020.06 – 2022.03｜南京

- 雨燕安全云平台
  - 背景：内网资产安全与 Web 资产安全平台，覆盖站点漏洞扫描、遗漏资产扫描与被动流量探测。
  - 动作：多租户数据权限（自定义注解 + AOP）；授权服务升级至 Spring Authorization Server（OAuth 2.1）且业务无感；报告中心（Word/HTML 模板）与延迟推送（Redis 列表）；定时任务集群去重（ZooKeeper）与失败重试。
  - 价值：数据隔离更安全、授权服务升级平滑；报告生产稳定性与效率提升；任务执行一致性增强。
  - 技术：Spring Boot/Cloud、OAuth 2.1、Redis、ZooKeeper、Poi

- IoT 物联网态势感知
  - 背景：终端资产采集与准入控制、异常告警与阻断；数据入库 ClickHouse。
  - 动作：`spring-security + oauth2 + aop` 做接口与数据权限；Kafka → ClickHouse 流水入库与 SQL 性能调优；Elasticsearch 生命周期策略与 Logstash 落盘；MQTT 命令通道与反馈可靠性（Redis 保障）；探索内核态网络阻断技术（TCP）。
  - 价值：海量数据处理链路稳定；日志与审计完善；准入控制可用性提升。
  - 技术：Spring Security、Kafka、ClickHouse、Elasticsearch/Logstash、Redis、MQTT

- 程序改造（硬件/网络方向，2021.12 – 2022.03）
  - 动作：用 Go 改造 Python 流量解析模块，统一解包框架；优化 DNS/私有协议解析；探索 pfring 降低大流量丢包；实现 TCP 阻断与重定向。
  - 价值：CPU 占用降低、吞吐提升；统一解析框架减少重复逻辑与维护成本。

### 青岛量谷网络科技有限公司｜Java 开发工程师｜2018.06 – 2020.05｜青岛

- 职责：业务模块设计与编码、接口联调与测试、上线与应急修复。
- 技术：Spring Boot、MyBatis-Plus、MySQL、Redis、前后端协作

---

## 教育经历

- 青岛大学｜本科｜食品科学与工程｜2014 – 2018

---

## 补充信息

- 工程实践：代码规范制定、代码评审、组件化与内部分享；业务异常统一（BizException）与错误码治理；配置分环境与降级回退。
- 测试与质量：单元测试（JUnit5/Mockito）、接口契约、日志追踪；重视性能与稳定性指标（延迟、吞吐、失败率）。
- 期望方向：Java 后端大模型应用（RAG/工具调用/知识库/检索融合/数据治理）；可承担架构与落地。

---

如需，我可以进一步：
- 基于具体 JD 调整关键词与亮点；
- 生成英文版 1 页要点；
- 输出 PDF 版本或将此内容写入仓库文件。

