package com.leyue.smartcs.domain.chat.domainservice;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.domain.chat.Message;
import com.leyue.smartcs.domain.chat.Session;
import com.leyue.smartcs.domain.chat.enums.MessageType;
import com.leyue.smartcs.domain.chat.enums.SenderRole;
import com.leyue.smartcs.domain.chat.gateway.MessageGateway;
import com.leyue.smartcs.domain.chat.gateway.SessionGateway;
import com.leyue.smartcs.domain.common.gateway.IdGeneratorGateway;

import lombok.RequiredArgsConstructor;

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
        message.setMsgId(idGeneratorGateway.generateIdStr());
        message.setSessionId(sessionId);
        message.setMsgType(messageType);
        message.setContent(content);
        message.setCreatedAt(System.currentTimeMillis());
        
        // 发送消息
        String msgId = messageGateway.sendMessage(message);
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
    public List<Message> getSessionMessages(Long sessionId, String beforeMessageId, int limit) {
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
    
    /**
     * 撤回消息
     *
     * @param msgId 消息ID
     * @param userId 操作用户ID
     * @param reason 撤回原因
     * @return 撤回后的消息
     */
    public Message recallMessage(String msgId, String userId, String reason) {
        // 查找消息
        Optional<Message> messageOpt = messageGateway.findById(msgId);
        if (messageOpt.isEmpty()) {
            throw new BizException("消息不存在: " + msgId);
        }
        
        Message message = messageOpt.get();
        
        // 检查消息是否可以撤回
        if (!message.canRecall(userId)) {
            throw new BizException("消息无法撤回：超出时间限制或无权限");
        }
        
        // 更新撤回状态
        boolean updated = messageGateway.updateMessageRecallStatus(msgId, userId, reason);
        if (!updated) {
            throw new BizException("消息撤回失败");
        }
        
        // 重新获取更新后的消息
        return messageGateway.findById(msgId).orElseThrow(() -> new BizException("撤回后获取消息失败"));
    }
}
