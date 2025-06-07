package com.leyue.smartcs.knowledge.executor.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
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

@Component
@RequiredArgsConstructor
public class FaqSearchQryExe {

    private final FaqGateway faqGateway;
    private final SearchGateway searchGateway;
    private final FaqConvertor faqConvertor;
    private final VectorStore vectorStore;

    public MultiResponse<FaqDTO> execute(FaqSearchQry qry) {
        return searchFaq(qry.getKeyword(), qry.getK());
    }

    /**
     * 搜索FAQ
     *
     * @param keyword 关键词
     * @param k       数量限制
     * @return FAQ结果
     */
    private MultiResponse<FaqDTO> searchFaq(String keyword, int k) {
        // 调用全文检索查询FAQ
        SearchRequest searchRequest = SearchRequest.builder()
                .query(keyword)
                .topK(k)
                .build();
        List<Document> documents = vectorStore.similaritySearch(searchRequest);
        if(documents==null||documents.isEmpty()){
            return MultiResponse.of(Collections.emptyList());
        }
        List<SearchResultsDTO> searchResults = documents.stream()
                .map(document -> SearchResultsDTO.builder()
                        .type("FAQ")
                        .chunkId(Long.parseLong(document.getId()))
                        .chunk(document.getText())
                        .score(document.getScore())
                        .build())
                .collect(Collectors.toList());
        Set<Long> faqIds = searchResults.stream().map(SearchResultsDTO::getChunkId).collect(Collectors.toSet());
        List<Faq> faqs = faqGateway.findByIds(faqIds);
        Map<Long, Faq> faqMap = faqs.stream().collect(Collectors.toMap(Faq::getId, Function.identity()));
        List<FaqDTO> faqDTOs = searchResults.stream().map(result -> {
            Faq faq = faqMap.get(result.getChunkId());
            if(faq!=null){
                FaqDTO faqDTO = faqConvertor.toDTO(faq);
                faqDTO.setScore(result.getScore());
                return faqDTO;
            }
            return null;
        }).collect(Collectors.toList());
        return MultiResponse.of(faqDTOs);
    }

}
