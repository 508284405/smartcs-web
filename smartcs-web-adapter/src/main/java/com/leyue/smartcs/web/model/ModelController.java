package com.leyue.smartcs.web.model;

import com.alibaba.cola.dto.PageResponse;
import com.alibaba.cola.dto.SingleResponse;
import com.leyue.smartcs.api.ModelService;
import com.leyue.smartcs.dto.model.*;
import com.leyue.smartcs.dto.common.SingleClientObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

/**
 * 客户端模型实例控制器
 */
@RestController
@RequestMapping("/api/model")
@RequiredArgsConstructor
@Slf4j
public class ModelController {
    
    private final ModelService modelService;
    
    /**
     * 分页查询模型实例列表
     */
    @GetMapping("/page")
    public PageResponse<ModelDTO> pageModels(ModelPageQry qry) {
        return modelService.pageModels(qry);
    }
    
    /**
     * 获取模型实例详情
     */
    @GetMapping("/{id}")
    public SingleResponse<ModelDTO> getModel(@PathVariable("id") Long id) {
        return modelService.getModel(id);
    }
    
    // ======== 模型推理接口 ========
    
    /**
     * 模型推理（同步）
     */
    @PostMapping("/{id}/infer")
    public SingleResponse<ModelInferResponse> infer(@PathVariable("id") Long modelId, 
                                                   @Valid @RequestBody ModelInferRequest request) {
        log.info("收到模型推理请求: modelId={}, request={}", modelId, request);
        request.setModelId(modelId);
        return modelService.infer(request);
    }
    
    /**
     * 模型推理（异步）
     */
    @PostMapping("/{id}/infer/async")
    public SingleResponse<String> inferAsync(@PathVariable("id") Long modelId, 
                                           @Valid @RequestBody ModelInferRequest request) {
        log.info("收到异步模型推理请求: modelId={}, request={}", modelId, request);
        request.setModelId(modelId);
        return modelService.inferAsync(request);
    }
    
    // ======== 模型任务管理接口 ========
    
    /**
     * 获取任务状态
     */
    @GetMapping("/task/{taskId}")
    public SingleResponse<ModelTaskDTO> getTask(@PathVariable("taskId") String taskId) {
        log.info("获取任务状态: taskId={}", taskId);
        return modelService.getTask(taskId);
    }
    
    // ======== 模型上下文管理接口 ========
    
    /**
     * 获取模型上下文
     */
    @GetMapping("/{id}/context/{sessionId}")
    public SingleResponse<ModelContextDTO> getContext(@PathVariable("id") Long modelId,
                                                     @PathVariable("sessionId") String sessionId) {
        log.info("获取模型上下文: modelId={}, sessionId={}", modelId, sessionId);
        return modelService.getContext(SingleClientObject.of(sessionId));
    }
    
    /**
     * 清除模型上下文
     */
    @DeleteMapping("/{id}/context/{sessionId}")
    public SingleResponse<Boolean> clearContext(@PathVariable("id") Long modelId,
                                              @PathVariable("sessionId") String sessionId) {
        log.info("清除模型上下文: modelId={}, sessionId={}", modelId, sessionId);
        return modelService.clearContext(SingleClientObject.of(sessionId));
    }
    
    // ======== Prompt模板管理接口 ========
    
    /**
     * 创建Prompt模板
     */
    @PostMapping("/prompt-template")
    public SingleResponse<ModelPromptTemplateDTO> createPromptTemplate(@Valid @RequestBody ModelPromptTemplateCreateCmd cmd) {
        log.info("创建Prompt模板: {}", cmd);
        return modelService.createPromptTemplate(cmd);
    }
    
    /**
     * 获取Prompt模板
     */
    @GetMapping("/prompt-template/{id}")
    public SingleResponse<ModelPromptTemplateDTO> getPromptTemplate(@PathVariable("id") Long id) {
        log.info("获取Prompt模板: id={}", id);
        return modelService.getPromptTemplate(id);
    }
    
    /**
     * 根据模板键获取Prompt模板
     */
    @GetMapping("/prompt-template/key/{templateKey}")
    public SingleResponse<ModelPromptTemplateDTO> getPromptTemplateByKey(@PathVariable("templateKey") String templateKey) {
        log.info("根据模板键获取Prompt模板: templateKey={}", templateKey);
        return modelService.getPromptTemplateByKey(templateKey);
    }
}