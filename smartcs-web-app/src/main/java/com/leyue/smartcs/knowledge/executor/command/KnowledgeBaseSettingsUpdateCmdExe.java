package com.leyue.smartcs.knowledge.executor.command;

import com.alibaba.cola.dto.Response;
import com.leyue.smartcs.config.context.UserContext;
import com.leyue.smartcs.domain.knowledge.KnowledgeBase;
import com.leyue.smartcs.domain.knowledge.KnowledgeBaseSettings;
import com.leyue.smartcs.domain.knowledge.gateway.KnowledgeBaseGateway;
import com.leyue.smartcs.domain.knowledge.gateway.KnowledgeBaseSettingsGateway;
import com.leyue.smartcs.dto.knowledge.KnowledgeBaseSettingsUpdateCmd;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 知识库设置更新执行器
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class KnowledgeBaseSettingsUpdateCmdExe {

    private final KnowledgeBaseGateway knowledgeBaseGateway;
    
    private final KnowledgeBaseSettingsGateway settingsGateway;

    /**
     * 执行知识库设置更新
     */
    public Response execute(KnowledgeBaseSettingsUpdateCmd cmd) {
        try {
            // 检查知识库是否存在
            KnowledgeBase existingKnowledgeBase = knowledgeBaseGateway.findById(cmd.getId());
            if (existingKnowledgeBase == null) {
                log.warn("知识库不存在, ID: {}", cmd.getId());
                return Response.buildFailure("KNOWLEDGE_BASE_NOT_FOUND", "知识库不存在");
            }

            // 检查权限：只有所有者才能更新知识库设置
            Long currentUserId = UserContext.getCurrentUser().getId();
            if (!existingKnowledgeBase.isOwner(currentUserId)) {
                log.warn("用户无权限更新知识库设置, 用户ID: {}, 知识库ID: {}", currentUserId, cmd.getId());
                return Response.buildFailure("KNOWLEDGE_BASE_NO_PERMISSION", "无权限更新此知识库设置");
            }

            // 检查名称是否重复（排除自己）
            if (StringUtils.hasText(cmd.getName()) && !cmd.getName().equals(existingKnowledgeBase.getName())) {
                if (knowledgeBaseGateway.existsByName(cmd.getName())) {
                    log.warn("知识库名称已存在: {}", cmd.getName());
                    return Response.buildFailure("KNOWLEDGE_BASE_NAME_EXISTS", "知识库名称已存在");
                }
            }

            // 更新主表的基本信息（如果有变更）
            updateKnowledgeBaseBasicInfo(existingKnowledgeBase, cmd);
            
            // 更新知识库设置
            updateKnowledgeBaseSettings(cmd);

            log.info("知识库设置更新成功, ID: {}, 名称: {}", cmd.getId(), existingKnowledgeBase.getName());

            return Response.buildSuccess();

        } catch (Exception e) {
            log.error("知识库设置更新失败", e);
            return Response.buildFailure("KNOWLEDGE_BASE_SETTINGS_UPDATE_ERROR", 
                "知识库设置更新失败: " + e.getMessage());
        }
    }

    /**
     * 更新知识库基本信息
     */
    private void updateKnowledgeBaseBasicInfo(KnowledgeBase knowledgeBase, KnowledgeBaseSettingsUpdateCmd cmd) {
        boolean hasUpdate = false;

        // 更新名称
        if (StringUtils.hasText(cmd.getName()) && !cmd.getName().equals(knowledgeBase.getName())) {
            knowledgeBase.setName(cmd.getName().trim());
            hasUpdate = true;
        }

        // 更新描述
        if (cmd.getDescription() != null && !cmd.getDescription().equals(knowledgeBase.getDescription())) {
            knowledgeBase.setDescription(StringUtils.hasText(cmd.getDescription()) ? 
                cmd.getDescription().trim() : null);
            hasUpdate = true;
        }

        // 更新可见性
        if (StringUtils.hasText(cmd.getVisibility()) && !cmd.getVisibility().equals(knowledgeBase.getVisibility())) {
            knowledgeBase.setVisibility(cmd.getVisibility());
            hasUpdate = true;
        }

        // 如果有更新，保存主表信息
        if (hasUpdate) {
            knowledgeBase.setUpdatedAt(System.currentTimeMillis());
            knowledgeBaseGateway.update(knowledgeBase);
            log.info("知识库基本信息更新完成, ID: {}", knowledgeBase.getId());
        }
    }

    /**
     * 更新知识库设置
     */
    private void updateKnowledgeBaseSettings(KnowledgeBaseSettingsUpdateCmd cmd) {
        // 构建设置对象
        KnowledgeBaseSettings settings = KnowledgeBaseSettings.builder()
            .knowledgeBaseId(cmd.getId())
            .indexingMode(cmd.getIndexingMode())
            .embeddingModel(cmd.getEmbeddingModel())
            .vectorSearch(KnowledgeBaseSettings.VectorSearchSettings.builder()
                .enabled(cmd.getRetrievalSettings().getVectorSearch().getEnabled())
                .topK(cmd.getRetrievalSettings().getVectorSearch().getTopK())
                .scoreThreshold(cmd.getRetrievalSettings().getVectorSearch().getScoreThreshold())
                .build())
            .fullTextSearch(KnowledgeBaseSettings.FullTextSearchSettings.builder()
                .enabled(cmd.getRetrievalSettings().getFullTextSearch().getEnabled())
                .build())
            .hybridSearch(KnowledgeBaseSettings.HybridSearchSettings.builder()
                .enabled(cmd.getRetrievalSettings().getHybridSearch().getEnabled())
                .rerankEnabled(cmd.getRetrievalSettings().getHybridSearch().getRerankEnabled())
                .build())
            .updatedBy(UserContext.getCurrentUser().getId().toString())
            .updatedAt(System.currentTimeMillis())
            .build();

        // 保存或更新设置
        settingsGateway.saveOrUpdate(settings);
        log.info("知识库设置更新完成, ID: {}", cmd.getId());
    }
}