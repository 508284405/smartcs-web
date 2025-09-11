package com.leyue.smartcs.eval.serviceimpl;

import org.springframework.stereotype.Service;

import com.alibaba.cola.dto.SingleResponse;
import com.leyue.smartcs.api.eval.SimpleEvalService;
import com.leyue.smartcs.domain.eval.gateway.SimpleEvalGateway;
import com.leyue.smartcs.dto.eval.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 简化评估服务实现类
 * 基于事件驱动架构的RAG评估服务
 * 
 * @author Claude
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SimpleEvalServiceImpl implements SimpleEvalService {
    
    private final SimpleEvalGateway simpleEvalGateway;
    
    @Override
    public SingleResponse<SimpleEvalResponse> runBaselineEvaluation(SimpleEvalRequest request) {
        log.info("执行基准集评估: itemCount={}", request.getItems() != null ? request.getItems().size() : 0);
        
        try {
            SimpleEvalResponse response = simpleEvalGateway.evaluate(request);
            
            log.info("基准集评估完成: itemCount={}, passThreshold={}, avgFaithfulness={}", 
                    response.getResults() != null ? response.getResults().size() : 0,
                    response.getAggregate() != null ? response.getAggregate().getPassThreshold() : null,
                    response.getAggregate() != null ? response.getAggregate().getAvgFaithfulness() : null);
            
            return SingleResponse.of(response);
            
        } catch (Exception e) {
            log.error("基准集评估失败: {}", e.getMessage(), e);
            return SingleResponse.buildFailure("EVAL_ERROR", "基准集评估失败: " + e.getMessage());
        }
    }
    
    @Override
    public SingleResponse<RagasServiceStatusDTO> getServiceStatus() {
        log.info("获取评估服务状态");
        
        try {
            SimpleEvalGateway.HealthStatus healthStatus = simpleEvalGateway.getHealthStatus();
            
            // 转换为DTO格式
            RagasServiceStatusDTO statusDTO = new RagasServiceStatusDTO();
            statusDTO.setStatus("healthy".equals(healthStatus.status()) ? "运行中" : "异常");
            statusDTO.setVersion(healthStatus.version());
            statusDTO.setHealthStatus(healthStatus.status());
            statusDTO.setLastHealthCheck(java.time.LocalDateTime.now());
            
            return SingleResponse.of(statusDTO);
            
        } catch (Exception e) {
            log.error("获取服务状态失败: {}", e.getMessage(), e);
            return SingleResponse.buildFailure("SERVICE_ERROR", "获取服务状态失败: " + e.getMessage());
        }
    }
    
    @Override
    public SingleResponse<RagasConnectionTestResultDTO> testConnection(RagasConnectionTestCmd cmd) {
        log.info("测试评估服务连接");
        
        try {
            long startTime = System.currentTimeMillis();
            SimpleEvalGateway.HealthStatus healthStatus = simpleEvalGateway.getHealthStatus();
            long responseTime = System.currentTimeMillis() - startTime;
            
            // 转换为DTO格式
            RagasConnectionTestResultDTO resultDTO = new RagasConnectionTestResultDTO();
            resultDTO.setConnectionStatus("healthy".equals(healthStatus.status()) ? "SUCCESS" : "FAILED");
            resultDTO.setErrorMessage("healthy".equals(healthStatus.status()) ? null : "服务不健康");
            resultDTO.setResponseTime(responseTime);
            resultDTO.setTestTime(java.time.LocalDateTime.now());
            
            return SingleResponse.of(resultDTO);
            
        } catch (Exception e) {
            log.error("测试连接失败: {}", e.getMessage(), e);
            
            RagasConnectionTestResultDTO resultDTO = new RagasConnectionTestResultDTO();
            resultDTO.setConnectionStatus("FAILED");
            resultDTO.setErrorMessage("连接测试失败: " + e.getMessage());
            resultDTO.setResponseTime(0L);
            resultDTO.setTestTime(java.time.LocalDateTime.now());
            
            return SingleResponse.of(resultDTO);
        }
    }
}