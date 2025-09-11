package com.leyue.smartcs.dto.dictionary;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 字典数据导入命令
 * 
 * @author Claude
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DictionaryImportCmd {
    
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
     * 导入数据（JSON格式）
     */
    private String data;
    
    /**
     * 导入模式：MERGE-合并，REPLACE-替换
     */
    @Builder.Default
    private String importMode = "MERGE";
    
    /**
     * 是否覆盖已存在的条目
     */
    @Builder.Default
    private Boolean overrideExisting = false;
    
    /**
     * 导入人
     */
    private String importedBy;
}