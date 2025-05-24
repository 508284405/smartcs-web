package com.leyue.smartcs.knowledge.parser.factory;

import com.leyue.smartcs.dto.knowledge.enums.StrategyNameEnum;
import com.leyue.smartcs.knowledge.parser.SegmentStrategy;

/**
 * 文档分段策略工厂接口
 */
public interface SegmentStrategyFactory {
    
    /**
     * 根据策略枚举获取分段策略实例
     * @param strategyEnum 策略枚举
     * @return SegmentStrategy 实例
     */
    SegmentStrategy getStrategy(StrategyNameEnum strategyEnum);
} 