package com.leyue.smartcs.dto.model;

import com.alibaba.cola.dto.Command;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;

/**
 * 创建模型实例命令
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ModelCreateCmd extends Command {
    
    /**
     * 关联provider.id
     */
    @NotNull(message = "提供商ID不能为空")
    private Long providerId;
    
    
    /**
     * 名称
     */
    @NotEmpty(message = "模型名称不能为空")
    private String label;
    
    /**
     * 模型类型（支持多种类型）
     */
    @NotEmpty(message = "模型类型不能为空")
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
     * 是否废弃，默认false
     */
    private Boolean deprecated = false;
    
    /**
     * 状态，默认active
     */
    private String status = "active";
    
    /**
     * 是否负载均衡，默认false
     */
    private Boolean loadBalancingEnabled = false;
}