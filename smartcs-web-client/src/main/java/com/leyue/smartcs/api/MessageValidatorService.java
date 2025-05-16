package com.leyue.smartcs.api;

import com.leyue.smartcs.dto.chat.ws.ChatMessage;

/**
 * 消息验证器接口
 */
public interface MessageValidatorService {
    
    /**
     * 验证消息
     * @param message 聊天消息
     * @throws IllegalArgumentException 当消息无效时抛出异常
     */
    void validate(ChatMessage message) throws IllegalArgumentException;
}
