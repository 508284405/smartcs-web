package com.leyue.smartcs.moderation.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.alibaba.cola.dto.PageResponse;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.api.ModerationService;
import com.leyue.smartcs.domain.moderation.ModerationCategory;
import com.leyue.smartcs.domain.moderation.enums.ActionType;
import com.leyue.smartcs.domain.moderation.enums.SeverityLevel;
import com.leyue.smartcs.domain.moderation.gateway.ModerationGateway;
import com.leyue.smartcs.dto.moderation.ModerationDTOs;
import com.leyue.smartcs.dto.moderation.ModerationWithModelRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 审核服务实现类
 * 提供内容审核相关的MVP服务
 *
 * @author Claude
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ModerationServiceImpl implements ModerationService {

    private final LangChain4jModerationService langChain4jModerationService;
    private final ModerationGateway moderationGateway;

    @Override
    public ModerationDTOs.ModerationTestResultDTO testContent(ModerationDTOs.ModerationTestCmd cmd) {
        log.info("开始测试内容审核，contentLength: {}", cmd.getContent().length());
        
        try {
            // 调用LangChain4j审核服务，使用默认模型ID
            Long defaultModelId = 1L; // 使用默认模型ID
            CompletableFuture<LangChain4jModerationService.AiModerationResult> future = 
                    langChain4jModerationService.moderateContent(cmd.getContent(), defaultModelId);
            
            // 等待结果
            LangChain4jModerationService.AiModerationResult aiResult = future.get();
            
            // 转换为DTO
            ModerationDTOs.ModerationTestResultDTO result = new ModerationDTOs.ModerationTestResultDTO();
            result.setContent(cmd.getContent());
            result.setResult(aiResult.getResult().name());
            result.setRiskLevel(aiResult.getRiskLevel() != null ? aiResult.getRiskLevel().name() : "LOW");
            result.setConfidence(aiResult.getConfidence() != null ? aiResult.getConfidence().doubleValue() : 0.0);
            result.setProcessingTime((long) aiResult.getProcessingTimeMs());
            return result;
                    
        } catch (Exception e) {
            log.error("内容审核测试失败，content: {}", cmd.getContent(), e);
            ModerationDTOs.ModerationTestResultDTO result = new ModerationDTOs.ModerationTestResultDTO();
            result.setContent(cmd.getContent());
            result.setResult("ERROR");
            result.setRiskLevel("UNKNOWN");
            result.setConfidence(0.0);
            result.setProcessingTime(0L);
            return result;
        }
    }

    @Override
    public List<ModerationDTOs.ModerationTestResultDTO> batchTestContent(ModerationDTOs.ModerationBatchTestCmd cmd) {
        log.info("开始批量测试内容审核，contentCount: {}", cmd.getContents().size());
        
        List<ModerationDTOs.ModerationTestResultDTO> results = new ArrayList<>();
        for (String content : cmd.getContents()) {
            ModerationDTOs.ModerationTestCmd singleCmd = new ModerationDTOs.ModerationTestCmd();
            singleCmd.setContent(content);
            results.add(testContent(singleCmd));
        }
        return results;
    }

    @Override
    public ModerationDTOs.ModerationTestResultDTO moderateContentWithModel(ModerationWithModelRequest request) {
        log.info("开始AI模型审核，modelId: {}, contentLength: {}", request.getModelId(), request.getContent().length());
        
        try {
            // 调用LangChain4j审核服务
            CompletableFuture<LangChain4jModerationService.AiModerationResult> future = 
                    langChain4jModerationService.moderateContent(request.getContent(), request.getModelId());
            
            // 等待结果
            LangChain4jModerationService.AiModerationResult aiResult = future.get();
            
            // 转换为DTO
            ModerationDTOs.ModerationTestResultDTO result = new ModerationDTOs.ModerationTestResultDTO();
            result.setContent(request.getContent());
            result.setResult(aiResult.getResult().name());
            result.setRiskLevel(aiResult.getRiskLevel() != null ? aiResult.getRiskLevel().name() : "LOW");
            result.setConfidence(aiResult.getConfidence() != null ? aiResult.getConfidence().doubleValue() : 0.0);
            result.setProcessingTime((long) aiResult.getProcessingTimeMs());
            return result;
                    
        } catch (Exception e) {
            log.error("AI模型审核失败，modelId: {}, content: {}", 
                    request.getModelId(), request.getContent(), e);
            ModerationDTOs.ModerationTestResultDTO result = new ModerationDTOs.ModerationTestResultDTO();
            result.setContent(request.getContent());
            result.setResult("ERROR");
            result.setRiskLevel("UNKNOWN");
            result.setConfidence(0.0);
            result.setProcessingTime(0L);
            return result;
        }
    }

    @Override
    public ModerationDTOs.ModerationTestResultDTO quickModerateWithModel(ModerationWithModelRequest request) {
        log.info("开始快速AI模型预检，modelId: {}, contentLength: {}", request.getModelId(), request.getContent().length());
        
        try {
            // 调用LangChain4j快速审核服务
            CompletableFuture<LangChain4jModerationService.QuickModerationResult> future = 
                    langChain4jModerationService.quickModerate(request.getContent(), request.getModelId());
            
            // 等待结果
            LangChain4jModerationService.QuickModerationResult quickResult = future.get();
            
            // 转换为DTO
            ModerationDTOs.ModerationTestResultDTO result = new ModerationDTOs.ModerationTestResultDTO();
            result.setContent(request.getContent());
            result.setResult(quickResult.getResult().name());
            result.setRiskLevel(quickResult.isBlocked() ? "HIGH" : "LOW");
            result.setConfidence(0.8); // 快速预检使用默认置信度
            result.setProcessingTime((long) quickResult.getProcessingTimeMs());
            return result;
                    
        } catch (Exception e) {
            log.error("快速AI模型预检失败，modelId: {}, content: {}", 
                    request.getModelId(), request.getContent(), e);
            ModerationDTOs.ModerationTestResultDTO result = new ModerationDTOs.ModerationTestResultDTO();
            result.setContent(request.getContent());
            result.setResult("ERROR");
            result.setRiskLevel("UNKNOWN");
            result.setConfidence(0.0);
            result.setProcessingTime(0L);
            return result;
        }
    }

    // ===== 审核分类管理 =====

    @Override
    public Long createCategory(ModerationDTOs.ModerationCategoryCreateCmd cmd) {
        log.info("创建审核分类: name={}, code={}", cmd.getName(), cmd.getCode());
        
        // 检查分类编码是否已存在
        if (moderationGateway.findCategoryByCode(cmd.getCode()).isPresent()) {
            throw new BizException("分类编码已存在: " + cmd.getCode());
        }
        
        // 如果指定了父分类，检查父分类是否存在
        if (cmd.getParentId() != null) {
            if (!moderationGateway.findCategoryById(cmd.getParentId()).isPresent()) {
                throw new BizException("父分类不存在: " + cmd.getParentId());
            }
        }
        
        // 创建分类实体
        ModerationCategory category = ModerationCategory.create(
                cmd.getName(),
                cmd.getCode(),
                cmd.getDescription(),
                SeverityLevel.valueOf(cmd.getSeverityLevel()),
                ActionType.valueOf(cmd.getActionType()),
                cmd.getParentId(),
                cmd.getSortOrder(),
                "admin" // TODO: 从上下文获取当前用户
        );
        
        ModerationCategory saved = moderationGateway.saveModerationCategory(category);
        return saved.getId();
    }

    @Override
    public void updateCategory(ModerationDTOs.ModerationCategoryUpdateCmd cmd) {
        log.info("更新审核分类: id={}, name={}", cmd.getId(), cmd.getName());
        
        // 检查分类是否存在
        ModerationCategory category = moderationGateway.findCategoryById(cmd.getId())
                .orElseThrow(() -> new BizException("分类不存在: " + cmd.getId()));
        
        // 更新分类信息
        category.update(
                cmd.getName(),
                cmd.getDescription(),
                SeverityLevel.valueOf(cmd.getSeverityLevel()),
                ActionType.valueOf(cmd.getActionType()),
                cmd.getSortOrder(),
                "admin" // TODO: 从上下文获取当前用户
        );
        
        // 更新启用状态
        if (cmd.getIsActive() != null) {
            if (cmd.getIsActive()) {
                category.enable("admin");
            } else {
                category.disable("admin");
            }
        }
        
        moderationGateway.saveModerationCategory(category);
    }

    @Override
    public void deleteCategory(Long id) {
        log.info("删除审核分类: id={}", id);
        
        // 检查分类是否存在
        ModerationCategory category = moderationGateway.findCategoryById(id)
                .orElseThrow(() -> new BizException("分类不存在: " + id));
        
        // 检查是否有子分类
        List<ModerationCategory> children = moderationGateway.findSubCategoriesByParentId(id);
        if (!children.isEmpty()) {
            throw new BizException("存在子分类，不能删除");
        }
        
        moderationGateway.deleteModerationCategory(id);
    }

    @Override
    public ModerationDTOs.ModerationCategoryDTO getCategory(Long id) {
        ModerationCategory category = moderationGateway.findCategoryById(id)
                .orElseThrow(() -> new BizException("分类不存在: " + id));
        return convertToDTO(category);
    }

    @Override
    public PageResponse<ModerationDTOs.ModerationCategoryDTO> listCategories(ModerationDTOs.ModerationCategoryPageQry qry) {
        // For now, return all categories since the gateway doesn't have page query methods
        // This should be implemented properly in the gateway layer
        List<ModerationCategory> categories = moderationGateway.findAllCategories();
        
        List<ModerationDTOs.ModerationCategoryDTO> dtos = categories.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        PageResponse<ModerationDTOs.ModerationCategoryDTO> response = new PageResponse<>();
        response.setData(dtos);
        response.setTotalCount(dtos.size());
        response.setPageIndex(qry.getPageIndex());
        response.setPageSize(qry.getPageSize());
        return response;
    }

    @Override
    public List<ModerationDTOs.ModerationCategoryDTO> getCategoryTree() {
        List<ModerationCategory> allCategories = moderationGateway.findAllCategories();
        
        // 构建树形结构
        List<ModerationCategory> topLevelCategories = allCategories.stream()
                .filter(ModerationCategory::isTopLevel)
                .collect(Collectors.toList());
        
        for (ModerationCategory topCategory : topLevelCategories) {
            List<ModerationCategory> children = allCategories.stream()
                    .filter(c -> topCategory.getId().equals(c.getParentId()))
                    .collect(Collectors.toList());
            topCategory.setChildren(children);
        }
        
        return topLevelCategories.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    private ModerationDTOs.ModerationCategoryDTO convertToDTO(ModerationCategory category) {
        ModerationDTOs.ModerationCategoryDTO dto = ModerationDTOs.ModerationCategoryDTO.builder()
                .id(category.getId())
                .parentId(category.getParentId())
                .name(category.getName())
                .code(category.getCode())
                .description(category.getDescription())
                .severityLevel(category.getSeverityLevel() != null ? category.getSeverityLevel().name() : null)
                .actionType(category.getActionType() != null ? category.getActionType().name() : null)
                .isActive(category.getIsActive())
                .sortOrder(category.getSortOrder())
                .createdBy(category.getCreatedBy())
                .updatedBy(category.getUpdatedBy())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
                
        // 转换子分类
        if (category.getChildren() != null && !category.getChildren().isEmpty()) {
            List<ModerationDTOs.ModerationCategoryDTO> childrenDTOs = category.getChildren().stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
            dto.setChildren(childrenDTOs);
        }
        
        return dto;
    }
}