package com.leyue.smartcs.domain.model.domainservice;

import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.domain.common.gateway.IdGeneratorGateway;
import com.leyue.smartcs.domain.model.Model;
import com.leyue.smartcs.domain.model.enums.ModelStatus;
import com.leyue.smartcs.domain.model.gateway.ModelGateway;
import com.leyue.smartcs.domain.model.gateway.ProviderGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 模型实例领域服务
 */
@Service
@RequiredArgsConstructor
public class ModelDomainService {
    
    private final ModelGateway modelGateway;
    private final ProviderGateway providerGateway;
    private final IdGeneratorGateway idGeneratorGateway;
    
    /**
     * 创建模型实例
     */
    public Model createModel(Model model) {
        // 验证提供商是否存在
        if (providerGateway.findById(model.getProviderId()).isEmpty()) {
            throw new BizException("模型提供商不存在: " + model.getProviderId());
        }
        
        
        // 生成ID和设置默认值
        model.setId(idGeneratorGateway.generateId());
        model.setIsDeleted(0);
        
        // 设置默认值
        if (model.getDeprecated() == null) {
            model.setDeprecated(false);
        }
        if (model.getStatus() == null) {
            model.setStatus(ModelStatus.ACTIVE);
        }
        if (model.getLoadBalancingEnabled() == null) {
            model.setLoadBalancingEnabled(false);
        }
        
        long currentTime = System.currentTimeMillis();
        model.setCreatedAt(currentTime);
        model.setUpdatedAt(currentTime);
        
        // 保存
        Long modelId = modelGateway.createModel(model);
        model.setId(modelId);
        
        return model;
    }
    
    /**
     * 更新模型实例
     */
    public Model updateModel(Model model) {
        // 检查模型是否存在
        Optional<Model> existingOpt = modelGateway.findById(model.getId());
        if (!existingOpt.isPresent()) {
            throw new BizException("模型实例不存在: " + model.getId());
        }
        
        Model existing = existingOpt.get();
        if (existing.isDeleted()) {
            throw new BizException("模型实例已删除，无法更新: " + model.getId());
        }
        
        // 验证提供商是否存在
        if (providerGateway.findById(model.getProviderId()).isEmpty()) {
            throw new BizException("模型提供商不存在: " + model.getProviderId());
        }
        
        
        // 更新时间
        model.setUpdatedAt(System.currentTimeMillis());
        
        // 保持创建信息不变
        model.setCreatedAt(existing.getCreatedAt());
        model.setCreatedBy(existing.getCreatedBy());
        model.setIsDeleted(existing.getIsDeleted());
        
        // 更新
        boolean success = modelGateway.updateModel(model);
        if (!success) {
            throw new BizException("更新模型实例失败");
        }
        
        return model;
    }
    
    /**
     * 删除模型实例
     */
    public Model deleteModel(Long modelId) {
        // 检查模型是否存在
        Optional<Model> existingOpt = modelGateway.findById(modelId);
        if (!existingOpt.isPresent()) {
            throw new BizException("模型实例不存在: " + modelId);
        }
        
        Model existing = existingOpt.get();
        if (existing.isDeleted()) {
            throw new BizException("模型实例已删除: " + modelId);
        }
        
        // 执行逻辑删除
        boolean success = modelGateway.deleteById(modelId);
        if (!success) {
            throw new BizException("删除模型实例失败");
        }
        
        return existing;
    }
    
    /**
     * 启用/禁用模型实例
     */
    public boolean enableModel(Long modelId, ModelStatus status) {
        // 检查模型是否存在
        Optional<Model> existingOpt = modelGateway.findById(modelId);
        if (existingOpt.isEmpty()) {
            throw new BizException("模型实例不存在: " + modelId);
        }
        
        Model existing = existingOpt.get();
        if (existing.isDeleted()) {
            throw new BizException("模型实例已删除: " + modelId);
        }
        
        // 更新状态
        boolean success = modelGateway.updateStatus(modelId, status);
        if (!success) {
            throw new BizException("更新模型状态失败");
        }
        
        return true;
    }
}