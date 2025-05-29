package com.leyue.smartcs.knowledge.executor.command;

import com.alibaba.cola.dto.Response;
import com.leyue.smartcs.config.context.UserContext;
import com.leyue.smartcs.domain.knowledge.KnowledgeBase;
import com.leyue.smartcs.domain.knowledge.gateway.KnowledgeBaseGateway;
import com.leyue.smartcs.dto.knowledge.KnowledgeBaseUpdateCmd;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 知识库更新执行器
 */
@Component
@Slf4j
public class KnowledgeBaseUpdateCmdExe {

    @Autowired
    private KnowledgeBaseGateway knowledgeBaseGateway;

    /**
     * 执行知识库更新
     */
    public Response execute(KnowledgeBaseUpdateCmd cmd) {
        try {
            // 检查知识库是否存在
            KnowledgeBase existingKnowledgeBase = knowledgeBaseGateway.findById(cmd.getId());
            if (existingKnowledgeBase == null) {
                log.warn("知识库不存在, ID: {}", cmd.getId());
                return Response.buildFailure("KNOWLEDGE_BASE_NOT_FOUND", "知识库不存在");
            }

            // 检查权限：只有所有者才能更新知识库
            Long currentUserId = UserContext.getCurrentUser().getId();
            if (!existingKnowledgeBase.isOwner(currentUserId)) {
                log.warn("用户无权限更新知识库, 用户ID: {}, 知识库ID: {}", currentUserId, cmd.getId());
                return Response.buildFailure("KNOWLEDGE_BASE_NO_PERMISSION", "无权限更新此知识库");
            }

            // 检查名称是否重复（排除自己）
            if (StringUtils.hasText(cmd.getName()) && !cmd.getName().equals(existingKnowledgeBase.getName())) {
                if (knowledgeBaseGateway.existsByName(cmd.getName())) {
                    log.warn("知识库名称已存在: {}", cmd.getName());
                    return Response.buildFailure("KNOWLEDGE_BASE_NAME_EXISTS", "知识库名称已存在");
                }
            }

            // 更新知识库信息
            updateKnowledgeBase(existingKnowledgeBase, cmd);

            // 保存更新
            knowledgeBaseGateway.update(existingKnowledgeBase);

            log.info("知识库更新成功, ID: {}, 名称: {}", cmd.getId(), existingKnowledgeBase.getName());

            return Response.buildSuccess();

        } catch (Exception e) {
            log.error("知识库更新失败", e);
            return Response.buildFailure("KNOWLEDGE_BASE_UPDATE_ERROR", "知识库更新失败: " + e.getMessage());
        }
    }

    /**
     * 更新知识库信息
     */
    private void updateKnowledgeBase(KnowledgeBase knowledgeBase, KnowledgeBaseUpdateCmd cmd) {
        boolean hasUpdate = false;

        // 更新名称
        if (StringUtils.hasText(cmd.getName()) && !cmd.getName().equals(knowledgeBase.getName())) {
            knowledgeBase.setName(cmd.getName().trim());
            hasUpdate = true;
        }

        // 更新描述
        if (cmd.getDescription() != null && !cmd.getDescription().equals(knowledgeBase.getDescription())) {
            knowledgeBase.setDescription(StringUtils.hasText(cmd.getDescription()) ? cmd.getDescription().trim() : null);
            hasUpdate = true;
        }

        // 更新可见性
        if (StringUtils.hasText(cmd.getVisibility()) && !cmd.getVisibility().equals(knowledgeBase.getVisibility())) {
            knowledgeBase.setVisibility(cmd.getVisibility());
            hasUpdate = true;
        }

        // 如果有更新，设置更新时间
        if (hasUpdate) {
            knowledgeBase.setUpdatedAt(System.currentTimeMillis());
        }

        log.debug("知识库更新信息: {}", knowledgeBase);
    }
} 