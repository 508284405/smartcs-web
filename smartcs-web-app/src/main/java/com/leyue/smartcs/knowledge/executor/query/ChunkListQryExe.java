package com.leyue.smartcs.knowledge.executor.query;

import com.alibaba.cola.dto.PageResponse;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.domain.knowledge.Chunk;
import com.leyue.smartcs.domain.knowledge.gateway.ChunkGateway;
import com.leyue.smartcs.dto.knowledge.ChunkDTO;
import com.leyue.smartcs.dto.knowledge.ChunkListQry;
import com.leyue.smartcs.knowledge.convertor.ChunkConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 切片列表查询执行器
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ChunkListQryExe {
    
    private final ChunkGateway chunkGateway;
    private final ChunkConverter chunkConverter;
    
    /**
     * 执行切片列表查询
     * @param qry 查询条件
     * @return 分页结果
     */
    public PageResponse<ChunkDTO> execute(ChunkListQry qry) {
        log.info("查询切片列表，查询条件: {}", qry);
        
        try {
            // 调用网关查询
            PageResponse<Chunk> chunkPage = chunkGateway.findByPage(
                    qry.getContentId(), 
                    qry.getKeyword(), 
                    qry.getChunkIndex(), 
                    qry.getPageIndex(), 
                    qry.getPageSize()
            );
            
            // 转换为DTO
            List<ChunkDTO> chunkDTOList = chunkPage.getData().stream()
                    .map(chunkConverter::toDTO)
                    .collect(Collectors.toList());
            
            PageResponse<ChunkDTO> result = PageResponse.of(
                    chunkDTOList, 
                    chunkPage.getTotalCount(), 
                    qry.getPageSize(), 
                    qry.getPageIndex()
            );
            
            log.info("切片列表查询成功，共查询到 {} 条记录", chunkPage.getTotalCount());
            return result;
            
        } catch (Exception e) {
            log.error("查询切片列表失败", e);
            throw new BizException("CHUNK_LIST_QUERY_FAILED", "查询切片列表失败: " + e.getMessage());
        }
    }
} 