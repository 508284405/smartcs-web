package com.leyue.smartcs.knowledge.serviceimpl;

import com.alibaba.cola.dto.PageResponse;
import com.alibaba.cola.dto.Response;
import com.alibaba.cola.dto.SingleResponse;
import com.leyue.smartcs.api.ChunkService;
import com.leyue.smartcs.dto.knowledge.ChunkDTO;
import com.leyue.smartcs.dto.knowledge.ChunkCreateCmd;
import com.leyue.smartcs.dto.knowledge.ChunkListQry;
import com.leyue.smartcs.dto.knowledge.ChunkUpdateCmd;
import com.leyue.smartcs.knowledge.executor.command.ChunkCreateCmdExe;
import com.leyue.smartcs.knowledge.executor.command.ChunkDeleteCmdExe;
import com.leyue.smartcs.knowledge.executor.command.ChunkUpdateCmdExe;
import com.leyue.smartcs.knowledge.executor.command.ChunkVectorizeCmdExe;
import com.leyue.smartcs.knowledge.executor.query.ChunkDetailQryExe;
import com.leyue.smartcs.knowledge.executor.query.ChunkListQryExe;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 切片服务实现
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChunkServiceImpl implements ChunkService {
    
    private final ChunkCreateCmdExe chunkCreateCmdExe;
    private final ChunkUpdateCmdExe chunkUpdateCmdExe;
    private final ChunkDeleteCmdExe chunkDeleteCmdExe;
    private final ChunkVectorizeCmdExe chunkVectorizeCmdExe;
    private final ChunkDetailQryExe chunkDetailQryExe;
    private final ChunkListQryExe chunkListQryExe;
    
    @Override
    public SingleResponse<ChunkDTO> createChunk(ChunkCreateCmd cmd) {
        return chunkCreateCmdExe.execute(cmd);
    }
    
    @Override
    public Response updateChunk(ChunkUpdateCmd cmd) {
        return chunkUpdateCmdExe.execute(cmd);
    }
    
    @Override
    public Response deleteChunk(Long id) {
        return chunkDeleteCmdExe.execute(id);
    }
    
    @Override
    public SingleResponse<ChunkDTO> getChunk(Long id) {
        return chunkDetailQryExe.execute(id);
    }
    
    @Override
    public PageResponse<ChunkDTO> listChunks(ChunkListQry qry) {
        return chunkListQryExe.execute(qry);
    }
    
    @Override
    public Response vectorizeChunk(Long id) {
        return chunkVectorizeCmdExe.execute(id);
    }
} 