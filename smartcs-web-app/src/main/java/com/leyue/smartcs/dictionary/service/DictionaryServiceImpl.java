package com.leyue.smartcs.dictionary.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leyue.smartcs.api.DictionaryService;
import com.leyue.smartcs.domain.dictionary.entity.DictionaryEntry;
import com.leyue.smartcs.domain.dictionary.enums.DictionaryType;
import com.leyue.smartcs.domain.dictionary.gateway.DictionaryGateway;
import com.leyue.smartcs.domain.dictionary.valueobject.DictionaryConfig;
import com.leyue.smartcs.domain.dictionary.valueobject.IntentCatalog;
import com.leyue.smartcs.domain.dictionary.valueobject.PatternRule;
import com.leyue.smartcs.domain.dictionary.valueobject.PatternWeight;
import com.leyue.smartcs.dto.dictionary.DictionaryDataDTO;
import com.leyue.smartcs.dto.dictionary.IntentCatalogDTO;
import com.leyue.smartcs.dto.dictionary.PatternRuleDTO;
import com.leyue.smartcs.dto.dictionary.PatternWeightDTO;
import com.leyue.smartcs.dto.intent.IntentDictionaryDTO;
import com.leyue.smartcs.dto.intent.SlotTemplateDTO;
import com.leyue.smartcs.domain.intent.gateway.SlotTemplateGateway;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 字典服务实现类
 * 面向业务运行时的字典查询服务实现
 * 
 * @author Claude
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DictionaryServiceImpl implements DictionaryService {
    
    private final DictionaryGateway dictionaryGateway;
    private final ObjectMapper objectMapper;
    private final SlotTemplateGateway slotTemplateGateway;
    
    /**
     * 本地缓存，用于存储缓存统计信息
     */
    private final Map<String, Object> cacheStatsMap = new ConcurrentHashMap<>();
    
    /**
     * 编译后的Pattern缓存，用于提高正则表达式性能
     */
    private final Map<String, Pattern> compiledPatternCache = new ConcurrentHashMap<>();
    
    /**
     * Trie树缓存，用于前缀匹配
     */
    private final Map<String, TrieNode> trieCache = new ConcurrentHashMap<>();
    
    @Override
    @Cacheable(value = "dictionary:normalization", key = "#tenant + ':' + #channel + ':' + #domain")
    public Map<String, String> getNormalizationRules(String tenant, String channel, String domain) {
        log.debug("获取标准化规则字典: tenant={}, channel={}, domain={}", tenant, channel, domain);
        
        DictionaryConfig config = DictionaryConfig.of(tenant, channel, domain);
        List<DictionaryEntry> entries = dictionaryGateway.findActiveEntries(DictionaryType.NORMALIZATION_RULES, config);
        
        Map<String, String> rules = new HashMap<>();
        for (DictionaryEntry entry : entries) {
            try {
                // 解析JSON格式的条目值
                String value = parseStringValue(entry.getEntryValue());
                rules.put(entry.getEntryKey(), value);
            } catch (Exception e) {
                log.warn("解析标准化规则失败: key={}, value={}", entry.getEntryKey(), entry.getEntryValue(), e);
            }
        }
        
        updateCacheStats("normalization_rules", tenant, channel, domain, rules.size());
        return rules;
    }
    
    @Override
    @Cacheable(value = "dictionary:phonetic", key = "#tenant + ':' + #channel + ':' + #domain")
    public Map<String, String> getPhoneticCorrections(String tenant, String channel, String domain) {
        log.debug("获取拼音纠错字典: tenant={}, channel={}, domain={}", tenant, channel, domain);
        
        DictionaryConfig config = DictionaryConfig.of(tenant, channel, domain);
        List<DictionaryEntry> entries = dictionaryGateway.findActiveEntries(DictionaryType.PHONETIC_CORRECTIONS, config);
        
        Map<String, String> corrections = new HashMap<>();
        for (DictionaryEntry entry : entries) {
            try {
                String value = parseStringValue(entry.getEntryValue());
                corrections.put(entry.getEntryKey(), value);
            } catch (Exception e) {
                log.warn("解析拼音纠错数据失败: key={}, value={}", entry.getEntryKey(), entry.getEntryValue(), e);
            }
        }
        
        updateCacheStats("phonetic_corrections", tenant, channel, domain, corrections.size());
        return corrections;
    }
    
    @Override
    @Cacheable(value = "dictionary:prefix", key = "#tenant + ':' + #channel + ':' + #domain")
    public Set<String> getPrefixWords(String tenant, String channel, String domain) {
        log.debug("获取前缀补全词典: tenant={}, channel={}, domain={}", tenant, channel, domain);
        
        DictionaryConfig config = DictionaryConfig.of(tenant, channel, domain);
        List<DictionaryEntry> entries = dictionaryGateway.findActiveEntries(DictionaryType.PREFIX_WORDS, config);
        
        Set<String> prefixWords = new HashSet<>();
        for (DictionaryEntry entry : entries) {
            try {
                // 支持两种格式：单个词或JSON数组
                if (entry.getEntryValue().startsWith("[")) {
                    List<String> words = parseStringListValue(entry.getEntryValue());
                    prefixWords.addAll(words);
                } else {
                    String word = parseStringValue(entry.getEntryValue());
                    prefixWords.add(word);
                }
            } catch (Exception e) {
                log.warn("解析前缀词汇数据失败: key={}, value={}", entry.getEntryKey(), entry.getEntryValue(), e);
            }
        }
        
        updateCacheStats("prefix_words", tenant, channel, domain, prefixWords.size());
        return prefixWords;
    }
    
    @Override
    @Cacheable(value = "dictionary:synonym", key = "#tenant + ':' + #channel + ':' + #domain")
    public Map<String, Set<String>> getSynonymSets(String tenant, String channel, String domain) {
        log.debug("获取同义词召回字典: tenant={}, channel={}, domain={}", tenant, channel, domain);
        
        DictionaryConfig config = DictionaryConfig.of(tenant, channel, domain);
        List<DictionaryEntry> entries = dictionaryGateway.findActiveEntries(DictionaryType.SYNONYM_SETS, config);
        
        Map<String, Set<String>> synonymSets = new HashMap<>();
        for (DictionaryEntry entry : entries) {
            try {
                List<String> synonyms = parseStringListValue(entry.getEntryValue());
                synonymSets.put(entry.getEntryKey(), new HashSet<>(synonyms));
            } catch (Exception e) {
                log.warn("解析同义词数据失败: key={}, value={}", entry.getEntryKey(), entry.getEntryValue(), e);
            }
        }
        
        updateCacheStats("synonym_sets", tenant, channel, domain, synonymSets.size());
        return synonymSets;
    }
    
    @Override
    @Cacheable(value = "dictionary:stopwords", key = "#tenant + ':' + #channel + ':' + #domain")
    public Set<String> getStopWords(String tenant, String channel, String domain) {
        log.debug("获取停用词字典: tenant={}, channel={}, domain={}", tenant, channel, domain);
        
        DictionaryConfig config = DictionaryConfig.of(tenant, channel, domain);
        List<DictionaryEntry> entries = dictionaryGateway.findActiveEntries(DictionaryType.STOP_WORDS, config);
        
        Set<String> stopWords = new HashSet<>();
        for (DictionaryEntry entry : entries) {
            try {
                if (entry.getEntryValue().startsWith("[")) {
                    List<String> words = parseStringListValue(entry.getEntryValue());
                    stopWords.addAll(words);
                } else {
                    String word = parseStringValue(entry.getEntryValue());
                    stopWords.add(word);
                }
            } catch (Exception e) {
                log.warn("解析停用词数据失败: key={}, value={}", entry.getEntryKey(), entry.getEntryValue(), e);
            }
        }
        
        updateCacheStats("stop_words", tenant, channel, domain, stopWords.size());
        return stopWords;
    }
    
    @Override
    public DictionaryDataDTO getDictionaryData(String dictionaryType, String tenant, String channel, String domain) {
        log.debug("获取字典数据: type={}, tenant={}, channel={}, domain={}", dictionaryType, tenant, channel, domain);
        
        try {
            DictionaryType type = DictionaryType.fromCode(dictionaryType);
            DictionaryConfig config = DictionaryConfig.of(tenant, channel, domain);
            
            List<DictionaryEntry> entries = dictionaryGateway.findActiveEntries(type, config);
            Long version = dictionaryGateway.getLatestVersionTimestamp(type, config);
            
            return buildDictionaryDataDTO(type, config, entries, version);
        } catch (Exception e) {
            log.error("获取字典数据失败: type={}, config={}:{}:{}", dictionaryType, tenant, channel, domain, e);
            return null;
        }
    }
    
    @Override
    public Map<String, DictionaryDataDTO> getBatchDictionaryData(List<String> dictionaryTypes, 
                                                               String tenant, String channel, String domain) {
        log.debug("批量获取字典数据: types={}, tenant={}, channel={}, domain={}", dictionaryTypes, tenant, channel, domain);
        
        Map<String, DictionaryDataDTO> result = new HashMap<>();
        if (dictionaryTypes == null || dictionaryTypes.isEmpty()) {
            return result;
        }
        
        for (String dictionaryType : dictionaryTypes) {
            try {
                DictionaryDataDTO data = getDictionaryData(dictionaryType, tenant, channel, domain);
                if (data != null) {
                    result.put(dictionaryType, data);
                }
            } catch (Exception e) {
                log.warn("获取字典数据失败，跳过: type={}, config={}:{}:{}", dictionaryType, tenant, channel, domain, e);
            }
        }
        
        return result;
    }
    
    @Override
    public Long getDictionaryVersion(String dictionaryType, String tenant, String channel, String domain) {
        try {
            DictionaryType type = DictionaryType.fromCode(dictionaryType);
            DictionaryConfig config = DictionaryConfig.of(tenant, channel, domain);
            
            return dictionaryGateway.getLatestVersionTimestamp(type, config);
        } catch (Exception e) {
            log.error("获取字典版本失败: type={}, config={}:{}:{}", dictionaryType, tenant, channel, domain, e);
            return null;
        }
    }
    
    @Override
    public boolean hasDictionaryData(String dictionaryType, String tenant, String channel, String domain) {
        try {
            DictionaryType type = DictionaryType.fromCode(dictionaryType);
            DictionaryConfig config = DictionaryConfig.of(tenant, channel, domain);
            
            List<DictionaryEntry> entries = dictionaryGateway.findActiveEntries(type, config);
            return !entries.isEmpty();
        } catch (Exception e) {
            log.error("检查字典数据存在性失败: type={}, config={}:{}:{}", dictionaryType, tenant, channel, domain, e);
            return false;
        }
    }
    
    @Override
    public void refreshCache(String dictionaryType, String tenant, String channel, String domain) {
        log.info("刷新字典缓存: type={}, tenant={}, channel={}, domain={}", dictionaryType, tenant, channel, domain);
        // 这里应该调用缓存管理器的清除方法
        // 由于使用了@Cacheable，可以通过Spring的CacheManager来清除
        // 暂时记录日志，具体实现需要根据缓存配置来做
    }
    
    @Override
    public Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new HashMap<>(cacheStatsMap);
        stats.put("lastUpdated", System.currentTimeMillis());
        // 添加编译缓存统计
        stats.put("compiledPatternCacheSize", compiledPatternCache.size());
        stats.put("trieCacheSize", trieCache.size());
        return stats;
    }
    
    // ==================== 查询转换器专用字典方法实现 ====================
    
    @Override
    @Cacheable(value = "dictionary:normalization_stopwords", key = "#tenant + ':' + #channel + ':' + #domain")
    public Set<String> getNormalizationStopwords(String tenant, String channel, String domain) {
        log.debug("获取标准化停用词字典: tenant={}, channel={}, domain={}", tenant, channel, domain);
        return getSetData(DictionaryType.NORMALIZATION_STOPWORDS, tenant, channel, domain);
    }
    
    @Override
    @Cacheable(value = "dictionary:normalization_spelling", key = "#tenant + ':' + #channel + ':' + #domain")
    public Map<String, String> getNormalizationSpellingCorrections(String tenant, String channel, String domain) {
        log.debug("获取标准化拼写纠错字典: tenant={}, channel={}, domain={}", tenant, channel, domain);
        return getMapData(DictionaryType.NORMALIZATION_SPELL_CORRECTIONS, tenant, channel, domain);
    }
    
    @Override
    @Cacheable(value = "dictionary:semantic_synonyms", key = "#tenant + ':' + #channel + ':' + #domain")
    public Map<String, Set<String>> getSemanticSynonyms(String tenant, String channel, String domain) {
        log.debug("获取语义同义词字典: tenant={}, channel={}, domain={}", tenant, channel, domain);
        return getMapSetData(DictionaryType.SEMANTIC_SYNONYMS, tenant, channel, domain);
    }
    
    @Override
    @Cacheable(value = "dictionary:semantic_units", key = "#tenant + ':' + #channel + ':' + #domain")
    public Map<String, String> getSemanticUnitMappings(String tenant, String channel, String domain) {
        log.debug("获取语义单位映射字典: tenant={}, channel={}, domain={}", tenant, channel, domain);
        return getMapData(DictionaryType.SEMANTIC_UNIT_MAPPINGS, tenant, channel, domain);
    }
    
    @Override
    @Cacheable(value = "dictionary:semantic_time_patterns", key = "#tenant + ':' + #channel + ':' + #domain")
    public List<PatternRuleDTO> getSemanticTimePatterns(String tenant, String channel, String domain) {
        log.debug("获取语义时间模式字典: tenant={}, channel={}, domain={}", tenant, channel, domain);
        return getPatternRuleList(DictionaryType.SEMANTIC_TIME_PATTERNS, tenant, channel, domain);
    }
    
    @Override
    @Cacheable(value = "dictionary:semantic_entity_aliases", key = "#tenant + ':' + #channel + ':' + #domain")
    public Map<String, String> getSemanticEntityAliases(String tenant, String channel, String domain) {
        log.debug("获取语义实体别名字典: tenant={}, channel={}, domain={}", tenant, channel, domain);
        return getMapData(DictionaryType.SEMANTIC_ENTITY_ALIASES, tenant, channel, domain);
    }
    
    @Override
    @Cacheable(value = "dictionary:intent_catalog", key = "#tenant + ':' + #channel + ':' + #domain")
    public Map<String, IntentCatalogDTO> getIntentCatalog(String tenant, String channel, String domain) {
        log.debug("获取意图目录字典: tenant={}, channel={}, domain={}", tenant, channel, domain);
        return getIntentCatalogMap(DictionaryType.INTENT_CATALOG, tenant, channel, domain);
    }
    
    @Override
    @Cacheable(value = "dictionary:intent_entity_patterns", key = "#tenant + ':' + #channel + ':' + #domain")
    public List<PatternRuleDTO> getIntentEntityPatterns(String tenant, String channel, String domain) {
        log.debug("获取意图实体模式字典: tenant={}, channel={}, domain={}", tenant, channel, domain);
        return getPatternRuleList(DictionaryType.INTENT_ENTITY_PATTERNS, tenant, channel, domain);
    }
    
    @Override
    @Cacheable(value = "dictionary:intent_query_type_patterns", key = "#tenant + ':' + #channel + ':' + #domain")
    public List<PatternRuleDTO> getIntentQueryTypePatterns(String tenant, String channel, String domain) {
        log.debug("获取意图查询类型模式字典: tenant={}, channel={}, domain={}", tenant, channel, domain);
        return getPatternRuleList(DictionaryType.INTENT_QUERY_TYPE_PATTERNS, tenant, channel, domain);
    }
    
    @Override
    @Cacheable(value = "dictionary:intent_comparison_patterns", key = "#tenant + ':' + #channel + ':' + #domain")
    public List<PatternRuleDTO> getIntentComparisonPatterns(String tenant, String channel, String domain) {
        log.debug("获取意图比较模式字典: tenant={}, channel={}, domain={}", tenant, channel, domain);
        return getPatternRuleList(DictionaryType.INTENT_COMPARISON_PATTERNS, tenant, channel, domain);
    }
    
    @Override
    @Cacheable(value = "dictionary:rewrite_colloquial_patterns", key = "#tenant + ':' + #channel + ':' + #domain")
    public List<PatternRuleDTO> getRewriteColloquialPatterns(String tenant, String channel, String domain) {
        log.debug("获取改写口语化模式字典: tenant={}, channel={}, domain={}", tenant, channel, domain);
        return getPatternRuleList(DictionaryType.REWRITE_COLLOQUIAL_PATTERNS, tenant, channel, domain);
    }
    
    @Override
    @Cacheable(value = "dictionary:rewrite_keyword_weight_patterns", key = "#tenant + ':' + #channel + ':' + #domain")
    public List<PatternWeightDTO> getRewriteKeywordWeightPatterns(String tenant, String channel, String domain) {
        log.debug("获取改写关键词权重模式字典: tenant={}, channel={}, domain={}", tenant, channel, domain);
        return getPatternWeightList(DictionaryType.REWRITE_KEYWORD_WEIGHT_PATTERNS, tenant, channel, domain);
    }
    
    @Override
    @Cacheable(value = "dictionary:rewrite_tech_terms", key = "#tenant + ':' + #channel + ':' + #domain")
    public Map<String, String> getRewriteTechTermMappings(String tenant, String channel, String domain) {
        log.debug("获取改写技术术语映射字典: tenant={}, channel={}, domain={}", tenant, channel, domain);
        return getMapData(DictionaryType.REWRITE_TECH_TERM_MAPPINGS, tenant, channel, domain);
    }
    
    @Override
    @Cacheable(value = "dictionary:rewrite_stopwords", key = "#tenant + ':' + #channel + ':' + #domain")
    public Set<String> getRewriteStopwords(String tenant, String channel, String domain) {
        log.debug("获取改写停用词字典: tenant={}, channel={}, domain={}", tenant, channel, domain);
        return getSetData(DictionaryType.REWRITE_STOPWORDS, tenant, channel, domain);
    }
    
    @Override
    @Cacheable(value = "dictionary:prefix_source_words", key = "#tenant + ':' + #channel + ':' + #domain")
    public Set<String> getPrefixSourceWords(String tenant, String channel, String domain) {
        log.debug("获取前缀源词字典: tenant={}, channel={}, domain={}", tenant, channel, domain);
        return getSetData(DictionaryType.PREFIX_SOURCE_WORDS, tenant, channel, domain);
    }
    
    // ==================== 通用数据获取方法 ====================
    
    /**
     * 获取Map类型数据
     */
    private Map<String, String> getMapData(DictionaryType type, String tenant, String channel, String domain) {
        DictionaryConfig config = DictionaryConfig.of(tenant, channel, domain);
        List<DictionaryEntry> entries = dictionaryGateway.findActiveEntries(type, config);
        
        Map<String, String> result = new HashMap<>();
        for (DictionaryEntry entry : entries) {
            try {
                String value = parseStringValue(entry.getEntryValue());
                result.put(entry.getEntryKey(), value);
            } catch (Exception e) {
                log.warn("解析{}数据失败: key={}, value={}", type.getName(), entry.getEntryKey(), entry.getEntryValue(), e);
            }
        }
        
        updateCacheStats(type.getCode(), tenant, channel, domain, result.size());
        return result;
    }
    
    /**
     * 获取Set类型数据
     */
    private Set<String> getSetData(DictionaryType type, String tenant, String channel, String domain) {
        DictionaryConfig config = DictionaryConfig.of(tenant, channel, domain);
        List<DictionaryEntry> entries = dictionaryGateway.findActiveEntries(type, config);
        
        Set<String> result = new HashSet<>();
        for (DictionaryEntry entry : entries) {
            try {
                if (entry.getEntryValue().startsWith("[")) {
                    List<String> values = parseStringListValue(entry.getEntryValue());
                    result.addAll(values);
                } else {
                    String value = parseStringValue(entry.getEntryValue());
                    result.add(value);
                }
            } catch (Exception e) {
                log.warn("解析{}数据失败: key={}, value={}", type.getName(), entry.getEntryKey(), entry.getEntryValue(), e);
            }
        }
        
        updateCacheStats(type.getCode(), tenant, channel, domain, result.size());
        return result;
    }
    
    /**
     * 获取MapSet类型数据
     */
    private Map<String, Set<String>> getMapSetData(DictionaryType type, String tenant, String channel, String domain) {
        DictionaryConfig config = DictionaryConfig.of(tenant, channel, domain);
        List<DictionaryEntry> entries = dictionaryGateway.findActiveEntries(type, config);
        
        Map<String, Set<String>> result = new HashMap<>();
        for (DictionaryEntry entry : entries) {
            try {
                List<String> values = parseStringListValue(entry.getEntryValue());
                result.put(entry.getEntryKey(), new HashSet<>(values));
            } catch (Exception e) {
                log.warn("解析{}数据失败: key={}, value={}", type.getName(), entry.getEntryKey(), entry.getEntryValue(), e);
            }
        }
        
        updateCacheStats(type.getCode(), tenant, channel, domain, result.size());
        return result;
    }
    
    /**
     * 获取PatternRule列表数据
     */
    private List<PatternRuleDTO> getPatternRuleList(DictionaryType type, String tenant, String channel, String domain) {
        DictionaryConfig config = DictionaryConfig.of(tenant, channel, domain);
        List<DictionaryEntry> entries = dictionaryGateway.findActiveEntries(type, config);
        
        List<PatternRuleDTO> result = new ArrayList<>();
        for (DictionaryEntry entry : entries) {
            try {
                PatternRule patternRule = parsePatternRule(entry.getEntryValue());
                if (patternRule != null) {
                    result.add(convertToPatternRuleDTO(patternRule));
                    // 预编译Pattern并缓存
                    cacheCompiledPattern(entry.getEntryKey(), patternRule.getPattern(), 
                                       patternRule.getIgnoreCase(), patternRule.getMultiline(), patternRule.getDotAll());
                }
            } catch (Exception e) {
                log.warn("解析{}数据失败: key={}, value={}", type.getName(), entry.getEntryKey(), entry.getEntryValue(), e);
            }
        }
        
        updateCacheStats(type.getCode(), tenant, channel, domain, result.size());
        return result;
    }
    
    /**
     * 获取PatternWeight列表数据
     */
    private List<PatternWeightDTO> getPatternWeightList(DictionaryType type, String tenant, String channel, String domain) {
        DictionaryConfig config = DictionaryConfig.of(tenant, channel, domain);
        List<DictionaryEntry> entries = dictionaryGateway.findActiveEntries(type, config);
        
        List<PatternWeightDTO> result = new ArrayList<>();
        for (DictionaryEntry entry : entries) {
            try {
                PatternWeight patternWeight = parsePatternWeight(entry.getEntryValue());
                if (patternWeight != null) {
                    result.add(convertToPatternWeightDTO(patternWeight));
                    // 预编译Pattern并缓存
                    cacheCompiledPattern(entry.getEntryKey(), patternWeight.getPattern(), 
                                       patternWeight.getIgnoreCase(), patternWeight.getMultiline(), false);
                }
            } catch (Exception e) {
                log.warn("解析{}数据失败: key={}, value={}", type.getName(), entry.getEntryKey(), entry.getEntryValue(), e);
            }
        }
        
        updateCacheStats(type.getCode(), tenant, channel, domain, result.size());
        return result;
    }
    
    /**
     * 获取IntentCatalog映射数据
     */
    private Map<String, IntentCatalogDTO> getIntentCatalogMap(DictionaryType type, String tenant, String channel, String domain) {
        DictionaryConfig config = DictionaryConfig.of(tenant, channel, domain);
        List<DictionaryEntry> entries = dictionaryGateway.findActiveEntries(type, config);
        
        Map<String, IntentCatalogDTO> result = new HashMap<>();
        for (DictionaryEntry entry : entries) {
            try {
                IntentCatalog intentCatalog = parseIntentCatalog(entry.getEntryValue());
                if (intentCatalog != null) {
                    result.put(entry.getEntryKey(), convertToIntentCatalogDTO(intentCatalog));
                }
            } catch (Exception e) {
                log.warn("解析{}数据失败: key={}, value={}", type.getName(), entry.getEntryKey(), entry.getEntryValue(), e);
            }
        }
        
        updateCacheStats(type.getCode(), tenant, channel, domain, result.size());
        return result;
    }
    
    // ==================== 解析和转换方法 ====================
    
    /**
     * 解析PatternRule
     */
    private PatternRule parsePatternRule(String jsonValue) throws Exception {
        if (jsonValue == null || jsonValue.trim().isEmpty()) {
            return null;
        }
        
        return objectMapper.readValue(jsonValue, PatternRule.class);
    }
    
    /**
     * 解析PatternWeight
     */
    private PatternWeight parsePatternWeight(String jsonValue) throws Exception {
        if (jsonValue == null || jsonValue.trim().isEmpty()) {
            return null;
        }
        
        return objectMapper.readValue(jsonValue, PatternWeight.class);
    }
    
    /**
     * 解析IntentCatalog
     */
    private IntentCatalog parseIntentCatalog(String jsonValue) throws Exception {
        if (jsonValue == null || jsonValue.trim().isEmpty()) {
            return null;
        }
        
        return objectMapper.readValue(jsonValue, IntentCatalog.class);
    }
    
    /**
     * 转换PatternRule到DTO
     */
    private PatternRuleDTO convertToPatternRuleDTO(PatternRule patternRule) {
        return PatternRuleDTO.builder()
                .name(patternRule.getName())
                .pattern(patternRule.getPattern())
                .replacement(patternRule.getReplacement())
                .description(patternRule.getDescription())
                .enabled(patternRule.getEnabled())
                .priority(patternRule.getPriority())
                .ignoreCase(patternRule.getIgnoreCase())
                .multiline(patternRule.getMultiline())
                .dotAll(patternRule.getDotAll())
                .build();
    }
    
    /**
     * 转换PatternWeight到DTO
     */
    private PatternWeightDTO convertToPatternWeightDTO(PatternWeight patternWeight) {
        return PatternWeightDTO.builder()
                .name(patternWeight.getName())
                .pattern(patternWeight.getPattern())
                .weight(patternWeight.getWeight())
                .handlerId(patternWeight.getHandlerId())
                .description(patternWeight.getDescription())
                .enabled(patternWeight.getEnabled())
                .priority(patternWeight.getPriority())
                .ignoreCase(patternWeight.getIgnoreCase())
                .multiline(patternWeight.getMultiline())
                .weightMode(patternWeight.getWeightMode())
                .build();
    }
    
    /**
     * 转换IntentCatalog到DTO
     */
    private IntentCatalogDTO convertToIntentCatalogDTO(IntentCatalog intentCatalog) {
        return IntentCatalogDTO.builder()
                .intentId(intentCatalog.getIntentId())
                .intentName(intentCatalog.getIntentName())
                .description(intentCatalog.getDescription())
                .parentIntentId(intentCatalog.getParentIntentId())
                .intentType(intentCatalog.getIntentType())
                .weight(intentCatalog.getWeight())
                .enabled(intentCatalog.getEnabled())
                .keywords(intentCatalog.getKeywords())
                .entityTypes(intentCatalog.getEntityTypes())
                .queryPatterns(intentCatalog.getQueryPatterns())
                .handlerId(intentCatalog.getHandlerId())
                .properties(intentCatalog.getProperties())
                .build();
    }
    
    /**
     * 缓存编译后的Pattern
     */
    private void cacheCompiledPattern(String key, String pattern, Boolean ignoreCase, Boolean multiline, Boolean dotAll) {
        if (pattern == null || pattern.isEmpty()) {
            return;
        }
        
        try {
            int flags = 0;
            if (Boolean.TRUE.equals(ignoreCase)) {
                flags |= Pattern.CASE_INSENSITIVE;
            }
            if (Boolean.TRUE.equals(multiline)) {
                flags |= Pattern.MULTILINE;
            }
            if (Boolean.TRUE.equals(dotAll)) {
                flags |= Pattern.DOTALL;
            }
            
            Pattern compiledPattern = Pattern.compile(pattern, flags);
            compiledPatternCache.put(key, compiledPattern);
        } catch (Exception e) {
            log.warn("编译Pattern失败: key={}, pattern={}", key, pattern, e);
        }
    }
    
    /**
     * 获取编译后的Pattern
     */
    public Pattern getCompiledPattern(String key) {
        return compiledPatternCache.get(key);
    }
    
    /**
     * 构建Trie树
     */
    private TrieNode buildTrie(Set<String> words) {
        TrieNode root = new TrieNode();
        for (String word : words) {
            if (word != null && !word.trim().isEmpty()) {
                insertToTrie(root, word.trim().toLowerCase());
            }
        }
        return root;
    }
    
    /**
     * 向Trie树插入单词
     */
    private void insertToTrie(TrieNode root, String word) {
        TrieNode current = root;
        for (char c : word.toCharArray()) {
            current.children.computeIfAbsent(c, k -> new TrieNode());
            current = current.children.get(c);
        }
        current.isEnd = true;
    }
    
    /**
     * Trie树节点
     */
    private static class TrieNode {
        Map<Character, TrieNode> children = new HashMap<>();
        boolean isEnd = false;
    }
    
    /**
     * 构建字典数据DTO
     */
    private DictionaryDataDTO buildDictionaryDataDTO(DictionaryType type, 
                                                   DictionaryConfig config, 
                                                   List<DictionaryEntry> entries,
                                                   Long version) {
        DictionaryDataDTO.DictionaryDataDTOBuilder builder = DictionaryDataDTO.builder()
                .dictionaryType(type.getCode())
                .tenant(config.getTenant())
                .channel(config.getChannel())
                .domain(config.getDomain())
                .version(version)
                .entryCount(entries.size())
                .updateTimestamp(System.currentTimeMillis());
        
        if (type.isMapSetType()) {
            // 复合映射类型：Map<String, Set<String>>
            Map<String, Object> mappingData = new HashMap<>();
            for (DictionaryEntry entry : entries) {
                try {
                    List<String> values = parseStringListValue(entry.getEntryValue());
                    mappingData.put(entry.getEntryKey(), new HashSet<>(values));
                } catch (Exception e) {
                    log.warn("解析复合映射数据失败: key={}", entry.getEntryKey(), e);
                }
            }
            builder.mappingData(mappingData);
        } else if (type.isMapType()) {
            // 简单映射类型：Map<String, String>
            Map<String, Object> mappingData = new HashMap<>();
            for (DictionaryEntry entry : entries) {
                try {
                    String value = parseStringValue(entry.getEntryValue());
                    mappingData.put(entry.getEntryKey(), value);
                } catch (Exception e) {
                    log.warn("解析映射数据失败: key={}", entry.getEntryKey(), e);
                }
            }
            builder.mappingData(mappingData);
        } else if (type.isSetType()) {
            // 集合类型：Set<String>
            Set<String> setData = new HashSet<>();
            for (DictionaryEntry entry : entries) {
                try {
                    if (entry.getEntryValue().startsWith("[")) {
                        List<String> values = parseStringListValue(entry.getEntryValue());
                        setData.addAll(values);
                    } else {
                        String value = parseStringValue(entry.getEntryValue());
                        setData.add(value);
                    }
                } catch (Exception e) {
                    log.warn("解析集合数据失败: key={}", entry.getEntryKey(), e);
                }
            }
            builder.setData(setData);
        } else if (type.isListType()) {
            // 列表类型：List<PatternRule> 或 List<PatternWeight>
            List<Object> listData = new ArrayList<>();
            for (DictionaryEntry entry : entries) {
                try {
                    if (type.isRuleListType()) {
                        PatternRule rule = parsePatternRule(entry.getEntryValue());
                        if (rule != null) {
                            listData.add(convertToPatternRuleDTO(rule));
                        }
                    } else if (type.isWeightListType()) {
                        PatternWeight weight = parsePatternWeight(entry.getEntryValue());
                        if (weight != null) {
                            listData.add(convertToPatternWeightDTO(weight));
                        }
                    }
                } catch (Exception e) {
                    log.warn("解析列表数据失败: key={}", entry.getEntryKey(), e);
                }
            }
            builder.listData(listData);
        } else if (type.isIntentCatalogType()) {
            // 意图目录类型：Map<String, IntentCatalog>
            Map<String, Object> mappingData = new HashMap<>();
            for (DictionaryEntry entry : entries) {
                try {
                    IntentCatalog intentCatalog = parseIntentCatalog(entry.getEntryValue());
                    if (intentCatalog != null) {
                        mappingData.put(entry.getEntryKey(), convertToIntentCatalogDTO(intentCatalog));
                    }
                } catch (Exception e) {
                    log.warn("解析意图目录数据失败: key={}", entry.getEntryKey(), e);
                }
            }
            builder.mappingData(mappingData);
        }
        
        return builder.build();
    }
    
    /**
     * 解析字符串值
     */
    private String parseStringValue(String jsonValue) throws Exception {
        if (jsonValue == null) {
            return null;
        }
        
        jsonValue = jsonValue.trim();
        if (jsonValue.startsWith("\"") && jsonValue.endsWith("\"")) {
            return objectMapper.readValue(jsonValue, String.class);
        }
        
        return jsonValue;
    }
    
    /**
     * 解析字符串列表值
     */
    private List<String> parseStringListValue(String jsonValue) throws Exception {
        if (jsonValue == null) {
            return List.of();
        }
        
        return objectMapper.readValue(jsonValue, new TypeReference<List<String>>() {});
    }
    
    /**
     * 更新缓存统计信息
     */
    /**
     * 更新缓存统计信息
     */
    private void updateCacheStats(String dictionaryType, String tenant, String channel, String domain, int count) {
        String key = String.format("%s:%s:%s:%s", dictionaryType, tenant, channel, domain);
        Map<String, Object> stats = new HashMap<>();
        stats.put("type", dictionaryType);
        stats.put("tenant", tenant);
        stats.put("channel", channel);
        stats.put("domain", domain);
        stats.put("count", count);
        stats.put("lastAccess", System.currentTimeMillis());
        
        cacheStatsMap.put(key, stats);
    }
    
    /**
     * 清理缓存
     */
    public void clearCompiledPatternCache() {
        compiledPatternCache.clear();
        log.info("已清理编译Pattern缓存");
    }
    
    /**
     * 清理Trie缓存
     */
    public void clearTrieCache() {
        trieCache.clear();
        log.info("已清理Trie缓存");
    }
    
    // ==================== M2 语义对齐阶段专用方法实现 ====================
    
    @Override
    @Cacheable(value = "dictionary-semantic-alignment", key = "#tenant + ':' + #channel + ':' + #domain + ':' + #locale")
    public Map<String, String> getSemanticAlignmentRules(String tenant, String channel, String domain, String locale) {
        log.debug("获取语义对齐规则: tenant={}, channel={}, domain={}, locale={}", tenant, channel, domain, locale);
        return getStringToStringMapping(DictionaryType.SEMANTIC_ALIGNMENT, tenant, channel, domain, locale);
    }
    
    @Override
    @Cacheable(value = "dictionary-semantic-keywords", key = "#tenant + ':' + #channel + ':' + #domain + ':' + #locale")
    public Map<String, String> getSemanticKeywords(String tenant, String channel, String domain, String locale) {
        log.debug("获取语义关键词: tenant={}, channel={}, domain={}, locale={}", tenant, channel, domain, locale);
        return getStringToStringMapping(DictionaryType.SEMANTIC_KEYWORDS, tenant, channel, domain, locale);
    }
    
    @Override
    @Cacheable(value = "dictionary-semantic-categories", key = "#tenant + ':' + #channel + ':' + #domain + ':' + #locale")
    public Map<String, String> getSemanticCategories(String tenant, String channel, String domain, String locale) {
        log.debug("获取语义分类: tenant={}, channel={}, domain={}, locale={}", tenant, channel, domain, locale);
        return getStringToStringMapping(DictionaryType.SEMANTIC_CATEGORIES, tenant, channel, domain, locale);
    }
    
    @Override
    @Cacheable(value = "dictionary-domain-terms", key = "#tenant + ':' + #channel + ':' + #domain + ':' + #locale")
    public Map<String, String> getDomainTerms(String tenant, String channel, String domain, String locale) {
        log.debug("获取领域术语: tenant={}, channel={}, domain={}, locale={}", tenant, channel, domain, locale);
        return getStringToStringMapping(DictionaryType.DOMAIN_TERMS, tenant, channel, domain, locale);
    }
    
    // ==================== M3 意图提取与改写扩展阶段专用方法实现 ====================
    
    @Override
    @Cacheable(value = "dictionary-intent-patterns", key = "#tenant + ':' + #channel + ':' + #domain + ':' + #locale")
    public Map<String, String> getIntentPatterns(String tenant, String channel, String domain, String locale) {
        log.debug("获取意图模式: tenant={}, channel={}, domain={}, locale={}", tenant, channel, domain, locale);
        return getStringToStringMapping(DictionaryType.INTENT_PATTERNS, tenant, channel, domain, locale);
    }
    
    @Override
    @Cacheable(value = "dictionary-intent-keywords", key = "#tenant + ':' + #channel + ':' + #domain + ':' + #locale")
    public Map<String, String> getIntentKeywords(String tenant, String channel, String domain, String locale) {
        log.debug("获取意图关键词: tenant={}, channel={}, domain={}, locale={}", tenant, channel, domain, locale);
        return getStringToStringMapping(DictionaryType.INTENT_KEYWORDS, tenant, channel, domain, locale);
    }
    
    @Override
    @Cacheable(value = "dictionary-rewrite-rules", key = "#tenant + ':' + #channel + ':' + #domain + ':' + #locale")
    public Map<String, String> getRewriteRules(String tenant, String channel, String domain, String locale) {
        log.debug("获取改写规则: tenant={}, channel={}, domain={}, locale={}", tenant, channel, domain, locale);
        return getStringToStringMapping(DictionaryType.REWRITE_RULES, tenant, channel, domain, locale);
    }
    
    @Override
    @Cacheable(value = "dictionary-expansion-strategies", key = "#tenant + ':' + #channel + ':' + #domain + ':' + #locale")
    public Map<String, String> getExpansionStrategies(String tenant, String channel, String domain, String locale) {
        log.debug("获取扩展策略: tenant={}, channel={}, domain={}, locale={}", tenant, channel, domain, locale);
        return getStringToStringMapping(DictionaryType.EXPANSION_STRATEGIES, tenant, channel, domain, locale);
    }
    
    // ==================== 辅助方法 ====================
    
    /**
     * 获取支持语言环境的字符串到字符串映射
     * 通用方法，用于获取带语言环境支持的字典数据
     */
    private Map<String, String> getStringToStringMapping(DictionaryType type, String tenant, String channel, String domain, String locale) {
        // 构建支持locale的配置key，通过在domain后添加locale后缀来实现
        String domainWithLocale = (locale != null && !locale.trim().isEmpty()) ? 
            domain + "_" + locale : domain;
            
        DictionaryConfig config = DictionaryConfig.of(tenant, channel, domainWithLocale);
        List<DictionaryEntry> entries = dictionaryGateway.findActiveEntries(type, config);
        
        // 如果没有找到带locale的数据，则回退到不带locale的默认数据
        if (entries.isEmpty() && locale != null && !locale.trim().isEmpty()) {
            log.debug("未找到{}语言环境的{}数据，回退到默认配置", locale, type.getName());
            config = DictionaryConfig.of(tenant, channel, domain);
            entries = dictionaryGateway.findActiveEntries(type, config);
        }
        
        Map<String, String> result = new HashMap<>();
        for (DictionaryEntry entry : entries) {
            try {
                String value = entry.getEntryValue();
                if (value != null && !value.trim().isEmpty()) {
                    result.put(entry.getEntryKey(), value.trim());
                }
            } catch (Exception e) {
                log.warn("解析{}数据失败: key={}, value={}", type.getName(), entry.getEntryKey(), entry.getEntryValue(), e);
            }
        }
        
        String cacheKey = type.getCode() + (locale != null ? "_" + locale : "");
        updateCacheStats(cacheKey, tenant, channel, domain, result.size());
        log.debug("获取{}字典数据完成: locale={}, size={}", type.getName(), locale, result.size());
        return result;
    }
    
    // ==================== 槽位模板和意图字典服务实现 ====================
    
    @Override
    @Cacheable(value = "intent-slot-templates", key = "#tenant + ':' + #channel + ':' + #domain")
    public Map<String, SlotTemplateDTO> getIntentSlotTemplates(String tenant, String channel, String domain) {
        log.debug("获取意图槽位模板: tenant={}, channel={}, domain={}", tenant, channel, domain);
        
        Map<String, SlotTemplateDTO> templates = slotTemplateGateway.getIntentSlotTemplates(tenant, channel, domain);
        updateCacheStats("slot-templates", tenant, channel, domain, templates.size());
        return templates;
    }
    
    @Override
    @Cacheable(value = "slot-template-by-intent", key = "#intentCode + ':' + #tenant + ':' + #channel + ':' + #domain")
    public SlotTemplateDTO getSlotTemplateByIntent(String intentCode, String tenant, String channel, String domain) {
        log.debug("获取意图槽位模板: intentCode={}, tenant={}, channel={}, domain={}", intentCode, tenant, channel, domain);
        
        return slotTemplateGateway.getSlotTemplateByIntent(intentCode, tenant, channel, domain);
    }
    
    @Override
    @Cacheable(value = "intent-dictionaries", key = "#tenant + ':' + #channel + ':' + #domain")
    public Map<String, IntentDictionaryDTO> getIntentDictionaries(String tenant, String channel, String domain) {
        log.debug("获取意图字典映射: tenant={}, channel={}, domain={}", tenant, channel, domain);
        
        Map<String, IntentDictionaryDTO> dictionaries = slotTemplateGateway.getIntentDictionaries(tenant, channel, domain);
        updateCacheStats("intent-dictionaries", tenant, channel, domain, dictionaries.size());
        return dictionaries;
    }
    
    @Override
    @Cacheable(value = "intent-dictionary", key = "#intentCode + ':' + #tenant + ':' + #channel + ':' + #domain")
    public IntentDictionaryDTO getIntentDictionary(String intentCode, String tenant, String channel, String domain) {
        log.debug("获取意图字典: intentCode={}, tenant={}, channel={}, domain={}", intentCode, tenant, channel, domain);
        
        return slotTemplateGateway.getIntentDictionary(intentCode, tenant, channel, domain);
    }
}