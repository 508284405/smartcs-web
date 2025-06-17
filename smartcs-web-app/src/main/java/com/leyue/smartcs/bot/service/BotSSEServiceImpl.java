package com.leyue.smartcs.bot.service;

import com.alibaba.fastjson2.JSON;
import com.leyue.smartcs.api.BotSSEService;
import com.leyue.smartcs.bot.executor.ChatSSECmdExe;
import com.leyue.smartcs.dto.bot.BotChatSSERequest;
import com.leyue.smartcs.dto.bot.SSEMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Bot SSE服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BotSSEServiceImpl implements BotSSEService {

    private final ChatSSECmdExe chatSSECmdExe;

    private final Executor commonThreadPoolExecutor;

    @Override
    public Object chatSSE(BotChatSSERequest request) {
        // 生成会话ID
        Long sessionId = request.getSessionId();
        if (sessionId == null) {
            return SSEMessage.error(null, "会话ID不能为空");
        }

        // 创建SSE发射器
        SseEmitter emitter = new SseEmitter(500000L);

        // 设置完成和超时回调
        emitter.onCompletion(() -> {
            log.info("SSE连接完成: sessionId={}", sessionId);
        });

        emitter.onTimeout(() -> {
            log.warn("SSE连接超时: sessionId={}", sessionId);
            try {
                emitter.send(SseEmitter.event()
                        .id("timeout_" + sessionId)
                        .name("timeout")
                        .data(JSON.toJSONString(SSEMessage.error(sessionId, "连接超时"))));
            } catch (IOException e) {
                log.error("发送超时消息失败: {}", e.getMessage());
            }
            emitter.complete();
        });

        emitter.onError((throwable) -> {
            log.error("SSE连接错误: sessionId={}, error={}", sessionId, throwable.getMessage());
            try {
                emitter.send(SseEmitter.event()
                        .id("error_" + sessionId)
                        .name("error")
                        .data(JSON.toJSONString(SSEMessage.error(sessionId, throwable.getMessage()))));
            } catch (IOException e) {
                log.error("发送错误消息失败: {}", e.getMessage());
            }
            emitter.complete();
        });

        // 异步处理聊天请求
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        CompletableFuture.runAsync(() -> {
            RequestContextHolder.setRequestAttributes(attributes);
            try {
                // 发送开始消息
                emitter.send(SseEmitter.event()
                        .id("start_" + sessionId)
                        .name("start")
                        .data(JSON.toJSONString(SSEMessage.start(sessionId))));

                // 执行聊天命令
                chatSSECmdExe.execute(request, emitter);

            } catch (Exception e) {
                log.error("SSE聊天处理失败: sessionId={}, error={}", sessionId, e.getMessage(), e);
                try {
                    emitter.send(SseEmitter.event()
                            .id("error_" + sessionId)
                            .name("error")
                            .data(JSON.toJSONString(SSEMessage.error(sessionId, e.getMessage()))));
                } catch (IOException ioException) {
                    log.error("发送错误消息失败: {}", ioException.getMessage());
                }
                emitter.complete();
            } finally {
                RequestContextHolder.resetRequestAttributes();
            }
        }, commonThreadPoolExecutor);

        return emitter;
    }
} 