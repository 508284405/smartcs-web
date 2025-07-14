package com.leyue.smartcs.api;

import com.alibaba.cola.dto.MultiResponse;
import com.alibaba.cola.dto.PageResponse;
import com.alibaba.cola.dto.Response;
import com.alibaba.cola.dto.SingleResponse;
import com.leyue.smartcs.dto.knowledge.*;

/**
 * 知识库管理服务接口
 */
public interface KnowledgeBaseService {
    
    /**
     * 创建知识库
     * @param cmd 创建命令
     * @return 创建的知识库
     */
    SingleResponse<KnowledgeBaseDTO> createKnowledgeBase(KnowledgeBaseCreateCmd cmd);
    
    /**
     * 更新知识库
     * @param cmd 更新命令
     * @return 更新结果
     */
    Response updateKnowledgeBase(KnowledgeBaseUpdateCmd cmd);
    
    /**
     * 根据ID查询知识库
     * @param id 知识库ID
     * @return 知识库信息
     */
    SingleResponse<KnowledgeBaseDTO> getKnowledgeBase(Long id);
    
    /**
     * 删除知识库
     * @param id 知识库ID
     * @return 删除结果
     */
    Response deleteKnowledgeBase(Long id);
    
    /**
     * 查询知识库列表
     * @param qry 查询条件
     * @return 知识库列表
     */
    PageResponse<KnowledgeBaseDTO> listKnowledgeBases(KnowledgeBaseListQry qry);

    /**
     * 执行全文检索
     *
     * @param qry 检索条件（包含keyword、k等）
     * @return 检索结果
     */
    MultiResponse<EmbeddingWithScore> searchByText(KnowledgeSearchQry qry);

    /**
     * 通用文档分块
     * @param cmd 通用分块命令
     * @return 分块结果
     */
    MultiResponse<ChunkDTO> generalChunk(KnowledgeGeneralChunkCmd cmd);

    /**
     * 父子文档分块
     * @param cmd 父子分块命令
     * @return 分块结果
     */
    MultiResponse<ChunkDTO> parentChildChunk(KnowledgeParentChildChunkCmd cmd);
} 