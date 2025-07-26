package com.leyue.smartcs.model.gatewayimpl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.leyue.smartcs.domain.model.ModelPromptTemplate;
import com.leyue.smartcs.domain.model.gateway.ModelPromptTemplateGateway;
import com.leyue.smartcs.model.convertor.ModelPromptTemplateConvertor;
import com.leyue.smartcs.model.dataobject.ModelPromptTemplateDO;
import com.leyue.smartcs.model.mapper.ModelPromptTemplateMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 模型Prompt模板Gateway实现
 */
@Component
@RequiredArgsConstructor
public class ModelPromptTemplateGatewayImpl implements ModelPromptTemplateGateway {
    
    private final ModelPromptTemplateMapper templateMapper;
    private final ModelPromptTemplateConvertor templateConvertor;
    
    @Override
    public ModelPromptTemplate save(ModelPromptTemplate template) {
        ModelPromptTemplateDO templateDO = templateConvertor.toDO(template);
        
        if (template.getId() == null) {
            // 新增
            templateDO.setCreatedAt(System.currentTimeMillis());
            templateDO.setUpdatedAt(System.currentTimeMillis());
            templateMapper.insert(templateDO);
            template.setId(templateDO.getId());
        } else {
            // 更新
            templateDO.setUpdatedAt(System.currentTimeMillis());
            templateMapper.updateById(templateDO);
        }
        
        return template;
    }
    
    @Override
    public Optional<ModelPromptTemplate> findById(Long id) {
        LambdaQueryWrapper<ModelPromptTemplateDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ModelPromptTemplateDO::getId, id)
               .eq(ModelPromptTemplateDO::getIsDeleted, 0);
        
        ModelPromptTemplateDO templateDO = templateMapper.selectOne(wrapper);
        if (templateDO == null) {
            return Optional.empty();
        }
        
        ModelPromptTemplate template = templateConvertor.toDomain(templateDO);
        return Optional.of(template);
    }
    
    @Override
    public Optional<ModelPromptTemplate> findByTemplateKey(String templateKey) {
        ModelPromptTemplateDO templateDO = templateMapper.selectByTemplateKey(templateKey);
        if (templateDO == null) {
            return Optional.empty();
        }
        
        ModelPromptTemplate template = templateConvertor.toDomain(templateDO);
        return Optional.of(template);
    }
    
    @Override
    public boolean existsByTemplateKey(String templateKey) {
        int count = templateMapper.countByTemplateKey(templateKey, null);
        return count > 0;
    }
    
    @Override
    public List<ModelPromptTemplate> findByModelType(String modelType) {
        List<ModelPromptTemplateDO> templateDOs = templateMapper.selectActiveByModelType(modelType);
        return templateDOs.stream()
                .map(templateConvertor::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<ModelPromptTemplate> findSystemTemplates() {
        List<ModelPromptTemplateDO> templateDOs = templateMapper.selectSystemTemplates();
        return templateDOs.stream()
                .map(templateConvertor::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<ModelPromptTemplate> findUserTemplates(String createdBy) {
        LambdaQueryWrapper<ModelPromptTemplateDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ModelPromptTemplateDO::getIsDeleted, 0)
               .eq(ModelPromptTemplateDO::getCreatedBy, createdBy)
               .eq(ModelPromptTemplateDO::getIsSystem, false)
               .orderByDesc(ModelPromptTemplateDO::getCreatedAt);
        
        List<ModelPromptTemplateDO> templateDOs = templateMapper.selectList(wrapper);
        return templateDOs.stream()
                .map(templateConvertor::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<ModelPromptTemplate> findActiveTemplates() {
        List<ModelPromptTemplateDO> templateDOs = templateMapper.selectByStatus("ACTIVE");
        return templateDOs.stream()
                .map(templateConvertor::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<ModelPromptTemplate> findWithPagination(int offset, int limit) {
        LambdaQueryWrapper<ModelPromptTemplateDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ModelPromptTemplateDO::getIsDeleted, 0)
               .orderByDesc(ModelPromptTemplateDO::getCreatedAt)
               .last("LIMIT " + offset + ", " + limit);
        
        List<ModelPromptTemplateDO> templateDOs = templateMapper.selectList(wrapper);
        return templateDOs.stream()
                .map(templateConvertor::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public long countTemplates() {
        LambdaQueryWrapper<ModelPromptTemplateDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ModelPromptTemplateDO::getIsDeleted, 0);
        
        return templateMapper.selectCount(wrapper);
    }
    
    @Override
    public boolean update(ModelPromptTemplate template) {
        ModelPromptTemplateDO templateDO = templateConvertor.toDO(template);
        templateDO.setUpdatedAt(System.currentTimeMillis());
        
        int result = templateMapper.updateById(templateDO);
        return result > 0;
    }
    
    @Override
    public boolean activate(Long id) {
        int result = templateMapper.updateStatus(id, "ACTIVE");
        return result > 0;
    }
    
    @Override
    public boolean deactivate(Long id) {
        int result = templateMapper.updateStatus(id, "INACTIVE");
        return result > 0;
    }
    
    @Override
    public boolean deleteById(Long id) {
        LambdaUpdateWrapper<ModelPromptTemplateDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(ModelPromptTemplateDO::getId, id)
               .set(ModelPromptTemplateDO::getIsDeleted, 1)
               .set(ModelPromptTemplateDO::getUpdatedAt, System.currentTimeMillis());
        
        int result = templateMapper.update(null, wrapper);
        return result > 0;
    }
    
    @Override
    public boolean deleteByTemplateKey(String templateKey) {
        LambdaUpdateWrapper<ModelPromptTemplateDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(ModelPromptTemplateDO::getTemplateKey, templateKey)
               .set(ModelPromptTemplateDO::getIsDeleted, 1)
               .set(ModelPromptTemplateDO::getUpdatedAt, System.currentTimeMillis());
        
        int result = templateMapper.update(null, wrapper);
        return result > 0;
    }
    
    @Override
    public int batchDelete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        
        LambdaUpdateWrapper<ModelPromptTemplateDO> wrapper = new LambdaUpdateWrapper<>();
        wrapper.in(ModelPromptTemplateDO::getId, ids)
               .set(ModelPromptTemplateDO::getIsDeleted, 1)
               .set(ModelPromptTemplateDO::getUpdatedAt, System.currentTimeMillis());
        
        return templateMapper.update(null, wrapper);
    }
    
    @Override
    public List<ModelPromptTemplate> searchTemplates(String keyword, String modelType, Boolean isSystem) {
        LambdaQueryWrapper<ModelPromptTemplateDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ModelPromptTemplateDO::getIsDeleted, 0);
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w.like(ModelPromptTemplateDO::getTemplateName, keyword.trim())
                           .or()
                           .like(ModelPromptTemplateDO::getDescription, keyword.trim()));
        }
        
        if (modelType != null && !modelType.trim().isEmpty()) {
            wrapper.like(ModelPromptTemplateDO::getModelTypes, modelType.trim());
        }
        
        if (isSystem != null) {
            wrapper.eq(ModelPromptTemplateDO::getIsSystem, isSystem);
        }
        
        wrapper.orderByDesc(ModelPromptTemplateDO::getCreatedAt);
        
        List<ModelPromptTemplateDO> templateDOs = templateMapper.selectList(wrapper);
        return templateDOs.stream()
                .map(templateConvertor::toDomain)
                .collect(Collectors.toList());
    }
}