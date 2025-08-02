package com.leyue.smartcs.domain.app.gateway;

import com.leyue.smartcs.domain.app.entity.AppTestMessage;

import java.util.List;

/**
 * AI应用测试消息网关接口
 */
public interface AppTestMessageGateway {
    
    /**
     * 保存消息
     * @param message 消息信息
     * @return 保存的消息
     */
    AppTestMessage save(AppTestMessage message);
    
    /**
     * 批量保存消息
     * @param messages 消息列表
     * @return 保存的消息数量
     */
    int batchSave(List<AppTestMessage> messages);
    
    /**
     * 根据消息ID查询消息
     * @param messageId 消息ID
     * @return 消息信息
     */
    AppTestMessage findByMessageId(String messageId);
    
    /**
     * 根据会话ID查询消息列表
     * @param sessionId 会话ID
     * @param limit 限制数量
     * @param offset 偏移量
     * @return 消息列表
     */
    List<AppTestMessage> findMessagesBySessionId(String sessionId, Integer limit, Integer offset);
    
    /**
     * 根据会话ID统计消息数量
     * @param sessionId 会话ID
     * @return 消息数量
     */
    Integer countMessagesBySessionId(String sessionId);
    
    /**
     * 根据会话ID获取最新的消息
     * @param sessionId 会话ID
     * @param messageType 消息类型（可选）
     * @return 最新消息
     */
    AppTestMessage findLatestMessageBySessionId(String sessionId, String messageType);
    
    /**
     * 更新消息状态
     * @param messageId 消息ID
     * @param status 消息状态
     * @param errorMessage 错误信息
     * @return 是否更新成功
     */
    boolean updateMessageStatus(String messageId, String status, String errorMessage);
}