package com.leyue.smartcs.knowledge.executor;

import com.alibaba.cola.dto.MultiResponse;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.domain.knowledge.gateway.DocumentGateway;
import com.leyue.smartcs.domain.knowledge.gateway.EmbeddingGateway;
import com.leyue.smartcs.domain.knowledge.gateway.VectorSearchGateway;
import com.leyue.smartcs.domain.knowledge.model.Document;
import com.leyue.smartcs.domain.knowledge.model.Embedding;
import com.leyue.smartcs.dto.knowledge.EmbeddingDTO;
import com.leyue.smartcs.dto.knowledge.KnowledgeSearchQry;
import com.leyue.smartcs.dto.knowledge.KnowledgeSearchResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

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

    // 向量集合名称（与EmbeddingGatewayImpl中定义的相同）
    private static final String VECTOR_COLLECTION = "embedding_vectors";

    /**
     * 执行向量检索查询
     *
     * @param qry 查询条件
     * @return 检索结果
     */
    public MultiResponse<KnowledgeSearchResult> execute(KnowledgeSearchQry qry) {
        log.info("执行向量检索查询: {}", qry);

        // 参数校验
        if (qry.getVector() == null || qry.getVector().length > 0) {
            throw new BizException("向量不能为空");
        }

        // 解析参数
        byte[] vector = qry.getVector();
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
            // 调用向量检索（使用RedisSearch作为向量库）
            Map<Long, Float> searchResults = vectorSearchGateway.searchTopK(
                    VECTOR_COLLECTION, vector, k, modelType, threshold);

            // 没有结果则返回空
            if (searchResults.isEmpty()) {
                log.info("向量检索查询无结果");
                return MultiResponse.of(new ArrayList<>());
            }

            log.info("向量检索获取到 {} 个结果", searchResults.size());

            // 查询文档段落详情并组装结果
            List<KnowledgeSearchResult.EmbeddingWithScore> embeddingResults = new ArrayList<>();
            for (Map.Entry<Long, Float> entry : searchResults.entrySet()) {
                Long embeddingId = entry.getKey();
                Float score = entry.getValue();

                log.debug("处理向量检索结果: id={}, score={}", embeddingId, score);

                Optional<Embedding> embeddingOpt = embeddingGateway.findById(embeddingId);
                if (embeddingOpt.isEmpty()) {
                    log.warn("未找到对应的Embedding记录: id={}", embeddingId);
                    continue;
                }

                Embedding embedding = embeddingOpt.get();
                EmbeddingDTO embeddingDTO = convertToDTO(embedding);

                // 获取文档标题
                String docTitle = "";
                Optional<Document> docOpt = documentGateway.findById(embedding.getDocId());
                if (docOpt.isPresent()) {
                    docTitle = docOpt.get().getTitle();
                } else {
                    log.warn("未找到对应的文档记录: docId={}", embedding.getDocId());
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
     *
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