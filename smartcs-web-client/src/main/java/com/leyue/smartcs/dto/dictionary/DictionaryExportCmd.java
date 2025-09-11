package com.leyue.smartcs.dto.dictionary;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 字典数据导出命令
 * 
 * @author Claude
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DictionaryExportCmd {
    
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
     * 导出格式：JSON, CSV, EXCEL
     */
    @Builder.Default
    private String format = "JSON";
    
    /**
     * 是否仅导出生效数据
     */
    @Builder.Default
    private Boolean activeOnly = true;
    
    /**
     * 导出人
     */
    private String exportedBy;
}