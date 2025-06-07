package com.leyue.smartcs.knowledge.executor.query;

import com.alibaba.cola.dto.SingleResponse;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.domain.knowledge.Chunk;
import com.leyue.smartcs.domain.knowledge.gateway.ChunkGateway;
import com.leyue.smartcs.dto.knowledge.ChunkDTO;
import com.leyue.smartcs.knowledge.convertor.ChunkConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 切片详情查询执行器
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ChunkDetailQryExe {
    
    private final ChunkGateway chunkGateway;
    private final ChunkConverter chunkConverter;
    
    /**
     * 执行切片详情查询
     * @param id 切片ID
     * @return 切片信息
     */
    public SingleResponse<ChunkDTO> execute(Long id) {
        log.info("查询切片详情，切片ID: {}", id);
        
        try {
            // 查询切片信息
            Chunk chunk = chunkGateway.findById(id);
            if (chunk == null) {
                throw new BizException("CHUNK_NOT_FOUND", "切片不存在: " + id);
            }
            
            // 转换为DTO
            ChunkDTO chunkDTO = chunkConverter.toDTO(chunk);
            
            log.info("切片详情查询成功，切片ID: {}", id);
            return SingleResponse.of(chunkDTO);
            
        } catch (Exception e) {
            log.error("查询切片详情失败", e);
            throw new BizException("CHUNK_QUERY_FAILED", "查询切片详情失败: " + e.getMessage());
        }
    }
} 