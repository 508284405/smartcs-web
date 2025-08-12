package com.leyue.smartcs.domain.eval.gateway;

import com.leyue.smartcs.dto.eval.SimpleEvalRequest;
import com.leyue.smartcs.dto.eval.SimpleEvalResponse;

/**
 * 简化评估网关接口
 * 负责与Python评估服务通信
 */
public interface SimpleEvalGateway {
    
    /**
     * 执行评估
     * 
     * @param request 评估请求
     * @return 评估响应
     */
    SimpleEvalResponse evaluate(SimpleEvalRequest request);
    
    /**
     * 检查评估服务健康状态
     * 
     * @return 健康状态信息
     */
    HealthStatus getHealthStatus();
    
    /**
     * 健康状态响应
     */
    record HealthStatus(
            String status,
            String version,
            String uptime,
            Object config
    ) {}
}