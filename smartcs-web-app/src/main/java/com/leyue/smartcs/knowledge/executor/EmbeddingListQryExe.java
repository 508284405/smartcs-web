package com.leyue.smartcs.knowledge.executor;

import com.alibaba.cola.dto.MultiResponse;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.domain.knowledge.gateway.DocumentGateway;
import com.leyue.smartcs.domain.knowledge.gateway.EmbeddingGateway;
import com.leyue.smartcs.domain.knowledge.model.Embedding;
import com.leyue.smartcs.dto.common.SingleClientObject;
import com.leyue.smartcs.dto.knowledge.EmbeddingDTO;
import com.leyue.smartcs.knowledge.convertor.EmbeddingConvertor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * 向量列表查询执行器
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EmbeddingListQryExe {
    
    private final EmbeddingGateway embeddingGateway;
    private final DocumentGateway documentGateway;
    private final EmbeddingConvertor embeddingConvertor;
    
    /**
     * 执行向量列表查询
     * @param cmd 文档ID
     * @return 向量列表
     */
    public MultiResponse<EmbeddingDTO> execute(SingleClientObject<Long> cmd) {
        // 参数校验
        if (cmd.getValue() == null) {
            throw new BizException("文档ID不能为空");
        }
        
        Long docId = cmd.getValue();
        
        // 检查文档是否存在
        if (documentGateway.findById(docId).isEmpty()) {
            throw new BizException("文档不存在，ID: " + docId);
        }
        
        // 执行查询
        List<Embedding> embeddings = embeddingGateway.findByDocId(docId);
        return MultiResponse.of(embeddingConvertor.toDTOList(embeddings));
    }
    
    /**
     * 批量转换为DTO
     * @param embeddings 向量实体列表
     * @return 向量DTO列表
     */
    private List<EmbeddingDTO> convertToDTOs(List<Embedding> embeddings) {
        if (embeddings == null || embeddings.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<EmbeddingDTO> dtos = new ArrayList<>(embeddings.size());
        for (Embedding embedding : embeddings) {
            EmbeddingDTO dto = new EmbeddingDTO();
            dto.setId(embedding.getId());
            dto.setDocId(embedding.getDocId());
            dto.setSectionIdx(embedding.getSectionIdx());
            dto.setContentSnip(embedding.getContentSnip());
            dto.setVector(embedding.getVector());
            dto.setCreatedAt(embedding.getCreatedAt());
            dto.setUpdatedAt(embedding.getUpdatedAt());
            dtos.add(dto);
        }
        
        return dtos;
    }
} 