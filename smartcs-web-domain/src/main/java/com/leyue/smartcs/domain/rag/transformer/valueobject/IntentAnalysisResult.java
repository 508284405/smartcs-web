package com.leyue.smartcs.domain.rag.transformer.valueobject;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 意图分析结果值对象
 * 包含意图识别的完整信息和业务判断方法
 * 
 * @author Claude
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class IntentAnalysisResult {
    
    private final String intentCode;
    private final String catalogCode;
    private final Double confidenceScore;
    private final String reasoning;
    private final long classificationTime;
    
    /**
     * 创建意图分析结果
     */
    public static IntentAnalysisResult create(String intentCode, String catalogCode, 
                                            Double confidenceScore, String reasoning) {
        return new IntentAnalysisResult(intentCode, catalogCode, confidenceScore, reasoning, System.currentTimeMillis());
    }
    
    /**
     * 创建默认意图分析结果（意图识别失败时使用）
     */
    public static IntentAnalysisResult createDefault() {
        return new IntentAnalysisResult(
            "UNKNOWN", 
            "UNKNOWN", 
            0.0, 
            "意图分析失败，使用默认处理", 
            System.currentTimeMillis()
        );
    }
    
    /**
     * 判断是否为高置信度
     */
    public boolean isHighConfidence() {
        return confidenceScore != null && confidenceScore > 0.7;
    }
    
    /**
     * 判断是否为中等置信度
     */
    public boolean isMediumConfidence() {
        return confidenceScore != null && confidenceScore > 0.4 && confidenceScore <= 0.7;
    }
    
    /**
     * 判断是否为低置信度
     */
    public boolean isLowConfidence() {
        return confidenceScore != null && confidenceScore <= 0.4;
    }
    
    /**
     * 判断是否需要查询扩展
     */
    public boolean requiresExpansion() {
        if (intentCode == null) {
            return true; // 未知意图默认需要扩展
        }
        
        // 问候语和告别语不需要扩展
        return !isGreetingOrGoodbye();
    }
    
    /**
     * 判断是否为问候或告别意图
     */
    public boolean isGreetingOrGoodbye() {
        return "greeting".equals(intentCode) || "goodbye".equals(intentCode);
    }
    
    /**
     * 判断是否为问题询问类意图
     */
    public boolean isInquiry() {
        return "question".equals(intentCode) || "inquiry".equals(intentCode);
    }
    
    /**
     * 判断是否为投诉类意图
     */
    public boolean isComplaint() {
        return "complaint".equals(intentCode);
    }
    
    /**
     * 判断是否为技术支持类意图
     */
    public boolean isTechnicalSupport() {
        return "technical_support".equals(intentCode);
    }
    
    /**
     * 判断是否为有效的分析结果
     */
    public boolean isValid() {
        return intentCode != null && !"UNKNOWN".equals(intentCode) && confidenceScore != null;
    }
    
    @Override
    public String toString() {
        return String.format("IntentAnalysisResult{intentCode='%s', catalogCode='%s', confidence=%.2f, time=%d}", 
                           intentCode, catalogCode, confidenceScore, classificationTime);
    }
}