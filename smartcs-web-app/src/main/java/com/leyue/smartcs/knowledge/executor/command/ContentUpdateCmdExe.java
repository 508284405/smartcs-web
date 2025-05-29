package com.leyue.smartcs.knowledge.executor.command;

import com.alibaba.cola.dto.Response;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.domain.knowledge.Content;
import com.leyue.smartcs.domain.knowledge.gateway.ContentGateway;
import com.leyue.smartcs.dto.knowledge.ContentUpdateCmd;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 内容更新命令执行器
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ContentUpdateCmdExe {

    private final ContentGateway contentGateway;

    /**
     * 执行内容更新命令
     * 
     * @param cmd 更新命令
     * @return 更新结果
     */
    public Response execute(ContentUpdateCmd cmd) {
        // 查找现有内容
        Content existingContent = contentGateway.findById(cmd.getId());
        if (existingContent == null) {
            throw new BizException("内容不存在，ID: " + cmd.getId());
        }

        existingContent.setTitle(cmd.getTitle());
        existingContent.setUpdatedAt(System.currentTimeMillis());
        contentGateway.update(existingContent);
        return Response.buildSuccess();
    }
}