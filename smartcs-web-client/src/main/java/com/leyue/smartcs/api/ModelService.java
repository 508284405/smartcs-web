package com.leyue.smartcs.api;

import com.alibaba.cola.dto.PageResponse;
import com.alibaba.cola.dto.SingleResponse;
import com.leyue.smartcs.dto.model.*;
import com.leyue.smartcs.dto.common.SingleClientObject;

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
    
    // ======== 模型推理相关接口 ========
    
    /**
     * 模型推理（同步）
     * @param request 推理请求
     * @return 推理结果
     */
    SingleResponse<ModelInferResponse> infer(ModelInferRequest request);
    
    /**
     * 模型推理（异步）
     * @param request 推理请求
     * @return 任务ID
     */
    SingleResponse<String> inferAsync(ModelInferRequest request);
    
    // ======== 模型任务管理接口 ========
    
    /**
     * 获取任务状态
     * @param taskId 任务ID
     * @return 任务详情
     */
    SingleResponse<ModelTaskDTO> getTask(String taskId);
    
    // ======== 模型上下文管理接口 ========
    
    /**
     * 获取模型上下文
     * @param sessionId 会话ID
     * @return 上下文信息
     */
    SingleResponse<ModelContextDTO> getContext(SingleClientObject<String> sessionId);
    
    /**
     * 清除模型上下文
     * @param sessionId 会话ID
     * @return 操作结果
     */
    SingleResponse<Boolean> clearContext(SingleClientObject<String> sessionId);
    
    // ======== Prompt模板管理接口 ========
    
    /**
     * 创建Prompt模板
     * @param cmd 创建命令
     * @return 模板DTO
     */
    SingleResponse<ModelPromptTemplateDTO> createPromptTemplate(ModelPromptTemplateCreateCmd cmd);
    
    /**
     * 获取Prompt模板
     * @param id 模板ID
     * @return 模板DTO
     */
    SingleResponse<ModelPromptTemplateDTO> getPromptTemplate(Long id);
    
    /**
     * 根据模板键获取Prompt模板
     * @param templateKey 模板键
     * @return 模板DTO
     */
    SingleResponse<ModelPromptTemplateDTO> getPromptTemplateByKey(String templateKey);
}