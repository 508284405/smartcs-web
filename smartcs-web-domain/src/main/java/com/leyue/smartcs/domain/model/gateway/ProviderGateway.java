package com.leyue.smartcs.domain.model.gateway;

import com.alibaba.cola.dto.PageResponse;
import com.leyue.smartcs.domain.model.Provider;

import java.util.List;
import java.util.Optional;

/**
 * 模型提供商Gateway接口
 */
public interface ProviderGateway {
    
    /**
     * 创建模型提供商
     * @param provider 提供商信息
     * @return 提供商ID
     */
    Long createProvider(Provider provider);
    
    /**
     * 更新模型提供商
     * @param provider 提供商信息
     * @return 是否更新成功
     */
    boolean updateProvider(Provider provider);
    
    /**
     * 根据ID查询模型提供商
     * @param id 提供商ID
     * @return 提供商信息
     */
    Optional<Provider> findById(Long id);
    
    /**
     * 根据提供商标识查询
     * @param providerKey 提供商唯一标识
     * @return 提供商信息
     */
    Optional<Provider> findByProviderKey(String providerKey);
    
    /**
     * 根据ID删除模型提供商（逻辑删除）
     * @param id 提供商ID
     * @return 是否删除成功
     */
    boolean deleteById(Long id);
    
    /**
     * 查询所有模型提供商
     * @return 提供商列表
     */
    List<Provider> findAll();
    
    /**
     * 分页查询模型提供商
     * @param pageIndex 页码
     * @param pageSize 页大小
     * @param label 提供商名称（可选）
     * @return 分页结果
     */
    PageResponse<Provider> pageQuery(int pageIndex, int pageSize, String label);
    
    /**
     * 检查提供商标识是否已存在
     * @param providerKey 提供商标识
     * @param excludeId 排除的提供商ID（用于更新时检查）
     * @return 是否存在
     */
    boolean existsByProviderKey(String providerKey, Long excludeId);
}