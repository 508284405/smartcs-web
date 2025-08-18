package com.leyue.smartcs.domain.intent.gateway;

import com.leyue.smartcs.domain.intent.entity.IntentCatalog;

import java.util.List;

/**
 * 意图目录Gateway接口
 * 
 * @author Claude
 */
public interface IntentCatalogGateway {
    
    /**
     * 保存意图目录
     * @param catalog 意图目录对象
     * @return 保存后的意图目录对象
     */
    IntentCatalog save(IntentCatalog catalog);
    
    /**
     * 更新意图目录
     * @param catalog 意图目录对象
     */
    void update(IntentCatalog catalog);
    
    /**
     * 根据ID查找意图目录
     * @param id 目录ID
     * @return 意图目录对象
     */
    IntentCatalog findById(Long id);
    
    /**
     * 根据编码查找意图目录
     * @param code 目录编码
     * @return 意图目录对象
     */
    IntentCatalog findByCode(String code);
    
    /**
     * 查询所有有效的意图目录
     * @return 意图目录列表
     */
    List<IntentCatalog> findAllActive();
    
    /**
     * 根据父目录ID查询子目录
     * @param parentId 父目录ID
     * @return 子目录列表
     */
    List<IntentCatalog> findByParentId(Long parentId);
    
    /**
     * 删除意图目录
     * @param id 目录ID
     */
    void deleteById(Long id);
}