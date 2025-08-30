package com.leyue.smartcs.domain.eval.gateway;

// TODO: 重构评估网关，移除对client层DTO的依赖
// import com.leyue.smartcs.dto.eval.SimpleEvalRequest;
// import com.leyue.smartcs.dto.eval.SimpleEvalResponse;

/**
 * 简化评估网关接口
 * 负责与Python评估服务通信
 * TODO: 重构此接口以符合COLA架构规范
 */
public interface SimpleEvalGateway {
    
    /**
     * 执行评估
     * TODO: 参数和返回值需要改为域模型类型
     * 
     * @param request 评估请求
     * @return 评估响应
     */
    // SimpleEvalResponse evaluate(SimpleEvalRequest request);
    
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