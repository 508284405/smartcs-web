package com.leyue.smartcs.web.moderation;

import com.leyue.smartcs.api.ModerationService;
import com.leyue.smartcs.dto.moderation.*;
import com.alibaba.cola.dto.SingleResponse;
import com.alibaba.cola.dto.PageResponse;
import com.alibaba.cola.dto.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * 审核管理控制器
 * 提供内容审核相关的管理接口
 *
 * @author Claude
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/moderation")
@RequiredArgsConstructor
public class AdminModerationController {

    private final ModerationService moderationService;

    // ===== 违规分类管理 =====

    /**
     * 获取违规分类树
     */
    @GetMapping("/categories/tree")
    public SingleResponse<List<ModerationCategoryDTO>> getCategoryTree() {
        return SingleResponse.of(moderationService.getCategoryTree());
    }

    /**
     * 分页查询违规分类
     */
    @GetMapping("/categories")
    public PageResponse<ModerationCategoryDTO> getCategories(
            @Valid ModerationCategoryPageQry qry) {
        return moderationService.getCategoriesPage(qry);
    }

    /**
     * 获取分类详情
     */
    @GetMapping("/categories/{categoryId}")
    public SingleResponse<ModerationCategoryDTO> getCategoryDetail(
            @PathVariable Long categoryId) {
        return SingleResponse.of(moderationService.getCategoryDetail(categoryId));
    }

    /**
     * 创建违规分类
     */
    @PostMapping("/categories")
    public SingleResponse<ModerationCategoryDTO> createCategory(
            @Valid @RequestBody ModerationCategoryCreateCmd cmd) {
        return SingleResponse.of(moderationService.createCategory(cmd));
    }

    /**
     * 更新违规分类
     */
    @PutMapping("/categories/{categoryId}")
    public SingleResponse<ModerationCategoryDTO> updateCategory(
            @PathVariable Long categoryId,
            @Valid @RequestBody ModerationCategoryUpdateCmd cmd) {
        cmd.setCategoryId(categoryId);
        return SingleResponse.of(moderationService.updateCategory(cmd));
    }

    /**
     * 删除违规分类
     */
    @DeleteMapping("/categories/{categoryId}")
    public Response deleteCategory(@PathVariable Long categoryId) {
        moderationService.deleteCategory(categoryId);
        return Response.buildSuccess();
    }

    /**
     * 批量删除违规分类
     */
    @DeleteMapping("/categories/batch")
    public Response deleteCategoriesBatch(@RequestBody List<Long> categoryIds) {
        moderationService.deleteCategoriesBatch(categoryIds);
        return Response.buildSuccess();
    }

    // ===== 审核记录管理 =====

    /**
     * 分页查询审核记录
     */
    @GetMapping("/records")
    public PageResponse<ModerationRecordDTO> getRecords(
            @Valid ModerationRecordPageQry qry) {
        return moderationService.getRecordsPage(qry);
    }

    /**
     * 获取审核记录详情
     */
    @GetMapping("/records/{recordId}")
    public SingleResponse<ModerationRecordDTO> getRecordDetail(
            @PathVariable String recordId) {
        return SingleResponse.of(moderationService.getRecordDetail(recordId));
    }

    /**
     * 批量审核记录
     */
    @PostMapping("/records/batch-review")
    public Response batchReviewRecords(
            @Valid @RequestBody ModerationBatchReviewCmd cmd) {
        moderationService.batchReviewRecords(cmd);
        return Response.buildSuccess();
    }

    /**
     * 人工审核记录
     */
    @PostMapping("/records/{recordId}/review")
    public Response reviewRecord(
            @PathVariable String recordId,
            @Valid @RequestBody ModerationReviewCmd cmd) {
        cmd.setRecordId(recordId);
        moderationService.reviewRecord(cmd);
        return Response.buildSuccess();
    }

    /**
     * 导出审核记录
     */
    @PostMapping("/records/export")
    public SingleResponse<String> exportRecords(
            @Valid @RequestBody ModerationRecordExportCmd cmd) {
        return SingleResponse.of(moderationService.exportRecords(cmd));
    }

    /**
     * 获取待审核记录列表
     */
    @GetMapping("/records/pending")
    public PageResponse<ModerationRecordDTO> getPendingReviews(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        ModerationRecordPageQry qry = new ModerationRecordPageQry();
        qry.setPageIndex(pageNum);
        qry.setPageSize(pageSize);
        qry.setStatus("PENDING_REVIEW");
        return moderationService.getRecordsPage(qry);
    }

    // ===== 关键词规则管理 =====

    /**
     * 分页查询关键词规则
     */
    @GetMapping("/keyword-rules")
    public PageResponse<ModerationKeywordRuleDTO> getKeywordRules(
            @Valid ModerationKeywordRulePageQry qry) {
        return moderationService.getKeywordRulesPage(qry);
    }

    /**
     * 获取关键词规则详情
     */
    @GetMapping("/keyword-rules/{ruleId}")
    public SingleResponse<ModerationKeywordRuleDTO> getKeywordRuleDetail(
            @PathVariable Long ruleId) {
        return SingleResponse.of(moderationService.getKeywordRuleDetail(ruleId));
    }

    /**
     * 创建关键词规则
     */
    @PostMapping("/keyword-rules")
    public SingleResponse<ModerationKeywordRuleDTO> createKeywordRule(
            @Valid @RequestBody ModerationKeywordRuleCreateCmd cmd) {
        return SingleResponse.of(moderationService.createKeywordRule(cmd));
    }

