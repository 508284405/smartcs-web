package com.leyue.smartcs.moderation.service;

import com.leyue.smartcs.domain.moderation.ModerationRecord;
import com.leyue.smartcs.domain.moderation.enums.SeverityLevel;
import com.leyue.smartcs.domain.moderation.gateway.ModerationGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * 关键词规则引擎
 * 基于预定义的关键词规则进行内容审核
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KeywordRuleEngine {

    private final ModerationGateway moderationGateway;

    // 缓存编译后的正则表达式模式
    private final Map<Long, Pattern> compiledPatterns = new ConcurrentHashMap<>();
    
    // 缓存关键词规则
    private final Map<String, List<ModerationGateway.KeywordRule>> rulesByLanguage = new ConcurrentHashMap<>();
    
    // 缓存所有规则
    private List<ModerationGateway.KeywordRule> allRules = new ArrayList<>();
    
    @PostConstruct
    public void init() {
        refreshRules();
        log.info("Keyword rule engine initialized with {} rules", allRules.size());
    }

    /**
     * 刷新规则缓存
     */
    public void refreshRules() {
        try {
            List<ModerationGateway.KeywordRule> rules = moderationGateway.findActiveKeywordRules();
            this.allRules = new ArrayList<>(rules);
            
            // 按语言分组缓存
            rulesByLanguage.clear();
            for (ModerationGateway.KeywordRule rule : rules) {
                String language = getLanguageOrDefault(rule);
                rulesByLanguage.computeIfAbsent(language, k -> new ArrayList<>()).add(rule);
            }
            
            // 预编译正则表达式
            compiledPatterns.clear();
            for (ModerationGateway.KeywordRule rule : rules) {
                if ("REGEX".equals(rule.getRuleType())) {
                    try {
                        int flags = rule.getCaseSensitive() ? 0 : Pattern.CASE_INSENSITIVE;
                        Pattern pattern = Pattern.compile(rule.getKeyword(), flags);
                        compiledPatterns.put(rule.getId(), pattern);
                    } catch (PatternSyntaxException e) {
                        log.warn("Invalid regex pattern in rule {}: {}", rule.getId(), rule.getKeyword(), e);
                    }
                }
            }
            
            log.info("Refreshed {} keyword rules", rules.size());
        } catch (Exception e) {
            log.error("Failed to refresh keyword rules", e);
        }
    }

    /**
     * 执行关键词规则检查
     */
    public KeywordModerationResult moderateContent(String content, String language) {
        if (!StringUtils.hasText(content)) {
            return KeywordModerationResult.clean("Empty content");
        }

        long startTime = System.currentTimeMillis();
        List<KeywordMatch> matches = new ArrayList<>();
        
        // 获取适用的规则
        List<ModerationGateway.KeywordRule> applicableRules = getApplicableRules(language);
        
        // 按优先级排序执行规则检查
        for (ModerationGateway.KeywordRule rule : applicableRules) {
            KeywordMatch match = checkRule(content, rule);
            if (match != null) {
                matches.add(match);
                // 更新规则命中统计（异步）
                updateRuleHitCount(rule.getId());
            }
        }
        
        long processingTime = System.currentTimeMillis() - startTime;
        
        if (matches.isEmpty()) {
            return KeywordModerationResult.clean("No keyword violations found", processingTime);
        }
        
        // 分析匹配结果，确定最终审核结果
        return analyzeMatches(matches, processingTime);
    }

    /**
     * 批量审核内容
     */
    public List<KeywordModerationResult> moderateBatch(List<String> contents, String language) {
        return contents.stream()
                .map(content -> moderateContent(content, language))
                .toList();
    }

    /**
     * 检查单个规则
     */
    private KeywordMatch checkRule(String content, ModerationGateway.KeywordRule rule) {
        try {
            String ruleType = rule.getRuleType();
            String keyword = rule.getKeyword();
            boolean caseSensitive = rule.getCaseSensitive() != null && rule.getCaseSensitive();
            
            return switch (ruleType) {
                case "EXACT" -> checkExactMatch(content, keyword, caseSensitive, rule);
                case "FUZZY" -> checkFuzzyMatch(content, keyword, rule);
                case "REGEX" -> checkRegexMatch(content, rule);
                case "SUBSTRING" -> checkSubstringMatch(content, keyword, caseSensitive, rule);
                default -> {
                    log.warn("Unknown rule type: {} for rule {}", ruleType, rule.getId());
                    yield null;
                }
            };
        } catch (Exception e) {
            log.error("Error checking rule {}: {}", rule.getId(), rule.getKeyword(), e);
            return null;
        }
    }

    /**
     * 精确匹配检查
     */
    private KeywordMatch checkExactMatch(String content, String keyword, boolean caseSensitive, 
                                       ModerationGateway.KeywordRule rule) {
        String processedContent = caseSensitive ? content : content.toLowerCase();
        String processedKeyword = caseSensitive ? keyword : keyword.toLowerCase();
        
        if (processedContent.equals(processedKeyword)) {
            return KeywordMatch.builder()
                    .ruleId(rule.getId())
                    .ruleName(rule.getRuleName())
                    .keyword(keyword)
                    .matchedText(keyword)
                    .categoryId(rule.getCategoryId())
                    .confidence(BigDecimal.ONE)
                    .severityWeight(getSeverityWeight(rule))
                    .position(0)
                    .matchType("EXACT")
                    .build();
        }
        
        return null;
    }

    /**
     * 子字符串匹配检查
     */
    private KeywordMatch checkSubstringMatch(String content, String keyword, boolean caseSensitive, 
                                           ModerationGateway.KeywordRule rule) {
        String processedContent = caseSensitive ? content : content.toLowerCase();
        String processedKeyword = caseSensitive ? keyword : keyword.toLowerCase();
        
        int index = processedContent.indexOf(processedKeyword);
        if (index >= 0) {
            return KeywordMatch.builder()
                    .ruleId(rule.getId())
                    .ruleName(rule.getRuleName())
                    .keyword(keyword)
                    .matchedText(content.substring(index, index + keyword.length()))
                    .categoryId(rule.getCategoryId())
                    .confidence(BigDecimal.valueOf(0.9))
                    .severityWeight(getSeverityWeight(rule))
                    .position(index)
                    .matchType("SUBSTRING")
                    .build();
        }
        
        return null;
    }

    /**
     * 正则表达式匹配检查
     */
    private KeywordMatch checkRegexMatch(String content, ModerationGateway.KeywordRule rule) {
        Pattern pattern = compiledPatterns.get(rule.getId());
        if (pattern == null) {
            return null;
        }
        
        java.util.regex.Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return KeywordMatch.builder()
                    .ruleId(rule.getId())
                    .ruleName(rule.getRuleName())
                    .keyword(rule.getKeyword())
                    .matchedText(matcher.group())
                    .categoryId(rule.getCategoryId())
                    .confidence(BigDecimal.valueOf(0.95))
                    .severityWeight(getSeverityWeight(rule))
                    .position(matcher.start())
                    .matchType("REGEX")
                    .build();
        }
        
        return null;
    }

    /**
     * 模糊匹配检查（基于编辑距离）
     */
    private KeywordMatch checkFuzzyMatch(String content, String keyword, ModerationGateway.KeywordRule rule) {
        BigDecimal threshold = rule.getSimilarityThreshold() != null ? 
                rule.getSimilarityThreshold() : BigDecimal.valueOf(0.8);
        
        // 简化的模糊匹配实现，实际可以使用更复杂的算法
        String[] words = content.toLowerCase().split("\\s+");
        String keywordLower = keyword.toLowerCase();
        
        for (int i = 0; i < words.length; i++) {
            double similarity = calculateSimilarity(words[i], keywordLower);
            if (similarity >= threshold.doubleValue()) {
                return KeywordMatch.builder()
                        .ruleId(rule.getId())
                        .ruleName(rule.getRuleName())
                        .keyword(keyword)
                        .matchedText(words[i])
                        .categoryId(rule.getCategoryId())
                        .confidence(BigDecimal.valueOf(similarity))
                        .severityWeight(getSeverityWeight(rule))
                        .position(content.toLowerCase().indexOf(words[i]))
                        .matchType("FUZZY")
                        .build();
            }
        }
        
        return null;
    }

    /**
     * 计算字符串相似度（简化版本的编辑距离）
     */
    private double calculateSimilarity(String s1, String s2) {
        if (s1.equals(s2)) {
            return 1.0;
        }
        
        int maxLen = Math.max(s1.length(), s2.length());
        if (maxLen == 0) {
            return 1.0;
        }
        
        int distance = calculateLevenshteinDistance(s1, s2);
        return 1.0 - (double) distance / maxLen;
    }

    /**
     * 计算编辑距离
     */
    private int calculateLevenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];
        
        for (int i = 0; i <= s1.length(); i++) {
            dp[i][0] = i;
        }
        
        for (int j = 0; j <= s2.length(); j++) {
            dp[0][j] = j;
        }
        
        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = Math.min(Math.min(dp[i - 1][j], dp[i][j - 1]), dp[i - 1][j - 1]) + 1;
                }
            }
        }
        
        return dp[s1.length()][s2.length()];
    }

    /**
     * 分析匹配结果
     */
    private KeywordModerationResult analyzeMatches(List<KeywordMatch> matches, long processingTime) {
        if (matches.isEmpty()) {
            return KeywordModerationResult.clean("No matches found", processingTime);
        }

        // 按严重程度排序
        matches.sort((m1, m2) -> m2.getSeverityWeight().compareTo(m1.getSeverityWeight()));
        
        KeywordMatch mostSevere = matches.get(0);
        BigDecimal maxSeverity = mostSevere.getSeverityWeight();
        
        // 根据严重程度确定风险级别
        SeverityLevel riskLevel;
        if (maxSeverity.compareTo(BigDecimal.valueOf(3.0)) >= 0) {
            riskLevel = SeverityLevel.CRITICAL;
        } else if (maxSeverity.compareTo(BigDecimal.valueOf(2.0)) >= 0) {
            riskLevel = SeverityLevel.HIGH;
        } else if (maxSeverity.compareTo(BigDecimal.valueOf(1.0)) >= 0) {
            riskLevel = SeverityLevel.MEDIUM;
        } else {
            riskLevel = SeverityLevel.LOW;
        }

        // 构建违规信息
        List<ModerationRecord.ModerationViolation> violations = matches.stream()
                .map(this::convertToViolation)
                .toList();

        return KeywordModerationResult.builder()
                .hasViolations(true)
                .riskLevel(riskLevel)
                .confidence(mostSevere.getConfidence())
                .matches(matches)
                .violations(violations)
                .processingTimeMs(processingTime)
                .matchedKeywords(matches.stream().map(KeywordMatch::getKeyword).distinct().toList())
                .reasoning("Keyword violations detected: " + matches.size() + " matches")
                .build();
    }

    /**
     * 转换为违规信息
     */
    private ModerationRecord.ModerationViolation convertToViolation(KeywordMatch match) {
        return ModerationRecord.ModerationViolation.builder()
                .categoryId(match.getCategoryId())
                .categoryName("Keyword Violation") // 实际应该查询分类名称
                .categoryCode("KEYWORD_" + match.getRuleId())
                .confidence(match.getConfidence())
                .triggerRule(match.getRuleName() + " -> " + match.getKeyword())
                .build();
    }

    /**
     * 获取适用的规则
     */
    private List<ModerationGateway.KeywordRule> getApplicableRules(String language) {
        List<ModerationGateway.KeywordRule> rules = new ArrayList<>();
        
        // 添加指定语言的规则
        if (StringUtils.hasText(language)) {
            rules.addAll(rulesByLanguage.getOrDefault(language, Collections.emptyList()));
        }
        
        // 添加通用规则（auto语言）
        rules.addAll(rulesByLanguage.getOrDefault("auto", Collections.emptyList()));
        
        // 如果没有找到特定语言的规则，使用所有规则
        if (rules.isEmpty()) {
            rules.addAll(allRules);
        }
        
        // 按优先级排序
        rules.sort(Comparator.comparing(ModerationGateway.KeywordRule::getPriority, 
                Comparator.nullsLast(Comparator.naturalOrder())));
        
        return rules;
    }

    /**
     * 异步更新规则命中统计
     */
    private void updateRuleHitCount(Long ruleId) {
        // 异步更新，避免影响审核性能
        CompletableFuture.runAsync(() -> {
            try {
                moderationGateway.updateKeywordRuleHitCount(ruleId, 1, System.currentTimeMillis());
            } catch (Exception e) {
                log.warn("Failed to update hit count for rule {}", ruleId, e);
            }
        });
    }

    /**
     * 获取规则的严重程度权重
     */
    private BigDecimal getSeverityWeight(ModerationGateway.KeywordRule rule) {
        // 这里可能需要从数据库字段获取，暂时使用默认值
        return BigDecimal.valueOf(1.0);
    }

    /**
     * 获取语言或默认值
     */
    private String getLanguageOrDefault(ModerationGateway.KeywordRule rule) {
        // 从规则获取语言信息的逻辑需要补充
        return "zh"; // 默认中文
    }

    /**
     * 关键词匹配结果
     */
    public static class KeywordMatch {
        private Long ruleId;
        private String ruleName;
        private String keyword;
        private String matchedText;
        private Long categoryId;
        private BigDecimal confidence;
        private BigDecimal severityWeight;
        private int position;
        private String matchType;

        private KeywordMatch(Builder builder) {
            this.ruleId = builder.ruleId;
            this.ruleName = builder.ruleName;
            this.keyword = builder.keyword;
            this.matchedText = builder.matchedText;
            this.categoryId = builder.categoryId;
            this.confidence = builder.confidence;
            this.severityWeight = builder.severityWeight;
            this.position = builder.position;
            this.matchType = builder.matchType;
        }

        public static Builder builder() { return new Builder(); }

        // Getters
        public Long getRuleId() { return ruleId; }
        public String getRuleName() { return ruleName; }
        public String getKeyword() { return keyword; }
        public String getMatchedText() { return matchedText; }
        public Long getCategoryId() { return categoryId; }
        public BigDecimal getConfidence() { return confidence; }
        public BigDecimal getSeverityWeight() { return severityWeight; }
        public int getPosition() { return position; }
        public String getMatchType() { return matchType; }

        public static class Builder {
            private Long ruleId;
            private String ruleName;
            private String keyword;
            private String matchedText;
            private Long categoryId;
            private BigDecimal confidence;
            private BigDecimal severityWeight;
            private int position;
            private String matchType;

            public Builder ruleId(Long ruleId) { this.ruleId = ruleId; return this; }
            public Builder ruleName(String ruleName) { this.ruleName = ruleName; return this; }
            public Builder keyword(String keyword) { this.keyword = keyword; return this; }
            public Builder matchedText(String matchedText) { this.matchedText = matchedText; return this; }
            public Builder categoryId(Long categoryId) { this.categoryId = categoryId; return this; }
            public Builder confidence(BigDecimal confidence) { this.confidence = confidence; return this; }
            public Builder severityWeight(BigDecimal severityWeight) { this.severityWeight = severityWeight; return this; }
            public Builder position(int position) { this.position = position; return this; }
            public Builder matchType(String matchType) { this.matchType = matchType; return this; }
            public KeywordMatch build() { return new KeywordMatch(this); }
        }
    }

    /**
     * 关键词审核结果
     */
    public static class KeywordModerationResult {
        private boolean hasViolations;
        private SeverityLevel riskLevel;
        private BigDecimal confidence;
        private List<KeywordMatch> matches;
        private List<ModerationRecord.ModerationViolation> violations;
        private long processingTimeMs;
        private List<String> matchedKeywords;
        private String reasoning;

        private KeywordModerationResult(Builder builder) {
            this.hasViolations = builder.hasViolations;
            this.riskLevel = builder.riskLevel;
            this.confidence = builder.confidence;
            this.matches = builder.matches != null ? builder.matches : new ArrayList<>();
            this.violations = builder.violations != null ? builder.violations : new ArrayList<>();
            this.processingTimeMs = builder.processingTimeMs;
            this.matchedKeywords = builder.matchedKeywords != null ? builder.matchedKeywords : new ArrayList<>();
            this.reasoning = builder.reasoning;
        }

        public static Builder builder() { return new Builder(); }

        public static KeywordModerationResult clean(String reasoning) {
            return builder()
                    .hasViolations(false)
                    .riskLevel(SeverityLevel.LOW)
                    .confidence(BigDecimal.ONE)
                    .reasoning(reasoning)
                    .build();
        }

        public static KeywordModerationResult clean(String reasoning, long processingTime) {
            return builder()
                    .hasViolations(false)
                    .riskLevel(SeverityLevel.LOW)
                    .confidence(BigDecimal.ONE)
                    .processingTimeMs(processingTime)
                    .reasoning(reasoning)
                    .build();
        }

        // Getters
        public boolean hasViolations() { return hasViolations; }
        public SeverityLevel getRiskLevel() { return riskLevel; }
        public BigDecimal getConfidence() { return confidence; }
        public List<KeywordMatch> getMatches() { return matches; }
        public List<ModerationRecord.ModerationViolation> getViolations() { return violations; }
        public long getProcessingTimeMs() { return processingTimeMs; }
        public List<String> getMatchedKeywords() { return matchedKeywords; }
        public String getReasoning() { return reasoning; }

        public static class Builder {
            private boolean hasViolations;
            private SeverityLevel riskLevel;
            private BigDecimal confidence;
            private List<KeywordMatch> matches;
            private List<ModerationRecord.ModerationViolation> violations;
            private long processingTimeMs;
            private List<String> matchedKeywords;
            private String reasoning;

            public Builder hasViolations(boolean hasViolations) { this.hasViolations = hasViolations; return this; }
            public Builder riskLevel(SeverityLevel riskLevel) { this.riskLevel = riskLevel; return this; }
            public Builder confidence(BigDecimal confidence) { this.confidence = confidence; return this; }
            public Builder matches(List<KeywordMatch> matches) { this.matches = matches; return this; }
            public Builder violations(List<ModerationRecord.ModerationViolation> violations) { this.violations = violations; return this; }
            public Builder processingTimeMs(long processingTimeMs) { this.processingTimeMs = processingTimeMs; return this; }
            public Builder matchedKeywords(List<String> matchedKeywords) { this.matchedKeywords = matchedKeywords; return this; }
            public Builder reasoning(String reasoning) { this.reasoning = reasoning; return this; }
            public KeywordModerationResult build() { return new KeywordModerationResult(this); }
        }
    }
}