package com.leyue.smartcs.eval.gatewayimpl;

import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.domain.eval.gateway.RagasServiceGateway;
import com.leyue.smartcs.dto.eval.RagasServiceStatusDTO;
import com.leyue.smartcs.dto.eval.RagasConnectionTestCmd;
import com.leyue.smartcs.dto.eval.RagasConnectionTestResultDTO;
import com.leyue.smartcs.eval.client.RagasClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * RAGAS服务Gateway实现
 * 通过OpenFeign客户端调用RAGAS评估服务
 * 
 * @author Claude
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RagasServiceGatewayImpl implements RagasServiceGateway {

    private final RagasClient ragasClient;

    @Override
    public RagasServiceStatusDTO getServiceStatus() {
        try {
            log.debug("获取RAGAS服务状态");

            // 调用RAGAS服务获取状态
            RagasClient.RagasHealthCheckResult healthResult = ragasClient.testConnection();
            
            RagasServiceStatusDTO status = RagasServiceStatusDTO.builder()
                    .serviceName("RAGAS Service")
                    .status(healthResult.isHealthy() ? "RUNNING" : "ERROR")
                    .version(healthResult.getVersion())
                    .serviceUrl("unknown")
                    .healthStatus(healthResult.isHealthy() ? "healthy" : "unhealthy")
                    .lastHealthCheck(LocalDateTime.now())
                    .responseTime(0L)
                    .availabilityPercentage(healthResult.isHealthy() ? 100.0 : 0.0)
                    .build();
            
            if (!healthResult.isHealthy()) {
                status.setErrorMessage(healthResult.getMessage());
            }
            
            log.debug("RAGAS服务状态: status={}, version={}", status.getStatus(), status.getVersion());
            return status;

        } catch (Exception e) {
            log.warn("获取RAGAS服务状态失败: {}", e.getMessage());
            
            // 返回不可用状态
            RagasServiceStatusDTO status = RagasServiceStatusDTO.builder()
                    .serviceName("RAGAS Service")
                    .status("UNKNOWN")
                    .version("unknown")
                    .serviceUrl("unknown")
                    .healthStatus("unhealthy")
                    .lastHealthCheck(LocalDateTime.now())
                    .errorMessage("服务连接失败: " + e.getMessage())
                    .responseTime(0L)
                    .availabilityPercentage(0.0)
                    .build();
            
            return status;
        }
    }

    @Override
    public RagasConnectionTestResultDTO testConnection(RagasConnectionTestCmd cmd) {
        try {
            log.info("测试RAGAS服务连接: endpoint={}", cmd.getServiceEndpoint());

            long startTime = System.currentTimeMillis();
            
            // 执行连接测试
            RagasClient.RagasHealthCheckResult healthResult = ragasClient.testConnection();
            
            long duration = System.currentTimeMillis() - startTime;
            
            RagasConnectionTestResultDTO result = RagasConnectionTestResultDTO.builder()
                    .connectionStatus(healthResult.isHealthy() ? "SUCCESS" : "FAILED")
                    .responseTime(duration)
                    .errorMessage(healthResult.isHealthy() ? null : healthResult.getMessage())
                    .testTime(LocalDateTime.now())
                    .serviceEndpoint(cmd.getServiceEndpoint())
                    .build();
            
            log.info("RAGAS连接测试完成: success={}, duration={}ms", healthResult.isHealthy(), duration);
            
            return result;

        } catch (Exception e) {
            log.error("RAGAS连接测试失败: endpoint={}, error={}", cmd.getServiceEndpoint(), e.getMessage(), e);
            
            // 返回失败结果
            RagasConnectionTestResultDTO result = RagasConnectionTestResultDTO.builder()
                    .connectionStatus("FAILED")
                    .errorMessage("连接测试失败: " + e.getMessage())
                    .testTime(LocalDateTime.now())
                    .responseTime(System.currentTimeMillis())
                    .serviceEndpoint(cmd.getServiceEndpoint())
                    .build();
            
            return result;
        }
    }
}