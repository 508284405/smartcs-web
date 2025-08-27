package com.leyue.smartcs.dto.dictionary;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 字典数据导出结果
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DictionaryExportResult {
    
    private Boolean success;
    private String data;
    private String fileName;
    private Long elapsedMs;
}