package com.leyue.smartcs.bot.advisor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FusionQuestionAnswerAdvisorTest {

    @Mock
    private VectorStore vectorStore;

    private FusionQuestionAnswerAdvisor fusionAdvisor;

    @BeforeEach
    void setUp() {
        fusionAdvisor = new FusionQuestionAnswerAdvisor(vectorStore,null,null);
    }

    @Test
    void testPerformFusionSearch_WithValidQuery() {
        // 准备测试数据
        String query = "如何使用Spring AI";
        
        Document doc1 = new Document("1", "Spring AI是一个用于构建AI应用的框架", Collections.emptyMap());
        Document doc2 = new Document("2", "Spring AI提供了与大语言模型集成的能力", Collections.emptyMap());
        Document doc3 = new Document("3", "使用Spring AI可以快速构建聊天机器人", Collections.emptyMap());
        
        // 模拟向量检索结果
        when(vectorStore.similaritySearch(any(SearchRequest.class)))
                .thenReturn(Arrays.asList(doc1, doc2))
                .thenReturn(Arrays.asList(doc2, doc3))
                .thenReturn(Arrays.asList(doc1, doc3))
                .thenReturn(Arrays.asList(doc1))
                .thenReturn(Arrays.asList(doc2));

        // 执行测试
        String result = fusionAdvisor.performFusionSearch(query);

        // 验证结果
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.contains("Spring AI"));
        
        // 验证向量检索被调用了多次（对应多个query变体）
        verify(vectorStore, times(5)).similaritySearch(any(SearchRequest.class));
    }

    @Test
    void testPerformFusionSearch_WithEmptyQuery() {
        // 执行测试
        String result = fusionAdvisor.performFusionSearch("");

        // 验证结果
        assertEquals("", result);
        
        // 验证向量检索未被调用
        verify(vectorStore, never()).similaritySearch(any(SearchRequest.class));
    }

    @Test
    void testPerformFusionSearch_WithNullQuery() {
        // 执行测试
        String result = fusionAdvisor.performFusionSearch(null);

        // 验证结果
        assertEquals("", result);
        
        // 验证向量检索未被调用
        verify(vectorStore, never()).similaritySearch(any(SearchRequest.class));
    }

    @Test
    void testPerformFusionSearch_WithNoResults() {
        // 准备测试数据
        String query = "不存在的查询";
        
        // 模拟向量检索返回空结果
        when(vectorStore.similaritySearch(any(SearchRequest.class)))
                .thenReturn(Collections.emptyList());

        // 执行测试
        String result = fusionAdvisor.performFusionSearch(query);

        // 验证结果
        assertEquals("", result);
        
        // 验证向量检索被调用了多次
        verify(vectorStore, times(5)).similaritySearch(any(SearchRequest.class));
    }

    @Test
    void testPerformFusionSearch_WithException() {
        // 准备测试数据
        String query = "测试异常";
        
        // 模拟向量检索抛出异常
        when(vectorStore.similaritySearch(any(SearchRequest.class)))
                .thenThrow(new RuntimeException("Vector search failed"));

        // 执行测试
        String result = fusionAdvisor.performFusionSearch(query);

        // 验证结果
        assertEquals("", result);
        
        // 验证向量检索被调用了
        verify(vectorStore, atLeastOnce()).similaritySearch(any(SearchRequest.class));
    }

    @Test
    void testPerformFusionSearch_WithCustomConfig() {
        // 准备自定义配置
        FusionQuestionAnswerAdvisor.FusionConfig config = new FusionQuestionAnswerAdvisor.FusionConfig();
        config.setMaxQueries(3);
        config.setTopK(2);
        config.setMaxFusedDocuments(5);
        config.setMaxContextTokens(1000);
        
        FusionQuestionAnswerAdvisor customAdvisor = new FusionQuestionAnswerAdvisor(vectorStore, null,config);
        
        // 准备测试数据
        String query = "自定义配置测试";
        
        Document doc1 = new Document("1", "测试文档1", Collections.emptyMap());
        Document doc2 = new Document("2", "测试文档2", Collections.emptyMap());
        
        // 模拟向量检索结果
        when(vectorStore.similaritySearch(any(SearchRequest.class)))
                .thenReturn(Arrays.asList(doc1, doc2));

        // 执行测试
        String result = customAdvisor.performFusionSearch(query);

        // 验证结果
        assertNotNull(result);
        
        // 验证向量检索被调用了3次（对应maxQueries=3）
        verify(vectorStore, times(3)).similaritySearch(any(SearchRequest.class));
    }

    @Test
    void testFusionConfig_DefaultValues() {
        FusionQuestionAnswerAdvisor.FusionConfig config = new FusionQuestionAnswerAdvisor.FusionConfig();
        
        assertEquals(5, config.getMaxQueries());
        assertEquals(3, config.getTopK());
        assertEquals(0.7, config.getSimilarityThreshold());
        assertEquals(10, config.getMaxFusedDocuments());
        assertEquals(4000, config.getMaxContextTokens());
    }

    @Test
    void testFusionConfig_SettersAndGetters() {
        FusionQuestionAnswerAdvisor.FusionConfig config = new FusionQuestionAnswerAdvisor.FusionConfig();
        
        config.setMaxQueries(10);
        config.setTopK(5);
        config.setSimilarityThreshold(0.8);
        config.setMaxFusedDocuments(15);
        config.setMaxContextTokens(8000);
        
        assertEquals(10, config.getMaxQueries());
        assertEquals(5, config.getTopK());
        assertEquals(0.8, config.getSimilarityThreshold());
        assertEquals(15, config.getMaxFusedDocuments());
        assertEquals(8000, config.getMaxContextTokens());
    }
} 