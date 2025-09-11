package com.leyue.smartcs.domain.dictionary.valueobject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

/**
 * 意图目录值对象
 * 封装意图分类信息，用于意图识别和处理
 * 
 * 应用场景：
 * - 意图识别阶段的分类目录
 * - 查询类型判断
 * - 意图权重和优先级管理
 * - 意图处理策略配置
 * 
 * @author Claude
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IntentCatalog {
    
    /**
     * 意图ID
     */
    private String intentId;
    
    /**
     * 意图名称
     */
    private String intentName;
    
    /**
     * 意图描述
     */
    private String description;
    
    /**
     * 父意图ID（用于构建意图层级结构）
     */
    private String parentIntentId;
    
    /**
     * 意图类型：QUERY(查询), COMMAND(命令), CONVERSATION(对话)
     */
    @Builder.Default
    private String intentType = "QUERY";
    
    /**
     * 意图权重（影响匹配优先级）
     */
    @Builder.Default
    private Double weight = 1.0;
    
    /**
     * 是否启用
     */
    @Builder.Default
    private Boolean enabled = true;
    
    /**
     * 关键词列表（用于意图匹配）
     */
    @Builder.Default
    private Set<String> keywords = new HashSet<>();
    
    /**
     * 实体类型列表（该意图可能涉及的实体类型）
     */
    @Builder.Default
    private Set<String> entityTypes = new HashSet<>();
    
    /**
     * 查询模式列表（用于模式匹配）
     */
    @Builder.Default
    private List<String> queryPatterns = new ArrayList<>();
    
    /**
     * 处理器ID（用于指定处理逻辑）
     */
    private String handlerId;
    
    /**
     * 扩展属性（灵活配置）
     */
    @Builder.Default
    private Map<String, Object> properties = new HashMap<>();
    
    /**
     * 创建意图目录
     * 
     * @param intentId 意图ID
     * @param intentName 意图名称
     * @param description 意图描述
     * @param intentType 意图类型
     * @return 意图目录对象
     */
    public static IntentCatalog of(String intentId, String intentName, String description, String intentType) {
        validateParams(intentId, intentName);
        
        return IntentCatalog.builder()
                .intentId(intentId.trim())
                .intentName(intentName.trim())
                .description(description != null ? description.trim() : null)
                .intentType(intentType != null ? intentType : "QUERY")
                .build();
    }
    
    /**
     * 创建查询类型意图目录
     * 
     * @param intentId 意图ID
     * @param intentName 意图名称
     * @param description 意图描述
     * @return 意图目录对象
     */
    public static IntentCatalog ofQuery(String intentId, String intentName, String description) {
        return of(intentId, intentName, description, "QUERY");
    }
    
    /**
     * 创建命令类型意图目录
     * 
     * @param intentId 意图ID
     * @param intentName 意图名称
     * @param description 意图描述
     * @return 意图目录对象
     */
    public static IntentCatalog ofCommand(String intentId, String intentName, String description) {
        return of(intentId, intentName, description, "COMMAND");
    }
    
    /**
     * 添加关键词
     * 
     * @param keyword 关键词
     * @return this
     */
    public IntentCatalog addKeyword(String keyword) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            this.keywords.add(keyword.trim().toLowerCase());
        }
        return this;
    }
    
    /**
     * 批量添加关键词
     * 
     * @param keywords 关键词列表
     * @return this
     */
    public IntentCatalog addKeywords(Collection<String> keywords) {
        if (keywords != null) {
            keywords.forEach(this::addKeyword);
        }
        return this;
    }
    
    /**
     * 添加实体类型
     * 
     * @param entityType 实体类型
     * @return this
     */
    public IntentCatalog addEntityType(String entityType) {
        if (entityType != null && !entityType.trim().isEmpty()) {
            this.entityTypes.add(entityType.trim().toUpperCase());
        }
        return this;
    }
    
    /**
     * 批量添加实体类型
     * 
     * @param entityTypes 实体类型列表
     * @return this
     */
    public IntentCatalog addEntityTypes(Collection<String> entityTypes) {
        if (entityTypes != null) {
            entityTypes.forEach(this::addEntityType);
        }
        return this;
    }
    
    /**
     * 添加查询模式
     * 
     * @param pattern 查询模式
     * @return this
     */
    public IntentCatalog addQueryPattern(String pattern) {
        if (pattern != null && !pattern.trim().isEmpty()) {
            this.queryPatterns.add(pattern.trim());
        }
        return this;
    }
    
    /**
     * 批量添加查询模式
     * 
     * @param patterns 查询模式列表
     * @return this
     */
    public IntentCatalog addQueryPatterns(Collection<String> patterns) {
        if (patterns != null) {
            patterns.forEach(this::addQueryPattern);
        }
        return this;
    }
    
    /**
     * 设置扩展属性
     * 
     * @param key 属性键
     * @param value 属性值
     * @return this
     */
    public IntentCatalog setProperty(String key, Object value) {
        if (key != null && !key.trim().isEmpty()) {
            this.properties.put(key.trim(), value);
        }
        return this;
    }
    
    /**
     * 获取扩展属性
     * 
     * @param key 属性键
     * @param defaultValue 默认值
     * @param <T> 值类型
     * @return 属性值
     */
    @SuppressWarnings("unchecked")
    public <T> T getProperty(String key, T defaultValue) {
        Object value = this.properties.get(key);
        if (value != null) {
            try {
                return (T) value;
            } catch (ClassCastException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }
    
    /**
     * 检查是否包含关键词
     * 
     * @param keyword 关键词
     * @return 是否包含
     */
    public boolean hasKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return false;
        }
        return this.keywords.contains(keyword.trim().toLowerCase());
    }
    
    /**
     * 检查是否包含实体类型
     * 
     * @param entityType 实体类型
     * @return 是否包含
     */
    public boolean hasEntityType(String entityType) {
        if (entityType == null || entityType.trim().isEmpty()) {
            return false;
        }
        return this.entityTypes.contains(entityType.trim().toUpperCase());
    }
    
    /**
     * 检查是否为查询类型意图
     * 
     * @return 是否为查询类型
     */
    public boolean isQueryIntent() {
        return "QUERY".equalsIgnoreCase(this.intentType);
    }
    
    /**
     * 检查是否为命令类型意图
     * 
     * @return 是否为命令类型
     */
    public boolean isCommandIntent() {
        return "COMMAND".equalsIgnoreCase(this.intentType);
    }
    
    /**
     * 检查是否为对话类型意图
     * 
     * @return 是否为对话类型
     */
    public boolean isConversationIntent() {
        return "CONVERSATION".equalsIgnoreCase(this.intentType);
    }
    
    /**
     * 启用意图
     */
    public void enable() {
        this.enabled = true;
    }
    
    /**
     * 禁用意图
     */
    public void disable() {
        this.enabled = false;
    }
    
    /**
     * 检查意图是否有效
     * 
     * @return 是否有效
     */
    public boolean isValid() {
        return intentId != null && !intentId.trim().isEmpty() &&
               intentName != null && !intentName.trim().isEmpty() &&
               enabled;
    }
    
    /**
     * 参数校验
     */
    private static void validateParams(String intentId, String intentName) {
        if (intentId == null || intentId.trim().isEmpty()) {
            throw new IllegalArgumentException("意图ID不能为空");
        }
        if (intentName == null || intentName.trim().isEmpty()) {
            throw new IllegalArgumentException("意图名称不能为空");
        }
        
        // ID长度限制
        if (intentId.trim().length() > 100) {
            throw new IllegalArgumentException("意图ID长度不能超过100个字符");
        }
        
        // 名称长度限制
        if (intentName.trim().length() > 200) {
            throw new IllegalArgumentException("意图名称长度不能超过200个字符");
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IntentCatalog that = (IntentCatalog) o;
        return Objects.equals(intentId, that.intentId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(intentId);
    }
    
    @Override
    public String toString() {
        return String.format("IntentCatalog{intentId='%s', intentName='%s', intentType='%s', enabled=%s}", 
                intentId, intentName, intentType, enabled);
    }
}