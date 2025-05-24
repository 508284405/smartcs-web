package com.leyue.smartcs.knowledge.parser.factory.impl;

import com.leyue.smartcs.knowledge.parser.factory.SegmentStrategyFactory;
import com.leyue.smartcs.knowledge.parser.SegmentStrategy;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import com.alibaba.cola.exception.BizException;

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
    public SegmentStrategy getStrategy(String strategyName) {
        SegmentStrategy strategy = strategyMap.get(strategyName);
        if (strategy == null) {
            // 可以根据实际情况抛出异常或返回默认策略
            throw new BizException("Unsupported segment strategy: " + strategyName);
        }
        return strategy;
    }
} 