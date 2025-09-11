package com.leyue.smartcs.intent.executor.command;

import com.alibaba.cola.dto.SingleResponse;
import com.leyue.smartcs.config.context.UserContext;
import com.leyue.smartcs.domain.intent.domainservice.IntentDomainService;
import com.leyue.smartcs.domain.intent.entity.Intent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 意图创建命令执行器
 * 
 * @author Claude
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class IntentCreateCmdExe {
    
    private final IntentDomainService intentDomainService;
    
    /**
     * 执行意图创建
     */
    public SingleResponse<Long> execute(Long catalogId, String name, String code, String description, 
                                        List<String> labels, Map<String, Object> boundaries) {
        
        Long creatorId = UserContext.getCurrentUser() != null ? 
                UserContext.getCurrentUser().getId() : 1L;
        
        Intent intent = intentDomainService.createIntent(catalogId, name, code, description, creatorId);
        
        // 设置其他属性
        if (labels != null && !labels.isEmpty()) {
            intent.setLabels(labels);
        }
        if (boundaries != null && !boundaries.isEmpty()) {
            intent.setBoundaries(boundaries);
        }
        
        log.info("意图创建成功，ID: {}, 名称: {}", intent.getId(), intent.getName());
        
        return SingleResponse.of(intent.getId());
    }
}