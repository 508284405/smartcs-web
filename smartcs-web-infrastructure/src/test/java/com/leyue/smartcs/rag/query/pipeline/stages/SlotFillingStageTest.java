package com.leyue.smartcs.rag.query.pipeline.stages;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leyue.smartcs.api.DictionaryService;
import com.leyue.smartcs.dto.intent.SlotDefinitionDTO;
import com.leyue.smartcs.dto.intent.SlotTemplateDTO;
import com.leyue.smartcs.rag.metrics.SlotFillingMetricsCollector;
import com.leyue.smartcs.rag.query.pipeline.QueryContext;
import dev.langchain4j.rag.query.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * SlotFillingStage单元测试
 * 
 * @author Claude
 */
@ExtendWith(MockitoExtension.class)
class SlotFillingStageTest {
    
    @Mock
    private DictionaryService dictionaryService;
    
    @Mock
    private SlotFillingMetricsCollector metricsCollector;
    
    private ObjectMapper objectMapper;
    private SlotFillingStage slotFillingStage;
    private QueryContext queryContext;
    
    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        slotFillingStage = new SlotFillingStage(dictionaryService, objectMapper, metricsCollector);
        
        // 设置QueryContext
        Map<String, Object> attributes = new HashMap<>();
        Map<String, Object> intentResults = new HashMap<>();
        Map<String, Object> intentInfo = new HashMap<>();
        intentInfo.put("intentCode", "ORDER_QUERY");
        intentResults.put("查询我的订单", intentInfo);
        attributes.put("intent_extraction", intentResults);
        
        QueryContext.PipelineConfig pipelineConfig = QueryContext.PipelineConfig.builder()
                .enableSlotFilling(true)
                .slotFillingConfig(QueryContext.SlotFillingConfig.builder()
                        .maxClarificationAttempts(3)
                        .blockRetrievalOnMissing(true)
                        .build())
                .build();
        
