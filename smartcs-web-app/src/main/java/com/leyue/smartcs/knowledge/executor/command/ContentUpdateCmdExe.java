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
     * @param cmd 更新命令
     * @return 更新结果
     */
    public Response execute(ContentUpdateCmd cmd) {
        log.info("执行内容更新命令: {}", cmd);
        
        // 参数校验
        validateCmd(cmd);
        
        // 查找现有内容
        Content existingContent = contentGateway.findById(cmd.getId());
        if (existingContent == null) {
            throw new BizException("内容不存在，ID: " + cmd.getId());
        }
        
        // 更新内容字段
        boolean hasUpdate = false;
        
        if (cmd.getTitle() != null && !cmd.getTitle().equals(existingContent.getTitle())) {
            existingContent.setTitle(cmd.getTitle());
            hasUpdate = true;
        }
        
        if (cmd.getStatus() != null && !cmd.getStatus().equals(existingContent.getStatus())) {
            if (!isValidStatusTransition(existingContent.getStatus(), cmd.getStatus())) {
                throw new BizException("无效的状态转换: " + existingContent.getStatus() + " -> " + cmd.getStatus());
            }
            existingContent.setStatus(cmd.getStatus());
            hasUpdate = true;
        }
        
        if (cmd.getExtractedText() != null && !cmd.getExtractedText().equals(existingContent.getTextExtracted())) {
            existingContent.setTextExtracted(cmd.getExtractedText());
            hasUpdate = true;
        }
        
        // 如果有更新，则保存
        if (hasUpdate) {
            existingContent.setUpdatedAt(System.currentTimeMillis());
            contentGateway.update(existingContent);
            log.info("内容更新成功，ID: {}", cmd.getId());
        } else {
            log.info("内容无需更新，ID: {}", cmd.getId());
        }
        
        return Response.buildSuccess();
    }
    
    /**
     * 参数校验
     * @param cmd 更新命令
     */
    private void validateCmd(ContentUpdateCmd cmd) {
        if (cmd.getId() == null) {
            throw new BizException("内容ID不能为空");
        }
        
        if (cmd.getTitle() != null && cmd.getTitle().trim().isEmpty()) {
            throw new BizException("内容标题不能为空");
        }
        
        if (cmd.getTitle() != null && cmd.getTitle().length() > 256) {
            throw new BizException("内容标题长度不能超过256个字符");
        }
        
        if (cmd.getStatus() != null && !isValidStatus(cmd.getStatus())) {
            throw new BizException("无效的内容状态: " + cmd.getStatus());
        }
    }
    
    /**
     * 检查状态是否有效
     * @param status 状态
     * @return 是否有效
     */
    private boolean isValidStatus(String status) {
        return "uploaded".equals(status) || 
               "parsed".equals(status) || 
               "vectorized".equals(status);
    }
    
    /**
     * 检查状态转换是否有效
     * @param fromStatus 原状态
     * @param toStatus 目标状态
     * @return 是否有效
     */
    private boolean isValidStatusTransition(String fromStatus, String toStatus) {
        if ("uploaded".equals(fromStatus)) {
            return "parsed".equals(toStatus);
        } else if ("parsed".equals(fromStatus)) {
            return "vectorized".equals(toStatus);
        }
        return false;
    }
}