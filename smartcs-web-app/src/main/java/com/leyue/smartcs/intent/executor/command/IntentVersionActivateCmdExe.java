package com.leyue.smartcs.intent.executor.command;

import com.alibaba.cola.dto.Response;
import com.leyue.smartcs.config.context.UserContext;
import com.leyue.smartcs.domain.intent.domainservice.IntentDomainService;
import com.leyue.smartcs.domain.intent.domainservice.VersionDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 意图版本激活命令执行器
 * 
 * @author Claude
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class IntentVersionActivateCmdExe {
    
    private final VersionDomainService versionDomainService;
    private final IntentDomainService intentDomainService;
    
    /**
     * 执行意图版本激活
     */
    public Response execute(Long intentId, Long versionId) {
        
        Long approvedBy = UserContext.getCurrentUser() != null ? 
                UserContext.getCurrentUser().getId() : 1L;
        
        // 激活版本
        versionDomainService.activateVersion(versionId, approvedBy);
        
        // 激活意图
        intentDomainService.activateIntent(intentId, versionId);
        
        log.info("意图版本激活成功，意图ID: {}, 版本ID: {}", intentId, versionId);
        
        return Response.buildSuccess();
    }
}