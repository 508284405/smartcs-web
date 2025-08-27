package com.leyue.smartcs.dto.dictionary;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 意图目录DTO
 * 用于在各层之间传输意图目录数据
 * 
 * @author Claude
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IntentCatalogDTO {
    
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
     * 父意图ID
     */
    private String parentIntentId;
    
    /**
     * 意图类型
     */
    private String intentType;
    
    /**
     * 意图权重
     */
    private Double weight;
    
    /**
     * 是否启用
     */
    private Boolean enabled;
    
    /**
     * 关键词列表
     */
    private Set<String> keywords;
    
    /**
     * 实体类型列表
     */
    private Set<String> entityTypes;
    
    /**
     * 查询模式列表
     */
    private List<String> queryPatterns;
    
    /**
     * 处理器ID
     */
    private String handlerId;
    
    /**
     * 扩展属性
     */
    private Map<String, Object> properties;
}