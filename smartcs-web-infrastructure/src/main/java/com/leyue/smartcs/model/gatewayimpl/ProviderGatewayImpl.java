package com.leyue.smartcs.model.gatewayimpl;

import com.alibaba.cola.dto.PageResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leyue.smartcs.domain.model.Provider;
import com.leyue.smartcs.domain.model.enums.ProviderType;
import com.leyue.smartcs.domain.model.gateway.ProviderGateway;
import com.leyue.smartcs.model.convertor.ProviderConvertor;
import com.leyue.smartcs.model.dataobject.ProviderDO;
import com.leyue.smartcs.model.mapper.ProviderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 模型提供商Gateway实现
 */
@Component
@RequiredArgsConstructor
public class ProviderGatewayImpl implements ProviderGateway {
    
    private final ProviderMapper providerMapper;
    private final ProviderConvertor providerConvertor;
    
    @Override
    public Long createProvider(Provider provider) {
        ProviderDO providerDO = providerConvertor.toDOWithEncryption(provider, null);
        providerMapper.insert(providerDO);
        return providerDO.getId();
    }
    
    @Override
    public boolean updateProvider(Provider provider) {
        // 获取现有数据以保留未更改的加密字段
        ProviderDO existingDO = null;
        if (provider.getId() != null) {
            existingDO = providerMapper.selectById(provider.getId());
        }
        
        ProviderDO providerDO = providerConvertor.toDOWithEncryption(provider, existingDO);
        int result = providerMapper.updateById(providerDO);
        return result > 0;
    }
    
    @Override
    public Optional<Provider> findById(Long id) {
        LambdaQueryWrapper<ProviderDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProviderDO::getId, id)
               .eq(ProviderDO::getIsDeleted, 0);
        
        ProviderDO providerDO = providerMapper.selectOne(wrapper);
        if (providerDO == null) {
            return Optional.empty();
        }
        
        Provider provider = providerConvertor.toDomain(providerDO);
        return Optional.of(provider);
    }
    
    @Override
    public Optional<Provider> findByProviderType(ProviderType providerType) {
        ProviderDO providerDO = providerMapper.selectByProviderType(providerType);
        if (providerDO == null) {
            return Optional.empty();
        }
        
        Provider provider = providerConvertor.toDomain(providerDO);
        return Optional.of(provider);
    }
    
    @Override
    public boolean deleteById(Long id) {
        LambdaUpdateWrapper<ProviderDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(ProviderDO::getId, id)
               .set(ProviderDO::getIsDeleted, 1)
               .set(ProviderDO::getUpdatedAt, System.currentTimeMillis());
        
        int result = providerMapper.update(null, wrapper);
        return result > 0;
    }
    
    @Override
    public List<Provider> findAll() {
        LambdaQueryWrapper<ProviderDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProviderDO::getIsDeleted, 0)
               .orderByDesc(ProviderDO::getCreatedAt);
        
        List<ProviderDO> providerDOs = providerMapper.selectList(wrapper);
        return providerDOs.stream()
                .map(providerConvertor::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public PageResponse<Provider> pageQuery(int pageIndex, int pageSize, String label) {
        LambdaQueryWrapper<ProviderDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProviderDO::getIsDeleted, 0);
        
        if (StringUtils.hasText(label)) {
            wrapper.like(ProviderDO::getLabel, label);
        }
        
        wrapper.orderByDesc(ProviderDO::getCreatedAt);
        
        Page<ProviderDO> page = new Page<>(pageIndex, pageSize);
        Page<ProviderDO> result = providerMapper.selectPage(page, wrapper);
        
        List<Provider> providers = result.getRecords().stream()
                .map(providerConvertor::toDomain)
                .collect(Collectors.toList());
        
        return PageResponse.of(providers, (int) result.getTotal(), pageSize, pageIndex);
    }
    
    @Override
    public boolean existsByProviderType(ProviderType providerType, Long excludeId) {
        int count = providerMapper.countByProviderType(providerType, excludeId);
        return count > 0;
    }
    
    @Override
    public PageResponse<Provider> pageVisualProviders(int pageIndex, int pageSize, String label) {
        LambdaQueryWrapper<ProviderDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProviderDO::getIsDeleted, 0)
               .like(ProviderDO::getSupportedModelTypes, "chat"); // 视觉模型通常是支持图像输入的chat模型
        
        if (StringUtils.hasText(label)) {
            wrapper.like(ProviderDO::getLabel, label);
        }
        
        wrapper.orderByDesc(ProviderDO::getCreatedAt);
        
        Page<ProviderDO> page = new Page<>(pageIndex, pageSize);
        Page<ProviderDO> result = providerMapper.selectPage(page, wrapper);
        
        List<Provider> providers = result.getRecords().stream()
                .map(providerConvertor::toDomain)
                .collect(Collectors.toList());
        
        return PageResponse.of(providers, (int) result.getTotal(), pageSize, pageIndex);
    }
}