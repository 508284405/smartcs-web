package com.leyue.smartcs.dto.dictionary;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 字典条目数据传输对象
 * 
 * @author Claude
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DictionaryEntryDTO {
    
    /**
     * 条目ID
     */
    private Long id;
    
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
     * 条目键
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
    private String status;
    
    /**
     * 优先级
     */
    private Integer priority;
    
    /**
     * 版本号
     */
    private Long version;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 创建人
     */
    private String createdBy;
    
    /**
     * 更新人
     */
    private String updatedBy;
}