package com.leyue.smartcs.intent.executor.command;

import com.alibaba.cola.dto.SingleResponse;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.domain.intent.entity.IntentSample;
import com.leyue.smartcs.domain.intent.enums.SampleType;
import com.leyue.smartcs.domain.intent.gateway.IntentSampleGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 意图样本创建命令执行器
 * 
 * @author Claude
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class IntentSampleCreateCmdExe {
    
    private final IntentSampleGateway sampleGateway;
    
    /**
     * 执行意图样本创建
     */
    public SingleResponse<Long> execute(Long intentId, String text, String type, String channel, String tenant) {
        if (intentId == null) {
            throw new BizException("意图ID不能为空");
        }
        if (!StringUtils.hasText(text)) {
            throw new BizException("样本文本不能为空");
        }
        
        SampleType sampleType = null;
        if (StringUtils.hasText(type)) {
            try {
                sampleType = SampleType.fromCode(type);
            } catch (IllegalArgumentException e) {
                throw new BizException("无效的样本类型: " + type);
            }
        }
        
        long currentTime = System.currentTimeMillis();
        IntentSample sample = IntentSample.builder()
                .versionId(intentId) // Note: Using versionId field as per domain entity
                .text(text)
                .type(sampleType != null ? sampleType : SampleType.TRAIN)
                .source(channel != null ? channel : tenant) // Note: Using source field as per domain entity
                .isDeleted(false)
                .createdAt(currentTime)
                .updatedAt(currentTime)
                .build();
        
        IntentSample savedSample = sampleGateway.save(sample);
        
        log.info("意图样本创建成功，ID: {}, 意图ID: {}", savedSample.getId(), intentId);
        
        return SingleResponse.of(savedSample.getId());
    }
}