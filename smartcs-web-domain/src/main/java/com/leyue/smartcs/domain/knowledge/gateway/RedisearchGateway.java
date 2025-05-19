package com.leyue.smartcs.domain.knowledge.gateway;

import com.leyue.smartcs.dto.knowledge.IndexInfoDTO;

import java.util.List;
import java.util.Map;

/**
 * Redisearch 操作网关接口
 */
public interface RedisearchGateway {
    
    /**
     * 创建索引
     * 
     * @param indexName 索引名称
     * @param prefix 键前缀
     * @param schema 字段定义
     * @param replaceIfExists 如存在是否替换
     */
    void createIndex(String indexName, String prefix, Map<String, String> schema, boolean replaceIfExists);
    
    /**
     * 获取索引信息
     * 
     * @param indexName 索引名称
     * @return 索引信息
     */
    IndexInfoDTO getIndexInfo(String indexName);
    
    /**
     * 删除索引
     * 
     * @param indexName 索引名称
     * @return 是否删除成功
     */
    boolean deleteIndex(String indexName);

    /**
     * 获取所有索引名称列表
     *
     * @return 索引名称列表
     */
    List<String> listIndexes();
} 