package com.leyue.smartcs.dto.chat;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 会话DTO
 */
@Data
public class SessionDTO {
    /**
     * 会话ID
     */
    private Long sessionId;
    
    /**
     * 客户ID
     */
    private Long customerId;
    
    /**
     * 客户昵称
     */
    private String customerName;
    
    /**
     * 客户头像
     */
    private String customerAvatar;
    
    /**
     * 客服ID
     */
    private Long agentId;
    
    /**
     * 客服昵称
     */
    private String agentName;
    
    /**
     * 客服头像
     */
    private String agentAvatar;
    
    /**
     * 会话状态 0=排队 1=进行中 2=已结束
     */
    private Integer sessionState;
    
    /**
     * 最后消息时间
     */
    private LocalDateTime lastMsgTime;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
