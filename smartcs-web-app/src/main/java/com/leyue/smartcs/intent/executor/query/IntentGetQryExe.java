package com.leyue.smartcs.intent.executor.query;

import com.alibaba.cola.dto.SingleResponse;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.domain.intent.entity.Intent;
import com.leyue.smartcs.domain.intent.gateway.IntentGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 意图获取查询执行器
 * 
 * @author Claude
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class IntentGetQryExe {
    
    private final IntentGateway intentGateway;
    
    /**
     * 执行意图获取查询
     */
    public SingleResponse<Intent> execute(Long intentId) {
        if (intentId == null) {
            throw new BizException("意图ID不能为空");
        }
        
        Intent intent = intentGateway.findById(intentId);
        if (intent == null) {
            throw new BizException("意图不存在，ID: " + intentId);
        }
        
        log.debug("查询到意图: {}", intent.getName());
        
        return SingleResponse.of(intent);
    }
}