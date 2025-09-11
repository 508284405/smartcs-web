package com.leyue.smartcs.dto.dictionary;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 字典条目更新命令
 * 
 * @author Claude
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DictionaryEntryUpdateCmd {
    
    /**
     * 条目ID
     */
    private Long id;
    
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
     * 更新人
     */
    private String updatedBy;
}