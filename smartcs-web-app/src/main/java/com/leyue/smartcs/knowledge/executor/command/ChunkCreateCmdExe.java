package com.leyue.smartcs.knowledge.executor.command;

import com.alibaba.cola.dto.SingleResponse;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.domain.knowledge.Chunk;
import com.leyue.smartcs.domain.knowledge.gateway.ChunkGateway;
import com.leyue.smartcs.dto.knowledge.ChunkCreateCmd;
import com.leyue.smartcs.dto.knowledge.ChunkDTO;
import com.leyue.smartcs.knowledge.convertor.ChunkConvertor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 创建切片命令执行器
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ChunkCreateCmdExe {
    
    private final ChunkGateway chunkGateway;
    private final ChunkConvertor chunkConverter;
    
    /**
     * 执行创建切片命令
     * @param cmd 创建命令
     * @return 切片信息
     */
    public SingleResponse<ChunkDTO> execute(ChunkCreateCmd cmd) {
        try {
            // 构建切片领域对象
            Chunk chunk = Chunk.builder()
                    .contentId(cmd.getContentId())
                    .content(cmd.getContent())
                    .tokenSize(cmd.getTokenSize())
                    .metadata(cmd.getMetadata())
                    .createTime(System.currentTimeMillis())
                    .updateTime(System.currentTimeMillis())
                    .build();
            
            // 保存切片
            Long chunkId = chunkGateway.save(chunk);
            chunk.setId(chunkId);
            
            // 转换为DTO返回
            ChunkDTO chunkDTO = chunkConverter.toDTO(chunk);
            
            log.info("切片创建成功，切片ID: {}", chunkId);
            return SingleResponse.of(chunkDTO);
            
        } catch (Exception e) {
            log.error("创建切片失败", e);
            throw new BizException("CHUNK_CREATE_FAILED", "创建切片失败: " + e.getMessage());
        }
    }
} 