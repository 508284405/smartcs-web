package com.leyue.smartcs.web.model;

import com.alibaba.cola.dto.PageResponse;
import com.alibaba.cola.dto.SingleResponse;
import com.leyue.smartcs.api.ModelService;
import com.leyue.smartcs.dto.model.ModelDTO;
import com.leyue.smartcs.dto.model.ModelPageQry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

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
}