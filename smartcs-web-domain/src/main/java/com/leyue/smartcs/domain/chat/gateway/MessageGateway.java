package com.leyue.smartcs.domain.chat.gateway;

import com.leyue.smartcs.domain.chat.Message;
import java.util.List;
import java.util.Optional;

/**
 * 消息网关接口
 */
public interface MessageGateway {
    /**
     * 发送消息
     * 
     * @param message 消息对象
     * @return 消息ID
     */
    Long sendMessage(Message message);
    
    /**
     * 根据ID查询消息
     * 
     * @param msgId 消息ID
     * @return 消息对象
     */
    Optional<Message> findById(Long msgId);
    
    /**
     * 根据会话ID查询消息列表
     * 
     * @param sessionId 会话ID
     * @param limit 限制数量
     * @return 消息列表
     */
    List<Message> findMessagesBySessionId(Long sessionId, int limit);
    
    /**
     * 根据会话ID和消息ID查询该消息之前的消息列表
     * 
     * @param sessionId 会话ID
     * @param beforeMessageId 消息ID，获取该消息之前的历史
     * @param limit 限制数量
     * @return 消息列表
     */
    List<Message> findMessagesBySessionIdBeforeMessageId(Long sessionId, Long beforeMessageId, int limit);
    
    /**
     * 根据会话ID分页查询消息
     * 
     * @param sessionId 会话ID
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 消息列表
     */
    List<Message> findMessagesBySessionIdWithPagination(Long sessionId, int offset, int limit);
    
    /**
     * 批量存储消息
     * 
     * @param messages 消息列表
     * @return 是否成功
     */
    boolean batchSaveMessages(List<Message> messages);
}
