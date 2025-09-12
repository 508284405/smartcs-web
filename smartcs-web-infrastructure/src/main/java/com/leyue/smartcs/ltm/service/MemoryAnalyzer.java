package com.leyue.smartcs.ltm.service;

import dev.langchain4j.model.language.LanguageModel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.HashMap;
import java.util.regex.Pattern;

/**
 * 记忆分析器
 * 负责分析对话内容的重要性和行为模式
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MemoryAnalyzer {

    private final LanguageModel languageModel;

    @Value("${smartcs.ai.ltm.analyzer.use-llm:true}")
    private boolean useLlmAnalysis;

    // 重要性关键词模式
    private static final Pattern IMPORTANT_PATTERNS = Pattern.compile(
        "\\b(重要|关键|记住|一定要|不要忘记|特别|注意|偏好|喜欢|不喜欢|习惯|总是|从不|经常|通常)\\b",
        Pattern.CASE_INSENSITIVE
    );

    // 偏好模式
    private static final Pattern PREFERENCE_PATTERNS = Pattern.compile(
        "\\b(我(更)?喜欢|我(不)?希望|我(通常|经常|总是)|我的习惯|我认为|我觉得)\\b",
        Pattern.CASE_INSENSITIVE
    );

    // 技能模式
    private static final Pattern SKILL_PATTERNS = Pattern.compile(
        "\\b(我会|我懂|我熟悉|我了解|我不会|我不懂|我擅长|我的专业|我的工作)\\b",
        Pattern.CASE_INSENSITIVE
    );

    // 时间模式
    private static final Pattern TIME_PATTERNS = Pattern.compile(
        "\\b(今天|明天|昨天|本周|下周|上周|这个月|下个月|上个月|今年|明年|去年)\\b",
        Pattern.CASE_INSENSITIVE
    );

    /**
     * 分析内容重要性
     */
    public double analyzeImportance(String content, Map<String, Object> context) {
        if (content == null || content.trim().isEmpty()) {
            return 0.0;
        }

        double importance = 0.0;

        // 基础规则分析
        importance += analyzeBasicImportance(content, context);

        // 如果启用LLM分析，使用AI进行更精确的重要性评估
        if (useLlmAnalysis) {
            try {
                double llmImportance = analyzeLlmImportance(content, context);
                // 结合规则分析和LLM分析结果
                importance = (importance * 0.4) + (llmImportance * 0.6);
            } catch (Exception e) {
                log.warn("LLM重要性分析失败，使用基础分析结果: {}", e.getMessage());
            }
        }

        // 归一化到0-1范围
        return Math.min(1.0, Math.max(0.0, importance));
    }

    /**
     * 分析行为模式
     */
    public Map<String, Object> analyzeBehaviorPatterns(String content, Map<String, Object> context) {
        Map<String, Object> patterns = new HashMap<>();

        // 分析偏好模式
        analyzePreferencePatterns(content, patterns);

        // 分析技能模式
        analyzeSkillPatterns(content, patterns);

        // 分析时间模式
        analyzeTimePatterns(content, context, patterns);

        // 分析响应风格
        analyzeResponseStylePatterns(content, context, patterns);

        log.debug("分析到{}个行为模式", patterns.size());
        return patterns;
    }

    /**
     * 基础重要性分析
     */
    private double analyzeBasicImportance(String content, Map<String, Object> context) {
        double score = 0.3; // 基础分数

        // 长度因子
        int length = content.length();
        if (length > 100) score += 0.1;
        if (length > 300) score += 0.1;

        // 关键词匹配
        if (IMPORTANT_PATTERNS.matcher(content).find()) {
            score += 0.2;
        }

        // 偏好表达
        if (PREFERENCE_PATTERNS.matcher(content).find()) {
            score += 0.3;
        }

        // 技能声明
        if (SKILL_PATTERNS.matcher(content).find()) {
            score += 0.2;
        }

        // 时间相关（时效性信息）
        if (TIME_PATTERNS.matcher(content).find()) {
            score += 0.1;
        }

        // 上下文因子
        if (context != null) {
            // 对话轮数越多，重要性可能越高
            Integer messageCount = (Integer) context.get("total_message_count");
            if (messageCount != null && messageCount > 10) {
                score += 0.1;
            }

            // 用户消息比例高表示用户更主动
            Integer userCount = (Integer) context.get("user_message_count");
            Integer totalCount = (Integer) context.get("total_message_count");
            if (userCount != null && totalCount != null && totalCount > 0) {
                double userRatio = (double) userCount / totalCount;
                if (userRatio > 0.6) {
                    score += 0.1;
                }
            }
        }

        return score;
    }

    /**
     * LLM重要性分析
     */
    private double analyzeLlmImportance(String content, Map<String, Object> context) {
        String prompt = buildImportanceAnalysisPrompt(content, context);
        String result = languageModel.generate(prompt);
        
        return parseImportanceScore(result);
    }

    /**
     * 分析偏好模式
     */
    private void analyzePreferencePatterns(String content, Map<String, Object> patterns) {
        if (PREFERENCE_PATTERNS.matcher(content).find()) {
            Map<String, Object> preferenceData = new HashMap<>();
            preferenceData.put("name", "user_preference");
            preferenceData.put("description", "用户表达了个人偏好");
            preferenceData.put("triggers", Map.of("content_contains_preference", true));
            patterns.put("preference", preferenceData);
        }
    }

    /**
     * 分析技能模式
     */
    private void analyzeSkillPatterns(String content, Map<String, Object> patterns) {
        if (SKILL_PATTERNS.matcher(content).find()) {
            Map<String, Object> skillData = new HashMap<>();
            skillData.put("name", "skill_declaration");
            skillData.put("description", "用户声明了技能或知识水平");
            skillData.put("triggers", Map.of("content_contains_skill", true));
            patterns.put("habit", skillData);
        }
    }

    /**
     * 分析时间模式
     */
    private void analyzeTimePatterns(String content, Map<String, Object> context, Map<String, Object> patterns) {
        if (TIME_PATTERNS.matcher(content).find()) {
            Map<String, Object> timeData = new HashMap<>();
            timeData.put("name", "time_reference");
            timeData.put("description", "用户提及了时间相关信息");
            timeData.put("triggers", Map.of("content_contains_time", true));
            patterns.put("habit", timeData);
        }
    }

    /**
     * 分析响应风格模式
     */
    private void analyzeResponseStylePatterns(String content, Map<String, Object> context, Map<String, Object> patterns) {
        // 简单的风格分析
        boolean isDetailed = content.length() > 200;
        boolean isPolite = content.contains("请") || content.contains("谢谢") || content.contains("麻烦");
        boolean isTechnical = content.matches(".*\\b(技术|代码|算法|系统|架构|开发)\\b.*");

        Map<String, Object> styleData = new HashMap<>();
        styleData.put("name", "communication_style");
        
        StringBuilder description = new StringBuilder("用户沟通风格特征：");
        if (isDetailed) description.append("详细, ");
        if (isPolite) description.append("礼貌, ");
        if (isTechnical) description.append("技术导向, ");
        
        styleData.put("description", description.toString());
        styleData.put("triggers", Map.of(
            "detailed", isDetailed,
            "polite", isPolite,
            "technical", isTechnical
        ));
        
        patterns.put("response_style", styleData);
    }

    /**
     * 构建重要性分析提示
     */
    private String buildImportanceAnalysisPrompt(String content, Map<String, Object> context) {
        return """
            请分析以下对话内容的重要性，用于长期记忆存储决策。
            
            对话内容：
            %s
            
            上下文信息：
            %s
            
            评估标准：
            1. 个人偏好和习惯 (高重要性)
            2. 技能和专业信息 (高重要性)  
            3. 重要决定和计划 (高重要性)
            4. 日常对话 (低重要性)
            5. 重复信息 (低重要性)
            
            请返回一个0-1之间的重要性分数，精确到小数点后1位。
            只返回数字，不需要额外解释。
            
            例如：0.8
            """.formatted(content, context != null ? context.toString() : "无");
    }

    /**
     * 解析重要性分数
     */
    private double parseImportanceScore(String result) {
        if (result == null) return 0.5;
        
        // 提取数字
        String cleaned = result.trim().replaceAll("[^0-9.]", "");
        try {
            double score = Double.parseDouble(cleaned);
            return Math.min(1.0, Math.max(0.0, score));
        } catch (NumberFormatException e) {
            log.warn("解析重要性分数失败: {}", result);
            return 0.5; // 默认中等重要性
        }
    }
}
