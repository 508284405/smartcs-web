# QueryTransformer 能力增强实施计划（e/f/h）

本文面向 RAG 查询改写与扩展能力的短板（e/f/h）给出详细实施方案、里程碑、设计说明与验收标准。

- e：拼音改写（容错纠错：同音/形近 → 正确实体）
- f：前缀补全（Trie 前缀树 + 领域词典）
- h：近义词召回（基于嵌入的相似词扩展）

同时统一动态管线装配，使运行时与 Spring Bean 装配的阶段集保持一致。

---

## 1. 背景与现状

当前 QueryTransformer 具备：
- 标准化（NormalizationStage）：清洗、简繁/全半角统一、轻拼写纠错、停用词等。
- 语义对齐（SemanticAlignmentStage）：同义/别名归一、单位/时间规范化。
- 意图抽取（IntentExtractionStage）：意图分类、实体与槽位抽取。
- 可检索化改写（RewriteStage）：口语→检索表达、负向/必含词抽取、关键词权重、子查询分解。
- 扩展（ExpandingStage）：LLM 查询扩展；可选高级策略（ExpansionStrategyStage）。

仍缺（或未接入）的能力：
- e：拼音改写；
- f：前缀补全；
- h：近义词召回；
- 动态管线与 Spring Bean 装配阶段不一致（DynamicModelManager 仅“标准化+扩展”）。

---

## 2. 目标与范围

### 2.1 目标
- 覆盖 e/f/h 三类增强能力，提升召回鲁棒性与用户输入容错。
- 与现有阶段解耦为独立 Stage，可按租户/渠道配置启停。
- 保证可观测（指标）、可降级（失败不影响主流程）。

### 2.2 范围
- 代码实现阶段：Service + Stage + 配置 + 动态装配 + 基础词典/索引构建脚本。
- 测试与灰度：单测/集成、性能基准、灰度开关与监控上报。

---

## 3. 路线图与里程碑（Milestones）

| 里程碑 | 交付物 | 说明 |
|---|---|---|
| M1 配置与骨架 | 配置项、DTO、空 Stage 骨架 | 新开关与参数、管线挂点确定 |
| M2 e 拼音改写 | PhoneticCorrectionService/Stage | 词典与拼音索引、纠错算法与指标 |
| M3 f 前缀补全 | PrefixCompletionService/Stage | Trie 前缀树、领域词典与排序 |
| M4 h 近义词召回 | SynonymRecallService/Stage | TermEmbeddingIndex 与近邻检索 |
| M5 融合与约束 | 统一语义去重与上限 | 与现有 Expanding/Rewrite 结果融合 |
| M6 动态装配一致性 | DynamicModelManager 接入 | 与 Spring Bean 装配一致 |
| M7 测试与基准 | 单测/集成/性能 | 指标面板初版 |
| M8 发布与观测 | 灰度开关/文档 | 分租户放量、回滚预案 |

---

## 4. 设计总览

### 4.1 新增配置（RagComponentConfig.QueryTransformerConfig）
- `enablePhoneticCorrection`（Boolean，默认 false）
- `enablePrefixCompletion`（Boolean，默认 false）
- `enableSynonymRecall`（Boolean，默认 false）
- `phonetic.minConfidence`（Double，默认 0.6）
- `phonetic.maxCandidates`（Integer，默认 3）
- `prefix.minPrefixLength`（Integer，默认 2）
- `prefix.maxCandidates`（Integer，默认 5）
- `synonym.embeddingModelId`（Long，默认使用会话模型）
- `synonym.topK`（Integer，默认 5）
- `synonym.simThreshold`（Double，默认 0.7）

在 `QueryContext.PipelineConfig` 映射对应开关参数，便于阶段读取。

### 4.2 新增阶段
- `PhoneticCorrectionStage`：位于 NormalizationStage 之后、RewriteStage 之前。
- `PrefixCompletionStage`：位于 RewriteStage 之后、ExpandingStage 之前（或与 RewriteStage 并列）。
- `SynonymRecallStage`：位于 IntentExtraction/Rewrite 之后（复用关键词/实体），在 ExpandingStage 之前。

### 4.3 动态装配一致性
在 `DynamicModelManager#createQueryTransformerPipeline`：
- 按 `QueryTransformerConfig` 开关，装配：Normalization → SemanticAlignment → IntentExtraction → Rewrite → [Phonetic] → [Prefix] → [SynonymRecall] → Expanding → [ExpansionStrategy]。
- 保持与 `QueryTransformerConfiguration`/`EnhancedQueryTransformerConfig` 的阶段序与默认启停一致（除非前端明确关闭）。

---

## 5. 组件设计

### 5.1 PhoneticCorrectionService（e）
- 词典与索引：
  - 词典来源：KB 标题、实体主名/别名、业务词表（可分租户）。
  - 建索引：为每条词条生成拼音（全拼/首字母）与简化形近映射，内存索引 + 本地快照。
- 算法：
  - 对 OOV/疑似错拼词：计算与候选词的拼音编辑距离、形近字替换代价，融合频率/领域权重，得到置信度。
  - 低于阈值不改写；高于阈值生成“纠正版”候选（TopN）。
- Stage 行为：
  - 输入 Query 列表，输出包含“纠正后的替代查询”（保留原始查询），并记录纠错对齐信息到 `QueryContext`（用于后续检索器特征）。
- 配置：
  - `minConfidence`、`maxCandidates`、白/黑名单（避免过纠）。
- 指标：命中率、过纠率、延迟 P95。

### 5.2 PrefixCompletionService（f）
- 索引：
  - Trie（前缀树），节点存储词频/打分，支持热更新；多词典分租户加载。
