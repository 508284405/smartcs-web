package com.leyue.smartcs.dto.app;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Map;

/**
 * 创建AI应用命令
 */
@Data
public class AiAppCreateCmd {
    
    /**
     * 应用名称
     */
    @NotBlank(message = "应用名称不能为空")
    @Size(max = 128, message = "应用名称长度不能超过128个字符")
    private String name;
    
    /**
     * 应用唯一编码
     */
    @NotBlank(message = "应用编码不能为空")  
    @Size(max = 64, message = "应用编码长度不能超过64个字符")
    private String code;
    
    /**
     * 应用描述
     */
    @Size(max = 500, message = "应用描述长度不能超过500个字符")
    private String description;
    
    /**
     * 应用类型
     */
    @NotNull(message = "应用类型不能为空")
    private String type;
    
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