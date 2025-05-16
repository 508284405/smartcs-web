package com.leyue.smartcs.api;

import com.leyue.smartcs.dto.chat.ws.ChatMessage;

/**
 * 消息发送执行器接口
 */
public interface MessageSendService {
    
    /**
     * 发送消息
     * @param message 聊天消息
     */
    void send(ChatMessage message);
}
