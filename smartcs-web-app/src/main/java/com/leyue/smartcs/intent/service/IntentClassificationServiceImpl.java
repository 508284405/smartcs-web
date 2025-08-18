package com.leyue.smartcs.intent.service;

import com.alibaba.cola.dto.SingleResponse;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.api.IntentClassificationService;
import com.leyue.smartcs.domain.intent.gateway.IntentClassificationGateway;
import com.leyue.smartcs.domain.intent.gateway.IntentSnapshotGateway;
import com.leyue.smartcs.dto.intent.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 意图分类服务实现
 * 
 * @author Claude
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IntentClassificationServiceImpl implements IntentClassificationService {
    
    private final IntentClassificationGateway intentClassificationGateway;
    private final IntentSnapshotGateway intentSnapshotGateway;
    
    @Override
    public SingleResponse<IntentClassifyResponseDTO> classify(IntentClassifyCmd cmd) {
        try {
            log.info("开始意图分类: channel={}, tenant={}, textLength={}", 
                    cmd.getChannel(), cmd.getTenant(), cmd.getText() != null ? cmd.getText().length() : 0);
            
            // 参数验证
            if (cmd.getText() == null || cmd.getText().trim().isEmpty()) {
                throw new BizException("INVALID_INPUT", "输入文本不能为空");
            }
            
            // 构建分类上下文
            Map<String, Object> context = buildClassificationContext(cmd);
            
            // 调用分类网关
            Map<String, Object> classificationResult = intentClassificationGateway.classify(cmd.getText().trim(), context);
            
            // 构建响应
            IntentClassifyResponseDTO responseDTO = buildClassifyResponse(classificationResult, cmd);
            
            log.info("意图分类完成: intentCode={}, confidence={}, processingTime={}ms", 
                    responseDTO.getIntentCode(), 
                    responseDTO.getConfidenceScore(), 
                    responseDTO.getProcessingTimeMs());
            
            return SingleResponse.of(responseDTO);
            
        } catch (BizException e) {
            log.warn("意图分类业务异常: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("意图分类失败", e);
            throw new BizException("CLASSIFICATION_ERROR", "意图分类失败: " + e.getMessage());
        }
    }
    
    @Override
    public SingleResponse<IntentBatchClassifyResponseDTO> batchClassify(IntentBatchClassifyCmd cmd) {
        try {
            log.info("开始批量意图分类: channel={}, tenant={}, textCount={}", 
                    cmd.getChannel(), cmd.getTenant(), cmd.getTexts() != null ? cmd.getTexts().length : 0);
            
            // 参数验证
            if (cmd.getTexts() == null || cmd.getTexts().length == 0) {
                throw new BizException("INVALID_INPUT", "输入文本列表不能为空");
            }
            
            // 构建分类上下文
            Map<String, Object> context = buildBatchClassificationContext(cmd);
            
            // 调用批量分类网关
            Map<String, Map<String, Object>> classificationResults = 
                    intentClassificationGateway.batchClassify(cmd.getTexts(), context);
            
            // 构建响应
            IntentBatchClassifyResponseDTO responseDTO = buildBatchClassifyResponse(classificationResults, cmd);
            
            log.info("批量意图分类完成: processedCount={}, averageConfidence={}", 
                    responseDTO.getResults().size(), 
                    responseDTO.getAverageConfidence());
            
            return SingleResponse.of(responseDTO);
            
        } catch (BizException e) {
            log.warn("批量意图分类业务异常: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("批量意图分类失败", e);
            throw new BizException("BATCH_CLASSIFICATION_ERROR", "批量意图分类失败: " + e.getMessage());
        }
    }
    
    @Override
    public SingleResponse<IntentRuntimeConfigDTO> getRuntimeConfig(IntentRuntimeConfigQry qry) {
        try {
            log.info("获取意图运行时配置: channel={}, tenant={}", qry.getChannel(), qry.getTenant());
            
            // 构建运行时配置
            IntentRuntimeConfigDTO configDTO = new IntentRuntimeConfigDTO();
            configDTO.setChannel(qry.getChannel());
            configDTO.setTenant(qry.getTenant());
            configDTO.setRegion(qry.getRegion());
            configDTO.setEnv(qry.getEnv());
            configDTO.setConfigVersion("1.0.0");
            configDTO.setLastUpdateTime(System.currentTimeMillis());
            
            // TODO: 从快照中获取实际的运行时配置
            // 这里先返回默认配置
            configDTO.setDefaultThreshold(0.6);
            configDTO.setMaxRetries(3);
            configDTO.setTimeout(5000);
            
            Map<String, Object> thresholds = new HashMap<>();
            thresholds.put("greeting", 0.8);
            thresholds.put("goodbye", 0.7);
            thresholds.put("question", 0.6);
            configDTO.setIntentThresholds(thresholds);
            
            log.info("获取意图运行时配置完成: version={}", configDTO.getConfigVersion());
            
            return SingleResponse.of(configDTO);
            
        } catch (Exception e) {
            log.error("获取意图运行时配置失败", e);
            throw new BizException("CONFIG_ERROR", "获取运行时配置失败: " + e.getMessage());
        }
    }
    
    @Override
    public SingleResponse<Boolean> reportHardSample(IntentHardSampleReportCmd cmd) {
        try {
            log.info("上报困难样本: channel={}, tenant={}, textLength={}, expectedIntent={}", 
                    cmd.getChannel(), cmd.getTenant(), 
                    cmd.getText() != null ? cmd.getText().length() : 0,
                    cmd.getExpectedIntentCode());
            
            // 参数验证
            if (cmd.getText() == null || cmd.getText().trim().isEmpty()) {
                throw new BizException("INVALID_INPUT", "困难样本文本不能为空");
            }
            
            // TODO: 保存困难样本到数据库，用于后续模型训练和优化
            // 这里可以异步处理，避免影响实时分类性能
            
            log.info("困难样本上报成功: sessionId={}", cmd.getSessionId());
            
            return SingleResponse.of(Boolean.TRUE);
            
        } catch (BizException e) {
            log.warn("困难样本上报业务异常: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("困难样本上报失败", e);
            throw new BizException("REPORT_ERROR", "困难样本上报失败: " + e.getMessage());
        }
    }
    
    /**
     * 构建分类上下文
     */
    private Map<String, Object> buildClassificationContext(IntentClassifyCmd cmd) {
        Map<String, Object> context = new HashMap<>();
        context.put("channel", cmd.getChannel());
        context.put("tenant", cmd.getTenant());
        context.put("region", cmd.getRegion());
        context.put("env", cmd.getEnv());
        context.put("session_id", cmd.getSessionId());
        context.put("user_id", cmd.getUserId());
        context.put("timestamp", System.currentTimeMillis());
        return context;
    }
    
    /**
     * 构建批量分类上下文
     */
    private Map<String, Object> buildBatchClassificationContext(IntentBatchClassifyCmd cmd) {
        Map<String, Object> context = new HashMap<>();
        context.put("channel", cmd.getChannel());
        context.put("tenant", cmd.getTenant());
        context.put("timestamp", System.currentTimeMillis());
        return context;
    }
    
    /**
     * 构建分类响应
     */
    private IntentClassifyResponseDTO buildClassifyResponse(Map<String, Object> result, IntentClassifyCmd cmd) {
        IntentClassifyResponseDTO response = new IntentClassifyResponseDTO();
        
        response.setIntentCode((String) result.get("intent_code"));
        response.setIntentName((String) result.get("intent_name"));
        response.setConfidenceScore(getDoubleValue(result, "confidence_score"));
        response.setAboveThreshold(response.getConfidenceScore() >= 0.6); // TODO: 使用动态阈值
        response.setChannel(cmd.getChannel());
        response.setTenant(cmd.getTenant());
        response.setSnapshotId(getStringValue(result, "snapshot_id"));
        response.setClassificationTime(System.currentTimeMillis());
        response.setProcessingTimeMs(getIntegerValue(result, "processing_time_ms"));
        response.setResultData(new HashMap<>(result));
        
        return response;
    }
    
    /**
     * 构建批量分类响应
     */
    private IntentBatchClassifyResponseDTO buildBatchClassifyResponse(
            Map<String, Map<String, Object>> results, IntentBatchClassifyCmd cmd) {
        
        IntentBatchClassifyResponseDTO response = new IntentBatchClassifyResponseDTO();
        
        List<IntentClassifyResponseDTO> resultList = new ArrayList<>();
        double totalConfidence = 0.0;
        int successCount = 0;
        
        for (int i = 0; i < cmd.getTexts().length; i++) {
            String key = "text_" + i;
            Map<String, Object> result = results.get(key);
            
            if (result != null) {
                IntentClassifyCmd singleCmd = new IntentClassifyCmd();
                singleCmd.setText(cmd.getTexts()[i]);
                singleCmd.setChannel(cmd.getChannel());
                singleCmd.setTenant(cmd.getTenant());
                
                IntentClassifyResponseDTO singleResponse = buildClassifyResponse(result, singleCmd);
                resultList.add(singleResponse);
                
                totalConfidence += singleResponse.getConfidenceScore();
                successCount++;
            }
        }
        
        response.setResults(resultList);
        response.setTotalCount(cmd.getTexts().length);
        response.setSuccessCount(successCount);
        response.setFailureCount(cmd.getTexts().length - successCount);
        response.setAverageConfidence(successCount > 0 ? totalConfidence / successCount : 0.0);
        response.setProcessingTime(System.currentTimeMillis());
        
        return response;
    }
    
    /**
     * 安全获取Double值
     */
    private Double getDoubleValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return 0.0;
    }
    
    /**
     * 安全获取Integer值
     */
    private Integer getIntegerValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return 0;
    }
    
    /**
     * 安全获取String值
     */
    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }
}