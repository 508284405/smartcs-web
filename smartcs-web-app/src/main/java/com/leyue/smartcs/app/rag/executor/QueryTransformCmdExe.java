package com.leyue.smartcs.app.rag.executor;

import com.leyue.smartcs.app.rag.service.QueryTransformationApplicationService;
import com.leyue.smartcs.dto.app.RagComponentConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * 查询转换命令执行器
 * 处理查询转换相关的命令执行
 * 
 * @author Claude
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class QueryTransformCmdExe {
    
    private final QueryTransformationApplicationService applicationService;
    
    /**
     * 执行查询转换
     * 
     * @param originalQuery 原始查询
     * @param config 转换配置
     * @return 转换后的查询集合
     */
    public Collection<String> execute(String originalQuery, RagComponentConfig.QueryTransformerConfig config) {
        log.debug("执行查询转换命令: query={}", originalQuery);
        
        // 参数验证
        validateQuery(originalQuery);
        
        // 调用应用服务
        return applicationService.transformQuery(originalQuery, config);
    }
    
    /**
     * 执行查询转换（使用默认配置）
     * 
     * @param originalQuery 原始查询
     * @return 转换后的查询集合
     */
    public Collection<String> execute(String originalQuery) {
        return execute(originalQuery, null);
    }
    
    /**
     * 执行带意图识别的查询转换
     * 
     * @param originalQuery 原始查询
     * @param modelId 模型ID
     * @return 转换后的查询集合
     */
    public Collection<String> executeWithIntent(String originalQuery, Long modelId) {
        log.debug("执行带意图识别的查询转换命令: query={}, modelId={}", originalQuery, modelId);
        
        validateQuery(originalQuery);
        
        return applicationService.transformQueryWithIntent(originalQuery, modelId);
    }
    
    /**
     * 验证查询参数
     */
    private void validateQuery(String query) {
        if (query == null || query.trim().isEmpty()) {
            throw new IllegalArgumentException("查询内容不能为空");
        }
        
        if (query.length() > 1000) {
            throw new IllegalArgumentException("查询内容过长，最大支持1000字符");
        }
    }
}