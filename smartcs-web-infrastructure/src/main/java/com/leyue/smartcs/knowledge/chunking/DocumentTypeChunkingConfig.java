package com.leyue.smartcs.knowledge.chunking;

import com.leyue.smartcs.knowledge.enums.DocumentTypeEnum;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 文档类型分块配置
 * 定义每种文档类型应该使用的分块策略组合
 */
@Data
@Builder
public class DocumentTypeChunkingConfig {
    
    /**
     * 文档类型
     */
    private DocumentTypeEnum documentType;
    
    /**
     * 策略配置列表，按执行顺序排列
     */
    private List<StrategyConfig> strategyConfigs;
    
    /**
     * 全局配置参数
     */
    private Map<String, Object> globalConfig;
    
    /**
     * 是否启用并行处理
     */
    private boolean enableParallel;
    
    /**
     * 配置描述
     */
    private String description;
    
    /**
     * 单个策略配置
     */
    @Data
    @Builder
    public static class StrategyConfig {
        /**
         * 策略名称
         */
        private String strategyName;
        
        /**
         * 策略特定配置参数
         */
        private Map<String, Object> config;
        
        /**
         * 是否启用该策略
         */
        private boolean enabled;
        
        /**
         * 策略权重（用于并行处理时的结果合并）
         */
        private double weight;
        
        /**
         * 条件执行表达式（可选）
         * 例如：文档大小超过某个阈值时才执行
         */
        private String condition;
    }
    
    /**
     * 获取启用的策略配置
     */
    public List<StrategyConfig> getEnabledStrategies() {
        return strategyConfigs.stream()
                .filter(StrategyConfig::isEnabled)
                .toList();
    }
    
    /**
     * 验证配置有效性
     */
    public boolean isValid() {
        if (documentType == null || strategyConfigs == null || strategyConfigs.isEmpty()) {
            return false;
        }
        
        // 检查是否至少有一个策略启用
        return strategyConfigs.stream().anyMatch(StrategyConfig::isEnabled);
    }
    
    /**
     * 合并全局配置和策略特定配置
     */
    public Map<String, Object> getMergedConfig(StrategyConfig strategyConfig) {
        Map<String, Object> mergedConfig = globalConfig != null ? 
                Map.copyOf(globalConfig) : Map.of();
        
        if (strategyConfig.getConfig() != null) {
            // 策略特定配置优先级更高
            mergedConfig.putAll(strategyConfig.getConfig());
        }
        
        return mergedConfig;
    }
}