package com.leyue.smartcs.api;

import com.leyue.smartcs.dto.bot.BotChatSSERequest;

/**
 * Bot SSE服务接口
 */
public interface BotSSEService {
    
    /**
     * 处理SSE聊天请求
     * @param request SSE聊天请求
     * @return SSE发射器对象
     */
    Object chatSSE(BotChatSSERequest request);
} 