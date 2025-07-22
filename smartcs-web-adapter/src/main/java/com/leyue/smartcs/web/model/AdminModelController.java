package com.leyue.smartcs.web.model;

import com.alibaba.cola.dto.PageResponse;
import com.alibaba.cola.dto.SingleResponse;
import com.leyue.smartcs.api.ModelService;
import com.leyue.smartcs.dto.model.ModelCreateCmd;
import com.leyue.smartcs.dto.model.ModelDTO;
import com.leyue.smartcs.dto.model.ModelDeleteCmd;
import com.leyue.smartcs.dto.model.ModelEnableCmd;
import com.leyue.smartcs.dto.model.ModelPageQry;
import com.leyue.smartcs.dto.model.ModelUpdateCmd;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端模型实例控制器
 */
@RestController
@RequestMapping("/api/admin/model")
@RequiredArgsConstructor
@Slf4j
public class AdminModelController {
    
    private final ModelService modelService;
    
    /**
     * 创建模型实例
     */
    @PostMapping
    public SingleResponse<ModelDTO> createModel(@RequestBody @Valid ModelCreateCmd cmd) {
        return modelService.createModel(cmd);
    }
    
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
    
    /**
     * 更新模型实例
     */
    @PutMapping
    public SingleResponse<ModelDTO> updateModel(@RequestBody @Valid ModelUpdateCmd cmd) {
        return modelService.updateModel(cmd);
    }
    
    /**
     * 删除模型实例
     */
    @DeleteMapping("/{id}")
    public SingleResponse<Boolean> deleteModel(@PathVariable("id") Long id) {
        ModelDeleteCmd cmd = new ModelDeleteCmd();
        cmd.setId(id);
        return modelService.deleteModel(cmd);
    }
    
    /**
     * 启用/禁用模型实例
     */
    @PatchMapping("/{id}/enable")
    public SingleResponse<Boolean> enableModel(@PathVariable("id") Long id, @RequestBody @Valid ModelEnableCmd cmd) {
        cmd.setId(id);
        return modelService.enableModel(cmd);
    }
}