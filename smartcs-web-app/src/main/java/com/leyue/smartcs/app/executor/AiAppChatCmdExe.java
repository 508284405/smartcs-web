package com.leyue.smartcs.app.executor;

import com.leyue.smartcs.app.security.ChatSecurityValidator;
import com.leyue.smartcs.app.service.*;
import com.leyue.smartcs.domain.app.entity.AppTestMessage;
import com.leyue.smartcs.domain.app.entity.AppTestSession;
import com.leyue.smartcs.domain.model.Model;
import com.leyue.smartcs.domain.model.Provider;
import com.leyue.smartcs.dto.app.AiAppChatCmd;
import com.leyue.smartcs.dto.app.AiAppChatSSEMessage;
import com.leyue.smartcs.dto.app.AiAppDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * AI应用聊天命令执行器
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AiAppChatCmdExe {

    private final ChatSecurityValidator securityValidator;
    private final ChatConfigurationService configurationService;
    private final SessionManager sessionManager;
    private final MessageManager messageManager;
    private final AiAppChatApplicationService chatService;

    /**
     * 执行SSE聊天
     */
    public SseEmitter execute(AiAppChatCmd cmd) {
        SseEmitter emitter = new SseEmitter(cmd.getTimeout());
        String sessionId = sessionManager.generateSessionId(cmd.getAppId(), cmd.getSessionId());
        
        log.info("开始执行AI应用聊天: appId={}, sessionId={}, message length={}",
                cmd.getAppId(), sessionId, cmd.getMessage().length());

        CompletableFuture.runAsync(() -> {
            try {
                sendSSEMessage(emitter, AiAppChatSSEMessage.start(sessionId));
                
                // 安全验证
                if (!validateInput(cmd, sessionId, emitter)) {
                    return;
                }
                
                // 处理聊天
                processChatAsync(emitter, cmd, sessionId);
                
            } catch (Exception e) {
                handleError(emitter, cmd.getAppId(), sessionId, e);
            }
        });

        setupEmitterCallbacks(emitter, cmd.getAppId(), sessionId);
        return emitter;
    }

    /**
     * 异步处理聊天
     */
    private void processChatAsync(SseEmitter emitter, AiAppChatCmd cmd, String sessionId) throws Exception {
        long startTime = System.currentTimeMillis();
        
        // 1. 获取配置信息
        sendSSEMessage(emitter, AiAppChatSSEMessage.progress(sessionId, "正在加载应用配置..."));
        ChatConfigurationService.ConfigurationValidationResult configResult = 
            configurationService.validateConfiguration(cmd.getAppId(), cmd.getModelId());
        
        if (!configResult.isValid()) {
            throw new RuntimeException("配置验证失败: " + configResult.getErrorMessage());
        }
        
        ChatConfigurationService.ChatConfiguration config = configResult.getConfiguration();
        AiAppDTO app = config.app;
        Model model = config.model;
        Provider provider = config.provider;
        
        // 2. 构建系统提示词
        sendSSEMessage(emitter, AiAppChatSSEMessage.progress(sessionId, "正在构建对话上下文..."));
        String systemPrompt = configurationService.buildSystemPrompt(app, cmd.getVariables());
        
        // 3. 初始化会话
        sendSSEMessage(emitter, AiAppChatSSEMessage.progress(sessionId, "正在初始化会话..."));
        AppTestSession session = sessionManager.initializeSession(sessionId, cmd.getAppId(), model.getId(), cmd);
        
        // 4. 保存用户消息
        String userMessageId = UUID.randomUUID().toString().replace("-", "");
        AppTestMessage userMessage = messageManager.createEnhancedUserMessage(
            userMessageId, sessionId, cmd, app, model, provider, systemPrompt);
        messageManager.saveUserMessage(userMessage);
        
        // 5. 执行聊天
        sendSSEMessage(emitter, AiAppChatSSEMessage.progress(sessionId, "正在生成AI回答..."));
        executeChatStrategy(emitter, cmd, sessionId, systemPrompt, startTime, model);
    }

    /**
     * 执行聊天策略
     */
    private void executeChatStrategy(SseEmitter emitter, AiAppChatCmd cmd,
                                   String sessionId, String systemPrompt, long startTime, Model model) throws IOException {
        
        if (cmd.getIncludeHistory()) {
            if (cmd.getEnableRAG() && cmd.getKnowledgeId() != null) {
                chatService.executeRagMemoryChat(emitter, sessionId, systemPrompt, 
                    cmd.getMessage(), cmd.getVariables(), cmd.getKnowledgeId(), 
                    startTime, cmd.getAppId(), model, messageManager);
            } else {
                chatService.executeMemoryChat(emitter, sessionId, systemPrompt, 
                    cmd.getMessage(), cmd.getVariables(), startTime, cmd.getAppId(), model, messageManager);
            }
        } else {
            String tempSessionId = "temp_" + (cmd.getEnableRAG() ? "rag_" : "") + 
                                  UUID.randomUUID().toString().replace("-", "");
            
            if (cmd.getEnableRAG() && cmd.getKnowledgeId() != null) {
                chatService.executeRagMemoryChat(emitter, tempSessionId, systemPrompt, 
                    cmd.getMessage(), cmd.getVariables(), cmd.getKnowledgeId(), 
                    startTime, cmd.getAppId(), model, messageManager);
            } else {
                chatService.executeMemoryChat(emitter, tempSessionId, systemPrompt, 
                    cmd.getMessage(), cmd.getVariables(), startTime, cmd.getAppId(), model, messageManager);
            }
        }
    }

    /**
     * 验证输入
     */
    private boolean validateInput(AiAppChatCmd cmd, String sessionId, SseEmitter emitter) {
        try {
            ChatSecurityValidator.ValidationResult validationResult = 
                securityValidator.validateChatInput(cmd.getMessage(), cmd.getVariables(), sessionId);
            
            if (!validationResult.isValid()) {
                log.warn("聊天输入安全验证失败: sessionId={}, error={}", 
                        sessionId, validationResult.getErrorMessage());
                sendSSEMessage(emitter, AiAppChatSSEMessage.error(sessionId, 
                    "输入验证失败: " + validationResult.getErrorMessage()));
                emitter.complete();
                return false;
            }
            
            // 清理输入内容
            String sanitizedMessage = securityValidator.sanitizeInput(cmd.getMessage());
            cmd.setMessage(sanitizedMessage);
            return true;
            
        } catch (Exception e) {
            log.error("输入验证失败: sessionId={}, error={}", sessionId, e.getMessage(), e);
            try {
                sendSSEMessage(emitter, AiAppChatSSEMessage.error(sessionId, "输入验证失败"));
                emitter.complete();
            } catch (IOException ioException) {
                log.error("发送错误消息失败", ioException);
            }
            return false;
        }
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
            sessionManager.updateSessionStatus(sessionId, AppTestSession.SessionState.FINISHED);
        });

        emitter.onError(throwable -> {
            log.error("AI应用聊天出错: appId={}, sessionId={}, error={}", 
                     appId, sessionId, throwable.getMessage(), throwable);
            sessionManager.updateSessionStatus(sessionId, AppTestSession.SessionState.EXPIRED);
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