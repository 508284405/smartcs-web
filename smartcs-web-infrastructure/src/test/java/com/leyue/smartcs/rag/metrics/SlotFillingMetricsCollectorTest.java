package com.leyue.smartcs.rag.metrics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SlotFillingMetricsCollector单元测试
 * 
 * @author Claude
 */
class SlotFillingMetricsCollectorTest {
    
    private SlotFillingMetricsCollector metricsCollector;
    
    @BeforeEach
    void setUp() {
        metricsCollector = new SlotFillingMetricsCollector();
    }
    
    @Test
    void testRecordQueryStart_ShouldIncrementTotalQueries() {
        // Given
        String intentCode = "ORDER_QUERY";
        
        // When
        metricsCollector.recordQueryStart(intentCode);
        
        // Then
        SlotFillingMetricsCollector.SlotFillingMetricsSummary summary = metricsCollector.getMetricsSummary();
        assertEquals(1, summary.getTotalQueriesProcessed());
        
        Map<String, SlotFillingMetricsCollector.IntentMetrics> intentMetrics = metricsCollector.getIntentMetrics();
        assertTrue(intentMetrics.containsKey(intentCode));
        assertEquals(1, intentMetrics.get(intentCode).getQueryCount().get());
    }
    
    @Test
    void testRecordSlotFillingActivated_ShouldIncrementActivatedQueries() {
        // Given
        String intentCode = "ORDER_QUERY";
        int totalSlots = 3;
        int missingSlots = 2;
        
        // When
        metricsCollector.recordSlotFillingActivated(intentCode, totalSlots, missingSlots);
        
        // Then
        SlotFillingMetricsCollector.SlotFillingMetricsSummary summary = metricsCollector.getMetricsSummary();
        assertEquals(1, summary.getQueriesWithSlotFilling());
        
        Map<String, SlotFillingMetricsCollector.IntentMetrics> intentMetrics = metricsCollector.getIntentMetrics();
        SlotFillingMetricsCollector.IntentMetrics metrics = intentMetrics.get(intentCode);
        assertNotNull(metrics);
        assertEquals(1, metrics.getSlotFillingActivated().get());
        assertEquals(totalSlots, metrics.getTotalSlotsSum().get());
        assertEquals(missingSlots, metrics.getMissingSlotsSum().get());
    }
    
    @Test
    void testRecordClarificationRequired_ShouldIncrementClarificationQueries() {
        // Given
        String intentCode = "ORDER_QUERY";
        int missingSlots = 2;
        int questionsGenerated = 2;
        
        // When
        metricsCollector.recordClarificationRequired(intentCode, missingSlots, questionsGenerated);
        
        // Then
        SlotFillingMetricsCollector.SlotFillingMetricsSummary summary = metricsCollector.getMetricsSummary();
        assertEquals(1, summary.getQueriesRequiringClarification());
        
        Map<String, SlotFillingMetricsCollector.IntentMetrics> intentMetrics = metricsCollector.getIntentMetrics();
        SlotFillingMetricsCollector.IntentMetrics metrics = intentMetrics.get(intentCode);
        assertNotNull(metrics);
        assertEquals(1, metrics.getClarificationRequired().get());
        assertEquals(questionsGenerated, metrics.getQuestionsGeneratedSum().get());
    }
    
    @Test
    void testRecordClarificationSuccess_ShouldIncrementSuccessfulClarifications() {
        // Given
        String intentCode = "ORDER_QUERY";
        int clarificationAttempts = 2;
        
        // When
        metricsCollector.recordClarificationSuccess(intentCode, clarificationAttempts);
        
        // Then
        SlotFillingMetricsCollector.SlotFillingMetricsSummary summary = metricsCollector.getMetricsSummary();
        assertEquals(1, summary.getSuccessfulClarifications());
        
        Map<String, SlotFillingMetricsCollector.IntentMetrics> intentMetrics = metricsCollector.getIntentMetrics();
        SlotFillingMetricsCollector.IntentMetrics metrics = intentMetrics.get(intentCode);
        assertNotNull(metrics);
        assertEquals(1, metrics.getClarificationSuccess().get());
        assertEquals(clarificationAttempts, metrics.getClarificationAttemptsSum().get());
    }
    
