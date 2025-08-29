package com.leyue.smartcs.chat.executor;

import com.alibaba.cola.dto.Response;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.domain.chat.Message;
import com.leyue.smartcs.domain.chat.gateway.MessageGateway;
import com.leyue.smartcs.dto.chat.RetryMessageSendCmd;
import com.leyue.smartcs.dto.chat.ws.MessageStatusUpdate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 重试发送消息命令执行器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RetryMessageSendCmdExe {

    private final MessageGateway messageGateway;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 执行重试发送操作
     */
    @Transactional
    public Response execute(RetryMessageSendCmd cmd) {
        try {
            // 查找消息
            Message message = messageGateway.findByMsgId(cmd.getMsgId());
            if (message == null) {
                return Response.buildFailure("MESSAGE_NOT_FOUND", "消息不存在");
            }

            // 检查是否可以重试
            if (!message.canRetry()) {
                return Response.buildFailure("MESSAGE_CANNOT_RETRY", 
                    "消息无法重试：可能不是失败状态或已达最大重试次数");
            }

            // 执行重试操作
            message.retry();

            // 保存更新
            boolean success = messageGateway.updateMessage(message);
            if (!success) {
                throw new BizException("MESSAGE_RETRY_FAILED", "消息重试失败");
            }

            // 发送状态更新通知
            sendStatusUpdateNotification(message, cmd.getSessionId());

            // 这里应该触发实际的消息重发逻辑
            // 比如重新调用消息发送服务
            // messageSendService.resend(message);

            log.info("消息重试发送成功: msgId={}, userId={}, retryCount={}", 
                cmd.getMsgId(), cmd.getUserId(), message.getRetryCount());
            return Response.buildSuccess();

        } catch (BizException e) {
            log.warn("重试发送消息业务异常: {}", e.getMessage());
            return Response.buildFailure(e.getErrCode(), e.getErrMessage());
        } catch (Exception e) {
            log.error("重试发送消息执行异常", e);
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