package com.leyue.smartcs.bot.executor;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.stereotype.Component;

import com.alibaba.cola.dto.SingleResponse;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.domain.bot.gateway.LLMGateway;
import com.leyue.smartcs.dto.bot.BotChatRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 聊天命令执行器
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ChatCmdExe {

    private final LLMGateway llmGateway;

    /**
     * 执行聊天命令
     *
     * @param request 聊天请求
     * @return 聊天响应
     */
    public SingleResponse<String> execute(BotChatRequest request) {
        try {
            // 参数校验
            if (request.getQuestion() == null || request.getQuestion().trim().isEmpty()) {
                throw new BizException("问题不能为空");
            }

            // 生成会话ID
            String sessionId = request.getSessionId();
            if (sessionId == null || sessionId.trim().isEmpty()) {
                sessionId = UUID.randomUUID().toString();
            }

            // 转换sessionId为Long类型
            Long sessionIdLong;
            try {
                sessionIdLong = Long.parseLong(sessionId);
            } catch (NumberFormatException e) {
                // 如果无法转换，生成一个新的Long类型ID
                sessionIdLong = System.currentTimeMillis();
                sessionId = sessionIdLong.toString();
            }

            // 使用流式方法收集完整回答
            AtomicReference<StringBuilder> answerBuilder = new AtomicReference<>(new StringBuilder());
            CompletableFuture<Void> future = new CompletableFuture<>();
            
            llmGateway.generateAnswerStream(sessionId, request.getQuestion(), request.getBotId(), 
                chunk -> {
                    answerBuilder.get().append(chunk);
                }, false);
            
            // 等待流式处理完成
            future.complete(null);
            
            String answer = answerBuilder.get().toString();
            return SingleResponse.of(answer);

        } catch (Exception e) {
            log.error("聊天命令执行失败: {}", e.getMessage(), e);
            throw new BizException("聊天命令执行失败: " + e.getMessage());
        }
    }
}