    @Test
    void testRecordClarificationTimeout_ShouldIncrementTimeoutClarifications() {
        // Given
        String intentCode = "ORDER_QUERY";
        int clarificationAttempts = 3;
        
        // When
        metricsCollector.recordClarificationTimeout(intentCode, clarificationAttempts);
        
        // Then
        SlotFillingMetricsCollector.SlotFillingMetricsSummary summary = metricsCollector.getMetricsSummary();
        assertEquals(1, summary.getTimeoutClarifications());
        
        Map<String, SlotFillingMetricsCollector.IntentMetrics> intentMetrics = metricsCollector.getIntentMetrics();
        SlotFillingMetricsCollector.IntentMetrics metrics = intentMetrics.get(intentCode);
        assertNotNull(metrics);
        assertEquals(1, metrics.getClarificationTimeout().get());
        assertEquals(clarificationAttempts, metrics.getClarificationAttemptsSum().get());
    }
    
    @Test
    void testRecordRetrievalBlocked_ShouldIncrementBlockedRetrievals() {
        // Given
        String intentCode = "ORDER_QUERY";
        String reason = "缺失必填槽位";
        
        // When
        metricsCollector.recordRetrievalBlocked(intentCode, reason);
        
        // Then
        SlotFillingMetricsCollector.SlotFillingMetricsSummary summary = metricsCollector.getMetricsSummary();
        assertEquals(1, summary.getRetrievalBlocked());
        
        Map<String, SlotFillingMetricsCollector.IntentMetrics> intentMetrics = metricsCollector.getIntentMetrics();
        SlotFillingMetricsCollector.IntentMetrics metrics = intentMetrics.get(intentCode);
        assertNotNull(metrics);
        assertEquals(1, metrics.getRetrievalBlocked().get());
    }
    
    @Test
    void testRecordProcessingTime_ShouldUpdateProcessingTimeMetrics() {
        // Given
        String intentCode = "ORDER_QUERY";
        long processingTime1 = 100L;
        long processingTime2 = 200L;
        
        // When
        metricsCollector.recordQueryStart(intentCode); // 需要先记录查询开始
        metricsCollector.recordProcessingTime(intentCode, processingTime1);
        metricsCollector.recordQueryStart(intentCode);
        metricsCollector.recordProcessingTime(intentCode, processingTime2);
        
        // Then
        SlotFillingMetricsCollector.SlotFillingMetricsSummary summary = metricsCollector.getMetricsSummary();
        assertEquals(300L, summary.getTotalProcessingTime());
        assertEquals(200L, summary.getMaxProcessingTime());
        assertEquals(150.0, summary.getAverageProcessingTime());
        
        Map<String, SlotFillingMetricsCollector.IntentMetrics> intentMetrics = metricsCollector.getIntentMetrics();
        SlotFillingMetricsCollector.IntentMetrics metrics = intentMetrics.get(intentCode);
        assertNotNull(metrics);
        assertEquals(300L, metrics.getTotalProcessingTime().get());
        assertEquals(200L, metrics.getMaxProcessingTime().get());
        assertEquals(150.0, metrics.getAverageProcessingTime());
    }
    
