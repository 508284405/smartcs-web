package com.leyue.smartcs.moderation.dataobject;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

/**
 * 审核策略维度关联数据对象
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("t_moderation_policy_dimension")
public class ModerationPolicyDimensionDO {

    /**
     * 关联ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 策略ID
     */
    @TableField("policy_id")
    private Long policyId;

    /**
     * 维度ID
     */
    @TableField("dimension_id")
    private Long dimensionId;

    /**
     * 在该策略中是否启用
     */
    @TableField("is_active")
    private Boolean isActive;

    /**
     * 在该策略中的权重（0.00-1.00）
     */
    private BigDecimal weight;

    /**
     * 在该策略中的自定义阈值
     */
    @TableField("custom_threshold")
    private BigDecimal customThreshold;

    /**
     * 在该策略中的自定义动作
     */
    @TableField("custom_action")
    private String customAction;

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