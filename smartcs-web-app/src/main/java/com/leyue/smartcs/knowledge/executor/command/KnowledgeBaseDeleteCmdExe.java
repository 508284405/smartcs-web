package com.leyue.smartcs.knowledge.executor.command;

import com.alibaba.cola.dto.Response;
import com.leyue.smartcs.domain.knowledge.KnowledgeBase;
import com.leyue.smartcs.domain.knowledge.gateway.ContentGateway;
import com.leyue.smartcs.domain.knowledge.gateway.KnowledgeBaseGateway;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 知识库删除执行器
 */
@Component
@Slf4j
public class KnowledgeBaseDeleteCmdExe {

    @Autowired
    private KnowledgeBaseGateway knowledgeBaseGateway;
    
    @Autowired
    private ContentGateway contentGateway;

    /**
     * 执行知识库删除
     */
    public Response execute(Long knowledgeBaseId) {
        log.info("执行知识库删除, ID: {}", knowledgeBaseId);
        
        try {
            
            // 检查知识库是否存在
            KnowledgeBase knowledgeBase = knowledgeBaseGateway.findById(knowledgeBaseId);
            if (knowledgeBase == null) {
                log.warn("知识库不存在, ID: {}", knowledgeBaseId);
                return Response.buildFailure("KNOWLEDGE_BASE_NOT_FOUND", "知识库不存在");
            }
            
            // 检查是否有关联的内容
            Long contentCount = contentGateway.countByKnowledgeBaseId(knowledgeBaseId);
            if (contentCount > 0) {
                log.warn("知识库包含内容，不能删除, ID: {}, 内容数量: {}", 
                    knowledgeBaseId, contentCount);
                return Response.buildFailure("KNOWLEDGE_BASE_HAS_CONTENT", 
                    String.format("知识库包含 %d 个内容，请先删除内容后再删除知识库", contentCount));
            }
            
            // 执行删除
            knowledgeBaseGateway.deleteById(knowledgeBaseId);
            
            log.info("知识库删除成功, ID: {}, 名称: {}", knowledgeBaseId, knowledgeBase.getName());
            
            return Response.buildSuccess();
            
        } catch (Exception e) {
            log.error("知识库删除失败", e);
            return Response.buildFailure("KNOWLEDGE_BASE_DELETE_ERROR", "知识库删除失败: " + e.getMessage());
        }
    }
} 