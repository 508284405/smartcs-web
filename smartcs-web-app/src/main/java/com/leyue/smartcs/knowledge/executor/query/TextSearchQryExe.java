package com.leyue.smartcs.knowledge.executor.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.alibaba.cola.dto.MultiResponse;
import com.leyue.smartcs.domain.bot.gateway.LLMGateway;
import com.leyue.smartcs.domain.common.Constants;
import com.leyue.smartcs.domain.knowledge.Chunk;
import com.leyue.smartcs.domain.knowledge.gateway.ChunkGateway;
import com.leyue.smartcs.domain.knowledge.gateway.ContentGateway;
import com.leyue.smartcs.domain.knowledge.gateway.FaqGateway;
import com.leyue.smartcs.domain.knowledge.gateway.SearchGateway;
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
    private final SearchGateway searchGateway;
    private final ChunkGateway chunkGateway;
    private final ChunkConvertor chunkConvertor;
    /**
     * 执行文本检索查询
     *
     * @param qry 查询条件
     * @return 检索结果
     */
    public MultiResponse<EmbeddingWithScore> execute(KnowledgeSearchQry qry) {
        return searchDocEmbeddings(qry);
    }

    /**
     * 搜索文档段落
     *
     * @param keyword 关键词
     * @param k       数量限制
     * @return 段落结果
     */
    private MultiResponse<EmbeddingWithScore> searchDocEmbeddings(KnowledgeSearchQry qry) {
        // 调用全文检索查询文档段落
        Map<Long, Double> embSearchResults = searchGateway.searchTopK(Constants.EMBEDDING_INDEX_REDISEARCH, qry.getKeyword(), qry.getK(), qry.getKbId(), qry.getContentId());

        // 没有结果则返回null
        if (embSearchResults == null || embSearchResults.isEmpty()) {
            return null;
        }

        // 查询详情
        List<EmbeddingWithScore> embeddingResults = new ArrayList<>();
        for (Map.Entry<Long, Double> entry : embSearchResults.entrySet()) {
            Long chunkId = entry.getKey();
            Double score = entry.getValue();

            Chunk chunk = chunkGateway.findById(chunkId);
            if (chunk == null) {
                continue;
            }

            ChunkDTO chunkDTO = chunkConvertor.toDTO(chunk);

            EmbeddingWithScore resultItem = new EmbeddingWithScore();
            resultItem.setEmbedding(chunkDTO);
            resultItem.setScore(score.floatValue());

            embeddingResults.add(resultItem);
        }

        if (embeddingResults.isEmpty()) {
            return MultiResponse.of(new ArrayList<>(0));
        }

        // 组装结果
        return MultiResponse.of(embeddingResults);
    }
} 