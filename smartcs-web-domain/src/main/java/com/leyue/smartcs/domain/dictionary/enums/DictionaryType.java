package com.leyue.smartcs.domain.dictionary.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 字典类型枚举
 * 定义系统支持的所有字典类型及其特性
 * 
 * @author Claude
 */
@Getter
@RequiredArgsConstructor
public enum DictionaryType {
    
    /**
     * 标准化规则字典
     * 用于查询标准化阶段的文本清理和标准化处理
     * 数据格式：Map<String, String> - 原始文本->标准化文本的映射
     */
    NORMALIZATION_RULES("normalization_rules", "标准化规则字典", "MAP", "用于查询标准化阶段的文本清理和标准化处理"),
    
    /**
     * 拼音纠错字典  
     * 用于拼音改写阶段的错拼纠正处理
     * 数据格式：Map<String, String> - 错误拼音->正确文本的映射
     */
    PHONETIC_CORRECTIONS("phonetic_corrections", "拼音纠错字典", "MAP", "用于拼音改写阶段的错拼纠正处理"),
    
    /**
     * 前缀补全词典
     * 用于前缀补全阶段的查询词汇扩展
     * 数据格式：Set<String> - 前缀词汇集合
     */
    PREFIX_WORDS("prefix_words", "前缀补全词典", "SET", "用于前缀补全阶段的查询词汇扩展"),
    
    /**
     * 同义词召回字典
     * 用于同义词召回阶段的语义扩展处理
     * 数据格式：Map<String, Set<String>> - 词汇->同义词组的映射
     */
    SYNONYM_SETS("synonym_sets", "同义词召回字典", "MAP_SET", "用于同义词召回阶段的语义扩展处理"),
    
    /**
     * 停用词字典
     * 用于查询标准化阶段的停用词过滤
     * 数据格式：Set<String> - 停用词集合
     */
    STOP_WORDS("stop_words", "停用词字典", "SET", "用于查询标准化阶段的停用词过滤"),
    
    /**
     * 领域特定术语字典
     * 用于特定领域的专业术语处理
     * 数据格式：Map<String, String> - 术语->标准表达的映射
     */
    DOMAIN_TERMS("domain_terms", "领域术语字典", "MAP", "用于特定领域的专业术语处理"),
    
    /**
     * 缩写扩展字典
     * 用于缩写词的全称扩展
     * 数据格式：Map<String, String> - 缩写->全称的映射
     */
    ABBREVIATION_EXPANSIONS("abbreviation_expansions", "缩写扩展字典", "MAP", "用于缩写词的全称扩展"),
    
    /**
     * 实体识别字典
     * 用于命名实体识别和标准化
     * 数据格式：Map<String, Set<String>> - 实体类型->实体名称集合的映射
     */
    ENTITY_RECOGNITION("entity_recognition", "实体识别字典", "MAP_SET", "用于命名实体识别和标准化"),
    
    // ==================== 查询转换器专用字典类型 ====================
    
    /**
     * 标准化停用词字典
     * 用于查询标准化阶段的停用词过滤处理
     * 数据格式：Set<String> - 停用词集合
     */
    NORMALIZATION_STOPWORDS("normalization_stopwords", "标准化停用词字典", "SET", "用于查询标准化阶段的停用词过滤处理"),
    
    /**
     * 标准化拼写纠错字典  
     * 用于查询标准化阶段的拼写错误纠正
     * 数据格式：Map<String, String> - 错误词->正确词的映射
     */
    NORMALIZATION_SPELL_CORRECTIONS("normalization_spell_corrections", "标准化拼写纠错字典", "MAP", "用于查询标准化阶段的拼写错误纠正"),
    
    /**
     * 语义同义词字典
     * 用于语义对齐阶段的同义词处理
     * 数据格式：Map<String, Set<String>> - 词汇->同义词集合的映射
     */
    SEMANTIC_SYNONYMS("semantic_synonyms", "语义同义词字典", "MAP_SET", "用于语义对齐阶段的同义词处理"),
    
    /**
     * 语义单位映射字典
     * 用于语义对齐阶段的单位标准化处理  
     * 数据格式：Map<String, String> - 非标准单位->标准单位的映射
     */
    SEMANTIC_UNIT_MAPPINGS("semantic_unit_mappings", "语义单位映射字典", "MAP", "用于语义对齐阶段的单位标准化处理"),
    
