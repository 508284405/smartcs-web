package com.leyue.smartcs.dto.dictionary;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 字典数据校验命令
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DictionaryValidateCmd {
    
    private String dictionaryType;
    private String tenant;
    private String channel;
    private String domain;
    private String data;
}