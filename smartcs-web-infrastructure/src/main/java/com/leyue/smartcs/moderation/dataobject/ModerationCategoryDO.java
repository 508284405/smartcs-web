package com.leyue.smartcs.moderation.dataobject;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * 内容审核违规分类数据对象
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("t_moderation_category")
public class ModerationCategoryDO {

    /**
     * 分类ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 父分类ID，NULL表示一级分类
     */
    @TableField("parent_id")
    private Long parentId;

    /**
     * 分类名称
     */
    @TableField("name")
    private String name;

    /**
     * 分类编码，用于程序识别
     */
    @TableField("code")
    private String code;

    /**
     * 分类描述
     */
    @TableField("description")
    private String description;

    /**
     * 严重程度级别
     */
    @TableField("severity_level")
    private String severityLevel;

    /**
     * 默认处理动作类型
     */
    @TableField("action_type")
    private String actionType;

    /**
     * 是否启用
     */
    @TableField("is_active")
    private Integer isActive;

    /**
     * 排序权重
     */
    @TableField("sort_order")
    private Integer sortOrder;

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
    @TableField("created_at")
    private Long createdAt;

    /**
     * 更新时间（毫秒时间戳）
     */
    @TableField("updated_at")
    private Long updatedAt;
}