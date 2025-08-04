package com.leyue.smartcs.knowledge.executor.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

import com.alibaba.cola.dto.MultiResponse;
import com.leyue.smartcs.domain.knowledge.gateway.ChunkGateway;
import com.leyue.smartcs.dto.knowledge.EmbeddingWithScore;
import com.leyue.smartcs.dto.knowledge.KnowledgeSearchQry;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingStore;
import com.leyue.smartcs.model.ai.DynamicModelManager;
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
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final DynamicModelManager dynamicModelManager;

    /**
     * 执行文本检索查询
     *
     * @param qry 查询条件
     * @return 检索结果
     */
    public MultiResponse<EmbeddingWithScore> execute(KnowledgeSearchQry qry) {
        try {
            // 获取嵌入模型
            EmbeddingModel embeddingModel = dynamicModelManager.getEmbeddingModel(qry.getModelId());

            // 生成查询向量
            dev.langchain4j.data.embedding.Embedding queryEmbedding = embeddingModel.embed(qry.getKeyword()).content();

            // 执行向量搜索
            EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                    .queryEmbedding(queryEmbedding)
                    .maxResults(10)
                    // .minScore(0.5)
                    .build();
            List<EmbeddingMatch<TextSegment>> matches = embeddingStore.search(searchRequest).matches();
            if (matches == null || matches.isEmpty()) {
                return MultiResponse.of(Collections.emptyList());
            }

            // 查询详情
            List<EmbeddingWithScore> embeddingResults = new ArrayList<>();
            for (EmbeddingMatch<TextSegment> match : matches) {
                try {
                    // 简化处理：直接使用匹配的分数，暂时不关联具体的chunk
                    // 在实际实现中，需要根据存储结构来获取chunk信息
                    EmbeddingWithScore resultItem = new EmbeddingWithScore();
                    resultItem.setChunk(null); // 暂时设为null
                    resultItem.setScore(match.score().floatValue());

                    embeddingResults.add(resultItem);
                } catch (Exception e) {
                    log.warn("处理搜索结果失败", e);
                }
            }

            if (embeddingResults.isEmpty()) {
                return MultiResponse.of(Collections.emptyList());
            }

            // 组装结果
            return MultiResponse.of(embeddingResults);
        } catch (Exception e) {
            log.error("文本检索查询失败", e);
            return MultiResponse.of(Collections.emptyList());
        }
    }
}