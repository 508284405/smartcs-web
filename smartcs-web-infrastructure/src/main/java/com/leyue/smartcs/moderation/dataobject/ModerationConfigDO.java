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
 * 内容审核配置数据对象
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("t_moderation_config")
public class ModerationConfigDO {

    /**
     * 配置ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 配置键
     */
    @TableField("config_key")
    private String configKey;

    /**
     * 配置名称
     */
    @TableField("config_name")
    private String configName;

    /**
     * 配置值
     */
    @TableField("config_value")
    private String configValue;

    /**
     * 配置类型
     */
    @TableField("config_type")
    private String configType;

    /**
     * 配置描述
     */
    @TableField("description")
    private String description;

    /**
     * 配置分类
     */
    @TableField("category")
    private String category;

    /**
     * 是否启用
     */
    @TableField("is_active")
    private Integer isActive;

    /**
     * 是否系统配置
     */
    @TableField("is_system")
    private Integer isSystem;

    /**
     * 验证规则
     */
    @TableField("validation_rule")
    private String validationRule;

    /**
     * 默认值
     */
    @TableField("default_value")
    private String defaultValue;

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