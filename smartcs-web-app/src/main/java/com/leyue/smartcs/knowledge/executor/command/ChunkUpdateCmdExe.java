package com.leyue.smartcs.knowledge.executor.command;

import com.alibaba.cola.dto.Response;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.domain.knowledge.Chunk;
import com.leyue.smartcs.domain.knowledge.gateway.ChunkGateway;
import com.leyue.smartcs.dto.knowledge.ChunkUpdateCmd;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 更新切片命令执行器
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ChunkUpdateCmdExe {
    
    private final ChunkGateway chunkGateway;
    
    /**
     * 执行更新切片命令
     * @param cmd 更新命令
     * @return 操作结果
     */
    public Response execute(ChunkUpdateCmd cmd) {
        log.info("开始更新切片，切片ID: {}", cmd.getId());
        
        try {
            // 查询现有切片
            Chunk existingChunk = chunkGateway.findById(cmd.getId());
            if (existingChunk == null) {
                throw new BizException("CHUNK_NOT_FOUND", "切片不存在: " + cmd.getId());
            }
            if (cmd.getContent() != null) {
                existingChunk.setContent(cmd.getContent());
            }
            if (cmd.getTokenSize() != null) {
                existingChunk.setTokenSize(cmd.getTokenSize());
            }
            if (cmd.getMetadata() != null) {
                existingChunk.setMetadata(cmd.getMetadata());
            }
            existingChunk.setUpdateTime(System.currentTimeMillis());
            
            // 保存更新
            boolean success = chunkGateway.update(existingChunk);
            if (!success) {
                throw new BizException("CHUNK_UPDATE_FAILED", "更新切片失败");
            }
            
            log.info("切片更新成功，切片ID: {}", cmd.getId());
            return Response.buildSuccess();
            
        } catch (Exception e) {
            log.error("更新切片失败", e);
            throw new BizException("CHUNK_UPDATE_FAILED", "更新切片失败: " + e.getMessage());
        }
    }
} 