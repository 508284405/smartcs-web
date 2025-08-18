package com.leyue.smartcs.domain.moderation.gateway;

import com.leyue.smartcs.domain.moderation.ModerationCategory;
import com.leyue.smartcs.domain.moderation.ModerationRecord;
import com.leyue.smartcs.domain.moderation.enums.ContentType;
import com.leyue.smartcs.domain.moderation.enums.ModerationResult;
import com.leyue.smartcs.domain.moderation.enums.SeverityLevel;
import com.leyue.smartcs.domain.moderation.enums.SourceType;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * 内容审核领域网关接口
 * 定义审核相关的数据访问操作
 */
public interface ModerationGateway {

    // ====================== 审核分类管理 ======================

    /**
     * 保存或更新违规分类
     */
    ModerationCategory saveModerationCategory(ModerationCategory category);

    /**
     * 根据ID获取违规分类
     */
    Optional<ModerationCategory> findCategoryById(Long id);

    /**
     * 根据编码获取违规分类
     */
    Optional<ModerationCategory> findCategoryByCode(String code);

    /**
     * 获取所有一级分类
     */
    List<ModerationCategory> findTopLevelCategories();

    /**
     * 获取指定父分类下的子分类
     */
    List<ModerationCategory> findSubCategoriesByParentId(Long parentId);

    /**
     * 获取所有启用的分类（树形结构）
     */
    List<ModerationCategory> findActiveCategoriesTree();

    /**
     * 获取所有分类（扁平列表）
     */
    List<ModerationCategory> findAllCategories();

    /**
     * 根据严重程度查找分类
     */
    List<ModerationCategory> findCategoriesBySeverityLevel(SeverityLevel severityLevel);

    /**
     * 删除违规分类
     */
    boolean deleteModerationCategory(Long id);

    // ====================== 审核记录管理 ======================

    /**
     * 保存审核记录
     */
    ModerationRecord saveModerationRecord(ModerationRecord record);

    /**
     * 根据ID获取审核记录
     */
    Optional<ModerationRecord> findRecordById(Long id);

    /**
     * 根据内容哈希获取审核记录
     */
    Optional<ModerationRecord> findRecordByContentHash(String contentHash);

    /**
     * 根据源ID和类型查找审核记录
     */
    List<ModerationRecord> findRecordsBySourceIdAndType(String sourceId, SourceType sourceType);

    /**
     * 根据用户ID查找审核记录
     */
    List<ModerationRecord> findRecordsByUserId(String userId);

    /**
     * 根据会话ID查找审核记录
     */
    List<ModerationRecord> findRecordsBySessionId(String sessionId);

    /**
     * 分页查询审核记录
     */
    PageResult<ModerationRecord> findRecordsByPage(ModerationRecordQuery query);

    /**
     * 获取需要人工审核的记录
     */
    List<ModerationRecord> findRecordsNeedingManualReview();

    /**
     * 获取被阻断的记录
     */
    List<ModerationRecord> findBlockedRecords(int limit);

    /**
     * 根据风险等级统计记录数量
     */
    RecordCountByRiskLevel countRecordsByRiskLevel();

    // ====================== 关键词规则管理 ======================

    /**
     * 获取所有启用的关键词规则
     */
    List<KeywordRule> findActiveKeywordRules();

    /**
     * 根据分类ID获取关键词规则
     */
    List<KeywordRule> findKeywordRulesByCategoryId(Long categoryId);

    /**
     * 根据语言获取关键词规则
     */
    List<KeywordRule> findKeywordRulesByLanguage(String language);

    /**
     * 保存关键词规则
     */
    KeywordRule saveKeywordRule(KeywordRule rule);

    /**
     * 更新关键词规则命中统计
     */
    void updateKeywordRuleHitCount(Long ruleId, long hitCount, long lastHitTime);

    // ====================== 配置管理 ======================

    /**
     * 获取配置值
     */
    Optional<String> getConfigValue(String configKey);

    /**
     * 保存配置
     */
    void saveConfig(String configKey, String configValue);

    /**
     * 获取所有配置
     */
    List<ModerationConfig> findAllConfigs();

    /**
     * 根据分类获取配置
     */
    List<ModerationConfig> findConfigsByCategory(String category);

    // ====================== 统计查询 ======================

    /**
     * 获取审核统计信息
     */
    ModerationStatistics getModerationStatistics();

    /**
     * 获取违规趋势统计
     */
    List<ViolationTrend> getViolationTrends(long startTime, long endTime);

    /**
     * 获取热点违规分类
     */
    List<HotViolationCategory> getHotViolationCategories(int topN);

    // ====================== 内部接口和类 ======================

    /**
     * 分页结果封装类
     */
    interface PageResult<T> {
        List<T> getData();
        long getTotal();
        int getPageSize();
        int getPageNumber();
        boolean hasNext();
    }

    /**
     * 审核记录查询条件
     */
    interface ModerationRecordQuery {
        ContentType getContentType();
        SourceType getSourceType();
        ModerationResult getModerationResult();
        SeverityLevel getRiskLevel();
        String getUserId();
        String getSessionId();
        Boolean getIsBlocked();
        Long getStartTime();
        Long getEndTime();
        int getPageNumber();
        int getPageSize();
        String getSortBy();
        String getSortOrder();
    }

    /**
     * 关键词规则实体接口
     */
    interface KeywordRule {
        Long getId();
        String getRuleName();
        String getKeyword();
        Long getCategoryId();
        String getRuleType();
        String getMatchMode();
        Boolean getCaseSensitive();
        BigDecimal getSimilarityThreshold();
        Boolean getIsActive();
        Integer getPriority();
    }

    /**
     * 审核配置实体接口
     */
    interface ModerationConfig {
        String getConfigKey();
        String getConfigName();
        String getConfigValue();
        String getConfigType();
        String getCategory();
        Boolean getIsActive();
    }

    /**
     * 风险等级统计结果
     */
    interface RecordCountByRiskLevel {
        long getLowCount();
        long getMediumCount();
        long getHighCount();
        long getCriticalCount();
    }

    /**
     * 审核统计信息
     */
    interface ModerationStatistics {
        long getTotalRecords();
        long getApprovedCount();
        long getRejectedCount();
        long getPendingCount();
        long getBlockedCount();
        double getAverageProcessingTime();
        long getTodayRecords();
        RecordCountByRiskLevel getRiskLevelDistribution();
    }

    /**
     * 违规趋势统计
     */
    interface ViolationTrend {
        long getTimestamp();
        String getDate();
        long getViolationCount();
        String getTopViolationType();
    }

    /**
     * 热点违规分类
     */
    interface HotViolationCategory {
        String getCategoryCode();
        String getCategoryName();
        long getHitCount();
        double getPercentage();
    }
}