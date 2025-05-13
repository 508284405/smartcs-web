package com.leyue.smartcs.domain.bot.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Prompt模板领域模型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromptTemplate {
    
    /**
     * 模板ID
     */
    private Long id;
    
    /**
     * 模板标识
     */
    private String templateKey;
    
    /**
     * 模板内容
     */
    private String templateContent;
    
    /**
     * 创建人
     */
    private String createdBy;
    
    /**
     * 创建时间（毫秒时间戳）
     */
    private Long createdAt;
    
    /**
     * 最后更新人
     */
    private String updatedBy;
    
    /**
     * 最后更新时间（毫秒时间戳）
     */
    private Long updatedAt;
    
    /**
     * 根据变量填充模板
     * @param variables 变量映射
     * @return 填充后的Prompt
     */
    public String format(Map<String, Object> variables) {
        String result = templateContent;
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            Object value = entry.getValue();
            result = result.replace(placeholder, value != null ? value.toString() : "");
        }
        return result;
    }
} 