package com.leyue.smartcs.dto.eval;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * RAG评估运行启动命令
 * 
 * @author Claude
 * @since 1.0.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RagEvalRunStartCmd {
    
    /**
     * 数据集ID
     */
    private String datasetId;
    
    /**
     * 应用ID
     */
    private Long appId;
    
    /**
     * 模型ID
     */
    private Long modelId;
    
    /**
     * 运行名称
     */
    private String runName;
    
    /**
     * 运行描述
     */
    private String runDescription;
    
    /**
     * 运行类型：FULL, INCREMENTAL, SAMPLE
     */
    private String runType;
    
    /**
     * 测试用例ID列表（为空则使用全部）
     */
    private List<String> caseIds;
    
    /**
     * 评估参数配置
     */
    private Map<String, Object> evaluationParams;
    
    /**
     * 并发数限制
     */
    private Integer maxConcurrency;
    
    /**
     * 超时时间（秒）
     */
    private Integer timeout;
    
    /**
     * 是否启用详细日志
     */
    private Boolean enableDetailedLogging;
    
    /**
     * 是否保存中间结果
     */
    private Boolean saveIntermediateResults;
    
    /**
     * 通知配置
     */
    private NotificationConfig notificationConfig;
    
    /**
     * 扩展配置
     */
    private Map<String, Object> extraConfig;
    
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
