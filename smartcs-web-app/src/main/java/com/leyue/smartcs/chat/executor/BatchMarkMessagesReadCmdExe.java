package com.leyue.smartcs.chat.executor;

import com.alibaba.cola.dto.Response;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.domain.chat.Message;
import com.leyue.smartcs.domain.chat.gateway.MessageGateway;
import com.leyue.smartcs.dto.chat.BatchMarkMessagesReadCmd;
import com.leyue.smartcs.dto.chat.ws.ReadReceiptMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 批量标记消息已读命令执行器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BatchMarkMessagesReadCmdExe {

    private final MessageGateway messageGateway;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 执行批量标记已读操作
     */
    @Transactional
    public Response execute(BatchMarkMessagesReadCmd cmd) {
        try {
            List<String> successMsgIds = new ArrayList<>();
            List<String> failedMsgIds = new ArrayList<>();
            
            for (String msgId : cmd.getMsgIds()) {
                try {
                    // 查找消息
                    Message message = messageGateway.findByMsgId(msgId);
                    if (message == null) {
                        failedMsgIds.add(msgId);
                        continue;
                    }

                    // 检查是否可以标记为已读
                    if (!message.canMarkAsRead(cmd.getUserId())) {
                        failedMsgIds.add(msgId);
                        continue;
                    }

                    // 执行标记已读操作
                    message.markAsRead(cmd.getUserId());

                    // 保存更新
                    boolean success = messageGateway.updateMessage(message);
                    if (success) {
                        successMsgIds.add(msgId);
                        
                        // 发送已读回执通知
                        sendReadReceiptNotification(message, cmd.getSessionId(), cmd.getUserId());
                    } else {
                        failedMsgIds.add(msgId);
                    }

                } catch (Exception e) {
                    log.warn("标记消息已读失败: msgId={}, error={}", msgId, e.getMessage());
                    failedMsgIds.add(msgId);
                }
            }

            // 构建响应结果
            if (failedMsgIds.isEmpty()) {
                log.info("批量标记消息已读全部成功: userId={}, 成功数量={}", cmd.getUserId(), successMsgIds.size());
                return Response.buildSuccess();
            } else if (successMsgIds.isEmpty()) {
                return Response.buildFailure("BATCH_MARK_READ_ALL_FAILED", 
                    String.format("批量标记已读全部失败，失败数量: %d", failedMsgIds.size()));
            } else {
                log.warn("批量标记消息已读部分成功: userId={}, 成功数量={}, 失败数量={}", 
                    cmd.getUserId(), successMsgIds.size(), failedMsgIds.size());
                return Response.buildFailure("BATCH_MARK_READ_PARTIAL_FAILED", 
                    String.format("批量标记已读部分成功，成功: %d 个，失败: %d 个", 
                        successMsgIds.size(), failedMsgIds.size()));
            }

        } catch (Exception e) {
            log.error("批量标记消息已读执行异常", e);
            return Response.buildFailure("SYSTEM_ERROR", "系统异常，请稍后重试");
        }
    }

    /**
     * 发送已读回执通知
     */
    private void sendReadReceiptNotification(Message message, String sessionId, String userId) {
        try {
            ReadReceiptMessage readReceiptMessage = new ReadReceiptMessage();
            readReceiptMessage.setMsgId(message.getMsgId());
            readReceiptMessage.setSessionId(sessionId);
            readReceiptMessage.setUserId(userId);
            readReceiptMessage.setReadAt(message.getReadAt());

            // 广播给会话中的所有用户，告知消息已被读取
            String destination = "/topic/readreceipts/" + sessionId;
            messagingTemplate.convertAndSend(destination, readReceiptMessage);

            log.debug("已读回执通知已发送: msgId={}, userId={}", message.getMsgId(), userId);

        } catch (Exception e) {
            log.error("发送已读回执通知失败: msgId={}", message.getMsgId(), e);
        }
    }
}