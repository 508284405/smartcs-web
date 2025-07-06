package com.leyue.smartcs.dto.chat.ws;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * WebSocket消息基类
 */
@Getter
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

    public void setType(String type) {
        this.type = type;
    }

    /**
     * 时间戳
     */
    @Setter
    private Long timestamp = System.currentTimeMillis();
}