    /**
     * 语义时间模式字典
     * 用于语义对齐阶段的时间表达式识别与标准化
     * 数据格式：List<PatternRule> - 时间模式规则列表
     */
    SEMANTIC_TIME_PATTERNS("semantic_time_patterns", "语义时间模式字典", "RULE_LIST", "用于语义对齐阶段的时间表达式识别与标准化"),
    
    /**
     * 语义实体别名字典
     * 用于语义对齐阶段的实体别名标准化处理
     * 数据格式：Map<String, String> - 实体别名->标准名称的映射
     */
    SEMANTIC_ENTITY_ALIASES("semantic_entity_aliases", "语义实体别名字典", "MAP", "用于语义对齐阶段的实体别名标准化处理"),
    
    /**
     * 意图目录字典
     * 用于意图识别阶段的意图分类目录
     * 数据格式：Map<String, IntentCatalog> - 意图ID->意图目录信息的映射
     */
    INTENT_CATALOG("intent_catalog", "意图目录字典", "INTENT_CATALOG", "用于意图识别阶段的意图分类目录"),
    
    /**
     * 意图实体模式字典
     * 用于意图识别阶段的实体模式匹配
     * 数据格式：List<PatternRule> - 实体模式规则列表
     */
    INTENT_ENTITY_PATTERNS("intent_entity_patterns", "意图实体模式字典", "RULE_LIST", "用于意图识别阶段的实体模式匹配"),
    
    /**
     * 意图查询类型模式字典
     * 用于意图识别阶段的查询类型判断
     * 数据格式：List<PatternRule> - 查询类型模式规则列表
     */
    INTENT_QUERY_TYPE_PATTERNS("intent_query_type_patterns", "意图查询类型模式字典", "RULE_LIST", "用于意图识别阶段的查询类型判断"),
    
    /**
     * 意图比较模式字典
     * 用于意图识别阶段的比较操作识别
     * 数据格式：List<PatternRule> - 比较操作模式规则列表
     */
    INTENT_COMPARISON_PATTERNS("intent_comparison_patterns", "意图比较模式字典", "RULE_LIST", "用于意图识别阶段的比较操作识别"),
    
    /**
     * 改写口语化模式字典
     * 用于查询改写阶段的口语化表达转换
     * 数据格式：List<PatternRule> - 口语化模式规则列表
     */
    REWRITE_COLLOQUIAL_PATTERNS("rewrite_colloquial_patterns", "改写口语化模式字典", "RULE_LIST", "用于查询改写阶段的口语化表达转换"),
    
    /**
     * 改写关键词权重模式字典
     * 用于查询改写阶段的关键词权重调整
     * 数据格式：List<PatternWeight> - 关键词权重规则列表
     */
    REWRITE_KEYWORD_WEIGHT_PATTERNS("rewrite_keyword_weight_patterns", "改写关键词权重模式字典", "WEIGHT_LIST", "用于查询改写阶段的关键词权重调整"),
    
    /**
     * 改写技术术语映射字典
     * 用于查询改写阶段的技术术语标准化
     * 数据格式：Map<String, String> - 非标准术语->标准术语的映射
     */
    REWRITE_TECH_TERM_MAPPINGS("rewrite_tech_term_mappings", "改写技术术语映射字典", "MAP", "用于查询改写阶段的技术术语标准化"),
    
    /**
     * 改写停用词字典
     * 用于查询改写阶段的停用词处理
     * 数据格式：Set<String> - 停用词集合
     */
    REWRITE_STOPWORDS("rewrite_stopwords", "改写停用词字典", "SET", "用于查询改写阶段的停用词处理"),
    
    // ==================== M2/M3 新增字典类型 ====================
    
    /**
     * 语义对齐规则字典
     * 用于语义对齐阶段的同义词归一化处理
     * 数据格式：Map<String, String> - 同义词->标准词的映射
     */
    SEMANTIC_ALIGNMENT("semantic_alignment", "语义对齐规则字典", "MAP", "用于语义对齐阶段的同义词归一化处理"),
    
    /**
     * 语义关键词字典
     * 用于语义对齐阶段的关键词标准化处理
     * 数据格式：Map<String, String> - 关键词->标准关键词的映射
     */
    SEMANTIC_KEYWORDS("semantic_keywords", "语义关键词字典", "MAP", "用于语义对齐阶段的关键词标准化处理"),
    
