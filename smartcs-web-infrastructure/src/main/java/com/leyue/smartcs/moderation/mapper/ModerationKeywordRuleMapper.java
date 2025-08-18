package com.leyue.smartcs.moderation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leyue.smartcs.moderation.dataobject.ModerationKeywordRuleDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 内容审核关键词规则Mapper
 */
@Mapper
public interface ModerationKeywordRuleMapper extends BaseMapper<ModerationKeywordRuleDO> {

    /**
     * 查找所有启用的关键词规则
     */
    List<ModerationKeywordRuleDO> findActiveKeywordRules();

    /**
     * 根据分类ID查找关键词规则
     */
    List<ModerationKeywordRuleDO> findByCategoryId(@Param("categoryId") Long categoryId);

    /**
     * 根据语言查找关键词规则
     */
    List<ModerationKeywordRuleDO> findByLanguage(@Param("language") String language);

    /**
     * 根据规则类型查找关键词规则
     */
    List<ModerationKeywordRuleDO> findByRuleType(@Param("ruleType") String ruleType);

    /**
     * 查找高优先级的关键词规则
     */
    List<ModerationKeywordRuleDO> findHighPriorityRules(@Param("maxPriority") int maxPriority);

    /**
     * 更新关键词规则的命中统计
     */
    void updateHitStatistics(@Param("ruleId") Long ruleId, @Param("hitCount") Long hitCount, @Param("lastHitAt") Long lastHitAt);

    /**
     * 批量更新规则的命中次数
     */
    void incrementHitCount(@Param("ruleId") Long ruleId, @Param("currentTime") Long currentTime);

    /**
     * 获取命中次数最多的规则（热点规则）
     */
    List<ModerationKeywordRuleDO> findHotRules(@Param("topN") int topN);

    /**
     * 根据有效时间查找规则
     */
    List<ModerationKeywordRuleDO> findEffectiveRules(@Param("currentTime") Long currentTime);

    /**
     * 查找即将过期的规则
     */
    List<ModerationKeywordRuleDO> findRulesExpiringSoon(@Param("startTime") Long startTime, @Param("endTime") Long endTime);

    /**
     * 统计各分类下的规则数量
     */
    List<java.util.Map<String, Object>> countRulesByCategory();

    /**
     * 删除过期的规则
     */
    int deactivateExpiredRules(@Param("currentTime") Long currentTime);
}