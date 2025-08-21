package com.leyue.smartcs.domain.rag.transformer.valueobject;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 转换策略值对象
 * 描述查询转换的策略类型和参数
 * 
 * @author Claude
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TransformationStrategy {
    
    private final StrategyType type;
    private final int maxQueries;
    private final String description;
    private final String promptGuidance;
    
    public enum StrategyType {
        NO_EXPANSION,
        STANDARD_EXPANSION,
        DETAILED_EXPANSION,
        PROBLEM_FOCUSED_EXPANSION,
        TECHNICAL_EXPANSION
    }
    
    /**
     * 无扩展策略
     */
    public static TransformationStrategy noExpansion() {
        return new TransformationStrategy(
            StrategyType.NO_EXPANSION,
            1,
            "无扩展策略",
            "直接使用原始查询，无需扩展"
        );
    }
    
    /**
     * 标准扩展策略
     */
    public static TransformationStrategy standardExpansion() {
        return new TransformationStrategy(
            StrategyType.STANDARD_EXPANSION,
            3,
            "标准扩展策略",
            "生成标准的查询变体，保持查询的核心含义"
        );
    }
    
    /**
     * 详细扩展策略
     */
    public static TransformationStrategy detailedExpansion() {
        return new TransformationStrategy(
            StrategyType.DETAILED_EXPANSION,
            5,
            "详细扩展策略",
            "生成更多详细的查询变体，从不同角度探索问题"
        );
    }
    
    /**
     * 问题聚焦扩展策略
     */
    public static TransformationStrategy problemFocusedExpansion() {
        return new TransformationStrategy(
            StrategyType.PROBLEM_FOCUSED_EXPANSION,
            4,
            "问题聚焦扩展策略",
            "专注于问题解决的查询变体，包含故障排除和解决方案相关的表述"
        );
    }
    
    /**
     * 技术扩展策略
     */
    public static TransformationStrategy technicalExpansion() {
        return new TransformationStrategy(
            StrategyType.TECHNICAL_EXPANSION,
            6,
            "技术扩展策略",
            "生成技术相关的查询变体，包含专业术语和技术细节"
        );
    }
    
    /**
     * 是否需要跳过扩展
     */
    public boolean shouldSkipExpansion() {
        return type == StrategyType.NO_EXPANSION;
    }
    
    /**
     * 是否为高扩展策略
     */
    public boolean isHighExpansion() {
        return maxQueries >= 5;
    }
    
    /**
     * 是否为中等扩展策略
     */
    public boolean isMediumExpansion() {
        return maxQueries >= 3 && maxQueries < 5;
    }
    
    /**
     * 是否为低扩展策略
     */
    public boolean isLowExpansion() {
        return maxQueries < 3;
    }
    
    @Override
    public String toString() {
        return String.format("TransformationStrategy{type=%s, maxQueries=%d, description='%s'}", 
                           type, maxQueries, description);
    }
}