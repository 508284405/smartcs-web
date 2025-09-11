package com.leyue.smartcs.web.model;

import com.alibaba.cola.dto.PageResponse;
import com.alibaba.cola.dto.SingleResponse;
import com.leyue.smartcs.api.ProviderService;
import com.leyue.smartcs.dto.model.ProviderDTO;
import com.leyue.smartcs.dto.model.ProviderPageQry;
import com.leyue.smartcs.dto.model.VisualModelProviderQry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 客户端模型提供商控制器
 */
@RestController
@RequestMapping("/api/model/provider")
@RequiredArgsConstructor
@Slf4j
public class ProviderController {
    
    private final ProviderService providerService;
    
    /**
     * 分页查询模型提供商列表
     */
    @GetMapping("/page")
    public PageResponse<ProviderDTO> pageProviders(ProviderPageQry qry) {
        return providerService.pageProviders(qry);
    }
    
    /**
     * 获取模型提供商详情
     */
    @GetMapping("/{id}")
    public SingleResponse<ProviderDTO> getProvider(@PathVariable("id") Long id) {
        return providerService.getProvider(id);
    }
    
    /**
     * 分页查询支持视觉识别的模型提供商列表
     */
    @GetMapping("/visual")
    public PageResponse<ProviderDTO> pageVisualProviders(VisualModelProviderQry qry) {
        return providerService.pageVisualProviders(qry);
    }
}