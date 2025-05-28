package com.leyue.smartcs.knowledge.executor.query;

import com.alibaba.cola.dto.PageResponse;
import com.leyue.smartcs.config.context.UserContext;
import com.leyue.smartcs.domain.knowledge.KnowledgeBase;
import com.leyue.smartcs.domain.knowledge.gateway.KnowledgeBaseGateway;
import com.leyue.smartcs.dto.knowledge.KnowledgeBaseDTO;
import com.leyue.smartcs.dto.knowledge.KnowledgeBaseListQry;
import com.leyue.smartcs.knowledge.convertor.KnowledgeBaseConvertor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 知识库列表查询执行器
 */
@Component
@Slf4j
public class KnowledgeBaseListQryExe {

    @Autowired
    private KnowledgeBaseGateway knowledgeBaseGateway;

    @Autowired
    private KnowledgeBaseConvertor knowledgeBaseConvertor;

    /**
     * 执行知识库列表查询
     */
    public PageResponse<KnowledgeBaseDTO> execute(KnowledgeBaseListQry qry) {
        log.info("执行知识库列表查询: {}", qry);

        try {
            // 参数校验和默认值设置
            validateAndSetDefaults(qry);

            // 权限过滤：根据当前用户权限调整查询条件
            adjustQueryForPermission(qry);

            // 执行分页查询
            PageResponse<KnowledgeBase> pageResult = knowledgeBaseGateway.listByPage(qry);

            // 过滤用户有权限访问的知识库
            List<KnowledgeBase> filteredKnowledgeBases = filterByPermission(pageResult.getData());

            // 转换为DTO
            List<KnowledgeBaseDTO> knowledgeBaseDTOs = knowledgeBaseConvertor.toDTOList(filteredKnowledgeBases);

            log.info("知识库列表查询完成，共 {} 条记录", knowledgeBaseDTOs.size());

            return PageResponse.of(
                    knowledgeBaseDTOs,
                    pageResult.getTotalCount(),
                    qry.getPageSize(),
                    qry.getPageIndex()
            );

        } catch (Exception e) {
            log.error("知识库列表查询失败", e);
            return PageResponse.of(
                    List.of(),
                    0,
                    qry.getPageSize(),
                    qry.getPageIndex()
            );
        }
    }

    /**
     * 参数校验和默认值设置
     */
    private void validateAndSetDefaults(KnowledgeBaseListQry qry) {
        if (qry.getPageIndex() < 1) {
            qry.setPageIndex(1);
        }

        if (qry.getPageSize() < 1) {
            qry.setPageSize(10);
        }

        // 限制每页最大数量
        if (qry.getPageSize() > 100) {
            qry.setPageSize(100);
        }
    }

    /**
     * 根据当前用户权限调整查询条件
     * 如果没有指定ownerId，则默认查询当前用户拥有的知识库和公开知识库
     */
    private void adjustQueryForPermission(KnowledgeBaseListQry qry) {
        Long currentUserId = UserContext.getCurrentUser().getId();

        // 如果没有指定所有者，则查询当前用户有权限访问的知识库
        if (qry.getOwnerId() == null) {
            // 这里我们先查询所有，然后在filterByPermission中进行权限过滤
            // 也可以在数据库层面进行优化，使用OR条件查询
            log.debug("未指定所有者，将查询用户有权限访问的知识库，用户ID: {}", currentUserId);
        }
    }

    /**
     * 根据权限过滤知识库列表
     * 规则：
     * 1. 用户可以访问自己创建的所有知识库
     * 2. 用户可以访问所有公开的知识库
     */
    private List<KnowledgeBase> filterByPermission(List<KnowledgeBase> knowledgeBases) {
        Long currentUserId = UserContext.getCurrentUser().getId();

        return knowledgeBases.stream()
                .filter(knowledgeBase -> hasAccessPermission(knowledgeBase, currentUserId))
                .collect(Collectors.toList());
    }

    /**
     * 检查用户是否有访问权限
     */
    private boolean hasAccessPermission(KnowledgeBase knowledgeBase, Long userId) {
        // 用户可以访问自己创建的知识库
        if (knowledgeBase.isOwner(userId)) {
            return true;
        }

        // 用户可以访问公开的知识库
        return knowledgeBase.isPublic();
    }
} 