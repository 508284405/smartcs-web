package com.leyue.smartcs.domain.moderation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.leyue.smartcs.domain.moderation.enums.SeverityLevel;
import com.leyue.smartcs.domain.moderation.enums.ActionType;
import java.util.List;
import java.util.Map;

/**
 * 审核策略配置领域实体
 * 定义了特定场景下的审核规则和维度组合
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ModerationPolicy {

    /**
     * 策略ID
     */
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
     * 适用场景（如：USER_CHAT, BOT_REPLY, CONTENT_PUBLISH等）
     */
    private String scenario;

    /**
     * 策略类型（如：STANDARD, STRICT, LENIENT）
     */
    private String policyType;

    /**
     * 默认风险等级
     */
    private SeverityLevel defaultRiskLevel;

    /**
     * 默认处理动作
     */
    private ActionType defaultAction;

    /**
     * 是否启用
     */
    private Boolean isActive;

    /**
     * 优先级（数值越小优先级越高）
     */
    private Integer priority;

    /**
     * 策略配置参数（JSON格式存储）
     */
    private Map<String, Object> configParams;

    /**
     * 审核维度列表
     */
    private List<ModerationDimension> dimensions;

    /**
     * 关联的prompt模板ID
     */
    private Long templateId;

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
     * 验证策略数据的完整性
     */
    public boolean isValid() {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        if (code == null || code.trim().isEmpty()) {
            return false;
        }
        if (scenario == null || scenario.trim().isEmpty()) {
            return false;
        }
        if (defaultRiskLevel == null || defaultAction == null) {
            return false;
        }
        return true;
    }

    /**
     * 判断策略是否适用于指定场景
     */
    public boolean isApplicableForScenario(String targetScenario) {
        return scenario != null && scenario.equalsIgnoreCase(targetScenario);
    }

    /**
     * 获取指定维度的配置
     */
    public ModerationDimension getDimensionByCode(String dimensionCode) {
        if (dimensions == null || dimensionCode == null) {
            return null;
        }
        return dimensions.stream()
                .filter(dimension -> dimensionCode.equals(dimension.getCode()))
                .findFirst()
                .orElse(null);
    }

    /**
     * 获取启用的维度列表
     */
    public List<ModerationDimension> getActiveDimensions() {
        if (dimensions == null) {
            return List.of();
        }
        return dimensions.stream()
                .filter(dimension -> Boolean.TRUE.equals(dimension.getIsActive()))
                .sorted((d1, d2) -> Integer.compare(
                    d1.getSortOrder() != null ? d1.getSortOrder() : Integer.MAX_VALUE,
                    d2.getSortOrder() != null ? d2.getSortOrder() : Integer.MAX_VALUE
                ))
                .toList();
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
     * 创建新的策略实体（工厂方法）
     */
    public static ModerationPolicy create(String name, String code, String description,
                                        String scenario, String policyType,
                                        SeverityLevel defaultRiskLevel, ActionType defaultAction,
                                        Integer priority, String createdBy) {
        long currentTime = System.currentTimeMillis();
        return ModerationPolicy.builder()
                .name(name)
                .code(code)
                .description(description)
                .scenario(scenario)
                .policyType(policyType)
                .defaultRiskLevel(defaultRiskLevel)
                .defaultAction(defaultAction)
                .priority(priority != null ? priority : 100)
                .isActive(true)
                .configParams(new java.util.HashMap<>())
                .createdBy(createdBy)
                .updatedBy(createdBy)
                .createdAt(currentTime)
                .updatedAt(currentTime)
                .build();
    }

    /**
     * 更新策略信息
     */
    public void update(String name, String description, String scenario, String policyType,
                      SeverityLevel defaultRiskLevel, ActionType defaultAction,
                      Integer priority, Map<String, Object> configParams, String updatedBy) {
        this.name = name;
        this.description = description;
        this.scenario = scenario;
        this.policyType = policyType;
        this.defaultRiskLevel = defaultRiskLevel;
        this.defaultAction = defaultAction;
        this.priority = priority;
        if (configParams != null) {
            this.configParams = new java.util.HashMap<>(configParams);
        }
        this.updatedBy = updatedBy;
        this.updatedAt = System.currentTimeMillis();
    }

    /**
     * 启用策略
     */
    public void enable(String updatedBy) {
        this.isActive = true;
        this.updatedBy = updatedBy;
        this.updatedAt = System.currentTimeMillis();
    }

    /**
     * 禁用策略
     */
    public void disable(String updatedBy) {
        this.isActive = false;
        this.updatedBy = updatedBy;
        this.updatedAt = System.currentTimeMillis();
    }

    /**
     * 关联prompt模板
     */
    public void bindTemplate(Long templateId, String updatedBy) {
        this.templateId = templateId;
        this.updatedBy = updatedBy;
        this.updatedAt = System.currentTimeMillis();
    }

    /**
     * 比较优先级
     */
    public int compareTo(ModerationPolicy other) {
        if (other == null) {
            return 1;
        }
        
        // 首先按优先级排序（数值越小优先级越高）
        int priorityCompare = Integer.compare(
            this.priority != null ? this.priority : Integer.MAX_VALUE,
            other.priority != null ? other.priority : Integer.MAX_VALUE
        );
        if (priorityCompare != 0) {
            return priorityCompare;
        }
        
        // 然后按创建时间排序（较新的在前）
        if (this.createdAt != null && other.createdAt != null) {
            return Long.compare(other.createdAt, this.createdAt);
        }
        
        // 最后按名称排序
        return this.name.compareTo(other.name);
    }
}