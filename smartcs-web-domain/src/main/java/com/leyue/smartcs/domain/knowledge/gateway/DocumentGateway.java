package com.leyue.smartcs.domain.knowledge.gateway;

import com.leyue.smartcs.domain.knowledge.Document;

import java.util.List;
import java.util.Optional;

/**
 * 文档存储网关接口
 */
public interface DocumentGateway {
    /**
     * 保存文档
     * @param document 文档实体
     * @return 保存后的文档
     */
    Document save(Document document);
    
    /**
     * 根据ID查询文档
     * @param id 文档ID
     * @return 文档实体(可能为空)
     */
    Optional<Document> findById(Long id);
    
    /**
     * 根据ID删除文档
     * @param id 文档ID
     * @return 是否删除成功
     */
    boolean deleteById(Long id);
    
    /**
     * 分页查询文档
     * @param keyword 关键词
     * @param pageNum 页码(从1开始)
     * @param pageSize 每页大小
     * @return 文档列表
     */
    List<Document> listByPage(String keyword, int pageNum, int pageSize);
    
    /**
     * 获取总记录数
     * @param keyword 关键词
     * @return 总记录数
     */
    long count(String keyword);
} 