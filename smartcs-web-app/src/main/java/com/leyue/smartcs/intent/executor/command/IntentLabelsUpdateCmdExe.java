package com.leyue.smartcs.intent.executor.command;

import com.alibaba.cola.dto.Response;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.domain.intent.entity.Intent;
import com.leyue.smartcs.domain.intent.gateway.IntentGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 意图标签更新命令执行器
 * 
 * @author Claude
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class IntentLabelsUpdateCmdExe {
    
    private final IntentGateway intentGateway;
    
    /**
     * 执行意图标签更新
     */
    public Response execute(Long intentId, List<String> labels) {
        if (intentId == null) {
            throw new BizException("意图ID不能为空");
        }
        
        Intent intent = intentGateway.findById(intentId);
        if (intent == null) {
            throw new BizException("意图不存在，ID: " + intentId);
        }
        
        intent.setLabels(labels);
        intent.setUpdatedAt(System.currentTimeMillis());
        intentGateway.update(intent);
        
        log.info("意图标签更新成功，ID: {}, 标签数量: {}", intent.getId(), 
                labels != null ? labels.size() : 0);
        
        return Response.buildSuccess();
    }
}