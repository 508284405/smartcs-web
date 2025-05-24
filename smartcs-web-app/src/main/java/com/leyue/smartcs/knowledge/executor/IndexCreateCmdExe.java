package com.leyue.smartcs.knowledge.executor;

import com.alibaba.cola.dto.Response;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.domain.knowledge.gateway.SearchGateway;
import com.leyue.smartcs.dto.knowledge.CreateIndexCmd;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 索引创建命令执行器
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class IndexCreateCmdExe {

    private final SearchGateway searchGateway;

    /**
     * 执行索引创建命令
     * @param cmd 索引创建命令
     * @return 操作结果
     */
    public Response execute(CreateIndexCmd cmd) {
        log.info("执行索引创建命令: {}", cmd);
        
        // 参数校验
        if (cmd.getIndexName() == null || cmd.getIndexName().trim().isEmpty()) {
            throw new BizException("索引名称不能为空");
        }
        
        if (cmd.getSchema() == null || cmd.getSchema().isEmpty()) {
            throw new BizException("索引字段定义不能为空");
        }
        
        try {
            // 创建索引
            searchGateway.createIndex(cmd.getIndexName(), cmd.getPrefix(), cmd.getSchema(), cmd.isReplaceIfExists());
            log.info("索引创建成功: {}", cmd.getIndexName());
            return Response.buildSuccess();
        } catch (Exception e) {
            log.error("创建索引失败: " + cmd.getIndexName(), e);
            throw new BizException("创建索引失败: " + e.getMessage());
        }
    }
} 