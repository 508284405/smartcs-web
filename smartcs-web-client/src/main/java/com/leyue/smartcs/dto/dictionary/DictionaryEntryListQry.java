package com.leyue.smartcs.dto.dictionary;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 字典条目列表查询命令
 * 
 * @author Claude
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DictionaryEntryListQry {
    
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
     * 状态
     */
    @Builder.Default
    private String status = "ACTIVE";
    
    /**
     * 限制条数
     */
    @Builder.Default
    private Integer limit = 1000;
}