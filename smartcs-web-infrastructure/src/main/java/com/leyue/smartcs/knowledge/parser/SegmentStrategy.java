package com.leyue.smartcs.knowledge.parser;

import java.util.List;

/**
 * 文档内容分段策略接口
 * 用于将长文本按照不同策略分割成段落
 */
public interface SegmentStrategy {
    
    /**
     * 将文本分割成多个段落
     * @param text 完整文本
     * @return 分段后的文本列表
     */
    List<String> segment(String text);
    
    /**
     * 获取策略名称
     * @return 策略名称
     */
    String getStrategyName();
} 