package com.leyue.smartcs.client.ltm.dto.memory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 程序性记忆DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProceduralMemoryDTO {

    /**
     * 记忆ID
     */
    private Long id;

    /**
     * 模式类型
     */
    private PatternType patternType;

    /**
     * 模式名称
     */
    private String patternName;

    /**
     * 模式描述
     */
    private String patternDescription;

    /**
     * 成功次数
     */
    private Integer successCount;

    /**
     * 失败次数
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
     * 是否活跃
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
     * 触发条件
     */
    private Map<String, Object> triggerConditions;

    /**
     * 行为模板
     */
    private String actionTemplate;

    /**
     * 学习率
     */
    private Double learningRate;

    /**
     * 模式强度
     */
    private PatternStrength patternStrength;

    /**
     * 用户手动设置
     */
    private Boolean isUserDefined;

    /**
     * 应用场景
     */
    private java.util.List<String> applicationScenarios;

    /**
     * 效果评分
     */
    private EffectivenessRating effectivenessRating;

    /**
     * 相关偏好标签
     */
    private java.util.List<String> preferenceTags;

    /**
     * 模式类型枚举
     */
    public enum PatternType {
        PREFERENCE("偏好", "个人偏好和喜好"),
        RULE("规则", "行为规则和约束"),
        HABIT("习惯", "行为习惯和模式"),
        RESPONSE_STYLE("响应风格", "沟通和响应风格");

        private final String name;
        private final String description;

        PatternType(String name, String description) {
            this.name = name;
            this.description = description;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 模式强度
     */
    public enum PatternStrength {
        WEAK("弱", "偶尔表现"),
        MODERATE("中", "经常表现"),
        STRONG("强", "稳定表现"),
        VERY_STRONG("很强", "一致性表现");

        private final String level;
        private final String description;

        PatternStrength(String level, String description) {
            this.level = level;
            this.description = description;
        }

        public String getLevel() {
            return level;
        }

        public String getDescription() {
            return description;
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EffectivenessRating {
        /**
         * 用户满意度 (1-5)
         */
        private Integer userSatisfaction;

        /**
         * 系统准确度 (0.0-1.0)
         */
        private Double systemAccuracy;

        /**
         * 响应相关性 (0.0-1.0)
         */
        private Double responseRelevance;

        /**
         * 最后评估时间
         */
        private Long lastEvaluatedAt;

        /**
         * 评估备注
         */
        private String evaluationNote;
    }
}