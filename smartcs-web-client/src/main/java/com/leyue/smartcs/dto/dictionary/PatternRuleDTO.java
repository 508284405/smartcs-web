package com.leyue.smartcs.dto.dictionary;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 模式规则DTO
 * 用于在各层之间传输模式规则数据
 * 
 * @author Claude
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PatternRuleDTO {
    
    /**
     * 规则名称
     */
    private String name;
    
    /**
     * 正则表达式模式
     */
    private String pattern;
    
    /**
     * 替换模板
     */
    private String replacement;
    
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
     * 是否点号匹配任意字符
     */
    private Boolean dotAll;
}