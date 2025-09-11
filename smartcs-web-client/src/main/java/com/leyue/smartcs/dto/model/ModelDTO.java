package com.leyue.smartcs.dto.model;

import com.alibaba.cola.dto.DTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;

/**
 * 模型实例数据传输对象
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ModelDTO extends DTO {
    
    /**
     * 模型ID
     */
    private Long id;
    
    /**
     * 关联provider.id
     */
    private Long providerId;
    
    /**
     * 提供商名称
     */
    private String providerName;
    
    
    /**
     * 名称
     */
    private String label;
    
    /**
     * 模型类型（支持多种类型）
     */
    private List<String> modelType;
    
    /**
     * 能力标签（逗号分隔）
     */
    private String features;
    
    /**
     * 来源
     */
    private String fetchFrom;
    
    /**
     * 其他属性（如context_size, mode等，JSON格式）
     */
    private String modelProperties;
    
    /**
     * 是否废弃
     */
    private Boolean deprecated;
    
    /**
     * 状态
     */
    private String status;
    
    /**
     * 是否负载均衡
     */
    private Boolean loadBalancingEnabled;
    
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