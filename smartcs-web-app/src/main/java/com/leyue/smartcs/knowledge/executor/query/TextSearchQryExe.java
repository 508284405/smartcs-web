package com.leyue.smartcs.knowledge.executor.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import com.alibaba.cola.dto.MultiResponse;
import com.leyue.smartcs.domain.knowledge.Chunk;
import com.leyue.smartcs.domain.knowledge.gateway.ChunkGateway;
import com.leyue.smartcs.dto.knowledge.ChunkDTO;
import com.leyue.smartcs.dto.knowledge.EmbeddingWithScore;
import com.leyue.smartcs.dto.knowledge.KnowledgeSearchQry;
import com.leyue.smartcs.knowledge.convertor.ChunkConvertor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 文本检索查询执行器
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TextSearchQryExe {
    private final ChunkGateway chunkGateway;
    private final ChunkConvertor chunkConvertor;
    private final VectorStore vectorStore;

    /**
     * 执行文本检索查询
     *
     * @param qry 查询条件
     * @return 检索结果
     */
    public MultiResponse<EmbeddingWithScore> execute(KnowledgeSearchQry qry) {
        // 调用全文检索查询文档段落
        List<Document> documents = vectorStore.similaritySearch(qry.getKeyword());
        if (documents == null || documents.isEmpty()) {
            return MultiResponse.of(Collections.emptyList());
        }

        // 查询详情
        List<EmbeddingWithScore> embeddingResults = new ArrayList<>();
        for (Document doc : documents) {
            Long chunkId = Long.parseLong(doc.getId());
            Double score = doc.getScore() != null ? doc.getScore() : 0.0;

            Chunk chunk = chunkGateway.findById(chunkId);
            if (chunk == null) {
                continue;
            }

            ChunkDTO chunkDTO = chunkConvertor.toDTO(chunk);

            EmbeddingWithScore resultItem = new EmbeddingWithScore();
            resultItem.setChunk(chunkDTO);
            resultItem.setScore(score.floatValue());

            embeddingResults.add(resultItem);
        }

        if (embeddingResults.isEmpty()) {
            return MultiResponse.of(Collections.emptyList());
        }

        // 组装结果
        return MultiResponse.of(embeddingResults);
    }
}