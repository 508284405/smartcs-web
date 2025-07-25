package com.leyue.smartcs.dto.model;

import com.alibaba.cola.dto.Command;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 创建模型Prompt模板命令
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ModelPromptTemplateCreateCmd extends Command {

    /**
     * 模板键（唯一标识）
     */
    @NotEmpty(message = "模板键不能为空")
    private String templateKey;

    /**
     * 模板名称
     */
    @NotEmpty(message = "模板名称不能为空")
    private String templateName;

    /**
     * 模板内容
     */
    @NotEmpty(message = "模板内容不能为空")
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
}