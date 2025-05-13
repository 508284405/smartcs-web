package com.leyue.smartcs.domain.bot.gateway;

import java.util.List;
import java.util.Map;

/**
 * 知识网关接口
 */
public interface KnowledgeGateway {
    
    /**
     * 向量检索
     * @param vector 查询向量
     * @param k 返回数量
     * @param threshold 相似度阈值
     * @return 检索结果
     */
    List<Map<String, Object>> searchByVector(String vector, int k, float threshold);
    
    /**
     * 文本检索
     * @param keyword 关键词
     * @param k 返回数量
     * @return 检索结果
     */
    List<Map<String, Object>> searchByText(String keyword, int k);
    
    /**
     * 查询FAQ
     * @param id FAQ ID
     * @return FAQ数据
     */
    Map<String, Object> getFaq(Long id);
    
    /**
     * 查询文档段落
     * @param docId 文档ID
     * @param sectionIdx 段落索引
     * @return 段落数据
     */
    Map<String, Object> getDocSection(Long docId, Integer sectionIdx);
} 