package com.leyue.smartcs.domain.intent.gateway;

import com.alibaba.cola.dto.PageResponse;
import com.leyue.smartcs.domain.intent.entity.Intent;
import com.leyue.smartcs.domain.intent.enums.IntentStatus;

import java.util.List;

/**
 * 意图Gateway接口
 * 
 * @author Claude
 */
public interface IntentGateway {
    
    /**
     * 保存意图
     * @param intent 意图对象
     * @return 保存后的意图对象
     */
    Intent save(Intent intent);
    
    /**
     * 更新意图
     * @param intent 意图对象
     */
    void update(Intent intent);
    
    /**
     * 根据ID查找意图
     * @param id 意图ID
     * @return 意图对象
     */
    Intent findById(Long id);
    
    /**
     * 根据编码查找意图
     * @param code 意图编码
     * @return 意图对象
     */
    Intent findByCode(String code);
    
    /**
     * 根据目录ID查找意图列表
     * @param catalogId 目录ID
     * @return 意图列表
     */
    List<Intent> findByCatalogId(Long catalogId);
    
    /**
     * 根据状态查找意图列表
     * @param status 意图状态
     * @return 意图列表
     */
    List<Intent> findByStatus(IntentStatus status);
    
    /**
     * 分页查询意图
     * @param catalogId 目录ID
     * @param status 状态
     * @param keyword 关键词
     * @param pageNum 页码
     * @param pageSize 页大小
     * @return 分页结果
     */
    PageResponse<Intent> findByPage(Long catalogId, IntentStatus status, String keyword, int pageNum, int pageSize);
    
    /**
     * 删除意图
     * @param id 意图ID
     */
    void deleteById(Long id);
}