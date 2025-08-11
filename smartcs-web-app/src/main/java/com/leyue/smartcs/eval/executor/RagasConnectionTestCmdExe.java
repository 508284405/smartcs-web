package com.leyue.smartcs.eval.executor;

import com.alibaba.cola.dto.SingleResponse;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.dto.eval.RagasConnectionTestCmd;
import com.leyue.smartcs.dto.eval.RagasConnectionTestResultDTO;
import com.leyue.smartcs.domain.eval.gateway.RagasServiceGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * RAGAS连接测试命令执行器
 * 
 * @author Claude
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RagasConnectionTestCmdExe {
    
    private final RagasServiceGateway ragasServiceGateway;
    
    /**
     * 执行RAGAS连接测试命令
     * 
     * @param cmd 连接测试命令
     * @return 测试结果
     */
    public SingleResponse<RagasConnectionTestResultDTO> execute(RagasConnectionTestCmd cmd) {
        log.info("执行RAGAS服务连接测试");
        
        try {
            // 通过Gateway接口调用Infrastructure层能力
            RagasConnectionTestResultDTO result = ragasServiceGateway.testConnection(cmd);
            
            if (result == null) {
                throw new BizException("CONNECTION_TEST_RESULT_NOT_FOUND", "连接测试结果不存在");
            }
            
            log.info("RAGAS服务连接测试成功，状态: {}, 响应时间: {}ms", 
                    result.getConnectionStatus(), result.getResponseTime());
            return SingleResponse.of(result);
            
        } catch (BizException e) {
            log.warn("RAGAS服务连接测试业务异常: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("RAGAS服务连接测试失败", e);
            throw new BizException("TEST_RAGAS_CONNECTION_FAILED", "测试RAGAS服务连接失败: " + e.getMessage());
        }
    }
}
