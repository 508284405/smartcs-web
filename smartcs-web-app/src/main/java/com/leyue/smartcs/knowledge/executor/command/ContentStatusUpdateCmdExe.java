package com.leyue.smartcs.knowledge.executor.command;

import com.alibaba.cola.dto.Response;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.domain.knowledge.Content;
import com.leyue.smartcs.domain.knowledge.enums.ContentStatusEnum;
import com.leyue.smartcs.domain.knowledge.gateway.ContentGateway;
import com.leyue.smartcs.dto.knowledge.ContentStatusUpdateCmd;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 内容状态更新命令执行器
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ContentStatusUpdateCmdExe {

    private final ContentGateway contentGateway;

    /**
     * 执行内容状态更新命令
     * 
     * @param cmd 状态更新命令
     * @return 更新结果
     */
    public Response execute(ContentStatusUpdateCmd cmd) {
        log.info("执行内容状态更新命令: {}", cmd);
        
        // 参数校验
        if (cmd.getContentId() == null) {
            throw new BizException("内容ID不能为空");
        }
        
        if (cmd.getStatus() == null) {
            throw new BizException("状态不能为空");
        }
        
        // 查找现有内容
        Content existingContent = contentGateway.findById(cmd.getContentId());
        if (existingContent == null) {
            throw new BizException("内容不存在，ID: " + cmd.getContentId());
        }
        
        // 转换状态枚举
        ContentStatusEnum newStatus = ContentStatusEnum.fromCode(cmd.getStatus());
        
        // 检查状态转换是否合法
        if (!existingContent.getStatus().canTransitionTo(newStatus)) {
            throw new BizException("不能从状态 " + existingContent.getStatus().getDescription() + " 转换到 " + newStatus.getDescription());
        }
        
        // 更新状态
        try {
            if (newStatus == ContentStatusEnum.ENABLED) {
                existingContent.enable();
            } else if (newStatus == ContentStatusEnum.DISABLED) {
                existingContent.disable();
            }
            
            existingContent.setUpdatedAt(System.currentTimeMillis());
            contentGateway.updateById(existingContent);
            
            log.info("内容状态更新成功，ID: {}, 新状态: {}", cmd.getContentId(), newStatus.getDescription());
            return Response.buildSuccess();
            
        } catch (IllegalStateException e) {
            log.error("内容状态更新失败: {}", e.getMessage());
            throw new BizException(e.getMessage());
        }
    }
} 