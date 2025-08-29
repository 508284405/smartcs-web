package com.leyue.smartcs.chat.executor;

import com.alibaba.cola.dto.Response;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.api.MessageSendService;
import com.leyue.smartcs.config.websocket.WebSocketSessionManager;
import com.leyue.smartcs.domain.chat.Message;
import com.leyue.smartcs.domain.chat.domainservice.MessageDomainService;
import com.leyue.smartcs.dto.chat.RecallMessageCmd;
import com.leyue.smartcs.dto.chat.ws.RecallMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

/**
 * 消息撤回命令执行器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RecallMessageCmdExe {
    
    private final MessageDomainService messageDomainService;
    private final SimpMessagingTemplate messagingTemplate;
    private final WebSocketSessionManager sessionManager;
    
    /**
     * 执行消息撤回命令
     *
     * @param recallMessageCmd 撤回命令
     * @return 执行结果
     */
    public Response execute(RecallMessageCmd recallMessageCmd) {
        try {
            // 执行撤回操作
            Message recalledMessage = messageDomainService.recallMessage(
                    recallMessageCmd.getMsgId(),
                    recallMessageCmd.getUserId(),
                    recallMessageCmd.getReason()
            );
            
            // 构建撤回通知消息
            RecallMessage recallNotice = new RecallMessage();
            recallNotice.setMsgId(recalledMessage.getMsgId());
            recallNotice.setSessionId(recalledMessage.getSessionId());
            recallNotice.setUserId(recallMessageCmd.getUserId());
            recallNotice.setReason(recallMessageCmd.getReason());
            recallNotice.setRecallTime(System.currentTimeMillis());
            
            // 通过WebSocket广播撤回通知
            broadcastRecallNotice(recallNotice);
            
            log.info("消息撤回成功: msgId={}, userId={}", 
                    recallMessageCmd.getMsgId(), recallMessageCmd.getUserId());
                    
            return Response.buildSuccess();
            
        } catch (BizException e) {
            log.warn("消息撤回失败: {}", e.getMessage());
            return Response.buildFailure("RECALL_FAILED", e.getMessage());
        } catch (Exception e) {
            log.error("消息撤回异常: msgId={}", recallMessageCmd.getMsgId(), e);
            return Response.buildFailure("SYSTEM_ERROR", "系统异常");
        }
    }
    
    /**
     * 广播撤回通知
     */
    private void broadcastRecallNotice(RecallMessage recallMessage) {
        try {
            // 向会话中的所有在线用户发送撤回通知
            String destination = "/topic/chat/" + recallMessage.getSessionId();
            messagingTemplate.convertAndSend(destination, recallMessage);
            
            log.debug("撤回通知已广播: sessionId={}, msgId={}", 
                    recallMessage.getSessionId(), recallMessage.getMsgId());
                    
        } catch (Exception e) {
            log.error("广播撤回通知失败: {}", e.getMessage(), e);
        }
    }
}