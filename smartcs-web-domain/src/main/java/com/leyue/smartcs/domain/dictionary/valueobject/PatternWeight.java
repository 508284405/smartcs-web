package com.leyue.smartcs.domain.dictionary.valueobject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * 模式权重值对象
 * 封装正则表达式模式和权重值，用于关键词权重调整
 * 
 * 应用场景：
 * - 查询改写阶段的关键词权重调整
 * - 基于模式的权重分配
 * - 动态的重要性评估
 * 
 * @author Claude
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PatternWeight {
    
    /**
     * 规则名称
     */
    private String name;
    
    /**
     * 正则表达式模式
     */
    private String pattern;
    
    /**
     * 权重值（0.0 - 10.0）
     */
    private Double weight;
    
    /**
     * 处理器ID（可选）
     * 用于指定特定的权重处理逻辑
     */
    private String handlerId;
    
    /**
     * 规则描述
     */
    private String description;
    
    /**
     * 是否启用
     */
    @Builder.Default
    private Boolean enabled = true;
    
    /**
     * 优先级（数值越大优先级越高）
     */
    @Builder.Default
    private Integer priority = 100;
    
    /**
     * 是否忽略大小写
     */
    @Builder.Default
    private Boolean ignoreCase = false;
    
    /**
     * 是否多行模式
     */
    @Builder.Default
    private Boolean multiline = false;
    
    /**
     * 权重计算方式：REPLACE(替换), ADD(累加), MULTIPLY(相乘)
     */
    @Builder.Default
    private String weightMode = "REPLACE";
    
    /**
     * 编译后的Pattern对象（缓存）
     */
    private transient Pattern compiledPattern;
    
    /**
     * 创建模式权重
     * 
     * @param name 规则名称
     * @param pattern 正则表达式模式
     * @param weight 权重值
     * @param description 规则描述
     * @return 模式权重对象
     */
    public static PatternWeight of(String name, String pattern, Double weight, String description) {
        validateParams(name, pattern, weight);
        
        PatternWeight rule = PatternWeight.builder()
                .name(name.trim())
                .pattern(pattern)
                .weight(weight)
                .description(description != null ? description.trim() : null)
                .build();
        
        // 验证正则表达式有效性
        rule.compilePattern();
        
        return rule;
    }
    
    /**
     * 创建带处理器的模式权重
     * 
     * @param name 规则名称
     * @param pattern 正则表达式模式
     * @param weight 权重值
     * @param handlerId 处理器ID
     * @param description 规则描述
     * @return 模式权重对象
     */
    public static PatternWeight of(String name, String pattern, Double weight, String handlerId, String description) {
        PatternWeight rule = of(name, pattern, weight, description);
        rule.handlerId = handlerId;
        return rule;
    }
    
    /**
     * 检查文本是否匹配模式
     * 
     * @param text 待检查的文本
     * @return 是否匹配
     */
    public boolean matches(String text) {
        if (text == null || !enabled) {
            return false;
        }
        
        try {
            Pattern p = getCompiledPattern();
            return p.matcher(text).find();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 获取编译后的Pattern对象
     * 
     * @return 编译后的Pattern
     */
    public Pattern getCompiledPattern() {
        if (compiledPattern == null) {
            compilePattern();
        }
        return compiledPattern;
    }
    
    /**
     * 启用规则
     */
    public void enable() {
        this.enabled = true;
    }
    
    /**
     * 禁用规则
     */
    public void disable() {
        this.enabled = false;
    }
    
    /**
     * 检查规则是否有效
     * 
     * @return 是否有效
     */
    public boolean isValid() {
        try {
            compilePattern();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 编译正则表达式模式
     */
    private void compilePattern() {
        if (pattern == null || pattern.isEmpty()) {
            throw new IllegalArgumentException("正则表达式模式不能为空");
        }
        
        try {
            int flags = 0;
            if (ignoreCase) {
                flags |= Pattern.CASE_INSENSITIVE;
            }
            if (multiline) {
                flags |= Pattern.MULTILINE;
            }
            
            this.compiledPattern = Pattern.compile(pattern, flags);
        } catch (PatternSyntaxException e) {
            throw new IllegalArgumentException("无效的正则表达式模式: " + pattern, e);
        }
    }
    
    /**
     * 参数校验
     */
    private static void validateParams(String name, String pattern, Double weight) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("规则名称不能为空");
        }
        if (pattern == null || pattern.isEmpty()) {
            throw new IllegalArgumentException("正则表达式模式不能为空");
        }
        if (weight == null) {
            throw new IllegalArgumentException("权重值不能为null");
        }
        if (weight < 0.0 || weight > 10.0) {
            throw new IllegalArgumentException("权重值必须在 0.0 - 10.0 之间");
        }
        
        // 名称长度限制
        if (name.trim().length() > 100) {
            throw new IllegalArgumentException("规则名称长度不能超过100个字符");
        }
        
        // 模式长度限制
        if (pattern.length() > 1000) {
            throw new IllegalArgumentException("正则表达式模式长度不能超过1000个字符");
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PatternWeight that = (PatternWeight) o;
        return Objects.equals(name, that.name) && 
               Objects.equals(pattern, that.pattern) && 
               Objects.equals(weight, that.weight);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(name, pattern, weight);
    }
    
    @Override
    public String toString() {
        return String.format("PatternWeight{name='%s', pattern='%s', weight=%s, enabled=%s}", 
                name, pattern, weight, enabled);
    }
}