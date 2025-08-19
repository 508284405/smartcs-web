package com.leyue.smartcs.eval.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * RAGAS服务客户端
 * 负责与外部Python RAGAS微服务进行交互，提供RAG评估指标计算功能
 * 
 * @author Claude
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RagasClient {
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    @Value("${eval.ragas.base-url:http://localhost:8080}")
    private String ragasBaseUrl;
    
    @Value("${eval.ragas.api-key:}")
    private String ragasApiKey;
    
    @Value("${eval.ragas.timeout-ms:60000}")
    private long timeoutMs;
    
    @Value("${eval.ragas.enabled:true}")
    private boolean ragasEnabled;
    
    /**
     * 测试RAGAS服务连接
     */
    public RagasHealthCheckResult testConnection() {
        if (!ragasEnabled) {
            return RagasHealthCheckResult.builder()
                    .healthy(false)
                    .message("RAGAS服务已禁用")
                    .build();
        }
        
        try {
            log.info("测试RAGAS服务连接: {}", ragasBaseUrl);
            
            HttpHeaders headers = createHeaders();
            HttpEntity<Void> request = new HttpEntity<>(headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                    ragasBaseUrl + "/health",
                    HttpMethod.GET,
                    request,
                    Map.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> body = response.getBody();
                return RagasHealthCheckResult.builder()
                        .healthy(true)
                        .message("RAGAS服务连接正常")
                        .version((String) body.get("version"))
                        .serviceInfo(body)
                        .build();
            } else {
                return RagasHealthCheckResult.builder()
                        .healthy(false)
                        .message("RAGAS服务响应异常: " + response.getStatusCode())
                        .build();
            }
            
        } catch (Exception e) {
            log.error("RAGAS服务连接测试失败", e);
            return RagasHealthCheckResult.builder()
                    .healthy(false)
                    .message("RAGAS服务连接失败: " + e.getMessage())
                    .build();
        }
    }
    
    /**
     * 创建评估任务
     */
    @Retryable(value = {RestClientException.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public RagasTaskResult createEvaluationTask(RagasEvaluationRequest request) {
        if (!ragasEnabled) {
            throw new RuntimeException("RAGAS服务已禁用，无法创建评估任务");
        }
        
        try {
            log.info("创建RAGAS评估任务，数据样本数量: {}", request.getSamples().size());
            
            HttpHeaders headers = createHeaders();
            HttpEntity<RagasEvaluationRequest> requestEntity = new HttpEntity<>(request, headers);
            
            ResponseEntity<RagasTaskResult> response = restTemplate.exchange(
                    ragasBaseUrl + "/api/v1/evaluation/tasks",
                    HttpMethod.POST,
                    requestEntity,
                    RagasTaskResult.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK || response.getStatusCode() == HttpStatus.CREATED) {
                RagasTaskResult result = response.getBody();
                log.info("RAGAS评估任务创建成功，任务ID: {}", result.getTaskId());
                return result;
            } else {
                throw new RuntimeException("RAGAS任务创建失败: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            log.error("创建RAGAS评估任务失败", e);
            throw new RuntimeException("创建RAGAS评估任务失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 查询任务状态
     */
    public RagasTaskStatus getTaskStatus(String taskId) {
        if (!ragasEnabled) {
            throw new RuntimeException("RAGAS服务已禁用，无法查询任务状态");
        }
        
        try {
            log.debug("查询RAGAS任务状态: {}", taskId);
            
            HttpHeaders headers = createHeaders();
            HttpEntity<Void> request = new HttpEntity<>(headers);
            
            ResponseEntity<RagasTaskStatus> response = restTemplate.exchange(
                    ragasBaseUrl + "/api/v1/evaluation/tasks/" + taskId + "/status",
                    HttpMethod.GET,
                    request,
                    RagasTaskStatus.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            } else {
                throw new RuntimeException("查询RAGAS任务状态失败: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            log.error("查询RAGAS任务状态失败: {}", taskId, e);
            throw new RuntimeException("查询RAGAS任务状态失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取评估结果
     */
    public RagasEvaluationResult getEvaluationResult(String taskId) {
        if (!ragasEnabled) {
            throw new RuntimeException("RAGAS服务已禁用，无法获取评估结果");
        }
        
        try {
            log.info("获取RAGAS评估结果: {}", taskId);
            
            HttpHeaders headers = createHeaders();
            HttpEntity<Void> request = new HttpEntity<>(headers);
            
            ResponseEntity<RagasEvaluationResult> response = restTemplate.exchange(
                    ragasBaseUrl + "/api/v1/evaluation/tasks/" + taskId + "/result",
                    HttpMethod.GET,
                    request,
                    RagasEvaluationResult.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK) {
                RagasEvaluationResult result = response.getBody();
                log.info("获取RAGAS评估结果成功，指标数量: {}", 
                        result.getAggregatedMetrics().size());
                return result;
            } else {
                throw new RuntimeException("获取RAGAS评估结果失败: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            log.error("获取RAGAS评估结果失败: {}", taskId, e);
            throw new RuntimeException("获取RAGAS评估结果失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 取消评估任务
     */
    public void cancelTask(String taskId) {
        if (!ragasEnabled) {
            log.warn("RAGAS服务已禁用，跳过取消任务操作");
            return;
        }
        
        try {
            log.info("取消RAGAS评估任务: {}", taskId);
            
            HttpHeaders headers = createHeaders();
            HttpEntity<Void> request = new HttpEntity<>(headers);
            
            ResponseEntity<Void> response = restTemplate.exchange(
                    ragasBaseUrl + "/api/v1/evaluation/tasks/" + taskId + "/cancel",
                    HttpMethod.POST,
                    request,
                    Void.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK) {
                log.info("RAGAS评估任务取消成功: {}", taskId);
            } else {
                log.warn("RAGAS评估任务取消失败: {} - {}", taskId, response.getStatusCode());
            }
            
        } catch (Exception e) {
            log.error("取消RAGAS评估任务失败: {}", taskId, e);
            // 取消任务失败不抛出异常，只记录日志
        }
    }
    
    /**
     * 轮询等待任务完成
     */
    public RagasEvaluationResult waitForTaskCompletion(String taskId, long maxWaitTimeMs) {
        if (!ragasEnabled) {
            throw new RuntimeException("RAGAS服务已禁用，无法等待任务完成");
        }
        
        long startTime = System.currentTimeMillis();
        long pollInterval = Math.min(5000L, maxWaitTimeMs / 20); // 轮询间隔，最小5秒
        
        try {
            log.info("开始等待RAGAS任务完成: {}，最大等待时间: {}ms", taskId, maxWaitTimeMs);
            
            while (System.currentTimeMillis() - startTime < maxWaitTimeMs) {
                RagasTaskStatus status = getTaskStatus(taskId);
                
                switch (status.getStatus()) {
                    case "completed":
                        log.info("RAGAS任务完成: {}", taskId);
                        return getEvaluationResult(taskId);
                    
                    case "failed":
                        String errorMsg = "RAGAS任务执行失败: " + taskId + 
                                        (status.getErrorMessage() != null ? " - " + status.getErrorMessage() : "");
                        log.error(errorMsg);
                        throw new RuntimeException(errorMsg);
                    
                    case "cancelled":
                        log.warn("RAGAS任务已被取消: {}", taskId);
                        throw new RuntimeException("RAGAS任务已被取消: " + taskId);
                    
                    case "pending":
                    case "running":
                        log.debug("RAGAS任务正在执行: {}，进度: {}%", taskId, status.getProgress());
                        break;
                    
                    default:
                        log.warn("RAGAS任务状态未知: {} - {}", taskId, status.getStatus());
                }
                
                // 等待下次轮询
                TimeUnit.MILLISECONDS.sleep(pollInterval);
            }
            
            // 超时
            log.error("等待RAGAS任务完成超时: {}", taskId);
            throw new RuntimeException("等待RAGAS任务完成超时: " + taskId);
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("等待RAGAS任务完成被中断: {}", taskId);
            throw new RuntimeException("等待RAGAS任务完成被中断: " + taskId, e);
        } catch (Exception e) {
            log.error("等待RAGAS任务完成失败: {}", taskId, e);
            throw new RuntimeException("等待RAGAS任务完成失败: " + taskId, e);
        }
    }
    
    /**
     * 创建HTTP请求头
     */
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        if (ragasApiKey != null && !ragasApiKey.trim().isEmpty()) {
            headers.set("Authorization", "Bearer " + ragasApiKey);
        }
        
        headers.set("User-Agent", "SmartCS-RAG-Eval/1.0.0");
        return headers;
    }
    
    // ====== 数据模型类 ======
    
    /**
     * RAGAS健康检查结果
     */
    public static class RagasHealthCheckResult {
        private boolean healthy;
        private String message;
        private String version;
        private Map<String, Object> serviceInfo;
        
        public static RagasHealthCheckResultBuilder builder() {
            return new RagasHealthCheckResultBuilder();
        }
        
        // Builder模式实现
        public static class RagasHealthCheckResultBuilder {
            private boolean healthy;
            private String message;
            private String version;
            private Map<String, Object> serviceInfo;
            
            public RagasHealthCheckResultBuilder healthy(boolean healthy) {
                this.healthy = healthy;
                return this;
            }
            
            public RagasHealthCheckResultBuilder message(String message) {
                this.message = message;
                return this;
            }
            
            public RagasHealthCheckResultBuilder version(String version) {
                this.version = version;
                return this;
            }
            
            public RagasHealthCheckResultBuilder serviceInfo(Map<String, Object> serviceInfo) {
                this.serviceInfo = serviceInfo;
                return this;
            }
            
            public RagasHealthCheckResult build() {
                RagasHealthCheckResult result = new RagasHealthCheckResult();
                result.healthy = this.healthy;
                result.message = this.message;
                result.version = this.version;
                result.serviceInfo = this.serviceInfo;
                return result;
            }
        }
        
        // Getters
        public boolean isHealthy() { return healthy; }
        public String getMessage() { return message; }
        public String getVersion() { return version; }
        public Map<String, Object> getServiceInfo() { return serviceInfo; }
    }
    
    /**
     * RAGAS评估请求
     */
    public static class RagasEvaluationRequest {
        private String taskName;
        private List<RagasEvaluationSample> samples;
        private List<String> metrics;
        private Map<String, Object> config;
        
        // Getters and Setters
        public String getTaskName() { return taskName; }
        public void setTaskName(String taskName) { this.taskName = taskName; }
        public List<RagasEvaluationSample> getSamples() { return samples; }
        public void setSamples(List<RagasEvaluationSample> samples) { this.samples = samples; }
        public List<String> getMetrics() { return metrics; }
        public void setMetrics(List<String> metrics) { this.metrics = metrics; }
        public Map<String, Object> getConfig() { return config; }
        public void setConfig(Map<String, Object> config) { this.config = config; }
    }
    
    /**
     * RAGAS评估样本
     */
    public static class RagasEvaluationSample {
        private String caseId;
        private String question;
        private String answer;
        private List<String> contexts;
        private List<String> groundTruthContexts;
        private String groundTruthAnswer;
        
        // Getters and Setters
        public String getCaseId() { return caseId; }
        public void setCaseId(String caseId) { this.caseId = caseId; }
        public String getQuestion() { return question; }
        public void setQuestion(String question) { this.question = question; }
        public String getAnswer() { return answer; }
        public void setAnswer(String answer) { this.answer = answer; }
        public List<String> getContexts() { return contexts; }
        public void setContexts(List<String> contexts) { this.contexts = contexts; }
        public List<String> getGroundTruthContexts() { return groundTruthContexts; }
        public void setGroundTruthContexts(List<String> groundTruthContexts) { this.groundTruthContexts = groundTruthContexts; }
        public String getGroundTruthAnswer() { return groundTruthAnswer; }
        public void setGroundTruthAnswer(String groundTruthAnswer) { this.groundTruthAnswer = groundTruthAnswer; }
    }
    
    /**
     * RAGAS任务结果
     */
    public static class RagasTaskResult {
        private String taskId;
        private String status;
        private String message;
        private Long createdAt;
        
        // Getters and Setters
        public String getTaskId() { return taskId; }
        public void setTaskId(String taskId) { this.taskId = taskId; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public Long getCreatedAt() { return createdAt; }
        public void setCreatedAt(Long createdAt) { this.createdAt = createdAt; }
    }
    
    /**
     * RAGAS任务状态
     */
    public static class RagasTaskStatus {
        private String taskId;
        private String status;
        private Double progress;
        private String currentStage;
        private String errorMessage;
        private Long startedAt;
        private Long completedAt;
        
        // Getters and Setters
        public String getTaskId() { return taskId; }
        public void setTaskId(String taskId) { this.taskId = taskId; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public Double getProgress() { return progress; }
        public void setProgress(Double progress) { this.progress = progress; }
        public String getCurrentStage() { return currentStage; }
        public void setCurrentStage(String currentStage) { this.currentStage = currentStage; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        public Long getStartedAt() { return startedAt; }
        public void setStartedAt(Long startedAt) { this.startedAt = startedAt; }
        public Long getCompletedAt() { return completedAt; }
        public void setCompletedAt(Long completedAt) { this.completedAt = completedAt; }
    }
    
    /**
     * RAGAS评估结果
     */
    public static class RagasEvaluationResult {
        private String taskId;
        private Map<String, Double> aggregatedMetrics;
        private List<Map<String, Object>> detailedResults;
        private Map<String, Object> statisticalAnalysis;
        private Long computedAt;
        
        // Getters and Setters
        public String getTaskId() { return taskId; }
        public void setTaskId(String taskId) { this.taskId = taskId; }
        public Map<String, Double> getAggregatedMetrics() { return aggregatedMetrics; }
        public void setAggregatedMetrics(Map<String, Double> aggregatedMetrics) { this.aggregatedMetrics = aggregatedMetrics; }
        public List<Map<String, Object>> getDetailedResults() { return detailedResults; }
        public void setDetailedResults(List<Map<String, Object>> detailedResults) { this.detailedResults = detailedResults; }
        public Map<String, Object> getStatisticalAnalysis() { return statisticalAnalysis; }
        public void setStatisticalAnalysis(Map<String, Object> statisticalAnalysis) { this.statisticalAnalysis = statisticalAnalysis; }
        public Long getComputedAt() { return computedAt; }
        public void setComputedAt(Long computedAt) { this.computedAt = computedAt; }
    }
}