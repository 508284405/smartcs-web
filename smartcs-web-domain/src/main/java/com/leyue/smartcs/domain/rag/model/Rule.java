package com.leyue.smartcs.domain.rag.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分段规则配置
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Rule {
    
    /**
     * 分段规则
     */
    private SegmentationRule segmentation;
    
    /**
     * 模式：automatic、custom、hierarchical
     */
    private String mode;
    
    /**
     * 父子分段模式
     */
    private ParentMode parentMode;
    
    /**
     * 是否启用预处理
     */
    private boolean enablePreprocess;
    
    /**
     * 是否移除停用词
     */
    private boolean removeStopwords;
    
    /**
     * 是否移除URL和邮箱
     */
    private boolean removeUrlsAndEmails;
    
    /**
     * 自定义分隔符
     */
    private String separator;
} 