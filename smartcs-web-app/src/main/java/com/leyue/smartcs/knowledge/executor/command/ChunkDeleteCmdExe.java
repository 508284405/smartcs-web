package com.leyue.smartcs.knowledge.executor.command;

import com.alibaba.cola.dto.Response;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.domain.knowledge.gateway.ChunkGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 删除切片命令执行器
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ChunkDeleteCmdExe {
    
    private final ChunkGateway chunkGateway;
    
    /**
     * 执行删除切片命令
     * @param id 切片ID
     * @return 操作结果
     */
    public Response execute(Long id) {
        log.info("开始删除切片，切片ID: {}", id);
        
        try {
            // 检查切片是否存在
            if (chunkGateway.findById(id) == null) {
                throw new BizException("CHUNK_NOT_FOUND", "切片不存在: " + id);
            }
            
            // 删除切片
            boolean success = chunkGateway.deleteById(id);
            if (!success) {
                throw new BizException("CHUNK_DELETE_FAILED", "删除切片失败");
            }
            
            log.info("切片删除成功，切片ID: {}", id);
            return Response.buildSuccess();
            
        } catch (Exception e) {
            log.error("删除切片失败", e);
            throw new BizException("CHUNK_DELETE_FAILED", "删除切片失败: " + e.getMessage());
        }
    }
} 