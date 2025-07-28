package com.leyue.smartcs.dto.model;

import com.alibaba.cola.dto.Command;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;

/**
 * 更新模型实例命令
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ModelUpdateCmd extends Command {
    
    /**
     * 模型ID
     */
    @NotNull(message = "模型ID不能为空")
    private Long id;
    
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
}