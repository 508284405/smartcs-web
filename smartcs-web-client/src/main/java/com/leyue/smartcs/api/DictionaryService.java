package com.leyue.smartcs.api;

import com.leyue.smartcs.dto.dictionary.DictionaryDataDTO;
import com.leyue.smartcs.dto.dictionary.DictionaryEntryDTO;
import com.leyue.smartcs.dto.dictionary.PatternRuleDTO;
import com.leyue.smartcs.dto.dictionary.PatternWeightDTO;
import com.leyue.smartcs.dto.dictionary.IntentCatalogDTO;
import com.leyue.smartcs.dto.intent.SlotTemplateDTO;
import com.leyue.smartcs.dto.intent.IntentDictionaryDTO;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 字典服务接口 - 面向业务运行时的字典查询服务
 * 提供高性能的字典数据查询功能，支持多租户、多渠道、多领域配置
 * 
 * 核心特性：
 * - 支持多种字典类型的数据查询
 * - 提供缓存优化的高性能访问
 * - 支持多租户隔离和多渠道配置
 * - 版本管理和热更新支持
 * 
 * @author Claude
 */
public interface DictionaryService {
    
    /**
     * 获取标准化规则字典数据
     * 用于查询标准化阶段的文本清理和标准化处理
     * 
     * @param tenant 租户标识
     * @param channel 渠道标识
     * @param domain 领域标识
     * @return 标准化规则映射 Map<原始文本, 标准化文本>
     */
    Map<String, String> getNormalizationRules(String tenant, String channel, String domain);
    
    /**
     * 获取拼音纠错字典数据
     * 用于拼音改写阶段的错拼纠正处理
     * 
     * @param tenant 租户标识
     * @param channel 渠道标识
     * @param domain 领域标识
     * @return 拼音纠错映射 Map<错误拼音, 正确文本>
     */
    Map<String, String> getPhoneticCorrections(String tenant, String channel, String domain);
    
    /**
     * 获取前缀补全词典数据
     * 用于前缀补全阶段的查询词汇扩展
     * 
     * @param tenant 租户标识
     * @param channel 渠道标识
     * @param domain 领域标识
     * @return 前缀补全词汇集合
     */
    Set<String> getPrefixWords(String tenant, String channel, String domain);
    
    /**
     * 获取同义词召回字典数据
     * 用于同义词召回阶段的语义扩展处理
     * 
     * @param tenant 租户标识
     * @param channel 渠道标识
     * @param domain 领域标识
     * @return 同义词组映射 Map<词汇, Set<同义词>>
     */
    Map<String, Set<String>> getSynonymSets(String tenant, String channel, String domain);
    
    /**
     * 获取停用词字典数据
     * 用于查询标准化阶段的停用词过滤
     * 
     * @param tenant 租户标识
     * @param channel 渠道标识
     * @param domain 领域标识
     * @return 停用词集合
     */
    Set<String> getStopWords(String tenant, String channel, String domain);
    
    /**
     * 根据字典类型获取指定配置的字典数据
     * 通用方法，支持所有字典类型的统一访问
     * 
     * @param dictionaryType 字典类型
     * @param tenant 租户标识
     * @param channel 渠道标识
     * @param domain 领域标识
     * @return 字典数据DTO
     */
    DictionaryDataDTO getDictionaryData(String dictionaryType, String tenant, String channel, String domain);
    
    /**
     * 批量获取多种类型的字典数据
     * 用于一次性获取某个配置下的所有字典类型数据，提升性能
     * 
     * @param dictionaryTypes 字典类型列表
     * @param tenant 租户标识
     * @param channel 渠道标识
     * @param domain 领域标识
     * @return 字典类型到数据的映射
     */
    Map<String, DictionaryDataDTO> getBatchDictionaryData(List<String> dictionaryTypes, 
                                                         String tenant, String channel, String domain);
    
    /**
     * 获取字典数据的当前版本信息
     * 用于缓存版本控制和热更新判断
     * 
     * @param dictionaryType 字典类型
     * @param tenant 租户标识
     * @param channel 渠道标识
     * @param domain 领域标识
     * @return 版本戳（通常是时间戳）
     */
    Long getDictionaryVersion(String dictionaryType, String tenant, String channel, String domain);
    
    /**
     * 检查字典数据是否存在
     * 用于判断特定配置下的字典类型是否有数据
     * 
     * @param dictionaryType 字典类型
     * @param tenant 租户标识
     * @param channel 渠道标识
     * @param domain 领域标识
     * @return 是否存在数据
     */
    boolean hasDictionaryData(String dictionaryType, String tenant, String channel, String domain);
    
    /**
     * 刷新字典缓存
     * 用于手动触发缓存刷新，支持热更新
     * 
     * @param dictionaryType 字典类型（null表示刷新所有类型）
     * @param tenant 租户标识（null表示所有租户）
     * @param channel 渠道标识（null表示所有渠道）
     * @param domain 领域标识（null表示所有领域）
     */
    void refreshCache(String dictionaryType, String tenant, String channel, String domain);
    
