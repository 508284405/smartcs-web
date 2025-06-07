package com.leyue.smartcs.api;

import com.alibaba.cola.dto.PageResponse;
import com.alibaba.cola.dto.Response;
import com.alibaba.cola.dto.SingleResponse;
import com.leyue.smartcs.dto.knowledge.ChunkDTO;
import com.leyue.smartcs.dto.knowledge.ChunkCreateCmd;
import com.leyue.smartcs.dto.knowledge.ChunkUpdateCmd;
import com.leyue.smartcs.dto.knowledge.ChunkListQry;

/**
 * 内容切片服务接口
 */
public interface ChunkService {
    
    /**
     * 创建切片
     * @param cmd 创建命令
     * @return 切片信息
     */
    SingleResponse<ChunkDTO> createChunk(ChunkCreateCmd cmd);
    
    /**
     * 更新切片
     * @param cmd 更新命令
     * @return 操作结果
     */
    Response updateChunk(ChunkUpdateCmd cmd);
    
    /**
     * 删除切片
     * @param id 切片ID
     * @return 操作结果
     */
    Response deleteChunk(Long id);
    
    /**
     * 查询切片详情
     * @param id 切片ID
     * @return 切片信息
     */
    SingleResponse<ChunkDTO> getChunk(Long id);
    
    /**
     * 分页查询切片列表
     * @param qry 查询条件
     * @return 分页结果
     */
    PageResponse<ChunkDTO> listChunks(ChunkListQry qry);
    
    /**
     * 切片向量化存储
     * @param id 切片ID
     * @return 操作结果
     */
    Response vectorizeChunk(Long id);
} 