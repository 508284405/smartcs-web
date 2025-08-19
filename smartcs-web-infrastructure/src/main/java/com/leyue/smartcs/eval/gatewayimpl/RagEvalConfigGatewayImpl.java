package com.leyue.smartcs.eval.gatewayimpl;

import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.domain.eval.gateway.RagEvalConfigGateway;
import com.leyue.smartcs.dto.eval.RagEvalConfigGetQry;
import com.leyue.smartcs.dto.eval.RagEvalConfigDTO;
import com.leyue.smartcs.dto.eval.RagEvalConfigUpdateCmd;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RAG评估配置Gateway实现
 * 当前为简化实现，基于配置文件和内存存储
 * 后续可扩展为数据库存储
 * 
 * @author Claude
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RagEvalConfigGatewayImpl implements RagEvalConfigGateway {

    @Value("${rag.eval.default.batch-size:50}")
    private int defaultBatchSize;

    @Value("${rag.eval.default.timeout:300}")
    private int defaultTimeout;

    @Value("${rag.eval.default.concurrent-workers:2}")
    private int defaultConcurrentWorkers;

    @Value("${rag.eval.default.pass-threshold:0.7}")
    private double defaultPassThreshold;

    // 简化实现：使用内存存储配置
    // 实际应用中应该存储在数据库或配置中心
    private final Map<String, Map<String, Object>> configCache = new HashMap<>();

    @Override
    public RagEvalConfigDTO getEvalConfig(RagEvalConfigGetQry qry) {
        try {
            log.debug("获取评估配置: configId={}, configName={}", qry.getConfigId(), qry.getConfigName());

            // 构建配置键
            String configKey = buildConfigKey(qry.getConfigId(), qry.getConfigName());
            
            // 从缓存获取配置，如果不存在则使用默认配置
            Map<String, Object> config = configCache.getOrDefault(configKey, getDefaultConfig());

            // 构建配置DTO
            RagEvalConfigDTO dto = RagEvalConfigDTO.builder()
                    .configId(qry.getConfigId())
                    .configName(qry.getConfigName() != null ? qry.getConfigName() : "default")
                    .defaultModelId(null)
                    .defaultDatasetId(null)
                    .evaluationTimeout((Integer) config.getOrDefault("timeout", defaultTimeout))
                    .maxConcurrentEvaluations((Integer) config.getOrDefault("concurrentWorkers", defaultConcurrentWorkers))
                    .defaultEvaluationParams(config)
                    .supportedMetrics(List.of("faithfulness", "relevancy", "coherence", "fluency"))
                    .evaluationThresholds(Map.of("pass_threshold", (Double) config.getOrDefault("passThreshold", defaultPassThreshold)))
                    .qualityLevels(new HashMap<>())
                    .reportConfig(RagEvalConfigDTO.ReportConfig.builder()
                            .defaultReportFormat("JSON")
                            .supportedFormats(List.of("JSON", "CSV", "PDF"))
                            .autoGenerateReports(true)
                            .reportSavePath("/tmp/reports")
                            .build())
                    .notificationConfig(RagEvalConfigDTO.NotificationConfig.builder()
                            .enableEmailNotification(false)
                            .enableWebhookNotification(false)
                            .notificationRecipients(List.of())
                            .notificationTriggers(List.of())
                            .build())
                    .build();

            return dto;
        } catch (Exception e) {
            log.error("获取评估配置失败", e);
            throw new BizException("GET_EVAL_CONFIG_FAILED", "获取评估配置失败: " + e.getMessage());
        }
    }

    @Override
    public void updateEvalConfig(RagEvalConfigUpdateCmd cmd) {
        try {
            log.info("更新评估配置: configId={}, configName={}", cmd.getConfigId(), cmd.getConfigName());

            // 构建配置键
            String configKey = buildConfigKey(cmd.getConfigId(), cmd.getConfigName());
            
            // 获取现有配置或创建新配置
            Map<String, Object> config = new HashMap<>(configCache.getOrDefault(configKey, getDefaultConfig()));

            // 更新基础配置
            if (cmd.getEvaluationTimeout() != null) {
                config.put("timeout", cmd.getEvaluationTimeout());
            }
            if (cmd.getMaxConcurrentEvaluations() != null) {
                config.put("concurrentWorkers", cmd.getMaxConcurrentEvaluations());
            }
            if (cmd.getEvaluationThresholds() != null) {
                config.put("thresholds", cmd.getEvaluationThresholds());
            }

            // 更新其他配置
            if (cmd.getDefaultEvaluationParams() != null) {
                config.putAll(cmd.getDefaultEvaluationParams());
            }
            if (cmd.getSupportedMetrics() != null) {
                config.put("supportedMetrics", cmd.getSupportedMetrics());
            }
            if (cmd.getQualityLevels() != null) {
                config.put("qualityLevels", cmd.getQualityLevels());
            }
            if (cmd.getReportConfig() != null) {
                config.put("reportConfig", cmd.getReportConfig());
            }
            if (cmd.getNotificationConfig() != null) {
                config.put("notificationConfig", cmd.getNotificationConfig());
            }

            // 保存配置到缓存
            configCache.put(configKey, config);

            log.info("评估配置更新成功: configKey={}", configKey);
        } catch (Exception e) {
            log.error("更新评估配置失败", e);
            throw new BizException("UPDATE_EVAL_CONFIG_FAILED", "更新评估配置失败: " + e.getMessage());
        }
    }

    private String buildConfigKey(String configId, String configName) {
        if (configId != null) {
            return "config:" + configId;
        } else if (configName != null) {
            return "config:name:" + configName;
        } else {
            return "config:default";
        }
    }

    private Map<String, Object> getDefaultConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("batchSize", defaultBatchSize);
        config.put("timeout", defaultTimeout);
        config.put("concurrentWorkers", defaultConcurrentWorkers);
        config.put("passThreshold", defaultPassThreshold);

        // 默认检索配置
        Map<String, Object> retrievalConfig = new HashMap<>();
        retrievalConfig.put("topK", 5);
        retrievalConfig.put("similarityThreshold", 0.7);
        retrievalConfig.put("embeddingModel", "text-embedding-ada-002");
        config.put("retrievalConfig", retrievalConfig);

        // 默认生成配置
        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("maxTokens", 1000);
        generationConfig.put("temperature", 0.7);
        generationConfig.put("topP", 0.9);
        config.put("generationConfig", generationConfig);

        // 默认评估指标配置
        Map<String, Object> metricsConfig = new HashMap<>();
        metricsConfig.put("enableRetrievalMetrics", true);
        metricsConfig.put("enableGenerationMetrics", true);
        metricsConfig.put("customMetrics", new HashMap<>());
        config.put("metricsConfig", metricsConfig);

        return config;
    }
}