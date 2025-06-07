package com.leyue.smartcs.web.bot;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.cola.dto.SingleResponse;
import com.leyue.smartcs.api.BotService;
import com.leyue.smartcs.dto.bot.BotChatRequest;
import com.leyue.smartcs.dto.bot.BotContextDTO;
import com.leyue.smartcs.dto.common.SingleClientObject;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Bot控制器
 */
@RestController
@RequestMapping("/api/bot")
@RequiredArgsConstructor
@Slf4j
public class BotController {
    
    private final BotService botService;
    
    /**
     * 处理聊天请求
     */
    @PostMapping("/chat")
    public SingleResponse<String> chat(@RequestBody BotChatRequest request) {
        log.info("收到聊天请求: {}", request);
        return botService.chat(request);
    }
    
    /**
     * 获取聊天上下文
     */
    @GetMapping("/context/{sessionId}")
    public SingleResponse<BotContextDTO> getContext(@PathVariable("sessionId") String sessionId) {
        log.info("获取聊天上下文: {}", sessionId);
        SingleClientObject<String> param = SingleClientObject.of(sessionId);
        return botService.getContext(param);
    }
    
    /**
     * 删除聊天上下文
     */
    @DeleteMapping("/context/{sessionId}")
    public SingleResponse<Boolean> deleteContext(@PathVariable("sessionId") String sessionId) {
        log.info("删除聊天上下文: {}", sessionId);
        SingleClientObject<String> param = SingleClientObject.of(sessionId);
        return botService.deleteContext(param);
    }
} 