    /**
     * 语义分类字典
     * 用于语义对齐阶段的分类标准化处理
     * 数据格式：Map<String, String> - 分类->标准分类的映射
     */
    SEMANTIC_CATEGORIES("semantic_categories", "语义分类字典", "MAP", "用于语义对齐阶段的分类标准化处理"),
    
    /**
     * 意图模式字典
     * 用于意图提取阶段的意图识别模式匹配
     * 数据格式：Map<String, String> - 意图模式->意图标识的映射
     */
    INTENT_PATTERNS("intent_patterns", "意图模式字典", "MAP", "用于意图提取阶段的意图识别模式匹配"),
    
    /**
     * 意图关键词字典
     * 用于意图提取阶段的关键词基础意图识别
     * 数据格式：Map<String, String> - 关键词->意图标识的映射
     */
    INTENT_KEYWORDS("intent_keywords", "意图关键词字典", "MAP", "用于意图提取阶段的关键词基础意图识别"),
    
    /**
     * 改写规则字典
     * 用于改写阶段的查询重构规则
     * 数据格式：Map<String, String> - 原始表达->改写表达的映射
     */
    REWRITE_RULES("rewrite_rules", "改写规则字典", "MAP", "用于改写阶段的查询重构规则"),
    
    /**
     * 扩展策略字典
     * 用于扩展策略阶段的查询扩展规则
     * 数据格式：Map<String, String> - 查询模式->扩展策略的映射
     */
    EXPANSION_STRATEGIES("expansion_strategies", "扩展策略字典", "MAP", "用于扩展策略阶段的查询扩展规则"),
    
    // 保持现有的字典类型兼容性
    /**
     * 前缀源词字典 (兼容性别名)
     * 用于前缀补全阶段的源词汇管理
     * 数据格式：Set<String> - 源词汇集合  
     */
    PREFIX_SOURCE_WORDS("prefix_source_words", "前缀源词字典", "SET", "用于前缀补全阶段的源词汇管理");
    
    /**
     * 字典类型代码
     */
    private final String code;
    
    /**
     * 字典类型名称
     */
    private final String name;
    
    /**
     * 数据结构类型
     * MAP: Map<String, String>
     * SET: Set<String> 
     * MAP_SET: Map<String, Set<String>>
     * RULE_LIST: List<PatternRule>
     * WEIGHT_LIST: List<PatternWeight>
     * INTENT_CATALOG: Map<String, IntentCatalog>
     */
    private final String dataStructure;
    
    /**
     * 描述说明
     */
    private final String description;
    
    /**
     * 根据代码获取字典类型
     * 
     * @param code 字典类型代码
     * @return 字典类型枚举
     */
    public static DictionaryType fromCode(String code) {
        for (DictionaryType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的字典类型代码: " + code);
    }
    
    /**
     * 检查是否为映射类型数据结构
     * 
     * @return 是否为映射类型
     */
    public boolean isMapType() {
        return "MAP".equals(dataStructure) || "MAP_SET".equals(dataStructure) || "INTENT_CATALOG".equals(dataStructure);
    }
    
    /**
     * 检查是否为集合类型数据结构
     * 
     * @return 是否为集合类型
     */
    public boolean isSetType() {
        return "SET".equals(dataStructure) || "MAP_SET".equals(dataStructure);
    }
    
    /**
     * 检查是否为复合映射类型数据结构（值为Set的Map）
     * 
     * @return 是否为复合映射类型
     */
    public boolean isMapSetType() {
        return "MAP_SET".equals(dataStructure);
    }
    
    /**
     * 检查是否为规则列表类型数据结构
     * 
     * @return 是否为规则列表类型
     */
    public boolean isRuleListType() {
        return "RULE_LIST".equals(dataStructure);
    }
    
    /**
     * 检查是否为权重列表类型数据结构
     * 
     * @return 是否为权重列表类型
     */
    public boolean isWeightListType() {
        return "WEIGHT_LIST".equals(dataStructure);
    }
    
    /**
     * 检查是否为列表类型数据结构
     * 
     * @return 是否为列表类型
     */
    public boolean isListType() {
        return "RULE_LIST".equals(dataStructure) || "WEIGHT_LIST".equals(dataStructure);
    }
    
    /**
     * 检查是否为意图目录类型数据结构
     * 
     * @return 是否为意图目录类型
     */
    public boolean isIntentCatalogType() {
        return "INTENT_CATALOG".equals(dataStructure);
    }
}