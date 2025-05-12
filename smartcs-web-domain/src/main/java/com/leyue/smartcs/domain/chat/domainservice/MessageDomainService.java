package com.leyue.smartcs.domain.chat.domainservice;

import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.domain.chat.Message;
import com.leyue.smartcs.domain.chat.MessageType;
import com.leyue.smartcs.domain.chat.SenderRole;
import com.leyue.smartcs.domain.chat.Session;
import com.leyue.smartcs.domain.chat.gateway.MessageGateway;
import com.leyue.smartcs.domain.chat.gateway.SessionGateway;
import com.leyue.smartcs.domain.common.gateway.IdGeneratorGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * 消息领域服务
 */
@Service
@RequiredArgsConstructor
public class MessageDomainService {
    
    private final MessageGateway messageGateway;
    private final SessionGateway sessionGateway;
    private final SessionDomainService sessionDomainService;
    private final IdGeneratorGateway idGeneratorGateway;
    
    /**
     * 发送消息
     *
     * @param sessionId 会话ID
     * @param senderId 发送者ID
     * @param senderRole 发送者角色
     * @param content 消息内容
     * @return 发送的消息
     */
    public Message sendMessage(Long sessionId, Long senderId, SenderRole senderRole, String content) {
        return sendMessage(sessionId, senderId, senderRole, MessageType.TEXT, content, Collections.emptyList());
    }
    
    /**
     * 发送消息（包含消息类型和@列表）
     *
     * @param sessionId 会话ID
     * @param senderId 发送者ID
     * @param senderRole 发送者角色
     * @param messageType 消息类型
     * @param content 消息内容
     * @param atList @提及的用户列表
     * @return 发送的消息
     */
    public Message sendMessage(Long sessionId, Long senderId, SenderRole senderRole, 
                              MessageType messageType, String content, List<Long> atList) {
        // 检查会话是否存在
        Optional<Session> sessionOpt = sessionGateway.findBySessionId(sessionId);
        if (sessionOpt.isEmpty()) {
            throw new IllegalArgumentException("会话不存在: " + sessionId);
        }
        
        Session session = sessionOpt.get();
        if (session.isClosed()) {
            throw new IllegalStateException("会话已关闭，无法发送消息");
        }
        
        // 创建消息
        Message message = new Message();
        message.setMsgId(idGeneratorGateway.generateId());
        message.setSessionId(sessionId);
        message.setSenderId(senderId);
        message.setSenderRole(senderRole);
        message.setMsgType(messageType);
        message.setContent(content);
        message.setAtList(atList);
        message.setCreatedAt(System.currentTimeMillis());
        
        // 发送消息
        Long msgId = messageGateway.sendMessage(message);
        message.setMsgId(msgId);
        
        // 更新会话最后消息时间
        sessionDomainService.updateLastMessageTime(sessionId, message.getCreatedAt());
        
        return message;
    }
    
    /**
     * 获取会话消息历史
     *
     * @param sessionId 会话ID
     * @param beforeMessageId 消息ID，获取该消息之前的历史，为空则获取最新消息
     * @param limit 限制数量
     * @return 消息列表
     */
    public List<Message> getSessionMessages(Long sessionId, Long beforeMessageId, int limit) {
        // 检查会话是否存在
        Optional<Session> sessionOpt = sessionGateway.findBySessionId(sessionId);
        if (sessionOpt.isEmpty()) {
            throw new BizException("会话不存在: " + sessionId);
        }
        
        if (beforeMessageId == null) {
            return messageGateway.findMessagesBySessionId(sessionId, limit);
        } else {
            return messageGateway.findMessagesBySessionIdBeforeMessageId(sessionId, beforeMessageId, limit);
        }
    }
    
    /**
     * 获取会话最新消息历史
     *
     * @param sessionId 会话ID
     * @param limit 限制数量
     * @return 消息列表
     */
    public List<Message> getSessionMessages(Long sessionId, int limit) {
        return getSessionMessages(sessionId, null, limit);
    }
    
    /**
     * 分页获取会话消息历史
     *
     * @param sessionId 会话ID
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 消息列表
     */
    public List<Message> getSessionMessagesWithPagination(Long sessionId, int offset, int limit) {
        // 检查会话是否存在
        Optional<Session> sessionOpt = sessionGateway.findBySessionId(sessionId);
        if (!sessionOpt.isPresent()) {
            throw new IllegalArgumentException("会话不存在: " + sessionId);
        }
        
        return messageGateway.findMessagesBySessionIdWithPagination(sessionId, offset, limit);
    }
}