    /**
     * 获取缓存统计信息
     * 用于监控和调试缓存性能
     * 
     * @return 缓存统计信息
     */
    Map<String, Object> getCacheStats();
    
    // ==================== 查询转换器专用字典方法 ====================
    
    /**
     * 获取标准化停用词数据
     * 用于查询标准化阶段的停用词过滤处理
     * 
     * @param tenant 租户标识
     * @param channel 渠道标识
     * @param domain 领域标识
     * @return 停用词集合
     */
    Set<String> getNormalizationStopwords(String tenant, String channel, String domain);
    
    /**
     * 获取标准化拼写纠错数据
     * 用于查询标准化阶段的拼写错误纠正
     * 
     * @param tenant 租户标识
     * @param channel 渠道标识
     * @param domain 领域标识
     * @return 拼写纠错映射
     */
    Map<String, String> getNormalizationSpellingCorrections(String tenant, String channel, String domain);
    
    /**
     * 获取语义同义词数据
     * 用于语义对齐阶段的同义词处理
     * 
     * @param tenant 租户标识
     * @param channel 渠道标识
     * @param domain 领域标识
     * @return 同义词组映射
     */
    Map<String, Set<String>> getSemanticSynonyms(String tenant, String channel, String domain);
    
    /**
     * 获取语义单位映射数据
     * 用于语义对齐阶段的单位标准化处理
     * 
     * @param tenant 租户标识
     * @param channel 渠道标识
     * @param domain 领域标识
     * @return 单位映射
     */
    Map<String, String> getSemanticUnitMappings(String tenant, String channel, String domain);
    
    /**
     * 获取语义时间模式数据
     * 用于语义对齐阶段的时间表达式识别与标准化
     * 
     * @param tenant 租户标识
     * @param channel 渠道标识
     * @param domain 领域标识
     * @return 时间模式规则列表
     */
    List<PatternRuleDTO> getSemanticTimePatterns(String tenant, String channel, String domain);
    
    /**
     * 获取语义实体别名数据
     * 用于语义对齐阶段的实体别名标准化处理
     * 
     * @param tenant 租户标识
     * @param channel 渠道标识
     * @param domain 领域标识
     * @return 实体别名映射
     */
    Map<String, String> getSemanticEntityAliases(String tenant, String channel, String domain);
    
    /**
     * 获取意图目录数据
     * 用于意图识别阶段的意图分类目录
     * 
     * @param tenant 租户标识
     * @param channel 渠道标识
     * @param domain 领域标识
     * @return 意图目录映射
     */
    Map<String, IntentCatalogDTO> getIntentCatalog(String tenant, String channel, String domain);
    
    /**
     * 获取意图实体模式数据
     * 用于意图识别阶段的实体模式匹配
     * 
     * @param tenant 租户标识
     * @param channel 渠道标识
     * @param domain 领域标识
     * @return 实体模式规则列表
     */
    List<PatternRuleDTO> getIntentEntityPatterns(String tenant, String channel, String domain);
    
    /**
     * 获取意图查询类型模式数据
     * 用于意图识别阶段的查询类型判断
     * 
     * @param tenant 租户标识
     * @param channel 渠道标识
     * @param domain 领域标识
     * @return 查询类型模式规则列表
     */
    List<PatternRuleDTO> getIntentQueryTypePatterns(String tenant, String channel, String domain);
    
    /**
     * 获取意图比较模式数据
     * 用于意图识别阶段的比较操作识别
     * 
     * @param tenant 租户标识
     * @param channel 渠道标识
     * @param domain 领域标识
     * @return 比较模式规则列表
     */
    List<PatternRuleDTO> getIntentComparisonPatterns(String tenant, String channel, String domain);
    
    /**
     * 获取改写口语化模式数据
     * 用于查询改写阶段的口语化表达转换
     * 
     * @param tenant 租户标识
     * @param channel 渠道标识
     * @param domain 领域标识
     * @return 口语化模式规则列表
     */
    List<PatternRuleDTO> getRewriteColloquialPatterns(String tenant, String channel, String domain);
    
    /**
     * 获取改写关键词权重模式数据
     * 用于查询改写阶段的关键词权重调整
     * 
     * @param tenant 租户标识
     * @param channel 渠道标识
     * @param domain 领域标识
     * @return 关键词权重规则列表
     */
    List<PatternWeightDTO> getRewriteKeywordWeightPatterns(String tenant, String channel, String domain);
    
    /**
     * 获取改写技术术语映射数据
     * 用于查询改写阶段的技术术语标准化
     * 
     * @param tenant 租户标识
     * @param channel 渠道标识
     * @param domain 领域标识
     * @return 技术术语映射
     */
    Map<String, String> getRewriteTechTermMappings(String tenant, String channel, String domain);
    
    /**
     * 获取改写停用词数据
     * 用于查询改写阶段的停用词处理
     * 
     * @param tenant 租户标识
     * @param channel 渠道标识
     * @param domain 领域标识
     * @return 停用词集合
     */
    Set<String> getRewriteStopwords(String tenant, String channel, String domain);
    
