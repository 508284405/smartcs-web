package com.leyue.smartcs.model.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leyue.smartcs.domain.model.enums.ProviderType;
import com.leyue.smartcs.model.dataobject.ProviderDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 模型提供商Mapper接口
 */
@Mapper
public interface ProviderMapper extends BaseMapper<ProviderDO> {
    
    /**
     * 根据提供商类型查询（不包含已删除的）
     * @param providerType 提供商类型
     * @return 提供商信息
     */
    ProviderDO selectByProviderType(@Param("providerType") ProviderType providerType);
    
    /**
     * 检查提供商类型是否已存在（不包含已删除的）
     * @param providerType 提供商类型
     * @param excludeId 排除的ID
     * @return 是否存在
     */
    int countByProviderType(@Param("providerType") ProviderType providerType, @Param("excludeId") Long excludeId);
    
    /**
     * 查询需要API Key迁移的提供商
     * 条件：有明文api_key但没有加密存储的记录
     * @return 需要迁移的提供商列表
     */
    List<ProviderDO> selectByApiKeyToMigrate();
    
    /**
     * 查询已加密API Key的提供商
     * @return 已加密的提供商列表
     */
    List<ProviderDO> selectByEncryptedApiKey();
}