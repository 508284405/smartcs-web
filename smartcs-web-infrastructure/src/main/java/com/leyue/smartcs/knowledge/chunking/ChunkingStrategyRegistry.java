package com.leyue.smartcs.knowledge.chunking;

import com.leyue.smartcs.knowledge.enums.DocumentTypeEnum;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 分块策略注册器接口
 * 负责管理所有可用的分块策略
 */
public interface ChunkingStrategyRegistry {
    
    /**
     * 注册新的分块策略
     */
    void registerStrategy(ChunkingStrategy strategy);
    
    /**
     * 注销分块策略
     */
    void unregisterStrategy(String strategyName);
    
    /**
     * 根据名称获取策略
     */
    Optional<ChunkingStrategy> getStrategy(String strategyName);
    
    /**
     * 获取支持指定文档类型的所有策略
     */
    List<ChunkingStrategy> getStrategiesForDocumentType(DocumentTypeEnum documentType);
    
    /**
     * 获取所有已注册的策略
     */
    List<ChunkingStrategy> getAllStrategies();
    
    /**
     * 获取所有策略名称
     */
    List<String> getAllStrategyNames();
    
    /**
     * 检查策略是否已注册
     */
    boolean isRegistered(String strategyName);
    
    /**
     * 根据文档类型获取默认策略组合
     */
    List<ChunkingStrategy> getDefaultStrategies(DocumentTypeEnum documentType);
    
    /**
     * 根据配置创建分块管道
     */
    ChunkingPipeline createPipeline(DocumentTypeChunkingConfig config);
    
    /**
     * 获取策略统计信息
     */
    Map<String, Object> getStrategyStats();
}