package com.leyue.smartcs.dto.dictionary;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 模式权重DTO
 * 用于在各层之间传输模式权重数据
 * 
 * @author Claude
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PatternWeightDTO {
    
    /**
     * 规则名称
     */
    private String name;
    
    /**
     * 正则表达式模式
     */
    private String pattern;
    
    /**
     * 权重值
     */
    private Double weight;
    
    /**
     * 处理器ID
     */
    private String handlerId;
    
    /**
     * 规则描述
     */
    private String description;
    
    /**
     * 是否启用
     */
    private Boolean enabled;
    
    /**
     * 优先级
     */
    private Integer priority;
    
    /**
     * 是否忽略大小写
     */
    private Boolean ignoreCase;
    
    /**
     * 是否多行模式
     */
    private Boolean multiline;
    
    /**
     * 权重计算方式
     */
    private String weightMode;
}