package com.leyue.smartcs.domain.ltm.gateway;

import com.leyue.smartcs.domain.ltm.entity.SemanticMemory;

import java.util.List;
import java.util.Optional;

/**
 * 语义记忆网关接口
 * 定义语义记忆的数据访问操作
 */
public interface SemanticMemoryGateway {

    /**
     * 保存语义记忆
     */
    void save(SemanticMemory semanticMemory);

    /**
     * 批量保存语义记忆
     */
    void batchSave(List<SemanticMemory> memories);

    /**
     * 根据ID查找语义记忆
     */
    Optional<SemanticMemory> findById(Long id);

    /**
     * 根据用户ID和概念查找语义记忆
     */
    Optional<SemanticMemory> findByUserIdAndConcept(Long userId, String concept);

    /**
     * 根据用户ID查找所有语义记忆
     */
    List<SemanticMemory> findByUserId(Long userId, int page, int size);

    /**
     * 根据置信度查找语义记忆
     */
    List<SemanticMemory> findByConfidenceRange(Long userId, Double minConfidence, Double maxConfidence, int limit);

    /**
     * 查找高置信度的语义记忆
     */
    List<SemanticMemory> findHighConfidenceMemories(Long userId, Double minConfidence, int limit);

    /**
     * 查找争议性的语义记忆
     */
    List<SemanticMemory> findControversialMemories(Long userId, int limit);

    /**
     * 语义检索语义记忆
     */
    List<SemanticMemory> semanticSearch(Long userId, byte[] queryVector, Double threshold, int limit);

    /**
     * 概念模糊匹配
     */
    List<SemanticMemory> findByConceptLike(Long userId, String conceptPattern, int limit);

    /**
     * 查找需要更新的记忆
     */
    List<SemanticMemory> findMemoriesNeedingUpdate(Long userId, int limit);

    /**
     * 查找最近强化的记忆
     */
    List<SemanticMemory> findRecentlyReinforced(Long userId, int limit);

    /**
     * 根据来源情景记忆查找相关语义记忆
     */
    List<SemanticMemory> findBySourceEpisode(Long userId, String episodeId);

    /**
     * 更新语义记忆
     */
    void update(SemanticMemory semanticMemory);

    /**
     * 批量更新置信度
     */
    void batchUpdateConfidence(List<Long> ids, Double confidence);

    /**
     * 应用遗忘衰减到所有记忆
     */
    void applyDecayToAll(Long userId);

    /**
     * 删除语义记忆
     */
    void deleteById(Long id);

    /**
     * 根据用户ID删除所有语义记忆
     */
    void deleteByUserId(Long userId);

    /**
     * 删除低置信度的记忆
     */
    void deleteLowConfidenceMemories(Long userId, Double threshold);

    /**
     * 统计用户的语义记忆数量
     */
    Long countByUserId(Long userId);

    /**
     * 统计用户高置信度记忆数量
     */
    Long countHighConfidenceMemories(Long userId, Double threshold);

    /**
     * 获取用户记忆的平均置信度
     */
    Double getAverageConfidence(Long userId);

    /**
     * 查找相似概念的记忆
     */
    List<SemanticMemory> findSimilarConcepts(Long userId, String concept, Double similarity, int limit);

    /**
     * 查找与指定记忆冲突的其他记忆
     */
    List<SemanticMemory> findConflictingMemories(Long userId, Long memoryId, int limit);
}