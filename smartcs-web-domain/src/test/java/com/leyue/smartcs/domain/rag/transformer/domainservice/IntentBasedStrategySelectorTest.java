package com.leyue.smartcs.domain.rag.transformer.domainservice;

import com.leyue.smartcs.domain.rag.transformer.strategy.*;
import com.leyue.smartcs.domain.rag.transformer.valueobject.IntentAnalysisResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 意图驱动策略选择器测试
 * 
 * @author Claude
 */
class IntentBasedStrategySelectorTest {

    private IntentBasedStrategySelector strategySelector;

    @BeforeEach
    void setUp() {
        strategySelector = new IntentBasedStrategySelector();
    }

    @Test
    void testSelectStrategyForGreeting() {
        IntentAnalysisResult greeting = IntentAnalysisResult.create("greeting", "social", 0.9, "问候语");
        
        QueryExpansionStrategy strategy = strategySelector.selectStrategy(greeting);
        
        assertTrue(strategy instanceof NoExpansionStrategy);
        assertTrue(strategy.shouldSkipExpansion());
    }

    @Test
    void testSelectStrategyForHighConfidenceInquiry() {
        IntentAnalysisResult inquiry = IntentAnalysisResult.create("question", "inquiry", 0.8, "用户询问");
        
        QueryExpansionStrategy strategy = strategySelector.selectStrategy(inquiry);
        
        assertTrue(strategy instanceof DetailedExpansionStrategy);
        assertEquals(5, strategy.getMaxQueries());
    }

    @Test
    void testSelectStrategyForComplaint() {
        IntentAnalysisResult complaint = IntentAnalysisResult.create("complaint", "service", 0.7, "用户投诉");
        
        QueryExpansionStrategy strategy = strategySelector.selectStrategy(complaint);
        
        assertTrue(strategy instanceof ProblemFocusedExpansionStrategy);
        assertEquals(4, strategy.getMaxQueries());
    }

    @Test
    void testSelectStrategyForTechnicalSupport() {
        IntentAnalysisResult technical = IntentAnalysisResult.create("technical_support", "support", 0.8, "技术支持");
        
        QueryExpansionStrategy strategy = strategySelector.selectStrategy(technical);
        
        assertTrue(strategy instanceof TechnicalExpansionStrategy);
        assertEquals(6, strategy.getMaxQueries());
    }

    @Test
    void testSelectStrategyForOtherIntent() {
        IntentAnalysisResult other = IntentAnalysisResult.create("other", "general", 0.6, "其他意图");
        
        QueryExpansionStrategy strategy = strategySelector.selectStrategy(other);
        
        assertTrue(strategy instanceof StandardExpansionStrategy);
        assertEquals(3, strategy.getMaxQueries());
    }

    @Test
    void testSelectStrategyForUnknownIntent() {
        IntentAnalysisResult unknown = IntentAnalysisResult.createDefault();
        
        QueryExpansionStrategy strategy = strategySelector.selectStrategy(unknown);
        
        // 未知意图仍然需要扩展，应该使用标准策略
        assertTrue(strategy instanceof StandardExpansionStrategy);
    }

    @Test
    void testIsStrategyApplicable() {
        IntentAnalysisResult inquiry = IntentAnalysisResult.create("question", "inquiry", 0.8, "用户询问");
        
        DetailedExpansionStrategy detailedStrategy = new DetailedExpansionStrategy();
        StandardExpansionStrategy standardStrategy = new StandardExpansionStrategy();
        
        assertTrue(strategySelector.isStrategyApplicable(detailedStrategy, inquiry));
        assertTrue(strategySelector.isStrategyApplicable(standardStrategy, inquiry));
    }

    @Test
    void testGetAvailableStrategies() {
        var strategies = strategySelector.getAvailableStrategies();
        
        assertNotNull(strategies);
        assertFalse(strategies.isEmpty());
        assertEquals(5, strategies.size());
        
        // 确保包含所有策略类型
        assertTrue(strategies.stream().anyMatch(s -> s instanceof NoExpansionStrategy));
        assertTrue(strategies.stream().anyMatch(s -> s instanceof DetailedExpansionStrategy));
        assertTrue(strategies.stream().anyMatch(s -> s instanceof TechnicalExpansionStrategy));
        assertTrue(strategies.stream().anyMatch(s -> s instanceof ProblemFocusedExpansionStrategy));
        assertTrue(strategies.stream().anyMatch(s -> s instanceof StandardExpansionStrategy));
    }
}