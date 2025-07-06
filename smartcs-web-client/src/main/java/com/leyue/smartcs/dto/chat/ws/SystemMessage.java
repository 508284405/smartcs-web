package com.leyue.smartcs.dto.chat.ws;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统消息
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SystemMessage extends WebSocketMessage {
    
    /**
     * 消息ID
     */
    private String msgId;
    
    /**
     * 会话ID
     */
    private String sessionId;
    
    /**
     * 消息代码，用于客户端翻译
     */
    private String code;
    
    /**
     * 消息内容
     */
    private String content;
    
    /**
     * 附加数据，JSON格式
     */
    private String data;
    
    /**
     * 创建时间
     */
    private Long createTime;
    
    public SystemMessage() {
        super.setType("SYSTEM");
    }
}
