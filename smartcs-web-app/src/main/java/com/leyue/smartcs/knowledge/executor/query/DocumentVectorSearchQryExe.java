package com.leyue.smartcs.knowledge.executor.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

import com.alibaba.cola.dto.MultiResponse;
import com.leyue.smartcs.config.ModelBeanManagerService;
import com.leyue.smartcs.domain.knowledge.gateway.ChunkGateway;
import com.leyue.smartcs.dto.knowledge.DocumentSearchRequest;
import com.leyue.smartcs.dto.knowledge.DocumentSearchResultDTO;
import com.leyue.smartcs.knowledge.convertor.ChunkConverter;
import com.leyue.smartcs.knowledge.convertor.DocumentSearchConvertor;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 文档向量搜索查询执行器
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DocumentVectorSearchQryExe {

    private final EmbeddingStore<TextSegment> embeddingStore;
    private final ChunkGateway chunkGateway;
    private final DocumentSearchConvertor documentSearchConvertor;
    private final ChunkConverter chunkConverter;
    private final ModelBeanManagerService modelBeanManagerService;

    /**
     * 执行文档向量搜索
     *
     * @param request 搜索请求
     * @return 搜索结果
     */
    public MultiResponse<DocumentSearchResultDTO> execute(DocumentSearchRequest request) {
        log.info("执行文档向量搜索，查询: {}, topK: {}", request.getQuery(), request.getTopK());

        try {
            // 获取嵌入模型
            EmbeddingModel embeddingModel = (EmbeddingModel) modelBeanManagerService.getFirstModelBean();
            if (embeddingModel == null) {
                log.warn("嵌入模型未找到，无法执行向量搜索");
                return MultiResponse.of(Collections.emptyList());
            }

            // 生成查询向量
            dev.langchain4j.data.embedding.Embedding queryEmbedding = embeddingModel.embed(request.getQuery()).content();

            // 执行向量搜索
            List<EmbeddingMatch<TextSegment>> matches = embeddingStore.search(
                EmbeddingSearchRequest.builder()
                    .queryEmbedding(queryEmbedding)
                    .maxResults(request.getTopK())
                    .build()
            ).matches();

            if (matches == null || matches.isEmpty()) {
                log.info("向量搜索未找到匹配结果");
                return MultiResponse.of(Collections.emptyList());
            }

            // 转换搜索结果
            List<DocumentSearchResultDTO> results = new ArrayList<>();
            for (EmbeddingMatch<TextSegment> match : matches) {
                try {
                    // 过滤相似度阈值
                    if (match.score() < request.getSimilarityThreshold()) {
                        continue;
                    }

                    DocumentSearchResultDTO result = new DocumentSearchResultDTO();
                    result.setScore(match.score());

                    // 从匹配的嵌入中获取文档信息
                    if (match.embedded() != null) {
                        // 这里需要根据实际的存储结构来获取文档内容
                        // 简化处理，假设嵌入中包含了文档信息
                        result.setChunkInfo(null); // 暂时设为null，需要根据实际存储结构调整
                    }

                    results.add(result);
                } catch (Exception e) {
                    log.warn("转换搜索结果失败", e);
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