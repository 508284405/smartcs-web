package com.leyue.smartcs.api;

import com.alibaba.cola.dto.PageResponse;
import com.leyue.smartcs.dto.moderation.*;

import java.util.List;

/**
 * 审核服务接口
 * 提供内容审核相关的业务服务
 *
 * @author Claude
 */
public interface ModerationService {

    // ===== 违规分类管理 =====

    /**
     * 获取违规分类树
     */
    List<ModerationCategoryDTO> getCategoryTree();

    /**
     * 分页查询违规分类
     */
    PageResponse<ModerationCategoryDTO> getCategoriesPage(ModerationCategoryPageQry qry);

    /**
     * 获取分类详情
     */
    ModerationCategoryDTO getCategoryDetail(Long categoryId);

    /**
     * 创建违规分类
     */
    ModerationCategoryDTO createCategory(ModerationCategoryCreateCmd cmd);

    /**
     * 更新违规分类
     */
    ModerationCategoryDTO updateCategory(ModerationCategoryUpdateCmd cmd);

    /**
     * 删除违规分类
     */
    void deleteCategory(Long categoryId);

    /**
     * 批量删除违规分类
     */
    void deleteCategoriesBatch(List<Long> categoryIds);

    // ===== 审核记录管理 =====

    /**
     * 分页查询审核记录
     */
    PageResponse<ModerationRecordDTO> getRecordsPage(ModerationRecordPageQry qry);

    /**
     * 获取审核记录详情
     */
    ModerationRecordDTO getRecordDetail(String recordId);

    /**
     * 批量审核记录
     */
    void batchReviewRecords(ModerationBatchReviewCmd cmd);

    /**
     * 人工审核记录
     */
    void reviewRecord(ModerationReviewCmd cmd);

    /**
     * 导出审核记录
     */
    String exportRecords(ModerationRecordExportCmd cmd);

    // ===== 关键词规则管理 =====

    /**
     * 分页查询关键词规则
     */
    PageResponse<ModerationKeywordRuleDTO> getKeywordRulesPage(ModerationKeywordRulePageQry qry);

    /**
     * 获取关键词规则详情
     */
    ModerationKeywordRuleDTO getKeywordRuleDetail(Long ruleId);

    /**
     * 创建关键词规则
     */
    ModerationKeywordRuleDTO createKeywordRule(ModerationKeywordRuleCreateCmd cmd);

    /**
     * 更新关键词规则
     */
    ModerationKeywordRuleDTO updateKeywordRule(ModerationKeywordRuleUpdateCmd cmd);

    /**
     * 删除关键词规则
     */
    void deleteKeywordRule(Long ruleId);

    /**
     * 批量删除关键词规则
     */
    void deleteKeywordRulesBatch(List<Long> ruleIds);

    /**
     * 启用/禁用关键词规则
     */
    void toggleKeywordRule(Long ruleId);

    /**
     * 测试关键词规则
     */
    ModerationTestResultDTO testKeywordRule(ModerationKeywordTestCmd cmd);

    // ===== 审核配置管理 =====

    /**
     * 获取审核配置
     */
    ModerationConfigDTO getConfig();

    /**
     * 更新审核配置
     */
    void updateConfig(ModerationConfigUpdateCmd cmd);

    /**
     * 重置审核配置为默认值
     */
    void resetConfig();

    // ===== 审核统计 =====

    /**
     * 获取审核统计概览
     */
    ModerationStatisticsDTO getStatisticsOverview(String timeRange);

    /**
     * 获取审核趋势统计
     */
    ModerationTrendsDTO getStatisticsTrends(ModerationStatisticsTrendsQry qry);

    /**
     * 获取违规分类统计
     */
    List<ModerationCategoryStatDTO> getCategoryStatistics(String timeRange);

    /**
     * 获取审核员绩效统计
     */
    List<ModerationReviewerStatDTO> getReviewerStatistics(ModerationReviewerStatQry qry);

    // ===== 内容测试 =====

    /**
     * 测试内容审核
     */
    ModerationTestResultDTO testContent(ModerationTestCmd cmd);

    /**
     * 批量测试内容审核
     */
    List<ModerationTestResultDTO> batchTestContent(ModerationBatchTestCmd cmd);

    // ===== AI模型审核 =====

    /**
     * 使用指定AI模型进行内容审核
     * 
     * @param request 包含内容和模型ID的审核请求
     * @return 审核结果
     */
    ModerationTestResultDTO moderateContentWithModel(ModerationWithModelRequest request);

    /**
     * 使用指定AI模型进行快速内容预检
     * 
     * @param request 包含内容和模型ID的审核请求
     * @return 快速审核结果
     */
    ModerationTestResultDTO quickModerateWithModel(ModerationWithModelRequest request);
}