# 计划A：RAG 查询转换器（QueryTransformer）增强方案

> 目标：在保持现有稳定性的前提下，系统性增强查询转换能力，提升召回率、相关性与稳健性，并做到可配置、可观测、可灰度、可回退。

## 一、现状与问题
- 已有能力：
  - 意图感知扩展：`IntentAwareQueryTransformer`（按意图选择扩展策略，失败回退基础扩展）。
  - 标准扩展：关闭意图识别时回退 `ExpandingQueryTransformer (n 路扩展)`。
- 主要不足：
  - 缺少系统化的“多阶段处理管线”（清洗/标准化/扩展/去重/排序/约束补全）。
  - 缺少多语言与术语标准化、HyDE、子问题分解、关键词/布尔式改写等策略的可插拔实现。
  - 可观测性与成本控制（扩展数、tokens、超时、缓存）不足。
  - PromptTemplate 自定义、A/B 与灰度发布能力欠缺。

## 二、总体设计
- 管线化：引入 `QueryTransformerPipeline`，由若干“阶段化处理器 (Stage)”串联构成；每个 Stage 可开关、可参数化、可观测。
- 策略化：扩展策略以策略对象实现，并由“策略选择器”基于意图、置信度、对话上下文与配置做动态选择。
- 配置化：扩展 `RagComponentConfig.QueryTransformerConfig`，支持模块开关、阈值、Prompt 模板、模型路由、预算/超时与缓存策略。
- 可观测：日志分级、指标埋点、采样记录（输入/输出长度、耗时、tokens、命中率等）。
- 可回退：任一阶段失败自动降级（跳过或回退到基础扩展），确保稳定性。

## 三、功能路线图（三阶段）

### Phase 1（MVP，可两周内交付）
- 清洗与标准化
  - 去噪：去多余空白、编号/标点清理、大小写标准化、停用词最小化（可选）。
  - 归一：时间/单位/符号归一化，限制查询长度。
- 标准 LLM 扩展
  - `Expanding`：支持自定义 `PromptTemplate`，n 路扩展，保证包含原始查询。
  - 去重与剪裁：语义/编辑距离去重，长度/数量/tokens 裁剪。
- 意图感知升级
  - 将现有 `IntentAwareQueryTransformer` 适配为 Pipeline 策略选择器；问候/闲聊绕过扩展，技术/问题类选择更强扩展数。
- 可观测与成本控制
  - 指标：扩展数、平均长度、耗时、失败率、降级率、token 消耗。日志采样与trace id。
- 配置扩展（基础）
  - `n、maxTokens、maxLatencyMs、enableIntent、enableExpanding、dedupThreshold` 等。

### Phase 2（检索成效增强）
- 多语言与术语
  - 自动语言检测与翻译到索引语言；领域术语词典映射、同义词注入。
- HyDE（Hypothetical Document Expansion）
  - 先生成“假设文档/答案”再反推多个查询变体，适合语义模糊场景。
- 子问题分解（Decomposition）
  - 将复合问题拆为子查询，支持“并发检索→聚合”。
- 关键词与布尔改写
  - 关键短语提取、must/should/filters 约束生成，输出结构化检索提示（metadata 过滤建议）。
- 约束补全与上下文注入
  - 时间/地域/版本/渠道等约束自动补全；结合会话历史与系统配置注入缺省上下文。

### Phase 3（工程化与灰度）
- A/B & 灰度
  - 支持按用户/渠道/租户/会话进行策略灰度，效果对比评估。
- 缓存与并发优化
  - 扩展结果缓存（key=标准化查询+策略+模型版本），TTL 控制；并发与超时治理。
- 评测与对照组
  - 构建检索集（带标注相关性），对比 Recall@K、MRR、nDCG；离线评测管线。
- 运维工具
  - 可视化看板：扩展分布、失败/降级、成本、效果趋势；一键回退。

## 四、接口与类设计（示意）
- 接口
  - `interface QueryTransformerStage { Collection<Query> apply(QueryContext ctx, Collection<Query> input); }`
  - `class QueryTransformerPipeline implements QueryTransformer { List<QueryTransformerStage> stages; }`
  - `class StrategySelector { Strategy select(Intent, Confidence, Context, Config); }`
  - `class QueryContext { originalQuery, locale, tenant, channel, chatHistory, budget, timeouts, metricsCollector, ... }`
