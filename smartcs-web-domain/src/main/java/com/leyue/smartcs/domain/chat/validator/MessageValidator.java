package com.leyue.smartcs.domain.chat.validator;

import com.leyue.smartcs.api.chat.dto.websocket.ChatMessage;

/**
 * 消息验证器接口
 */
public interface MessageValidator {
    
    /**
     * 验证消息
     * @param message 聊天消息
     * @throws IllegalArgumentException 当消息无效时抛出异常
     */
    void validate(ChatMessage message) throws IllegalArgumentException;
}
