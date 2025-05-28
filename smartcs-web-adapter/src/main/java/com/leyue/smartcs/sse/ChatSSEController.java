package com.leyue.smartcs.sse;

import com.leyue.smartcs.api.BotSSEService;
import com.leyue.smartcs.dto.bot.BotChatSSERequest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * SSE聊天控制器
 */
@RestController
@RequestMapping("/api/sse")
@RequiredArgsConstructor
@Slf4j
public class ChatSSEController {
    
    private final BotSSEService botSSEService;
    
    /**
     * 处理SSE聊天请求
     */
    @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatSSE(@RequestBody @Valid BotChatSSERequest request) {
        return (SseEmitter) botSSEService.chatSSE(request);
    }
    
    /**
     * 获取SSE连接状态
     */
    @GetMapping("/status")
    public String getStatus() {
        return "SSE服务正常运行";
    }
} 