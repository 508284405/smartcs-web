package com.leyue.smartcs.dto.moderation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

/**
 * 审核策略模板DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ModerationPolicyTemplateDTO {

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
     * 模板类型
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
     * 支持的语言
     */
    private String language;

    /**
     * 模板变量定义
     */
    private Map<String, Object> variables;

    /**
     * 默认变量值
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
     * 创建时间
     */
    private Long createdAt;

    /**
     * 更新时间
     */
    private Long updatedAt;
}