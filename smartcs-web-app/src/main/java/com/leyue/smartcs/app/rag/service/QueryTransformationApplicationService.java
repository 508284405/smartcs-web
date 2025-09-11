package com.leyue.smartcs.app.rag.service;

import com.leyue.smartcs.domain.rag.transformer.domainservice.QueryTransformationDomainService;
import com.leyue.smartcs.domain.rag.transformer.entity.QueryTransformationContext;
import com.leyue.smartcs.domain.rag.transformer.valueobject.QueryExpansionConfig;
import com.leyue.smartcs.dto.app.RagComponentConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

/**
 * 查询转换应用服务
 * 编排查询转换的应用层逻辑，处理DTO转换和事务边界
 * 
 * @author Claude
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class QueryTransformationApplicationService {
    
    private final QueryTransformationDomainService domainService;
    
    /**
     * 转换查询
     * 
     * @param originalQuery 原始查询
     * @param config 查询转换器配置
     * @return 转换后的查询列表
     */
    public Collection<String> transformQuery(String originalQuery, RagComponentConfig.QueryTransformerConfig config) {
        log.debug("应用服务开始查询转换: query={}", originalQuery);
        
        try {
            // 1. DTO转换：Client层配置 -> Domain层值对象
            QueryExpansionConfig domainConfig = convertToDomainConfig(config);
            
            // 2. 调用领域服务执行转换
            QueryTransformationContext context = domainService.transformQuery(originalQuery, domainConfig);
            
            // 3. 返回转换结果
            List<String> result = context.getExpandedQueries();
            
            log.info("应用服务查询转换完成: originalQuery={}, expandedCount={}, duration={}ms", 
                    originalQuery, result.size(), context.getDuration());
            
            return result;
            
        } catch (Exception e) {
            log.error("应用服务查询转换失败: query={}", originalQuery, e);
            // 应用层异常处理：确保至少返回原始查询
            return List.of(originalQuery);
        }
    }
    
    /**
     * 转换查询（使用默认配置）
     * 
     * @param originalQuery 原始查询
     * @return 转换后的查询列表
     */
    public Collection<String> transformQuery(String originalQuery) {
        return transformQuery(originalQuery, createDefaultConfig());
    }
    
    /**
     * 转换查询（启用意图识别）
     * 
     * @param originalQuery 原始查询
     * @param modelId 模型ID
     * @return 转换后的查询列表
     */
    public Collection<String> transformQueryWithIntent(String originalQuery, Long modelId) {
        RagComponentConfig.QueryTransformerConfig config = createDefaultConfig();
        config.setIntentRecognitionEnabled(true);
        config.setModelId(modelId);
        
        return transformQuery(originalQuery, config);
    }
    
    /**
     * 获取转换上下文（用于调试和监控）
     * 
     * @param originalQuery 原始查询
     * @param config 查询转换器配置
     * @return 查询转换上下文
     */
    public QueryTransformationContext getTransformationContext(String originalQuery, 
                                                               RagComponentConfig.QueryTransformerConfig config) {
        log.debug("获取查询转换上下文: query={}", originalQuery);
        
        QueryExpansionConfig domainConfig = convertToDomainConfig(config);
        return domainService.transformQuery(originalQuery, domainConfig);
    }
    
    /**
     * 将Client层配置转换为Domain层值对象
     */
    private QueryExpansionConfig convertToDomainConfig(RagComponentConfig.QueryTransformerConfig clientConfig) {
        if (clientConfig == null) {
            return QueryExpansionConfig.createDefault();
        }
        
        return QueryExpansionConfig.create(
            clientConfig.getN() != null ? clientConfig.getN() : 5,
            clientConfig.getIntentRecognitionEnabled() != null ? clientConfig.getIntentRecognitionEnabled() : false,
            clientConfig.getDefaultChannel() != null ? clientConfig.getDefaultChannel() : "web",
            clientConfig.getDefaultTenant() != null ? clientConfig.getDefaultTenant() : "default",
            clientConfig.getModelId(),
            clientConfig.getPromptTemplate()
        );
    }
    
    /**
     * 创建默认配置
     */
    private RagComponentConfig.QueryTransformerConfig createDefaultConfig() {
        return RagComponentConfig.QueryTransformerConfig.builder()
                .n(5)
                .intentRecognitionEnabled(false)
                .defaultChannel("web")
                .defaultTenant("default")
                .build();
    }
}