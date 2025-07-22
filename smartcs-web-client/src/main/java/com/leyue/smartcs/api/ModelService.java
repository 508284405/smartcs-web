package com.leyue.smartcs.api;

import com.alibaba.cola.dto.PageResponse;
import com.alibaba.cola.dto.SingleResponse;
import com.leyue.smartcs.dto.model.ModelCreateCmd;
import com.leyue.smartcs.dto.model.ModelUpdateCmd;
import com.leyue.smartcs.dto.model.ModelDeleteCmd;
import com.leyue.smartcs.dto.model.ModelPageQry;
import com.leyue.smartcs.dto.model.ModelEnableCmd;
import com.leyue.smartcs.dto.model.ModelDTO;

/**
 * 模型实例管理服务接口
 */
public interface ModelService {
    
    /**
     * 创建模型实例
     * @param cmd 创建命令
     * @return 模型DTO
     */
    SingleResponse<ModelDTO> createModel(ModelCreateCmd cmd);
    
    /**
     * 更新模型实例
     * @param cmd 更新命令
     * @return 模型DTO
     */
    SingleResponse<ModelDTO> updateModel(ModelUpdateCmd cmd);
    
    /**
     * 删除模型实例
     * @param cmd 删除命令
     * @return 删除结果
     */
    SingleResponse<Boolean> deleteModel(ModelDeleteCmd cmd);
    
    /**
     * 获取模型实例详情
     * @param id 模型ID
     * @return 模型DTO
     */
    SingleResponse<ModelDTO> getModel(Long id);
    
    /**
     * 分页查询模型实例列表
     * @param qry 查询参数
     * @return 分页结果
     */
    PageResponse<ModelDTO> pageModels(ModelPageQry qry);
    
    /**
     * 启用/禁用模型实例
     * @param cmd 启用禁用命令
     * @return 操作结果
     */
    SingleResponse<Boolean> enableModel(ModelEnableCmd cmd);
}