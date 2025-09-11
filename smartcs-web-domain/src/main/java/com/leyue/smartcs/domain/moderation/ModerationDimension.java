package com.leyue.smartcs.domain.moderation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.leyue.smartcs.domain.moderation.enums.SeverityLevel;
import com.leyue.smartcs.domain.moderation.enums.ActionType;
import java.util.Map;

/**
 * 审核维度配置领域实体
 * 定义了具体的审核检查维度和规则
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ModerationDimension {

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
    private SeverityLevel severityLevel;

    /**
     * 处理动作类型
     */
    private ActionType actionType;

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
     * 维度分类（如：CONTENT_SAFETY, PRIVACY_PROTECTION等）
     */
    private String category;

    /**
     * 维度配置参数（JSON格式存储）
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
     * 创建时间（毫秒时间戳）
     */
    private Long createdAt;

    /**
     * 更新时间（毫秒时间戳）
     */
    private Long updatedAt;

    /**
     * 验证维度数据的完整性
     */
    public boolean isValid() {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        if (code == null || code.trim().isEmpty()) {
            return false;
        }
        if (severityLevel == null || actionType == null) {
            return false;
        }
        if (confidenceThreshold != null && (confidenceThreshold < 0.0 || confidenceThreshold > 1.0)) {
            return false;
        }
        return true;
    }

    /**
     * 判断是否属于指定分类
     */
    public boolean belongsToCategory(String targetCategory) {
        return category != null && category.equalsIgnoreCase(targetCategory);
    }

    /**
     * 获取配置参数
     */
    public Object getConfigParam(String key) {
        return configParams != null ? configParams.get(key) : null;
    }

    /**
     * 设置配置参数
     */
    public void setConfigParam(String key, Object value) {
        if (configParams == null) {
            configParams = new java.util.HashMap<>();
        }
        configParams.put(key, value);
    }

    /**
     * 获取有效的置信度阈值
     */
    public double getEffectiveConfidenceThreshold() {
        return confidenceThreshold != null ? confidenceThreshold : 0.5;
    }

    /**
     * 检查置信度是否超过阈值
     */
    public boolean isConfidenceAboveThreshold(double confidence) {
        return confidence >= getEffectiveConfidenceThreshold();
    }

    /**
     * 创建新的维度实体（工厂方法）
     */
    public static ModerationDimension create(String name, String code, String description,
                                           String checkGuideline, SeverityLevel severityLevel,
                                           ActionType actionType, String category,
                                           Double confidenceThreshold, Integer sortOrder,
                                           Long categoryId, String createdBy) {
        long currentTime = System.currentTimeMillis();
        return ModerationDimension.builder()
                .name(name)
                .code(code)
                .description(description)
                .checkGuideline(checkGuideline)
                .severityLevel(severityLevel)
                .actionType(actionType)
                .category(category)
                .confidenceThreshold(confidenceThreshold)
                .sortOrder(sortOrder != null ? sortOrder : 0)
                .categoryId(categoryId)
                .isActive(true)
                .configParams(new java.util.HashMap<>())
                .createdBy(createdBy)
                .updatedBy(createdBy)
                .createdAt(currentTime)
                .updatedAt(currentTime)
                .build();
    }

    /**
     * 更新维度信息
     */
    public void update(String name, String description, String checkGuideline,
                      SeverityLevel severityLevel, ActionType actionType, String category,
                      Double confidenceThreshold, Integer sortOrder, Long categoryId,
                      Map<String, Object> configParams, String updatedBy) {
        this.name = name;
        this.description = description;
        this.checkGuideline = checkGuideline;
        this.severityLevel = severityLevel;
        this.actionType = actionType;
        this.category = category;
        this.confidenceThreshold = confidenceThreshold;
        this.sortOrder = sortOrder;
        this.categoryId = categoryId;
        if (configParams != null) {
            this.configParams = new java.util.HashMap<>(configParams);
        }
        this.updatedBy = updatedBy;
        this.updatedAt = System.currentTimeMillis();
    }

    /**
     * 启用维度
     */
    public void enable(String updatedBy) {
        this.isActive = true;
        this.updatedBy = updatedBy;
        this.updatedAt = System.currentTimeMillis();
    }

    /**
     * 禁用维度
     */
    public void disable(String updatedBy) {
        this.isActive = false;
        this.updatedBy = updatedBy;
        this.updatedAt = System.currentTimeMillis();
    }

    /**
     * 比较排序权重
     */
    public int compareTo(ModerationDimension other) {
        if (other == null) {
            return 1;
        }
        
        // 首先按排序权重排序
        int sortCompare = Integer.compare(
            this.sortOrder != null ? this.sortOrder : Integer.MAX_VALUE,
            other.sortOrder != null ? other.sortOrder : Integer.MAX_VALUE
        );
        if (sortCompare != 0) {
            return sortCompare;
        }
        
        // 然后按严重程度排序（严重的在前）
        if (this.severityLevel != null && other.severityLevel != null) {
            int severityCompare = other.severityLevel.compareTo(this.severityLevel);
            if (severityCompare != 0) {
                return severityCompare;
            }
        }
        
        // 最后按名称排序
        return this.name.compareTo(other.name);
    }

    /**
     * 生成prompt描述文本
     */
    public String generatePromptDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append(name);
        if (description != null && !description.trim().isEmpty()) {
            sb.append(" - ").append(description);
        }
        if (checkGuideline != null && !checkGuideline.trim().isEmpty()) {
            sb.append(" (").append(checkGuideline).append(")");
        }
        return sb.toString();
    }
}