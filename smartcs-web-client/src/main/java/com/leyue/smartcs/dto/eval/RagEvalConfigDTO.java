package com.leyue.smartcs.dto.eval;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * RAG评估配置DTO
 * 
 * @author Claude
 * @since 1.0.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RagEvalConfigDTO {
    
    /**
     * 配置ID
     */
    private String configId;
    
    /**
     * 配置名称
     */
    private String configName;
    
    /**
     * 默认模型ID
     */
    private Long defaultModelId;
    
    /**
     * 默认数据集ID
     */
    private String defaultDatasetId;
    
    /**
     * 评估超时时间（秒）
     */
    private Integer evaluationTimeout;
    
    /**
     * 最大并发评估数
     */
    private Integer maxConcurrentEvaluations;
    
    /**
     * 默认评估参数
     */
    private Map<String, Object> defaultEvaluationParams;
    
    /**
     * 支持的评估指标
     */
    private List<String> supportedMetrics;
    
    /**
     * 评估阈值配置
     */
    private Map<String, Double> evaluationThresholds;
    
    /**
     * 质量等级配置
     */
    private Map<String, QualityLevelConfig> qualityLevels;
    
    /**
     * 报告配置
     */
    private ReportConfig reportConfig;
    
    /**
     * 通知配置
     */
    private NotificationConfig notificationConfig;
    
    /**
     * 质量等级配置
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class QualityLevelConfig {
        
        /**
         * 等级名称
         */
        private String levelName;
        
        /**
         * 最小分数
         */
        private Double minScore;
        
        /**
         * 最大分数
         */
        private Double maxScore;
        
        /**
         * 等级描述
         */
        private String description;
        
        /**
         * 等级颜色
         */
        private String color;
    }
    
    /**
     * 报告配置
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ReportConfig {
        
        /**
         * 默认报告格式
         */
        private String defaultReportFormat;
        
        /**
         * 支持的报告格式
         */
        private List<String> supportedFormats;
        
        /**
         * 报告模板配置
         */
        private Map<String, String> reportTemplates;
        
        /**
         * 自动生成报告
         */
        private Boolean autoGenerateReports;
        
        /**
         * 报告保存路径
         */
        private String reportSavePath;
    }
    
    /**
     * 通知配置
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class NotificationConfig {
        
        /**
         * 启用邮件通知
         */
        private Boolean enableEmailNotification;
        
        /**
         * 启用Webhook通知
         */
        private Boolean enableWebhookNotification;
        
        /**
         * 通知收件人
         */
        private List<String> notificationRecipients;
        
        /**
         * Webhook URL
         */
        private String webhookUrl;
        
        /**
         * 通知触发条件
         */
        private List<String> notificationTriggers;
    }
}
