package com.leyue.smartcs.knowledge.parser.factory.impl;

import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.domain.knowledge.enums.StrategyNameEnum;
import com.leyue.smartcs.knowledge.parser.SegmentStrategy;
import com.leyue.smartcs.knowledge.parser.factory.SegmentStrategyFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 文档分段策略工厂实现类
 */
@Component
public class SegmentStrategyFactoryImpl implements SegmentStrategyFactory {

    private final Map<String, SegmentStrategy> strategyMap;

    public SegmentStrategyFactoryImpl(List<SegmentStrategy> segmentStrategies) {
        this.strategyMap = segmentStrategies.stream()
                .collect(Collectors.toMap(SegmentStrategy::getStrategyName, Function.identity()));
    }

    @Override
    public SegmentStrategy getStrategy(StrategyNameEnum strategyEnum) {
        SegmentStrategy strategy = strategyMap.get(strategyEnum.getCode());
        if (strategy == null) {
            // 可以根据实际情况抛出异常或返回默认策略
            throw new BizException("Unsupported segment strategy: " + strategyEnum.name());
        }
        return strategy;
    }
} 