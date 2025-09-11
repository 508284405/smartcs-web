package com.leyue.smartcs.intent.gateway;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.leyue.smartcs.domain.intent.entity.IntentCatalog;
import com.leyue.smartcs.domain.intent.gateway.IntentCatalogGateway;
import com.leyue.smartcs.intent.convertor.IntentCatalogConvertor;
import com.leyue.smartcs.intent.dataobject.IntentCatalogDO;
import com.leyue.smartcs.intent.mapper.IntentCatalogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 意图目录Gateway实现
 * 
 * @author Claude
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class IntentCatalogGatewayImpl implements IntentCatalogGateway {
    
    private final IntentCatalogMapper catalogMapper;
    private final IntentCatalogConvertor catalogConvertor;
    
    @Override
    public IntentCatalog save(IntentCatalog catalog) {
        IntentCatalogDO catalogDO = catalogConvertor.toDO(catalog);
        catalogMapper.insert(catalogDO);
        return catalogConvertor.toDomain(catalogDO);
    }
    
    @Override
    public void update(IntentCatalog catalog) {
        catalogMapper.updateById(catalogConvertor.toDO(catalog));
    }
    
    @Override
    public IntentCatalog findById(Long id) {
        IntentCatalogDO catalogDO = catalogMapper.selectById(id);
        return catalogDO != null ? catalogConvertor.toDomain(catalogDO) : null;
    }
    
    @Override
    public IntentCatalog findByCode(String code) {
        LambdaQueryWrapper<IntentCatalogDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(IntentCatalogDO::getCode, code);
        IntentCatalogDO catalogDO = catalogMapper.selectOne(wrapper);
        return catalogDO != null ? catalogConvertor.toDomain(catalogDO) : null;
    }
    
    @Override
    public List<IntentCatalog> findAllActive() {
        LambdaQueryWrapper<IntentCatalogDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(IntentCatalogDO::getIsDeleted, 0)
                .orderByAsc(IntentCatalogDO::getSortOrder);
        List<IntentCatalogDO> catalogDOList = catalogMapper.selectList(wrapper);
        List<IntentCatalog> catalogs = catalogConvertor.toDomainList(catalogDOList);
        
        // 为每个目录统计意图数量
        for (IntentCatalog catalog : catalogs) {
            int intentCount = countIntentsByCatalogId(catalog.getId());
            catalog.setIntentCount(intentCount);
        }
        
        return catalogs;
    }
    
    @Override
    public List<IntentCatalog> findByParentId(Long parentId) {
        LambdaQueryWrapper<IntentCatalogDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(IntentCatalogDO::getParentId, parentId)
                .eq(IntentCatalogDO::getIsDeleted, 0)
                .orderByAsc(IntentCatalogDO::getSortOrder);
        List<IntentCatalogDO> catalogDOList = catalogMapper.selectList(wrapper);
        List<IntentCatalog> catalogs = catalogConvertor.toDomainList(catalogDOList);
        
        // 为每个目录统计意图数量
        for (IntentCatalog catalog : catalogs) {
            int intentCount = countIntentsByCatalogId(catalog.getId());
            catalog.setIntentCount(intentCount);
        }
        
        return catalogs;
    }
    
    @Override
    public void deleteById(Long id) {
        catalogMapper.deleteById(id);
    }
    
    /**
     * 统计指定目录下的意图数量
     * @param catalogId 目录ID
     * @return 意图数量
     */
    private int countIntentsByCatalogId(Long catalogId) {
        try {
            return catalogMapper.countIntentsByCatalogId(catalogId);
        } catch (Exception e) {
            log.warn("统计目录{}的意图数量失败: {}", catalogId, e.getMessage());
            return 0;
        }
    }
}