package com.leyue.smartcs.domain.rag.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文档提取设置
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExtractSetting {
    
    /**
     * 文件URL
     */
    private String fileUrl;
    
    /**
     * 文件类型
     */
    private String fileType;
    
    /**
     * 是否自动处理
     */
    private boolean automatic;
    
    /**
     * 处理规则模式
     */
    private String processRuleMode;
    
    /**
     * 其他配置
     */
    private java.util.Map<String, Object> extraConfig;
} 