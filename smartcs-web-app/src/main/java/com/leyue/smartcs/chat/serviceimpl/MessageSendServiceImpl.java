package com.leyue.smartcs.chat.serviceimpl;

import com.leyue.smartcs.dto.chat.ws.ChatMessage;
import com.leyue.smartcs.config.websocket.WebSocketSessionManager;
import com.leyue.smartcs.domain.chat.Message;
import com.leyue.smartcs.domain.chat.enums.MessageType;
import com.leyue.smartcs.domain.chat.enums.SenderRole;
import com.leyue.smartcs.domain.chat.Session;
import com.leyue.smartcs.api.MessageSendService;
import com.leyue.smartcs.domain.chat.gateway.MessageGateway;
import com.leyue.smartcs.domain.chat.gateway.SessionGateway;
import com.leyue.smartcs.domain.common.gateway.IdGeneratorGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * 消息发送执行器实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageSendServiceImpl implements MessageSendService {

    private final WebSocketSessionManager sessionManager;
    private final MessageGateway messageGateway;
    private final SessionGateway sessionGateway;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final IdGeneratorGateway idGeneratorGateway;

    @Override
    public void send(ChatMessage chatMessage) {
        log.info("发送消息: {}", chatMessage);

        // 1. 获取会话信息
        Session session = sessionGateway.getSession(chatMessage.getSessionId());
        if (session == null) {
            throw new IllegalArgumentException("会话不存在: " + chatMessage.getSessionId());
        }

        if (session.isClosed()) {
            return;
        }

        // 2. 确定接收者ID
        String receiverId;
        if ("0".equalsIgnoreCase(chatMessage.getFromUserType())) {
            // 客户发送给客服
            receiverId = String.valueOf(session.getAgentId());
        } else {
            // 客服发送给客户
            receiverId = String.valueOf(session.getCustomerId());
        }

        // 3. 设置接收者ID
        chatMessage.setToUserId(receiverId);

        // 4. 保存消息到数据库
        Message message = convertToMessage(chatMessage);
        message.setMsgId(idGeneratorGateway.generateId());
        Long messageId = messageGateway.sendMessage(message);

        // 将Long类型的消息ID转换为String类型
        String messageIdStr = messageId != null ? messageId.toString() : chatMessage.getMsgId();

        // 5. 通过WebSocket发送给接收者
        if (sessionManager.isUserOnline(Long.valueOf(receiverId))) {
            sessionManager.sendToUser(receiverId, "messages", chatMessage);
        }

        // 6. 发送消息到Kafka用于异步处理（例如消息推送、统计等）
        kafkaTemplate.send("chat-messages", String.valueOf(chatMessage.getSessionId()), messageIdStr);

        log.info("消息发送成功: msgId={}, sessionId={}, from={}, to={}",
                messageIdStr, chatMessage.getSessionId(),
                chatMessage.getFromUserId(), chatMessage.getToUserId());
    }

    /**
     * 将ChatMessage转换为领域模型Message
     */
    private Message convertToMessage(ChatMessage chatMessage) {
        Message message = new Message();
        message.setAtList(Collections.singletonList(Long.parseLong(chatMessage.getToUserId())));

        // 转换ID字段（String->Long）
        if (chatMessage.getMsgId() != null) {
            try {
                message.setMsgId(Long.parseLong(chatMessage.getMsgId()));
            } catch (NumberFormatException e) {
                log.warn("消息ID转换失败: {}", chatMessage.getMsgId());
            }
        }

        if (chatMessage.getSessionId() != null) {
            try {
                message.setSessionId(chatMessage.getSessionId());
            } catch (NumberFormatException e) {
                log.warn("会话ID转换失败: {}", chatMessage.getSessionId());
            }
        }

        // 转换发送者ID
        if (chatMessage.getFromUserId() != null) {
            try {
                message.setSenderId(Long.parseLong(chatMessage.getFromUserId()));
            } catch (NumberFormatException e) {
                log.warn("发送者ID转换失败: {}", chatMessage.getFromUserId());
            }
        }

        // 转换发送者角色
        if ("0".equalsIgnoreCase(chatMessage.getFromUserType())) {
            message.setSenderRole(SenderRole.USER);
        } else if ("1".equalsIgnoreCase(chatMessage.getFromUserType())) {
            message.setSenderRole(SenderRole.AGENT);
        } else if ("2".equalsIgnoreCase(chatMessage.getFromUserType())) {
            message.setSenderRole(SenderRole.BOT);
        } else {
            // 默认为用户
            message.setSenderRole(SenderRole.USER);
        }

        // 转换消息类型
        if ("IMAGE".equalsIgnoreCase(chatMessage.getContentType())) {
            message.setMsgType(MessageType.IMAGE);
        } else if ("SYSTEM".equalsIgnoreCase(chatMessage.getContentType())) {
            message.setMsgType(MessageType.SYSTEM);
        } else if ("ORDER_CARD".equalsIgnoreCase(chatMessage.getContentType()) ||
                "CARD".equalsIgnoreCase(chatMessage.getContentType())) {
            message.setMsgType(MessageType.ORDER_CARD);
        } else {
            // 默认为文本
            message.setMsgType(MessageType.TEXT);
        }

        // 设置内容
        message.setContent(chatMessage.getContent());

        // 转换时间
        if (chatMessage.getCreateTime() != null) {
            message.setCreatedAt(chatMessage.getCreateTime());
        } else {
            message.setCreatedAt(System.currentTimeMillis());
        }

        return message;
    }
}
