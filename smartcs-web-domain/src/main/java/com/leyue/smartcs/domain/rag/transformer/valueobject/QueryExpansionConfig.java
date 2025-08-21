package com.leyue.smartcs.domain.rag.transformer.valueobject;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 查询扩展配置值对象
 * 包含查询扩展相关的配置参数和业务验证
 * 
 * @author Claude
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class QueryExpansionConfig {
    
    private final int maxQueries;
    private final boolean intentRecognitionEnabled;
    private final String defaultChannel;
    private final String defaultTenant;
    private final Long modelId;
    private final String customPromptTemplate;
    
    /**
     * 创建查询扩展配置
     */
    public static QueryExpansionConfig create(int maxQueries, boolean intentRecognitionEnabled, 
                                            String defaultChannel, String defaultTenant, 
                                            Long modelId, String customPromptTemplate) {
        validateMaxQueries(maxQueries);
        validateChannel(defaultChannel);
        validateTenant(defaultTenant);
        
        return new QueryExpansionConfig(
            maxQueries, 
            intentRecognitionEnabled, 
            defaultChannel, 
            defaultTenant, 
            modelId, 
            customPromptTemplate
        );
    }
    
    /**
     * 创建默认配置
     */
    public static QueryExpansionConfig createDefault() {
        return new QueryExpansionConfig(5, false, "web", "default", null, null);
    }
    
    /**
     * 创建启用意图识别的默认配置
     */
    public static QueryExpansionConfig createWithIntentRecognition() {
        return new QueryExpansionConfig(5, true, "web", "default", null, null);
    }
    
    /**
     * 验证最大查询数量
     */
    private static void validateMaxQueries(int maxQueries) {
        if (maxQueries < 1 || maxQueries > 10) {
            throw new IllegalArgumentException("查询扩展数量必须在1-10之间，当前值: " + maxQueries);
        }
    }
    
    /**
     * 验证渠道
     */
    private static void validateChannel(String channel) {
        if (channel == null || channel.trim().isEmpty()) {
            throw new IllegalArgumentException("默认渠道不能为空");
        }
    }
    
    /**
     * 验证租户
     */
    private static void validateTenant(String tenant) {
        if (tenant == null || tenant.trim().isEmpty()) {
            throw new IllegalArgumentException("默认租户不能为空");
        }
    }
    
    /**
     * 是否有自定义提示模板
     */
    public boolean hasCustomPromptTemplate() {
        return customPromptTemplate != null && !customPromptTemplate.trim().isEmpty();
    }
    
    /**
     * 是否有指定模型ID
     */
    public boolean hasModelId() {
        return modelId != null;
    }
    
    /**
     * 是否为基础配置（不启用意图识别）
     */
    public boolean isBasicConfig() {
        return !intentRecognitionEnabled;
    }
    
    @Override
    public String toString() {
        return String.format("QueryExpansionConfig{maxQueries=%d, intentEnabled=%s, channel='%s', tenant='%s', modelId=%s}", 
                           maxQueries, intentRecognitionEnabled, defaultChannel, defaultTenant, modelId);
    }
}