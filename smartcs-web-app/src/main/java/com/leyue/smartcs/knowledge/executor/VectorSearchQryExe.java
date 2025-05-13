package com.leyue.smartcs.knowledge.executor;

import com.alibaba.cola.dto.MultiResponse;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.domain.knowledge.gateway.DocumentGateway;
import com.leyue.smartcs.domain.knowledge.gateway.EmbeddingGateway;
import com.leyue.smartcs.domain.knowledge.gateway.VectorSearchGateway;
import com.leyue.smartcs.domain.knowledge.model.Document;
import com.leyue.smartcs.domain.knowledge.model.Embedding;
import com.leyue.smartcs.knowledge.dto.EmbeddingDTO;
import com.leyue.smartcs.knowledge.dto.KnowledgeSearchQry;
import com.leyue.smartcs.knowledge.dto.KnowledgeSearchResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 向量检索查询执行器
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class VectorSearchQryExe {
    
    private final VectorSearchGateway vectorSearchGateway;
    private final EmbeddingGateway embeddingGateway;
    private final DocumentGateway documentGateway;
    
    /**
     * 执行向量检索查询
     * @param qry 查询条件
     * @return 检索结果
     */
    public MultiResponse<KnowledgeSearchResult> execute(KnowledgeSearchQry qry) {
        log.info("执行向量检索查询: {}", qry);
        
        // 参数校验
        if (qry.getVector() == null || qry.getVector().isEmpty()) {
            throw new BizException("向量不能为空");
        }
        
        // 解析参数
        String vector = qry.getVector();
        Integer k = qry.getK();
        String modelType = qry.getModelType();
        Float threshold = qry.getThreshold();
        
        if (k == null || k < 1) {
            k = 5;
        }
        
        if (threshold == null) {
            threshold = 0.7f;
        }
        
        try {
            // 调用向量检索（这里简化为从向量库中检索并按相似度排序）
            byte[] queryVector = Base64.getDecoder().decode(vector);
            Map<Long, Float> searchResults = vectorSearchGateway.searchTopK(
                    "cs_embeddings", queryVector, k, modelType, threshold);
            
            // 没有结果则返回空
            if (searchResults.isEmpty()) {
                log.info("向量检索查询无结果");
                return MultiResponse.of(new ArrayList<>());
            }
            
            // 查询文档段落详情并组装结果
            List<KnowledgeSearchResult.EmbeddingWithScore> embeddingResults = new ArrayList<>();
            for (Map.Entry<Long, Float> entry : searchResults.entrySet()) {
                Long embeddingId = entry.getKey();
                Float score = entry.getValue();
                
                Optional<Embedding> embeddingOpt = embeddingGateway.findById(embeddingId);
                if (embeddingOpt.isEmpty()) {
                    continue;
                }
                
                Embedding embedding = embeddingOpt.get();
                EmbeddingDTO embeddingDTO = convertToDTO(embedding);
                
                // 获取文档标题
                String docTitle = "";
                Optional<Document> docOpt = documentGateway.findById(embedding.getDocId());
                if (docOpt.isPresent()) {
                    docTitle = docOpt.get().getTitle();
                }
                
                KnowledgeSearchResult.EmbeddingWithScore resultItem = new KnowledgeSearchResult.EmbeddingWithScore();
                resultItem.setEmbedding(embeddingDTO);
                resultItem.setScore(score);
                resultItem.setDocTitle(docTitle);
                
                embeddingResults.add(resultItem);
            }
            
            // 组装最终结果
            KnowledgeSearchResult result = new KnowledgeSearchResult();
            result.setResultType("DOC");
            result.setEmbeddingResults(embeddingResults);
            
            List<KnowledgeSearchResult> results = new ArrayList<>();
            results.add(result);
            
            log.info("向量检索查询完成，共 {} 条结果", embeddingResults.size());
            return MultiResponse.of(results);
            
        } catch (Exception e) {
            log.error("向量检索查询失败: {}", e.getMessage(), e);
            throw new BizException("向量检索查询失败: " + e.getMessage());
        }
    }
    
    /**
     * 将Embedding转换为DTO
     * @param embedding 向量实体
     * @return 向量DTO
     */
    private EmbeddingDTO convertToDTO(Embedding embedding) {
        if (embedding == null) {
            return null;
        }
        
        EmbeddingDTO dto = new EmbeddingDTO();
        dto.setId(embedding.getId());
        dto.setDocId(embedding.getDocId());
        dto.setSectionIdx(embedding.getSectionIdx());
        dto.setContentSnip(embedding.getContentSnip());
        
        // 向量数据转Base64
        if (embedding.getVector() != null) {
            if (embedding.getVector() instanceof byte[]) {
                dto.setVector(Base64.getEncoder().encodeToString((byte[]) embedding.getVector()));
            } else if (embedding.getVector() instanceof String) {
                dto.setVector((String) embedding.getVector());
            }
        }
        
        dto.setCreatedAt(embedding.getCreatedAt());
        dto.setUpdatedAt(embedding.getUpdatedAt());
        
        return dto;
    }
} 