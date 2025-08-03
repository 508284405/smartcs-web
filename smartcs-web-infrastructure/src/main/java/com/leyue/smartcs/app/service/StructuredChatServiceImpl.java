package com.leyue.smartcs.app.service;

import com.leyue.smartcs.domain.app.model.StructuredChatResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 结构化聊天服务实现 - 基于AiServices方案
 * 使用StructuredChatServiceAi的AiServices实现，提供业务层封装
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StructuredChatServiceImpl implements com.leyue.smartcs.domain.app.service.StructuredChatService {

    private final StructuredChatServiceAi structuredChatServiceAi;

    @Override
    public StructuredChatResponse generateStructuredResponse(String sessionId, String systemPrompt, 
                                                           String userMessage, Map<String, Object> variables) {
        try {
            log.debug("生成结构化响应: sessionId={}, messageLength={}", sessionId, userMessage.length());
            
            return structuredChatServiceAi.generateStructuredResponse(sessionId, systemPrompt, userMessage, variables);
            
        } catch (Exception e) {
            log.error("生成结构化响应失败: sessionId={}", sessionId, e);
            return createErrorResponse("生成结构化响应失败: " + e.getMessage(), userMessage);
        }
    }

    @Override
    public StructuredChatResponse generateKnowledgeBasedResponse(String sessionId, String userMessage, 
                                                               Long knowledgeBaseId) {
        try {
            log.debug("生成知识库结构化响应: sessionId={}, knowledgeBaseId={}, messageLength={}", 
                    sessionId, knowledgeBaseId, userMessage.length());
            
            return structuredChatServiceAi.generateKnowledgeBasedResponse(sessionId, userMessage, knowledgeBaseId);
            
        } catch (Exception e) {
            log.error("生成知识库结构化响应失败: sessionId={}, knowledgeBaseId={}", sessionId, knowledgeBaseId, e);
            return createErrorResponse("生成知识库结构化响应失败: " + e.getMessage(), userMessage);
        }
    }

    @Override
    public StructuredChatResponse analyzeIntentAndRespond(String userMessage, String context) {
        try {
            log.debug("分析用户意图: messageLength={}, contextLength={}", userMessage.length(), context.length());
            
            return structuredChatServiceAi.analyzeIntentAndRespond(userMessage, context);
            
        } catch (Exception e) {
            log.error("分析用户意图失败", e);
            return createErrorResponse("分析用户意图失败: " + e.getMessage(), userMessage);
        }
    }

    @Override
    public StructuredChatResponse generateErrorResponse(String errorMessage, String userMessage) {
        try {
            log.debug("生成错误处理响应: error={}, messageLength={}", errorMessage, userMessage.length());
            
            return structuredChatServiceAi.generateErrorResponse(errorMessage, userMessage);
            
        } catch (Exception e) {
            log.error("生成错误处理响应失败", e);
            return createErrorResponse("生成错误处理响应失败: " + e.getMessage(), userMessage);
        }
    }

    @Override
    public boolean isAvailable() {
        try {
            // 简单的可用性检查
            return structuredChatServiceAi != null;
        } catch (Exception e) {
            log.warn("检查结构化聊天服务可用性失败", e);
            return false;
        }
    }

    /**
     * 获取服务状态信息
     * 
     * @return 状态描述
     */
    public String getStatus() {
        if (isAvailable()) {
            return "AVAILABLE";
        } else {
            return "UNAVAILABLE";
        }
    }

    @Override
    public boolean validateResponse(StructuredChatResponse response) {
        if (response == null) {
            return false;
        }
        
        // 基本验证逻辑
        return response.getContent() != null && !response.getContent().trim().isEmpty() &&
               response.getType() != null;
    }

    /**
     * 创建错误响应
     */
    private StructuredChatResponse createErrorResponse(String errorMessage, String userMessage) {
        return StructuredChatResponse.builder()
                .content("抱歉，处理您的请求时出现了错误。请稍后重试。")
                .type(StructuredChatResponse.ResponseType.ERROR)
                .confidence(0.0)
                .needsClarification(false)
                .metadata(Map.of("error", errorMessage, "originalMessage", userMessage))
                .build();
    }
} 