    @Test
    void testGetMetricsSummary_ShouldCalculateRatesCorrectly() {
        // Given
        String intentCode = "ORDER_QUERY";
        
        // 记录各种指标
        metricsCollector.recordQueryStart(intentCode);
        metricsCollector.recordQueryStart(intentCode);
        metricsCollector.recordSlotFillingActivated(intentCode, 3, 2);
        metricsCollector.recordClarificationRequired(intentCode, 2, 2);
        metricsCollector.recordClarificationSuccess(intentCode, 1);
        
        // When
        SlotFillingMetricsCollector.SlotFillingMetricsSummary summary = metricsCollector.getMetricsSummary();
        
        // Then
        assertEquals(2, summary.getTotalQueriesProcessed());
        assertEquals(1, summary.getQueriesWithSlotFilling());
        assertEquals(1, summary.getQueriesRequiringClarification());
        assertEquals(1, summary.getSuccessfulClarifications());
        
        // 验证比率计算
        assertEquals(0.5, summary.getSlotFillingActivationRate(), 0.001); // 1/2
        assertEquals(0.5, summary.getClarificationRequiredRate(), 0.001); // 1/2
        assertEquals(1.0, summary.getClarificationSuccessRate(), 0.001); // 1/1
    }
    
    @Test
    void testIntentMetrics_ShouldCalculateAveragesCorrectly() {
        // Given
        String intentCode = "ORDER_QUERY";
        
        // 记录多次激活
        metricsCollector.recordSlotFillingActivated(intentCode, 3, 2);
        metricsCollector.recordSlotFillingActivated(intentCode, 5, 1);
        metricsCollector.recordSlotFillingActivated(intentCode, 4, 3);
        
        // When
        Map<String, SlotFillingMetricsCollector.IntentMetrics> intentMetrics = metricsCollector.getIntentMetrics();
        SlotFillingMetricsCollector.IntentMetrics metrics = intentMetrics.get(intentCode);
        
        // Then
        assertNotNull(metrics);
        assertEquals(3, metrics.getSlotFillingActivated().get());
        assertEquals(12, metrics.getTotalSlotsSum().get()); // 3+5+4
        assertEquals(6, metrics.getMissingSlotsSum().get()); // 2+1+3
        assertEquals(4.0, metrics.getAverageTotalSlots(), 0.001); // 12/3
        assertEquals(2.0, metrics.getAverageMissingSlots(), 0.001); // 6/3
    }
    
    @Test
    void testReset_ShouldClearAllMetrics() {
        // Given
        String intentCode = "ORDER_QUERY";
        metricsCollector.recordQueryStart(intentCode);
        metricsCollector.recordSlotFillingActivated(intentCode, 3, 2);
        metricsCollector.recordClarificationRequired(intentCode, 2, 2);
        
        // When
        metricsCollector.reset();
        
        // Then
        SlotFillingMetricsCollector.SlotFillingMetricsSummary summary = metricsCollector.getMetricsSummary();
        assertEquals(0, summary.getTotalQueriesProcessed());
        assertEquals(0, summary.getQueriesWithSlotFilling());
        assertEquals(0, summary.getQueriesRequiringClarification());
        
        Map<String, SlotFillingMetricsCollector.IntentMetrics> intentMetrics = metricsCollector.getIntentMetrics();
        assertTrue(intentMetrics.isEmpty());
    }
    
    @Test
    void testHandleNullIntentCode_ShouldNotThrowException() {
        // Given & When & Then - 应该不抛出异常
        assertDoesNotThrow(() -> {
            metricsCollector.recordQueryStart(null);
            metricsCollector.recordSlotFillingActivated(null, 3, 2);
            metricsCollector.recordClarificationRequired(null, 2, 2);
            metricsCollector.recordClarificationSuccess(null, 1);
            metricsCollector.recordClarificationTimeout(null, 3);
            metricsCollector.recordRetrievalBlocked(null, "test");
            metricsCollector.recordProcessingTime(null, 100L);
        });
        
        // 全局指标应该正常记录
        SlotFillingMetricsCollector.SlotFillingMetricsSummary summary = metricsCollector.getMetricsSummary();
        assertEquals(1, summary.getTotalQueriesProcessed());
        assertEquals(1, summary.getQueriesWithSlotFilling());
    }
}