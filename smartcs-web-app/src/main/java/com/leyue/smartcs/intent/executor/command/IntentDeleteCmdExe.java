package com.leyue.smartcs.intent.executor.command;

import com.alibaba.cola.dto.Response;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.domain.intent.entity.Intent;
import com.leyue.smartcs.domain.intent.gateway.IntentGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 意图删除命令执行器
 * 
 * @author Claude
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class IntentDeleteCmdExe {
    
    private final IntentGateway intentGateway;
    
    /**
     * 执行意图删除（逻辑删除）
     */
    public Response execute(Long intentId) {
        if (intentId == null) {
            throw new BizException("意图ID不能为空");
        }
        
        Intent intent = intentGateway.findById(intentId);
        if (intent == null) {
            throw new BizException("意图不存在，ID: " + intentId);
        }
        
        intent.setIsDeleted(true);
        intent.setUpdatedAt(System.currentTimeMillis());
        intentGateway.update(intent);
        
        log.info("意图删除成功，ID: {}, 名称: {}", intent.getId(), intent.getName());
        
        return Response.buildSuccess();
    }
}