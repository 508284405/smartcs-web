package com.leyue.smartcs.domain.intent.domainservice;

import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.domain.intent.entity.IntentSnapshot;
import com.leyue.smartcs.domain.intent.gateway.IntentClassificationGateway;
import com.leyue.smartcs.domain.intent.gateway.IntentSnapshotGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 分类领域服务
 * 
 * @author Claude
 */
@Service
@RequiredArgsConstructor
public class ClassificationDomainService {
    
    private final IntentClassificationGateway classificationGateway;
    private final IntentSnapshotGateway snapshotGateway;
    
    /**
     * 分类用户输入
     * @param text 输入文本
     * @param channel 渠道
     * @param tenant 租户
     * @return 分类结果
     */
    public Map<String, Object> classifyUserInput(String text, String channel, String tenant) {
        if (!StringUtils.hasText(text)) {
            throw new BizException("输入文本不能为空");
        }
        
        // 获取当前激活的快照配置
        IntentSnapshot currentSnapshot = snapshotGateway.getCurrentActiveSnapshot();
        if (currentSnapshot == null) {
            throw new BizException("没有激活的意图配置快照");
        }
        
        // 构建上下文信息
        Map<String, Object> context = new HashMap<>();
        context.put("channel", channel);
        context.put("tenant", tenant);
        context.put("snapshot_id", currentSnapshot.getCode());
        
        // 调用分类服务
        Map<String, Object> result = classificationGateway.classify(text, context);
        
        // 增强结果信息
        result.put("snapshot_id", currentSnapshot.getCode());
        result.put("classification_time", System.currentTimeMillis());
        
        return result;
    }
    
    /**
     * 批量分类
     * @param texts 输入文本数组
     * @param channel 渠道
     * @param tenant 租户
     * @return 分类结果
     */
    public Map<String, Map<String, Object>> batchClassify(String[] texts, String channel, String tenant) {
        if (texts == null || texts.length == 0) {
            throw new BizException("输入文本数组不能为空");
        }
        
        // 获取当前激活的快照配置
        IntentSnapshot currentSnapshot = snapshotGateway.getCurrentActiveSnapshot();
        if (currentSnapshot == null) {
            throw new BizException("没有激活的意图配置快照");
        }
        
        // 构建上下文信息
        Map<String, Object> context = new HashMap<>();
        context.put("channel", channel);
        context.put("tenant", tenant);
        context.put("snapshot_id", currentSnapshot.getCode());
        
        // 调用批量分类服务
        return classificationGateway.batchClassify(texts, context);
    }
    
    /**
     * 获取阈值建议
     * @param samples 样本数据
     * @return 阈值建议
     */
    public Map<String, Double> getThresholdSuggestion(Map<String, Object> samples) {
        if (samples == null || samples.isEmpty()) {
            throw new BizException("样本数据不能为空");
        }
        
        return classificationGateway.getThresholdSuggestion(samples);
    }
    
    /**
     * 验证分类结果格式
     * @param result 分类结果
     * @return 是否有效
     */
    public boolean isValidClassificationResult(Map<String, Object> result) {
        if (result == null) {
            return false;
        }
        
        // 必须包含意图代码和置信度
        return result.containsKey("intent_code") && result.containsKey("confidence_score");
    }
}