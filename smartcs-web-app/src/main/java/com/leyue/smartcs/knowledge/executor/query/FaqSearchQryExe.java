package com.leyue.smartcs.knowledge.executor.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.alibaba.cola.dto.MultiResponse;
import com.leyue.smartcs.domain.common.Constants;
import com.leyue.smartcs.domain.knowledge.Faq;
import com.leyue.smartcs.domain.knowledge.gateway.FaqGateway;
import com.leyue.smartcs.domain.knowledge.gateway.SearchGateway;
import com.leyue.smartcs.dto.knowledge.FaqDTO;
import com.leyue.smartcs.dto.knowledge.FaqSearchQry;
import com.leyue.smartcs.knowledge.convertor.FaqConvertor;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class FaqSearchQryExe {

    private final FaqGateway faqGateway;
    private final SearchGateway searchGateway;
    private final FaqConvertor faqConvertor;

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
        Map<Long, Double> faqSearchResults = searchGateway.searchTopK(Constants.FAQ_INDEX_REDISEARCH, keyword, k, null,
                null);

        // 没有结果则返回null
        if (faqSearchResults == null || faqSearchResults.isEmpty()) {
            return MultiResponse.of(new ArrayList<>(0));
        }
        // 批量获取FAQ详情
        List<Faq> faqs = faqGateway.findByIds(faqSearchResults.keySet());
        Map<Long, Faq> faqMap = faqs.stream().collect(Collectors.toMap(Faq::getId, Function.identity()));

        // 查询详情
        List<FaqDTO> faqResults = new ArrayList<>();
        for (Map.Entry<Long, Double> entry : faqSearchResults.entrySet()) {
            Long faqId = entry.getKey();
            Faq faq = faqMap.get(faqId);
            if (faq == null) {
                continue;
            }
            FaqDTO faqDTO = faqConvertor.toDTO(faq);
            faqDTO.setScore(entry.getValue());
            faqResults.add(faqDTO);

            // 更新命中次数
            faqGateway.incrementHitCount(faqId);
        }

        if (faqResults.isEmpty()) {
            return MultiResponse.of(new ArrayList<>(0));
        }

        return MultiResponse.of(faqResults);
    }

}
