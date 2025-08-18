package com.leyue.smartcs.intent.executor.query;

import com.alibaba.cola.dto.MultiResponse;
import com.leyue.smartcs.domain.intent.entity.IntentVersion;
import com.leyue.smartcs.domain.intent.gateway.IntentVersionGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 意图版本列表查询执行器
 * 
 * @author Claude
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class IntentVersionListQryExe {
    
    private final IntentVersionGateway versionGateway;
    
    /**
     * 根据意图ID查询版本列表
     */
    public MultiResponse<IntentVersion> execute(Long intentId) {
        List<IntentVersion> versions = versionGateway.findByIntentId(intentId);
        
        log.debug("查询到版本数量: {}, 意图ID: {}", versions.size(), intentId);
        
        return MultiResponse.of(versions);
    }
}