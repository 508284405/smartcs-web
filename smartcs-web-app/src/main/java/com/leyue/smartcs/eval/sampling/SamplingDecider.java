package com.leyue.smartcs.eval.sampling;

import com.leyue.smartcs.dto.eval.event.EvalConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Objects;

/**
 * RAG评估采样决策器
 * 支持请求头强制采样和哈希采样两种策略
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SamplingDecider {
    
    @Value("${eval.sampling.rate:0.05}")
    private double samplingRate;
    
    @Value("${eval.sampling.header:X-RAG-EVAL}")
    private String forceHeader;
    
    /**
     * 判断是否应该进行评估采样
     * 
     * @param traceId 链路追踪ID
     * @param userId 用户ID
     * @return true表示需要采样
     */
    public boolean shouldSample(String traceId, String userId) {
        // 1. 检查请求头强制采样
        if (isForceEnabled()) {
            log.debug("Force sampling enabled via header: {}", forceHeader);
            return true;
        }
        
        // 2. 哈希采样策略
        return isHashSampled(traceId, userId);
    }
    
    /**
     * 检查是否通过请求头强制开启采样
     */
    private boolean isForceEnabled() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return false;
            }
            
            HttpServletRequest request = attributes.getRequest();
            String headerValue = request.getHeader(forceHeader);
            
            return EvalConstants.Headers.RAG_EVAL_FORCE_VALUE.equals(headerValue);
        } catch (Exception e) {
            log.debug("Failed to check force sampling header", e);
            return false;
        }
    }
    
    /**
     * 基于哈希的采样决策
     * 使用traceId或userId的哈希值进行一致性采样
     */
    private boolean isHashSampled(String traceId, String userId) {
        // 优先使用traceId，如果不存在则使用userId
        String samplingKey = traceId != null ? traceId : userId;
        
        if (samplingKey == null) {
            log.debug("No sampling key available (traceId and userId are null)");
            return false;
        }
        
        // 计算哈希值并转换为[0,1)区间的double值
        int hash = Objects.hash(samplingKey);
        double normalizedHash = Math.abs(hash % 10000) / 10000.0;
        
        boolean shouldSample = normalizedHash < samplingRate;
        
        if (log.isDebugEnabled()) {
            log.debug("Hash sampling decision: key={}, hash={}, normalized={}, rate={}, sample={}", 
                    samplingKey, hash, normalizedHash, samplingRate, shouldSample);
        }
        
        return shouldSample;
    }
    
    /**
     * 获取当前采样率
     */
    public double getSamplingRate() {
        return samplingRate;
    }
    
    /**
     * 获取强制采样请求头名称
     */
    public String getForceHeader() {
        return forceHeader;
    }
}