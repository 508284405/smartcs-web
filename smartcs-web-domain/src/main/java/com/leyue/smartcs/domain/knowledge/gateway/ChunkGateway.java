package com.leyue.smartcs.domain.knowledge.gateway;

import java.util.List;

import com.leyue.smartcs.domain.knowledge.Chunk;
import com.leyue.smartcs.domain.knowledge.enums.StrategyNameEnum;

/**
 * 切片Gateway接口
 */
public interface ChunkGateway {
    
    /**
     * 保存切片
     * @param chunk 切片对象
     */
    void save(Chunk chunk);
    
    /**
     * 更新切片
     * @param chunk 切片对象
     */
    void update(Chunk chunk);
    
    /**
     * 根据ID查找切片
     * @param id 切片ID
     * @return 切片对象
     */
    Chunk findById(Long id);
    
    /**
     * 根据ID删除切片
     * @param id 切片ID
     */
    void deleteById(Long id);

    /**
     * 根据内容ID删除切片
     * @param contentId 内容ID
     * @param strategyName 解析策略名称
     */
    void deleteByContentId(Long contentId);

    /**
     * 根据内容ID保存切片
     * @param contentId 内容ID
     * @param chunks 切片列表
     * @param strategyName 解析策略名称
     */
    List<Chunk> saveBatch(Long contentId, List<Chunk> chunks, StrategyNameEnum strategyName);

    /**
     * 根据切片ID更新切片向量ID
     * @param chunks 切片列表
     */
    void updateBatchVectorId(List<Chunk> chunks);
} 