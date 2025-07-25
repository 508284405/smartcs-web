package com.leyue.smartcs.model.serviceimpl;

import com.alibaba.cola.dto.PageResponse;
import com.alibaba.cola.dto.SingleResponse;
import com.leyue.smartcs.api.ModelService;
import com.leyue.smartcs.dto.model.*;
import com.leyue.smartcs.dto.common.SingleClientObject;
import com.leyue.smartcs.model.executor.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 模型实例服务实现
 */
@Service
@RequiredArgsConstructor
public class ModelServiceImpl implements ModelService {
    
    private final ModelCreateCmdExe modelCreateCmdExe;
    private final ModelUpdateCmdExe modelUpdateCmdExe;
    private final ModelDeleteCmdExe modelDeleteCmdExe;
    private final ModelGetQryExe modelGetQryExe;
    private final ModelPageQryExe modelPageQryExe;
    private final ModelEnableCmdExe modelEnableCmdExe;
    
    // 推理相关执行器
    private final ModelInferCmdExe modelInferCmdExe;
    private final ModelInferAsyncCmdExe modelInferAsyncCmdExe;
    
    // 任务管理执行器
    private final ModelTaskQryExe modelTaskQryExe;
    
    // 上下文管理执行器
    private final ModelContextQryExe modelContextQryExe;
    
    // Prompt模板管理执行器
    private final ModelPromptTemplateCreateCmdExe modelPromptTemplateCreateCmdExe;
    private final ModelPromptTemplateQryExe modelPromptTemplateQryExe;
    
    @Override
    public SingleResponse<ModelDTO> createModel(ModelCreateCmd cmd) {
        return modelCreateCmdExe.execute(cmd);
    }
    
    @Override
    public SingleResponse<ModelDTO> updateModel(ModelUpdateCmd cmd) {
        return modelUpdateCmdExe.execute(cmd);
    }
    
    @Override
    public SingleResponse<Boolean> deleteModel(ModelDeleteCmd cmd) {
        return modelDeleteCmdExe.execute(cmd);
    }
    
    @Override
    public SingleResponse<ModelDTO> getModel(Long id) {
        return modelGetQryExe.execute(id);
    }
    
    @Override
    public PageResponse<ModelDTO> pageModels(ModelPageQry qry) {
        return modelPageQryExe.execute(qry);
    }
    
    @Override
    public SingleResponse<Boolean> enableModel(ModelEnableCmd cmd) {
        return modelEnableCmdExe.execute(cmd);
    }
    
    // ======== 模型推理相关接口实现 ========
    
    @Override
    public SingleResponse<ModelInferResponse> infer(ModelInferRequest request) {
        return modelInferCmdExe.execute(request);
    }
    
    @Override
    public SingleResponse<String> inferAsync(ModelInferRequest request) {
        return modelInferAsyncCmdExe.execute(request);
    }
    
    // ======== 模型任务管理接口实现 ========
    
    @Override
    public SingleResponse<ModelTaskDTO> getTask(String taskId) {
        return modelTaskQryExe.execute(taskId);
    }
    
    // ======== 模型上下文管理接口实现 ========
    
    @Override
    public SingleResponse<ModelContextDTO> getContext(SingleClientObject<String> sessionId) {
        return modelContextQryExe.execute(sessionId);
    }
    
    @Override
    public SingleResponse<Boolean> clearContext(SingleClientObject<String> sessionId) {
        return modelContextQryExe.executeClear(sessionId);
    }
    
    // ======== Prompt模板管理接口实现 ========
    
    @Override
    public SingleResponse<ModelPromptTemplateDTO> createPromptTemplate(ModelPromptTemplateCreateCmd cmd) {
        return modelPromptTemplateCreateCmdExe.execute(cmd);
    }
    
    @Override
    public SingleResponse<ModelPromptTemplateDTO> getPromptTemplate(Long id) {
        return modelPromptTemplateQryExe.execute(id);
    }
    
    @Override
    public SingleResponse<ModelPromptTemplateDTO> getPromptTemplateByKey(String templateKey) {
        return modelPromptTemplateQryExe.executeByKey(templateKey);
    }
}