package com.leyue.smartcs.dto.dictionary;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 字典数据传输对象
 * 封装某个字典类型的完整数据
 * 
 * @author Claude
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DictionaryDataDTO {
    
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
     * 字典数据版本
     */
    private Long version;
    
    /**
     * 映射类型数据（如标准化规则、拼音纠错、同义词组）
     * Map<String, String> 或 Map<String, Set<String>>
     */
    private Map<String, Object> mappingData;
    
    /**
     * 集合类型数据（如停用词、前缀词汇）
     */
    private Set<String> setData;
    
    /**
     * 列表类型数据（如模式规则、权重规则）
     * List<PatternRuleDTO> 或 List<PatternWeightDTO>
     */
    private List<Object> listData;
    
    /**
     * 原始 JSON 数据（备用）
     */
    private String rawData;
    
    /**
     * 数据条目数量
     */
    private Integer entryCount;
    
    /**
     * 数据更新时间戳
     */
    private Long updateTimestamp;
}