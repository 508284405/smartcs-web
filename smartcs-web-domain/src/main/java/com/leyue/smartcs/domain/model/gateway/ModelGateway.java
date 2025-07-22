package com.leyue.smartcs.domain.model.gateway;

import com.alibaba.cola.dto.PageResponse;
import com.leyue.smartcs.domain.model.Model;
import com.leyue.smartcs.domain.model.enums.ModelStatus;
import com.leyue.smartcs.domain.model.enums.ModelType;

import java.util.List;
import java.util.Optional;

/**
 * 模型实例Gateway接口
 */
public interface ModelGateway {
    
    /**
     * 创建模型实例
     * @param model 模型信息
     * @return 模型ID
     */
    Long createModel(Model model);
    
    /**
     * 更新模型实例
     * @param model 模型信息
     * @return 是否更新成功
     */
    boolean updateModel(Model model);
    
    /**
     * 根据ID查询模型实例
     * @param id 模型ID
     * @return 模型信息
     */
    Optional<Model> findById(Long id);
    
    /**
     * 根据提供商ID和模型标识查询
     * @param providerId 提供商ID
     * @param modelKey 模型标识
     * @return 模型信息
     */
    Optional<Model> findByProviderIdAndModelKey(Long providerId, String modelKey);
    
    /**
     * 根据ID删除模型实例（逻辑删除）
     * @param id 模型ID
     * @return 是否删除成功
     */
    boolean deleteById(Long id);
    
    /**
     * 查询所有模型实例
     * @return 模型列表
     */
    List<Model> findAll();
    
    /**
     * 根据提供商ID查询模型实例
     * @param providerId 提供商ID
     * @return 模型列表
     */
    List<Model> findByProviderId(Long providerId);
    
    /**
     * 分页查询模型实例
     * @param pageIndex 页码
     * @param pageSize 页大小
     * @param providerId 提供商ID（可选）
     * @param modelType 模型类型（可选）
     * @param status 状态（可选）
     * @return 分页结果
     */
    PageResponse<Model> pageQuery(int pageIndex, int pageSize, Long providerId, ModelType modelType, ModelStatus status);
    
    /**
     * 启用/禁用模型实例
     * @param id 模型ID
     * @param status 状态
     * @return 是否成功
     */
    boolean updateStatus(Long id, ModelStatus status);
    
    /**
     * 检查模型标识在指定提供商下是否已存在
     * @param providerId 提供商ID
     * @param modelKey 模型标识
     * @param excludeId 排除的模型ID（用于更新时检查）
     * @return 是否存在
     */
    boolean existsByProviderIdAndModelKey(Long providerId, String modelKey, Long excludeId);
    
    /**
     * 根据模型类型查询活跃的模型实例
     * @param modelType 模型类型
     * @return 模型列表
     */
    List<Model> findActiveByModelType(ModelType modelType);
    
    /**
     * 根据特性查询模型实例
     * @param feature 特性
     * @return 模型列表
     */
    List<Model> findByFeature(String feature);
}