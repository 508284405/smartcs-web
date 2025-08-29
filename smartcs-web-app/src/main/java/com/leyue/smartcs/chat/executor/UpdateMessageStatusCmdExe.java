package com.leyue.smartcs.chat.executor;

import com.alibaba.cola.dto.Response;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.domain.chat.Message;
import com.leyue.smartcs.domain.chat.enums.MessageSendStatus;
import com.leyue.smartcs.domain.chat.gateway.MessageGateway;
import com.leyue.smartcs.dto.chat.UpdateMessageStatusCmd;
import com.leyue.smartcs.dto.chat.ws.MessageStatusUpdate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 更新消息状态命令执行器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UpdateMessageStatusCmdExe {

    private final MessageGateway messageGateway;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 执行状态更新操作
     */
    @Transactional
    public Response execute(UpdateMessageStatusCmd cmd) {
        try {
            // 查找消息
            Message message = messageGateway.findByMsgId(cmd.getMsgId());
            if (message == null) {
                return Response.buildFailure("MESSAGE_NOT_FOUND", "消息不存在");
            }

            // 根据状态码更新消息状态
            MessageSendStatus newStatus = MessageSendStatus.fromCode(cmd.getSendStatus());
            switch (newStatus) {
                case SENDING:
                    message.markAsSending();
                    break;
                case DELIVERED:
                    message.markAsDelivered();
                    break;
                case SEND_FAILED:
                    message.markAsSendFailed(cmd.getFailReason());
                    break;
                case READ:
                    // 已读状态应该通过markAsRead方法更新，这里只更新sendStatus
                    message.setSendStatus(MessageSendStatus.READ.getCode());
                    break;
                default:
                    return Response.buildFailure("INVALID_STATUS", "无效的消息状态");
            }

            // 保存更新
            boolean success = messageGateway.updateMessage(message);
            if (!success) {
                throw new BizException("MESSAGE_STATUS_UPDATE_FAILED", "消息状态更新失败");
            }

            // 发送状态更新通知
            sendStatusUpdateNotification(message, cmd.getSessionId());

            log.info("消息状态更新成功: msgId={}, status={}", 
                cmd.getMsgId(), newStatus.getDescription());
            return Response.buildSuccess();

        } catch (BizException e) {
            log.warn("更新消息状态业务异常: {}", e.getMessage());
            return Response.buildFailure(e.getErrCode(), e.getErrMessage());
        } catch (IllegalArgumentException e) {
            log.warn("无效的消息状态码: {}", cmd.getSendStatus());
            return Response.buildFailure("INVALID_STATUS_CODE", "无效的状态码");
        } catch (Exception e) {
            log.error("更新消息状态执行异常", e);
            return Response.buildFailure("SYSTEM_ERROR", "系统异常，请稍后重试");
        }
    }

    /**
     * 发送状态更新通知
     */
    private void sendStatusUpdateNotification(Message message, String sessionId) {
        try {
            MessageStatusUpdate statusUpdate = new MessageStatusUpdate();
            statusUpdate.setMsgId(message.getMsgId());
            statusUpdate.setSessionId(sessionId);
            statusUpdate.setSendStatus(message.getSendStatus().getCode());
            statusUpdate.setStatusText(message.getSendStatusText());
            statusUpdate.setStatusIcon(message.getSendStatusIcon());
            statusUpdate.setFailReason(message.getSendFailReason());
            statusUpdate.setRetryCount(message.getRetryCount());
            statusUpdate.setCanRetry(message.canRetry());
            statusUpdate.setUpdatedAt(System.currentTimeMillis());

            // 广播给会话中的相关用户
            String destination = "/topic/status/" + sessionId;
            messagingTemplate.convertAndSend(destination, statusUpdate);

            log.debug("消息状态更新通知已发送: msgId={}, status={}", 
                message.getMsgId(), message.getSendStatusText());

        } catch (Exception e) {
            log.error("发送消息状态更新通知失败: msgId={}", message.getMsgId(), e);
        }
    }
}