- 关键 Stage（初版内置）
  - `NormalizationStage`：清洗/归一。
  - `ExpandingStage`：LLM 扩展（支持 PromptTemplate、n、温度等）。
  - `HydeStage`：生成假设文档→反推查询。
  - `DecompositionStage`：子问题分解。
  - `KeywordRewriteStage`：关键词/布尔式改写与结构化过滤建议。
  - `ConstraintCompletionStage`：时间/地域/版本等缺省约束补全。
  - `MultilingualStage`：语言检测、翻译与术语标准化。
  - `SafetyStage`：敏感词与 PII 处理。
  - `DedupRankStage`：去重、重排与剪裁（保留原始查询兜底）。

## 五、配置方案（RagComponentConfig.QueryTransformerConfig 扩展）
- 通用参数
  - `enabled`、`n`、`maxTokens`、`maxLatencyMs`、`maxQueries`、`keepOriginal`、`dedupThreshold`、`maxCost`。
- 开关控制
  - `enableIntentRecognition`、`enableExpanding`、`enableHyde`、`enableDecomposition`、`enableMultilingual`、`enableKeywordRewrite`、`enableConstraintCompletion`、`enableSafetyFilter`。
- Prompt 与模型路由
  - `expandingPromptTemplate`、`hydePromptTemplate`、`decompositionPromptTemplate`、`keywordPromptTemplate`；每阶段可指定 `modelId`。
- 多语言与术语
  - `defaultIndexLanguage`、`supportedLanguages`、`domainLexiconId`。
- 缓存与回退
  - `enableCache`、`cacheTtlSeconds`、`fallbackPolicy`（跳过/回退到 Expanding/仅原始查询）。

## 六、可观测与评测
- 运行指标
  - `queries_generated_total`、`avg_query_length`、`latency_ms`、`expand_fail_ratio`、`fallback_ratio`、`tokens_input/output`、`cost_usd`。
- 日志与采样
  - 重要阶段 debug 日志；按采样率记录输入与输出；trace id 贯穿。
- 效果评测（离线）
  - 标注数据集，评估 `Recall@K / MRR / nDCG`，并与基线（仅原始/仅Expanding/意图感知）对照。

## 七、实施计划与里程碑
- 里程碑
  - M1（第1周）：Pipeline 骨架、Normalization/Expanding/DedupRank、配置与基础指标。
  - M2（第2周）：Intent 策略化、Multilingual、KeywordRewrite、ConstraintCompletion、A/B 骨架。
  - M3（第3-4周）：HyDE、Decomposition、缓存与灰度、离线评测与看板。
- 验收标准
  - 线上无故障回退；扩展失败可降级；指标完整；对照实验在目标场景 Recall@10 提升 ≥ X%（与业务共同设定）。

## 八、变更点清单（影响面）
- 代码
  - `smartcs-web-infrastructure/.../DynamicModelManager#createQueryTransformer`：接入 Pipeline 与新配置。
  - 新增 `pipeline` 与各 `Stage` 类，保持对 `QueryTransformer` 接口兼容。
  - `IntentAwareQueryTransformer` 部分能力迁移为策略/Stage（保留向后兼容构造）。
- 配置与文档
  - 扩展 `RagComponentConfig.QueryTransformerConfig` 字段；新增示例与参数校验。
  - 运维/监控文档与使用指南。

## 九、风险与回避
- LLM 不稳定与成本波动 → 超时/预算/熔断/重试/缓存与降级策略。
- 提升召回但引入噪声 → ReRank 阶段与 minScore 联动；扩展数与质量阈值调参。
- 多语言误翻/术语偏差 → 领域词典与规则兜底；高置信度再扩展。
- 引入新配置导致误配 → 参数校验、默认合理值、失败回退到旧路径。

## 十、后续工作（可选）
- 端到端“查询→检索→聚合→回答”联动调参器（面向运营）。
- 在线学习（基于点击/反馈信号的扩展策略自适应）。
- 与 ReRanking 的协同优化（把扩展策略信息暴露给打分模型）。

---

附录A：示例 Prompt（节选）

- Expanding 示例
```
你是检索查询扩展助手。请为“{query}”生成 {n} 个紧密相关的查询变体：
- 专注同义与常见写法；
- 保持原意，不添加新实体；
- 每行一个变体，不要编号；
- 控制在 12 个中文词以内。
```

- HyDE 示例（先答后问）
```
基于用户问题生成一段假设性、简洁的答案/摘要（不需要真实准确）：
{query}
然后将上述内容提炼为 {n} 个用于检索的查询（每行一个），涵盖关键实体、同义词与不同问法。
```

- 子问题分解示例
```
将复合问题拆分为 2-5 个可独立检索的子问题，每行一个，覆盖全部关键维度：
{query}
```
