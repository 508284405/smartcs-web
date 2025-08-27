package com.leyue.smartcs.api;

import com.alibaba.cola.dto.PageResponse;
import com.leyue.smartcs.dto.moderation.ModerationWithModelRequest;
import com.leyue.smartcs.dto.moderation.ModerationDTOs;

import java.util.List;

/**
 * 审核服务接口
 * 提供内容审核相关的MVP服务
 *
 * @author Claude
 */
public interface ModerationService {

    // ===== 内容测试 =====

    /**
     * 测试内容审核
     */
    ModerationDTOs.ModerationTestResultDTO testContent(ModerationDTOs.ModerationTestCmd cmd);

    /**
     * 批量测试内容审核
     */
    List<ModerationDTOs.ModerationTestResultDTO> batchTestContent(ModerationDTOs.ModerationBatchTestCmd cmd);

    // ===== AI模型审核 =====

    /**
     * 使用指定AI模型进行内容审核
     * 
     * @param request 包含内容和模型ID的审核请求
     * @return 审核结果
     */
    ModerationDTOs.ModerationTestResultDTO moderateContentWithModel(ModerationWithModelRequest request);

    /**
     * 使用指定AI模型进行快速内容预检
     * 
     * @param request 包含内容和模型ID的快速预检请求
     * @return 快速预检结果
     */
    ModerationDTOs.ModerationTestResultDTO quickModerateWithModel(ModerationWithModelRequest request);

    // ===== 审核分类管理 =====

    /**
     * 创建审核分类
     */
    Long createCategory(ModerationDTOs.ModerationCategoryCreateCmd cmd);

    /**
     * 更新审核分类
     */
    void updateCategory(ModerationDTOs.ModerationCategoryUpdateCmd cmd);

    /**
     * 删除审核分类
     */
    void deleteCategory(Long id);

    /**
     * 根据ID查询审核分类
     */
    ModerationDTOs.ModerationCategoryDTO getCategory(Long id);

    /**
     * 分页查询审核分类
     */
    PageResponse<ModerationDTOs.ModerationCategoryDTO> listCategories(ModerationDTOs.ModerationCategoryPageQry qry);

    /**
     * 获取所有审核分类树形结构
     */
    List<ModerationDTOs.ModerationCategoryDTO> getCategoryTree();
}