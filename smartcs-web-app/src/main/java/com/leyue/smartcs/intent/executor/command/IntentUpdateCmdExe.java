package com.leyue.smartcs.intent.executor.command;

import com.alibaba.cola.dto.Response;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.domain.intent.entity.Intent;
import com.leyue.smartcs.domain.intent.gateway.IntentGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 意图更新命令执行器
 * 
 * @author Claude
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class IntentUpdateCmdExe {
    
    private final IntentGateway intentGateway;
    
    /**
     * 执行意图更新
     */
    public Response execute(Long intentId, String name, String description) {
        if (intentId == null) {
            throw new BizException("意图ID不能为空");
        }
        
        Intent intent = intentGateway.findById(intentId);
        if (intent == null) {
            throw new BizException("意图不存在，ID: " + intentId);
        }
        
        if (StringUtils.hasText(name)) {
            intent.setName(name);
        }
        if (StringUtils.hasText(description)) {
            intent.setDescription(description);
        }
        
        intent.setUpdatedAt(System.currentTimeMillis());
        intentGateway.update(intent);
        
        log.info("意图更新成功，ID: {}, 名称: {}", intent.getId(), intent.getName());
        
        return Response.buildSuccess();
    }
}