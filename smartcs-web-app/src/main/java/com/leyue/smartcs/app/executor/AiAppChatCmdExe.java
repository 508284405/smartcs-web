package com.leyue.smartcs.app.executor;

import com.leyue.smartcs.app.service.SmartChatService;
import com.leyue.smartcs.domain.common.gateway.IdGeneratorGateway;
import com.leyue.smartcs.dto.app.AiAppChatCmd;
import com.leyue.smartcs.dto.app.AiAppChatResponse;
import com.leyue.smartcs.dto.app.AiAppChatSSEMessage;
import dev.langchain4j.service.TokenStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

/**
 * AI应用聊天命令执行器
 * 重构版本：完全基于LangChain4j框架的SmartChatService
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AiAppChatCmdExe {

    private final SmartChatService smartChatService;
    private final IdGeneratorGateway idGeneratorGateway;

    /**
     * 执行SSE聊天
     * 简化版本：直接使用SmartChatService的流式能力
     */
    public SseEmitter execute(AiAppChatCmd cmd) {
        SseEmitter emitter = new SseEmitter(cmd.getTimeout() != null ? cmd.getTimeout() : 30000L);
        String sessionId = generateSessionId(cmd);
        
        log.info("开始执行AI应用聊天: appId={}, sessionId={}, message length={}",
                cmd.getAppId(), sessionId, cmd.getMessage().length());

        CompletableFuture.runAsync(() -> {
            try {
                sendSSEMessage(emitter, AiAppChatSSEMessage.start(sessionId));
                
                // 直接使用SmartChatService的流式聊天
                processChatStream(emitter, cmd, sessionId);
                
            } catch (Exception e) {
                handleError(emitter, cmd.getAppId(), sessionId, e);
            }
        });

        setupEmitterCallbacks(emitter, cmd.getAppId(), sessionId);
        return emitter;
    }

    /**
     * 处理流式聊天
     * 使用LangChain4j原生TokenStream，框架自动处理RAG和记忆
     */
    private void processChatStream(SseEmitter emitter, AiAppChatCmd cmd, String sessionId) throws Exception {
        sendSSEMessage(emitter, AiAppChatSSEMessage.progress(sessionId, "正在生成AI回答..."));
        
        // 使用SmartChatService的流式聊天 - 框架自动处理RAG和记忆
        TokenStream tokenStream = smartChatService.chatStream(sessionId, cmd.getMessage());
        StringBuilder fullResponse = new StringBuilder();
        
        tokenStream
            .onPartialResponse(partialResponse -> {
                try {
                    fullResponse.append(partialResponse);
                    AiAppChatResponse dataResponse = AiAppChatResponse.builder()
                            .sessionId(sessionId)
                            .content(partialResponse)
                            .finished(false)
                            .timestamp(System.currentTimeMillis())
                            .build();
                    sendSSEMessage(emitter, AiAppChatSSEMessage.data(sessionId, dataResponse));
                } catch (IOException e) {
                    log.error("发送流式消息失败: sessionId={}", sessionId, e);
                }
            })
            .onCompleteResponse(response -> {
                try {
                    log.info("AI聊天完成: sessionId={}, responseLength={}", 
                            sessionId, fullResponse.length());
                    AiAppChatResponse completeResponse = AiAppChatResponse.builder()
                            .sessionId(sessionId)
                            .content(fullResponse.toString())
                            .finished(true)
                            .timestamp(System.currentTimeMillis())
                            .build();
                    sendSSEMessage(emitter, AiAppChatSSEMessage.complete(sessionId, completeResponse));
                    emitter.complete();
                } catch (IOException e) {
                    log.error("发送完成消息失败: sessionId={}", sessionId, e);
                    emitter.completeWithError(e);
                }
            })
            .onError(throwable -> {
                log.error("AI聊天流式处理出错: sessionId={}", sessionId, throwable);
                try {
                    sendSSEMessage(emitter, AiAppChatSSEMessage.error(sessionId, 
                        "聊天处理失败: " + throwable.getMessage()));
                } catch (IOException e) {
                    log.error("发送错误消息失败", e);
                }
                emitter.completeWithError(throwable);
            })
            .start();
    }

    /**
     * 生成会话ID
     */
    private String generateSessionId(AiAppChatCmd cmd) {
        return idGeneratorGateway.generateIdStr();
    }

    /**
     * 处理错误
     */
    private void handleError(SseEmitter emitter, Long appId, String sessionId, Exception e) {
        log.error("AI应用聊天处理失败: appId={}, sessionId={}, error={}", 
                 appId, sessionId, e.getMessage(), e);
        try {
            sendSSEMessage(emitter, AiAppChatSSEMessage.error(sessionId, "聊天处理失败: " + e.getMessage()));
        } catch (IOException ioException) {
            log.error("发送错误消息失败", ioException);
        }
        emitter.completeWithError(e);
    }

    /**
     * 设置发射器回调
     */
    private void setupEmitterCallbacks(SseEmitter emitter, Long appId, String sessionId) {
        emitter.onTimeout(() -> {
            log.warn("AI应用聊天超时: appId={}, sessionId={}", appId, sessionId);
            try {
                sendSSEMessage(emitter, AiAppChatSSEMessage.timeout(sessionId));
            } catch (IOException e) {
                log.error("发送超时消息失败", e);
            }
            emitter.complete();
        });

        emitter.onCompletion(() -> {
            log.info("AI应用聊天完成: appId={}, sessionId={}", appId, sessionId);
        });

        emitter.onError(throwable -> {
            log.error("AI应用聊天出错: appId={}, sessionId={}, error={}", 
                     appId, sessionId, throwable.getMessage(), throwable);
        });
    }

    /**
     * 发送SSE消息
     */
    private void sendSSEMessage(SseEmitter emitter, AiAppChatSSEMessage message) throws IOException {
        emitter.send(SseEmitter.event()
                .id(message.getId())
                .name(message.getType().getValue())
                .data(message));
    }
}