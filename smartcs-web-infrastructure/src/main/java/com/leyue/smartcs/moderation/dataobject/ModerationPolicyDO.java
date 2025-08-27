package com.leyue.smartcs.moderation.dataobject;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 审核策略配置数据对象
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("t_moderation_policy")
public class ModerationPolicyDO {

    /**
     * 策略ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 策略名称
     */
    private String name;

    /**
     * 策略编码，用于程序识别
     */
    private String code;

    /**
     * 策略描述
     */
    private String description;

    /**
     * 适用场景
     */
    private String scenario;

    /**
     * 策略类型
     */
    @TableField("policy_type")
    private String policyType;

    /**
     * 默认风险等级
     */
    @TableField("default_risk_level")
    private String defaultRiskLevel;

    /**
     * 默认处理动作
     */
    @TableField("default_action")
    private String defaultAction;

    /**
     * 是否启用
     */
    @TableField("is_active")
    private Boolean isActive;

    /**
     * 优先级
     */
    private Integer priority;

    /**
     * 策略配置参数（JSON格式）
     */
    @TableField("config_params")
    private String configParams;

    /**
     * 关联的prompt模板ID
     */
    @TableField("template_id")
    private Long templateId;

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