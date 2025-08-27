# QueryTransformer 字典模块设计计划

## 1. 现状分析与目标
- 现状：各阶段（Normalization/SemanticAlignment/IntentExtraction/Rewrite/Phonetic/Prefix/Synonym/ExpansionStrategy）普遍内嵌静态映射或正则，难以按租户/渠道/领域动态配置、灰度与热更新。
- 目标：提供统一、分层、可配置、可热更新的“字典模块”，以数据驱动各阶段的词表、别名、规则与提示，并具备版本化与观测能力。

## 2. 管线与阶段简述（字典依赖点）
- NormalizationStage：停用词、拼写纠错映射、全角/半角映射、繁简映射（或外部库策略开关）。
- SemanticAlignmentStage：领域同义词/别名（含单位别名）、单位标准化映射、时间表达式正则与处理策略、实体标准化映射（按租户/领域）。
- IntentExtractionStage：
  - 意图清单/目录（供 LLM 分类提示），
  - NER 正则模版，
  - 查询类型识别正则，
  - 时间/数值/比较词模式。
- RewriteStage：口语化改写规则（pattern->replacement）、关键词权重模式（pattern->weight）、停用词、技术术语映射。
- PhoneticCorrectionStage：错拼/形近字纠错词典（string->string）及阈值。
- PrefixCompletionStage：前缀补全词典（Trie 构建源词表）。
- SynonymRecallStage：同义词召回词典（key<->aliases）。
- ExpansionStrategyStage：意图关键词映射、主题抽象提示/规则（可选，先保留简单映射）。

## 3. 字典分类与数据模型
为覆盖不同阶段需求，定义以下字典类型（DictionaryType）：
- NORMALIZATION_STOPWORDS：停用词（按 locale/租户/渠道 可覆盖）。
- NORMALIZATION_SPELL_CORRECTIONS：拼写纠错对（error->correction）。
- NORMALIZATION_WIDTH_MAP：全角/半角映射（如需数据驱动）。
- SEMANTIC_SYNONYMS：领域同义词/别名映射（alias->canonical）。
- SEMANTIC_UNIT_MAPPINGS：单位标准化映射（unit->stdUnit），可含换算系数。
- SEMANTIC_TIME_PATTERNS：时间表达式（regex + handlerId）。
- SEMANTIC_ENTITY_ALIASES：实体别名映射（alias->entityId/canonical）。
- INTENT_CATALOG：意图目录清单（多级：catalog/intents，含展示名与编码）。
- INTENT_ENTITY_PATTERNS：NER 模式（name->regex）。
- INTENT_QUERY_TYPE_PATTERNS：（type->regex）。
- INTENT_COMPARISON_PATTERNS：（token->operator）。
- REWRITE_COLLOQUIAL_PATTERNS：（regex->replacement）。
- REWRITE_KEYWORD_WEIGHT_PATTERNS：（regex->weight）。
- REWRITE_TECH_TERM_MAPPINGS：（term->stdTerm）。
- PHONETIC_CORRECTIONS：（from->to）。
- PREFIX_SOURCE_WORDS：前缀词表（行式）。
- SYNONYM_SETS：同义词集合（canonical->[aliases]）。
- EXPANSION_INTENT_KEYWORDS：（intentCode->keywords）。

数据表建议（也支持 YAML/JSON 文件存储，见 6.2）：
- dictionary_entry
  - id, type(枚举), scope_tenant, scope_channel, locale, domain, key, value_json, version, enabled, effective_from, effective_to, updated_by, updated_at, checksum
  - value_json：统一 JSON 存储值；对集合/映射/正则携带结构化字段。

范围覆盖与优先级（Scope）：
- Global < Tenant < Channel < Domain/Model（如需）
- 查找顺序：Channel(tenant+channel) -> Tenant -> Global

## 4. 访问 API 与内存结构
核心接口：DictionaryService（只读）/ DictionaryAdminService（管理）
- DictionaryService：
  - getStopWords(locale, tenant, channel)
  - getSpellCorrections(tenant, channel)
  - getSynonyms(tenant, channel)
  - getUnitMappings(tenant)
  - getTimePatterns(tenant) -> List<CompiledPattern>
  - getEntityAliases(tenant)
  - getIntentCatalog(tenant, channel)
  - getEntityPatterns(tenant) -> Map<String, Pattern>
  - getQueryTypePatterns(tenant) -> Map<String, Pattern>
  - getComparisonPatterns(tenant)
  - getColloquialRules(tenant) -> List<PatternRule>
  - getKeywordWeightRules(tenant) -> List<PatternWeight>
  - getTechTermMappings(tenant)
  - getPhoneticCorrections(tenant)
  - getPrefixWords(tenant) -> List<String>（或构建 Trie）
  - getSynonymSets(tenant)
  - getExpansionIntentKeywords(tenant)

内存结构与编译：
- Regex/Pattern 类型在装载时编译为 Pattern；
- 时间/实体/改写规则统一抽象为 PatternRule {pattern, replacement/handler, weight, flags}；
- Prefix 构建 Trie 后缓存；
- 同义词集合构建正反向 Map 提升匹配效率；
- LRU/版本戳缓存，Key = (type, tenant, channel, locale, domain, version)。

