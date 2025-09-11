package com.leyue.smartcs.dto.model;

import com.alibaba.cola.dto.DTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 模型Prompt模板DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ModelPromptTemplateDTO extends DTO {

    /**
     * 模板ID
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
     * 状态（ACTIVE, INACTIVE）
     */
    private String status;

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
}