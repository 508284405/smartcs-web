package com.leyue.smartcs.dto.intent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 槽位定义DTO
 * 
 * @author Claude
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlotDefinitionDTO {
    
    /**
     * 槽位名称（标识符）
     */
    private String name;
    
    /**
     * 槽位显示标签
     */
    private String label;
    
    /**
     * 槽位类型
     */
    private String type;
    
    /**
     * 是否必填
     */
    private Boolean required;
    
    /**
     * 校验规则（正则表达式、枚举值等）
     */
    private Map<String, Object> validation;
    
    /**
     * 提示信息
     */
    private String hint;
    
    /**
     * 示例值
     */
    private List<String> examples;
    
    /**
     * 默认值
     */
    private String defaultValue;
    
    /**
     * 依赖关系（依赖其他槽位）
     */
    private List<String> dependencies;
    
    /**
     * 排序顺序
     */
    private Integer order;
    
    /**
     * 是否支持多值
     */
    private Boolean multiple;
    
    /**
     * 最小值（数值类型）
     */
    private Double minValue;
    
    /**
     * 最大值（数值类型）
     */
    private Double maxValue;
    
    /**
     * 最小长度（字符串类型）
     */
    private Integer minLength;
    
    /**
     * 最大长度（字符串类型）
     */
    private Integer maxLength;
    
    /**
     * 枚举选项（枚举类型）
     */
    private List<String> enumOptions;
    
    /**
     * 格式化模式（日期、时间等）
     */
    private String pattern;
    
    /**
     * 单位信息
     */
    private String unit;
    
    /**
     * 扩展属性
     */
    private Map<String, Object> extensions;
}