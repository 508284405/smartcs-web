package com.leyue.smartcs.domain.rag.transformer.domainservice;

import com.leyue.smartcs.domain.rag.transformer.strategy.*;
import com.leyue.smartcs.domain.rag.transformer.valueobject.IntentAnalysisResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * 意图驱动的策略选择器
 * 根据意图分析结果选择合适的查询扩展策略
 * 
 * @author Claude
 */
@Slf4j
@Service
public class IntentBasedStrategySelector {
    
    private final List<QueryExpansionStrategy> availableStrategies;
    
    public IntentBasedStrategySelector() {
        this.availableStrategies = Arrays.asList(
            new NoExpansionStrategy(),
            new DetailedExpansionStrategy(),
            new TechnicalExpansionStrategy(),
            new ProblemFocusedExpansionStrategy(),
            new StandardExpansionStrategy() // 默认策略放在最后
        );
    }
    
    /**
     * 根据意图分析结果选择最适合的扩展策略
     * 
     * @param intentResult 意图分析结果
     * @return 选择的查询扩展策略
     */
    public QueryExpansionStrategy selectStrategy(IntentAnalysisResult intentResult) {
        log.debug("开始选择查询扩展策略: intent={}, confidence={}", 
                 intentResult.getIntentCode(), intentResult.getConfidenceScore());
        
        // 首先检查是否需要扩展
        if (!intentResult.requiresExpansion()) {
            log.debug("意图不需要查询扩展，选择无扩展策略");
            return new NoExpansionStrategy();
        }
        
        // 遍历可用策略，找到第一个适用的策略
        for (QueryExpansionStrategy strategy : availableStrategies) {
            if (strategy.isApplicableFor(intentResult)) {
                log.debug("选择策略: {}, 适用于意图: {}", 
                         strategy.getStrategyName(), intentResult.getIntentCode());
                return strategy;
            }
        }
        
        // 如果没有找到适用的策略，使用标准扩展策略作为兜底
        log.debug("未找到特定策略，使用标准扩展策略作为兜底");
        return new StandardExpansionStrategy();
    }
    
    /**
     * 检查策略是否适用于指定意图
     * 
     * @param strategy 查询扩展策略
     * @param intentResult 意图分析结果
     * @return 是否适用
     */
    public boolean isStrategyApplicable(QueryExpansionStrategy strategy, IntentAnalysisResult intentResult) {
        return strategy.isApplicableFor(intentResult);
    }
    
    /**
     * 获取所有可用的策略
     * 
     * @return 策略列表
     */
    public List<QueryExpansionStrategy> getAvailableStrategies() {
        return List.copyOf(availableStrategies);
    }
}