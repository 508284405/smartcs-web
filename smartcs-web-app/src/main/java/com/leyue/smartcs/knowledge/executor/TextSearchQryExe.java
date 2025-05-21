package com.leyue.smartcs.knowledge.executor;

import com.alibaba.cola.dto.MultiResponse;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.domain.bot.gateway.LLMGateway;
import com.leyue.smartcs.domain.common.Constants;
import com.leyue.smartcs.domain.knowledge.gateway.*;
import com.leyue.smartcs.domain.knowledge.model.Document;
import com.leyue.smartcs.domain.knowledge.model.Embedding;
import com.leyue.smartcs.domain.knowledge.model.Faq;
import com.leyue.smartcs.dto.knowledge.EmbeddingDTO;
import com.leyue.smartcs.dto.knowledge.FaqDTO;
import com.leyue.smartcs.dto.knowledge.KnowledgeSearchQry;
import com.leyue.smartcs.dto.knowledge.KnowledgeSearchResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.leyue.smartcs.domain.common.Constants.FAQ_INDEX_REDISEARCH;

/**
 * 文本检索查询执行器
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TextSearchQryExe {

    private final TextSearchGateway textSearchGateway;
    private final VectorSearchGateway vectorSearchGateway;
    private final FaqGateway faqGateway;
    private final EmbeddingGateway embeddingGateway;
    private final DocumentGateway documentGateway;
    private final LLMGateway llmGateway;

    /**
     * 执行文本检索查询
     *
     * @param qry 查询条件
     * @return 检索结果
     */
    public MultiResponse<KnowledgeSearchResult> execute(KnowledgeSearchQry qry) {
        log.info("执行文本检索查询: {}", qry);

        // 参数校验
        if (qry.getKeyword() == null || qry.getKeyword().trim().isEmpty()) {
            throw new BizException("关键词不能为空");
        }

        String keyword = qry.getKeyword().trim();
        Integer k = qry.getK();

        if (k == null || k < 1) {
            k = 5;
        }

        List<KnowledgeSearchResult> results = new ArrayList<>();

        try {
            // 1. 搜索FAQ
            KnowledgeSearchResult faqResult = searchFaq(keyword, k);
            if (faqResult != null && faqResult.getFaqResults() != null && !faqResult.getFaqResults().isEmpty()) {
                results.add(faqResult);
            }

            // 2. 搜索文档段落
            KnowledgeSearchResult docResult = searchDocEmbeddings(keyword, k);
            if (docResult != null && docResult.getEmbeddingResults() != null && !docResult.getEmbeddingResults().isEmpty()) {
                results.add(docResult);
            }

            log.info("文本检索查询完成，共 {} 组结果", results.size());

            // embeddingResults 按照分数排序
//            results.forEach(result -> result.getEmbeddingResults().sort((o1, o2) -> o2.getScore().compareTo(o1.getScore())));
            return MultiResponse.of(results);

        } catch (Exception e) {
            log.error("文本检索查询失败: {}", e.getMessage(), e);
            throw new BizException("文本检索查询失败: " + e.getMessage());
        }
    }

    /**
     * 搜索FAQ
     *
     * @param keyword 关键词
     * @param k       数量限制
     * @return FAQ结果
     */
    private KnowledgeSearchResult searchFaq(String keyword, int k) {
        // 调用全文检索查询FAQ
        Map<Long, Double> faqSearchResults = textSearchGateway.searchByKeyword(FAQ_INDEX_REDISEARCH, keyword, k);

        // 没有结果则返回null
        if (faqSearchResults == null || faqSearchResults.isEmpty()) {
            return null;
        }

        // 查询详情
        List<FaqDTO> faqResults = new ArrayList<>();
        for (Map.Entry<Long, Double> entry : faqSearchResults.entrySet()) {
            Long faqId = entry.getKey();
            Optional<Faq> faqOpt = faqGateway.findById(faqId);
            if (faqOpt.isEmpty()) {
                continue;
            }

            Faq faq = faqOpt.get();
            FaqDTO faqDTO = convertToDTO(faq);
            faqDTO.setScore(entry.getValue());
            faqResults.add(faqDTO);

            // 更新命中次数
            faqGateway.incrementHitCount(faqId);
        }

        if (faqResults.isEmpty()) {
            return null;
        }

        // 组装结果
        KnowledgeSearchResult result = new KnowledgeSearchResult();
        result.setResultType("FAQ");
        result.setFaqResults(faqResults);

        return result;
    }

    /**
     * 搜索文档段落
     *
     * @param keyword 关键词
     * @param k       数量限制
     * @return 段落结果
     */
    private KnowledgeSearchResult searchDocEmbeddings(String keyword, int k) {
        // 解析向量
        // 调用LLM服务生成向量
        List<float[]> vectors = llmGateway.generateEmbeddings(Collections.singletonList(keyword));

        // 调用全文检索查询文档段落
        Map<Long, Double> embSearchResults = vectorSearchGateway.searchTopK(Constants.UMBEDDING_INDEX_REDISEARCH, vectors.get(0), k);

        // 没有结果则返回null
        if (embSearchResults == null || embSearchResults.isEmpty()) {
            return null;
        }

        // 查询详情
        List<KnowledgeSearchResult.EmbeddingWithScore> embeddingResults = new ArrayList<>();
        for (Map.Entry<Long, Double> entry : embSearchResults.entrySet()) {
            Long embId = entry.getKey();
            Double score = entry.getValue();

            Optional<Embedding> embOpt = embeddingGateway.findById(embId);
            if (embOpt.isEmpty()) {
                continue;
            }

            Embedding embedding = embOpt.get();
            EmbeddingDTO embeddingDTO = convertToDTO(embedding);

            // 获取文档标题
            String docTitle = "";
            Optional<Document> docOpt = documentGateway.findById(embedding.getDocId());
            if (docOpt.isPresent()) {
                docTitle = docOpt.get().getTitle();
            }

            KnowledgeSearchResult.EmbeddingWithScore resultItem = new KnowledgeSearchResult.EmbeddingWithScore();
            resultItem.setEmbedding(embeddingDTO);
            resultItem.setScore(score.floatValue());
            resultItem.setDocTitle(docTitle);

            embeddingResults.add(resultItem);
        }

        if (embeddingResults.isEmpty()) {
            return null;
        }

        // 组装结果
        KnowledgeSearchResult result = new KnowledgeSearchResult();
        result.setResultType("DOC");
        result.setEmbeddingResults(embeddingResults);

        return result;
    }

    /**
     * 将Faq转换为DTO
     *
     * @param faq FAQ实体
     * @return FAQ DTO
     */
    private FaqDTO convertToDTO(Faq faq) {
        if (faq == null) {
            return null;
        }

        FaqDTO dto = new FaqDTO();
        dto.setId(faq.getId());
        dto.setQuestion(faq.getQuestion());
        dto.setAnswer(faq.getAnswer());
        dto.setHitCount(faq.getHitCount());
        dto.setVersion(faq.getVersion());
        dto.setCreatedAt(faq.getCreatedAt());
        dto.setUpdatedAt(faq.getUpdatedAt());

        return dto;
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
        dto.setVector(embedding.getVector());

        dto.setCreatedAt(embedding.getCreatedAt());
        dto.setUpdatedAt(embedding.getUpdatedAt());

        return dto;
    }
} 