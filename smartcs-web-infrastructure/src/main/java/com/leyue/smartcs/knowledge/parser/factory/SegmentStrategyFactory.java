package com.leyue.smartcs.knowledge.factory;

import com.leyue.smartcs.knowledge.parser.SegmentStrategy;

/**
 * 文档分段策略工厂接口
 */
public interface SegmentStrategyFactory {

    /**
     * 根据策略名称获取分段策略实例
     * @param strategyName 策略名称
     * @return SegmentStrategy 实例
     */
    SegmentStrategy getStrategy(String strategyName);
} 