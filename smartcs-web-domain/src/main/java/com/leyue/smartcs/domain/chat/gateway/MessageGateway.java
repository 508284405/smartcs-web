package com.leyue.smartcs.domain.chat.gateway;

import com.alibaba.cola.dto.PageResponse;
import com.leyue.smartcs.domain.chat.Message;
import com.leyue.smartcs.dto.chat.MessageSearchQry;
import com.leyue.smartcs.dto.chat.MessageSearchResult;
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
    String sendMessage(Message message);
    
    /**
     * 根据ID查询消息
     * 
     * @param msgId 消息ID
     * @return 消息对象
     */
    Optional<Message> findById(String msgId);
    
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
    List<Message> findMessagesBySessionIdBeforeMessageId(Long sessionId, String beforeMessageId, int limit);
    
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
    
    /**
     * 更新消息撤回状态
     * 
     * @param msgId 消息ID
     * @param recalledBy 撤回操作者ID
     * @param recallReason 撤回原因
     * @return 是否成功
     */
    boolean updateMessageRecallStatus(String msgId, String recalledBy, String recallReason);

    /**
     * 根据消息ID查找消息
     * 
     * @param msgId 消息ID
     * @return 消息对象
     */
    Message findByMsgId(String msgId);

    /**
     * 更新消息
     * 
     * @param message 消息对象
     * @return 是否成功
     */
    boolean updateMessage(Message message);
    
    /**
     * 搜索消息
     * 
     * @param qry 搜索条件
     * @return 搜索结果分页数据
     */
    PageResponse<MessageSearchResult> searchMessages(MessageSearchQry qry);
}
