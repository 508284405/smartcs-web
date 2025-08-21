package com.leyue.smartcs.rag.query.pipeline.stages;

import com.leyue.smartcs.rag.query.pipeline.QueryContext;
import com.leyue.smartcs.rag.query.pipeline.QueryTransformerStage;
import com.leyue.smartcs.rag.query.pipeline.QueryTransformationException;
import dev.langchain4j.rag.query.Query;
import lombok.extern.slf4j.Slf4j;

import java.text.Normalizer;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

/**
 * 增强查询标准化阶段
 * 负责对输入查询进行清洗和标准化处理，包括：
 * - 去除多余空白字符和控制字符
 * - 清理编号和标点符号
 * - 大小写标准化
 * - 全角/半角字符统一
 * - 繁体/简体中文统一
 * - 轻量拼写纠错
 * - 语言识别和处理
 * - 长度限制
 * - 可选的停用词处理
 * 
 * @author Claude
 */
@Slf4j
public class NormalizationStage implements QueryTransformerStage {
    
    /**
     * 编号清理模式
     */
    private static final Pattern NUMBERING_PATTERN = Pattern.compile("^\\d+[.)\\s]+");
    
    /**
     * 多余空白模式（包括各种空白字符）
     */
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("[\\s\\u00A0\\u2000-\\u200B\\u2028\\u2029\\u3000]+");
    
    /**
     * 控制字符清理模式
     */
    private static final Pattern CONTROL_CHARS_PATTERN = Pattern.compile("[\\p{Cntrl}&&[^\\r\\n\\t]]");
    
    /**
     * 全角转半角映射
     */
    private static final Map<Character, Character> FULLWIDTH_TO_HALFWIDTH = createFullwidthMapping();
    
    /**
     * 常见拼写错误映射（中英文）
     */
    private static final Map<String, String> SPELLING_CORRECTIONS = createSpellingCorrections();
    
    /**
     * 重复标点模式
     */
    private static final Pattern REPEATED_PUNCTUATION = Pattern.compile("([\\p{Punct}])\\1{2,}");
    
    /**
     * 标点符号清理模式（保留基本标点）
     */
    private static final Pattern PUNCTUATION_PATTERN = Pattern.compile("[\\p{Punct}&&[^\\-.,!?:;\"'()]]+");
    
    /**
     * 扩展中文停用词集合
     */
    private static final Set<String> STOP_WORDS = createStopWords();
    
    /**
     * 中文标点符号集合
     */
    private static final Set<Character> CHINESE_PUNCTUATION = Set.of(
        '，', '。', '；', '：', '？', '！', '、', '“', '”', '‘', '’', '（', '）', '【', '】', '《', '》'
    );
    
    @Override
    public String getName() {
        return "NormalizationStage";
    }
    
    @Override
    public boolean isEnabled(QueryContext context) {
        return context.getPipelineConfig().isEnableNormalization();
    }
    
    @Override
    public Collection<Query> apply(QueryContext context, Collection<Query> queries) {
        if (queries == null || queries.isEmpty()) {
            log.debug("输入查询为空，跳过标准化处理");
            return Collections.emptyList();
        }
        
        log.debug("开始查询标准化处理: inputCount={}", queries.size());
        
        try {
            QueryContext.NormalizationConfig config = getNormalizationConfig(context);
            List<Query> normalizedQueries = new ArrayList<>();
            
            for (Query query : queries) {
                try {
                    Query normalizedQuery = normalizeQuery(query, config);
                    if (normalizedQuery != null && !isQueryEmpty(normalizedQuery.text())) {
                        normalizedQueries.add(normalizedQuery);
                    }
                } catch (Exception e) {
                    log.warn("单个查询标准化失败，跳过: query={}", query.text(), e);
                    // 失败时保留原始查询
                    normalizedQueries.add(query);
                }
            }
            
            // 去重处理
            normalizedQueries = removeDuplicates(normalizedQueries);
            
            log.debug("查询标准化处理完成: inputCount={}, outputCount={}", 
                    queries.size(), normalizedQueries.size());
            
            return normalizedQueries;
            
        } catch (Exception e) {
            log.error("查询标准化处理失败: inputCount={}", queries.size(), e);
            throw new QueryTransformationException(getName(), "查询标准化处理失败", e, true);
        }
    }
    
