package com.leyue.smartcs.knowledge.service;

import com.alibaba.cola.dto.MultiResponse;
import com.alibaba.cola.dto.PageResponse;
import com.alibaba.cola.dto.Response;
import com.alibaba.cola.dto.SingleResponse;
import com.leyue.smartcs.dto.common.SingleClientObject;
import com.leyue.smartcs.dto.knowledge.*;
import com.leyue.smartcs.api.KnowledgeService;
import com.leyue.smartcs.knowledge.executor.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 知识服务接口实现
 */
@Service
@RequiredArgsConstructor
public class KnowledgeServiceImpl implements KnowledgeService {
    
    private final FaqAddCmdExe faqAddCmdExe;
    private final FaqDeleteCmdExe faqDeleteCmdExe;
    private final FaqListQryExe faqListQryExe;
    private final DocAddCmdExe docAddCmdExe;
    private final DocEmbeddingTriggerCmdExe docEmbeddingTriggerCmdExe;
    private final DocListQryExe docListQryExe;
    private final EmbeddingListQryExe embeddingListQryExe;
    private final EmbeddingAddCmdExe embeddingAddCmdExe;
    private final VectorSearchQryExe vectorSearchQryExe;
    private final TextSearchQryExe textSearchQryExe;
    
    @Override
    public SingleResponse<FaqDTO> addFaq(FaqAddCmd cmd) {
        return faqAddCmdExe.execute(cmd);
    }
    
    @Override
    public Response deleteFaq(SingleClientObject<Long> idCmd) {
        return faqDeleteCmdExe.execute(idCmd);
    }
    
    @Override
    public PageResponse<FaqDTO> listFaqs(KnowledgeSearchQry qry) {
        return faqListQryExe.execute(qry);
    }
    
    @Override
    public SingleResponse<DocDTO> addDoc(DocAddCmd cmd) {
        return docAddCmdExe.execute(cmd);
    }
    
    @Override
    public Response triggerDocEmbedding(SingleClientObject<Long> docIdCmd) {
        return docEmbeddingTriggerCmdExe.execute(docIdCmd);
    }
    
    @Override
    public PageResponse<DocDTO> listDocs(KnowledgeSearchQry qry) {
        return docListQryExe.execute(qry);
    }
    
    @Override
    public MultiResponse<EmbeddingDTO> listEmbeddingsByDoc(SingleClientObject<Long> docIdCmd) {
        return embeddingListQryExe.execute(docIdCmd);
    }
    
    @Override
    public Response addEmbeddings(EmbeddingAddCmd cmd) {
        return embeddingAddCmdExe.execute(cmd);
    }
    
    @Override
    public MultiResponse<KnowledgeSearchResult> searchByVector(KnowledgeSearchQry qry) {
        return vectorSearchQryExe.execute(qry);
    }
    
    @Override
    public MultiResponse<KnowledgeSearchResult> searchByText(KnowledgeSearchQry qry) {
        return textSearchQryExe.execute(qry);
    }
} 