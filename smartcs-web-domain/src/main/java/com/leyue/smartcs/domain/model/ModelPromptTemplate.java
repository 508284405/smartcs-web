package com.leyue.smartcs.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.alibaba.fastjson2.JSON;

/**
 * 模型Prompt模板领域模型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelPromptTemplate {
    
    /**
     * 主键
     */
    private Long id;
    
    /**
     * 模板键（唯一标识）
     */
    private String templateKey;
    
    /**
     * 模板名称
     */
    private String templateName;
    
    /**
     * 模板内容
     */
    private String templateContent;
    
    /**
     * 模板描述
     */
    private String description;
    
    /**
     * 支持的模型类型（逗号分隔）
     */
    private String modelTypes;
    
    /**
     * 模板变量（JSON格式）
     */
    private String variables;
    
    /**
     * 是否为系统内置模板
     */
    private Boolean isSystem;
    
    /**
     * 状态
     */
    private String status;
    
    /**
     * 逻辑删除标识
     */
    private Integer isDeleted;
    
    /**
     * 创建人
     */
    private String createdBy;
    
    /**
     * 更新人
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
    
    /**
     * 验证模板配置是否有效
     */
    public boolean isValid() {
        return templateKey != null && !templateKey.trim().isEmpty()
                && templateName != null && !templateName.trim().isEmpty()
                && templateContent != null && !templateContent.trim().isEmpty()
                && status != null;
    }
    
    /**
     * 是否已删除
     */
    public boolean isDeleted() {
        return isDeleted != null && isDeleted == 1;
    }
    
    /**
     * 标记为删除
     */
    public void markAsDeleted() {
        this.isDeleted = 1;
        this.updatedAt = System.currentTimeMillis();
    }
    
    /**
     * 是否激活
     */
    public boolean isActive() {
        return "ACTIVE".equals(status) && !isDeleted();
    }
    
    /**
     * 激活模板
     */
    public void activate() {
        this.status = "ACTIVE";
        this.updatedAt = System.currentTimeMillis();
    }
    
    /**
     * 停用模板
     */
    public void deactivate() {
        this.status = "INACTIVE";
        this.updatedAt = System.currentTimeMillis();
    }
    
    /**
     * 获取支持的模型类型列表
     */
    public List<String> getModelTypesList() {
        if (modelTypes == null || modelTypes.trim().isEmpty()) {
            return List.of();
        }
        return Arrays.asList(modelTypes.split(","));
    }
    
    /**
     * 设置支持的模型类型列表
     */
    public void setModelTypesList(List<String> modelTypesList) {
        if (modelTypesList == null || modelTypesList.isEmpty()) {
            this.modelTypes = "";
        } else {
            this.modelTypes = String.join(",", modelTypesList);
        }
    }
    
    /**
     * 获取变量映射
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getVariablesMap() {
        if (variables == null || variables.trim().isEmpty()) {
            return Map.of();
        }
        try {
            return JSON.parseObject(variables, Map.class);
        } catch (Exception e) {
            return Map.of();
        }
    }
    
    /**
     * 设置变量映射
     */
    public void setVariablesMap(Map<String, Object> variablesMap) {
        if (variablesMap == null || variablesMap.isEmpty()) {
            this.variables = "{}";
        } else {
            this.variables = JSON.toJSONString(variablesMap);
        }
    }
    
    /**
     * 渲染模板内容，替换变量
     */
    public String render(Map<String, Object> params) {
        if (templateContent == null || templateContent.trim().isEmpty()) {
            return "";
        }
        
        String result = templateContent;
        if (params != null && !params.isEmpty()) {
            // 使用正则表达式替换 {{变量名}} 格式的占位符
            Pattern pattern = Pattern.compile("\\{\\{([^}]+)\\}\\}");
            Matcher matcher = pattern.matcher(result);
            
            StringBuffer sb = new StringBuffer();
            while (matcher.find()) {
                String varName = matcher.group(1).trim();
                Object value = params.get(varName);
                String replacement = value != null ? value.toString() : "";
                matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
            }
            matcher.appendTail(sb);
            result = sb.toString();
        }
        
        return result;
    }
    
    /**
     * 检查模板是否支持指定的模型类型
     */
    public boolean supportsModelType(String modelType) {
        if (modelType == null || modelType.trim().isEmpty()) {
            return false;
        }
        
        List<String> supportedTypes = getModelTypesList();
        return supportedTypes.isEmpty() || // 空列表表示支持所有类型
               supportedTypes.contains(modelType.toUpperCase()) ||
               supportedTypes.contains("ALL");
    }
}