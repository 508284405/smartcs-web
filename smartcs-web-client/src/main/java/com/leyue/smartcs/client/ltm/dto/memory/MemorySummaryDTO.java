package com.leyue.smartcs.client.ltm.dto.memory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 记忆摘要DTO
 * 用户记忆的统计和概览信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemorySummaryDTO {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 情景记忆总数
     */
    private Long totalEpisodicMemories;

    /**
     * 语义记忆总数
     */
    private Long totalSemanticMemories;

    /**
     * 程序性记忆总数
     */
    private Long totalProceduralMemories;

    /**
     * 高重要性记忆数量
     */
    private Long highImportanceCount;

    /**
     * 最近7天新增记忆数量
     */
    private Long recentMemoriesCount;

    /**
     * 平均记忆重要性评分
     */
    private Double averageImportanceScore;

    /**
     * 最活跃的记忆类型
     */
    private String mostActiveMemoryType;

    /**
     * 记忆形成频率（每天）
     */
    private Double memoryFormationRate;

    /**
     * 最早记忆时间
     */
    private Long earliestMemoryTime;

    /**
     * 最新记忆时间
     */
    private Long latestMemoryTime;

    /**
     * 存储空间占用（MB）
     */
    private Double storageUsageMB;

    /**
     * 记忆分类统计
     * key: 分类名称, value: 数量
     */
    private Map<String, Long> memoryCategoryStats;

    /**
     * 学习偏好分析
     */
    private PreferenceAnalysisDTO preferenceAnalysis;

    /**
     * 记忆健康度评分（0-100）
     */
    private Integer memoryHealthScore;

    /**
     * 个性化程度评分（0-100）
     */
    private Integer personalizationScore;

    /**
     * 最近访问的记忆概念
     */
    private java.util.List<String> recentConcepts;

    /**
     * 推荐优化建议
     */
    private java.util.List<String> optimizationSuggestions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PreferenceAnalysisDTO {
        /**
         * 主要兴趣领域
         */
        private java.util.List<String> primaryInterests;

        /**
         * 沟通风格偏好
         */
        private String communicationStyle;

        /**
         * 学习模式偏好
         */
        private String learningMode;

        /**
         * 响应详细程度偏好
         */
        private String responseDetailLevel;

        /**
         * 偏好置信度
         */
        private Double preferenceConfidence;
    }
}