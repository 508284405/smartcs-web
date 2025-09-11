package com.leyue.smartcs.domain.dictionary.valueobject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * 模式规则值对象
 * 封装正则表达式模式和替换规则，用于高级文本处理
 * 
 * 应用场景：
 * - 复杂的文本标准化规则
 * - 基于模式的拼音纠错
 * - 动态的实体识别和替换
 * - 领域特定的文本转换规则
 * 
 * @author Claude
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PatternRule {
    
    /**
     * 规则名称
     */
    private String name;
    
    /**
     * 正则表达式模式
     */
    private String pattern;
    
    /**
     * 替换模板
     * 支持捕获组引用，如：$1, $2
     */
    private String replacement;
    
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
     * 是否点号匹配任意字符（包括换行符）
     */
    @Builder.Default
    private Boolean dotAll = false;
    
    /**
     * 编译后的Pattern对象（缓存）
     */
    private transient Pattern compiledPattern;
    
    /**
     * 创建模式规则
     * 
     * @param name 规则名称
     * @param pattern 正则表达式模式
     * @param replacement 替换模板
     * @param description 规则描述
     * @return 模式规则对象
     */
    public static PatternRule of(String name, String pattern, String replacement, String description) {
        validateParams(name, pattern, replacement);
        
        PatternRule rule = PatternRule.builder()
                .name(name.trim())
                .pattern(pattern)
                .replacement(replacement != null ? replacement : "")
                .description(description != null ? description.trim() : null)
                .build();
        
        // 验证正则表达式有效性
        rule.compilePattern();
        
        return rule;
    }
    
    /**
     * 创建带选项的模式规则
     * 
     * @param name 规则名称
     * @param pattern 正则表达式模式
     * @param replacement 替换模板
     * @param description 规则描述
     * @param ignoreCase 是否忽略大小写
     * @param multiline 是否多行模式
     * @param dotAll 是否点号匹配任意字符
     * @return 模式规则对象
     */
    public static PatternRule of(String name, String pattern, String replacement, String description,
                               boolean ignoreCase, boolean multiline, boolean dotAll) {
        PatternRule rule = of(name, pattern, replacement, description);
        rule.ignoreCase = ignoreCase;
        rule.multiline = multiline;
        rule.dotAll = dotAll;
        
        // 重新编译Pattern
        rule.compiledPattern = null;
        rule.compilePattern();
        
        return rule;
    }
    
    /**
     * 应用规则进行文本替换
     * 
     * @param text 原文本
     * @return 替换后的文本
     */
    public String apply(String text) {
        if (text == null || text.isEmpty() || !enabled) {
            return text;
        }
        
        try {
            Pattern p = getCompiledPattern();
            return p.matcher(text).replaceAll(replacement);
        } catch (Exception e) {
            throw new RuntimeException("应用模式规则失败: " + name, e);
        }
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
            if (dotAll) {
                flags |= Pattern.DOTALL;
            }
            
            this.compiledPattern = Pattern.compile(pattern, flags);
        } catch (PatternSyntaxException e) {
            throw new IllegalArgumentException("无效的正则表达式模式: " + pattern, e);
        }
    }
    
    /**
     * 参数校验
     */
    private static void validateParams(String name, String pattern, String replacement) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("规则名称不能为空");
        }
        if (pattern == null || pattern.isEmpty()) {
            throw new IllegalArgumentException("正则表达式模式不能为空");
        }
        if (replacement == null) {
            throw new IllegalArgumentException("替换模板不能为null");
        }
        
        // 名称长度限制
        if (name.trim().length() > 100) {
            throw new IllegalArgumentException("规则名称长度不能超过100个字符");
        }
        
        // 模式长度限制
        if (pattern.length() > 1000) {
            throw new IllegalArgumentException("正则表达式模式长度不能超过1000个字符");
        }
        
        // 替换模板长度限制
        if (replacement.length() > 1000) {
            throw new IllegalArgumentException("替换模板长度不能超过1000个字符");
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PatternRule that = (PatternRule) o;
        return Objects.equals(name, that.name) && 
               Objects.equals(pattern, that.pattern) && 
               Objects.equals(replacement, that.replacement);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(name, pattern, replacement);
    }
    
    @Override
    public String toString() {
        return String.format("PatternRule{name='%s', pattern='%s', replacement='%s', enabled=%s}", 
                name, pattern, replacement, enabled);
    }
}