    /**
     * 获取标准化配置
     */
    private QueryContext.NormalizationConfig getNormalizationConfig(QueryContext context) {
        QueryContext.NormalizationConfig config = context.getPipelineConfig().getNormalizationConfig();
        
        // 如果没有配置，使用默认配置
        if (config == null) {
            config = QueryContext.NormalizationConfig.builder().build();
        }
        
        return config;
    }
    
    /**
     * 标准化单个查询（增强版）
     */
    private Query normalizeQuery(Query query, QueryContext.NormalizationConfig config) {
        String text = query.text();
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        
        // 0. 检测并记录语言类型
        LanguageType languageType = detectLanguage(text);
        
        // 1. 去除控制字符（保留基本换行和制表符）
        text = CONTROL_CHARS_PATTERN.matcher(text).replaceAll("");
        
        // 2. 去除编号前缀
        text = NUMBERING_PATTERN.matcher(text).replaceFirst("");
        
        // 3. 全角/半角字符统一
        text = normalizeFullwidthChars(text);
        
        // 4. 繁体/简体中文统一（转为简体）
        if (languageType == LanguageType.CHINESE || languageType == LanguageType.MIXED) {
            text = traditionalToSimplified(text);
        }
        
        // 5. Unicode标准化（NFC形式）
        text = Normalizer.normalize(text, Normalizer.Form.NFC);
        
        // 6. 清理重复标点符号
        text = REPEATED_PUNCTUATION.matcher(text).replaceAll("$1");
        
        // 7. 清理多余空白（包括各种Unicode空白字符）
        if (config.isCleanWhitespace()) {
            text = WHITESPACE_PATTERN.matcher(text.trim()).replaceAll(" ");
        }
        
        // 8. 大小写标准化（智能处理）
        if (config.isNormalizeCase()) {
            text = normalizeCaseIntelligently(text, languageType);
        }
        
        // 9. 轻量拼写纠错
        text = applySpellingCorrections(text);
        
        // 10. 清理部分标点符号（保留常用标点和中文标点）
        text = cleanPunctuation(text, languageType);
        
        // 11. 应用长度限制
        if (text.length() > config.getMaxQueryLength()) {
            text = truncateIntelligently(text, config.getMaxQueryLength());
        }
        
        // 12. 可选的停用词处理
        if (config.isRemoveStopwords()) {
            text = removeStopwords(text, languageType);
        }
        
        // 13. 最终清理
        text = text.trim();
        
        return isQueryEmpty(text) ? null : Query.from(text);
    }
    
    /**
     * 检查文本是否包含中文字符
     */
    private boolean containsChinese(String text) {
        return text.chars().anyMatch(c -> 
            Character.UnicodeScript.of(c) == Character.UnicodeScript.HAN);
    }
    
    /**
     * 语言类型枚举
     */
    private enum LanguageType {
        CHINESE, ENGLISH, MIXED, UNKNOWN
    }
    
    /**
     * 检测文本语言类型
     */
    private LanguageType detectLanguage(String text) {
        if (text == null || text.trim().isEmpty()) {
            return LanguageType.UNKNOWN;
        }
        
        int chineseCount = 0;
        int englishCount = 0;
        int totalChars = 0;
        
        for (char c : text.toCharArray()) {
            if (Character.isLetter(c)) {
                totalChars++;
                if (Character.UnicodeScript.of(c) == Character.UnicodeScript.HAN) {
                    chineseCount++;
                } else if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
                    englishCount++;
                }
            }
        }
        
        if (totalChars == 0) return LanguageType.UNKNOWN;
        
        double chineseRatio = (double) chineseCount / totalChars;
        double englishRatio = (double) englishCount / totalChars;
        
        if (chineseRatio > 0.5) return LanguageType.CHINESE;
        if (englishRatio > 0.5) return LanguageType.ENGLISH;
        if (chineseRatio > 0.2 && englishRatio > 0.2) return LanguageType.MIXED;
        
