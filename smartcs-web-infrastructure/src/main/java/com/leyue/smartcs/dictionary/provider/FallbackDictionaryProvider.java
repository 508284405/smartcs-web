package com.leyue.smartcs.dictionary.provider;

import com.leyue.smartcs.domain.dictionary.enums.DictionaryType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 回退字典数据提供者
 * 当字典服务不可用时，提供静态的回退数据
 * 封装现有Stage中的静态常量映射数据
 * 
 * @author Claude
 */
@Slf4j
@Component
public class FallbackDictionaryProvider {
    
    /**
     * 默认标准化规则映射
     * 封装自NormalizationStage的SPELLING_CORRECTIONS
     */
    private static final Map<String, String> DEFAULT_NORMALIZATION_RULES = createNormalizationRules();
    
    /**
     * 默认拼音纠错映射
     * 封装常见的拼音错拼纠正
     */
    private static final Map<String, String> DEFAULT_PHONETIC_CORRECTIONS = createPhoneticCorrections();
    
    /**
     * 默认前缀词汇集合
     * 封装常用的查询前缀词汇
     */
    private static final Set<String> DEFAULT_PREFIX_WORDS = createPrefixWords();
    
    /**
     * 默认同义词映射
     * 封装常见的同义词组
     */
    private static final Map<String, Set<String>> DEFAULT_SYNONYM_SETS = createSynonymSets();
    
    /**
     * 默认停用词集合
     * 封装自Stage中的STOP_WORDS
     */
    private static final Set<String> DEFAULT_STOP_WORDS = createStopWords();
    
    /**
     * 默认领域术语映射
     * 封装领域特定的术语标准化
     */
    private static final Map<String, String> DEFAULT_DOMAIN_TERMS = createDomainTerms();
    
    /**
     * 默认缩写扩展映射
     * 封装常见的缩写全称对应
     */
    private static final Map<String, String> DEFAULT_ABBREVIATION_EXPANSIONS = createAbbreviationExpansions();
    
    /**
     * 根据字典类型获取回退数据
     * 
     * @param dictionaryType 字典类型
     * @param tenant 租户（当前不影响回退数据）
     * @param channel 渠道（当前不影响回退数据）
     * @param domain 领域（当前不影响回退数据）
     * @return 回退数据对象
     */
    public Object getFallbackData(DictionaryType dictionaryType, String tenant, String channel, String domain) {
        log.debug("获取回退字典数据: type={}, tenant={}, channel={}, domain={}", 
                 dictionaryType.getCode(), tenant, channel, domain);
        
        switch (dictionaryType) {
            case NORMALIZATION_RULES:
                return new HashMap<>(DEFAULT_NORMALIZATION_RULES);
            case PHONETIC_CORRECTIONS:
                return new HashMap<>(DEFAULT_PHONETIC_CORRECTIONS);
            case PREFIX_WORDS:
                return new HashSet<>(DEFAULT_PREFIX_WORDS);
            case SYNONYM_SETS:
                return createDeepCopySynonymSets(DEFAULT_SYNONYM_SETS);
            case STOP_WORDS:
                return new HashSet<>(DEFAULT_STOP_WORDS);
            case DOMAIN_TERMS:
                return new HashMap<>(DEFAULT_DOMAIN_TERMS);
            case ABBREVIATION_EXPANSIONS:
                return new HashMap<>(DEFAULT_ABBREVIATION_EXPANSIONS);
            case ENTITY_RECOGNITION:
                // 实体识别暂时返回空映射
                return new HashMap<String, Set<String>>();
            default:
                log.warn("未知的字典类型，返回空数据: {}", dictionaryType);
                return Collections.emptyMap();
        }
    }
    
    /**
     * 检查字典类型是否有回退数据
     * 
     * @param dictionaryType 字典类型
     * @return 是否有回退数据
     */
    public boolean hasFallbackData(DictionaryType dictionaryType) {
        switch (dictionaryType) {
            case NORMALIZATION_RULES:
                return !DEFAULT_NORMALIZATION_RULES.isEmpty();
            case PHONETIC_CORRECTIONS:
                return !DEFAULT_PHONETIC_CORRECTIONS.isEmpty();
            case PREFIX_WORDS:
                return !DEFAULT_PREFIX_WORDS.isEmpty();
            case SYNONYM_SETS:
                return !DEFAULT_SYNONYM_SETS.isEmpty();
            case STOP_WORDS:
                return !DEFAULT_STOP_WORDS.isEmpty();
            case DOMAIN_TERMS:
                return !DEFAULT_DOMAIN_TERMS.isEmpty();
            case ABBREVIATION_EXPANSIONS:
                return !DEFAULT_ABBREVIATION_EXPANSIONS.isEmpty();
            case ENTITY_RECOGNITION:
                return false; // 暂无默认数据
            default:
                return false;
        }
    }
    
