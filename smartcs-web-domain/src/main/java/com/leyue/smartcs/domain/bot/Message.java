package com.leyue.smartcs.domain.bot;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 消息领域模型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    
    /**
     * 消息ID
     */
    private String id;
    
    /**
     * 角色（user/assistant）
     */
    private String role;
    
    /**
     * 内容
     */
    private String content;
    
    /**
     * 创建时间（毫秒时间戳）
     */
    private Long createdAt;
} 