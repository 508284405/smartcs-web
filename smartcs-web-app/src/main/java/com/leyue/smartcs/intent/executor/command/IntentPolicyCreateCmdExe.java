package com.leyue.smartcs.intent.executor.command;

import com.alibaba.cola.dto.SingleResponse;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.domain.intent.entity.IntentPolicy;
import com.leyue.smartcs.domain.intent.gateway.IntentPolicyGateway;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 意图策略创建命令执行器
 * 
 * @author Claude
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class IntentPolicyCreateCmdExe {
    
    private final IntentPolicyGateway policyGateway;
    
    /**
     * 执行意图策略创建
     */
    public SingleResponse<Long> execute(Long intentId, String channel, String tenant, Double threshold, String description) {
        if (intentId == null) {
            throw new BizException("意图ID不能为空");
        }
        if (threshold == null) {
            throw new BizException("阈值不能为空");
        }
        
        long currentTime = System.currentTimeMillis();
        
        // 构建渠道覆盖配置
        Map<String, Object> channelOverrides = new HashMap<>();
        if (channel != null) {
            channelOverrides.put("channel", channel);
        }
        if (tenant != null) {
            channelOverrides.put("tenant", tenant);
        }
        
        IntentPolicy policy = IntentPolicy.builder()
                .versionId(intentId)
                .thresholdTau(threshold != null ? BigDecimal.valueOf(threshold) : null)
                .unknownLabel(description)
                .channelOverrides(channelOverrides)
                .createdAt(currentTime)
                .updatedAt(currentTime)
                .build();
        
        IntentPolicy savedPolicy = policyGateway.save(policy);
        
        log.info("意图策略创建成功，ID: {}, 意图ID: {}", savedPolicy.getId(), intentId);
        
        return SingleResponse.of(savedPolicy.getId());
    }
}