        return LanguageType.UNKNOWN;
    }
    
    /**
     * 全角字符转半角
     */
    private String normalizeFullwidthChars(String text) {
        if (text == null) return null;
        
        StringBuilder result = new StringBuilder();
        for (char c : text.toCharArray()) {
            Character halfwidth = FULLWIDTH_TO_HALFWIDTH.get(c);
            result.append(halfwidth != null ? halfwidth : c);
        }
        return result.toString();
    }
    
    /**
     * 繁体转简体中文（基础实现）
     */
    private String traditionalToSimplified(String text) {
        // 这里使用一个基础的繁体转简体映射
        // 在实际项目中，建议使用专业的中文转换库如OpenCC
        return text
            .replace("資", "资").replace("與", "与").replace("個", "个")
            .replace("時", "时").replace("間", "间").replace("開", "开")
            .replace("關", "关").replace("來", "来").replace("說", "说")
            .replace("這", "这").replace("還", "还").replace("會", "会")
            .replace("現", "现").replace("門", "门").replace("業", "业")
            .replace("學", "学").replace("問", "问").replace("題", "题")
            .replace("師", "师").replace("員", "员").replace("計", "计")
            .replace("統", "统").replace("機", "机").replace("構", "构")
            .replace("議", "议").replace("網", "网").replace("電", "电")
            .replace("處", "处").replace("國", "国").replace("際", "际");
    }
    
    /**
     * 智能大小写标准化
     */
    private String normalizeCaseIntelligently(String text, LanguageType languageType) {
        switch (languageType) {
            case CHINESE:
                // 中文查询保持原样，但英文部分转小写
                return normalizeEnglishPartsToLowerCase(text);
            case ENGLISH:
                // 纯英文查询转小写，但保留专有名词
                return normalizeEnglishCase(text);
            case MIXED:
                // 混合语言，保持中文原样，英文部分智能处理
                return normalizeEnglishPartsToLowerCase(text);
            default:
                return text.toLowerCase();
        }
    }
    
    /**
     * 标准化文本中的英文部分
     */
    private String normalizeEnglishPartsToLowerCase(String text) {
        StringBuilder result = new StringBuilder();
        boolean inEnglishWord = false;
        StringBuilder englishWord = new StringBuilder();
        
        for (char c : text.toCharArray()) {
            if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z')) {
                inEnglishWord = true;
                englishWord.append(c);
            } else {
                if (inEnglishWord) {
                    // 检查是否为专有名词（首字母大写的单词保持原样）
                    String word = englishWord.toString();
                    if (isProperNoun(word)) {
                        result.append(word);
                    } else {
                        result.append(word.toLowerCase());
                    }
                    englishWord.setLength(0);
                    inEnglishWord = false;
                }
                result.append(c);
            }
        }
        
        // 处理最后的英文单词
        if (inEnglishWord) {
            String word = englishWord.toString();
            if (isProperNoun(word)) {
                result.append(word);
            } else {
                result.append(word.toLowerCase());
            }
        }
        
        return result.toString();
    }
    
    /**
     * 英文大小写智能处理
     */
    private String normalizeEnglishCase(String text) {
        String[] words = text.split("\\s+");
        StringBuilder result = new StringBuilder();
        
        for (int i = 0; i < words.length; i++) {
            if (i > 0) result.append(" ");
            
            String word = words[i].trim();
            if (isProperNoun(word) || isAcronym(word)) {
                result.append(word); // 保持专有名词和缩写原样
            } else {
                result.append(word.toLowerCase());
            }
        }
        
        return result.toString();
    }
    
    /**
     * 判断是否为专有名词
     */
    private boolean isProperNoun(String word) {
        if (word.length() < 2) return false;
        
        // 简单的专有名词检测：首字母大写，其余小写
        return Character.isUpperCase(word.charAt(0)) && 
               word.substring(1).equals(word.substring(1).toLowerCase()) &&
               word.length() > 2; // 避免误判单字母或缩写
    }
    
    /**
     * 判断是否为缩写词
     */
    private boolean isAcronym(String word) {
        if (word.length() < 2 || word.length() > 6) return false;
        
        // 检查是否全为大写字母
        for (char c : word.toCharArray()) {
            if (!Character.isUpperCase(c)) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * 应用拼写纠错
     */
    private String applySpellingCorrections(String text) {
        String result = text;
        
        // 按长度倒序应用纠错，避免重复替换
        List<String> sortedKeys = SPELLING_CORRECTIONS.keySet().stream()
                .sorted((a, b) -> Integer.compare(b.length(), a.length()))
                .collect(Collectors.toList());
        
        for (String error : sortedKeys) {
            String correction = SPELLING_CORRECTIONS.get(error);
            result = result.replaceAll("(?i)\\b" + Pattern.quote(error) + "\\b", correction);
        }
        
        return result;
    }
    
    /**
     * 清理标点符号（保留有意义的标点）
     */
    private String cleanPunctuation(String text, LanguageType languageType) {
        if (languageType == LanguageType.CHINESE || languageType == LanguageType.MIXED) {
            // 中文文本保留更多标点符号
            // 允许的标点：英文 - . , ! ? : ; " ' ( ) 以及常见中文标点
            return text.replaceAll("[\\p{Punct}&&[^\\-.,!?:;\"'()，。；：？！、“”‘’（）【】《》]]+", " ");
        } else {
            // 英文文本使用原有逻辑
            return PUNCTUATION_PATTERN.matcher(text).replaceAll(" ");
        }
    }
    
    /**
     * 智能截断文本
     */
    private String truncateIntelligently(String text, int maxLength) {
        if (text.length() <= maxLength) {
            return text;
        }
        
        // 尝试在单词边界截断
        String truncated = text.substring(0, maxLength);
        int lastSpace = truncated.lastIndexOf(' ');
        int lastChinese = findLastChineseCharBoundary(truncated);
        
        // 选择最佳截断位置
        int bestCutoff = Math.max(lastSpace, lastChinese);
        if (bestCutoff > maxLength * 0.8) { // 如果截断位置不会损失太多内容
            return text.substring(0, bestCutoff).trim();
        }
        
        return truncated.trim();
    }
    
    /**
     * 找到最后一个中文字符边界
     */
    private int findLastChineseCharBoundary(String text) {
        for (int i = text.length() - 1; i >= 0; i--) {
            char c = text.charAt(i);
            if (Character.UnicodeScript.of(c) == Character.UnicodeScript.HAN) {
                return i + 1; // 返回中文字符后的位置
            }
        }
        return -1;
    }
    
    /**
     * 移除停用词（增强版，支持多语言）
     */
    private String removeStopwords(String text, LanguageType languageType) {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }
        
        // 根据语言类型选择不同的分词策略
        String[] words;
        if (languageType == LanguageType.CHINESE || languageType == LanguageType.MIXED) {
            // 中文文本使用更精细的分割
            words = text.split("[\\s\\p{Punct}\\u3000]+");
        } else {
            // 英文文本使用标准分割
            words = text.split("[\\s\\p{Punct}]+");
        }
        
        List<String> filteredWords = Arrays.stream(words)
                .map(String::trim)
                .filter(word -> !word.isEmpty() && !STOP_WORDS.contains(word.toLowerCase()))
                .collect(Collectors.toList());
        
        // 如果过滤后为空，返回原文
        if (filteredWords.isEmpty()) {
            return text;
        }
        
        return String.join(" ", filteredWords);
    }
    
    /**
     * 移除停用词（兼容原方法）
     */
    private String removeStopwords(String text) {
        return removeStopwords(text, detectLanguage(text));
    }
    
    /**
     * 检查查询是否为空或无效
     */
    private boolean isQueryEmpty(String text) {
        return text == null || text.trim().isEmpty() || text.trim().length() < 2;
    }
    
    /**
     * 去重处理
     */
    private List<Query> removeDuplicates(List<Query> queries) {
        Set<String> seen = new HashSet<>();
        List<Query> uniqueQueries = new ArrayList<>();
        
        for (Query query : queries) {
            String normalizedText = query.text().trim().toLowerCase();
            if (seen.add(normalizedText)) {
                uniqueQueries.add(query);
            }
        }
        
        if (uniqueQueries.size() != queries.size()) {
            log.debug("去重处理: 原数量={}, 去重后={}", queries.size(), uniqueQueries.size());
        }
        
        return uniqueQueries;
    }
    
    @Override
    public void initialize(QueryContext context) {
        log.debug("初始化查询标准化阶段: config={}", context.getPipelineConfig().getNormalizationConfig());
    }
    
    @Override
    public void cleanup(QueryContext context) {
        // 无需清理资源
    }
    
    // 静态初始化方法
    
    /**
     * 创建全角转半角映射表
     */
    private static Map<Character, Character> createFullwidthMapping() {
        Map<Character, Character> mapping = new HashMap<>();
        
        // 全角数字转半角
        for (char c = '０'; c <= '９'; c++) {
            mapping.put(c, (char) (c - '０' + '0'));
        }
        
        // 全角大写字母转半角
        for (char c = 'Ａ'; c <= 'Ｚ'; c++) {
            mapping.put(c, (char) (c - 'Ａ' + 'A'));
        }
        
        // 全角小写字母转半角
        for (char c = 'ａ'; c <= 'ｚ'; c++) {
            mapping.put(c, (char) (c - 'ａ' + 'a'));
        }
        
        // 全角标点符号转半角
        mapping.put('　', ' ');  // 全角空格
        mapping.put('！', '!');
        mapping.put('＂', '"');
        mapping.put('＃', '#');
        mapping.put('＄', '$');
        mapping.put('％', '%');
        mapping.put('＆', '&');
        mapping.put('＇', '\'');
        mapping.put('（', '(');
        mapping.put('）', ')');
        mapping.put('＊', '*');
        mapping.put('＋', '+');
        mapping.put('，', ',');
        mapping.put('－', '-');
        mapping.put('．', '.');
        mapping.put('／', '/');
        mapping.put('：', ':');
        mapping.put('；', ';');
        mapping.put('＜', '<');
        mapping.put('＝', '=');
        mapping.put('＞', '>');
        mapping.put('？', '?');
        mapping.put('＠', '@');
        mapping.put('［', '[');
        mapping.put('＼', '\\');
        mapping.put('］', ']');
        mapping.put('＾', '^');
        mapping.put('＿', '_');
        mapping.put('｀', '`');
        mapping.put('｛', '{');
        mapping.put('｜', '|');
        mapping.put('｝', '}');
        mapping.put('～', '~');
        
        return mapping;
    }
    
    /**
     * 创建拼写纠错映射表
     */
    private static Map<String, String> createSpellingCorrections() {
        Map<String, String> corrections = new HashMap<>();
        
        // 常见英文拼写错误
        corrections.put("recieve", "receive");
        corrections.put("seperate", "separate");
        corrections.put("definately", "definitely");
        corrections.put("occured", "occurred");
        corrections.put("neccessary", "necessary");
        corrections.put("accomodate", "accommodate");
        corrections.put("beleive", "believe");
        corrections.put("begining", "beginning");
        corrections.put("existance", "existence");
        corrections.put("independant", "independent");
        
        // 常见中文输入错误
        corrections.put("因该", "应该");
        corrections.put("做为", "作为");
        corrections.put("既使", "即使");
        corrections.put("必需", "必须");
        corrections.put("以至", "以致");
        corrections.put("终于", "终於");
        
        // 技术术语纠错
        corrections.put("artifical", "artificial");
        corrections.put("intellegence", "intelligence");
        corrections.put("machiene", "machine");
        corrections.put("algoritm", "algorithm");
        corrections.put("databse", "database");
        corrections.put("framwork", "framework");
        corrections.put("libary", "library");
        corrections.put("inteface", "interface");
        
        return corrections;
    }
    
    /**
     * 创建扩展停用词集合
     */
    private static Set<String> createStopWords() {
        Set<String> stopWords = new HashSet<>();
        
        // 中文停用词（扩展版）
        stopWords.addAll(Arrays.asList(
            "的", "了", "在", "是", "我", "有", "和", "就", "不", "人", "都", "一", "一个", "上", "也", "很",
            "到", "说", "要", "去", "你", "会", "着", "没有", "看", "好", "自己", "这", "那", "个", "们", "这个",
            "那个", "什么", "怎么", "为什么", "哪里", "谁", "之", "与", "及", "等", "或者", "以及", "但是",
            "然后", "因为", "所以", "虽然", "如果", "那么", "还是", "已经", "可以", "应该", "需要", "能够",
            "由于", "关于", "对于", "根据", "通过", "按照", "只是", "只有", "还有", "而且", "不过", "但",
            "而", "再", "又", "还", "就是", "只要", "如何", "哪些", "多少", "怎样", "为何"
        ));
        
        // 英文停用词（扩展版）
        stopWords.addAll(Arrays.asList(
            "the", "a", "an", "and", "or", "but", "in", "on", "at", "to", "for", "of", "with", "by",
            "is", "are", "was", "were", "be", "been", "being", "have", "has", "had", "do", "does", "did",
            "will", "would", "could", "should", "may", "might", "can", "shall", "must",
            "i", "you", "he", "she", "it", "we", "they", "me", "him", "her", "us", "them",
            "my", "your", "his", "her", "its", "our", "their", "this", "that", "these", "those",
            "what", "where", "when", "why", "how", "who", "which", "whose", "whom",
            "some", "any", "all", "no", "not", "very", "just", "now", "then", "here", "there",
            "up", "down", "out", "off", "over", "under", "again", "further", "before", "after",
            "above", "below", "between", "through", "during", "into", "from", "about"
        ));
        
        // 特殊符号和无意义词
        stopWords.addAll(Arrays.asList(
            "啊", "呀", "呢", "吧", "哦", "嗯", "嗯嗯", "哈", "哈哈", "呵", "呵呵",
            "yeah", "yes", "no", "ok", "okay", "well", "um", "uh", "er", "ah", "oh"
        ));
        
        return stopWords;
    }
}
