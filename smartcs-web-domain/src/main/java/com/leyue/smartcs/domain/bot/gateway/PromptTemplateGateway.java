package com.leyue.smartcs.domain.bot.gateway;

import com.leyue.smartcs.bot.dto.BotPromptTemplatePageQry;
import com.leyue.smartcs.domain.bot.PromptTemplate;
import com.alibaba.cola.dto.PageResponse;

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

    /**
     * 分页查询Prompt模板
     * @param qry 查询条件
     * @return 分页结果
     */
    PageResponse<PromptTemplate> pageQuery(BotPromptTemplatePageQry qry);

    /**
     * 检查模板是否被机器人使用
     * @param templateKey 模板标识
     * @return 是否被使用
     */
    boolean isTemplateInUse(String templateKey);
} 