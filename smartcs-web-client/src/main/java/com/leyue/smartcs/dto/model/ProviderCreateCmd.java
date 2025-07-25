package com.leyue.smartcs.dto.model;

import com.alibaba.cola.dto.Command;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 创建模型提供商命令
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProviderCreateCmd extends Command {
    
    /**
     * 提供商类型
     */
    @NotNull(message = "提供商类型不能为空")
    private String providerType;
    
    /**
     * 名称
     */
    @NotEmpty(message = "提供商名称不能为空")
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
     * API Key（全局）
     */
    @NotEmpty(message = "API Key不能为空")
    private String apiKey;
    
    /**
     * API Endpoint
     */
    private String endpoint;
    
    /**
     * 支持的模型类型（逗号分隔）
     */
    private String supportedModelTypes;
}