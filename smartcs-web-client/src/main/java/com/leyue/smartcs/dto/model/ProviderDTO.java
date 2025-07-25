package com.leyue.smartcs.dto.model;

import com.alibaba.cola.dto.DTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 模型提供商数据传输对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProviderDTO extends DTO {
    
    /**
     * 提供商ID
     */
    private Long id;
    
    /**
     * 提供商类型
     */
    private String providerType;
    
    /**
     * 名称
     */
    private String label;
    
    /**
     * 小图标URL
     */
    private String iconSmall;
    
    /**
     * 大图标URL
     */
    private String iconLarge;
    
    /**
     * API Key（敏感信息，返回时脱敏）
     */
    private String apiKey;
    
    /**
     * API Endpoint
     */
    private String endpoint;
    
    /**
     * 支持的模型类型（逗号分隔）
     */
    private String supportedModelTypes;
    
    /**
     * 创建人
     */
    private String createdBy;
    
    /**
     * 更新人
     */
    private String updatedBy;
    
    /**
     * 创建时间（毫秒时间戳）
     */
    private Long createdAt;
    
    /**
     * 更新时间（毫秒时间戳）
     */
    private Long updatedAt;
}