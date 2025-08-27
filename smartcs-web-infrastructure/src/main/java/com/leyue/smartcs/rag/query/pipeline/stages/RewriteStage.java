package com.leyue.smartcs.rag.query.pipeline.stages;

import com.leyue.smartcs.api.DictionaryService;
import com.leyue.smartcs.rag.query.pipeline.QueryContext;
import com.leyue.smartcs.rag.query.pipeline.QueryTransformerStage;
import dev.langchain4j.rag.query.Query;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

/**
 * 可检索化改写阶段
 * 负责将自然语言查询改写为更适合检索的形式，包括：
 * 1. 语义改写 - 将口语化查询转换为面向检索的简洁指令
 * 2. 负向词抽取 - 识别"不要...""不包含..."等排除条件
 * 3. 关键词增强 - 提取必含词和加权词用于混合检索
 * 4. 查询结构化 - 将复杂查询分解为多个子查询
 * 5. 检索策略标记 - 为不同类型的查询添加检索提示
 * 
 * @author Claude
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RewriteStage implements QueryTransformerStage {
    
    private final DictionaryService dictionaryService;
    
    // 负向词模式
    private static final Pattern NEGATIVE_PATTERN = Pattern.compile(
        "(不要|不包含|不需要|除了|排除|忽略|跳过|避免|禁止|不能|不可以|不应该|别|勿)([^。，,]+)"
    );
    
    // 必含词模式
    private static final Pattern MUST_CONTAIN_PATTERN = Pattern.compile(
        "(必须|必要|一定要|务必|确保|需要|要求|包含|含有)([^。，,]+)"
    );
    
    // 口语化模式（需要改写的表达）
    private static final Map<Pattern, String> COLLOQUIAL_PATTERNS = createColloquialPatterns();
    
    // 关键词权重模式
    private static final Map<Pattern, Double> KEYWORD_WEIGHT_PATTERNS = createKeywordWeightPatterns();
    
    // 查询类型与检索策略映射
    private static final Map<String, String> QUERY_TYPE_SEARCH_HINTS = createSearchHints();
    
    // 停用词（用于关键词提取）
    private static final Set<String> STOP_WORDS = createStopWords();
    
    @Override
    public String getName() {
        return "RewriteStage";
    }
    
    @Override
    public boolean isEnabled(QueryContext context) {
        return context.getPipelineConfig() != null &&
               context.getAttribute("enableRewrite") != Boolean.FALSE;
    }
    
    @Override
    public Collection<Query> apply(QueryContext context, Collection<Query> queries) {
        if (queries == null || queries.isEmpty()) {
            log.debug("输入查询为空，跳过可检索化改写");
            return Collections.emptyList();
        }
        
        log.debug("开始可检索化改写处理: inputCount={}", queries.size());
        
        try {
            List<Query> rewrittenQueries = new ArrayList<>();
            
            for (Query query : queries) {
                try {
                    QueryRewriteResult result = rewriteQuery(context, query);
                    
                    // 生成主查询（改写后的查询）
                    rewrittenQueries.add(Query.from(result.getRewrittenText()));
                    
                    // 如果有子查询，也加入结果
                    for (String subQuery : result.getSubQueries()) {
                        rewrittenQueries.add(Query.from(subQuery));
                    }
                    
                    // 将改写信息存储到上下文
                    storeRewriteInfoInContext(context, query.text(), result);
                    
                } catch (Exception e) {
                    log.warn("单个查询可检索化改写失败，保留原查询: query={}", query.text(), e);
                    rewrittenQueries.add(query);
                }
            }
            
            // 去重并排序
            rewrittenQueries = deduplicateAndSort(rewrittenQueries);
            
            log.debug("可检索化改写处理完成: inputCount={}, outputCount={}", 
                    queries.size(), rewrittenQueries.size());
            
            return rewrittenQueries;
            
        } catch (Exception e) {
            log.error("可检索化改写处理失败: inputCount={}", queries.size(), e);
            // 发生错误时返回原始查询，保证系统稳定性
            log.warn("改写阶段异常，返回原始查询: {}", e.getMessage());
            return queries;
        }
    }
    
    /**
     * 改写单个查询
     */
    private QueryRewriteResult rewriteQuery(QueryContext context, Query query) {
        String originalText = query.text();
        
        // 1. 提取负向条件
        List<String> negativeConditions = extractNegativeConditions(originalText);
        String textWithoutNegatives = removeNegativeConditions(originalText);
        
        // 2. 提取必含词
        List<String> mustContainTerms = extractMustContainTerms(textWithoutNegatives);
        
        // 3. 语义改写（口语转书面语，集成字典数据）
        String rewrittenText = applySemanticRewriteWithDictionary(context, textWithoutNegatives);
        
        // 4. 关键词增强
        List<WeightedKeyword> keywords = extractWeightedKeywords(rewrittenText);
        
        // 5. 查询分解
        List<String> subQueries = decomposeQuery(rewrittenText);
        
        // 6. 添加检索策略提示
        String searchHint = determineSearchHint(context, originalText);
        if (searchHint != null) {
            rewrittenText = searchHint + " " + rewrittenText;
        }
        
        // 7. 最终优化
        rewrittenText = finalizeRewrite(rewrittenText, keywords);
        
        return QueryRewriteResult.builder()
                .originalText(originalText)
                .rewrittenText(rewrittenText)
                .negativeConditions(negativeConditions)
                .mustContainTerms(mustContainTerms)
                .keywords(keywords)
                .subQueries(subQueries)
                .searchHint(searchHint)
                .rewriteReason(generateRewriteReason(originalText, rewrittenText))
                .build();
    }
    
    /**
     * 提取负向条件
     */
    private List<String> extractNegativeConditions(String text) {
        List<String> negativeConditions = new ArrayList<>();
        Matcher matcher = NEGATIVE_PATTERN.matcher(text);
        
        while (matcher.find()) {
            String condition = matcher.group(2).trim();
            if (!condition.isEmpty() && condition.length() > 1) {
                negativeConditions.add(condition);
            }
        }
        
        return negativeConditions;
    }
    
    /**
     * 移除负向条件（为了不影响主查询）
     */
    private String removeNegativeConditions(String text) {
        String result = NEGATIVE_PATTERN.matcher(text).replaceAll("");
        return result.replaceAll("\\s+", " ").trim();
    }
    
    /**
     * 提取必含词
     */
    private List<String> extractMustContainTerms(String text) {
        List<String> mustContainTerms = new ArrayList<>();
        Matcher matcher = MUST_CONTAIN_PATTERN.matcher(text);
        
        while (matcher.find()) {
            String terms = matcher.group(2).trim();
            if (!terms.isEmpty()) {
                // 进一步分词
                String[] words = terms.split("[\\s\\p{Punct}]+");
                for (String word : words) {
                    word = word.trim();
                    if (!word.isEmpty() && !STOP_WORDS.contains(word.toLowerCase())) {
                        mustContainTerms.add(word);
                    }
                }
            }
        }
        
        return mustContainTerms;
    }
    
    /**
     * 语义改写（口语转书面语，集成字典数据）
     */
    private String applySemanticRewriteWithDictionary(QueryContext context, String text) {
        String rewritten = text;
        
        try {
            // 获取domain配置
            String domain = context.getAttribute("domain");
            if (domain == null) {
                domain = "default";
            }
            
            // 1. 从字典获取改写规则
            Map<String, String> rewriteRules = dictionaryService.getRewriteRules(
                context.getTenant(), context.getChannel(), domain, context.getLocale()
            );
            
            // 2. 应用字典改写规则（优先级最高）
            if (!rewriteRules.isEmpty()) {
                // 按长度倒序排列，优先匹配长表达式
                List<String> sortedKeys = rewriteRules.keySet().stream()
                        .sorted((a, b) -> Integer.compare(b.length(), a.length()))
                        .collect(Collectors.toList());
                
                for (String original : sortedKeys) {
                    String rewriteTarget = rewriteRules.get(original);
                    if (rewritten.toLowerCase().contains(original.toLowerCase())) {
                        rewritten = rewritten.replaceAll("(?i)" + Pattern.quote(original), rewriteTarget);
                        log.debug("字典改写: {} -> {}", original, rewriteTarget);
                    }
                }
            }
            
        } catch (Exception e) {
            log.warn("字典改写失败，使用内置规则: {}", e.getMessage());
        }
        
        // 3. 回退到内置改写规则
        rewritten = applySemanticRewrite(rewritten);
        
        return rewritten;
    }
    
    /**
     * 应用语义改写
     */
    private String applySemanticRewrite(String text) {
        String rewritten = text;
        
        // 应用口语化改写规则
        for (Map.Entry<Pattern, String> entry : COLLOQUIAL_PATTERNS.entrySet()) {
            Pattern pattern = entry.getKey();
            String replacement = entry.getValue();
            rewritten = pattern.matcher(rewritten).replaceAll(replacement);
        }
        
        // 去除冗余语气词
        rewritten = removeRedundantExpressions(rewritten);
        
        // 标准化技术术语
        rewritten = standardizeTechnicalTerms(rewritten);
        
        return rewritten.trim();
    }
    
    /**
     * 去除冗余表达
     */
    private String removeRedundantExpressions(String text) {
        // 去除语气词和填充词
        String[] redundantPhrases = {
            "我想问", "请问", "能否", "可以吗", "好吗", "如何是好", "怎么办",
            "谢谢", "麻烦", "辛苦", "拜托", "劳烦",
            "嗯", "哦", "啊", "呢", "吧", "呀"
        };
        
        String result = text;
        for (String phrase : redundantPhrases) {
            result = result.replace(phrase, "");
        }
        
        return result.replaceAll("\\s+", " ").trim();
    }
    
    /**
     * 标准化技术术语
     */
    private String standardizeTechnicalTerms(String text) {
        Map<String, String> termMappings = new HashMap<>();
        
        // AI/ML领域
        termMappings.put("人工智能", "AI");
        termMappings.put("机器学习", "ML");
        termMappings.put("深度学习", "DL");
        termMappings.put("神经网络", "neural network");
        
        // 技术概念
        termMappings.put("应用程序编程接口", "API");
        termMappings.put("数据库", "database");
        termMappings.put("用户界面", "UI");
        termMappings.put("用户体验", "UX");
        
        String result = text;
        for (Map.Entry<String, String> entry : termMappings.entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue());
        }
        
        return result;
    }
    
    /**
     * 提取加权关键词
     */
    private List<WeightedKeyword> extractWeightedKeywords(String text) {
        List<WeightedKeyword> keywords = new ArrayList<>();
        Map<String, Double> keywordScores = new HashMap<>();
        
        // 基于模式匹配的权重计算
        for (Map.Entry<Pattern, Double> entry : KEYWORD_WEIGHT_PATTERNS.entrySet()) {
            Pattern pattern = entry.getKey();
            Double weight = entry.getValue();
            
            Matcher matcher = pattern.matcher(text);
            while (matcher.find()) {
                String keyword = matcher.group(1).trim();
                if (!keyword.isEmpty() && !STOP_WORDS.contains(keyword.toLowerCase())) {
                    keywordScores.merge(keyword, weight, Double::max);
                }
            }
        }
        
        // 基于TF-IDF的基础权重计算
        String[] words = text.split("[\\s\\p{Punct}]+");
        Map<String, Integer> termFreq = new HashMap<>();
        
        for (String word : words) {
            word = word.trim().toLowerCase();
            if (!word.isEmpty() && !STOP_WORDS.contains(word) && word.length() > 1) {
                termFreq.merge(word, 1, Integer::sum);
            }
        }
        
        // 计算最终权重
        int totalWords = words.length;
        for (Map.Entry<String, Integer> entry : termFreq.entrySet()) {
            String term = entry.getKey();
            int freq = entry.getValue();
            double tf = (double) freq / totalWords;
            
            // 简化的权重计算（实际项目中可以使用更复杂的IDF计算）
            double baseWeight = tf * (1 + Math.log(freq));
            double finalWeight = keywordScores.getOrDefault(term, baseWeight);
            
            keywords.add(WeightedKeyword.builder()
                    .keyword(term)
                    .weight(finalWeight)
                    .frequency(freq)
                    .build());
        }
        
        // 按权重排序并限制数量
        return keywords.stream()
                .sorted((a, b) -> Double.compare(b.getWeight(), a.getWeight()))
                .limit(20)
                .collect(Collectors.toList());
    }
    
    /**
     * 查询分解
     */
    private List<String> decomposeQuery(String text) {
        List<String> subQueries = new ArrayList<>();
        
        // 基于连接词分解复合查询
        String[] conjunctions = {"和", "以及", "还有", "另外", "同时", "并且", "而且"};
        
        for (String conjunction : conjunctions) {
            if (text.contains(conjunction)) {
                String[] parts = text.split(conjunction);
                for (String part : parts) {
                    part = part.trim();
                    if (!part.isEmpty() && part.length() > 3) {
                        subQueries.add(part);
                    }
                }
                break; // 只使用第一个找到的连接词
            }
        }
        
        // 基于问号分解多问句
        if (text.contains("？") || text.contains("?")) {
            String[] questions = text.split("[？?]");
            for (String question : questions) {
                question = question.trim();
                if (!question.isEmpty() && question.length() > 3) {
                    subQueries.add(question + "?");
                }
            }
        }
        
        return subQueries;
    }
    
    /**
     * 确定检索策略提示
     */
    private String determineSearchHint(QueryContext context, String text) {
        // 从上下文中获取意图信息
        String intentKey = "intent_" + text.hashCode();
        Object intentObj = context.getAttribute(intentKey);
        
        if (intentObj instanceof IntentExtractionStage.QueryIntent) {
            IntentExtractionStage.QueryIntent intent = (IntentExtractionStage.QueryIntent) intentObj;
            String queryType = intent.getQueryType().toString();
            return QUERY_TYPE_SEARCH_HINTS.get(queryType);
        }
        
        // 基于文本模式的简单判断
        if (text.contains("对比") || text.contains("比较")) {
            return "[COMPARE]";
        } else if (text.contains("故障") || text.contains("问题")) {
            return "[TROUBLESHOOT]";
        } else if (text.contains("如何") || text.contains("怎么")) {
            return "[HOWTO]";
        }
        
        return null;
    }
    
    /**
     * 最终化改写结果
     */
    private String finalizeRewrite(String text, List<WeightedKeyword> keywords) {
        // 如果改写后的文本过短，添加高权重关键词
        if (text.length() < 10 && !keywords.isEmpty()) {
            List<String> topKeywords = keywords.stream()
                    .limit(3)
                    .map(WeightedKeyword::getKeyword)
                    .collect(Collectors.toList());
            text = String.join(" ", topKeywords) + " " + text;
        }
        
        // 清理多余空格
        return text.replaceAll("\\s+", " ").trim();
    }
    
    /**
     * 生成改写原因说明
     */
    private String generateRewriteReason(String original, String rewritten) {
        List<String> reasons = new ArrayList<>();
        
        if (!original.equals(rewritten)) {
            if (original.length() > rewritten.length()) {
                reasons.add("简化表达");
            }
            if (NEGATIVE_PATTERN.matcher(original).find()) {
                reasons.add("提取排除条件");
            }
            if (MUST_CONTAIN_PATTERN.matcher(original).find()) {
                reasons.add("标识必含词");
            }
            if (original.contains("？") || original.contains("?")) {
                reasons.add("优化疑问句");
            }
        }
        
        return reasons.isEmpty() ? "标准化处理" : String.join(", ", reasons);
    }
    
    /**
     * 去重并排序
     */
    private List<Query> deduplicateAndSort(List<Query> queries) {
        Map<String, Query> uniqueQueries = new LinkedHashMap<>();
        
        for (Query query : queries) {
            String normalized = query.text().toLowerCase().trim();
            if (!uniqueQueries.containsKey(normalized) && normalized.length() > 2) {
                uniqueQueries.put(normalized, query);
            }
        }
        
        return new ArrayList<>(uniqueQueries.values());
    }
    
    /**
     * 将改写信息存储到上下文
     */
    private void storeRewriteInfoInContext(QueryContext context, String originalQuery, QueryRewriteResult result) {
        String key = "rewrite_" + originalQuery.hashCode();
        context.setAttribute(key, result);
        
        // 统计改写效果
        @SuppressWarnings("unchecked")
        Map<String, Integer> rewriteStats = (Map<String, Integer>) context.getAttribute("rewriteStats");
        if (rewriteStats == null) {
            rewriteStats = new HashMap<>();
            context.setAttribute("rewriteStats", rewriteStats);
        }
        
        rewriteStats.merge("total", 1, Integer::sum);
        if (!result.getNegativeConditions().isEmpty()) {
            rewriteStats.merge("withNegatives", 1, Integer::sum);
        }
        if (!result.getSubQueries().isEmpty()) {
            rewriteStats.merge("decomposed", 1, Integer::sum);
        }
        if (result.getSearchHint() != null) {
            rewriteStats.merge("withHints", 1, Integer::sum);
        }
    }
    
    // 静态初始化方法
    
    private static Map<Pattern, String> createColloquialPatterns() {
        Map<Pattern, String> patterns = new HashMap<>();
        
        // 口语化改写模式
        patterns.put(Pattern.compile("我想知道(.+)"), "$1");
        patterns.put(Pattern.compile("能告诉我(.+)吗"), "$1");
        patterns.put(Pattern.compile("请问(.+)"), "$1");
        patterns.put(Pattern.compile("我想了解(.+)"), "$1");
        patterns.put(Pattern.compile("帮我查一下(.+)"), "$1");
        patterns.put(Pattern.compile("(.+)怎么样"), "$1");
        patterns.put(Pattern.compile("(.+)如何"), "如何 $1");
        patterns.put(Pattern.compile("(.+)的方法"), "$1 方法");
        patterns.put(Pattern.compile("有没有(.+)"), "$1");
        patterns.put(Pattern.compile("能否(.+)"), "$1");
        
        return patterns;
    }
    
    private static Map<Pattern, Double> createKeywordWeightPatterns() {
        Map<Pattern, Double> patterns = new HashMap<>();
        
        // 高权重关键词模式
        patterns.put(Pattern.compile("\"([^\"]+)\""), 2.0);  // 引号内的词
        patterns.put(Pattern.compile("\\*([^\\*]+)\\*"), 1.8);  // 星号强调
        patterns.put(Pattern.compile("【([^】]+)】"), 1.5);  // 中括号
        patterns.put(Pattern.compile("([A-Z][a-z]+(?:[A-Z][a-z]+)+)"), 1.4);  // 驼峰命名
        patterns.put(Pattern.compile("([a-zA-Z]+\\d+(?:\\.\\d+)*)"), 1.3);  // 版本号
        patterns.put(Pattern.compile("([\\u4e00-\\u9fa5]{2,}设备|[\\u4e00-\\u9fa5]{2,}系统)"), 1.2);  // 设备/系统名
        
        return patterns;
    }
    
    private static Map<String, String> createSearchHints() {
        Map<String, String> hints = new HashMap<>();
        
        hints.put("QUESTION_ANSWER", "[QA]");
        hints.put("FEATURE_COMPARISON", "[COMPARE]");
        hints.put("SUMMARY_REPORT", "[SUMMARY]");
        hints.put("TROUBLESHOOTING", "[TROUBLESHOOT]");
        hints.put("MANUAL_LOOKUP", "[MANUAL]");
        hints.put("CODE_EXAMPLE", "[CODE]");
        
        return hints;
    }
    
    private static Set<String> createStopWords() {
        Set<String> stopWords = new HashSet<>();
        
        // 中文停用词
        stopWords.addAll(Arrays.asList(
            "的", "了", "在", "是", "我", "有", "和", "就", "不", "人", "都", "一", "个", "上", "也", "很",
            "到", "说", "要", "去", "你", "会", "着", "没有", "看", "好", "自己", "这", "那", "们",
            "什么", "怎么", "为什么", "哪里", "谁", "如何", "多少", "怎样"
        ));
        
        // 英文停用词
        stopWords.addAll(Arrays.asList(
            "the", "a", "an", "and", "or", "but", "in", "on", "at", "to", "for", "of", "with", "by",
            "is", "are", "was", "were", "be", "been", "being", "have", "has", "had",
            "what", "where", "when", "why", "how", "who", "which"
        ));
        
        return stopWords;
    }
    
    // 内部数据结构
    
    /**
     * 加权关键词
     */
    @Data
    @lombok.Builder
    public static class WeightedKeyword {
        private String keyword;
        private double weight;
        private int frequency;
    }
    
    /**
     * 查询改写结果
     */
    @Data
    @lombok.Builder
    public static class QueryRewriteResult {
        private String originalText;
        private String rewrittenText;
        private List<String> negativeConditions;
        private List<String> mustContainTerms;
        private List<WeightedKeyword> keywords;
        private List<String> subQueries;
        private String searchHint;
        private String rewriteReason;
    }
    
    @Override
    public void initialize(QueryContext context) {
        log.debug("初始化可检索化改写阶段");
        context.setAttribute("rewriteStats", new HashMap<String, Integer>());
    }
    
    @Override
    public void cleanup(QueryContext context) {
        // 打印改写统计
        @SuppressWarnings("unchecked")
        Map<String, Integer> rewriteStats = (Map<String, Integer>) context.getAttribute("rewriteStats");
        if (rewriteStats != null && !rewriteStats.isEmpty()) {
            log.info("本次查询改写统计: {}", rewriteStats);
        }
    }
}
