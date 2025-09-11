package com.leyue.smartcs.dto.dictionary;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 字典条目删除命令
 * 
 * @author Claude
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DictionaryEntryDeleteCmd {
    
    /**
     * 条目ID
     */
    private Long id;
    
    /**
     * 删除人
     */
    private String deletedBy;
}