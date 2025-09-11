package com.leyue.smartcs.domain.moderation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * 审核策略模板领域实体
 * 定义了prompt模板和变量替换规则
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ModerationPolicyTemplate {

    /**
     * 模板ID
     */
    private Long id;

    /**
     * 模板名称
     */
    private String name;

    /**
     * 模板编码，用于程序识别
     */
    private String code;

    /**
     * 模板描述
     */
    private String description;

    /**
     * 模板类型（如：DETAILED, QUICK, BATCH）
     */
    private String templateType;

    /**
     * 基础prompt模板内容
     */
    private String promptTemplate;

    /**
     * 维度列表模板
     */
    private String dimensionTemplate;

    /**
     * 响应格式模板
     */
    private String responseTemplate;

    /**
     * 支持的语言（如：zh-CN, en-US）
     */
    private String language;

    /**
     * 模板变量定义（JSON格式存储）
     */
    private Map<String, Object> variables;

    /**
     * 默认变量值（JSON格式存储）
     */
    private Map<String, Object> defaultValues;

    /**
     * 是否启用
     */
    private Boolean isActive;

    /**
     * 版本号
     */
    private String version;

    /**
     * 创建者
     */
    private String createdBy;

    /**
     * 更新者
     */
    private String updatedBy;

    /**
     * 创建时间（毫秒时间戳）
     */
    private Long createdAt;

    /**
     * 更新时间（毫秒时间戳）
     */
    private Long updatedAt;

    // 变量替换的正则表达式
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{([^}]+)\\}\\}");

    /**
     * 验证模板数据的完整性
     */
    public boolean isValid() {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        if (code == null || code.trim().isEmpty()) {
            return false;
        }
        if (promptTemplate == null || promptTemplate.trim().isEmpty()) {
            return false;
        }
        return true;
    }

    /**
     * 判断是否支持指定语言
     */
    public boolean supportsLanguage(String targetLanguage) {
        return language != null && language.equalsIgnoreCase(targetLanguage);
    }

    /**
     * 获取变量定义
     */
    public Object getVariable(String key) {
        return variables != null ? variables.get(key) : null;
    }

    /**
     * 获取默认值
     */
    public Object getDefaultValue(String key) {
        return defaultValues != null ? defaultValues.get(key) : null;
    }

    /**
     * 设置变量定义
     */
    public void setVariable(String key, Object value) {
        if (variables == null) {
            variables = new java.util.HashMap<>();
        }
        variables.put(key, value);
    }

    /**
     * 设置默认值
     */
    public void setDefaultValue(String key, Object value) {
        if (defaultValues == null) {
            defaultValues = new java.util.HashMap<>();
        }
        defaultValues.put(key, value);
    }

    /**
     * 渲染完整的prompt
     */
    public String renderPrompt(Map<String, Object> contextVariables) {
        Map<String, Object> allVariables = new java.util.HashMap<>();
        
        // 添加默认值
        if (defaultValues != null) {
            allVariables.putAll(defaultValues);
        }
        
        // 添加上下文变量（覆盖默认值）
        if (contextVariables != null) {
            allVariables.putAll(contextVariables);
        }
        
        return replaceVariables(promptTemplate, allVariables);
    }

    /**
     * 渲染维度列表部分
     */
    public String renderDimensions(java.util.List<ModerationDimension> dimensions) {
        if (dimensionTemplate == null || dimensions == null || dimensions.isEmpty()) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        int index = 1;
        for (ModerationDimension dimension : dimensions) {
            Map<String, Object> dimVariables = new java.util.HashMap<>();
            dimVariables.put("index", index++);
            dimVariables.put("name", dimension.getName());
            dimVariables.put("description", dimension.getDescription());
            dimVariables.put("guideline", dimension.getCheckGuideline());
            
            String renderedDimension = replaceVariables(dimensionTemplate, dimVariables);
            sb.append(renderedDimension).append("\n");
        }
        
        return sb.toString().trim();
    }

    /**
     * 替换模板中的变量
     */
    private String replaceVariables(String template, Map<String, Object> variables) {
        if (template == null || variables == null) {
            return template;
        }
        
        Matcher matcher = VARIABLE_PATTERN.matcher(template);
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String variableName = matcher.group(1).trim();
            Object value = variables.get(variableName);
            String replacement = value != null ? value.toString() : "{{" + variableName + "}}";
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);
        
        return result.toString();
    }

    /**
     * 提取模板中的变量名列表
     */
    public java.util.Set<String> extractVariableNames() {
        java.util.Set<String> variableNames = new java.util.HashSet<>();
        
        if (promptTemplate != null) {
            extractVariableNamesFromText(promptTemplate, variableNames);
        }
        if (dimensionTemplate != null) {
            extractVariableNamesFromText(dimensionTemplate, variableNames);
        }
        if (responseTemplate != null) {
            extractVariableNamesFromText(responseTemplate, variableNames);
        }
        
        return variableNames;
    }

    /**
     * 从文本中提取变量名
     */
    private void extractVariableNamesFromText(String text, java.util.Set<String> variableNames) {
        Matcher matcher = VARIABLE_PATTERN.matcher(text);
        while (matcher.find()) {
            variableNames.add(matcher.group(1).trim());
        }
    }

    /**
     * 创建新的模板实体（工厂方法）
     */
    public static ModerationPolicyTemplate create(String name, String code, String description,
                                                String templateType, String promptTemplate,
                                                String dimensionTemplate, String responseTemplate,
                                                String language, String version, String createdBy) {
        long currentTime = System.currentTimeMillis();
        return ModerationPolicyTemplate.builder()
                .name(name)
                .code(code)
                .description(description)
                .templateType(templateType)
                .promptTemplate(promptTemplate)
                .dimensionTemplate(dimensionTemplate)
                .responseTemplate(responseTemplate)
                .language(language)
                .version(version != null ? version : "1.0")
                .isActive(true)
                .variables(new java.util.HashMap<>())
                .defaultValues(new java.util.HashMap<>())
                .createdBy(createdBy)
                .updatedBy(createdBy)
                .createdAt(currentTime)
                .updatedAt(currentTime)
                .build();
    }

    /**
     * 更新模板信息
     */
    public void update(String name, String description, String templateType,
                      String promptTemplate, String dimensionTemplate, String responseTemplate,
                      String language, Map<String, Object> variables, Map<String, Object> defaultValues,
                      String updatedBy) {
        this.name = name;
        this.description = description;
        this.templateType = templateType;
        this.promptTemplate = promptTemplate;
        this.dimensionTemplate = dimensionTemplate;
        this.responseTemplate = responseTemplate;
        this.language = language;
        if (variables != null) {
            this.variables = new java.util.HashMap<>(variables);
        }
        if (defaultValues != null) {
            this.defaultValues = new java.util.HashMap<>(defaultValues);
        }
        this.updatedBy = updatedBy;
        this.updatedAt = System.currentTimeMillis();
    }

    /**
     * 启用模板
     */
    public void enable(String updatedBy) {
        this.isActive = true;
        this.updatedBy = updatedBy;
        this.updatedAt = System.currentTimeMillis();
    }

    /**
     * 禁用模板
     */
    public void disable(String updatedBy) {
        this.isActive = false;
        this.updatedBy = updatedBy;
        this.updatedAt = System.currentTimeMillis();
    }

    /**
     * 创建新版本
     */
    public ModerationPolicyTemplate createNewVersion(String newVersion, String updatedBy) {
        long currentTime = System.currentTimeMillis();
        return ModerationPolicyTemplate.builder()
                .name(this.name)
                .code(this.code)
                .description(this.description)
                .templateType(this.templateType)
                .promptTemplate(this.promptTemplate)
                .dimensionTemplate(this.dimensionTemplate)
                .responseTemplate(this.responseTemplate)
                .language(this.language)
                .version(newVersion)
                .variables(this.variables != null ? new java.util.HashMap<>(this.variables) : new java.util.HashMap<>())
                .defaultValues(this.defaultValues != null ? new java.util.HashMap<>(this.defaultValues) : new java.util.HashMap<>())
                .isActive(true)
                .createdBy(updatedBy)
                .updatedBy(updatedBy)
                .createdAt(currentTime)
                .updatedAt(currentTime)
                .build();
    }
}