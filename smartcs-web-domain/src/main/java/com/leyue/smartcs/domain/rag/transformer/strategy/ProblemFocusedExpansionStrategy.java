package com.leyue.smartcs.domain.rag.transformer.strategy;

import com.leyue.smartcs.domain.rag.transformer.valueobject.IntentAnalysisResult;

/**
 * 问题聚焦扩展策略
 * 适用于投诉类意图的问题解决导向扩展处理
 * 
 * @author Claude
 */
public class ProblemFocusedExpansionStrategy implements QueryExpansionStrategy {
    
    private static final String STRATEGY_NAME = "问题聚焦扩展策略";
    private static final int MAX_QUERIES = 4;
    private static final String PROMPT_GUIDANCE = "专注于问题解决的查询变体，包含故障排除和解决方案相关的表述";
    
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
            你是一个问题解决专家查询优化助手。请根据用户的原始查询和识别的意图，生成%d个问题解决导向的查询变体。
            
            原始查询: %s
            识别意图: %s (置信度: %.2f)
            意图说明: %s
            
            %s
            
            问题聚焦要求：
            1. 专注于问题的根本原因和解决方案
            2. 包含故障排除和诊断相关的表述
            3. 从用户体验和服务质量角度思考
            4. 生成可能的替代解决方案查询
            5. 考虑预防类似问题的查询变体
            
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
        // 问题聚焦策略适用于投诉类意图
        return intentResult.isComplaint();
    }
}