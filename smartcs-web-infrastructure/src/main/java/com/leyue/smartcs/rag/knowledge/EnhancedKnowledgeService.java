package com.leyue.smartcs.rag.knowledge;

import com.leyue.smartcs.rag.knowledge.model.KnowledgeQuery;
import com.leyue.smartcs.rag.knowledge.model.KnowledgeResult;
import com.leyue.smartcs.rag.knowledge.model.MultiKnowledgeQuery;

import java.util.List;

/**
 * 增强知识库服务接口
 * 支持多知识库联合查询和高级检索策略
 */
public interface EnhancedKnowledgeService {

    /**
     * 单知识库查询
     * 
     * @param query 知识查询
     * @return 查询结果
     */
    List<KnowledgeResult> search(KnowledgeQuery query);

    /**
     * 多知识库联合查询
     * 
     * @param query 多知识库查询
     * @return 合并后的查询结果
     */
    List<KnowledgeResult> searchMultiple(MultiKnowledgeQuery query);

    /**
     * 混合查询（向量 + 关键词）
     * 
     * @param query 知识查询
     * @return 查询结果
     */
    List<KnowledgeResult> hybridSearch(KnowledgeQuery query);

    /**
     * 语义查询
     * 
     * @param query 知识查询
     * @return 查询结果
     */
    List<KnowledgeResult> semanticSearch(KnowledgeQuery query);

    /**
     * 关键词查询
     * 
     * @param query 知识查询
     * @return 查询结果
     */
    List<KnowledgeResult> keywordSearch(KnowledgeQuery query);

    /**
     * 相似内容推荐
     * 
     * @param contentId 内容ID
     * @param knowledgeBaseIds 知识库ID列表
     * @param limit 推荐数量
     * @return 相似内容列表
     */
    List<KnowledgeResult> findSimilar(String contentId, List<Long> knowledgeBaseIds, int limit);

    /**
     * 获取知识库统计信息
     * 
     * @param knowledgeBaseIds 知识库ID列表
     * @return 统计信息
     */
    KnowledgeStats getStats(List<Long> knowledgeBaseIds);

    /**
     * 检查知识库是否可用
     * 
     * @param knowledgeBaseId 知识库ID
     * @return 是否可用
     */
    boolean isKnowledgeBaseAvailable(Long knowledgeBaseId);

    /**
     * 获取知识库信息
     * 
     * @param knowledgeBaseIds 知识库ID列表
     * @return 知识库信息列表
     */
    List<KnowledgeBaseInfo> getKnowledgeBaseInfo(List<Long> knowledgeBaseIds);

    /**
     * 知识库统计信息接口
     */
    interface KnowledgeStats {
        int getTotalDocuments();
        int getTotalChunks();
        long getTotalIndexSize();
        double getAverageRelevanceScore();
        long getLastUpdateTime();
    }

    /**
     * 知识库信息接口
     */
    interface KnowledgeBaseInfo {
        Long getId();
        String getName();
        String getDescription();
        String getStatus();
        int getDocumentCount();
        long getCreatedTime();
        long getUpdatedTime();
    }
}