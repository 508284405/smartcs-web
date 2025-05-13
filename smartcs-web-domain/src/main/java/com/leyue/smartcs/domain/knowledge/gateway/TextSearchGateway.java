package com.leyue.smartcs.domain.knowledge.gateway;

import java.util.Map;

/**
 * 全文检索网关接口
 */
public interface TextSearchGateway {
    /**
     * 关键词搜索
     * @param index 索引名称
     * @param keyword 关键词
     * @param k 返回数量
     * @return ID与分数的映射
     */
    Map<Long, Float> searchByKeyword(String index, String keyword, int k);
    
    /**
     * 模糊搜索
     * @param index 索引名称
     * @param text 模糊文本
     * @param field 搜索字段
     * @param k 返回数量
     * @return ID与分数的映射
     */
    Map<Long, Float> searchFuzzy(String index, String text, String field, int k);
    
    /**
     * 创建或更新索引文档
     * @param index 索引名称
     * @param id 文档ID
     * @param source 文档内容
     * @return 是否成功
     */
    boolean indexDocument(String index, Long id, Map<String, Object> source);
    
    /**
     * 删除索引文档
     * @param index 索引名称
     * @param id 文档ID
     * @return 是否成功
     */
    boolean deleteDocument(String index, Long id);
} 