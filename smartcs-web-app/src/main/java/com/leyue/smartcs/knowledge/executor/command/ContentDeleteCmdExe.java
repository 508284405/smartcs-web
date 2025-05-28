package com.leyue.smartcs.knowledge.executor.command;

import com.alibaba.cola.dto.Response;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.domain.knowledge.Content;
import com.leyue.smartcs.domain.knowledge.gateway.ContentGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 内容删除命令执行器
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ContentDeleteCmdExe {

    private final ContentGateway contentGateway;

    /**
     * 执行内容删除命令
     * @param id 内容ID
     * @return 删除结果
     */
    public Response execute(Long id) {
        log.info("执行内容删除命令，ID: {}", id);
        
        // 参数校验
        if (id == null) {
            throw new BizException("内容ID不能为空");
        }
        
        // 检查内容是否存在
        Content existingContent = contentGateway.findById(id);
        if (existingContent == null) {
            throw new BizException("内容不存在，ID: " + id);
        }
        
        // 执行删除
        contentGateway.deleteById(id);
        
        log.info("内容删除成功，ID: {}", id);
        return Response.buildSuccess();
    }
}