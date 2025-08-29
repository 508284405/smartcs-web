package com.leyue.smartcs.chat.executor;

import com.alibaba.cola.dto.Response;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.domain.chat.Message;
import com.leyue.smartcs.domain.chat.gateway.MessageGateway;
import com.leyue.smartcs.dto.chat.EditMessageCmd;
import com.leyue.smartcs.dto.chat.ws.EditMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 编辑消息命令执行器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EditMessageCmdExe {

    private final MessageGateway messageGateway;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 执行编辑消息操作
     */
    @Transactional
    public Response execute(EditMessageCmd cmd) {
        try {
            // 查找消息
            Message message = messageGateway.findByMsgId(cmd.getMsgId());
            if (message == null) {
                return Response.buildFailure("MESSAGE_NOT_FOUND", "消息不存在");
            }

            // 检查是否可以编辑
            if (!message.canEdit(cmd.getUserId())) {
                return Response.buildFailure("MESSAGE_CANNOT_EDIT", "消息无法编辑：可能是非文本消息、已撤回/删除或超过编辑时限");
            }

            // 执行编辑操作
            message.editContent(cmd.getNewContent());

            // 保存更新
            boolean success = messageGateway.updateMessage(message);
            if (!success) {
                throw new BizException("MESSAGE_EDIT_FAILED", "消息编辑失败");
            }

            // 发送WebSocket通知
            sendWebSocketNotification(message, cmd.getSessionId());

            log.info("消息编辑成功: msgId={}, userId={}", cmd.getMsgId(), cmd.getUserId());
            return Response.buildSuccess();

        } catch (BizException e) {
            log.warn("消息编辑业务异常: {}", e.getMessage());
            return Response.buildFailure(e.getErrCode(), e.getErrMessage());
        } catch (Exception e) {
            log.error("消息编辑执行异常", e);
            return Response.buildFailure("SYSTEM_ERROR", "系统异常，请稍后重试");
        }
    }

    /**
     * 发送WebSocket通知
     */
    private void sendWebSocketNotification(Message message, String sessionId) {
        try {
            EditMessage editMessage = new EditMessage();
            editMessage.setMsgId(message.getMsgId());
            editMessage.setSessionId(sessionId);
            editMessage.setNewContent(message.getContent());
            editMessage.setEditedAt(message.getEditedAt());

            // 广播给会话中的所有用户
            String destination = "/topic/messages/" + sessionId;
            messagingTemplate.convertAndSend(destination, editMessage);

            log.debug("编辑消息WebSocket通知已发送: msgId={}", message.getMsgId());

        } catch (Exception e) {
            log.error("发送编辑消息WebSocket通知失败: msgId={}", message.getMsgId(), e);
        }
    }
}