package com.leyue.smartcs.api.chat.dto.websocket;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

/**
 * WebSocket消息基类
 */
@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = ChatMessage.class, name = "CHAT"),
    @JsonSubTypes.Type(value = SystemMessage.class, name = "SYSTEM"),
    @JsonSubTypes.Type(value = AckMessage.class, name = "ACK"),
    @JsonSubTypes.Type(value = SessionStatusMessage.class, name = "SESSION_STATUS")
})
public abstract class WebSocketMessage {
    
    /**
     * 消息类型
     */
    private String type;
    
    /**
     * 时间戳
     */
    private Long timestamp = System.currentTimeMillis();
}
