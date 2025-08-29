package com.leyue.smartcs.chat.executor;

import org.springframework.stereotype.Component;

import com.alibaba.cola.dto.Response;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.domain.chat.Message;
import com.leyue.smartcs.domain.chat.gateway.MessageGateway;
import com.leyue.smartcs.dto.chat.DeleteMessageCmd;
import com.leyue.smartcs.dto.chat.ws.DeleteMessage;
import com.leyue.smartcs.chat.service.WebSocketNotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 删除消息命令执行器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DeleteMessageCmdExe {
    
    private final MessageGateway messageGateway;
    private final WebSocketNotificationService webSocketNotificationService;
    
    /**
     * 执行删除消息命令
     */
    public Response execute(DeleteMessageCmd cmd) {
        log.info("开始执行删除消息命令: msgId={}, userId={}, deleteType={}", 
                cmd.getMsgId(), cmd.getUserId(), cmd.getDeleteType());
        
        try {
            // 1. 查找消息
            Message message = messageGateway.findByMsgId(cmd.getMsgId());
            if (message == null) {
                throw new BizException("消息不存在");
            }
            
            // 2. 权限校验
            if (!message.canDelete(cmd.getUserId())) {
                throw new BizException("没有权限删除此消息");
            }
            
            // 3. 执行删除逻辑
            message.deleteBy(cmd.getUserId(), cmd.getDeleteType(), cmd.getReason());
            
            // 4. 保存到数据库
            messageGateway.updateMessage(message);
            
            // 5. 发送WebSocket通知
            sendDeleteNotification(message, cmd);
            
            log.info("消息删除成功: msgId={}, userId={}", cmd.getMsgId(), cmd.getUserId());
            return Response.buildSuccess();
            
        } catch (BizException e) {
            log.warn("删除消息业务异常: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("删除消息异常", e);
            throw new BizException("删除消息失败");
        }
    }
    
    /**
     * 发送删除通知
     */
    private void sendDeleteNotification(Message message, DeleteMessageCmd cmd) {
        try {
            DeleteMessage deleteMessage = new DeleteMessage();
            deleteMessage.setMsgId(cmd.getMsgId());
            deleteMessage.setSessionId(cmd.getSessionId());
            deleteMessage.setUserId(cmd.getUserId());
            deleteMessage.setDeleteType(cmd.getDeleteType());
            deleteMessage.setReason(cmd.getReason());
            deleteMessage.setDeleteTime(System.currentTimeMillis());
            
            // 发送给会话中的所有用户
            webSocketNotificationService.sendToSession(cmd.getSessionId().toString(), deleteMessage);
            
        } catch (Exception e) {
            log.error("发送删除通知失败", e);
            // 不抛出异常，避免影响主流程
        }
    }
}