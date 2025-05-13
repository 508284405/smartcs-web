package com.leyue.smartcs.domain.bot.gateway;

import com.leyue.smartcs.domain.bot.model.PromptTemplate;

import java.util.List;
import java.util.Optional;

/**
 * Prompt模板网关接口
 */
public interface PromptTemplateGateway {
    
    /**
     * 保存Prompt模板
     * @param promptTemplate Prompt模板
     * @return 保存后的Prompt模板
     */
    PromptTemplate save(PromptTemplate promptTemplate);
    
    /**
     * 根据ID查询Prompt模板
     * @param id Prompt模板ID
     * @return Prompt模板
     */
    Optional<PromptTemplate> findById(Long id);
    
    /**
     * 根据模板标识查询Prompt模板
     * @param templateKey 模板标识
     * @return Prompt模板
     */
    Optional<PromptTemplate> findByTemplateKey(String templateKey);
    
    /**
     * 查询所有Prompt模板
     * @return Prompt模板列表
     */
    List<PromptTemplate> findAll();
    
    /**
     * 删除Prompt模板
     * @param id Prompt模板ID
     * @return 是否成功
     */
    boolean delete(Long id);
} 