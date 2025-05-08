package com.leyue.smartcs.domain.common.gateway;

/**
 * 分布式ID生成器网关接口
 * 提供获取分布式唯一ID的能力
 */
public interface IdGeneratorGateway {
    
    /**
     * 获取单个分布式唯一ID
     * 
     * @return 分布式唯一ID
     */
    Long generateId();
    
    /**
     * 批量获取分布式唯一ID
     * 
     * @param batchSize 批量大小
     * @return 分布式唯一ID数组
     */
    Long[] generateBatchIds(int batchSize);

    /** 获取分布式唯一ID，19位字符串*/
    String generateIdStr();
} 