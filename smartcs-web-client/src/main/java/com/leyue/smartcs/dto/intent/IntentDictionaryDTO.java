package com.leyue.smartcs.dto.intent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 意图字典DTO
 * 
 * @author Claude
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IntentDictionaryDTO {
    
    /**
     * 意图编码
     */
    private String intentCode;
    
    /**
     * 关键词列表
     */
    private List<String> keywords;
    
    /**
     * 匹配模式列表（支持正则）
     */
    private List<String> patterns;
    
    /**
     * 同义词映射
     */
    private Map<String, List<String>> synonyms;
    
    /**
     * 实体映射
     */
    private Map<String, List<String>> entities;
    
    /**
     * 语言代码
     */
    private String language;
    
    /**
     * 权重配置
     */
    private Map<String, Double> weights;
    
    /**
     * 版本
     */
    private String version;
    
    /**
     * 扩展配置
     */
    private Map<String, Object> extensions;
}