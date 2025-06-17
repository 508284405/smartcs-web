package com.leyue.smartcs.sse;

import java.io.IOException;
import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerResponse;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.alibaba.fastjson2.JSON;
import com.leyue.smartcs.api.BotSSEService;
import com.leyue.smartcs.dto.bot.BotChatSSERequest;
import com.leyue.smartcs.dto.bot.SSEMessage;

import jakarta.servlet.ServletException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
    // @PostMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    // public SseEmitter chatSSE(@RequestBody @Valid BotChatSSERequest request) {
    // return (SseEmitter) botSSEService.chatSSE(request);
    // }

    @Bean
    public RouterFunction<ServerResponse> chatSSERouter() {
        return RouterFunctions.route()
                .POST("/api/sse/chat",
                        req -> ServerResponse.sse(sse -> {
                            // 解析参数
                            BotChatSSERequest request;
                            try {
                                request = req.body(BotChatSSERequest.class);
                            } catch (ServletException | IOException e) {
                                throw new RuntimeException(e);
                            }
                            Long sessionId = request.getSessionId();
                            if (sessionId == null) {
                                try {
                                    sse.send(SseEmitter.event()
                                            .id("error_" + sessionId)
                                            .name("error")
                                            .data(JSON.toJSONString(
                                                    SSEMessage.error(sessionId, "会话ID不能为空"))));
                                } catch (IOException e) {
                                    log.error("发送错误消息失败: {}", e.getMessage());
                                }
                                sse.complete();
                                return;
                            }
                            // 设置完成和超时回调
                            sse.onComplete(() -> {
                                log.info("SSE连接完成: sessionId={}", sessionId);
                            });

                            sse.onTimeout(() -> {
                                log.warn("SSE连接超时: sessionId={}", sessionId);
                                try {
                                    sse.send(SseEmitter.event()
                                            .id("timeout_" + sessionId)
                                            .name("timeout")
                                            .data(JSON.toJSONString(SSEMessage.error(sessionId, "连接超时"))));
                                } catch (IOException e) {
                                    log.error("发送超时消息失败: {}", e.getMessage());
                                }
                                sse.complete();
                            });

                            sse.onError((throwable) -> {
                                log.error("SSE连接错误: sessionId={}, error={}", sessionId, throwable.getMessage());
                                try {
                                    sse.send(SseEmitter.event()
                                            .id("error_" + sessionId)
                                            .name("error")
                                            .data(JSON.toJSONString(
                                                    SSEMessage.error(sessionId, throwable.getMessage()))));
                                } catch (IOException e) {
                                    log.error("发送错误消息失败: {}", e.getMessage());
                                }
                                sse.complete();
                            });

                            botSSEService.chatSSE(request,sse);
                        }, Duration.ofMinutes(5)))
                .build();
    }
}