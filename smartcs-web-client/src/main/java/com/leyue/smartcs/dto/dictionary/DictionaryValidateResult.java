package com.leyue.smartcs.dto.dictionary;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 字典数据校验结果
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DictionaryValidateResult {
    
    private Boolean valid;
    private String errorMessage;
}
