package com.leyue.smartcs.app.ai;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * AI应用聊天助手实现类
 */
@RequiredArgsConstructor
@Slf4j
public class AppChatAssistantImpl implements AppChatAssistant {

    private final ChatModel chatModel;
    private final StreamingChatModel streamingChatModel;
    private final ChatMemoryStore chatMemoryStore;
    
    // 会话记忆缓存
    private final Map<String, ChatMemory> memoryCache = new ConcurrentHashMap<>();

    @Override
    public String chat(String sessionId, String systemPrompt, String userMessage, Map<String, Object> variables) {
        try {
            // 构建消息列表
            List<ChatMessage> messages = buildMessages(sessionId, systemPrompt, userMessage, variables);
            
            // 构建ChatRequest
            ChatRequest chatRequest = ChatRequest.builder()
                    .messages(messages)
                    .build();
            
            // 执行聊天
            ChatResponse response = chatModel.chat(chatRequest);
            String aiResponse = response.aiMessage().text();
            
            // 保存AI响应到记忆
            ChatMemory memory = getOrCreateMemory(sessionId);
            memory.add(AiMessage.from(aiResponse));
            
            return aiResponse;
            
        } catch (Exception e) {
            log.error("同步聊天失败: sessionId={}, error={}", sessionId, e.getMessage(), e);
            throw new RuntimeException("聊天失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void chatStream(String sessionId, String systemPrompt, String userMessage, Map<String, Object> variables,
                          Consumer<String> onNext, Consumer<Response<AiMessage>> onComplete, Consumer<Throwable> onError) {
        try {
            if (streamingChatModel == null) {
                // 如果没有流式模型，降级到同步调用
                try {
                    String response = chat(sessionId, systemPrompt, userMessage, variables);
                    // 模拟流式返回
                    onNext.accept(response);
                    onComplete.accept(Response.from(AiMessage.from(response)));
                } catch (Exception e) {
                    onError.accept(e);
                }
                return;
            }
            
            // 构建消息列表
            List<ChatMessage> messages = buildMessages(sessionId, systemPrompt, userMessage, variables);
            
            // 构建ChatRequest
            ChatRequest chatRequest = ChatRequest.builder()
                    .messages(messages)
                    .build();
            
            // 执行流式聊天
            streamingChatModel.chat(chatRequest, new StreamingChatResponseHandler() {
                private final StringBuilder fullResponse = new StringBuilder();
                
                @Override
                public void onPartialResponse(String token) {
                    fullResponse.append(token);
                    onNext.accept(token);
                }
                
                @Override
                public void onCompleteResponse(ChatResponse response) {
                    try {
                        // 保存AI响应到记忆
                        ChatMemory memory = getOrCreateMemory(sessionId);
                        memory.add(AiMessage.from(fullResponse.toString()));
                        
                        // 转换为Response<AiMessage>格式
                        Response<AiMessage> aiResponse = Response.from(AiMessage.from(fullResponse.toString()));
                        onComplete.accept(aiResponse);
                    } catch (Exception e) {
                        log.error("保存AI响应到记忆失败: sessionId={}, error={}", sessionId, e.getMessage(), e);
                        Response<AiMessage> aiResponse = Response.from(AiMessage.from(fullResponse.toString()));
                        onComplete.accept(aiResponse);
                    }
                }
                
                @Override
                public void onError(Throwable error) {
                    onError.accept(error);
                }
            });
            
        } catch (Exception e) {
            log.error("流式聊天失败: sessionId={}, error={}", sessionId, e.getMessage(), e);
            onError.accept(e);
        }
    }

    /**
     * 构建消息列表
     */
    private List<ChatMessage> buildMessages(String sessionId, String systemPrompt, String userMessage, Map<String, Object> variables) {
        // 获取记忆
        ChatMemory memory = getOrCreateMemory(sessionId);
        
        // 添加系统消息（如果提供）
        if (systemPrompt != null && !systemPrompt.trim().isEmpty()) {
            String processedSystemPrompt = processTemplate(systemPrompt, variables);
            memory.add(SystemMessage.from(processedSystemPrompt));
        }
        
        // 添加用户消息
        String processedUserMessage = processTemplate(userMessage, variables);
        memory.add(UserMessage.from(processedUserMessage));
        
        return memory.messages();
    }

    /**
     * 获取或创建记忆
     */
    private ChatMemory getOrCreateMemory(String sessionId) {
        return memoryCache.computeIfAbsent(sessionId, id -> {
            return MessageWindowChatMemory.builder()
                    .id(id)
                    .maxMessages(20)  // 保存最近20条消息
                    .chatMemoryStore(chatMemoryStore)
                    .build();
        });
    }

    /**
     * 处理模板变量
     */
    private String processTemplate(String template, Map<String, Object> variables) {
        if (template == null || variables == null || variables.isEmpty()) {
            return template;
        }
        
        String processed = template;
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            String value = entry.getValue() != null ? entry.getValue().toString() : "";
            processed = processed.replace(placeholder, value);
        }
        
        return processed;
    }
}