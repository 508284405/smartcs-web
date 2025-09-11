package com.leyue.smartcs.app.rag.service;

import com.leyue.smartcs.domain.rag.transformer.domainservice.QueryTransformationDomainService;
import com.leyue.smartcs.domain.rag.transformer.entity.QueryTransformationContext;
import com.leyue.smartcs.domain.rag.transformer.valueobject.QueryExpansionConfig;
import com.leyue.smartcs.dto.app.RagComponentConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * 查询转换应用服务测试
 * 
 * @author Claude
 */
@ExtendWith(MockitoExtension.class)
class QueryTransformationApplicationServiceTest {

    @Mock
    private QueryTransformationDomainService domainService;

    private QueryTransformationApplicationService applicationService;

    @BeforeEach
    void setUp() {
        applicationService = new QueryTransformationApplicationService(domainService);
    }

    @Test
    void testTransformQueryWithConfig() {
        // Given
        String originalQuery = "如何使用产品";
        RagComponentConfig.QueryTransformerConfig config = RagComponentConfig.QueryTransformerConfig.builder()
                .n(3)
                .intentRecognitionEnabled(true)
                .defaultChannel("web")
                .defaultTenant("default")
                .build();

        List<String> expandedQueries = Arrays.asList(
                "如何使用产品",
                "产品使用方法",
                "产品操作指南"
        );

        QueryTransformationContext mockContext = createMockContext(originalQuery, expandedQueries);
        when(domainService.transformQuery(eq(originalQuery), any(QueryExpansionConfig.class)))
                .thenReturn(mockContext);

        // When
        Collection<String> result = applicationService.transformQuery(originalQuery, config);

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.containsAll(expandedQueries));
    }

    @Test
    void testTransformQueryWithDefaultConfig() {
        // Given
        String originalQuery = "产品价格";
        List<String> expandedQueries = Arrays.asList(
                "产品价格",
                "价格信息",
                "费用标准"
        );

        QueryTransformationContext mockContext = createMockContext(originalQuery, expandedQueries);
        when(domainService.transformQuery(eq(originalQuery), any(QueryExpansionConfig.class)))
                .thenReturn(mockContext);

        // When
        Collection<String> result = applicationService.transformQuery(originalQuery);

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.contains(originalQuery));
    }

    @Test
    void testTransformQueryWithIntent() {
        // Given
        String originalQuery = "系统故障";
        Long modelId = 123L;
        List<String> expandedQueries = Arrays.asList(
                "系统故障",
                "系统错误",
                "技术问题",
                "故障排除"
        );

        QueryTransformationContext mockContext = createMockContext(originalQuery, expandedQueries);
        when(domainService.transformQuery(eq(originalQuery), any(QueryExpansionConfig.class)))
                .thenReturn(mockContext);

        // When
        Collection<String> result = applicationService.transformQueryWithIntent(originalQuery, modelId);

        // Then
        assertNotNull(result);
        assertEquals(4, result.size());
        assertTrue(result.contains(originalQuery));
    }

    @Test
    void testTransformQueryWithException() {
        // Given
        String originalQuery = "测试查询";
        when(domainService.transformQuery(eq(originalQuery), any(QueryExpansionConfig.class)))
                .thenThrow(new RuntimeException("Domain service error"));

        // When
        Collection<String> result = applicationService.transformQuery(originalQuery);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.contains(originalQuery));
    }

    @Test
    void testGetTransformationContext() {
        // Given
        String originalQuery = "上下文测试";
        RagComponentConfig.QueryTransformerConfig config = RagComponentConfig.QueryTransformerConfig.builder()
                .n(2)
                .intentRecognitionEnabled(false)
                .build();

        List<String> expandedQueries = Arrays.asList("上下文测试", "测试查询");
        QueryTransformationContext mockContext = createMockContext(originalQuery, expandedQueries);
        when(domainService.transformQuery(eq(originalQuery), any(QueryExpansionConfig.class)))
                .thenReturn(mockContext);

        // When
        QueryTransformationContext result = applicationService.getTransformationContext(originalQuery, config);

        // Then
        assertNotNull(result);
        assertEquals(originalQuery, result.getOriginalQuery());
        assertEquals(2, result.getExpandedQueries().size());
    }

    private QueryTransformationContext createMockContext(String originalQuery, List<String> expandedQueries) {
        QueryExpansionConfig config = QueryExpansionConfig.createDefault();
        QueryTransformationContext context = QueryTransformationContext.create(originalQuery, config);
        context.setExpandedQueries(expandedQueries);
        context.markCompleted();
        return context;
    }
}