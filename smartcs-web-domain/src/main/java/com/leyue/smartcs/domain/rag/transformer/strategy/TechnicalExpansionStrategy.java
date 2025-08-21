package com.leyue.smartcs.domain.rag.transformer.strategy;

import com.leyue.smartcs.domain.rag.transformer.valueobject.IntentAnalysisResult;

/**
 * 技术扩展策略
 * 适用于技术支持类意图的专业扩展处理
 * 
 * @author Claude
 */
public class TechnicalExpansionStrategy implements QueryExpansionStrategy {
    
    private static final String STRATEGY_NAME = "技术扩展策略";
    private static final int MAX_QUERIES = 6;
    private static final String PROMPT_GUIDANCE = "生成技术相关的查询变体，包含专业术语和技术细节";
    
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
            你是一个技术专家查询优化助手。请根据用户的原始查询和识别的意图，生成%d个技术相关的查询变体。
            
            原始查询: %s
            识别意图: %s (置信度: %.2f)
            意图说明: %s
            
            %s
            
            技术扩展要求：
            1. 包含相关的技术术语和专业词汇
            2. 从故障排除、配置、性能等技术角度扩展
            3. 考虑不同的技术层面（硬件、软件、网络等）
            4. 包含可能的错误代码、症状描述
            5. 生成调试和诊断相关的查询变体
            
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
        // 技术扩展策略适用于技术支持类意图
        return intentResult.isTechnicalSupport();
    }
}