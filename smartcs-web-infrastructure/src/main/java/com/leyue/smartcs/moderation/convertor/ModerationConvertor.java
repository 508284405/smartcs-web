package com.leyue.smartcs.moderation.convertor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leyue.smartcs.domain.moderation.*;
import com.leyue.smartcs.domain.moderation.enums.*;
import com.leyue.smartcs.moderation.dataobject.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 内容审核模块转换器
 * 负责Domain对象与DO对象之间的转换
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ModerationConvertor {

    private final ObjectMapper objectMapper;

    // ====================== 分类转换 ======================

    /**
     * 分类Domain转DO
     */
    public ModerationCategoryDO toCategoryDO(ModerationCategory category) {
        if (category == null) {
            return null;
        }

        return ModerationCategoryDO.builder()
                .id(category.getId())
                .parentId(category.getParentId())
                .name(category.getName())
                .code(category.getCode())
                .description(category.getDescription())
                .severityLevel(category.getSeverityLevel() != null ? category.getSeverityLevel().getCode() : null)
                .actionType(category.getActionType() != null ? category.getActionType().getCode() : null)
                .isActive(category.getIsActive() != null && category.getIsActive() ? 1 : 0)
                .sortOrder(category.getSortOrder())
                .createdBy(category.getCreatedBy())
                .updatedBy(category.getUpdatedBy())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }

    /**
     * 分类DO转Domain
     */
    public ModerationCategory toCategoryDomain(ModerationCategoryDO categoryDO) {
        if (categoryDO == null) {
            return null;
        }

        return ModerationCategory.builder()
                .id(categoryDO.getId())
                .parentId(categoryDO.getParentId())
                .name(categoryDO.getName())
                .code(categoryDO.getCode())
                .description(categoryDO.getDescription())
                .severityLevel(parseSeverityLevel(categoryDO.getSeverityLevel()))
                .actionType(parseActionType(categoryDO.getActionType()))
                .isActive(categoryDO.getIsActive() != null && categoryDO.getIsActive() == 1)
                .sortOrder(categoryDO.getSortOrder())
                .createdBy(categoryDO.getCreatedBy())
                .updatedBy(categoryDO.getUpdatedBy())
                .createdAt(categoryDO.getCreatedAt())
                .updatedAt(categoryDO.getUpdatedAt())
                .build();
    }

    /**
     * 构建分类树形结构
     */
    public List<ModerationCategory> buildCategoryTree(List<ModerationCategoryDO> allCategories) {
        Map<Long, ModerationCategory> categoryMap = new HashMap<>();
        List<ModerationCategory> rootCategories = new ArrayList<>();

        // 转换所有分类
        for (ModerationCategoryDO categoryDO : allCategories) {
            ModerationCategory category = toCategoryDomain(categoryDO);
            categoryMap.put(category.getId(), category);
        }

        // 构建树形结构
        for (ModerationCategory category : categoryMap.values()) {
            if (category.getParentId() == null) {
                // 根分类
                rootCategories.add(category);
            } else {
                // 子分类
                ModerationCategory parent = categoryMap.get(category.getParentId());
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(category);
                }
            }
        }

        return rootCategories;
    }

    // ====================== 记录转换 ======================

    /**
     * 记录Domain转DO
     */
    public ModerationRecordDO toRecordDO(ModerationRecord record) {
        if (record == null) {
            return null;
        }

        return ModerationRecordDO.builder()
                .id(record.getId())
                .contentHash(record.getContentHash())
                .originalContent(record.getOriginalContent())
                .contentType(record.getContentType() != null ? record.getContentType().getCode() : null)
                .sourceId(record.getSourceId())
                .sourceType(record.getSourceType() != null ? record.getSourceType().getCode() : null)
                .userId(record.getUserId())
                .sessionId(record.getSessionId())
                .moderationResult(record.getModerationResult() != null ? record.getModerationResult().getCode() : null)
                .riskLevel(record.getRiskLevel() != null ? record.getRiskLevel().getCode() : null)
                .confidenceScore(record.getConfidenceScore())
                .isBlocked(record.getIsBlocked() != null && record.getIsBlocked() ? 1 : 0)
                .violationCategories(serializeViolationCategories(record.getViolationCategories()))
                .aiAnalysisResult(record.getAiAnalysisResult())
                .keywordMatches(serializeKeywordMatches(record.getKeywordMatches()))
                .moderationMethods(record.getModerationMethods())
                .aiModelUsed(record.getAiModelUsed())
                .processingTimeMs(record.getProcessingTimeMs())
                .manualReviewStatus(record.getManualReviewStatus())
                .manualReviewerId(record.getManualReviewerId())
                .manualReviewNotes(record.getManualReviewNotes())
                .manualReviewedAt(record.getManualReviewedAt())
                .actionTaken(record.getActionTaken() != null ? record.getActionTaken().getCode() : null)
                .escalatedTo(record.getEscalatedTo())
                .escalatedAt(record.getEscalatedAt())
                .clientIp(record.getClientIp())
                .userAgent(record.getUserAgent())
                .requestId(record.getRequestId())
                .metadata(record.getMetadata())
                .createdAt(record.getCreatedAt())
                .updatedAt(record.getUpdatedAt())
                .build();
    }

    /**
     * 记录DO转Domain
     */
    public ModerationRecord toRecordDomain(ModerationRecordDO recordDO) {
        if (recordDO == null) {
            return null;
        }

        return ModerationRecord.builder()
                .id(recordDO.getId())
                .contentHash(recordDO.getContentHash())
                .originalContent(recordDO.getOriginalContent())
                .contentType(parseContentType(recordDO.getContentType()))
                .sourceId(recordDO.getSourceId())
                .sourceType(parseSourceType(recordDO.getSourceType()))
                .userId(recordDO.getUserId())
                .sessionId(recordDO.getSessionId())
                .moderationResult(parseModerationResult(recordDO.getModerationResult()))
                .riskLevel(parseSeverityLevel(recordDO.getRiskLevel()))
                .confidenceScore(recordDO.getConfidenceScore())
                .isBlocked(recordDO.getIsBlocked() != null && recordDO.getIsBlocked() == 1)
                .violationCategories(deserializeViolationCategories(recordDO.getViolationCategories()))
                .aiAnalysisResult(recordDO.getAiAnalysisResult())
                .keywordMatches(deserializeKeywordMatches(recordDO.getKeywordMatches()))
                .moderationMethods(recordDO.getModerationMethods())
                .aiModelUsed(recordDO.getAiModelUsed())
                .processingTimeMs(recordDO.getProcessingTimeMs())
                .manualReviewStatus(recordDO.getManualReviewStatus())
                .manualReviewerId(recordDO.getManualReviewerId())
                .manualReviewNotes(recordDO.getManualReviewNotes())
                .manualReviewedAt(recordDO.getManualReviewedAt())
                .actionTaken(parseActionType(recordDO.getActionTaken()))
                .escalatedTo(recordDO.getEscalatedTo())
                .escalatedAt(recordDO.getEscalatedAt())
                .clientIp(recordDO.getClientIp())
                .userAgent(recordDO.getUserAgent())
                .requestId(recordDO.getRequestId())
                .metadata(recordDO.getMetadata())
                .createdAt(recordDO.getCreatedAt())
                .updatedAt(recordDO.getUpdatedAt())
                .build();
    }

    // ====================== 枚举转换辅助方法 ======================

    private SeverityLevel parseSeverityLevel(String code) {
        if (code == null) {
            return null;
        }
        try {
            return SeverityLevel.fromCode(code);
        } catch (Exception e) {
            log.warn("Unknown severity level code: {}", code);
            return SeverityLevel.MEDIUM; // 默认中等风险
        }
    }

    private ActionType parseActionType(String code) {
        if (code == null) {
            return null;
        }
        try {
            return ActionType.fromCode(code);
        } catch (Exception e) {
            log.warn("Unknown action type code: {}", code);
            return ActionType.BLOCK; // 默认阻断
        }
    }

    private ContentType parseContentType(String code) {
        if (code == null) {
            return null;
        }
        try {
            return ContentType.fromCode(code);
        } catch (Exception e) {
            log.warn("Unknown content type code: {}", code);
            return ContentType.MESSAGE; // 默认消息类型
        }
    }

    private SourceType parseSourceType(String code) {
        if (code == null) {
            return null;
        }
        try {
            return SourceType.fromCode(code);
        } catch (Exception e) {
            log.warn("Unknown source type code: {}", code);
            return SourceType.CHAT; // 默认聊天来源
        }
    }

    private ModerationResult parseModerationResult(String code) {
        if (code == null) {
            return null;
        }
        try {
            return ModerationResult.fromCode(code);
        } catch (Exception e) {
            log.warn("Unknown moderation result code: {}", code);
            return ModerationResult.PENDING; // 默认待审核
        }
    }

    // ====================== JSON序列化辅助方法 ======================

    private Object serializeViolationCategories(List<ModerationRecord.ModerationViolation> violations) {
        if (violations == null || violations.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(violations);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize violation categories", e);
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private List<ModerationRecord.ModerationViolation> deserializeViolationCategories(Object violations) {
        if (violations == null) {
            return null;
        }
        try {
            if (violations instanceof String) {
                return objectMapper.readValue((String) violations,
                        objectMapper.getTypeFactory().constructCollectionType(List.class, ModerationRecord.ModerationViolation.class));
            } else if (violations instanceof List) {
                // 如果已经是List类型，可能是MyBatis-Plus的JacksonTypeHandler已经处理过了
                List<?> list = (List<?>) violations;
                return list.stream()
                        .filter(item -> item instanceof Map)
                        .map(item -> convertMapToViolation((Map<String, Object>) item))
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            log.error("Failed to deserialize violation categories: {}", violations, e);
        }
        return null;
    }

    private ModerationRecord.ModerationViolation convertMapToViolation(Map<String, Object> map) {
        return ModerationRecord.ModerationViolation.builder()
                .categoryId(map.get("categoryId") != null ? Long.valueOf(map.get("categoryId").toString()) : null)
                .categoryName((String) map.get("categoryName"))
                .categoryCode((String) map.get("categoryCode"))
                .confidence(map.get("confidence") != null ? new java.math.BigDecimal(map.get("confidence").toString()) : null)
                .triggerRule((String) map.get("triggerRule"))
                .build();
    }

    private Object serializeKeywordMatches(List<String> keywordMatches) {
        if (keywordMatches == null || keywordMatches.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(keywordMatches);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize keyword matches", e);
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private List<String> deserializeKeywordMatches(Object keywordMatches) {
        if (keywordMatches == null) {
            return null;
        }
        try {
            if (keywordMatches instanceof String) {
                return objectMapper.readValue((String) keywordMatches,
                        objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
            } else if (keywordMatches instanceof List) {
                return (List<String>) keywordMatches;
            }
        } catch (Exception e) {
            log.error("Failed to deserialize keyword matches: {}", keywordMatches, e);
        }
        return null;
    }

    // ====================== 策略转换 ======================

    /**
     * 策略Domain转DO
     */
    public ModerationPolicyDO toPolicyDO(ModerationPolicy policy) {
        if (policy == null) {
            return null;
        }

        return ModerationPolicyDO.builder()
                .id(policy.getId())
                .name(policy.getName())
                .code(policy.getCode())
                .description(policy.getDescription())
                .scenario(policy.getScenario())
                .policyType(policy.getPolicyType())
                .defaultRiskLevel(policy.getDefaultRiskLevel() != null ? policy.getDefaultRiskLevel().getCode() : null)
                .defaultAction(policy.getDefaultAction() != null ? policy.getDefaultAction().getCode() : null)
                .isActive(policy.getIsActive() != null && policy.getIsActive() ? true : false)
                .priority(policy.getPriority())
                .configParams(serializeConfigParams(policy.getConfigParams()))
                .templateId(policy.getTemplateId())
                .createdBy(policy.getCreatedBy())
                .updatedBy(policy.getUpdatedBy())
                .createdAt(policy.getCreatedAt())
                .updatedAt(policy.getUpdatedAt())
                .build();
    }

    /**
     * 策略DO转Domain
     */
    public ModerationPolicy toPolicyDomain(ModerationPolicyDO policyDO) {
        if (policyDO == null) {
            return null;
        }

        return ModerationPolicy.builder()
                .id(policyDO.getId())
                .name(policyDO.getName())
                .code(policyDO.getCode())
                .description(policyDO.getDescription())
                .scenario(policyDO.getScenario())
                .policyType(policyDO.getPolicyType())
                .defaultRiskLevel(parseSeverityLevel(policyDO.getDefaultRiskLevel()))
                .defaultAction(parseActionType(policyDO.getDefaultAction()))
                .isActive(policyDO.getIsActive() != null && policyDO.getIsActive())
                .priority(policyDO.getPriority())
                .configParams(deserializeConfigParams(policyDO.getConfigParams()))
                .templateId(policyDO.getTemplateId())
                .createdBy(policyDO.getCreatedBy())
                .updatedBy(policyDO.getUpdatedBy())
                .createdAt(policyDO.getCreatedAt())
                .updatedAt(policyDO.getUpdatedAt())
                .build();
    }

    // ====================== 维度转换 ======================

    /**
     * 维度Domain转DO
     */
    public ModerationDimensionDO toDimensionDO(ModerationDimension dimension) {
        if (dimension == null) {
            return null;
        }

        return ModerationDimensionDO.builder()
                .id(dimension.getId())
                .name(dimension.getName())
                .code(dimension.getCode())
                .description(dimension.getDescription())
                .checkGuideline(dimension.getCheckGuideline())
                .severityLevel(dimension.getSeverityLevel() != null ? dimension.getSeverityLevel().getCode() : null)
                .actionType(dimension.getActionType() != null ? dimension.getActionType().getCode() : null)
                .confidenceThreshold(dimension.getConfidenceThreshold() != null ? 
                    new java.math.BigDecimal(dimension.getConfidenceThreshold()) : null)
                .isActive(dimension.getIsActive() != null && dimension.getIsActive() ? true : false)
                .sortOrder(dimension.getSortOrder())
                .category(dimension.getCategory())
                .configParams(serializeConfigParams(dimension.getConfigParams()))
                .categoryId(dimension.getCategoryId())
                .createdBy(dimension.getCreatedBy())
                .updatedBy(dimension.getUpdatedBy())
                .createdAt(dimension.getCreatedAt())
                .updatedAt(dimension.getUpdatedAt())
                .build();
    }

    /**
     * 维度DO转Domain
     */
    public ModerationDimension toDimensionDomain(ModerationDimensionDO dimensionDO) {
        if (dimensionDO == null) {
            return null;
        }

        return ModerationDimension.builder()
                .id(dimensionDO.getId())
                .name(dimensionDO.getName())
                .code(dimensionDO.getCode())
                .description(dimensionDO.getDescription())
                .checkGuideline(dimensionDO.getCheckGuideline())
                .severityLevel(parseSeverityLevel(dimensionDO.getSeverityLevel()))
                .actionType(parseActionType(dimensionDO.getActionType()))
                .confidenceThreshold(dimensionDO.getConfidenceThreshold() != null ? 
                    dimensionDO.getConfidenceThreshold().doubleValue() : null)
                .isActive(dimensionDO.getIsActive() != null && dimensionDO.getIsActive())
                .sortOrder(dimensionDO.getSortOrder())
                .category(dimensionDO.getCategory())
                .configParams(deserializeConfigParams(dimensionDO.getConfigParams()))
                .categoryId(dimensionDO.getCategoryId())
                .createdBy(dimensionDO.getCreatedBy())
                .updatedBy(dimensionDO.getUpdatedBy())
                .createdAt(dimensionDO.getCreatedAt())
                .updatedAt(dimensionDO.getUpdatedAt())
                .build();
    }

    // ====================== 模板转换 ======================

    /**
     * 模板Domain转DO
     */
    public ModerationPolicyTemplateDO toTemplateDO(ModerationPolicyTemplate template) {
        if (template == null) {
            return null;
        }

        return ModerationPolicyTemplateDO.builder()
                .id(template.getId())
                .name(template.getName())
                .code(template.getCode())
                .description(template.getDescription())
                .templateType(template.getTemplateType())
                .promptTemplate(template.getPromptTemplate())
                .dimensionTemplate(template.getDimensionTemplate())
                .responseTemplate(template.getResponseTemplate())
                .language(template.getLanguage())
                .variables(serializeConfigParams(template.getVariables()))
                .defaultValues(serializeConfigParams(template.getDefaultValues()))
                .isActive(template.getIsActive() != null && template.getIsActive() ? true : false)
                .version(template.getVersion())
                .createdBy(template.getCreatedBy())
                .updatedBy(template.getUpdatedBy())
                .createdAt(template.getCreatedAt())
                .updatedAt(template.getUpdatedAt())
                .build();
    }

    /**
     * 模板DO转Domain
     */
    public ModerationPolicyTemplate toTemplateDomain(ModerationPolicyTemplateDO templateDO) {
        if (templateDO == null) {
            return null;
        }

        return ModerationPolicyTemplate.builder()
                .id(templateDO.getId())
                .name(templateDO.getName())
                .code(templateDO.getCode())
                .description(templateDO.getDescription())
                .templateType(templateDO.getTemplateType())
                .promptTemplate(templateDO.getPromptTemplate())
                .dimensionTemplate(templateDO.getDimensionTemplate())
                .responseTemplate(templateDO.getResponseTemplate())
                .language(templateDO.getLanguage())
                .variables(deserializeConfigParams(templateDO.getVariables()))
                .defaultValues(deserializeConfigParams(templateDO.getDefaultValues()))
                .isActive(templateDO.getIsActive() != null && templateDO.getIsActive())
                .version(templateDO.getVersion())
                .createdBy(templateDO.getCreatedBy())
                .updatedBy(templateDO.getUpdatedBy())
                .createdAt(templateDO.getCreatedAt())
                .updatedAt(templateDO.getUpdatedAt())
                .build();
    }

    // ====================== 配置参数序列化辅助方法 ======================

    private String serializeConfigParams(Map<String, Object> configParams) {
        if (configParams == null || configParams.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(configParams);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize config params", e);
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> deserializeConfigParams(String configParams) {
        if (configParams == null || configParams.trim().isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(configParams, Map.class);
        } catch (Exception e) {
            log.error("Failed to deserialize config params: {}", configParams, e);
            return null;
        }
    }
}