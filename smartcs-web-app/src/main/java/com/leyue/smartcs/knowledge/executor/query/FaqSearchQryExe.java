package com.leyue.smartcs.knowledge.executor.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import com.leyue.smartcs.model.ai.DynamicModelManager;
import org.springframework.stereotype.Component;

import com.alibaba.cola.dto.MultiResponse;
import com.leyue.smartcs.domain.common.Constants;
import com.leyue.smartcs.domain.knowledge.Faq;
import com.leyue.smartcs.domain.knowledge.gateway.FaqGateway;
import com.leyue.smartcs.domain.knowledge.gateway.SearchGateway;
import com.leyue.smartcs.dto.knowledge.FaqDTO;
import com.leyue.smartcs.dto.knowledge.FaqSearchQry;
import com.leyue.smartcs.dto.knowledge.SearchResultsDTO;
import com.leyue.smartcs.knowledge.convertor.FaqConvertor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class FaqSearchQryExe {

    private final FaqGateway faqGateway;
    private final SearchGateway searchGateway;
    private final FaqConvertor faqConvertor;
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final DynamicModelManager dynamicModelManager;

    public MultiResponse<FaqDTO> execute(FaqSearchQry qry) {
        return searchFaq(qry.getKeyword(), qry.getK(), qry.getModelId());
    }

    /**
     * 搜索FAQ
     *
     * @param keyword 关键词
     * @param k       数量限制
     * @param modelId 模型ID
     * @return FAQ结果
     */
    private MultiResponse<FaqDTO> searchFaq(String keyword, int k, Long modelId) {
        try {
            // 获取嵌入模型
            EmbeddingModel embeddingModel = dynamicModelManager.getEmbeddingModel(modelId);

            // 生成查询向量
            dev.langchain4j.data.embedding.Embedding queryEmbedding = embeddingModel.embed(keyword).content();

            // 执行向量搜索
            EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                    .queryEmbedding(queryEmbedding)
                    .maxResults(k)
                    // .minScore(0.5)
                    .build();
            List<EmbeddingMatch<TextSegment>> matches = embeddingStore.search(searchRequest).matches();
            if (matches == null || matches.isEmpty()) {
                return MultiResponse.of(Collections.emptyList());
            }

            List<SearchResultsDTO> searchResults = new ArrayList<>();
            for (EmbeddingMatch<TextSegment> match : matches) {
                SearchResultsDTO result = SearchResultsDTO.builder()
                        .type("FAQ")
                        .chunkId(0L) // 简化处理，暂时设为0
                        .chunk("") // 简化处理，暂时为空
                        .score(match.score())
                        .build();
                searchResults.add(result);
            }

            Set<Long> faqIds = searchResults.stream()
                    .map(SearchResultsDTO::getChunkId)
                    .collect(Collectors.toSet());

            List<Faq> faqs = faqGateway.findByIds(faqIds);
            Map<Long, Faq> faqMap = faqs.stream().collect(Collectors.toMap(Faq::getId, Function.identity()));

            List<FaqDTO> faqDTOs = searchResults.stream()
                    .map(result -> {
                        Faq faq = faqMap.get(result.getChunkId());
                        if (faq != null) {
                            return faqConvertor.toDTO(faq);
                        }
                        return null;
                    })
                    .filter(faqDTO -> faqDTO != null)
                    .collect(Collectors.toList());

            return MultiResponse.of(faqDTOs);

        } catch (Exception e) {
            log.error("FAQ搜索失败", e);
            return MultiResponse.of(Collections.emptyList());
        }
    }
}
