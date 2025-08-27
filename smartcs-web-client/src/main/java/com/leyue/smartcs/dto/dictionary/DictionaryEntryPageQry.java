package com.leyue.smartcs.dto.dictionary;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 字典条目分页查询命令
 * 
 * @author Claude
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DictionaryEntryPageQry {
    
    /**
     * 页码（从1开始）
     */
    @Builder.Default
    private Integer pageNum = 1;
    
    /**
     * 页大小
     */
    @Builder.Default
    private Integer pageSize = 20;
    
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
     * 条目键（支持模糊搜索）
     */
    private String entryKey;
    
    /**
     * 状态
     */
    private String status;
    
    /**
     * 创建人
     */
    private String createdBy;
    
    /**
     * 排序字段
     */
    @Builder.Default
    private String orderBy = "updateTime";
    
    /**
     * 排序方向：ASC, DESC
     */
    @Builder.Default
    private String orderDirection = "DESC";
}