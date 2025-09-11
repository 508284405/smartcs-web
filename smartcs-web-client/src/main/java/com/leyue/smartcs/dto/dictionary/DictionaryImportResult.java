package com.leyue.smartcs.dto.dictionary;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 字典数据导入结果
 * 
 * @author Claude
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DictionaryImportResult {
    
    /**
     * 导入是否成功
     */
    private Boolean success;
    
    /**
     * 总条目数
     */
    private Integer totalCount;
    
    /**
     * 成功导入数量
     */
    private Integer successCount;
    
    /**
     * 跳过数量
     */
    private Integer skipCount;
    
    /**
     * 失败数量
     */
    private Integer failCount;
    
    /**
     * 错误信息列表
     */
    private List<String> errorMessages;
    
    /**
     * 导入耗时（毫秒）
     */
    private Long elapsedMs;
}