package com.leyue.smartcs.domain.ltm.gateway;

import com.leyue.smartcs.domain.ltm.entity.ProceduralMemory;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 程序性记忆网关接口
 * 定义程序性记忆的数据访问操作
 */
public interface ProceduralMemoryGateway {

    /**
     * 保存程序性记忆
     */
    void save(ProceduralMemory proceduralMemory);

    /**
     * 批量保存程序性记忆
     */
    void batchSave(List<ProceduralMemory> memories);

    /**
     * 根据ID查找程序性记忆
     */
    Optional<ProceduralMemory> findById(Long id);

    /**
     * 根据用户ID、模式类型和模式名称查找
     */
    Optional<ProceduralMemory> findByUserIdAndPatternTypeAndName(Long userId, String patternType, String patternName);

    /**
     * 根据用户ID查找所有程序性记忆
     */
    List<ProceduralMemory> findByUserId(Long userId, int page, int size);

    /**
     * 根据用户ID和模式类型查找
     */
    List<ProceduralMemory> findByUserIdAndPatternType(Long userId, String patternType);

    /**
     * 查找活跃的程序性记忆
     */
    List<ProceduralMemory> findActiveMemories(Long userId);

    /**
     * 查找高成功率的记忆
     */
    List<ProceduralMemory> findHighSuccessRateMemories(Long userId, Double minSuccessRate, int limit);

    /**
     * 查找需要调整的记忆
     */
    List<ProceduralMemory> findMemoriesNeedingAdjustment(Long userId, int limit);

    /**
     * 查找最近触发的记忆
     */
    List<ProceduralMemory> findRecentlyTriggered(Long userId, int limit);

    /**
     * 根据触发条件匹配记忆
     */
    List<ProceduralMemory> findMatchingMemories(Long userId, Map<String, Object> context);

    /**
     * 根据用户偏好查找记忆
     */
    List<ProceduralMemory> findPreferenceMemories(Long userId);

    /**
     * 根据响应风格查找记忆
     */
    List<ProceduralMemory> findResponseStyleMemories(Long userId);

    /**
     * 根据习惯模式查找记忆
     */
    List<ProceduralMemory> findHabitMemories(Long userId);

    /**
     * 根据规则模式查找记忆
     */
    List<ProceduralMemory> findRuleMemories(Long userId);

    /**
     * 更新程序性记忆
     */
    void update(ProceduralMemory proceduralMemory);

    /**
     * 批量更新活跃状态
     */
    void batchUpdateActiveStatus(List<Long> ids, Boolean isActive);

    /**
     * 批量更新成功率
     */
    void batchUpdateSuccessRate(List<Long> ids);

    /**
     * 删除程序性记忆
     */
    void deleteById(Long id);

    /**
     * 根据用户ID删除所有程序性记忆
     */
    void deleteByUserId(Long userId);

    /**
     * 删除低成功率的记忆
     */
    void deleteLowSuccessRateMemories(Long userId, Double threshold);

    /**
     * 删除非活跃的记忆
     */
    void deleteInactiveMemories(Long userId);

    /**
     * 统计用户的程序性记忆数量
     */
    Long countByUserId(Long userId);

    /**
     * 统计活跃记忆数量
     */
    Long countActiveMemories(Long userId);

    /**
     * 统计各类型记忆数量
     */
    Map<String, Long> countByPatternType(Long userId);

    /**
     * 获取用户记忆的平均成功率
     */
    Double getAverageSuccessRate(Long userId);

    /**
     * 查找成功率最高的记忆
     */
    List<ProceduralMemory> findTopSuccessMemories(Long userId, int limit);

    /**
     * 查找最常触发的记忆
     */
    List<ProceduralMemory> findMostTriggeredMemories(Long userId, int limit);
}