package com.leyue.smartcs.chat.executor;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.cola.dto.Response;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.domain.chat.Message;
import com.leyue.smartcs.domain.chat.gateway.MessageGateway;
import com.leyue.smartcs.dto.chat.BatchDeleteMessagesCmd;
import com.leyue.smartcs.dto.chat.ws.DeleteMessage;
import com.leyue.smartcs.chat.service.WebSocketNotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 批量删除消息命令执行器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BatchDeleteMessagesCmdExe {
    
    private final MessageGateway messageGateway;
    private final WebSocketNotificationService webSocketNotificationService;
    
    /**
     * 执行批量删除消息命令
     */
    @Transactional(rollbackFor = Exception.class)
    public Response execute(BatchDeleteMessagesCmd cmd) {
        log.info("开始执行批量删除消息命令: msgIds={}, userId={}, deleteType={}", 
                cmd.getMsgIds().size(), cmd.getUserId(), cmd.getDeleteType());
        
        try {
            List<String> deletedMsgIds = new ArrayList<>();
            List<String> failedMsgIds = new ArrayList<>();
            
            for (String msgId : cmd.getMsgIds()) {
                try {
                    // 1. 查找消息
                    Message message = messageGateway.findByMsgId(msgId);
                    if (message == null) {
                        log.warn("消息不存在: msgId={}", msgId);
                        failedMsgIds.add(msgId);
                        continue;
                    }
                    
                    // 2. 权限校验
                    if (!message.canDelete(cmd.getUserId())) {
                        log.warn("没有权限删除消息: msgId={}, userId={}", msgId, cmd.getUserId());
                        failedMsgIds.add(msgId);
                        continue;
                    }
                    
                    // 3. 执行删除逻辑
                    message.deleteBy(cmd.getUserId(), cmd.getDeleteType(), cmd.getReason());
                    
                    // 4. 保存到数据库
                    messageGateway.updateMessage(message);
                    
                    deletedMsgIds.add(msgId);
                    
                } catch (Exception e) {
                    log.error("删除消息失败: msgId={}", msgId, e);
                    failedMsgIds.add(msgId);
                }
            }
            
            // 5. 发送批量删除通知
            if (!deletedMsgIds.isEmpty()) {
                sendBatchDeleteNotification(deletedMsgIds, cmd);
            }
            
            if (!failedMsgIds.isEmpty()) {
                log.warn("部分消息删除失败: {}", failedMsgIds);
                return Response.buildFailure("PARTIAL_SUCCESS", "部分消息删除失败");
            }
            
            log.info("批量删除消息成功: 成功删除{}条消息", deletedMsgIds.size());
            return Response.buildSuccess();
            
        } catch (Exception e) {
            log.error("批量删除消息异常", e);
            throw new BizException("批量删除消息失败");
        }
    }
    
    /**
     * 发送批量删除通知
     */
    private void sendBatchDeleteNotification(List<String> deletedMsgIds, BatchDeleteMessagesCmd cmd) {
        try {
            for (String msgId : deletedMsgIds) {
                DeleteMessage deleteMessage = new DeleteMessage();
                deleteMessage.setMsgId(msgId);
                deleteMessage.setSessionId(cmd.getSessionId());
                deleteMessage.setUserId(cmd.getUserId());
                deleteMessage.setDeleteType(cmd.getDeleteType());
                deleteMessage.setReason(cmd.getReason());
                deleteMessage.setDeleteTime(System.currentTimeMillis());
                
                // 发送给会话中的所有用户
                webSocketNotificationService.sendToSession(cmd.getSessionId().toString(), deleteMessage);
            }
            
        } catch (Exception e) {
            log.error("发送批量删除通知失败", e);
            // 不抛出异常，避免影响主流程
        }
    }
}