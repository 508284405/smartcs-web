package com.leyue.smartcs.api;

import com.alibaba.cola.dto.PageResponse;
import com.alibaba.cola.dto.SingleResponse;
import com.leyue.smartcs.dto.model.ProviderCreateCmd;
import com.leyue.smartcs.dto.model.ProviderUpdateCmd;
import com.leyue.smartcs.dto.model.ProviderDeleteCmd;
import com.leyue.smartcs.dto.model.ProviderPageQry;
import com.leyue.smartcs.dto.model.ProviderDTO;

/**
 * 模型提供商管理服务接口
 */
public interface ProviderService {
    
    /**
     * 创建模型提供商
     * @param cmd 创建命令
     * @return 提供商DTO
     */
    SingleResponse<ProviderDTO> createProvider(ProviderCreateCmd cmd);
    
    /**
     * 更新模型提供商
     * @param cmd 更新命令
     * @return 提供商DTO
     */
    SingleResponse<ProviderDTO> updateProvider(ProviderUpdateCmd cmd);
    
    /**
     * 删除模型提供商
     * @param cmd 删除命令
     * @return 删除结果
     */
    SingleResponse<Boolean> deleteProvider(ProviderDeleteCmd cmd);
    
    /**
     * 获取模型提供商详情
     * @param id 提供商ID
     * @return 提供商DTO
     */
    SingleResponse<ProviderDTO> getProvider(Long id);
    
    /**
     * 分页查询模型提供商列表
     * @param qry 查询参数
     * @return 分页结果
     */
    PageResponse<ProviderDTO> pageProviders(ProviderPageQry qry);
}