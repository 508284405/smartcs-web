package com.leyue.smartcs.domain.chat.executor;

import com.leyue.smartcs.api.chat.dto.websocket.ChatMessage;

/**
 * 消息发送执行器接口
 */
public interface MessageSendExecutor {
    
    /**
     * 发送消息
     * @param message 聊天消息
     */
    void send(ChatMessage message);
}
