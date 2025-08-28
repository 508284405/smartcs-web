package com.leyue.smartcs.chat.serviceimpl;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.leyue.smartcs.api.MessageSendService;
import com.leyue.smartcs.api.OfflineMessageService;
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
    private final OfflineMessageService offlineMessageService;
    private final MessageDistributionService messageDistributionService;

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
        chatMessage.setMsgId(messageIdStr);

        // 5. 通过跨节点分发服务处理消息投递
        messageDistributionService.publishDirectMessage(chatMessage, receiverId);

        // 6. 发布事件用于审计和统计
        messageDistributionService.publishEvent(java.util.Map.of(
                "type", "DIRECT_MESSAGE",
                "sessionId", chatMessage.getSessionId(),
                "msgId", messageIdStr,
                "fromUserId", chatMessage.getFromUserId(),
                "toUserId", receiverId,
                "timestamp", System.currentTimeMillis()
        ));
    }
    
    /**
     * 生成消息摘要
     * 
     * @param content 消息内容
     * @return 消息摘要
     */
    private String generateMessageBrief(String content) {
        if (content == null || content.isEmpty()) {
            return "[空消息]";
        }
        
        // 截取前50个字符作为摘要
        int maxLength = 50;
        if (content.length() <= maxLength) {
            return content;
        }
        
        return content.substring(0, maxLength) + "...";
    }
}
