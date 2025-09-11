package com.leyue.smartcs.web.model;

import com.alibaba.cola.dto.PageResponse;
import com.alibaba.cola.dto.SingleResponse;
import com.leyue.smartcs.api.ProviderService;
import com.leyue.smartcs.dto.model.ProviderCreateCmd;
import com.leyue.smartcs.dto.model.ProviderDTO;
import com.leyue.smartcs.dto.model.ProviderDeleteCmd;
import com.leyue.smartcs.dto.model.ProviderPageQry;
import com.leyue.smartcs.dto.model.ProviderUpdateCmd;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端模型提供商控制器
 */
@RestController
@RequestMapping("/api/admin/model/provider")
@RequiredArgsConstructor
@Slf4j
public class AdminProviderController {
    
    private final ProviderService providerService;
    
    /**
     * 创建模型提供商
     */
    @PostMapping
    public SingleResponse<ProviderDTO> createProvider(@RequestBody @Valid ProviderCreateCmd cmd) {
        return providerService.createProvider(cmd);
    }
    
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
     * 更新模型提供商
     */
    @PutMapping
    public SingleResponse<ProviderDTO> updateProvider(@RequestBody @Valid ProviderUpdateCmd cmd) {
        return providerService.updateProvider(cmd);
    }
    
    /**
     * 删除模型提供商
     */
    @DeleteMapping("/{id}")
    public SingleResponse<Boolean> deleteProvider(@PathVariable("id") Long id) {
        ProviderDeleteCmd cmd = new ProviderDeleteCmd();
        cmd.setId(id);
        return providerService.deleteProvider(cmd);
    }
}