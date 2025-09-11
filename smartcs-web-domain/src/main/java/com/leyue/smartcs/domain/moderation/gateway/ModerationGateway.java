package com.leyue.smartcs.domain.moderation.gateway;

import com.leyue.smartcs.domain.moderation.*;
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

    // ====================== 审核策略管理 ======================

    /**
     * 保存或更新审核策略
     */
    ModerationPolicy saveModerationPolicy(ModerationPolicy policy);

    /**
     * 根据ID获取审核策略
     */
    Optional<ModerationPolicy> findPolicyById(Long id);

    /**
     * 根据编码获取审核策略
     */
    Optional<ModerationPolicy> findPolicyByCode(String code);

    /**
     * 根据场景获取可用的策略列表（按优先级排序）
     */
    List<ModerationPolicy> findPoliciesByScenario(String scenario);

    /**
     * 根据策略类型获取策略列表
     */
    List<ModerationPolicy> findPoliciesByType(String policyType);

    /**
     * 获取所有启用的策略（按优先级排序）
     */
    List<ModerationPolicy> findAllActivePolicies();

    /**
     * 根据模板ID获取关联的策略列表
     */
    List<ModerationPolicy> findPoliciesByTemplateId(Long templateId);

    /**
     * 删除审核策略
     */
    boolean deleteModerationPolicy(Long id);

    // ====================== 审核维度管理 ======================

    /**
     * 保存或更新审核维度
     */
    ModerationDimension saveModerationDimension(ModerationDimension dimension);

    /**
     * 根据ID获取审核维度
     */
    Optional<ModerationDimension> findDimensionById(Long id);

    /**
     * 根据编码获取审核维度
     */
    Optional<ModerationDimension> findDimensionByCode(String code);

    /**
     * 根据分类获取维度列表（按排序权重排序）
     */
    List<ModerationDimension> findDimensionsByCategory(String category);

    /**
     * 根据严重程度获取维度列表
     */
    List<ModerationDimension> findDimensionsBySeverityLevel(SeverityLevel severityLevel);

    /**
     * 获取所有启用的维度（按排序权重排序）
     */
    List<ModerationDimension> findAllActiveDimensions();

    /**
     * 根据审核分类ID获取关联的维度列表
     */
    List<ModerationDimension> findDimensionsByCategoryId(Long categoryId);

    /**
     * 根据置信度阈值范围获取维度列表
     */
    List<ModerationDimension> findDimensionsByConfidenceRange(Double minThreshold, Double maxThreshold);

    /**
     * 删除审核维度
     */
    boolean deleteModerationDimension(Long id);

    // ====================== 策略维度关联管理 ======================

    /**
     * 根据策略ID获取关联的维度列表
     */
    List<ModerationDimension> findDimensionsByPolicyId(Long policyId);

    /**
     * 根据维度ID获取关联的策略列表
     */
    List<ModerationPolicy> findPoliciesByDimensionId(Long dimensionId);

    /**
     * 保存策略维度关联关系
     */
    boolean savePolicyDimensionRelation(Long policyId, Long dimensionId, Boolean isActive, Double weight, Double customThreshold, String customAction, String updatedBy);

    /**
     * 删除策略维度关联关系
     */
    boolean deletePolicyDimensionRelation(Long policyId, Long dimensionId);

    /**
     * 批量更新策略维度关联的启用状态
     */
    boolean batchUpdatePolicyDimensionStatus(Long policyId, List<Long> dimensionIds, Boolean isActive, String updatedBy);

    // ====================== 策略模板管理 ======================

    /**
     * 保存或更新策略模板
     */
    ModerationPolicyTemplate saveModerationPolicyTemplate(ModerationPolicyTemplate template);

    /**
     * 根据ID获取策略模板
     */
    Optional<ModerationPolicyTemplate> findTemplateById(Long id);

    /**
     * 根据编码获取策略模板（最新版本）
     */
    Optional<ModerationPolicyTemplate> findTemplateByCode(String code);

    /**
     * 根据编码和版本获取策略模板
     */
    Optional<ModerationPolicyTemplate> findTemplateByCodeAndVersion(String code, String version);

    /**
     * 根据模板类型获取模板列表
     */
    List<ModerationPolicyTemplate> findTemplatesByType(String templateType);

    /**
     * 根据语言获取模板列表
     */
    List<ModerationPolicyTemplate> findTemplatesByLanguage(String language);

    /**
     * 获取所有启用的模板（按类型和版本排序）
     */
    List<ModerationPolicyTemplate> findAllActiveTemplates();

    /**
     * 根据编码获取所有版本的模板
     */
    List<ModerationPolicyTemplate> findAllTemplateVersionsByCode(String code);

    /**
     * 获取指定编码的最新版本号
     */
    String findLatestTemplateVersionByCode(String code);

    /**
     * 根据模板类型和语言获取模板
     */
    Optional<ModerationPolicyTemplate> findTemplateByTypeAndLanguage(String templateType, String language);

    /**
     * 删除策略模板
     */
    boolean deleteModerationPolicyTemplate(Long id);

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