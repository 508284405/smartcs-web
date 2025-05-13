package com.leyue.smartcs.bot.service;

import com.alibaba.cola.dto.SingleResponse;
import com.leyue.smartcs.bot.api.BotService;
import com.leyue.smartcs.bot.dto.BotChatRequest;
import com.leyue.smartcs.bot.dto.BotChatResponse;
import com.leyue.smartcs.bot.dto.BotContextDTO;
import com.leyue.smartcs.bot.executor.ChatCmdExe;
import com.leyue.smartcs.bot.executor.ContextQryExe;
import com.leyue.smartcs.dto.common.SingleClientObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Bot服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BotServiceImpl implements BotService {
    
    private final ChatCmdExe chatCmdExe;
    private final ContextQryExe contextQryExe;
    
    @Override
    public SingleResponse<BotChatResponse> chat(BotChatRequest request) {
        log.info("处理聊天请求: {}", request);
        return chatCmdExe.execute(request);
    }
    
    @Override
    public SingleResponse<BotContextDTO> getContext(SingleClientObject<String> sessionId) {
        log.info("获取聊天上下文: {}", sessionId);
        return contextQryExe.execute(sessionId);
    }
    
    @Override
    public SingleResponse<Boolean> deleteContext(SingleClientObject<String> sessionId) {
        log.info("删除聊天上下文: {}", sessionId);
        return contextQryExe.executeDelete(sessionId);
    }
} 