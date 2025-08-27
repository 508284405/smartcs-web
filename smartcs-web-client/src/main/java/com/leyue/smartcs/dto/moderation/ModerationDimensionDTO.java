package com.leyue.smartcs.dto.moderation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

/**
 * 审核维度DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ModerationDimensionDTO {

    /**
     * 维度ID
     */
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
    private String checkGuideline;

    /**
     * 严重程度级别
     */
    private String severityLevel;

    /**
     * 处理动作类型
     */
    private String actionType;

    /**
     * 置信度阈值（0.0-1.0）
     */
    private Double confidenceThreshold;

    /**
     * 是否启用
     */
    private Boolean isActive;

    /**
     * 排序权重
     */
    private Integer sortOrder;

    /**
     * 维度分类
     */
    private String category;

    /**
     * 维度配置参数
     */
    private Map<String, Object> configParams;

    /**
     * 关联的审核分类ID
     */
    private Long categoryId;

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