package com.leyue.smartcs.app.service;

import com.leyue.smartcs.domain.app.entity.AppTestMessage;
import com.leyue.smartcs.domain.app.gateway.AppTestMessageGateway;
import com.leyue.smartcs.domain.app.gateway.AppTestSessionGateway;
import com.leyue.smartcs.domain.model.Model;
import com.leyue.smartcs.domain.model.Provider;
import com.leyue.smartcs.dto.app.AiAppChatCmd;
import com.leyue.smartcs.dto.app.AiAppDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 消息管理服务
 * 负责消息的创建、存储和管理
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MessageManager {

    private final AppTestMessageGateway appTestMessageGateway;
    private final AppTestSessionGateway appTestSessionGateway;

    /**
     * 创建增强的用户消息，包含完整的上下文信息
     */
    public AppTestMessage createEnhancedUserMessage(String messageId, String sessionId, AiAppChatCmd cmd, 
                                                   AiAppDTO app, Model model, Provider provider, String systemPrompt) {
        // 构建增强的变量信息，包含系统提示词和模型信息
        Map<String, Object> enhancedVariables = new HashMap<>();
        
        // 保存原始用户变量
        if (cmd.getVariables() != null) {
            enhancedVariables.putAll(cmd.getVariables());
        }
        
        // 添加系统提示词信息
        enhancedVariables.put("systemPrompt", systemPrompt);
        
        // 添加模型信息
        Map<String, Object> modelInfo = new HashMap<>();
        modelInfo.put("modelId", model.getId());
        modelInfo.put("modelName", model.getLabel());
        modelInfo.put("providerId", provider.getId());
        modelInfo.put("providerName", provider.getLabel());
        modelInfo.put("providerType", provider.getProviderType().name());
        enhancedVariables.put("modelInfo", modelInfo);
        
        // 添加应用信息
        Map<String, Object> appInfo = new HashMap<>();
        appInfo.put("appId", app.getId());
        appInfo.put("appName", app.getName());
        appInfo.put("appCode", app.getCode());
        if (app.getConfig() != null) {
            appInfo.put("appConfig", app.getConfig());
        }
        enhancedVariables.put("appInfo", appInfo);
        
        // 添加请求参数信息
        Map<String, Object> requestInfo = new HashMap<>();
        requestInfo.put("includeHistory", cmd.getIncludeHistory());
        requestInfo.put("enableRAG", cmd.getEnableRAG());
        if (cmd.getKnowledgeId() != null) {
            requestInfo.put("knowledgeId", cmd.getKnowledgeId());
        }
        if (cmd.getInferenceParams() != null) {
            requestInfo.put("inferenceParams", cmd.getInferenceParams());
        }
        requestInfo.put("timeout", cmd.getTimeout());
        enhancedVariables.put("requestInfo", requestInfo);
        
        // 创建用户消息
        long now = System.currentTimeMillis();
        return AppTestMessage.builder()
                .messageId(messageId)
                .sessionId(sessionId)
                .appId(cmd.getAppId())
                .messageType(AppTestMessage.MessageType.USER)
                .content(cmd.getMessage())
                .variables(enhancedVariables)
                .status(AppTestMessage.MessageStatus.SUCCESS)
                .timestamp(now)
                .cost(BigDecimal.ZERO)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    /**
     * 保存用户消息
     */
    public void saveUserMessage(AppTestMessage userMessage) {
        try {
            appTestMessageGateway.save(userMessage);
            log.info("保存用户消息: messageId={}, sessionId={}, contentLength={}", 
                    userMessage.getMessageId(), userMessage.getSessionId(), userMessage.getContent().length());
        } catch (Exception e) {
            log.error("保存用户消息失败: messageId={}, sessionId={}, error={}", 
                     userMessage.getMessageId(), userMessage.getSessionId(), e.getMessage(), e);
        }
    }

    /**
     * 保存AI助手消息
     */
    public void saveAssistantMessage(String messageId, String sessionId, Long appId, String content, 
                                   Model model, Integer processTime) {
        try {
            // 构建模型信息
            Map<String, Object> modelInfo = new HashMap<>();
            modelInfo.put("modelId", model.getId());
            modelInfo.put("modelName", model.getLabel());
            modelInfo.put("providerId", model.getProviderId());
            
            // 创建AI助手消息
            AppTestMessage assistantMessage = AppTestMessage.createAssistantMessage(
                    messageId, sessionId, appId, content, modelInfo, null, processTime, BigDecimal.ZERO);
            
            appTestMessageGateway.save(assistantMessage);
            log.info("保存AI助手消息: messageId={}, sessionId={}, contentLength={}", 
                    messageId, sessionId, content.length());
            
        } catch (Exception e) {
            log.error("保存AI助手消息失败: messageId={}, sessionId={}, error={}", 
                     messageId, sessionId, e.getMessage(), e);
        }
    }

    /**
     * 更新会话统计信息
     */
    public void updateSessionStats(String sessionId) {
        try {
            // 统计消息数量
            Integer messageCount = appTestMessageGateway.countMessagesBySessionId(sessionId);
            long now = System.currentTimeMillis();
            
            // 更新会话统计
            appTestSessionGateway.updateSessionStats(sessionId, messageCount, now, 0, BigDecimal.ZERO);
            log.info("更新会话统计: sessionId={}, messageCount={}", sessionId, messageCount);
            
        } catch (Exception e) {
            log.error("更新会话统计失败: sessionId={}, error={}", sessionId, e.getMessage(), e);
        }
    }

    /**
     * 获取消息统计
     */
    public MessageStats getMessageStats(String sessionId) {
        try {
            Integer messageCount = appTestMessageGateway.countMessagesBySessionId(sessionId);
            return new MessageStats(sessionId, messageCount);
        } catch (Exception e) {
            log.error("获取消息统计失败: sessionId={}, error={}", sessionId, e.getMessage(), e);
            return new MessageStats(sessionId, 0);
        }
    }

    /**
     * 消息统计信息
     */
    public static class MessageStats {
        public final String sessionId;
        public final int messageCount;
        
        public MessageStats(String sessionId, int messageCount) {
            this.sessionId = sessionId;
            this.messageCount = messageCount;
        }
        
        @Override
        public String toString() {
            return String.format("MessageStats{sessionId='%s', messageCount=%d}", sessionId, messageCount);
        }
    }
}