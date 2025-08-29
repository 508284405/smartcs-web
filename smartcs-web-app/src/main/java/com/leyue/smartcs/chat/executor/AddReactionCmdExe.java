package com.leyue.smartcs.chat.executor;

import com.alibaba.cola.dto.Response;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.config.context.UserContext;
import com.leyue.smartcs.domain.chat.MessageReaction;
import com.leyue.smartcs.domain.chat.gateway.MessageGateway;
import com.leyue.smartcs.domain.chat.gateway.MessageReactionGateway;
import com.leyue.smartcs.dto.chat.AddReactionCmd;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 添加表情反应命令执行器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AddReactionCmdExe {
    
    private final MessageReactionGateway reactionGateway;
    private final MessageGateway messageGateway;
    
    /**
     * 执行添加表情反应命令
     */
    public Response execute(AddReactionCmd cmd) {
        try {
            // 获取当前用户ID
            String currentUserId = UserContext.getCurrentUserId();
            if (currentUserId == null) {
                throw new BizException("用户未登录");
            }
            
            // 验证消息是否存在
            var message = messageGateway.findByMsgId(cmd.getMsgId());
            if (message == null) {
                throw new BizException("消息不存在");
            }
            
            // 验证消息是否已被删除或撤回
            if (message.isDeleted() || message.isRecalled()) {
                throw new BizException("无法对已删除或已撤回的消息添加表情反应");
            }
            
            boolean success;
            switch (cmd.getAction().toLowerCase()) {
                case "add":
                    // 直接添加反应
                    MessageReaction newReaction = MessageReaction.create(
                            cmd.getMsgId(), cmd.getSessionId(), currentUserId, 
                            cmd.getEmoji(), cmd.getName());
                    success = reactionGateway.addReaction(newReaction) != null;
                    break;
                    
                case "remove":
                    // 直接移除反应
                    success = reactionGateway.removeReaction(cmd.getMsgId(), currentUserId, cmd.getEmoji());
                    break;
                    
                case "toggle":
                default:
                    // 切换反应（默认行为）
                    success = reactionGateway.toggleReaction(
                            cmd.getMsgId(), cmd.getSessionId(), currentUserId, 
                            cmd.getEmoji(), cmd.getName());
                    break;
            }
            
            if (success) {
                log.info("表情反应操作成功: msgId={}, userId={}, emoji={}, action={}", 
                        cmd.getMsgId(), currentUserId, cmd.getEmoji(), cmd.getAction());
                return Response.buildSuccess();
            } else {
                throw new BizException("表情反应操作失败");
            }
            
        } catch (BizException e) {
            log.warn("表情反应操作失败: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("表情反应操作异常: cmd={}", cmd, e);
            throw new BizException("表情反应操作异常: " + e.getMessage());
        }
    }
}