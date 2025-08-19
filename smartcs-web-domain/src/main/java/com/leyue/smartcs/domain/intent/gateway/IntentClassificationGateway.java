package com.leyue.smartcs.domain.intent.gateway;

import java.util.Map;

/**
 * 意图分类Gateway接口
 * 
 * @author Claude
 */
public interface IntentClassificationGateway {
    
    /**
     * 分类文本意图
     * @param text 输入文本
     * @param context 上下文信息
     * @return 分类结果
     */
    Map<String, Object> classify(String text, Map<String, Object> context);
    
    /**
     * 批量分类文本意图
     * @param texts 输入文本列表
     * @param context 上下文信息
     * @return 分类结果列表
     */
    Map<String, Map<String, Object>> batchClassify(String[] texts, Map<String, Object> context);
    
    /**
     * 获取分类置信度阈值建议
     * @param samples 样本数据
     * @return 阈值建议
     */
    Map<String, Double> getThresholdSuggestion(Map<String, Object> samples);
}