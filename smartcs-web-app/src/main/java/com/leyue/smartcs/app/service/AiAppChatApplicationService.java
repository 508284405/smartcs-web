package com.leyue.smartcs.app.service;

import com.leyue.smartcs.domain.app.model.ChatRequest;
import com.leyue.smartcs.domain.app.model.ChatResponse;
import com.leyue.smartcs.domain.app.model.StreamingHandler;
import com.leyue.smartcs.domain.app.service.AiAppChatService;
import com.leyue.smartcs.domain.model.Model;
import com.leyue.smartcs.domain.model.Provider;
import com.leyue.smartcs.dto.app.AiAppChatCmd;
import com.leyue.smartcs.dto.app.AiAppChatResponse;
import com.leyue.smartcs.dto.app.AiAppChatSSEMessage;
import com.leyue.smartcs.dto.app.AiAppDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * AI应用聊天应用服务
 * 协调Domain层聊天服务和基础设施，处理SSE流式响应
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AiAppChatApplicationService {

    private final AiAppChatService domainChatService;

    /**
     * 执行带记忆的流式聊天
     */
    public void executeMemoryChat(SseEmitter emitter, String sessionId, String systemPrompt, 
                                 String userMessage, Map<String, Object> variables, 
                                 long startTime, Long appId, Model model, 
                                 MessageManager messageManager) throws IOException {
        
        String messageId = UUID.randomUUID().toString().replace("-", "");
        
        // 构建Domain层的聊天请求
        ChatRequest request = ChatRequest.builder()
                .sessionId(sessionId)
                .systemPrompt(systemPrompt)
                .userMessage(userMessage)
                .variables(enrichVariables(variables, model, appId))
                .includeMemory(true)
                .streamEnabled(true)
                .build();
        
        // 创建流式处理器，将Domain层响应转换为SSE消息
        StreamingHandler handler = StreamingHandler.simple(
            token -> {
                try {
                    AiAppChatResponse response = AiAppChatResponse.builder()
                            .sessionId(sessionId)
                            .messageId(messageId)
                            .content(token)
                            .finished(false)
                            .timestamp(System.currentTimeMillis())
                            .build();
                    
                    sendSSEMessage(emitter, AiAppChatSSEMessage.data(sessionId, response));
                } catch (IOException e) {
                    log.error("发送流式数据失败", e);
                }
            },
            fullResponse -> {
                try {
                    long processTime = System.currentTimeMillis() - startTime;
                    
                    messageManager.saveAssistantMessage(messageId, sessionId, appId, fullResponse, 
                                                      model, (int) processTime);
                    messageManager.updateSessionStats(sessionId);
                    
                    AiAppChatResponse finalResponse = AiAppChatResponse.builder()
                            .sessionId(sessionId)
                            .messageId(messageId)
                            .content(fullResponse)
                            .finished(true)
                            .timestamp(System.currentTimeMillis())
                            .processTime(processTime)
                            .build();
                    
                    sendSSEMessage(emitter, AiAppChatSSEMessage.complete(sessionId, finalResponse));
                    emitter.complete();
                    
                    log.info("AI应用聊天成功完成: sessionId={}, processTime={}ms, responseLength={}", 
                            sessionId, processTime, fullResponse.length());
                    
                } catch (IOException e) {
                    log.error("发送完成消息失败", e);
                    emitter.completeWithError(e);
                }
            },
            error -> {
                log.error("流式聊天出错: sessionId={}, error={}", sessionId, error.getMessage(), error);
                try {
                    sendSSEMessage(emitter, AiAppChatSSEMessage.error(sessionId, "AI响应出错: " + error.getMessage()));
                } catch (IOException e) {
                    log.error("发送错误消息失败", e);
                }
                emitter.completeWithError(error);
            }
        );
        
        // 使用Domain层服务执行流式聊天
        try {
            domainChatService.chatStream(request, handler);
        } catch (Exception e) {
            log.error("启动流式聊天失败: sessionId={}, error={}", sessionId, e.getMessage(), e);
            handler.onError(e);
        }
    }

    /**
     * 执行带RAG和记忆的流式聊天
     */
    public void executeRagMemoryChat(SseEmitter emitter, String sessionId, String systemPrompt, 
                                   String userMessage, Map<String, Object> variables, 
                                   Long knowledgeId, long startTime, Long appId, Model model,
                                   MessageManager messageManager) throws IOException {
        
        String messageId = UUID.randomUUID().toString().replace("-", "");
        
        try {
            log.info("开始RAG记忆流式聊天: sessionId={}, knowledgeId={}", sessionId, knowledgeId);
            
            // 构建Domain层的RAG聊天请求
            ChatRequest request = ChatRequest.builder()
                    .sessionId(sessionId)
                    .systemPrompt(systemPrompt)
                    .userMessage(userMessage)
                    .variables(enrichVariables(variables, model, appId))
                    .knowledgeBaseId(knowledgeId)
                    .includeMemory(true)
                    .streamEnabled(true)
                    .build();
            
            // 创建流式处理器，将Domain层响应转换为SSE消息
            StreamingHandler handler = StreamingHandler.simple(
                token -> {
                    try {
                        AiAppChatResponse response = AiAppChatResponse.builder()
                                .sessionId(sessionId)
                                .messageId(messageId)
                                .content(token)
                                .finished(false)
                                .timestamp(System.currentTimeMillis())
                                .build();
                        
                        sendSSEMessage(emitter, AiAppChatSSEMessage.data(sessionId, response));
                    } catch (IOException e) {
                        log.error("发送RAG流式数据失败", e);
                    }
                },
                fullResponse -> {
                    try {
                        long processTime = System.currentTimeMillis() - startTime;
                        
                        messageManager.saveAssistantMessage(messageId, sessionId, appId, fullResponse, 
                                                          model, (int) processTime);
                        messageManager.updateSessionStats(sessionId);
                        
                        AiAppChatResponse finalResponse = AiAppChatResponse.builder()
                                .sessionId(sessionId)
                                .messageId(messageId)
                                .content(fullResponse)
                                .finished(true)
                                .timestamp(System.currentTimeMillis())
                                .processTime(processTime)
                                .build();
                        
                        sendSSEMessage(emitter, AiAppChatSSEMessage.complete(sessionId, finalResponse));
                        emitter.complete();
                        
                        log.info("RAG记忆流式聊天成功完成: sessionId={}, processTime={}ms, responseLength={}", 
                                sessionId, processTime, fullResponse.length());
                        
                    } catch (IOException e) {
                        log.error("发送RAG完成消息失败", e);
                        emitter.completeWithError(e);
                    }
                },
                error -> {
                    log.error("RAG记忆流式聊天出错: sessionId={}, error={}", sessionId, error.getMessage(), error);
                    try {
                        sendSSEMessage(emitter, AiAppChatSSEMessage.error(sessionId, "RAG AI响应出错: " + error.getMessage()));
                    } catch (IOException e) {
                        log.error("发送RAG错误消息失败", e);
                    }
                    emitter.completeWithError(error);
                }
            );
            
            // 使用Domain层服务执行RAG流式聊天
            domainChatService.chatWithRagStream(request, handler);
                
        } catch (Exception e) {
            log.error("启动RAG记忆流式聊天失败: sessionId={}, error={}", sessionId, e.getMessage(), e);
            sendSSEMessage(emitter, AiAppChatSSEMessage.error(sessionId, "启动RAG聊天失败: " + e.getMessage()));
            emitter.completeWithError(e);
        }
    }

    /**
     * 丰富变量信息，添加模型和应用信息
     */
    private Map<String, Object> enrichVariables(Map<String, Object> variables, Model model, Long appId) {
        Map<String, Object> enrichedVariables = new HashMap<>();
        if (variables != null) {
            enrichedVariables.putAll(variables);
        }
        
        // 添加模型信息
        enrichedVariables.put("modelId", model.getId());
        enrichedVariables.put("appId", appId);
        
        return enrichedVariables;
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