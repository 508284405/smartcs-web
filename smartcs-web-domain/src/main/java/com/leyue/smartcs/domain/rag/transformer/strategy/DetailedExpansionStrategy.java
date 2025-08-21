package com.leyue.smartcs.domain.rag.transformer.strategy;

import com.leyue.smartcs.domain.rag.transformer.valueobject.IntentAnalysisResult;

/**
 * 详细查询扩展策略
 * 适用于高置信度询问类意图的详细扩展处理
 * 
 * @author Claude
 */
public class DetailedExpansionStrategy implements QueryExpansionStrategy {
    
    private static final String STRATEGY_NAME = "详细扩展策略";
    private static final int MAX_QUERIES = 5;
    private static final String PROMPT_GUIDANCE = "生成更多详细的查询变体，从不同角度探索问题";
    
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
            
            特别要求：
            1. 从不同角度重新表达查询
            2. 包含更具体和更抽象的表述
            3. 考虑相关的上下文和背景
            4. 生成可能的同义词和近义词变体
            
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
        // 详细扩展策略适用于高置信度的询问类意图
        return intentResult.isInquiry() && intentResult.isHighConfidence();
    }
}