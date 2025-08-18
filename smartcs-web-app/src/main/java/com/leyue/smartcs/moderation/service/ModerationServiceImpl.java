package com.leyue.smartcs.moderation.service;

import com.alibaba.cola.dto.PageResponse;
import com.leyue.smartcs.api.ModerationService;
import com.leyue.smartcs.dto.moderation.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 审核服务实现类
 * 提供内容审核相关的业务服务
 *
 * @author Claude
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ModerationServiceImpl implements ModerationService {

    private final LangChain4jModerationService langChain4jModerationService;

    @Override
    public List<ModerationCategoryDTO> getCategoryTree() {
        // 模拟返回违规分类树
        return Arrays.asList(
            ModerationCategoryDTO.builder()
                .id(1L)
                .name("仇恨言论")
                .code("HATE_SPEECH")
                .severityLevel("HIGH")
                .actionType("BLOCK")
                .enabled(true)
                .children(Arrays.asList(
                    ModerationCategoryDTO.builder()
                        .id(2L)
                        .parentId(1L)
                        .name("种族歧视")
                        .code("RACIAL_DISCRIMINATION")
                        .severityLevel("CRITICAL")
                        .actionType("ESCALATE")
                        .enabled(true)
                        .build()
                ))
                .build()
        );
    }

    @Override
    public PageResponse<ModerationCategoryDTO> getCategoriesPage(ModerationCategoryPageQry qry) {
        List<ModerationCategoryDTO> categories = getCategoryTree();
        return PageResponse.of(categories, categories.size(), 20, 1);
    }

    @Override
    public ModerationCategoryDTO getCategoryDetail(Long categoryId) {
        return ModerationCategoryDTO.builder()
            .id(categoryId)
            .name("测试分类")
            .code("TEST_CATEGORY")
            .description("测试用违规分类")
            .severityLevel("MEDIUM")
            .actionType("REVIEW")
            .enabled(true)
            .createTime(System.currentTimeMillis())
            .build();
    }

    @Override
    public ModerationCategoryDTO createCategory(ModerationCategoryCreateCmd cmd) {
        return ModerationCategoryDTO.builder()
            .id(System.currentTimeMillis())
            .name(cmd.getName())
            .code(cmd.getCode())
            .description(cmd.getDescription())
            .severityLevel(cmd.getSeverityLevel())
            .actionType(cmd.getActionType())
            .enabled(cmd.getEnabled())
            .createTime(System.currentTimeMillis())
            .build();
    }

    @Override
    public ModerationCategoryDTO updateCategory(ModerationCategoryUpdateCmd cmd) {
        return ModerationCategoryDTO.builder()
            .id(cmd.getCategoryId())
            .name(cmd.getName())
            .code(cmd.getCode())
            .description(cmd.getDescription())
            .severityLevel(cmd.getSeverityLevel())
            .actionType(cmd.getActionType())
            .enabled(cmd.getEnabled())
            .updateTime(System.currentTimeMillis())
            .build();
    }

    @Override
    public void deleteCategory(Long categoryId) {
        log.info("Deleting category: {}", categoryId);
    }

    @Override
    public void deleteCategoriesBatch(List<Long> categoryIds) {
        log.info("Batch deleting categories: {}", categoryIds);
    }

    @Override
    public PageResponse<ModerationRecordDTO> getRecordsPage(ModerationRecordPageQry qry) {
        List<ModerationRecordDTO> records = Arrays.asList(
            ModerationRecordDTO.builder()
                .id("record_001")
                .originalContent("这是一条测试内容")
                .contentType("MESSAGE")
                .source("CHAT")
                .userId("user_001")
                .riskLevel("LOW")
                .status("APPROVED")
                .result("APPROVED")
                .reviewType("AI")
                .reviewer("AI系统")
                .createTime(System.currentTimeMillis())
                .build()
        );
        return PageResponse.of(records, records.size(), 20, 1);
    }

    @Override
    public ModerationRecordDTO getRecordDetail(String recordId) {
        return ModerationRecordDTO.builder()
            .id(recordId)
            .originalContent("详细的测试内容")
            .contentType("MESSAGE")
            .source("CHAT")
            .userId("user_001")
            .riskLevel("LOW")
            .status("APPROVED")
            .result("APPROVED")
            .reviewType("AI")
            .reviewer("AI系统")
            .reviewNotes("内容正常，无违规")
            .createTime(System.currentTimeMillis())
            .build();
    }

    @Override
    public void batchReviewRecords(ModerationBatchReviewCmd cmd) {
        log.info("Batch reviewing records: {}", cmd.getRecordIds());
    }

    @Override
    public void reviewRecord(ModerationReviewCmd cmd) {
        log.info("Reviewing record: {}", cmd.getRecordId());
    }

    @Override
    public String exportRecords(ModerationRecordExportCmd cmd) {
        return "export_file_" + System.currentTimeMillis() + ".xlsx";
    }

    @Override
    public PageResponse<ModerationKeywordRuleDTO> getKeywordRulesPage(ModerationKeywordRulePageQry qry) {
        List<ModerationKeywordRuleDTO> rules = Arrays.asList(
            ModerationKeywordRuleDTO.builder()
                .id(1L)
                .name("测试规则")
                .keyword("违禁词")
                .matchType("EXACT")
                .categoryCode("HATE_SPEECH")
                .categoryName("仇恨言论")
                .actionType("BLOCK")
                .enabled(true)
                .createTime(System.currentTimeMillis())
                .build()
        );
        return PageResponse.of(rules, rules.size(), 20, 1);
    }

    @Override
    public ModerationKeywordRuleDTO getKeywordRuleDetail(Long ruleId) {
        return ModerationKeywordRuleDTO.builder()
            .id(ruleId)
            .name("测试规则")
            .keyword("违禁词")
            .matchType("EXACT")
            .categoryCode("HATE_SPEECH")
            .categoryName("仇恨言论")
            .actionType("BLOCK")
            .enabled(true)
            .createTime(System.currentTimeMillis())
            .build();
    }

    @Override
    public ModerationKeywordRuleDTO createKeywordRule(ModerationKeywordRuleCreateCmd cmd) {
        return ModerationKeywordRuleDTO.builder()
            .id(System.currentTimeMillis())
            .name(cmd.getName())
            .keyword(cmd.getKeyword())
            .matchType(cmd.getMatchType())
            .categoryCode(cmd.getCategoryCode())
            .actionType(cmd.getActionType())
            .enabled(cmd.getEnabled())
            .createTime(System.currentTimeMillis())
            .build();
    }

    @Override
    public ModerationKeywordRuleDTO updateKeywordRule(ModerationKeywordRuleUpdateCmd cmd) {
        return ModerationKeywordRuleDTO.builder()
            .id(cmd.getRuleId())
            .name(cmd.getName())
            .keyword(cmd.getKeyword())
            .matchType(cmd.getMatchType())
            .categoryCode(cmd.getCategoryCode())
            .actionType(cmd.getActionType())
            .enabled(cmd.getEnabled())
            .updateTime(System.currentTimeMillis())
            .build();
    }

    @Override
    public void deleteKeywordRule(Long ruleId) {
        log.info("Deleting keyword rule: {}", ruleId);
    }

    @Override
    public void deleteKeywordRulesBatch(List<Long> ruleIds) {
        log.info("Batch deleting keyword rules: {}", ruleIds);
    }

    @Override
    public void toggleKeywordRule(Long ruleId) {
        log.info("Toggling keyword rule: {}", ruleId);
    }

    @Override
    public ModerationTestResultDTO testKeywordRule(ModerationKeywordTestCmd cmd) {
        ModerationTestResultDTO result = new ModerationTestResultDTO();
        result.setContent(cmd.getContent());
        result.setResult("SAFE");
        result.setRiskLevel("LOW");
        result.setConfidence(0.95);
        result.setProcessingTime(50L);
        return result;
    }

    @Override
    public ModerationConfigDTO getConfig() {
        ModerationConfigDTO config = new ModerationConfigDTO();
        config.setAiModerationEnabled(true);
        config.setKeywordModerationEnabled(true);
        config.setDefaultAction("REVIEW");
        config.setAiConfidenceThreshold(0.8);
        config.setMaxProcessingTime(5000);
        config.setUpdateTime(System.currentTimeMillis());
        return config;
    }

    @Override
    public void updateConfig(ModerationConfigUpdateCmd cmd) {
        log.info("Updating moderation config");
    }

    @Override
    public void resetConfig() {
        log.info("Resetting moderation config to defaults");
    }

    @Override
    public ModerationStatisticsDTO getStatisticsOverview(String timeRange) {
        ModerationStatisticsDTO stats = new ModerationStatisticsDTO();
        stats.setTotalRecords(1000L);
        stats.setTodayRecords(50L);
        stats.setApprovedRecords(800L);
        stats.setRejectedRecords(150L);
        stats.setPendingRecords(50L);
        stats.setApprovalRate(0.8);
        stats.setRejectionRate(0.15);
        stats.setAvgProcessingTime(250L);
        return stats;
    }

    @Override
    public ModerationTrendsDTO getStatisticsTrends(ModerationStatisticsTrendsQry qry) {
        ModerationTrendsDTO trends = new ModerationTrendsDTO();
        trends.setTotalTrend(new ArrayList<>());
        trends.setApprovedTrend(new ArrayList<>());
        trends.setRejectedTrend(new ArrayList<>());
        trends.setProcessingTimeTrend(new ArrayList<>());
        return trends;
    }

    @Override
    public List<ModerationCategoryStatDTO> getCategoryStatistics(String timeRange) {
        ModerationCategoryStatDTO stat = new ModerationCategoryStatDTO();
        stat.setCategoryCode("HATE_SPEECH");
        stat.setCategoryName("仇恨言论");
        stat.setViolationCount(100L);
        stat.setPercentage(0.6);
        stat.setSeverityLevel("HIGH");
        stat.setAvgProcessingTime(300L);
        return Arrays.asList(stat);
    }

    @Override
    public List<ModerationReviewerStatDTO> getReviewerStatistics(ModerationReviewerStatQry qry) {
        ModerationReviewerStatDTO stat = new ModerationReviewerStatDTO();
        stat.setReviewerId("reviewer_001");
        stat.setReviewerName("审核员A");
        stat.setTotalReviews(500L);
        stat.setApprovedCount(400L);
        stat.setRejectedCount(100L);
        stat.setApprovalRate(0.8);
        stat.setAvgProcessingTime(180L);
        stat.setEfficiency(0.9);
        return Arrays.asList(stat);
    }

    @Override
    public ModerationTestResultDTO testContent(ModerationTestCmd cmd) {
        // 模拟返回测试结果
        ModerationDTOs.ModerationTestResultDTO dto = ModerationDTOs.ModerationTestResultDTO.builder()
                .content(cmd.getContent())
                .result("APPROVED")
                .riskLevel("LOW")
                .confidence(0.95)
                .processingTime(150L)
                .build();
        ModerationTestResultDTO result = new ModerationTestResultDTO();
        // 复制属性
        result.setContent(dto.getContent());
        result.setResult(dto.getResult());
        result.setRiskLevel(dto.getRiskLevel());
        result.setConfidence(dto.getConfidence());
        result.setProcessingTime(dto.getProcessingTime());
        return result;
    }

    @Override
    public List<ModerationTestResultDTO> batchTestContent(ModerationBatchTestCmd cmd) {
        List<ModerationTestResultDTO> results = new ArrayList<>();
        for (String content : cmd.getContents()) {
            ModerationDTOs.ModerationTestResultDTO dto = ModerationDTOs.ModerationTestResultDTO.builder()
                    .content(content)
                    .result("APPROVED")
                    .riskLevel("LOW")
                    .confidence(0.90)
                    .processingTime(120L)
                    .build();
            ModerationTestResultDTO result = new ModerationTestResultDTO();
            // 复制属性
            result.setContent(dto.getContent());
            result.setResult(dto.getResult());
            result.setRiskLevel(dto.getRiskLevel());
            result.setConfidence(dto.getConfidence());
            result.setProcessingTime(dto.getProcessingTime());
            results.add(result);
        }
        return results;
    }

    @Override
    public ModerationTestResultDTO moderateContentWithModel(ModerationWithModelRequest request) {
        log.info("开始AI模型审核，modelId: {}, contentLength: {}", request.getModelId(), request.getContent().length());
        
        try {
            // 调用LangChain4j审核服务
            CompletableFuture<LangChain4jModerationService.AiModerationResult> future = 
                    langChain4jModerationService.moderateContent(request.getContent(), request.getModelId());
            
            // 等待结果
            LangChain4jModerationService.AiModerationResult aiResult = future.get();
            
            // 转换为DTO
            ModerationDTOs.ModerationTestResultDTO dto = ModerationDTOs.ModerationTestResultDTO.builder()
                    .content(request.getContent())
                    .result(aiResult.getResult().name())
                    .riskLevel(aiResult.getRiskLevel() != null ? aiResult.getRiskLevel().name() : "LOW")
                    .confidence(aiResult.getConfidence() != null ? aiResult.getConfidence().doubleValue() : 0.0)
                    .processingTime(aiResult.getProcessingTimeMs())
                    .build();
            ModerationTestResultDTO result = new ModerationTestResultDTO();
            // 复制属性
            result.setContent(dto.getContent());
            result.setResult(dto.getResult());
            result.setRiskLevel(dto.getRiskLevel());
            result.setConfidence(dto.getConfidence());
            result.setProcessingTime(dto.getProcessingTime());
            return result;
                    
        } catch (Exception e) {
            log.error("AI模型审核失败，modelId: {}, content: {}", request.getModelId(), request.getContent(), e);
            
            ModerationDTOs.ModerationTestResultDTO dto = ModerationDTOs.ModerationTestResultDTO.builder()
                    .content(request.getContent())
                    .result("NEEDS_REVIEW")
                    .riskLevel("MEDIUM")
                    .confidence(0.0)
                    .processingTime(0L)
                    .build();
            ModerationTestResultDTO result = new ModerationTestResultDTO();
            // 复制属性
            result.setContent(dto.getContent());
            result.setResult(dto.getResult());
            result.setRiskLevel(dto.getRiskLevel());
            result.setConfidence(dto.getConfidence());
            result.setProcessingTime(dto.getProcessingTime());
            return result;
        }
    }

    @Override
    public ModerationTestResultDTO quickModerateWithModel(ModerationWithModelRequest request) {
        log.info("开始快速AI模型预检，modelId: {}, contentLength: {}", request.getModelId(), request.getContent().length());
        
        try {
            // 调用LangChain4j快速审核服务
            CompletableFuture<LangChain4jModerationService.QuickModerationResult> future = 
                    langChain4jModerationService.quickModerate(request.getContent(), request.getModelId());
            
            // 等待结果
            LangChain4jModerationService.QuickModerationResult quickResult = future.get();
            
            // 根据快速审核结果确定最终结果
            String result;
            String riskLevel;
            double confidence;
            
            switch (quickResult.getResult()) {
                case SAFE:
                    result = "APPROVED";
                    riskLevel = "LOW";
                    confidence = 0.9;
                    break;
                case BLOCKED:
                    result = "REJECTED";
                    riskLevel = "HIGH";
                    confidence = 0.8;
                    break;
                case NEEDS_REVIEW:
                case ERROR:
                case TIMEOUT:
                case DISABLED:
                default:
                    result = "NEEDS_REVIEW";
                    riskLevel = "MEDIUM";
                    confidence = 0.6;
                    break;
            }
            
            ModerationDTOs.ModerationTestResultDTO dto = ModerationDTOs.ModerationTestResultDTO.builder()
                    .content(request.getContent())
                    .result(result)
                    .riskLevel(riskLevel)
                    .confidence(confidence)
                    .processingTime(quickResult.getProcessingTimeMs())
                    .build();
            ModerationTestResultDTO finalResult = new ModerationTestResultDTO();
            // 复制属性
            finalResult.setContent(dto.getContent());
            finalResult.setResult(dto.getResult());
            finalResult.setRiskLevel(dto.getRiskLevel());
            finalResult.setConfidence(dto.getConfidence());
            finalResult.setProcessingTime(dto.getProcessingTime());
            return finalResult;
                    
        } catch (Exception e) {
            log.error("快速AI模型预检失败，modelId: {}, content: {}", request.getModelId(), request.getContent(), e);
            
            ModerationDTOs.ModerationTestResultDTO dto = ModerationDTOs.ModerationTestResultDTO.builder()
                    .content(request.getContent())
                    .result("NEEDS_REVIEW")
                    .riskLevel("MEDIUM")
                    .confidence(0.0)
                    .processingTime(0L)
                    .build();
            ModerationTestResultDTO result = new ModerationTestResultDTO();
            // 复制属性
            result.setContent(dto.getContent());
            result.setResult(dto.getResult());
            result.setRiskLevel(dto.getRiskLevel());
            result.setConfidence(dto.getConfidence());
            result.setProcessingTime(dto.getProcessingTime());
            return result;
        }
    }
}