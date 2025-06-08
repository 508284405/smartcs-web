package com.leyue.smartcs.chat.serviceimpl;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.leyue.smartcs.api.MessageSendService;
import com.leyue.smartcs.chat.convertor.ChatMessageConvertor;
import com.leyue.smartcs.config.websocket.WebSocketSessionManager;
import com.leyue.smartcs.domain.chat.Message;
import com.leyue.smartcs.domain.chat.Session;
import com.leyue.smartcs.domain.chat.gateway.MessageGateway;
import com.leyue.smartcs.domain.chat.gateway.SessionGateway;
import com.leyue.smartcs.domain.common.gateway.IdGeneratorGateway;
import com.leyue.smartcs.dto.chat.ws.ChatMessage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
    private final ChatMessageConvertor chatMessageConvertor;

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
        if ("User".equalsIgnoreCase(chatMessage.getChatType())) {
            // 客户发送给客服
            receiverId = String.valueOf(session.getAgentId());
        } else {
            // 客服发送给客户
            receiverId = String.valueOf(session.getCustomerId());
        }

        // 3. 设置接收者ID
        chatMessage.setToUserId(receiverId);

        // 4. 保存消息到数据库
        Message message = chatMessageConvertor.toMessage(chatMessage);
        message.setMsgId(idGeneratorGateway.generateIdStr());
        String messageId = messageGateway.sendMessage(message);

        // 使用返回的消息ID或原有的消息ID
        String messageIdStr = messageId != null ? messageId : chatMessage.getMsgId();

        // 5. 通过WebSocket发送给接收者
        if (sessionManager.isUserOnline(Long.valueOf(receiverId))) {
            sessionManager.sendToUser(receiverId, "messages", chatMessage);
        }

        // 6. 发送消息到Kafka用于异步处理（例如消息推送、统计等）
        kafkaTemplate.send("chat-messages", String.valueOf(chatMessage.getSessionId()), messageIdStr);
    }
}
