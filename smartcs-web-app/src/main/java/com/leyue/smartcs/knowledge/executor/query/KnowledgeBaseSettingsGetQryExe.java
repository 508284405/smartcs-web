package com.leyue.smartcs.knowledge.executor.query;

import com.alibaba.cola.dto.SingleResponse;
import com.leyue.smartcs.config.context.UserContext;
import com.leyue.smartcs.domain.knowledge.KnowledgeBase;
import com.leyue.smartcs.domain.knowledge.KnowledgeBaseSettings;
import com.leyue.smartcs.domain.knowledge.gateway.KnowledgeBaseGateway;
import com.leyue.smartcs.domain.knowledge.gateway.KnowledgeBaseSettingsGateway;
import com.leyue.smartcs.dto.knowledge.KnowledgeBaseSettingsDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 知识库设置查询执行器
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class KnowledgeBaseSettingsGetQryExe {

    private final KnowledgeBaseGateway knowledgeBaseGateway;
    
    private final KnowledgeBaseSettingsGateway settingsGateway;

    /**
     * 执行知识库设置查询
     */
    public SingleResponse<KnowledgeBaseSettingsDTO> execute(Long id) {
        log.info("执行知识库设置查询, ID: {}", id);

        try {
            // 参数校验
            if (id == null) {
                log.warn("知识库ID不能为空");
                return SingleResponse.buildFailure("KNOWLEDGE_BASE_ID_NULL", "知识库ID不能为空");
            }

            // 查询知识库
            KnowledgeBase knowledgeBase = knowledgeBaseGateway.findById(id);
            if (knowledgeBase == null) {
                log.warn("知识库不存在, ID: {}", id);
                return SingleResponse.buildFailure("KNOWLEDGE_BASE_NOT_FOUND", "知识库不存在");
            }

            // 检查访问权限
            if (!hasAccessPermission(knowledgeBase)) {
                log.warn("用户无权限访问知识库设置, 用户ID: {}, 知识库ID: {}", 
                    UserContext.getCurrentUser().getId(), id);
                return SingleResponse.buildFailure("KNOWLEDGE_BASE_NO_PERMISSION", "无权限访问此知识库设置");
            }

            // 查询知识库设置，如果不存在则使用默认值
            KnowledgeBaseSettings settings = settingsGateway.findByKnowledgeBaseId(id);
            if (settings == null) {
                settings = KnowledgeBaseSettings.createDefault(id);
                log.info("知识库设置不存在，使用默认值, ID: {}", id);
            }

            // 转换为DTO，包含主表的基本信息
            KnowledgeBaseSettingsDTO settingsDTO = buildSettingsDTO(knowledgeBase, settings);

            log.info("知识库设置查询成功, ID: {}, 名称: {}", id, knowledgeBase.getName());

            return SingleResponse.of(settingsDTO);

        } catch (Exception e) {
            log.error("知识库设置查询失败", e);
            return SingleResponse.buildFailure("KNOWLEDGE_BASE_SETTINGS_GET_ERROR", 
                "知识库设置查询失败: " + e.getMessage());
        }
    }

    /**
     * 构建知识库设置DTO
     */
    private KnowledgeBaseSettingsDTO buildSettingsDTO(KnowledgeBase knowledgeBase, KnowledgeBaseSettings settings) {
        return KnowledgeBaseSettingsDTO.builder()
            .id(knowledgeBase.getId())
            .name(knowledgeBase.getName())
            .description(knowledgeBase.getDescription())
            .visibility(knowledgeBase.getVisibility())
            .indexingMode(settings.getIndexingMode())
            .embeddingModel(settings.getEmbeddingModel())
            .retrievalSettings(KnowledgeBaseSettingsDTO.RetrievalSettingsDTO.builder()
                .vectorSearch(KnowledgeBaseSettingsDTO.VectorSearchDTO.builder()
                    .enabled(settings.getVectorSearch() != null ? 
                        settings.getVectorSearch().getEnabled() : true)
                    .topK(settings.getVectorSearch() != null ? 
                        settings.getVectorSearch().getTopK() : 10)
                    .scoreThreshold(settings.getVectorSearch() != null ? 
                        settings.getVectorSearch().getScoreThreshold() : BigDecimal.ZERO)
                    .build())
                .fullTextSearch(KnowledgeBaseSettingsDTO.FullTextSearchDTO.builder()
                    .enabled(settings.getFullTextSearch() != null ? 
                        settings.getFullTextSearch().getEnabled() : false)
                    .build())
                .hybridSearch(KnowledgeBaseSettingsDTO.HybridSearchDTO.builder()
                    .enabled(settings.getHybridSearch() != null ? 
                        settings.getHybridSearch().getEnabled() : false)
                    .rerankEnabled(settings.getHybridSearch() != null ? 
                        settings.getHybridSearch().getRerankEnabled() : false)
                    .build())
                .build())
            .build();
    }

    /**
     * 检查用户是否有访问权限
     * 规则：
     * 1. 公开知识库所有人都可以访问
     * 2. 私有知识库只有所有者可以访问
     */
    private boolean hasAccessPermission(KnowledgeBase knowledgeBase) {
        // 公开知识库所有人都可以访问
        if (knowledgeBase.isPublic()) {
            return true;
        }

        // 私有知识库只有所有者可以访问
        Long currentUserId = UserContext.getCurrentUser().getId();
        return knowledgeBase.isOwner(currentUserId);
    }
}