package com.leyue.smartcs.domain.model.domainservice;

import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.domain.common.gateway.IdGeneratorGateway;
import com.leyue.smartcs.domain.model.Provider;
import com.leyue.smartcs.domain.model.gateway.ProviderGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 模型提供商领域服务
 */
@Service
@RequiredArgsConstructor
public class ProviderDomainService {
    
    private final ProviderGateway providerGateway;
    private final IdGeneratorGateway idGeneratorGateway;
    
    /**
     * 创建模型提供商
     */
    public Provider createProvider(Provider provider) {
        // 业务验证
        validateProvider(provider);
        
        // 检查提供商标识是否已存在
        if (providerGateway.existsByProviderType(provider.getProviderType(), null)) {
            throw new BizException("提供商标识已存在: " + provider.getProviderType());
        }
        
        // 生成ID和设置默认值
        provider.setId(idGeneratorGateway.generateId());
        provider.setIsDeleted(0);
        
        long currentTime = System.currentTimeMillis();
        provider.setCreatedAt(currentTime);
        provider.setUpdatedAt(currentTime);
        
        // 保存
        Long providerId = providerGateway.createProvider(provider);
        provider.setId(providerId);
        
        return provider;
    }
    
    /**
     * 更新模型提供商
     */
    public Provider updateProvider(Provider provider) {
        // 检查提供商是否存在
        Optional<Provider> existingOpt = providerGateway.findById(provider.getId());
        if (!existingOpt.isPresent()) {
            throw new BizException("模型提供商不存在: " + provider.getId());
        }
        
        Provider existing = existingOpt.get();
        if (existing.isDeleted()) {
            throw new BizException("模型提供商已删除，无法更新: " + provider.getId());
        }
        
        // 业务验证
        validateProvider(provider);
        
        // 检查提供商标识是否已存在（排除自己）
        if (providerGateway.existsByProviderType(provider.getProviderType(), provider.getId())) {
            throw new BizException("提供商标识已存在: " + provider.getProviderType());
        }
        
        // 更新时间
        provider.setUpdatedAt(System.currentTimeMillis());
        
        // 保持创建信息不变
        provider.setCreatedAt(existing.getCreatedAt());
        provider.setCreatedBy(existing.getCreatedBy());
        provider.setIsDeleted(existing.getIsDeleted());
        
        // 更新
        boolean success = providerGateway.updateProvider(provider);
        if (!success) {
            throw new BizException("更新模型提供商失败");
        }
        
        return provider;
    }
    
    /**
     * 删除模型提供商
     */
    public Provider deleteProvider(Long providerId) {
        // 检查提供商是否存在
        Optional<Provider> existingOpt = providerGateway.findById(providerId);
        if (!existingOpt.isPresent()) {
            throw new BizException("模型提供商不存在: " + providerId);
        }
        
        Provider existing = existingOpt.get();
        if (existing.isDeleted()) {
            throw new BizException("模型提供商已删除: " + providerId);
        }
        
        // 执行逻辑删除
        boolean success = providerGateway.deleteById(providerId);
        if (!success) {
            throw new BizException("删除模型提供商失败");
        }
        
        return existing;
    }
    
    /**
     * 验证提供商配置
     */
    private void validateProvider(Provider provider) {
        if (!provider.isValid()) {
            throw new BizException("模型提供商配置参数无效");
        }
        
        // 验证提供商标识长度
        if (provider.getProviderType().getKey().length() > 128) {
            throw new BizException("提供商标识长度不能超过128字符");
        }
        
        // 验证提供商名称长度
        if (provider.getLabel().length() > 128) {
            throw new BizException("提供商名称长度不能超过128字符");
        }
        
        // 验证API Key长度
        if (provider.getApiKey().length() > 256) {
            throw new BizException("API Key长度不能超过256字符");
        }
        
        // 验证图标URL长度
        if (provider.getIconSmall() != null && provider.getIconSmall().length() > 256) {
            throw new BizException("小图标URL长度不能超过256字符");
        }
        
        if (provider.getIconLarge() != null && provider.getIconLarge().length() > 256) {
            throw new BizException("大图标URL长度不能超过256字符");
        }
        
        // 验证Endpoint长度
        if (provider.getEndpoint() != null && provider.getEndpoint().length() > 256) {
            throw new BizException("API Endpoint长度不能超过256字符");
        }
    }
}