package com.leyue.smartcs.knowledge.executor.command;

import com.alibaba.cola.dto.SingleResponse;
import com.leyue.smartcs.config.context.UserContext;
import com.leyue.smartcs.domain.knowledge.KnowledgeBase;
import com.leyue.smartcs.domain.knowledge.gateway.KnowledgeBaseGateway;
import com.leyue.smartcs.dto.knowledge.KnowledgeBaseCreateCmd;
import com.leyue.smartcs.dto.knowledge.KnowledgeBaseDTO;
import com.leyue.smartcs.knowledge.convertor.KnowledgeBaseConvertor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 知识库创建执行器
 */
@Component
@Slf4j
public class KnowledgeBaseCreateCmdExe {

    @Autowired
    private KnowledgeBaseGateway knowledgeBaseGateway;

    @Autowired
    private KnowledgeBaseConvertor knowledgeBaseConvertor;

    /**
     * 执行知识库创建
     */
    public SingleResponse<KnowledgeBaseDTO> execute(KnowledgeBaseCreateCmd cmd) {
        try {
            // 参数校验
            validateParams(cmd);
            // 检查名称是否重复
            if (knowledgeBaseGateway.existsByName(cmd.getName())) {
                return SingleResponse.buildFailure("KNOWLEDGE_BASE_NAME_EXISTS", "知识库名称已存在");
            }
            // 检查编码是否重复
            if (knowledgeBaseGateway.existsByCode(cmd.getCode())) {
                return SingleResponse.buildFailure("KNOWLEDGE_BASE_CODE_EXISTS", "知识库编码已存在");
            }

            // 构建知识库领域对象
            KnowledgeBase knowledgeBase = knowledgeBaseConvertor.toDomain(cmd);

            // 保存知识库
            knowledgeBase.setOwnerId(UserContext.getCurrentUser().getId());
            KnowledgeBase savedKnowledgeBase = knowledgeBaseGateway.save(knowledgeBase);

            // 转换为DTO
            KnowledgeBaseDTO knowledgeBaseDTO = knowledgeBaseConvertor.toDTO(savedKnowledgeBase);

            log.info("知识库创建成功, ID: {}, 名称: {}", savedKnowledgeBase.getId(), savedKnowledgeBase.getName());

            return SingleResponse.of(knowledgeBaseDTO);

        } catch (Exception e) {
            log.error("知识库创建失败", e);
            return SingleResponse.buildFailure("KNOWLEDGE_BASE_CREATE_ERROR", "知识库创建失败: " + e.getMessage());
        }
    }

    /**
     * 参数校验
     */
    private void validateParams(KnowledgeBaseCreateCmd cmd) {
        if (cmd == null) {
            throw new IllegalArgumentException("创建命令不能为空");
        }

        if (!StringUtils.hasText(cmd.getName())) {
            throw new IllegalArgumentException("知识库名称不能为空");
        }

        if (cmd.getName().length() > 100) {
            throw new IllegalArgumentException("知识库名称长度不能超过100个字符");
        }

        if (StringUtils.hasText(cmd.getDescription()) && cmd.getDescription().length() > 500) {
            throw new IllegalArgumentException("知识库描述长度不能超过500个字符");
        }
    }
}