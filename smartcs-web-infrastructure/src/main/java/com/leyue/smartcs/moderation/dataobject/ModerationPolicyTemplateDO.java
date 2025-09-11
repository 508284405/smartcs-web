package com.leyue.smartcs.moderation.dataobject;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 审核策略模板数据对象
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("t_moderation_policy_template")
public class ModerationPolicyTemplateDO {

    /**
     * 模板ID
     */
    @TableId(type = IdType.AUTO)
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
    @TableField("template_type")
    private String templateType;

    /**
     * 基础prompt模板内容
     */
    @TableField("prompt_template")
    private String promptTemplate;

    /**
     * 维度列表模板
     */
    @TableField("dimension_template")
    private String dimensionTemplate;

    /**
     * 响应格式模板
     */
    @TableField("response_template")
    private String responseTemplate;

    /**
     * 支持的语言
     */
    private String language;

    /**
     * 模板变量定义（JSON格式）
     */
    private String variables;

    /**
     * 默认变量值（JSON格式）
     */
    @TableField("default_values")
    private String defaultValues;

    /**
     * 是否启用
     */
    @TableField("is_active")
    private Boolean isActive;

    /**
     * 版本号
     */
    private String version;

    /**
     * 创建者
     */
    @TableField("created_by")
    private String createdBy;

    /**
     * 更新者
     */
    @TableField("updated_by")
    private String updatedBy;

    /**
     * 创建时间（毫秒时间戳）
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private Long createdAt;

    /**
     * 更新时间（毫秒时间戳）
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private Long updatedAt;
}