        queryContext = QueryContext.builder()
                .originalQuery(Query.from("查询我的订单"))
                .attributes(attributes)
                .pipelineConfig(pipelineConfig)
                .tenant("test_tenant")
                .channel("test_channel")
                .build();
    }
    
    @Test
    void testIsEnabled_WhenSlotFillingEnabled_ShouldReturnTrue() {
        // Given - QueryContext已在setUp中配置为启用槽位填充
        
        // When
        boolean enabled = slotFillingStage.isEnabled(queryContext);
        
        // Then
        assertTrue(enabled);
    }
    
    @Test
    void testIsEnabled_WhenSlotFillingDisabled_ShouldReturnFalse() {
        // Given
        QueryContext.PipelineConfig disabledConfig = QueryContext.PipelineConfig.builder()
                .enableSlotFilling(false)
                .build();
        
        QueryContext disabledContext = QueryContext.builder()
                .originalQuery(Query.from("test"))
                .pipelineConfig(disabledConfig)
                .attributes(new HashMap<>())
                .build();
        
        // When
        boolean enabled = slotFillingStage.isEnabled(disabledContext);
        
        // Then
        assertFalse(enabled);
    }
    
    @Test
    void testApply_WithNoIntentCode_ShouldReturnOriginalQueries() {
        // Given
        Collection<Query> queries = Arrays.asList(Query.from("test query without intent"));
        queryContext.getAttributes().clear(); // 清除意图识别结果
        
        // When
        Collection<Query> result = slotFillingStage.apply(queryContext, queries);
        
        // Then
        assertEquals(1, result.size());
        assertEquals("test query without intent", result.iterator().next().text());
        verifyNoInteractions(dictionaryService);
    }
    
    @Test
    void testApply_WithSlotTemplateNotActive_ShouldReturnOriginalQueries() {
        // Given
        Collection<Query> queries = Arrays.asList(Query.from("查询我的订单"));
        
        SlotTemplateDTO template = SlotTemplateDTO.builder()
                .intentCode("ORDER_QUERY")
                .slotFillingEnabled(false) // 未启用槽位填充
                .build();
        
        when(dictionaryService.getSlotTemplateByIntent(eq("ORDER_QUERY"), anyString(), anyString(), anyString()))
                .thenReturn(template);
        
        // When
        Collection<Query> result = slotFillingStage.apply(queryContext, queries);
        
        // Then
        assertEquals(1, result.size());
        assertEquals("查询我的订单", result.iterator().next().text());
        verify(metricsCollector).recordQueryStart("ORDER_QUERY");
    }
    
    @Test
    void testApply_WithNoMissingSlots_ShouldReturnOriginalQueries() {
        // Given
        Collection<Query> queries = Arrays.asList(Query.from("查询我的订单 订单号ORD123"));
        
        SlotDefinitionDTO orderIdSlot = SlotDefinitionDTO.builder()
                .name("orderId")
                .label("订单号")
                .type("STRING")
                .required(true)
                .examples(Arrays.asList("ORD123", "ORD456"))
                .build();
        
        SlotTemplateDTO template = SlotTemplateDTO.builder()
                .intentCode("ORDER_QUERY")
                .slotFillingEnabled(true)
                .slotDefinitions(Arrays.asList(orderIdSlot))
                .build();
        
        when(dictionaryService.getSlotTemplateByIntent(eq("ORDER_QUERY"), anyString(), anyString(), anyString()))
                .thenReturn(template);
        
        // When
        Collection<Query> result = slotFillingStage.apply(queryContext, queries);
        
        // Then
        assertEquals(1, result.size());
        assertEquals("查询我的订单 订单号ORD123", result.iterator().next().text());
        verify(metricsCollector).recordQueryStart("ORDER_QUERY");
        verify(metricsCollector, times(2)).recordSlotFillingActivated(eq("ORDER_QUERY"), anyInt(), anyInt());
        verify(metricsCollector).recordProcessingTime(eq("ORDER_QUERY"), anyLong());
    }
    
    @Test
    void testApply_WithMissingRequiredSlots_ShouldGenerateClarificationQuestions() {
        // Given
        Collection<Query> queries = Arrays.asList(Query.from("查询我的订单"));
        
        SlotDefinitionDTO orderIdSlot = SlotDefinitionDTO.builder()
                .name("orderId")
                .label("订单号")
                .type("STRING")
                .required(true)
                .hint("请提供您要查询的订单号")
                .examples(Arrays.asList("ORD123", "ORD456"))
                .build();
        
        SlotTemplateDTO template = SlotTemplateDTO.builder()
                .intentCode("ORDER_QUERY")
                .slotFillingEnabled(true)
                .blockRetrievalOnMissing(true)
                .slotDefinitions(Arrays.asList(orderIdSlot))
                .build();
        
        when(dictionaryService.getSlotTemplateByIntent(eq("ORDER_QUERY"), anyString(), anyString(), anyString()))
                .thenReturn(template);
        
        // When
        Collection<Query> result = slotFillingStage.apply(queryContext, queries);
        
        // Then
        assertEquals(1, result.size());
        assertEquals("查询我的订单", result.iterator().next().text());
        
        // 验证上下文中设置了槽位填充信息
        Map<String, Object> slotFillingInfo = (Map<String, Object>) queryContext.getAttributes().get("slot_filling");
        assertNotNull(slotFillingInfo);
        assertTrue((Boolean) slotFillingInfo.get("required"));
        assertEquals("ORDER_QUERY", slotFillingInfo.get("intent"));
        assertTrue((Boolean) slotFillingInfo.get("block_retrieval"));
        
        List<String> missingSlots = (List<String>) slotFillingInfo.get("missing");
        assertEquals(1, missingSlots.size());
        assertEquals("orderId", missingSlots.get(0));
        
        List<String> questions = (List<String>) slotFillingInfo.get("questions");
        assertEquals(1, questions.size());
        assertTrue(questions.get(0).contains("订单号"));
        
        // 验证指标收集
        verify(metricsCollector).recordQueryStart("ORDER_QUERY");
        verify(metricsCollector, times(2)).recordSlotFillingActivated(eq("ORDER_QUERY"), anyInt(), anyInt());
        verify(metricsCollector).recordClarificationRequired("ORDER_QUERY", 1, 1);
        verify(metricsCollector).recordRetrievalBlocked("ORDER_QUERY", "缺失必填槽位");
        verify(metricsCollector).recordProcessingTime(eq("ORDER_QUERY"), anyLong());
    }
    
    @Test
    void testApply_WithMaxClarificationAttemptsReached_ShouldNotRequestClarification() {
        // Given
        Collection<Query> queries = Arrays.asList(Query.from("查询我的订单"));
        
        // 设置已经达到最大澄清次数
        Map<String, Object> slotFillingInfo = new HashMap<>();
        slotFillingInfo.put("clarification_count", 3);
        queryContext.getAttributes().put("slot_filling", slotFillingInfo);
        
        SlotDefinitionDTO orderIdSlot = SlotDefinitionDTO.builder()
                .name("orderId")
                .label("订单号")
                .type("STRING")
                .required(true)
                .build();
        
        SlotTemplateDTO template = SlotTemplateDTO.builder()
                .intentCode("ORDER_QUERY")
                .slotFillingEnabled(true)
                .maxClarificationAttempts(3)
                .slotDefinitions(Arrays.asList(orderIdSlot))
                .build();
        
        when(dictionaryService.getSlotTemplateByIntent(eq("ORDER_QUERY"), anyString(), anyString(), anyString()))
                .thenReturn(template);
        
        // When
        Collection<Query> result = slotFillingStage.apply(queryContext, queries);
        
        // Then
        assertEquals(1, result.size());
        assertEquals("查询我的订单", result.iterator().next().text());
        
        // 验证没有设置新的澄清信息
        Map<String, Object> updatedSlotFillingInfo = (Map<String, Object>) queryContext.getAttributes().get("slot_filling");
        assertFalse(updatedSlotFillingInfo.containsKey("required") && 
                   Boolean.TRUE.equals(updatedSlotFillingInfo.get("required")));
    }
    
    @Test
    void testApply_WithEmptyQueries_ShouldReturnEmptyList() {
        // Given
        Collection<Query> queries = Collections.emptyList();
        
        // When
        Collection<Query> result = slotFillingStage.apply(queryContext, queries);
        
        // Then
        assertTrue(result.isEmpty());
    }
    
    @Test
    void testApply_WithNullQueries_ShouldReturnEmptyList() {
        // Given
        Collection<Query> queries = null;
        
        // When
        Collection<Query> result = slotFillingStage.apply(queryContext, queries);
        
        // Then
        assertTrue(result.isEmpty());
    }
    
    @Test
    void testGetName_ShouldReturnSlotFillingStage() {
        // When
        String name = slotFillingStage.getName();
        
        // Then
        assertEquals("SlotFillingStage", name);
    }
}