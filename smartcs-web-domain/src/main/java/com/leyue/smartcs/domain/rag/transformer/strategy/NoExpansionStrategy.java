package com.leyue.smartcs.domain.rag.transformer.strategy;

import com.leyue.smartcs.domain.rag.transformer.valueobject.IntentAnalysisResult;

/**
 * 无扩展策略
 * 适用于不需要查询扩展的场景，如问候语、告别语等
 * 
 * @author Claude
 */
public class NoExpansionStrategy implements QueryExpansionStrategy {
    
    private static final String STRATEGY_NAME = "无扩展策略";
    private static final int MAX_QUERIES = 1;
    private static final String PROMPT_GUIDANCE = "直接使用原始查询，无需扩展";
    
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
        return true;
    }
    
    @Override
    public String buildExpansionPrompt(String originalQuery, IntentAnalysisResult intentResult) {
        // 无扩展策略不需要构建提示词
        return originalQuery;
    }
    
    @Override
    public String getPromptGuidance() {
        return PROMPT_GUIDANCE;
    }
    
    @Override
    public boolean isApplicableFor(IntentAnalysisResult intentResult) {
        // 无扩展策略适用于问候语和告别语
        return intentResult.isGreetingOrGoodbye();
    }
}