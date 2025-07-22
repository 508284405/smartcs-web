package com.leyue.smartcs.model.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leyue.smartcs.model.dataobject.ModelDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 模型实例Mapper接口
 */
@Mapper
public interface ModelMapper extends BaseMapper<ModelDO> {
    
    /**
     * 根据提供商ID和模型标识查询（不包含已删除的）
     * @param providerId 提供商ID
     * @param modelKey 模型标识
     * @return 模型信息
     */
    ModelDO selectByProviderIdAndModelKey(@Param("providerId") Long providerId, @Param("modelKey") String modelKey);
    
    /**
     * 检查模型标识在指定提供商下是否已存在（不包含已删除的）
     * @param providerId 提供商ID
     * @param modelKey 模型标识
     * @param excludeId 排除的ID
     * @return 是否存在
     */
    int countByProviderIdAndModelKey(@Param("providerId") Long providerId, @Param("modelKey") String modelKey, @Param("excludeId") Long excludeId);
    
    /**
     * 根据提供商ID查询所有模型（不包含已删除的）
     * @param providerId 提供商ID
     * @return 模型列表
     */
    List<ModelDO> selectByProviderId(@Param("providerId") Long providerId);
    
    /**
     * 根据模型类型查询活跃的模型实例
     * @param modelType 模型类型
     * @return 模型列表
     */
    List<ModelDO> selectActiveByModelType(@Param("modelType") String modelType);
    
    /**
     * 根据特性查询模型实例
     * @param feature 特性
     * @return 模型列表
     */
    List<ModelDO> selectByFeature(@Param("feature") String feature);
    
    /**
     * 更新模型状态
     * @param id 模型ID
     * @param status 状态
     * @return 更新条数
     */
    int updateStatus(@Param("id") Long id, @Param("status") String status);
}