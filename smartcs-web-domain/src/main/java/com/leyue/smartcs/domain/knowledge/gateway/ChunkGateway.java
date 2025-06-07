package com.leyue.smartcs.domain.knowledge.gateway;

import com.leyue.smartcs.domain.knowledge.Chunk;
import com.alibaba.cola.dto.PageResponse;

import java.util.List;

/**
 * 内容切片网关接口
 */
public interface ChunkGateway {
    
    /**
     * 保存切片
     * @param chunk 切片对象
     * @return 保存后的切片ID
     */
    Long save(Chunk chunk);
    
    /**
     * 更新切片
     * @param chunk 切片对象
     * @return 是否更新成功
     */
    boolean update(Chunk chunk);
    
    /**
     * 根据ID删除切片
     * @param id 切片ID
     * @return 是否删除成功
     */
    boolean deleteById(Long id);
    
    /**
     * 根据ID查询切片
     * @param id 切片ID
     * @return 切片对象
     */
    Chunk findById(Long id);
    
    /**
     * 根据内容ID查询切片列表
     * @param contentId 内容ID
     * @return 切片列表
     */
    List<Chunk> findByContentId(Long contentId);
    
    /**
     * 分页查询切片列表
     * @param contentId 内容ID
     * @param keyword 关键词
     * @param chunkIndex 段落序号
     * @param pageIndex 页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    PageResponse<Chunk> findByPage(Long contentId, String keyword, Integer chunkIndex, Integer pageIndex, Integer pageSize);

    /**
     * 根据内容ID删除切片
     * @param contentId 内容ID
     * @return 删除的切片ID列表
     */
    List<Long> deleteByContentId(Long contentId);

    /**
     * 批量保存切片
     * @param chunks 切片列表
     */
    List<Long> saveBatch(List<Chunk> chunks);
} 