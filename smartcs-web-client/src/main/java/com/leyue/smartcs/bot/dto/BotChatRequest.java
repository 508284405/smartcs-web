package com.leyue.smartcs.bot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Bot聊天请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BotChatRequest {
    
    /**
     * 会话ID
     */
    private String sessionId;
    
    /**
     * 用户问题
     */
    private String question;
    
    /**
     * 是否包含历史消息
     */
    private Boolean includeHistory;
    
    /**
     * 模型ID（可选）
     */
    private String modelId;
    
    /**
     * 温度参数（可选）
     */
    private Float temperature;
    
    /**
     * 最大输出token数（可选）
     */
    private Integer maxTokens;
    
    /**
     * 知识库检索数量（可选）
     */
    private Integer topK;
} 