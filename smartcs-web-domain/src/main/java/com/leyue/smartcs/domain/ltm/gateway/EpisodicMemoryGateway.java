package com.leyue.smartcs.domain.ltm.gateway;

import com.leyue.smartcs.domain.ltm.entity.EpisodicMemory;

import java.util.List;
import java.util.Optional;

/**
 * 情景记忆网关接口
 * 定义情景记忆的数据访问操作
 */
public interface EpisodicMemoryGateway {

    /**
     * 保存情景记忆
     */
    void save(EpisodicMemory episodicMemory);

    /**
     * 批量保存情景记忆
     */
    void batchSave(List<EpisodicMemory> memories);

    /**
     * 根据ID查找情景记忆
     */
    Optional<EpisodicMemory> findById(Long id);

    /**
     * 根据情节ID查找情景记忆
     */
    Optional<EpisodicMemory> findByEpisodeId(String episodeId);

    /**
     * 根据用户ID和会话ID查找情景记忆列表
     */
    List<EpisodicMemory> findByUserIdAndSessionId(Long userId, Long sessionId);

    /**
     * 根据用户ID查找情景记忆列表（分页）
     */
    List<EpisodicMemory> findByUserId(Long userId, int page, int size);

    /**
     * 根据用户ID和时间范围查找情景记忆
     */
    List<EpisodicMemory> findByUserIdAndTimeRange(Long userId, Long startTime, Long endTime);

    /**
     * 根据重要性评分查找情景记忆
     */
    List<EpisodicMemory> findByImportanceScore(Long userId, Double minScore, int limit);

    /**
     * 语义检索情景记忆
     */
    List<EpisodicMemory> semanticSearch(Long userId, byte[] queryVector, Double threshold, int limit);

    /**
     * 混合检索（语义+关键词）
     */
    List<EpisodicMemory> hybridSearch(Long userId, byte[] queryVector, String keywords, Double threshold, int limit);

    /**
     * 查找需要巩固的记忆
     */
    List<EpisodicMemory> findMemoriesNeedingConsolidation(Long userId, double minImportanceScore, int limit);

    /**
     * 获取存在待巩固记忆的用户ID
     *
     * @param minImportanceScore 记忆重要性阈值
     * @param startingAfterUserId 游标模式下起始用户ID（可为空）
     * @param limit 单次提取的最大用户数量
     */
    List<Long> findUserIdsNeedingConsolidation(double minImportanceScore, Long startingAfterUserId, int limit);

    /**
     * 查找最近访问的记忆
     */
    List<EpisodicMemory> findRecentlyAccessed(Long userId, int limit);

    /**
     * 查找高频访问的记忆
     */
    List<EpisodicMemory> findHighFrequencyAccessed(Long userId, int minAccessCount, int limit);

    /**
     * 更新情景记忆
     */
    void update(EpisodicMemory episodicMemory);

    /**
     * 批量更新巩固状态
     */
    void batchUpdateConsolidationStatus(List<Long> ids, Integer status);

    /**
     * 删除情景记忆
     */
    void deleteById(Long id);

    /**
     * 根据用户ID删除所有情景记忆
     */
    void deleteByUserId(Long userId);

    /**
     * 根据时间范围删除情景记忆
     */
    void deleteByTimeRange(Long userId, Long startTime, Long endTime);

    /**
     * 统计用户的情景记忆数量
     */
    Long countByUserId(Long userId);

    /**
     * 统计用户在指定时间范围内的记忆数量
     */
    Long countByUserIdAndTimeRange(Long userId, Long startTime, Long endTime);

    /**
     * 获取用户记忆的平均重要性评分
     */
    Double getAverageImportanceScore(Long userId);

    /**
     * 查找最早的记忆时间
     */
    Optional<Long> findEarliestMemoryTime(Long userId);

    /**
     * 查找最新的记忆时间
     */
    Optional<Long> findLatestMemoryTime(Long userId);
}
