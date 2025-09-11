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
     * 根据提供商ID查询所有模型（不包含已删除的）
     * @param providerId 提供商ID
     * @return 模型列表
     */
    List<ModelDO> selectByProviderId(@Param("providerId") Long providerId);
    
    
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