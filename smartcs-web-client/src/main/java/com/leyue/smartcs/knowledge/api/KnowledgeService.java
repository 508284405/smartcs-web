package com.leyue.smartcs.knowledge.api;

import com.alibaba.cola.dto.MultiResponse;
import com.alibaba.cola.dto.PageResponse;
import com.alibaba.cola.dto.Response;
import com.alibaba.cola.dto.SingleResponse;
import com.leyue.smartcs.dto.common.SingleClientObject;
import com.leyue.smartcs.knowledge.dto.*;

/**
 * 知识服务接口
 */
public interface KnowledgeService {
    /**
     * 创建或更新FAQ
     *
     * @param cmd FAQ创建/更新命令
     * @return 创建/更新的FAQ
     */
    SingleResponse<FaqDTO> addFaq(FaqAddCmd cmd);
    
    /**
     * 删除FAQ
     *
     * @param idCmd FAQ ID
     * @return 操作结果
     */
    Response deleteFaq(SingleClientObject<Long> idCmd);
    
    /**
     * 查询FAQ列表
     *
     * @param qry 查询条件
     * @return FAQ列表（分页）
     */
    PageResponse<FaqDTO> listFaqs(KnowledgeSearchQry qry);
    
    /**
     * 创建文档记录
     *
     * @param cmd 文档创建命令
     * @return 创建的文档
     */
    SingleResponse<DocDTO> addDoc(DocAddCmd cmd);
    
    /**
     * 触发文档分段与向量生成任务
     *
     * @param docIdCmd 文档ID
     * @return 操作结果
     */
    Response triggerDocEmbedding(SingleClientObject<Long> docIdCmd);
    
    /**
     * 查询文档列表
     *
     * @param qry 查询条件
     * @return 文档列表（分页）
     */
    PageResponse<DocDTO> listDocs(KnowledgeSearchQry qry);
    
    /**
     * 根据文档ID获取分段Embedding
     *
     * @param docIdCmd 文档ID
     * @return 文档段落向量列表
     */
    MultiResponse<EmbeddingDTO> listEmbeddingsByDoc(SingleClientObject<Long> docIdCmd);
    
    /**
     * 批量添加向量
     *
     * @param cmd 向量批量添加命令
     * @return 操作结果
     */
    Response addEmbeddings(EmbeddingAddCmd cmd);
    
    /**
     * 执行向量检索
     *
     * @param qry 检索条件（包含vector、k等）
     * @return 检索结果
     */
    MultiResponse<KnowledgeSearchResult> searchByVector(KnowledgeSearchQry qry);
    
    /**
     * 执行全文检索
     *
     * @param qry 检索条件（包含keyword、k等）
     * @return 检索结果
     */
    MultiResponse<KnowledgeSearchResult> searchByText(KnowledgeSearchQry qry);
} 