    /**
     * 获取所有支持回退的字典类型
     * 
     * @return 支持回退的字典类型列表
     */
    public List<DictionaryType> getSupportedFallbackTypes() {
        return Arrays.asList(
            DictionaryType.NORMALIZATION_RULES,
            DictionaryType.PHONETIC_CORRECTIONS,
            DictionaryType.PREFIX_WORDS,
            DictionaryType.SYNONYM_SETS,
            DictionaryType.STOP_WORDS,
            DictionaryType.DOMAIN_TERMS,
            DictionaryType.ABBREVIATION_EXPANSIONS
        );
    }
    
    /**
     * 创建标准化规则数据
     */
    private static Map<String, String> createNormalizationRules() {
        Map<String, String> rules = new HashMap<>();
        
        // 常见错别字纠正
        rules.put("因该", "应该");
        rules.put("做为", "作为");
        rules.put("既然", "既然");
        rules.put("已经", "已经");
        rules.put("通过", "通过");
        rules.put("制做", "制作");
        rules.put("包括", "包括");
        rules.put("决对", "绝对");
        rules.put("帐户", "账户");
        rules.put("登陆", "登录");
        
        // 全角/半角标准化
        rules.put("？", "?");
        rules.put("！", "!");
        rules.put("（", "(");
        rules.put("）", ")");
        rules.put("，", ",");
        rules.put("。", ".");
        rules.put("；", ";");
        rules.put("：", ":");
        
        // 繁体转简体常见字
        rules.put("繁體", "繁体");
        rules.put("簡體", "简体");
        rules.put("設定", "设定");
        rules.put("設置", "设置");
        rules.put("確認", "确认");
        
        return rules;
    }
    
    /**
     * 创建拼音纠错数据
     */
    private static Map<String, String> createPhoneticCorrections() {
        Map<String, String> corrections = new HashMap<>();
        
        // 常见拼音错拼纠正
        corrections.put("朱丽业", "朱丽叶");
        corrections.put("罗密欧", "罗密欧");
        corrections.put("哈姆雷特", "哈姆雷特");
        corrections.put("莎士比亚", "莎士比亚");
        corrections.put("牛顿", "牛顿");
        corrections.put("爱因斯坦", "爱因斯坦");
        corrections.put("达芬奇", "达芬奇");
        
        // 技术词汇拼音纠错
        corrections.put("java", "Java");
        corrections.put("python", "Python");
        corrections.put("javascript", "JavaScript");
        corrections.put("mysql", "MySQL");
        corrections.put("redis", "Redis");
        corrections.put("docker", "Docker");
        corrections.put("kubernetes", "Kubernetes");
        
        return corrections;
    }
    
    /**
     * 创建前缀词汇数据
     */
    private static Set<String> createPrefixWords() {
        Set<String> prefixWords = new HashSet<>();
        
        // 中文常用查询前缀
        prefixWords.add("如何");
        prefixWords.add("怎么");
        prefixWords.add("什么");
        prefixWords.add("为什么");
        prefixWords.add("哪里");
        prefixWords.add("哪个");
        prefixWords.add("谁");
        prefixWords.add("何时");
        prefixWords.add("多少");
        prefixWords.add("多长");
        prefixWords.add("多久");
        
        // 英文常用查询前缀
        prefixWords.add("how");
        prefixWords.add("what");
        prefixWords.add("where");
        prefixWords.add("when");
        prefixWords.add("why");
        prefixWords.add("who");
        prefixWords.add("which");
        prefixWords.add("can");
        prefixWords.add("could");
        prefixWords.add("should");
        prefixWords.add("would");
        prefixWords.add("may");
        prefixWords.add("might");
        
        // 疑问副词
        prefixWords.add("是否");
        prefixWords.add("能否");
        prefixWords.add("可否");
        prefixWords.add("whether");
        prefixWords.add("if");
        
        return prefixWords;
    }
    
