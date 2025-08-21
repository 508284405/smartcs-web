package com.leyue.smartcs.rag.transformer.gateway;

import com.leyue.smartcs.model.ai.DynamicModelManager;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.output.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * 查询扩展Gateway实现测试
 * 
 * @author Claude
 */
@ExtendWith(MockitoExtension.class)
class QueryExpansionGatewayImplTest {

    @Mock
    private DynamicModelManager dynamicModelManager;

    @Mock
    private ChatModel chatModel;

    @Mock
    private Response<String> mockResponse;

    private QueryExpansionGatewayImpl gateway;

    @BeforeEach
    void setUp() {
        gateway = new QueryExpansionGatewayImpl(dynamicModelManager);
    }

    @Test
    void testGenerateExpansion() {
        // Given
        String prompt = "生成查询扩展";
        Long modelId = 123L;
        String expectedResult = "扩展查询1\n扩展查询2\n扩展查询3";

        when(dynamicModelManager.getChatModel(modelId)).thenReturn(chatModel);
        when(chatModel.generate(anyString())).thenReturn(mockResponse);
        when(mockResponse.content()).thenReturn(expectedResult);

        // When
        String result = gateway.generateExpansion(prompt, modelId);

        // Then
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test
    void testGenerateExpansionWithException() {
        // Given
        String prompt = "生成查询扩展";
        Long modelId = 123L;

        when(dynamicModelManager.getChatModel(modelId)).thenThrow(new RuntimeException("Model error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> gateway.generateExpansion(prompt, modelId));
    }

    @Test
    void testParseExpandedQueries() {
        // Given
        String expandedText = "查询1\n查询2\n查询3\n查询4";
        int maxQueries = 3;

        // When
        List<String> result = gateway.parseExpandedQueries(expandedText, maxQueries);

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("查询1", result.get(0));
        assertEquals("查询2", result.get(1));
        assertEquals("查询3", result.get(2));
    }

    @Test
    void testParseExpandedQueriesWithNumberedList() {
        // Given
        String expandedText = "1. 第一个查询\n2. 第二个查询\n3) 第三个查询\n4： 第四个查询";
        int maxQueries = 4;

        // When
        List<String> result = gateway.parseExpandedQueries(expandedText, maxQueries);

        // Then
        assertNotNull(result);
        assertEquals(4, result.size());
        assertEquals("第一个查询", result.get(0));
        assertEquals("第二个查询", result.get(1));
        assertEquals("第三个查询", result.get(2));
        assertEquals("第四个查询", result.get(3));
    }

    @Test
    void testParseExpandedQueriesWithBulletPoints() {
        // Given
        String expandedText = "- 查询一\n* 查询二\n• 查询三";
        int maxQueries = 5;

        // When
        List<String> result = gateway.parseExpandedQueries(expandedText, maxQueries);

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("查询一", result.get(0));
        assertEquals("查询二", result.get(1));
        assertEquals("查询三", result.get(2));
    }

    @Test
    void testParseExpandedQueriesWithQuotes() {
        // Given
        String expandedText = "\"查询一\"\n'查询二'\n查询三";
        int maxQueries = 3;

        // When
        List<String> result = gateway.parseExpandedQueries(expandedText, maxQueries);

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("查询一", result.get(0));
        assertEquals("查询二", result.get(1));
        assertEquals("查询三", result.get(2));
    }

    @Test
    void testParseExpandedQueriesWithEmptyText() {
        // Given
        String expandedText = "";
        int maxQueries = 3;

        // When
        List<String> result = gateway.parseExpandedQueries(expandedText, maxQueries);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testParseExpandedQueriesWithNullText() {
        // Given
        String expandedText = null;
        int maxQueries = 3;

        // When
        List<String> result = gateway.parseExpandedQueries(expandedText, maxQueries);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testParseExpandedQueriesWithWhitespaceAndEmptyLines() {
        // Given
        String expandedText = "查询一\n\n  \n查询二\n   \n查询三\n\n";
        int maxQueries = 5;

        // When
        List<String> result = gateway.parseExpandedQueries(expandedText, maxQueries);

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("查询一", result.get(0));
        assertEquals("查询二", result.get(1));
        assertEquals("查询三", result.get(2));
    }

    @Test
    void testParseExpandedQueriesMaxQueriesLimit() {
        // Given
        String expandedText = "1\n2\n3\n4\n5\n6\n7\n8\n9\n10";
        int maxQueries = 3;

        // When
        List<String> result = gateway.parseExpandedQueries(expandedText, maxQueries);

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("1", result.get(0));
        assertEquals("2", result.get(1));
        assertEquals("3", result.get(2));
    }
}