package com.leyue.smartcs.knowledge.executor;

import com.alibaba.cola.dto.MultiResponse;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.domain.knowledge.gateway.DocumentGateway;
import com.leyue.smartcs.domain.knowledge.gateway.EmbeddingGateway;
import com.leyue.smartcs.domain.knowledge.model.Embedding;
import com.leyue.smartcs.dto.common.SingleClientObject;
import com.leyue.smartcs.dto.knowledge.EmbeddingDTO;
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
    
    /**
     * 执行向量列表查询
     * @param cmd 文档ID
     * @return 向量列表
     */
    public MultiResponse<EmbeddingDTO> execute(SingleClientObject<Long> cmd) {
        log.info("执行向量列表查询: {}", cmd);
        
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
        
        // 转换结果
        List<EmbeddingDTO> embeddingDTOs = convertToDTOs(embeddings);
        
        log.info("向量列表查询完成，共 {} 条记录", embeddingDTOs.size());
        return MultiResponse.of(embeddingDTOs);
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
            
            // 转换向量为Base64字符串
            if (embedding.getVector() != null) {
                if (embedding.getVector() instanceof byte[]) {
                    dto.setVector(Base64.getEncoder().encodeToString((byte[]) embedding.getVector()));
                } else if (embedding.getVector() instanceof String) {
                    dto.setVector((String) embedding.getVector());
                }
            }
            
            dto.setCreatedAt(embedding.getCreatedAt());
            dto.setUpdatedAt(embedding.getUpdatedAt());
            dtos.add(dto);
        }
        
        return dtos;
    }
} 