    /**
     * 获取前缀源词数据
     * 用于前缀补全阶段的源词汇管理
     * 
     * @param tenant 租户标识
     * @param channel 渠道标识
     * @param domain 领域标识
     * @return 源词汇集合
     */
    Set<String> getPrefixSourceWords(String tenant, String channel, String domain);
    
    // ==================== M2 语义对齐阶段专用方法 ====================
    
    /**
     * 获取语义对齐规则数据
     * 用于语义对齐阶段的同义词归一化处理
     * 
     * @param tenant 租户标识
     * @param channel 渠道标识
     * @param domain 领域标识
     * @param locale 语言标识
     * @return 语义对齐规则映射
     */
    Map<String, String> getSemanticAlignmentRules(String tenant, String channel, String domain, String locale);
    
    /**
     * 获取语义关键词数据
     * 用于语义对齐阶段的关键词标准化处理
     * 
     * @param tenant 租户标识
     * @param channel 渠道标识
     * @param domain 领域标识
     * @param locale 语言标识
     * @return 语义关键词映射
     */
    Map<String, String> getSemanticKeywords(String tenant, String channel, String domain, String locale);
    
    /**
     * 获取语义分类数据
     * 用于语义对齐阶段的分类标准化处理
     * 
     * @param tenant 租户标识
     * @param channel 渠道标识
     * @param domain 领域标识
     * @param locale 语言标识
     * @return 语义分类映射
     */
    Map<String, String> getSemanticCategories(String tenant, String channel, String domain, String locale);
    
    /**
     * 获取领域术语数据
     * 用于语义对齐阶段的领域术语标准化处理
     * 
     * @param tenant 租户标识
     * @param channel 渠道标识
     * @param domain 领域标识
     * @param locale 语言标识
     * @return 领域术语映射
     */
    Map<String, String> getDomainTerms(String tenant, String channel, String domain, String locale);
    
    // ==================== M3 意图提取与改写扩展阶段专用方法 ====================
    
    /**
     * 获取意图模式数据
     * 用于意图提取阶段的意图识别模式匹配
     * 
     * @param tenant 租户标识
     * @param channel 渠道标识
     * @param domain 领域标识
     * @param locale 语言标识
     * @return 意图模式规则映射
     */
    Map<String, String> getIntentPatterns(String tenant, String channel, String domain, String locale);
    
    /**
     * 获取意图关键词数据
     * 用于意图提取阶段的关键词基础意图识别
     * 
     * @param tenant 租户标识
     * @param channel 渠道标识
     * @param domain 领域标识
     * @param locale 语言标识
     * @return 意图关键词映射
     */
    Map<String, String> getIntentKeywords(String tenant, String channel, String domain, String locale);
    
    /**
     * 获取改写规则数据
     * 用于改写阶段的查询重构规则
     * 
     * @param tenant 租户标识
     * @param channel 渠道标识
     * @param domain 领域标识
     * @param locale 语言标识
     * @return 改写规则映射
     */
    Map<String, String> getRewriteRules(String tenant, String channel, String domain, String locale);
    
    /**
     * 获取扩展策略数据
     * 用于扩展策略阶段的查询扩展规则
     * 
     * @param tenant 租户标识
     * @param channel 渠道标识
     * @param domain 领域标识
     * @param locale 语言标识
     * @return 扩展策略映射
     */
    Map<String, String> getExpansionStrategies(String tenant, String channel, String domain, String locale);
    
    // ==================== 槽位模板和意图字典服务 ====================
    
    /**
     * 获取意图槽位模板
     * 用于槽位填充阶段的槽位模板查询
     * 
     * @param tenant 租户标识
     * @param channel 渠道标识  
     * @param domain 领域标识
     * @return 意图编码到槽位模板的映射
     */
    Map<String, SlotTemplateDTO> getIntentSlotTemplates(String tenant, String channel, String domain);
    
    /**
     * 获取指定意图的槽位模板
     * 用于单个意图的槽位模板查询
     * 
     * @param intentCode 意图编码
     * @param tenant 租户标识
     * @param channel 渠道标识
     * @param domain 领域标识
     * @return 槽位模板，如果不存在则返回null
     */
    SlotTemplateDTO getSlotTemplateByIntent(String intentCode, String tenant, String channel, String domain);
    
    /**
     * 获取意图字典映射
     * 用于意图识别阶段的意图字典查询
     * 
     * @param tenant 租户标识
     * @param channel 渠道标识
     * @param domain 领域标识
     * @return 意图编码到意图字典的映射
     */
    Map<String, IntentDictionaryDTO> getIntentDictionaries(String tenant, String channel, String domain);
    
    /**
     * 获取指定意图的字典信息
     * 用于单个意图的字典信息查询
     * 
     * @param intentCode 意图编码
     * @param tenant 租户标识
     * @param channel 渠道标识
     * @param domain 领域标识
     * @return 意图字典，如果不存在则返回null
     */
    IntentDictionaryDTO getIntentDictionary(String intentCode, String tenant, String channel, String domain);
}