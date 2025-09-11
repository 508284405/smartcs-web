package com.leyue.smartcs.moderation.dataobject;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

/**
 * 审核维度配置数据对象
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("t_moderation_dimension")
public class ModerationDimensionDO {

    /**
     * 维度ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 维度名称
     */
    private String name;

    /**
     * 维度编码，用于程序识别
     */
    private String code;

    /**
     * 维度描述
     */
    private String description;

    /**
     * 详细的检查指南
     */
    @TableField("check_guideline")
    private String checkGuideline;

    /**
     * 严重程度级别
     */
    @TableField("severity_level")
    private String severityLevel;

    /**
     * 处理动作类型
     */
    @TableField("action_type")
    private String actionType;

    /**
     * 置信度阈值（0.00-1.00）
     */
    @TableField("confidence_threshold")
    private BigDecimal confidenceThreshold;

    /**
     * 是否启用
     */
    @TableField("is_active")
    private Boolean isActive;

    /**
     * 排序权重
     */
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 维度分类
     */
    private String category;

    /**
     * 维度配置参数（JSON格式）
     */
    @TableField("config_params")
    private String configParams;

    /**
     * 关联的审核分类ID
     */
    @TableField("category_id")
    private Long categoryId;

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