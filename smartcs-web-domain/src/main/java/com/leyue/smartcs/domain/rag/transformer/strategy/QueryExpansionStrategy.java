package com.leyue.smartcs.domain.rag.transformer.strategy;

import com.leyue.smartcs.domain.rag.transformer.valueobject.IntentAnalysisResult;

/**
 * 查询扩展策略接口
 * 定义不同类型的查询扩展行为
 * 
 * @author Claude
 */
public interface QueryExpansionStrategy {
    
    /**
     * 获取策略类型名称
     */
    String getStrategyName();
    
    /**
     * 获取最大查询数量
     */
    int getMaxQueries();
    
    /**
     * 是否应该跳过扩展
     */
    boolean shouldSkipExpansion();
    
    /**
     * 构建扩展提示词
     * 
     * @param originalQuery 原始查询
     * @param intentResult 意图分析结果
     * @return 扩展提示词
     */
    String buildExpansionPrompt(String originalQuery, IntentAnalysisResult intentResult);
    
    /**
     * 获取提示指导语
     */
    String getPromptGuidance();
    
    /**
     * 是否适用于指定的意图分析结果
     * 
     * @param intentResult 意图分析结果
     * @return 是否适用
     */
    boolean isApplicableFor(IntentAnalysisResult intentResult);
}