- 算法：
  - 识别最后 token 或短查询；进行前缀匹配，按词频/质量排序；去除噪声（过短/过长/停用）。
- Stage 行为：
  - 输出少量补全候选 Query（建议扩展类型），参与最终融合与上限控制。
- 配置：
  - `minPrefixLength`、`maxCandidates`、是否仅短查询触发。
- 指标：触发率、候选质量（点击/命中）与延迟。

### 5.3 SynonymRecallService（h）
- 词表与嵌入：
  - 从 KB/实体库抽取候选词（主名/别名/高频词），生成 term embedding，落地 `TermEmbeddingIndex`（内存 + 本地文件）。
- 近邻检索：
  - 对 Query 的关键词/实体做向量近邻（余弦相似度），TopK + 阈值过滤；可补充 SimHash 粗筛文本相似。
- Stage 行为：
  - 将近义词作为“扩展词”构造等义变体查询，或写入 `QueryContext` 供检索器 BM25 should 子句使用。
- 配置：
  - `embeddingModelId`、`topK`、`simThreshold`、黑名单词类（停用/泛词）。
- 指标：近义词 TopK 质量评估（人工/回放集）、延迟、召回贡献度。

---

## 6. 融合与约束

- 统一去重：
  - 文本归一 + 语义去重：引入简易嵌入相似度（> `dedupThreshold` 判为重复）。
- 统一上限：
  - 在 Pipeline 末端应用 `maxQueries`，保留原始查询（`keepOriginal`），其余按质量分排序截断。
- 质量排序：
  - 结合来源（e/f/h/Rewrite/Expanding）、长度/结构分、关键词覆盖率等，形成简单打分，用于排序与截断。

---

## 7. 动态装配与回退

- `DynamicModelManager#createQueryTransformerPipeline`：
  - 按配置启用 e/f/h 对应阶段；确保与 Spring Bean 装配一致。
  - 任一阶段异常 → 记录 metrics → 按 `FallbackPolicy` 降级（跳过或回退原始查询）。

---

## 8. 指标与监控

- 阶段级：执行次数、成功/失败/跳过、耗时（均值/分位）、输出条数、扩展贡献。
- 质量：纠错过纠率、前缀命中率、近义词人工评估 TopK 命中。
- 业务：召回率、点击率、答案引用率、响应时延 P95/P99。
- 输出到现有 `DefaultMetricsCollector`，并预留日志聚合/看板。

---

## 9. 测试计划

- 单元测试：
  - Service：拼音纠错/前缀匹配/近义词检索的核心逻辑与边界。
  - Stage：上下文传递、异常与降级、去重与上限。
- 集成测试：
  - 端到端 Pipeline：不同配置组合、中文混输、全半角、繁简、带噪声输入。
- 性能基准：
  - 词典规模 10^5 量级下 e/f/h 的延迟与内存曲线；Pipeline P95 目标。
- 回归数据集：
  - 实际问题回放集 + 人工标注集（人名/书名/术语/品牌等）。

---

## 10. 灰度发布与回滚

- 默认关闭 e/f/h，新功能以租户/渠道粒度灰度开启。
- 可快速回滚到“关闭 e/f/h 且保留现有阶段”的稳定配置。
- 观测期指标准线：召回/点击/答案质量不下降，延迟/失败率无异常。

---

## 11. 验收标准（KPIs）

- e（拼音改写）：Top1 纠正命中 ≥ 80%，过纠率 ≤ 5%，P95 < 5ms。
- f（前缀补全）：热门实体前缀命中 ≥ 95%，短查询召回显著提升，P95 < 3ms。
- h（近义词召回）：人工评估 Top5 命中 ≥ 85%，整体 RAG 召回有正向提升（A/B）。
- 稳定性：阶段失败可降级不影响主流程；指标齐全可观测。

---

## 12. 风险与缓解

- 过纠/错纠风险（e）：设置阈值与白/黑名单；低置信候选仅作为扩展不替换主查询。
- 噪声扩展（f/h）：候选上限与质量阈值；质量分排序 + 语义去重。
- 内存与延迟：词典/索引分租户分片，懒加载与 TTL 刷新；阶段内检查超时即时降级。
- 动态装配不一致：统一装配清单与默认策略，增加集成测试。

---

## 13. 交付清单（代码与资产）

- 配置：`RagComponentConfig.QueryTransformerConfig` 扩展；`QueryContext.PipelineConfig` 增参。
- 新服务：`PhoneticCorrectionService`、`PrefixCompletionService`、`SynonymRecallService`。
- 新阶段：`PhoneticCorrectionStage`、`PrefixCompletionStage`、`SynonymRecallStage`。
- 索引与脚本：
  - 词典构建与拼音索引生成脚本（离线，读取 KB/实体导出）。
  - TermEmbeddingIndex 构建脚本（批量嵌入 + 持久化）。
- 动态装配：`DynamicModelManager` 更新，新增阶段接入与开关处理。
- 指标：阶段指标上报、全局汇总；日志样例与看板字段约定。
- 文档：本实施计划、配置与运行手册、词典/索引构建指南。

---

## 14. 时间预估（可并行）

- M1：1d
- M2：3–5d（含词典样本与调参）
- M3：2–3d
- M4：4–7d（取决于嵌入与索引规模）
- M5：1–2d
- M6：1d
- M7：3–5d
- M8：1–2d

---

## 15. 后续演进

- 语义去重使用向量库（HNSW/FAISS）；
- e 阶段引入语言模型辅助纠错（低频长尾场景）；
- f 阶段结合“下一跳”n-gram 与 session 热度排序；
- h 阶段引入多源 Embedding 与可学习加权；
- 将 b/c/e/f/h 信号下沉到检索路由器与 BM25/混检策略中，形成统一检索 DSL。

