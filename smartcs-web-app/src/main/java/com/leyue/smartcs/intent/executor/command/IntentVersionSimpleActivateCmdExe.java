package com.leyue.smartcs.intent.executor.command;

import com.alibaba.cola.dto.Response;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.domain.intent.entity.IntentVersion;
import com.leyue.smartcs.domain.intent.gateway.IntentVersionGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 意图版本简单激活命令执行器（仅需版本ID）
 * 
 * @author Claude
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class IntentVersionSimpleActivateCmdExe {
    
    private final IntentVersionGateway versionGateway;
    private final IntentVersionActivateCmdExe versionActivateCmdExe;
    
    /**
     * 执行意图版本激活（通过版本ID自动获取意图ID）
     */
    public Response execute(Long versionId) {
        if (versionId == null) {
            throw new BizException("版本ID不能为空");
        }
        
        IntentVersion version = versionGateway.findById(versionId);
        if (version == null) {
            throw new BizException("版本不存在，ID: " + versionId);
        }
        
        // 调用原始的激活执行器，传入意图ID和版本ID
        return versionActivateCmdExe.execute(version.getIntentId(), versionId);
    }
}