package com.leyue.smartcs.moderation.gatewayimpl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leyue.smartcs.domain.moderation.*;
import com.leyue.smartcs.domain.moderation.enums.ContentType;
import com.leyue.smartcs.domain.moderation.enums.ModerationResult;
import com.leyue.smartcs.domain.moderation.enums.SeverityLevel;
import com.leyue.smartcs.domain.moderation.enums.SourceType;
import com.leyue.smartcs.domain.moderation.gateway.ModerationGateway;
import com.leyue.smartcs.moderation.convertor.ModerationConvertor;
import com.leyue.smartcs.moderation.dataobject.*;
import com.leyue.smartcs.moderation.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 内容审核领域网关实现
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ModerationGatewayImpl implements ModerationGateway {

    private final ModerationCategoryMapper categoryMapper;
    private final ModerationRecordMapper recordMapper;
    private final ModerationConfigMapper configMapper;
    private final ModerationKeywordRuleMapper keywordRuleMapper;
    private final ModerationPolicyMapper policyMapper;
    private final ModerationDimensionMapper dimensionMapper;
    private final ModerationPolicyDimensionMapper policyDimensionMapper;
    private final ModerationPolicyTemplateMapper templateMapper;
    private final ModerationConvertor moderationConvertor;

    // ====================== 审核分类管理 ======================

    @Override
    public ModerationCategory saveModerationCategory(ModerationCategory category) {
        ModerationCategoryDO categoryDO = moderationConvertor.toCategoryDO(category);
        if (categoryDO.getId() == null) {
            categoryMapper.insert(categoryDO);
        } else {
            categoryMapper.updateById(categoryDO);
        }
        return moderationConvertor.toCategoryDomain(categoryDO);
    }

    @Override
    public Optional<ModerationCategory> findCategoryById(Long id) {
        ModerationCategoryDO categoryDO = categoryMapper.selectById(id);
        return Optional.ofNullable(categoryDO)
                .map(moderationConvertor::toCategoryDomain);
    }

    @Override
    public Optional<ModerationCategory> findCategoryByCode(String code) {
        ModerationCategoryDO categoryDO = categoryMapper.findByCode(code);
        return Optional.ofNullable(categoryDO)
                .map(moderationConvertor::toCategoryDomain);
    }

    @Override
    public List<ModerationCategory> findTopLevelCategories() {
        List<ModerationCategoryDO> categoryDOs = categoryMapper.findTopLevelCategories();
        return categoryDOs.stream()
                .map(moderationConvertor::toCategoryDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ModerationCategory> findSubCategoriesByParentId(Long parentId) {
        List<ModerationCategoryDO> categoryDOs = categoryMapper.findSubCategoriesByParentId(parentId);
        return categoryDOs.stream()
                .map(moderationConvertor::toCategoryDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ModerationCategory> findActiveCategoriesTree() {
        List<ModerationCategoryDO> allCategories = categoryMapper.findAllActiveCategories();
        return moderationConvertor.buildCategoryTree(allCategories);
    }

    @Override
    public List<ModerationCategory> findAllCategories() {
        LambdaQueryWrapper<ModerationCategoryDO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(ModerationCategoryDO::getParentId)
                   .orderByAsc(ModerationCategoryDO::getSortOrder)
                   .orderByAsc(ModerationCategoryDO::getId);
        List<ModerationCategoryDO> categoryDOs = categoryMapper.selectList(queryWrapper);
        return categoryDOs.stream()
                .map(moderationConvertor::toCategoryDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ModerationCategory> findCategoriesBySeverityLevel(SeverityLevel severityLevel) {
        List<ModerationCategoryDO> categoryDOs = categoryMapper.findBySeverityLevel(severityLevel.getCode());
        return categoryDOs.stream()
                .map(moderationConvertor::toCategoryDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean deleteModerationCategory(Long id) {
        try {
            // 检查是否有子分类
            int subCount = categoryMapper.countSubCategories(id);
            if (subCount > 0) {
                log.warn("Cannot delete category {} because it has {} sub-categories", id, subCount);
                return false;
            }
            
            int result = categoryMapper.deleteById(id);
            return result > 0;
        } catch (Exception e) {
            log.error("Failed to delete moderation category {}", id, e);
            return false;
        }
    }

    // ====================== 审核记录管理 ======================

    @Override
    public ModerationRecord saveModerationRecord(ModerationRecord record) {
        ModerationRecordDO recordDO = moderationConvertor.toRecordDO(record);
        if (recordDO.getId() == null) {
            recordMapper.insert(recordDO);
        } else {
            recordMapper.updateById(recordDO);
        }
        return moderationConvertor.toRecordDomain(recordDO);
    }

    @Override
    public Optional<ModerationRecord> findRecordById(Long id) {
        ModerationRecordDO recordDO = recordMapper.selectById(id);
        return Optional.ofNullable(recordDO)
                .map(moderationConvertor::toRecordDomain);
    }

    @Override
    public Optional<ModerationRecord> findRecordByContentHash(String contentHash) {
        ModerationRecordDO recordDO = recordMapper.findByContentHash(contentHash);
        return Optional.ofNullable(recordDO)
                .map(moderationConvertor::toRecordDomain);
    }

    @Override
    public List<ModerationRecord> findRecordsBySourceIdAndType(String sourceId, SourceType sourceType) {
        List<ModerationRecordDO> recordDOs = recordMapper.findBySourceIdAndType(sourceId, sourceType.getCode());
        return recordDOs.stream()
                .map(moderationConvertor::toRecordDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ModerationRecord> findRecordsByUserId(String userId) {
        List<ModerationRecordDO> recordDOs = recordMapper.findByUserId(userId, 100);
        return recordDOs.stream()
                .map(moderationConvertor::toRecordDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ModerationRecord> findRecordsBySessionId(String sessionId) {
        List<ModerationRecordDO> recordDOs = recordMapper.findBySessionId(sessionId);
        return recordDOs.stream()
                .map(moderationConvertor::toRecordDomain)
                .collect(Collectors.toList());
    }

    @Override
    public PageResult<ModerationRecord> findRecordsByPage(ModerationRecordQuery query) {
        Page<ModerationRecordDO> page = new Page<>(query.getPageNumber(), query.getPageSize());
        IPage<ModerationRecordDO> result = recordMapper.findRecordsByPage(
                page,
                query.getContentType() != null ? query.getContentType().getCode() : null,
                query.getSourceType() != null ? query.getSourceType().getCode() : null,
                query.getModerationResult() != null ? query.getModerationResult().getCode() : null,
                query.getRiskLevel() != null ? query.getRiskLevel().getCode() : null,
                query.getUserId(),
                query.getSessionId(),
                query.getIsBlocked() != null ? (query.getIsBlocked() ? 1 : 0) : null,
                query.getStartTime(),
                query.getEndTime()
        );

        List<ModerationRecord> records = result.getRecords().stream()
                .map(moderationConvertor::toRecordDomain)
                .collect(Collectors.toList());

        return new PageResultImpl<>(records, result.getTotal(), 
                (int) result.getSize(), (int) result.getCurrent(), result.getCurrent() < result.getPages());
    }

    @Override
    public List<ModerationRecord> findRecordsNeedingManualReview() {
        List<ModerationRecordDO> recordDOs = recordMapper.findRecordsNeedingManualReview(50);
        return recordDOs.stream()
                .map(moderationConvertor::toRecordDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ModerationRecord> findBlockedRecords(int limit) {
        List<ModerationRecordDO> recordDOs = recordMapper.findBlockedRecords(limit);
        return recordDOs.stream()
                .map(moderationConvertor::toRecordDomain)
                .collect(Collectors.toList());
    }

    @Override
    public RecordCountByRiskLevel countRecordsByRiskLevel() {
        java.util.Map<String, Object> result = recordMapper.countByRiskLevel();
        return new RecordCountByRiskLevelImpl(result);
    }

    // ====================== 关键词规则管理 ======================

    @Override
    public List<KeywordRule> findActiveKeywordRules() {
        List<ModerationKeywordRuleDO> ruleDOs = keywordRuleMapper.findActiveKeywordRules();
        return ruleDOs.stream()
                .map(this::toKeywordRuleDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<KeywordRule> findKeywordRulesByCategoryId(Long categoryId) {
        List<ModerationKeywordRuleDO> ruleDOs = keywordRuleMapper.findByCategoryId(categoryId);
        return ruleDOs.stream()
                .map(this::toKeywordRuleDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<KeywordRule> findKeywordRulesByLanguage(String language) {
        List<ModerationKeywordRuleDO> ruleDOs = keywordRuleMapper.findByLanguage(language);
        return ruleDOs.stream()
                .map(this::toKeywordRuleDomain)
                .collect(Collectors.toList());
    }

    @Override
    public KeywordRule saveKeywordRule(KeywordRule rule) {
        // 这里需要实现，暂时返回空
        return rule;
    }

    @Override
    public void updateKeywordRuleHitCount(Long ruleId, long hitCount, long lastHitTime) {
        keywordRuleMapper.updateHitStatistics(ruleId, hitCount, lastHitTime);
    }

    // ====================== 配置管理 ======================

    @Override
    public Optional<String> getConfigValue(String configKey) {
        ModerationConfigDO configDO = configMapper.findByConfigKey(configKey);
        return Optional.ofNullable(configDO)
                .map(ModerationConfigDO::getConfigValue);
    }

    @Override
    public void saveConfig(String configKey, String configValue) {
        ModerationConfigDO existing = configMapper.findByConfigKey(configKey);
        if (existing != null) {
            existing.setConfigValue(configValue);
            existing.setUpdatedAt(System.currentTimeMillis());
            configMapper.updateById(existing);
        } else {
            // 创建新配置的逻辑需要更多信息，这里简化处理
            log.warn("Attempting to save non-existing config key: {}", configKey);
        }
    }

    @Override
    public List<ModerationConfig> findAllConfigs() {
        List<ModerationConfigDO> configDOs = configMapper.findAllActiveConfigs();
        return configDOs.stream()
                .map(this::toConfigDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ModerationConfig> findConfigsByCategory(String category) {
        List<ModerationConfigDO> configDOs = configMapper.findByCategory(category);
        return configDOs.stream()
                .map(this::toConfigDomain)
                .collect(Collectors.toList());
    }

    // ====================== 统计查询 ======================

    @Override
    public ModerationStatistics getModerationStatistics() {
        long todayStart = System.currentTimeMillis() - (System.currentTimeMillis() % 86400000);
        java.util.Map<String, Object> stats = recordMapper.getModerationStatistics(todayStart);
        java.util.Map<String, Object> riskStats = recordMapper.countByRiskLevel();
        return new ModerationStatisticsImpl(stats, riskStats);
    }

    @Override
    public List<ViolationTrend> getViolationTrends(long startTime, long endTime) {
        List<java.util.Map<String, Object>> trends = recordMapper.getViolationTrends(startTime, endTime);
        return trends.stream()
                .map(ViolationTrendImpl::new)
                .collect(Collectors.toList());
    }

    @Override
    public List<HotViolationCategory> getHotViolationCategories(int topN) {
        // 这里需要更复杂的查询逻辑，暂时返回空列表
        return List.of();
    }

    // ====================== 内部辅助方法 ======================

    private KeywordRule toKeywordRuleDomain(ModerationKeywordRuleDO ruleDO) {
        return new KeywordRuleImpl(ruleDO);
    }

    private ModerationConfig toConfigDomain(ModerationConfigDO configDO) {
        return new ModerationConfigImpl(configDO);
    }

    // ====================== 内部实现类 ======================

    private static class PageResultImpl<T> implements PageResult<T> {
        private final List<T> data;
        private final long total;
        private final int pageSize;
        private final int pageNumber;
        private final boolean hasNext;

        public PageResultImpl(List<T> data, long total, int pageSize, int pageNumber, boolean hasNext) {
            this.data = data;
            this.total = total;
            this.pageSize = pageSize;
            this.pageNumber = pageNumber;
            this.hasNext = hasNext;
        }

        @Override
        public List<T> getData() { return data; }
        @Override
        public long getTotal() { return total; }
        @Override
        public int getPageSize() { return pageSize; }
        @Override
        public int getPageNumber() { return pageNumber; }
        @Override
        public boolean hasNext() { return hasNext; }
    }

    private static class KeywordRuleImpl implements KeywordRule {
        private final ModerationKeywordRuleDO ruleDO;

        public KeywordRuleImpl(ModerationKeywordRuleDO ruleDO) {
            this.ruleDO = ruleDO;
        }

        @Override
        public Long getId() { return ruleDO.getId(); }
        @Override
        public String getRuleName() { return ruleDO.getRuleName(); }
        @Override
        public String getKeyword() { return ruleDO.getKeyword(); }
        @Override
        public Long getCategoryId() { return ruleDO.getCategoryId(); }
        @Override
        public String getRuleType() { return ruleDO.getRuleType(); }
        @Override
        public String getMatchMode() { return ruleDO.getMatchMode(); }
        @Override
        public Boolean getCaseSensitive() { return ruleDO.getCaseSensitive() == 1; }
        @Override
        public BigDecimal getSimilarityThreshold() { return ruleDO.getSimilarityThreshold(); }
        @Override
        public Boolean getIsActive() { return ruleDO.getIsActive() == 1; }
        @Override
        public Integer getPriority() { return ruleDO.getPriority(); }
    }

    private static class ModerationConfigImpl implements ModerationConfig {
        private final ModerationConfigDO configDO;

        public ModerationConfigImpl(ModerationConfigDO configDO) {
            this.configDO = configDO;
        }

        @Override
        public String getConfigKey() { return configDO.getConfigKey(); }
        @Override
        public String getConfigName() { return configDO.getConfigName(); }
        @Override
        public String getConfigValue() { return configDO.getConfigValue(); }
        @Override
        public String getConfigType() { return configDO.getConfigType(); }
        @Override
        public String getCategory() { return configDO.getCategory(); }
        @Override
        public Boolean getIsActive() { return configDO.getIsActive() == 1; }
    }

    private static class RecordCountByRiskLevelImpl implements RecordCountByRiskLevel {
        private final java.util.Map<String, Object> data;

        public RecordCountByRiskLevelImpl(java.util.Map<String, Object> data) {
            this.data = data;
        }

        @Override
        public long getLowCount() { return getLongValue("lowCount"); }
        @Override
        public long getMediumCount() { return getLongValue("mediumCount"); }
        @Override
        public long getHighCount() { return getLongValue("highCount"); }
        @Override
        public long getCriticalCount() { return getLongValue("criticalCount"); }

        private long getLongValue(String key) {
            Object value = data.get(key);
            return value != null ? ((Number) value).longValue() : 0L;
        }
    }

    private static class ModerationStatisticsImpl implements ModerationStatistics {
        private final java.util.Map<String, Object> stats;
        private final java.util.Map<String, Object> riskStats;

        public ModerationStatisticsImpl(java.util.Map<String, Object> stats, java.util.Map<String, Object> riskStats) {
            this.stats = stats;
            this.riskStats = riskStats;
        }

        @Override
        public long getTotalRecords() { return getLongValue(stats, "totalRecords"); }
        @Override
        public long getApprovedCount() { return getLongValue(stats, "approvedCount"); }
        @Override
        public long getRejectedCount() { return getLongValue(stats, "rejectedCount"); }
        @Override
        public long getPendingCount() { return getLongValue(stats, "pendingCount"); }
        @Override
        public long getBlockedCount() { return getLongValue(stats, "blockedCount"); }
        @Override
        public double getAverageProcessingTime() { 
            Object value = stats.get("avgProcessingTime");
            return value != null ? ((Number) value).doubleValue() : 0.0;
        }
        @Override
        public long getTodayRecords() { return getLongValue(stats, "todayRecords"); }
        @Override
        public RecordCountByRiskLevel getRiskLevelDistribution() {
            return new RecordCountByRiskLevelImpl(riskStats);
        }

        private long getLongValue(java.util.Map<String, Object> map, String key) {
            Object value = map.get(key);
            return value != null ? ((Number) value).longValue() : 0L;
        }
    }

    private static class ViolationTrendImpl implements ViolationTrend {
        private final java.util.Map<String, Object> data;

        public ViolationTrendImpl(java.util.Map<String, Object> data) {
            this.data = data;
        }

        @Override
        public long getTimestamp() { 
            Object value = data.get("timestamp");
            return value != null ? ((Number) value).longValue() : 0L;
        }
        @Override
        public String getDate() { return (String) data.get("date"); }
        @Override
        public long getViolationCount() {
            Object value = data.get("violationCount");
            return value != null ? ((Number) value).longValue() : 0L;
        }
        @Override
        public String getTopViolationType() { return (String) data.get("topViolationType"); }
    }

    // ====================== 审核策略管理 ======================

    @Override
    public ModerationPolicy saveModerationPolicy(ModerationPolicy policy) {
        ModerationPolicyDO policyDO = moderationConvertor.toPolicyDO(policy);
        if (policyDO.getId() == null) {
            policyMapper.insert(policyDO);
        } else {
            policyMapper.updateById(policyDO);
        }
        return moderationConvertor.toPolicyDomain(policyDO);
    }

    @Override
    public Optional<ModerationPolicy> findPolicyById(Long id) {
        ModerationPolicyDO policyDO = policyMapper.selectById(id);
        return Optional.ofNullable(moderationConvertor.toPolicyDomain(policyDO));
    }

    @Override
    public Optional<ModerationPolicy> findPolicyByCode(String code) {
        ModerationPolicyDO policyDO = policyMapper.findByCode(code);
        return Optional.ofNullable(moderationConvertor.toPolicyDomain(policyDO));
    }

    @Override
    public List<ModerationPolicy> findPoliciesByScenario(String scenario) {
        List<ModerationPolicyDO> policyDOs = policyMapper.findByScenarioOrderByPriority(scenario);
        return policyDOs.stream()
                .map(moderationConvertor::toPolicyDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ModerationPolicy> findPoliciesByType(String policyType) {
        List<ModerationPolicyDO> policyDOs = policyMapper.findByPolicyType(policyType);
        return policyDOs.stream()
                .map(moderationConvertor::toPolicyDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ModerationPolicy> findAllActivePolicies() {
        List<ModerationPolicyDO> policyDOs = policyMapper.findAllActiveOrderByPriority();
        return policyDOs.stream()
                .map(moderationConvertor::toPolicyDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ModerationPolicy> findPoliciesByTemplateId(Long templateId) {
        List<ModerationPolicyDO> policyDOs = policyMapper.findByTemplateId(templateId);
        return policyDOs.stream()
                .map(moderationConvertor::toPolicyDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean deleteModerationPolicy(Long id) {
        return policyMapper.deleteById(id) > 0;
    }

    // ====================== 审核维度管理 ======================

    @Override
    public ModerationDimension saveModerationDimension(ModerationDimension dimension) {
        ModerationDimensionDO dimensionDO = moderationConvertor.toDimensionDO(dimension);
        if (dimensionDO.getId() == null) {
            dimensionMapper.insert(dimensionDO);
        } else {
            dimensionMapper.updateById(dimensionDO);
        }
        return moderationConvertor.toDimensionDomain(dimensionDO);
    }

    @Override
    public Optional<ModerationDimension> findDimensionById(Long id) {
        ModerationDimensionDO dimensionDO = dimensionMapper.selectById(id);
        return Optional.ofNullable(moderationConvertor.toDimensionDomain(dimensionDO));
    }

    @Override
    public Optional<ModerationDimension> findDimensionByCode(String code) {
        ModerationDimensionDO dimensionDO = dimensionMapper.findByCode(code);
        return Optional.ofNullable(moderationConvertor.toDimensionDomain(dimensionDO));
    }

    @Override
    public List<ModerationDimension> findDimensionsByCategory(String category) {
        List<ModerationDimensionDO> dimensionDOs = dimensionMapper.findByCategoryOrderBySortOrder(category);
        return dimensionDOs.stream()
                .map(moderationConvertor::toDimensionDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ModerationDimension> findDimensionsBySeverityLevel(SeverityLevel severityLevel) {
        List<ModerationDimensionDO> dimensionDOs = dimensionMapper.findBySeverityLevel(severityLevel.getCode());
        return dimensionDOs.stream()
                .map(moderationConvertor::toDimensionDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ModerationDimension> findAllActiveDimensions() {
        List<ModerationDimensionDO> dimensionDOs = dimensionMapper.findAllActiveOrderBySortOrder();
        return dimensionDOs.stream()
                .map(moderationConvertor::toDimensionDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ModerationDimension> findDimensionsByCategoryId(Long categoryId) {
        List<ModerationDimensionDO> dimensionDOs = dimensionMapper.findByCategoryId(categoryId);
        return dimensionDOs.stream()
                .map(moderationConvertor::toDimensionDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ModerationDimension> findDimensionsByConfidenceRange(Double minThreshold, Double maxThreshold) {
        List<ModerationDimensionDO> dimensionDOs = dimensionMapper.findByConfidenceThresholdRange(minThreshold, maxThreshold);
        return dimensionDOs.stream()
                .map(moderationConvertor::toDimensionDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean deleteModerationDimension(Long id) {
        return dimensionMapper.deleteById(id) > 0;
    }

    // ====================== 策略维度关联管理 ======================

    @Override
    public List<ModerationDimension> findDimensionsByPolicyId(Long policyId) {
        List<ModerationPolicyDimensionDO> relationDOs = policyDimensionMapper.findByPolicyIdWithDimensionInfo(policyId);
        return relationDOs.stream()
                .map(relationDO -> {
                    // 这里需要从关联表中构造维度对象
                    // 实际实现中可能需要优化查询以避免N+1问题
                    ModerationDimensionDO dimensionDO = dimensionMapper.selectById(relationDO.getDimensionId());
                    return moderationConvertor.toDimensionDomain(dimensionDO);
                })
                .filter(dimension -> dimension != null)
                .collect(Collectors.toList());
    }

    @Override
    public List<ModerationPolicy> findPoliciesByDimensionId(Long dimensionId) {
        List<ModerationPolicyDimensionDO> relationDOs = policyDimensionMapper.findByDimensionIdWithPolicyInfo(dimensionId);
        return relationDOs.stream()
                .map(relationDO -> {
                    ModerationPolicyDO policyDO = policyMapper.selectById(relationDO.getPolicyId());
                    return moderationConvertor.toPolicyDomain(policyDO);
                })
                .filter(policy -> policy != null)
                .collect(Collectors.toList());
    }

    @Override
    public boolean savePolicyDimensionRelation(Long policyId, Long dimensionId, Boolean isActive, 
                                             Double weight, Double customThreshold, String customAction, String updatedBy) {
        ModerationPolicyDimensionDO existing = policyDimensionMapper.findByPolicyIdAndDimensionId(policyId, dimensionId);
        
        if (existing != null) {
            // 更新现有关联
            existing.setIsActive(isActive);
            existing.setWeight(weight != null ? new BigDecimal(weight) : null);
            existing.setCustomThreshold(customThreshold != null ? new BigDecimal(customThreshold) : null);
            existing.setCustomAction(customAction);
            existing.setUpdatedBy(updatedBy);
            existing.setUpdatedAt(System.currentTimeMillis());
            return policyDimensionMapper.updateById(existing) > 0;
        } else {
            // 创建新关联
            ModerationPolicyDimensionDO newRelation = ModerationPolicyDimensionDO.builder()
                    .policyId(policyId)
                    .dimensionId(dimensionId)
                    .isActive(isActive)
                    .weight(weight != null ? new BigDecimal(weight) : BigDecimal.ONE)
                    .customThreshold(customThreshold != null ? new BigDecimal(customThreshold) : null)
                    .customAction(customAction)
                    .createdBy(updatedBy)
                    .updatedBy(updatedBy)
                    .createdAt(System.currentTimeMillis())
                    .updatedAt(System.currentTimeMillis())
                    .build();
            return policyDimensionMapper.insert(newRelation) > 0;
        }
    }

    @Override
    public boolean deletePolicyDimensionRelation(Long policyId, Long dimensionId) {
        ModerationPolicyDimensionDO existing = policyDimensionMapper.findByPolicyIdAndDimensionId(policyId, dimensionId);
        if (existing != null) {
            return policyDimensionMapper.deleteById(existing.getId()) > 0;
        }
        return false;
    }

    @Override
    public boolean batchUpdatePolicyDimensionStatus(Long policyId, List<Long> dimensionIds, Boolean isActive, String updatedBy) {
        if (dimensionIds == null || dimensionIds.isEmpty()) {
            return false;
        }
        
        String dimensionIdsStr = dimensionIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        
        return policyDimensionMapper.batchUpdateActiveStatus(policyId, dimensionIdsStr, isActive, updatedBy, System.currentTimeMillis()) > 0;
    }

    // ====================== 策略模板管理 ======================

    @Override
    public ModerationPolicyTemplate saveModerationPolicyTemplate(ModerationPolicyTemplate template) {
        ModerationPolicyTemplateDO templateDO = moderationConvertor.toTemplateDO(template);
        if (templateDO.getId() == null) {
            templateMapper.insert(templateDO);
        } else {
            templateMapper.updateById(templateDO);
        }
        return moderationConvertor.toTemplateDomain(templateDO);
    }

    @Override
    public Optional<ModerationPolicyTemplate> findTemplateById(Long id) {
        ModerationPolicyTemplateDO templateDO = templateMapper.selectById(id);
        return Optional.ofNullable(moderationConvertor.toTemplateDomain(templateDO));
    }

    @Override
    public Optional<ModerationPolicyTemplate> findTemplateByCode(String code) {
        ModerationPolicyTemplateDO templateDO = templateMapper.findByCode(code);
        return Optional.ofNullable(moderationConvertor.toTemplateDomain(templateDO));
    }

    @Override
    public Optional<ModerationPolicyTemplate> findTemplateByCodeAndVersion(String code, String version) {
        ModerationPolicyTemplateDO templateDO = templateMapper.findByCodeAndVersion(code, version);
        return Optional.ofNullable(moderationConvertor.toTemplateDomain(templateDO));
    }

    @Override
    public List<ModerationPolicyTemplate> findTemplatesByType(String templateType) {
        List<ModerationPolicyTemplateDO> templateDOs = templateMapper.findByTemplateType(templateType);
        return templateDOs.stream()
                .map(moderationConvertor::toTemplateDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ModerationPolicyTemplate> findTemplatesByLanguage(String language) {
        List<ModerationPolicyTemplateDO> templateDOs = templateMapper.findByLanguage(language);
        return templateDOs.stream()
                .map(moderationConvertor::toTemplateDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ModerationPolicyTemplate> findAllActiveTemplates() {
        List<ModerationPolicyTemplateDO> templateDOs = templateMapper.findAllActiveOrderByTypeAndVersion();
        return templateDOs.stream()
                .map(moderationConvertor::toTemplateDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<ModerationPolicyTemplate> findAllTemplateVersionsByCode(String code) {
        List<ModerationPolicyTemplateDO> templateDOs = templateMapper.findAllVersionsByCode(code);
        return templateDOs.stream()
                .map(moderationConvertor::toTemplateDomain)
                .collect(Collectors.toList());
    }

    @Override
    public String findLatestTemplateVersionByCode(String code) {
        return templateMapper.findLatestVersionByCode(code);
    }

    @Override
    public Optional<ModerationPolicyTemplate> findTemplateByTypeAndLanguage(String templateType, String language) {
        ModerationPolicyTemplateDO templateDO = templateMapper.findByTemplateTypeAndLanguage(templateType, language);
        return Optional.ofNullable(moderationConvertor.toTemplateDomain(templateDO));
    }

    @Override
    public boolean deleteModerationPolicyTemplate(Long id) {
        return templateMapper.deleteById(id) > 0;
    }
}