package com.leyue.smartcs.chat.executor;

import com.alibaba.cola.dto.Response;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.domain.chat.Message;
import com.leyue.smartcs.domain.chat.gateway.MessageGateway;
import com.leyue.smartcs.dto.chat.MarkMessageReadCmd;
import com.leyue.smartcs.dto.chat.ws.ReadReceiptMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 标记消息已读命令执行器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MarkMessageReadCmdExe {

    private final MessageGateway messageGateway;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 执行标记已读操作
     */
    @Transactional
    public Response execute(MarkMessageReadCmd cmd) {
        try {
            // 查找消息
            Message message = messageGateway.findByMsgId(cmd.getMsgId());
            if (message == null) {
                return Response.buildFailure("MESSAGE_NOT_FOUND", "消息不存在");
            }

            // 检查是否可以标记为已读
            if (!message.canMarkAsRead(cmd.getUserId())) {
                return Response.buildFailure("MESSAGE_CANNOT_MARK_READ", "消息无法标记为已读：可能已经是已读状态或已被撤回/删除");
            }

            // 执行标记已读操作
            message.markAsRead(cmd.getUserId());

            // 保存更新
            boolean success = messageGateway.updateMessage(message);
            if (!success) {
                throw new BizException("MESSAGE_READ_MARK_FAILED", "标记消息已读失败");
            }

            // 发送已读回执通知
            sendReadReceiptNotification(message, cmd.getSessionId(), cmd.getUserId());

            log.info("消息标记已读成功: msgId={}, userId={}", cmd.getMsgId(), cmd.getUserId());
            return Response.buildSuccess();

        } catch (BizException e) {
            log.warn("标记消息已读业务异常: {}", e.getMessage());
            return Response.buildFailure(e.getErrCode(), e.getErrMessage());
        } catch (Exception e) {
            log.error("标记消息已读执行异常", e);
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