## 5. 与各阶段的集成改造
- NormalizationStage
  - 注入 DictionaryService，替换内置 STOP_WORDS、SPELLING_CORRECTIONS、FULLWIDTH 映射；
  - 支持按 locale/tenant 读取；保留 OpenCC 开关位于配置。
- SemanticAlignmentStage
  - 用 SEMANTIC_SYNONYMS、SEMANTIC_UNIT_MAPPINGS、SEMANTIC_TIME_PATTERNS（含 handlerId -> 内置处理器映射）
  - 实体标准化使用 SEMANTIC_ENTITY_ALIASES（租户/领域级）。
- IntentExtractionStage
  - 意图清单 INTENT_CATALOG 生成提示；
  - ENTITY_PATTERNS/QUERY_TYPE/TIME/COMPARISON 由字典提供，落地为 Pattern。
- RewriteStage
  - COLLOQUIAL、KEYWORD_WEIGHT、STOPWORDS、TECH_TERM_MAPPINGS 字典化；
  - 权重合并策略不变，来源由字典驱动。
- PhoneticCorrectionStage
  - PHONETIC_CORRECTIONS 替换内置表，阈值仍走配置。
- PrefixCompletionStage
  - PREFIX_SOURCE_WORDS 加载后构建 Trie（支持按租户/渠道差异化词表）。
- SynonymRecallStage
  - SYNONYM_SETS 提供 canonical 与 aliases；同时构建反查。
- ExpansionStrategyStage
  - EXPANSION_INTENT_KEYWORDS 注入 mapIntentToKeywords。

## 6. 存储形态与热更新
6.1 首选：数据库表 dictionary_entry
- 优点：统一审计、版本管理、灰度与多租户过滤；
- 配合 MyBatis/JPA 读写；

6.2 备选：YAML/JSON 配置文件（按环境/租户拆分）
- 目录约定：`/config/dictionaries/{tenant}/{type}.yaml`
- 启动加载 + 文件监听（Spring Cloud Config 或简单 WatchService）
- 适合快速试错与离线导入。

热更新机制：
- 变更后通过 Admin API 触发：invalidate(type, tenant, channel)；
- 或使用版本戳轮询（短 TTL）+ 事件通知（如 Redis Pub/Sub）。

## 7. API 草图（Java）
```java
public interface DictionaryService {
  Set<String> getStopWords(String locale, String tenant, String channel);
  Map<String, String> getSpellCorrections(String tenant, String channel);
  Map<String, String> getUnitMappings(String tenant);
  List<PatternRule> getSemanticSynonyms(String tenant, String channel); // 或 Map<String,String>
  List<CompiledPattern> getTimePatterns(String tenant);
  Map<String, String> getEntityAliases(String tenant);
  IntentCatalog getIntentCatalog(String tenant, String channel);
  Map<String, Pattern> getEntityPatterns(String tenant);
  Map<String, Pattern> getQueryTypePatterns(String tenant);
  Map<String, IntentOperator> getComparisonPatterns(String tenant);
  List<PatternRule> getColloquialRules(String tenant);
  List<PatternWeight> getKeywordWeightRules(String tenant);
  Map<String, String> getTechTermMappings(String tenant);
  Map<String, String> getPhoneticCorrections(String tenant);
  List<String> getPrefixWords(String tenant);
  Map<String, List<String>> getSynonymSets(String tenant);
  Map<String, String> getExpansionIntentKeywords(String tenant);
}
```

内置值提供者：FallbackDictionaryProvider（封装当前各 Stage 的常量），用于缺省回退。

## 8. 交付拆分与里程碑
- M1（基础能力，可读可配）
  - 落地 dictionary_entry 表/或 YAML 读取器；
  - 实现 DictionaryService + 缓存；
  - NormalizationStage/Phonetic/Prefix 接入字典（风险低、收益快）。
- M2（语义与意图）
  - SemanticAlignmentStage 接入同义词/单位/时间；
  - IntentExtractionStage 接入意图清单与正则集合；
  - 提供 Admin 校验与预览。
- M3（改写与召回）
  - RewriteStage/SynonymRecallStage 接入；
  - ExpansionStrategy 意图关键词映射；
  - 观测指标与灰度开关。

## 10. 风险与回退
- 正则规则质量：强制预检并提供沙盒测试；
- 规则冲突导致异常：分层优先级+单元测试模板；
- 热更新抖动：加入版本戳与逐步生效窗口；
- 性能：预编译 Pattern、Trie 构建与 LRU 缓存，提供最大内存与淘汰策略。

## 11. 集成清单（代码改动点）
- 新增：`com.leyue.smartcs.rag.dictionary` 包：
  - DictionaryService, DictionaryAdminService, DictionaryRepository
  - DTO：PatternRule, PatternWeight, CompiledPattern, IntentCatalog
  - FallbackDictionaryProvider（搬运现有常量）
- 改造：各 Stage 构造器注入 DictionaryService；保留兼容构造函数（默认使用 Fallback）。
- Spring 配置：`DictionaryConfiguration` 注入并在 `QueryTransformerConfiguration` 中装配传入。

—— 以上方案在不改变默认行为的前提下，实现数据驱动、可灰度、易观测、可演进的字典支撑模块。

