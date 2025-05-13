package com.leyue.smartcs.bot.gateway.impl;

import com.leyue.smartcs.domain.bot.gateway.KnowledgeGateway;
import com.leyue.smartcs.knowledge.api.KnowledgeService;
import com.leyue.smartcs.knowledge.dto.KnowledgeSearchQry;
import com.leyue.smartcs.knowledge.dto.KnowledgeSearchResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 知识网关实现
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class KnowledgeGatewayImpl implements KnowledgeGateway {
    
    private final KnowledgeService knowledgeService;
    
    @Override
    public List<Map<String, Object>> searchByVector(String vector, int k, float threshold) {
        try {
            log.info("向量检索: vector长度={}, k={}, threshold={}", vector.length(), k, threshold);
            
            KnowledgeSearchQry qry = new KnowledgeSearchQry();
            qry.setVector(vector);
            qry.setK(k);
            qry.setThreshold(threshold);
            
            List<KnowledgeSearchResult> results = knowledgeService.searchByVector(qry).getData();
            return convertResults(results);
        } catch (Exception e) {
            log.error("向量检索失败: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public List<Map<String, Object>> searchByText(String keyword, int k) {
        try {
            log.info("文本检索: keyword={}, k={}", keyword, k);
            
            KnowledgeSearchQry qry = new KnowledgeSearchQry();
            qry.setKeyword(keyword);
            qry.setK(k);
            
            List<KnowledgeSearchResult> results = knowledgeService.searchByText(qry).getData();
            return convertResults(results);
        } catch (Exception e) {
            log.error("文本检索失败: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public Map<String, Object> getFaq(Long id) {
        try {
            // 实际应用中，可以通过KnowledgeService获取FAQ详情
            // 这里简单返回空Map
            return new HashMap<>();
        } catch (Exception e) {
            log.error("获取FAQ失败: {}", e.getMessage(), e);
            return new HashMap<>();
        }
    }
    
    @Override
    public Map<String, Object> getDocSection(Long docId, Integer sectionIdx) {
        try {
            // 实际应用中，可以通过KnowledgeService获取文档段落详情
            // 这里简单返回空Map
            return new HashMap<>();
        } catch (Exception e) {
            log.error("获取文档段落失败: {}", e.getMessage(), e);
            return new HashMap<>();
        }
    }
    
    /**
     * 转换检索结果
     * @param results 原始结果
     * @return 转换后的结果
     */
    private List<Map<String, Object>> convertResults(List<KnowledgeSearchResult> results) {
        List<Map<String, Object>> convertedResults = new ArrayList<>();
        
        if (results == null || results.isEmpty()) {
            return convertedResults;
        }
        
        for (KnowledgeSearchResult result : results) {
            if ("FAQ".equals(result.getResultType()) && result.getFaqResults() != null) {
                for (var faq : result.getFaqResults()) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("type", "FAQ");
                    item.put("id", faq.getId());
                    item.put("question", faq.getQuestion());
                    item.put("answer", faq.getAnswer());
                    item.put("score", 1.0f); // FAQ默认分数
                    convertedResults.add(item);
                }
            } else if ("DOC".equals(result.getResultType()) && result.getEmbeddingResults() != null) {
                for (var embedding : result.getEmbeddingResults()) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("type", "DOC");
                    item.put("id", embedding.getEmbedding().getId());
                    item.put("docId", embedding.getEmbedding().getDocId());
                    item.put("docTitle", embedding.getDocTitle());
                    item.put("sectionIdx", embedding.getEmbedding().getSectionIdx());
                    item.put("content", embedding.getEmbedding().getContentSnip());
                    item.put("score", embedding.getScore());
                    convertedResults.add(item);
                }
            }
        }
        
        return convertedResults;
    }
} 