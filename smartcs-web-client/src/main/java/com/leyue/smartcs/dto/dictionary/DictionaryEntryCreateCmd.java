package com.leyue.smartcs.dto.dictionary;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// 使用Lombok的@NonNull或自定义校验

/**
 * 字典条目创建命令
 * 
 * @author Claude
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DictionaryEntryCreateCmd {
    
    /**
     * 字典类型
     */
    private String dictionaryType;
    
    /**
     * 租户标识
     */
    private String tenant;
    
    /**
     * 渠道标识
     */
    private String channel;
    
    /**
     * 领域标识
     */
    private String domain;
    
    /**
     * 条目key
     */
    private String entryKey;
    
    /**
     * 条目值（JSON格式）
     */
    private String entryValue;
    
    /**
     * 描述说明
     */
    private String description;
    
    /**
     * 状态：DRAFT-草稿，ACTIVE-生效，INACTIVE-失效
     */
    @Builder.Default
    private String status = "ACTIVE";
    
    /**
     * 优先级（默认为100）
     */
    @Builder.Default
    private Integer priority = 100;
    
    /**
     * 创建人
     */
    private String createdBy;
}