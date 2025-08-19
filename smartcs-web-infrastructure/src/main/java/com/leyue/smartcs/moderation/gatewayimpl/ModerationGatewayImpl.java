package com.leyue.smartcs.moderation.gatewayimpl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leyue.smartcs.domain.moderation.ModerationCategory;
import com.leyue.smartcs.domain.moderation.ModerationRecord;
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
}