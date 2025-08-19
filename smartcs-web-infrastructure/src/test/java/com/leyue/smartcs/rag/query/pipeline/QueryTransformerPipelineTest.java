package com.leyue.smartcs.rag.query.pipeline;

import com.leyue.smartcs.rag.query.pipeline.stages.NormalizationStage;
import dev.langchain4j.rag.query.Query;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 查询转换器管线测试
 * 
 * @author Claude
 */
class QueryTransformerPipelineTest {
    
    private QueryTransformerPipeline pipeline;
    
    @BeforeEach
    void setUp() {
        // 创建基础配置
        QueryContext.PipelineConfig config = QueryContext.PipelineConfig.builder()
                .enableNormalization(true)
                .enableExpanding(false) // 暂时禁用扩展阶段避免依赖ChatModel
                .maxQueries(10)
                .keepOriginal(true)
                .build();
        
        // 创建管线（仅包含标准化阶段用于测试）
        pipeline = QueryTransformerPipeline.builder()
                .stages(Arrays.asList(new NormalizationStage()))
                .pipelineConfig(config)
                .build();
    }
    
    @Test
    void testBasicTransformation() {
        // 测试基础转换功能
        Query inputQuery = Query.from("  1. 这是一个测试查询  ");
        
        Collection<Query> result = pipeline.transform(inputQuery);
        
        assertNotNull(result);
        assertFalse(result.isEmpty());
        
        // 验证结果包含清理后的查询
        String firstResult = result.iterator().next().text();
        assertEquals("这是一个测试查询", firstResult);
    }
    
    @Test
    void testEmptyQuery() {
        // 测试空查询处理
        Query inputQuery = Query.from("");
        
        Collection<Query> result = pipeline.transform(inputQuery);
        
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("", result.iterator().next().text());
    }
    
    @Test
    void testKeepOriginalQuery() {
        // 测试保留原始查询功能
        Query inputQuery = Query.from("测试查询");
        
        Collection<Query> result = pipeline.transform(inputQuery);
        
        assertNotNull(result);
        assertTrue(result.contains(inputQuery));
    }
    
    @Test
    void testNullInput() {
        // 测试空输入处理 - 应该能正常处理而不抛异常
        assertDoesNotThrow(() -> {
            Collection<Query> result = pipeline.transform(null);
            assertNotNull(result);
        });
    }
    
    @Test
    void testMaxQueriesLimit() {
        // 创建限制最大查询数量的配置
        QueryContext.PipelineConfig limitConfig = QueryContext.PipelineConfig.builder()
                .enableNormalization(true)
                .maxQueries(2)
                .keepOriginal(true)
                .build();
        
        QueryTransformerPipeline limitedPipeline = QueryTransformerPipeline.builder()
                .stages(Arrays.asList(new NormalizationStage()))
                .pipelineConfig(limitConfig)
                .build();
        
        Query inputQuery = Query.from("测试查询");
        Collection<Query> result = limitedPipeline.transform(inputQuery);
        
        assertNotNull(result);
        assertTrue(result.size() <= 2);
    }
}