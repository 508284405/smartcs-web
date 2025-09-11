package com.leyue.smartcs.config.feign;

import com.leyue.smartcs.domain.eval.gateway.SimpleEvalGateway;
import com.leyue.smartcs.dto.eval.SimpleEvalRequest;
import com.leyue.smartcs.dto.eval.SimpleEvalResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 简化评估服务OpenFeign客户端
 * 连接到Python FastAPI评估服务
 */
@FeignClient(
    name = "simple-eval-service",
    url = "${eval.simple-eval.base-url:http://localhost:8088}",
    configuration = SimpleEvalClientConfig.class
)
public interface SimpleEvalClient {
    
    /**
     * 执行评估
     */
    @PostMapping("/eval")
    SimpleEvalResponse evaluate(@RequestBody SimpleEvalRequest request);
    
    /**
     * 获取服务健康状态
     */
    @GetMapping("/health")
    SimpleEvalGateway.HealthStatus getHealth();
}