    /**
     * 创建同义词集合数据
     */
    private static Map<String, Set<String>> createSynonymSets() {
        Map<String, Set<String>> synonymSets = new HashMap<>();
        
        // 问题相关同义词
        synonymSets.put("问题", Set.of("问题", "疑问", "困惑", "难题", "issue", "problem", "question"));
        synonymSets.put("方法", Set.of("方法", "方式", "途径", "办法", "手段", "方案", "method", "way", "approach"));
        synonymSets.put("解决", Set.of("解决", "处理", "解决方案", "fix", "solve", "resolve"));
        
        // 技术相关同义词
        synonymSets.put("配置", Set.of("配置", "设置", "设定", "config", "configuration", "setting"));
        synonymSets.put("部署", Set.of("部署", "发布", "上线", "deploy", "deployment", "release"));
        synonymSets.put("监控", Set.of("监控", "监测", "观察", "monitor", "monitoring", "observe"));
        
        // 业务相关同义词
        synonymSets.put("用户", Set.of("用户", "客户", "使用者", "user", "customer", "client"));
        synonymSets.put("账户", Set.of("账户", "账号", "帐户", "帐号", "account"));
        synonymSets.put("订单", Set.of("订单", "工单", "单据", "order"));
        
        // 状态相关同义词
        synonymSets.put("成功", Set.of("成功", "完成", "成功了", "success", "successful", "complete"));
        synonymSets.put("失败", Set.of("失败", "错误", "异常", "fail", "failure", "error"));
        synonymSets.put("运行", Set.of("运行", "执行", "启动", "run", "execute", "start"));
        
        return synonymSets;
    }
    
    /**
     * 创建停用词数据
     */
    private static Set<String> createStopWords() {
        Set<String> stopWords = new HashSet<>();
        
        // 中文停用词
        stopWords.addAll(Arrays.asList(
            "的", "了", "在", "是", "我", "有", "和", "就", "不", "人", "都", "一", 
            "上", "也", "很", "到", "说", "要", "去", "你", "会", "着", "没有", 
            "看", "好", "自己", "这", "那", "么", "于", "把", "或", "给"
        ));
        
        // 英文停用词
        stopWords.addAll(Arrays.asList(
            "a", "an", "and", "are", "as", "at", "be", "by", "for", "from",
            "has", "he", "in", "is", "it", "its", "of", "on", "that", "the",
            "to", "was", "with", "will", "would", "can", "could", "should"
        ));
        
        return stopWords;
    }
    
    /**
     * 创建领域术语数据
     */
    private static Map<String, String> createDomainTerms() {
        Map<String, String> domainTerms = new HashMap<>();
        
        // 技术领域术语标准化
        domainTerms.put("后端", "后端");
        domainTerms.put("前端", "前端");
        domainTerms.put("全栈", "全栈");
        domainTerms.put("微服务", "微服务");
        domainTerms.put("容器化", "容器化");
        domainTerms.put("持续集成", "持续集成");
        domainTerms.put("持续部署", "持续部署");
        
        // 业务领域术语标准化
        domainTerms.put("客服", "客户服务");
        domainTerms.put("运营", "运营");
        domainTerms.put("产品", "产品");
        domainTerms.put("市场", "市场");
        
        return domainTerms;
    }
    
    /**
     * 创建缩写扩展数据
     */
    private static Map<String, String> createAbbreviationExpansions() {
        Map<String, String> abbreviations = new HashMap<>();
        
        // 技术缩写扩展
        abbreviations.put("API", "Application Programming Interface");
        abbreviations.put("REST", "Representational State Transfer");
        abbreviations.put("JSON", "JavaScript Object Notation");
        abbreviations.put("XML", "Extensible Markup Language");
        abbreviations.put("HTTP", "HyperText Transfer Protocol");
        abbreviations.put("HTTPS", "HyperText Transfer Protocol Secure");
        abbreviations.put("SQL", "Structured Query Language");
        abbreviations.put("NoSQL", "Not Only SQL");
        abbreviations.put("ORM", "Object-Relational Mapping");
        abbreviations.put("MVC", "Model-View-Controller");
        abbreviations.put("MVP", "Model-View-Presenter");
        abbreviations.put("MVVM", "Model-View-ViewModel");
        abbreviations.put("DDD", "Domain-Driven Design");
        abbreviations.put("TDD", "Test-Driven Development");
        abbreviations.put("CI/CD", "Continuous Integration/Continuous Deployment");
        
        // 业务缩写扩展
        abbreviations.put("CRM", "Customer Relationship Management");
        abbreviations.put("ERP", "Enterprise Resource Planning");
        abbreviations.put("SaaS", "Software as a Service");
        abbreviations.put("PaaS", "Platform as a Service");
        abbreviations.put("IaaS", "Infrastructure as a Service");
        
        return abbreviations;
    }
    
    /**
     * 创建同义词集合的深拷贝
     */
    private Map<String, Set<String>> createDeepCopySynonymSets(Map<String, Set<String>> original) {
        Map<String, Set<String>> copy = new HashMap<>();
        for (Map.Entry<String, Set<String>> entry : original.entrySet()) {
            copy.put(entry.getKey(), new HashSet<>(entry.getValue()));
        }
        return copy;
    }
}