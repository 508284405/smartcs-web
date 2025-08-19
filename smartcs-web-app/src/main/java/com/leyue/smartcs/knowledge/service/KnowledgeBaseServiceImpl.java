package com.leyue.smartcs.knowledge.service;

import com.alibaba.cola.dto.MultiResponse;
import com.alibaba.cola.dto.PageResponse;
import com.alibaba.cola.dto.Response;
import com.alibaba.cola.dto.SingleResponse;
import com.leyue.smartcs.api.KnowledgeBaseService;
import com.leyue.smartcs.dto.knowledge.*;
import com.leyue.smartcs.knowledge.executor.command.KnowledgeBaseCreateCmdExe;
import com.leyue.smartcs.knowledge.executor.command.KnowledgeBaseDeleteCmdExe;
import com.leyue.smartcs.knowledge.executor.command.KnowledgeBaseSettingsUpdateCmdExe;
import com.leyue.smartcs.knowledge.executor.command.KnowledgeBaseUpdateCmdExe;
import com.leyue.smartcs.knowledge.executor.command.KnowledgeGeneralChunkCmdExe;
import com.leyue.smartcs.knowledge.executor.command.KnowledgeParentChildChunkCmdExe;
import com.leyue.smartcs.knowledge.executor.query.KnowledgeBaseGetQryExe;
import com.leyue.smartcs.knowledge.executor.query.KnowledgeBaseListQryExe;
import com.leyue.smartcs.knowledge.executor.query.KnowledgeBaseSettingsGetQryExe;
import com.leyue.smartcs.knowledge.executor.query.RecallTestQryExe;
import com.leyue.smartcs.knowledge.executor.query.TextSearchQryExe;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 知识库管理服务实现
 */
@Service
@RequiredArgsConstructor
public class KnowledgeBaseServiceImpl implements KnowledgeBaseService {

    private final KnowledgeBaseCreateCmdExe knowledgeBaseCreateCmdExe;
    
    private final KnowledgeBaseUpdateCmdExe knowledgeBaseUpdateCmdExe;
    
    private final KnowledgeBaseDeleteCmdExe knowledgeBaseDeleteCmdExe;
    
    private final KnowledgeBaseGetQryExe knowledgeBaseGetQryExe;
    
    private final KnowledgeBaseListQryExe knowledgeBaseListQryExe;

    private final TextSearchQryExe textSearchQryExe;

    private final KnowledgeGeneralChunkCmdExe knowledgeGeneralChunkCmdExe;

    private final KnowledgeParentChildChunkCmdExe knowledgeParentChildChunkCmdExe;

    private final KnowledgeBaseSettingsGetQryExe knowledgeBaseSettingsGetQryExe;

    private final KnowledgeBaseSettingsUpdateCmdExe knowledgeBaseSettingsUpdateCmdExe;

    private final RecallTestQryExe recallTestQryExe;

    @Override
    public MultiResponse<EmbeddingWithScore> searchByText(KnowledgeSearchQry qry) {
        return textSearchQryExe.execute(qry);
    }
    
    @Override
    public SingleResponse<KnowledgeBaseDTO> createKnowledgeBase(KnowledgeBaseCreateCmd cmd) {
        return knowledgeBaseCreateCmdExe.execute(cmd);
    }

    @Override
    public Response updateKnowledgeBase(KnowledgeBaseUpdateCmd cmd) {
        return knowledgeBaseUpdateCmdExe.execute(cmd);
    }

    @Override
    public SingleResponse<KnowledgeBaseDTO> getKnowledgeBase(Long id) {
        return knowledgeBaseGetQryExe.execute(id);
    }

    @Override
    public Response deleteKnowledgeBase(Long id) {
        return knowledgeBaseDeleteCmdExe.execute(id);
    }

    @Override
    public PageResponse<KnowledgeBaseDTO> listKnowledgeBases(KnowledgeBaseListQry qry) {
        return knowledgeBaseListQryExe.execute(qry);
    }

    @Override
    public MultiResponse<ChunkDTO> generalChunk(KnowledgeGeneralChunkCmd cmd) {
        return knowledgeGeneralChunkCmdExe.execute(cmd);
    }

    @Override
    public MultiResponse<ChunkDTO> parentChildChunk(KnowledgeParentChildChunkCmd cmd) {
        return knowledgeParentChildChunkCmdExe.execute(cmd);
    }

    @Override
    public SingleResponse<KnowledgeBaseSettingsDTO> getKnowledgeBaseSettings(Long id) {
        return knowledgeBaseSettingsGetQryExe.execute(id);
    }

    @Override
    public Response updateKnowledgeBaseSettings(KnowledgeBaseSettingsUpdateCmd cmd) {
        return knowledgeBaseSettingsUpdateCmdExe.execute(cmd);
    }

    @Override
    public MultiResponse<RecallTestResultDTO> recallTest(RecallTestQry qry) {
        return recallTestQryExe.execute(qry);
    }
}