    /**
     * 更新关键词规则
     */
    @PutMapping("/keyword-rules/{ruleId}")
    public SingleResponse<ModerationKeywordRuleDTO> updateKeywordRule(
            @PathVariable Long ruleId,
            @Valid @RequestBody ModerationKeywordRuleUpdateCmd cmd) {
        cmd.setRuleId(ruleId);
        return SingleResponse.of(moderationService.updateKeywordRule(cmd));
    }

    /**
     * 删除关键词规则
     */
    @DeleteMapping("/keyword-rules/{ruleId}")
    public Response deleteKeywordRule(@PathVariable Long ruleId) {
        moderationService.deleteKeywordRule(ruleId);
        return Response.buildSuccess();
    }

    /**
     * 批量删除关键词规则
     */
    @DeleteMapping("/keyword-rules/batch")
    public Response deleteKeywordRulesBatch(@RequestBody List<Long> ruleIds) {
        moderationService.deleteKeywordRulesBatch(ruleIds);
        return Response.buildSuccess();
    }

    /**
     * 启用/禁用关键词规则
     */
    @PutMapping("/keyword-rules/{ruleId}/toggle")
    public Response toggleKeywordRule(@PathVariable Long ruleId) {
        moderationService.toggleKeywordRule(ruleId);
        return Response.buildSuccess();
    }

    /**
     * 测试关键词规则
     */
    @PostMapping("/keyword-rules/test")
    public SingleResponse<ModerationTestResultDTO> testKeywordRule(
            @Valid @RequestBody ModerationKeywordTestCmd cmd) {
        return SingleResponse.of(moderationService.testKeywordRule(cmd));
    }

    // ===== 审核配置管理 =====

    /**
     * 获取审核配置
     */
    @GetMapping("/config")
    public SingleResponse<ModerationConfigDTO> getConfig() {
        return SingleResponse.of(moderationService.getConfig());
    }

    /**
     * 更新审核配置
     */
    @PutMapping("/config")
    public Response updateConfig(
            @Valid @RequestBody ModerationConfigUpdateCmd cmd) {
        moderationService.updateConfig(cmd);
        return Response.buildSuccess();
    }

    /**
     * 重置审核配置为默认值
     */
    @PostMapping("/config/reset")
    public Response resetConfig() {
        moderationService.resetConfig();
        return Response.buildSuccess();
    }

    // ===== 审核统计 =====

    /**
     * 获取审核统计概览
     */
    @GetMapping("/statistics/overview")
    public SingleResponse<ModerationStatisticsDTO> getStatisticsOverview(
            @RequestParam(required = false) String timeRange) {
        return SingleResponse.of(moderationService.getStatisticsOverview(timeRange));
    }

    /**
     * 获取审核趋势统计
     */
    @GetMapping("/statistics/trends")
    public SingleResponse<ModerationTrendsDTO> getStatisticsTrends(
            @Valid ModerationStatisticsTrendsQry qry) {
        return SingleResponse.of(moderationService.getStatisticsTrends(qry));
    }

    /**
     * 获取违规分类统计
     */
    @GetMapping("/statistics/categories")
    public SingleResponse<List<ModerationCategoryStatDTO>> getCategoryStatistics(
            @RequestParam(required = false) String timeRange) {
        return SingleResponse.of(moderationService.getCategoryStatistics(timeRange));
    }

    /**
     * 获取审核员绩效统计
     */
    @GetMapping("/statistics/reviewers")
    public SingleResponse<List<ModerationReviewerStatDTO>> getReviewerStatistics(
            @Valid ModerationReviewerStatQry qry) {
        return SingleResponse.of(moderationService.getReviewerStatistics(qry));
    }

    // ===== AI模型审核接口 =====

    /**
     * 使用指定AI模型进行内容审核
     */
    @PostMapping("/content/ai")
    public SingleResponse<ModerationTestResultDTO> moderateContentWithModel(
            @Valid @RequestBody ModerationWithModelRequest request) {
        log.info("收到AI模型审核请求: modelId={}, contentLength={}", 
                request.getModelId(), request.getContent().length());
        return SingleResponse.of(moderationService.moderateContentWithModel(request));
    }

    /**
     * 使用指定AI模型进行快速内容预检
     */
    @PostMapping("/content/ai/quick")
    public SingleResponse<ModerationTestResultDTO> quickModerateWithModel(
            @Valid @RequestBody ModerationWithModelRequest request) {
        log.info("收到快速AI模型预检请求: modelId={}, contentLength={}", 
                request.getModelId(), request.getContent().length());
        return SingleResponse.of(moderationService.quickModerateWithModel(request));
    }

    // ===== 内容测试 =====

    /**
     * 测试内容审核
     */
    @PostMapping("/test")
    public SingleResponse<ModerationTestResultDTO> testContent(
            @Valid @RequestBody ModerationTestCmd cmd) {
        return SingleResponse.of(moderationService.testContent(cmd));
    }

    /**
     * 批量测试内容审核
     */
    @PostMapping("/test/batch")
    public SingleResponse<List<ModerationTestResultDTO>> batchTestContent(
            @Valid @RequestBody ModerationBatchTestCmd cmd) {
        return SingleResponse.of(moderationService.batchTestContent(cmd));
    }
}