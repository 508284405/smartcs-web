package com.leyue.smartcs.intent.executor.command;

import com.alibaba.cola.dto.SingleResponse;
import com.leyue.smartcs.config.context.UserContext;
import com.leyue.smartcs.domain.intent.domainservice.VersionDomainService;
import com.leyue.smartcs.domain.intent.entity.IntentVersion;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 意图版本创建命令执行器
 * 
 * @author Claude
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class IntentVersionCreateCmdExe {
    
    private final VersionDomainService versionDomainService;
    
    /**
     * 执行意图版本创建
     */
    public SingleResponse<Long> execute(Long intentId, String versionName, String changeNote) {
        
        Long createdBy = UserContext.getCurrentUser() != null ? 
                UserContext.getCurrentUser().getId() : 1L;
        
        IntentVersion version = versionDomainService.createVersion(intentId, versionName, changeNote, createdBy);
        
        log.info("意图版本创建成功，ID: {}, 版本号: {}", version.getId(), version.getVersionNumber());
        
        return SingleResponse.of(version.getId());
    }
}