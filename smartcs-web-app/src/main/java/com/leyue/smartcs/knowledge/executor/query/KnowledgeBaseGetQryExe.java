package com.leyue.smartcs.knowledge.executor.query;

import com.alibaba.cola.dto.SingleResponse;
import com.leyue.smartcs.config.context.UserContext;
import com.leyue.smartcs.domain.knowledge.KnowledgeBase;
import com.leyue.smartcs.domain.knowledge.gateway.KnowledgeBaseGateway;
import com.leyue.smartcs.dto.knowledge.KnowledgeBaseDTO;
import com.leyue.smartcs.knowledge.convertor.KnowledgeBaseConvertor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 知识库查询执行器
 */
@Component
@Slf4j
public class KnowledgeBaseGetQryExe {

    @Autowired
    private KnowledgeBaseGateway knowledgeBaseGateway;

    @Autowired
    private KnowledgeBaseConvertor knowledgeBaseConvertor;

    /**
     * 执行知识库查询
     */
    public SingleResponse<KnowledgeBaseDTO> execute(Long id) {
        log.info("执行知识库查询, ID: {}", id);

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
                log.warn("用户无权限访问知识库, 用户ID: {}, 知识库ID: {}", 
                    UserContext.getCurrentUser().getId(), id);
                return SingleResponse.buildFailure("KNOWLEDGE_BASE_NO_PERMISSION", "无权限访问此知识库");
            }

            // 转换为DTO
            KnowledgeBaseDTO knowledgeBaseDTO = knowledgeBaseConvertor.toDTO(knowledgeBase);

            log.info("知识库查询成功, ID: {}, 名称: {}", id, knowledgeBase.getName());

            return SingleResponse.of(knowledgeBaseDTO);

        } catch (Exception e) {
            log.error("知识库查询失败", e);
            return SingleResponse.buildFailure("KNOWLEDGE_BASE_GET_ERROR", "知识库查询失败: " + e.getMessage());
        }
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