package com.leyue.smartcs.model.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leyue.smartcs.model.dataobject.ProviderDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 模型提供商Mapper接口
 */
@Mapper
public interface ProviderMapper extends BaseMapper<ProviderDO> {
    
    /**
     * 根据提供商标识查询（不包含已删除的）
     * @param providerKey 提供商标识
     * @return 提供商信息
     */
    ProviderDO selectByProviderKey(@Param("providerKey") String providerKey);
    
    /**
     * 检查提供商标识是否已存在（不包含已删除的）
     * @param providerKey 提供商标识
     * @param excludeId 排除的ID
     * @return 是否存在
     */
    int countByProviderKey(@Param("providerKey") String providerKey, @Param("excludeId") Long excludeId);
}