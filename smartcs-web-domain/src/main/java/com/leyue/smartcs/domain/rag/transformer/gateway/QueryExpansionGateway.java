package com.leyue.smartcs.domain.rag.transformer.gateway;

import java.util.List;

/**
 * 查询扩展Gateway接口
 * 定义查询扩展的基础设施依赖
 * 
 * @author Claude
 */
public interface QueryExpansionGateway {
    
    /**
     * 使用LLM生成查询扩展
     * 
     * @param prompt 扩展提示词
     * @param modelId 模型ID
     * @return 生成的文本内容
     */
    String generateExpansion(String prompt, Long modelId);
    
    /**
     * 解析扩展后的查询文本
     * 
     * @param expandedText 扩展后的文本
     * @param maxQueries 最大查询数量
     * @return 解析后的查询列表
     */
    List<String> parseExpandedQueries(String expandedText, int maxQueries);
}