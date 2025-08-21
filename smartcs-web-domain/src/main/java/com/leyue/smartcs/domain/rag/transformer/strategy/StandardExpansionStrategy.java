package com.leyue.smartcs.domain.rag.transformer.strategy;

import com.leyue.smartcs.domain.rag.transformer.valueobject.IntentAnalysisResult;

/**
 * 标准查询扩展策略
 * 适用于一般查询的标准扩展处理
 * 
 * @author Claude
 */
public class StandardExpansionStrategy implements QueryExpansionStrategy {
    
    private static final String STRATEGY_NAME = "标准扩展策略";
    private static final int MAX_QUERIES = 3;
    private static final String PROMPT_GUIDANCE = "生成标准的查询变体，保持查询的核心含义";
    
    @Override
    public String getStrategyName() {
        return STRATEGY_NAME;
    }
    
    @Override
    public int getMaxQueries() {
        return MAX_QUERIES;
    }
    
    @Override
    public boolean shouldSkipExpansion() {
        return false;
    }
    
    @Override
    public String buildExpansionPrompt(String originalQuery, IntentAnalysisResult intentResult) {
        return String.format("""
            你是一个智能查询优化助手。请根据用户的原始查询和识别的意图，生成%d个相关的查询变体。
            
            原始查询: %s
            识别意图: %s (置信度: %.2f)
            意图说明: %s
            
            %s
            
            请生成查询变体，每个查询占一行，不要包含编号或其他标记：
            """, 
            MAX_QUERIES,
            originalQuery,
            intentResult.getIntentCode(),
            intentResult.getConfidenceScore(),
            intentResult.getReasoning(),
            PROMPT_GUIDANCE
        );
    }
    
    @Override
    public String getPromptGuidance() {
        return PROMPT_GUIDANCE;
    }
    
    @Override
    public boolean isApplicableFor(IntentAnalysisResult intentResult) {
        // 标准扩展策略适用于大部分场景
        return !intentResult.isGreetingOrGoodbye() && 
               !intentResult.isComplaint() && 
               !intentResult.isTechnicalSupport();
    }
}