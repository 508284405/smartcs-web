package com.leyue.smartcs.domain.rag.transformer.domainservice;

import com.leyue.smartcs.domain.intent.domainservice.ClassificationDomainService;
import com.leyue.smartcs.domain.rag.transformer.entity.QueryTransformationContext;
import com.leyue.smartcs.domain.rag.transformer.gateway.QueryExpansionGateway;
import com.leyue.smartcs.domain.rag.transformer.strategy.QueryExpansionStrategy;
import com.leyue.smartcs.domain.rag.transformer.valueobject.IntentAnalysisResult;
import com.leyue.smartcs.domain.rag.transformer.valueobject.QueryExpansionConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 查询转换领域服务
 * 包含查询转换的核心业务逻辑
 * 
 * @author Claude
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QueryTransformationDomainService {
    
    private final ClassificationDomainService classificationDomainService;
    private final IntentBasedStrategySelector strategySelector;
    private final QueryExpansionGateway queryExpansionGateway;
    
    /**
     * 执行完整的查询转换流程
     * 
     * @param originalQuery 原始查询
     * @param config 扩展配置
     * @return 查询转换上下文
     */
    public QueryTransformationContext transformQuery(String originalQuery, QueryExpansionConfig config) {
        log.debug("开始查询转换: query={}, config={}", originalQuery, config);
        
        // 创建转换上下文
        QueryTransformationContext context = QueryTransformationContext.create(originalQuery, config);
        
        try {
            // 1. 执行意图分析（如果启用）
            if (config.isIntentRecognitionEnabled()) {
                IntentAnalysisResult intentResult = performIntentAnalysis(originalQuery, config);
                context.setIntentResult(intentResult);
            } else {
                context.setIntentResult(IntentAnalysisResult.createDefault());
            }
            
            // 2. 选择扩展策略
            QueryExpansionStrategy strategy = strategySelector.selectStrategy(context.getIntentResult());
            context.setStrategy(strategy);
            
            // 3. 执行查询扩展
            List<String> expandedQueries = executeQueryExpansion(context);
            context.setExpandedQueries(expandedQueries);
            
            // 4. 标记转换完成
            context.markCompleted();
            
            log.info("查询转换完成: originalQuery={}, intent={}, strategy={}, expandedCount={}", 
                    originalQuery, context.getIntentResult().getIntentCode(), 
                    strategy.getStrategyName(), expandedQueries.size());
            
            return context;
            
        } catch (Exception e) {
            log.error("查询转换失败: query={}", originalQuery, e);
            context.markFailed(e.getMessage());
            
            // 降级处理：返回基础扩展结果
            return performFallbackTransformation(context);
        }
    }
    
    /**
     * 执行意图分析
     */
    private IntentAnalysisResult performIntentAnalysis(String query, QueryExpansionConfig config) {
        try {
            log.debug("执行意图分析: query={}", query);
            
            Map<String, Object> classificationResult = classificationDomainService.classifyUserInput(
                query, config.getDefaultChannel(), config.getDefaultTenant());
            
            return IntentAnalysisResult.create(
                (String) classificationResult.get("intent_code"),
                (String) classificationResult.get("catalog_code"),
                (Double) classificationResult.get("confidence_score"),
                (String) classificationResult.get("reasoning")
            );
            
        } catch (Exception e) {
            log.warn("意图分析失败: query={}", query, e);
            return IntentAnalysisResult.createDefault();
        }
    }
    
    /**
     * 执行查询扩展
     */
    private List<String> executeQueryExpansion(QueryTransformationContext context) {
        QueryExpansionStrategy strategy = context.getStrategy();
        
        // 检查是否需要跳过扩展
        if (strategy.shouldSkipExpansion()) {
            log.debug("策略要求跳过扩展: strategy={}", strategy.getStrategyName());
            return Collections.singletonList(context.getOriginalQuery());
        }
        
        try {
            // 构建扩展提示词
            String prompt = strategy.buildExpansionPrompt(context.getOriginalQuery(), context.getIntentResult());
            
            // 调用LLM生成扩展查询
            Long modelId = context.getConfig().hasModelId() ? context.getConfig().getModelId() : null;
            String expandedText = queryExpansionGateway.generateExpansion(prompt, modelId);
            
            // 解析扩展结果
            List<String> expandedQueries = queryExpansionGateway.parseExpandedQueries(
                expandedText, strategy.getMaxQueries());
            
            // 确保至少包含原始查询
            if (expandedQueries.isEmpty()) {
                expandedQueries = Collections.singletonList(context.getOriginalQuery());
            } else if (!expandedQueries.contains(context.getOriginalQuery())) {
                expandedQueries.add(0, context.getOriginalQuery());
            }
            
            return expandedQueries;
            
        } catch (Exception e) {
            log.warn("查询扩展失败: strategy={}, query={}", 
                    strategy.getStrategyName(), context.getOriginalQuery(), e);
            return Collections.singletonList(context.getOriginalQuery());
        }
    }
    
    /**
     * 降级处理：执行基础查询扩展
     */
    private QueryTransformationContext performFallbackTransformation(QueryTransformationContext context) {
        log.debug("执行降级查询转换: query={}", context.getOriginalQuery());
        
        try {
            // 使用基础扩展策略
            String basicPrompt = String.format("""
                请为以下查询生成%d个相关的查询变体，每个查询占一行：
                
                原始查询: %s
                """, 
                Math.max(1, context.getConfig().getMaxQueries() - 1),
                context.getOriginalQuery()
            );
            
            Long modelId = context.getConfig().hasModelId() ? context.getConfig().getModelId() : null;
            String expandedText = queryExpansionGateway.generateExpansion(basicPrompt, modelId);
            
            List<String> queries = queryExpansionGateway.parseExpandedQueries(
                expandedText, context.getConfig().getMaxQueries() - 1);
            
            // 添加原始查询到开头
            queries.add(0, context.getOriginalQuery());
            context.setExpandedQueries(queries);
            
            log.debug("降级查询转换完成: expandedCount={}", queries.size());
            
        } catch (Exception e) {
            log.error("降级查询转换也失败，返回原始查询: query={}", context.getOriginalQuery(), e);
            context.setExpandedQueries(Collections.singletonList(context.getOriginalQuery()));
        }
        
        return context;
    }
}