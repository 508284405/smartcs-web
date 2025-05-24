package com.leyue.smartcs.api;

import com.alibaba.cola.dto.MultiResponse;
import com.alibaba.cola.dto.Response;
import com.alibaba.cola.dto.SingleResponse;
import com.leyue.smartcs.dto.knowledge.*;

/**
 * 索引缓存服务接口
 */
public interface IndexCacheService {
    
    // ========== 索引管理相关方法 ==========
    
    /**
     * 创建RediSearch索引
     *
     * @param cmd 创建索引命令
     * @return 操作结果
     */
    Response createIndex(CreateIndexCmd cmd);
    
    /**
     * 获取索引信息
     *
     * @param qry 索引信息查询条件
     * @return 索引信息
     */
    SingleResponse<IndexInfoDTO> getIndexInfo(GetIndexInfoQry qry);
    
    /**
     * 删除索引
     *
     * @param cmd 删除索引命令
     * @return 操作结果
     */
    Response deleteIndex(DeleteIndexCmd cmd);
    
    /**
     * 获取所有RediSearch索引列表
     *
     * @return 索引名称列表
     */
    MultiResponse<String> listIndexes();
    
    // ========== 索引缓存操作相关方法 ==========
    
    /**
     * 清空指定索引下的所有缓存
     *
     * @param cmd 清空缓存命令
     * @return 操作结果
     */
    Response clearIndexCache(ClearIndexCacheCmd cmd);
    
    /**
     * 列出指定索引前缀下的所有缓存键
     *
     * @param qry 查询条件
     * @return 缓存键列表
     */
    MultiResponse<String> listIndexCacheKeys(ListIndexCacheKeysQry qry);
    
    /**
     * 根据键名获取缓存的详细值
     *
     * @param qry 查询条件
     * @return 缓存值详情
     */
    SingleResponse<CacheValueDTO> getCacheValue(GetCacheValueQry qry);
} 