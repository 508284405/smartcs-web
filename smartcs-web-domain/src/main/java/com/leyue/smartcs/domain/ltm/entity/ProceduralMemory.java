package com.leyue.smartcs.domain.ltm.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 程序性记忆领域实体
 * 存储用户行为模式、偏好和规则
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProceduralMemory {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 模式类型：preference/rule/habit/response_style
     */
    private String patternType;

    /**
     * 模式名称
     */
    private String patternName;

    /**
     * 模式描述
     */
    private String patternDescription;

    /**
     * 触发条件，JSON格式存储
     */
    private Map<String, Object> triggerConditions;

    /**
     * 行为模板或响应模式
     */
    private String actionTemplate;

    /**
     * 成功执行次数
     */
    private Integer successCount;

    /**
     * 失败执行次数
     */
    private Integer failureCount;

    /**
     * 成功率
     */
    private Double successRate;

    /**
     * 最后触发时间
     */
    private Long lastTriggeredAt;

    /**
     * 学习率，影响模式更新速度
     */
    private Double learningRate;

    /**
     * 是否活跃 1=活跃 0=休眠
     */
    private Boolean isActive;

    /**
     * 创建时间
     */
    private Long createdAt;

    /**
     * 更新时间
     */
    private Long updatedAt;

    /**
     * 模式类型枚举
     */
    public static class PatternType {
        public static final String PREFERENCE = "preference";
        public static final String RULE = "rule";
        public static final String HABIT = "habit";
        public static final String RESPONSE_STYLE = "response_style";
    }

    /**
     * 是否为有效模式
     */
    public boolean isEffective() {
        return successRate != null && successRate >= 0.7;
    }

    /**
     * 是否为高频模式
     */
    public boolean isHighFrequency() {
        if (successCount == null || failureCount == null) {
            return false;
        }
        return (successCount + failureCount) >= 10;
    }

    /**
     * 是否需要学习调整
     */
    public boolean needsAdjustment() {
        return successRate != null && successRate < 0.5 && isHighFrequency();
    }

    /**
     * 记录成功执行
     */
    public void recordSuccess() {
        if (this.successCount == null) {
            this.successCount = 0;
        }
        this.successCount++;
        updateSuccessRate();
        this.lastTriggeredAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    /**
     * 记录失败执行
     */
    public void recordFailure() {
        if (this.failureCount == null) {
            this.failureCount = 0;
        }
        this.failureCount++;
        updateSuccessRate();
        this.lastTriggeredAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    /**
     * 更新成功率
     */
    private void updateSuccessRate() {
        if (successCount == null || failureCount == null) {
            return;
        }
        
        int totalAttempts = successCount + failureCount;
        if (totalAttempts > 0) {
            double newSuccessRate = (double) successCount / totalAttempts;
            
            // 应用学习率进行平滑更新
            if (this.successRate != null && learningRate != null) {
                this.successRate = this.successRate * (1 - learningRate) + newSuccessRate * learningRate;
            } else {
                this.successRate = newSuccessRate;
            }
        }
        
        // 根据成功率调整活跃状态
        updateActiveStatus();
    }

    /**
     * 更新活跃状态
     */
    private void updateActiveStatus() {
        if (successRate == null) {
            return;
        }
        
        // 成功率低于30%且尝试次数超过20次，则设为非活跃
        if (successRate < 0.3 && getTotalAttempts() > 20) {
            this.isActive = false;
        }
        // 成功率高于70%则保持活跃
        else if (successRate >= 0.7) {
            this.isActive = true;
        }
    }

    /**
     * 获取总尝试次数
     */
    public int getTotalAttempts() {
        int success = successCount != null ? successCount : 0;
        int failure = failureCount != null ? failureCount : 0;
        return success + failure;
    }

    /**
     * 激活模式
     */
    public void activate() {
        this.isActive = true;
        this.updatedAt = System.currentTimeMillis();
    }

    /**
     * 停用模式
     */
    public void deactivate() {
        this.isActive = false;
        this.updatedAt = System.currentTimeMillis();
    }

    /**
     * 添加触发条件
     */
    public void addTriggerCondition(String key, Object value) {
        if (this.triggerConditions != null) {
            this.triggerConditions.put(key, value);
        }
        this.updatedAt = System.currentTimeMillis();
    }

    /**
     * 检查是否满足触发条件
     */
    public boolean matchesTriggerConditions(Map<String, Object> context) {
        if (triggerConditions == null || triggerConditions.isEmpty()) {
            return true;
        }
        
        if (context == null) {
            return false;
        }
        
        // 简单的条件匹配逻辑
        for (Map.Entry<String, Object> condition : triggerConditions.entrySet()) {
            Object contextValue = context.get(condition.getKey());
            Object conditionValue = condition.getValue();
            
            if (contextValue == null || !contextValue.equals(conditionValue)) {
                return false;
            }
        }
        
        return true;
    }
}