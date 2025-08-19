package com.leyue.smartcs.eval.gatewayimpl;

import com.leyue.smartcs.domain.eval.gateway.SimpleEvalGateway;
import com.leyue.smartcs.dto.eval.SimpleEvalRequest;
import com.leyue.smartcs.dto.eval.SimpleEvalResponse;
import com.leyue.smartcs.config.feign.SimpleEvalClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 简化评估网关实现
 * 通过OpenFeign客户端调用Python评估服务
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SimpleEvalGatewayImpl implements SimpleEvalGateway {
    
    private final SimpleEvalClient evalClient;
    
    @Override
    public SimpleEvalResponse evaluate(SimpleEvalRequest request) {
        log.info("执行简化评估: itemCount={}", request.getItems() != null ? request.getItems().size() : 0);
        
        try {
            long startTime = System.currentTimeMillis();
            
            SimpleEvalResponse response = evalClient.evaluate(request);
            
            long duration = System.currentTimeMillis() - startTime;
            log.info("简化评估完成: itemCount={}, duration={}ms, passThreshold={}", 
                    request.getItems() != null ? request.getItems().size() : 0,
                    duration,
                    response.getAggregate() != null ? response.getAggregate().getPassThreshold() : null);
            
            return response;
            
        } catch (Exception e) {
            log.error("简化评估失败: itemCount={}, error={}", 
                    request.getItems() != null ? request.getItems().size() : 0, 
                    e.getMessage(), e);
            throw new RuntimeException("评估服务调用失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public HealthStatus getHealthStatus() {
        log.debug("检查评估服务健康状态");
        
        try {
            HealthStatus status = evalClient.getHealth();
            log.debug("评估服务健康状态: status={}, version={}", status.status(), status.version());
            return status;
            
        } catch (Exception e) {
            log.warn("获取评估服务健康状态失败: {}", e.getMessage());
            
            // 返回不健康状态
            return new HealthStatus(
                    "unhealthy",
                    "unknown",
                    "0s",
                    "连接失败: " + e.getMessage()
            );
        }
    }
}