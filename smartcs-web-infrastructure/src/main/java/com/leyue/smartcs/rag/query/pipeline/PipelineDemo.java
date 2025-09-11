package com.leyue.smartcs.rag.query.pipeline;

import com.leyue.smartcs.rag.query.pipeline.stages.NormalizationStage;
import dev.langchain4j.rag.query.Query;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Collection;

/**
 * 管线功能演示
 * 用于验证QueryTransformerPipeline的基本功能
 * 
 * @author Claude
 */
@Slf4j
public class PipelineDemo {
    
    public static void main(String[] args) {
        demonstratePipelineFunctionality();
    }
    
    /**
     * 演示管线功能
     */
    public static void demonstratePipelineFunctionality() {
        log.info("开始演示QueryTransformerPipeline功能...");
        
        try {
            // 创建管线配置
            QueryContext.PipelineConfig config = QueryContext.PipelineConfig.builder()
                    .enableNormalization(true)
                    .enableExpanding(false) // 演示中禁用扩展阶段
                    .maxQueries(10)
                    .keepOriginal(true)
                    .dedupThreshold(0.85)
                    .normalizationConfig(QueryContext.NormalizationConfig.builder()
                            .removeStopwords(false)
                            .maxQueryLength(512)
                            .normalizeCase(true)
                            .cleanWhitespace(true)
                            .build())
                    .build();
            
            // 创建管线实例
            QueryTransformerPipeline pipeline = QueryTransformerPipeline.builder()
                    .stages(Arrays.asList(new NormalizationStage()))
                    .pipelineConfig(config)
                    .metricsCollector(createDemoMetricsCollector())
                    .build();
            
            // 测试查询
            String[] testQueries = {
                "  1. 如何使用Java开发Web应用？  ",
                "2. Spring Boot的配置方式有哪些？",
                "   什么是微服务架构？   ",
                "Docker容器化部署的最佳实践",
                ""  // 空查询测试
            };
            
            log.info("开始处理测试查询...");
            
            for (String queryText : testQueries) {
                if (queryText.trim().isEmpty()) {
                    continue; // 跳过空查询
                }
                
                Query inputQuery = Query.from(queryText);
                log.info("原始查询: '{}'", queryText);
                
                // 执行管线转换
                Collection<Query> results = pipeline.transform(inputQuery);
                
                log.info("转换结果: 共{}个查询", results.size());
                int index = 1;
                for (Query result : results) {
                    log.info("  {}. '{}'", index++, result.text());
                }
                log.info("---");
            }
            
            log.info("管线功能演示完成！");
            
        } catch (Exception e) {
            log.error("管线功能演示失败", e);
        }
    }
    
    /**
     * 创建演示用的指标收集器
     */
    private static QueryContext.MetricsCollector createDemoMetricsCollector() {
        return new QueryContext.MetricsCollector() {
            @Override
            public void recordStageStart(String stageName, int inputQueryCount) {
                log.debug("📊 阶段开始: {} (输入查询数: {})", stageName, inputQueryCount);
            }
            
            @Override
            public void recordStageComplete(String stageName, int outputQueryCount, long elapsedMs) {
                log.info("✅ 阶段完成: {} (输出查询数: {}, 耗时: {}ms)", 
                        stageName, outputQueryCount, elapsedMs);
            }
            
            @Override
            public void recordStageFailure(String stageName, Throwable error, long elapsedMs) {
                log.warn("❌ 阶段失败: {} (错误: {}, 耗时: {}ms)", 
                        stageName, error.getMessage(), elapsedMs);
            }
            
            @Override
            public void recordStageSkipped(String stageName, String reason) {
                log.info("⏭️ 阶段跳过: {} (原因: {})", stageName, reason);
            }
            
            @Override
            public void recordTokensConsumption(String stageName, int inputTokens, int outputTokens) {
                log.debug("🪙 Token消耗: {} (输入: {}, 输出: {})", 
                        stageName, inputTokens, outputTokens);
            }
            
            @Override
            public void recordCostConsumption(String stageName, double cost) {
                log.debug("💰 成本消耗: {} (费用: ${:.4f})", stageName, cost);
            }
        };
    }
}