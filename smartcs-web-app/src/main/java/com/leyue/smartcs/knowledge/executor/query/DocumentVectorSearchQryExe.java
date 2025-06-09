package com.leyue.smartcs.knowledge.executor.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import com.alibaba.cola.dto.MultiResponse;
import com.leyue.smartcs.domain.knowledge.Chunk;
import com.leyue.smartcs.domain.knowledge.gateway.ChunkGateway;
import com.leyue.smartcs.dto.knowledge.DocumentSearchRequest;
import com.leyue.smartcs.dto.knowledge.DocumentSearchResultDTO;
import com.leyue.smartcs.knowledge.convertor.ChunkConverter;
import com.leyue.smartcs.knowledge.convertor.DocumentSearchConvertor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 文档向量搜索查询执行器
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DocumentVectorSearchQryExe {

    private final VectorStore vectorStore;
    private final ChunkGateway chunkGateway;
    private final DocumentSearchConvertor documentSearchConvertor;
    private final ChunkConverter chunkConverter;

    /**
     * 执行文档向量搜索
     *
     * @param request 搜索请求
     * @return 搜索结果
     */
    public MultiResponse<DocumentSearchResultDTO> execute(DocumentSearchRequest request) {
        log.info("执行文档向量搜索，查询: {}, topK: {}", request.getQuery(), request.getTopK());

        try {
            // 构建搜索请求
            SearchRequest searchRequest = SearchRequest.builder()
                    .query(request.getQuery())
                    .topK(request.getTopK())
                    .similarityThreshold(request.getSimilarityThreshold())
                    .build();

            // 执行向量搜索
            List<Document> documents = vectorStore.similaritySearch(searchRequest);
            if (documents == null || documents.isEmpty()) {
                log.info("向量搜索未找到匹配结果");
                return MultiResponse.of(Collections.emptyList());
            }

            // 转换搜索结果
            List<DocumentSearchResultDTO> results = new ArrayList<>();
            for (Document doc : documents) {
                try {
                    // 如果指定了contentId，需要过滤结果
                    if (request.getContentId() != null) {
                        String chunkId = doc.getId();
                        Chunk chunk = chunkGateway.findByChunkId(chunkId);
                        if (chunk == null || !request.getContentId().equals(chunk.getContentId())) {
                            continue;
                        }
                    }

                    DocumentSearchResultDTO result = documentSearchConvertor.toDTO(doc);
                    String chunkId = doc.getId();
                    Chunk chunk = chunkGateway.findByChunkId(chunkId);
                    if (chunk != null) {
                        result.setChunkInfo(chunkConverter.toDTO(chunk));
                    }

                    results.add(result);
                } catch (Exception e) {
                    log.warn("转换搜索结果失败，文档ID: {}", doc.getId(), e);
                }
            }

            log.info("文档向量搜索完成，返回 {} 条结果", results.size());
            return MultiResponse.of(results);

        } catch (Exception e) {
            log.error("执行文档向量搜索失败", e);
            return MultiResponse.of(Collections.emptyList());
        }
    }
}