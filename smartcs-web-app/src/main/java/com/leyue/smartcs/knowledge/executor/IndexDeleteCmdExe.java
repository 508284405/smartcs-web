package com.leyue.smartcs.knowledge.executor;

import com.alibaba.cola.dto.Response;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.domain.knowledge.gateway.SearchGateway;
import com.leyue.smartcs.dto.knowledge.DeleteIndexCmd;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 索引删除命令执行器
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class IndexDeleteCmdExe {
    
    private final SearchGateway searchGateway;
    
    /**
     * 执行索引删除命令
     * @param cmd 删除命令
     * @return 操作结果
     */
    public Response execute(DeleteIndexCmd cmd) {
        log.info("执行索引删除命令: {}", cmd);
        
        // 参数校验
        if (cmd.getIndexName() == null || cmd.getIndexName().trim().isEmpty()) {
            throw new BizException("索引名称不能为空");
        }
        
        try {
            // 删除索引
            boolean success = searchGateway.deleteIndex(cmd.getIndexName());
            if (!success) {
                return Response.buildFailure("NOT_FOUND", "索引不存在或删除失败: " + cmd.getIndexName());
            }
            
            log.info("索引删除成功: {}", cmd.getIndexName());
            return Response.buildSuccess();
        } catch (Exception e) {
            log.error("删除索引失败: " + cmd.getIndexName(), e);
            throw new BizException("删除索引失败: " + e.getMessage());
        }
    }
} 