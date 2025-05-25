package com.leyue.smartcs.sse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

/**
 * SSE测试控制器
 */
@RestController
@RequestMapping("/api/sse/test")
@Slf4j
public class SSETestController {
    
    /**
     * 简单的SSE测试接口
     */
    @GetMapping(value = "/simple", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter testSSE() {
        log.info("收到SSE测试请求");
        
        SseEmitter emitter = new SseEmitter(30000L); // 30秒超时
        
        CompletableFuture.runAsync(() -> {
            try {
                for (int i = 1; i <= 5; i++) {
                    Thread.sleep(1000); // 每秒发送一次
                    emitter.send(SseEmitter.event()
                            .id("test_" + i)
                            .name("message")
                            .data("这是第 " + i + " 条测试消息"));
                }
                
                emitter.send(SseEmitter.event()
                        .id("complete")
                        .name("complete")
                        .data("测试完成"));
                        
                emitter.complete();
            } catch (Exception e) {
                log.error("SSE测试失败: {}", e.getMessage());
                emitter.completeWithError(e);
            }
        });
        
        return emitter;
    }
} 