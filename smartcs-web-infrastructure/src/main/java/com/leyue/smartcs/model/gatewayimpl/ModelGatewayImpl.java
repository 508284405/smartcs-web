package com.leyue.smartcs.model.gatewayimpl;

import com.alibaba.cola.dto.PageResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leyue.smartcs.domain.model.Model;
import com.leyue.smartcs.domain.model.enums.ModelStatus;
import com.leyue.smartcs.domain.model.enums.ModelType;
import com.leyue.smartcs.domain.model.gateway.ModelGateway;
import com.leyue.smartcs.model.convertor.ModelConvertor;
import com.leyue.smartcs.model.dataobject.ModelDO;
import com.leyue.smartcs.model.mapper.ModelMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 模型实例Gateway实现
 */
@Component
@RequiredArgsConstructor
public class ModelGatewayImpl implements ModelGateway {
    
    private final ModelMapper modelMapper;
    private final ModelConvertor modelConvertor;
    
    @Override
    public Long createModel(Model model) {
        ModelDO modelDO = modelConvertor.toDO(model);
        modelMapper.insert(modelDO);
        return modelDO.getId();
    }
    
    @Override
    public boolean updateModel(Model model) {
        ModelDO modelDO = modelConvertor.toDO(model);
        int result = modelMapper.updateById(modelDO);
        return result > 0;
    }
    
    @Override
    public Optional<Model> findById(Long id) {
        LambdaQueryWrapper<ModelDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ModelDO::getId, id)
               .eq(ModelDO::getIsDeleted, 0);
        
        ModelDO modelDO = modelMapper.selectOne(wrapper);
        if (modelDO == null) {
            return Optional.empty();
        }
        
        Model model = modelConvertor.toDomain(modelDO);
        return Optional.of(model);
    }
    
    
    @Override
    public boolean deleteById(Long id) {
        LambdaUpdateWrapper<ModelDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(ModelDO::getId, id)
               .set(ModelDO::getIsDeleted, 1)
               .set(ModelDO::getUpdatedAt, System.currentTimeMillis());
        
        int result = modelMapper.update(null, wrapper);
        return result > 0;
    }
    
    @Override
    public List<Model> findAll() {
        LambdaQueryWrapper<ModelDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ModelDO::getIsDeleted, 0)
               .orderByDesc(ModelDO::getCreatedAt);
        
        List<ModelDO> modelDOs = modelMapper.selectList(wrapper);
        return modelDOs.stream()
                .map(modelConvertor::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Model> findByProviderId(Long providerId) {
        List<ModelDO> modelDOs = modelMapper.selectByProviderId(providerId);
        return modelDOs.stream()
                .map(modelConvertor::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public PageResponse<Model> pageQuery(int pageIndex, int pageSize, Long providerId, List<ModelType> modelTypes, ModelStatus status) {
        LambdaQueryWrapper<ModelDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ModelDO::getIsDeleted, 0);
        
        if (providerId != null) {
            wrapper.eq(ModelDO::getProviderId, providerId);
        }
        
        if (modelTypes != null && !modelTypes.isEmpty()) {
            List<String> typeCodes = modelTypes.stream()
                .map(ModelType::getCode)
                .collect(Collectors.toList());
            wrapper.and(w -> {
                for (String typeCode : typeCodes) {
                    w.or().like(ModelDO::getModelType, typeCode);
                }
            });
        }
        
        if (status != null) {
            wrapper.eq(ModelDO::getStatus, status.getCode());
        }
        
        wrapper.orderByDesc(ModelDO::getCreatedAt);
        
        Page<ModelDO> page = new Page<>(pageIndex, pageSize);
        Page<ModelDO> result = modelMapper.selectPage(page, wrapper);
        
        List<Model> models = result.getRecords().stream()
                .map(modelConvertor::toDomain)
                .collect(Collectors.toList());
        
        return PageResponse.of(models, (int) result.getTotal(), pageSize, pageIndex);
    }
    
    @Override
    public boolean updateStatus(Long id, ModelStatus status) {
        int result = modelMapper.updateStatus(id, status.getCode());
        return result > 0;
    }
    
    
    @Override
    public List<Model> findActiveByModelTypes(List<ModelType> modelTypes) {
        if (modelTypes == null || modelTypes.isEmpty()) {
            return List.of();
        }
        
        LambdaQueryWrapper<ModelDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ModelDO::getIsDeleted, 0)
               .eq(ModelDO::getStatus, ModelStatus.ACTIVE.getCode())
               .eq(ModelDO::getDeprecated, false);
        
        // 模型类型查询（支持多选）
        List<String> typeCodes = modelTypes.stream()
            .map(ModelType::getCode)
            .collect(Collectors.toList());
        wrapper.and(w -> {
            for (String typeCode : typeCodes) {
                w.or().like(ModelDO::getModelType, typeCode);
            }
        });
        
        wrapper.orderByDesc(ModelDO::getCreatedAt);
        
        List<ModelDO> modelDOs = modelMapper.selectList(wrapper);
        return modelDOs.stream()
                .map(modelConvertor::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Model> findByFeature(String feature) {
        List<ModelDO> modelDOs = modelMapper.selectByFeature(feature);
        return modelDOs.stream()
                .map(modelConvertor::toDomain)
                .collect(Collectors.toList());
    }
}