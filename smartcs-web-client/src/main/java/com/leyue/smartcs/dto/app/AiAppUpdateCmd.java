package com.leyue.smartcs.dto.app;

import lombok.Data;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Map;

/**
 * 更新AI应用命令
 */
@Data
public class AiAppUpdateCmd {
    
    /**
     * 应用ID
     */
    @NotNull(message = "应用ID不能为空")
    private Long id;
    
    /**
     * 应用名称
     */
    @NotBlank(message = "应用名称不能为空")
    @Size(max = 128, message = "应用名称长度不能超过128个字符")
    private String name;
    
    /**
     * 应用描述
     */
    @Size(max = 500, message = "应用描述长度不能超过500个字符")
    private String description;
    
    /**
     * 应用配置信息
     */
    private Map<String, Object> config;
    
    /**
     * 应用图标
     */
    private String icon;
    
    /**
     * 应用标签
     */
    private List<String> tags;
}