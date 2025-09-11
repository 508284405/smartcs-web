package com.leyue.smartcs.domain.rag.transformer.entity;

import com.leyue.smartcs.domain.rag.transformer.strategy.QueryExpansionStrategy;
import com.leyue.smartcs.domain.rag.transformer.valueobject.IntentAnalysisResult;
import com.leyue.smartcs.domain.rag.transformer.valueobject.QueryExpansionConfig;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 查询转换上下文聚合根
 * 管理整个查询转换过程的状态和生命周期
 * 
 * @author Claude
 */
@Slf4j
@Getter
public class QueryTransformationContext {
    
    private final String originalQuery;
    private final QueryExpansionConfig config;
    private final long createdTime;
    
    private IntentAnalysisResult intentResult;
    private QueryExpansionStrategy strategy;
    private List<String> expandedQueries;
    private TransformationStatus status;
    private String errorMessage;
    private long completedTime;
    
    public enum TransformationStatus {
        CREATED,       // 已创建
        ANALYZING,     // 意图分析中
        EXPANDING,     // 查询扩展中
        COMPLETED,     // 已完成
        FAILED         // 失败
    }
    
    private QueryTransformationContext(String originalQuery, QueryExpansionConfig config) {
        this.originalQuery = originalQuery;
        this.config = config;
        this.createdTime = System.currentTimeMillis();
        this.status = TransformationStatus.CREATED;
        
        validateOriginalQuery(originalQuery);
        validateConfig(config);
    }
    
    /**
     * 创建查询转换上下文
     */
    public static QueryTransformationContext create(String originalQuery, QueryExpansionConfig config) {
        return new QueryTransformationContext(originalQuery, config);
    }
    
    /**
     * 设置意图分析结果
     */
    public void setIntentResult(IntentAnalysisResult intentResult) {
        if (this.status != TransformationStatus.CREATED) {
            throw new IllegalStateException("只能在CREATED状态下设置意图分析结果");
        }
        
        this.intentResult = intentResult;
        this.status = TransformationStatus.ANALYZING;
        
        log.debug("设置意图分析结果: context={}, intent={}", this.getId(), intentResult.getIntentCode());
    }
    
    /**
     * 设置扩展策略
     */
    public void setStrategy(QueryExpansionStrategy strategy) {
        if (this.status != TransformationStatus.ANALYZING) {
            throw new IllegalStateException("只能在ANALYZING状态下设置扩展策略");
        }
        
        this.strategy = strategy;
        this.status = TransformationStatus.EXPANDING;
        
        log.debug("设置扩展策略: context={}, strategy={}", this.getId(), strategy.getStrategyName());
    }
    
    /**
     * 设置扩展查询结果
     */
    public void setExpandedQueries(List<String> expandedQueries) {
        if (expandedQueries == null || expandedQueries.isEmpty()) {
            throw new IllegalArgumentException("扩展查询结果不能为空");
        }
        
        this.expandedQueries = List.copyOf(expandedQueries);
        
        log.debug("设置扩展查询结果: context={}, count={}", this.getId(), expandedQueries.size());
    }
    
    /**
     * 标记转换完成
     */
    public void markCompleted() {
        if (this.expandedQueries == null || this.expandedQueries.isEmpty()) {
            throw new IllegalStateException("必须先设置扩展查询结果才能标记完成");
        }
        
        this.status = TransformationStatus.COMPLETED;
        this.completedTime = System.currentTimeMillis();
        
        log.debug("查询转换完成: context={}, duration={}ms", 
                 this.getId(), this.completedTime - this.createdTime);
    }
    
    /**
     * 标记转换失败
     */
    public void markFailed(String errorMessage) {
        this.status = TransformationStatus.FAILED;
        this.errorMessage = errorMessage;
        this.completedTime = System.currentTimeMillis();
        
        log.warn("查询转换失败: context={}, error={}", this.getId(), errorMessage);
    }
    
    /**
     * 是否转换完成
     */
    public boolean isCompleted() {
        return status == TransformationStatus.COMPLETED;
    }
    
    /**
     * 是否转换失败
     */
    public boolean isFailed() {
        return status == TransformationStatus.FAILED;
    }
    
    /**
     * 是否启用了意图识别
     */
    public boolean isIntentRecognitionEnabled() {
        return config.isIntentRecognitionEnabled();
    }
    
    /**
     * 获取转换耗时
     */
    public long getDuration() {
        if (completedTime > 0) {
            return completedTime - createdTime;
        }
        return System.currentTimeMillis() - createdTime;
    }
    
    /**
     * 获取上下文唯一标识
     */
    public String getId() {
        return String.format("QTC_%d_%s", createdTime, Integer.toHexString(originalQuery.hashCode()));
    }
    
    /**
     * 获取转换摘要信息
     */
    public String getSummary() {
        return String.format("QueryTransformationContext{id=%s, status=%s, intent=%s, strategy=%s, queries=%d, duration=%dms}", 
                           getId(), status, 
                           intentResult != null ? intentResult.getIntentCode() : "N/A",
                           strategy != null ? strategy.getStrategyName() : "N/A",
                           expandedQueries != null ? expandedQueries.size() : 0,
                           getDuration());
    }
    
    /**
     * 验证原始查询
     */
    private void validateOriginalQuery(String originalQuery) {
        if (originalQuery == null || originalQuery.trim().isEmpty()) {
            throw new IllegalArgumentException("原始查询不能为空");
        }
    }
    
    /**
     * 验证配置
     */
    private void validateConfig(QueryExpansionConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("查询扩展配置不能为空");
        }
    }
    
    @Override
    public String toString() {
        return getSummary();
    }
}