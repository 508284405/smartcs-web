package com.leyue.smartcs.api;

import com.alibaba.cola.dto.SingleResponse;
import com.leyue.smartcs.dto.bot.BotChatRequest;
import com.leyue.smartcs.dto.bot.BotChatResponse;
import com.leyue.smartcs.dto.bot.BotContextDTO;
import com.leyue.smartcs.dto.common.SingleClientObject;

/**
 * Bot服务接口
 */
public interface BotService {
    
    /**
     * 处理聊天请求
     * @param request 聊天请求
     * @return 聊天响应
     */
    SingleResponse<BotChatResponse> chat(BotChatRequest request);
    
    /**
     * 获取聊天上下文
     * @param sessionId 会话ID
     * @return 聊天上下文
     */
    SingleResponse<BotContextDTO> getContext(SingleClientObject<String> sessionId);
    
    /**
     * 删除聊天上下文
     * @param sessionId 会话ID
     * @return 操作结果
     */
    SingleResponse<Boolean> deleteContext(SingleClientObject<String> sessionId);
} 