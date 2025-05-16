package com.leyue.smartcs.knowledge.executor;

import com.alibaba.cola.dto.PageResponse;
import com.leyue.smartcs.domain.knowledge.gateway.DocumentGateway;
import com.leyue.smartcs.domain.knowledge.model.Document;
import com.leyue.smartcs.dto.knowledge.DocDTO;
import com.leyue.smartcs.dto.knowledge.KnowledgeSearchQry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 文档列表查询执行器
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DocListQryExe {
    
    private final DocumentGateway documentGateway;
    
    /**
     * 执行文档列表查询
     * @param qry 查询条件
     * @return 文档列表（分页）
     */
    public PageResponse<DocDTO> execute(KnowledgeSearchQry qry) {
        log.info("执行文档列表查询: {}", qry);
        
        // 获取分页参数
        Integer pageNum = qry.getPageIndex();
        Integer pageSize = qry.getPageSize();
        String keyword = qry.getKeyword();
        
        if (pageNum == null || pageNum < 1) {
            pageNum = 1;
        }
        
        if (pageSize == null || pageSize < 1) {
            pageSize = 10;
        }
        
        // 执行查询
        List<Document> documents = documentGateway.listByPage(keyword, pageNum, pageSize);
        long total = documentGateway.count(keyword);
        
        // 转换结果
        List<DocDTO> docDTOs = convertToDTOs(documents);
        
        log.info("文档列表查询完成，共 {} 条记录", total);
        return PageResponse.of(docDTOs, pageSize, pageNum, (int)total);
    }
    
    /**
     * 批量转换为DTO
     * @param documents 文档实体列表
     * @return 文档DTO列表
     */
    private List<DocDTO> convertToDTOs(List<Document> documents) {
        if (documents == null || documents.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<DocDTO> dtos = new ArrayList<>(documents.size());
        for (Document document : documents) {
            DocDTO dto = new DocDTO();
            dto.setId(document.getId());
            dto.setTitle(document.getTitle());
            dto.setOssUrl(document.getOssUrl());
            dto.setFileType(document.getFileType());
            dto.setVersion(document.getVersion());
            dto.setCreatedBy(document.getCreatedBy());
            dto.setCreatedAt(document.getCreatedAt());
            dto.setUpdatedAt(document.getUpdatedAt());
            dtos.add(dto);
        }
        
        return dtos;
    }
} 