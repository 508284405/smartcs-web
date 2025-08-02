package com.leyue.smartcs.app.ai;

import com.leyue.smartcs.app.rag.RagOrchestrator;
import com.leyue.smartcs.app.service.AiAppChatServiceFactory;
import com.leyue.smartcs.domain.app.model.ChatRequest;
import com.leyue.smartcs.domain.app.model.ChatResponse;
import com.leyue.smartcs.domain.app.model.StreamingHandler;
import com.leyue.smartcs.domain.app.service.AiAppChatService;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.service.TokenStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Consumer;

/**
 * 增强版AI应用聊天助手
 * 基于LangChain4j AI Services和RAG系统重构
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EnhancedAppChatAssistant implements AppChatAssistant {

    private final AiAppChatServiceFactory chatServiceFactory;
    private final RagOrchestrator ragOrchestrator;

    private ChatModel chatModel;
    private StreamingChatModel streamingChatModel;
    private String modelKey;

    /**
     * 初始化助手
     * 
     * @param chatModel 聊天模型
     * @param streamingChatModel 流式聊天模型
     * @param modelKey 模型唯一标识
     */
    public void initialize(ChatModel chatModel, 
                          StreamingChatModel streamingChatModel, 
                          String modelKey) {
        this.chatModel = chatModel;
        this.streamingChatModel = streamingChatModel;
        this.modelKey = modelKey;
        
        log.info("初始化增强版聊天助手: modelKey={}", modelKey);
    }

    @Override
    public String chat(String sessionId, String systemPrompt, String userMessage, Map<String, Object> variables) {
        try {
            log.debug("开始同步聊天: sessionId={}, messageLength={}", sessionId, userMessage.length());
            
            // 获取简单聊天服务（不带RAG）
            AiAppChatService chatService = chatServiceFactory.getOrCreateService(
                chatModel, streamingChatModel, modelKey, false);
            
            // 构建ChatRequest对象
            ChatRequest request = ChatRequest.builder()
                    .sessionId(sessionId)
                    .systemPrompt(systemPrompt)
                    .userMessage(userMessage)
                    .variables(variables)
                    .includeMemory(true)
                    .streamEnabled(false)
                    .build();
            
            ChatResponse response = chatService.chat(request);
            String content = response.getContent();
            
            log.debug("同步聊天完成: sessionId={}, responseLength={}", sessionId, content.length());
            return content;
            
        } catch (Exception e) {
            log.error("同步聊天失败: sessionId={}, error={}", sessionId, e.getMessage(), e);
            throw new RuntimeException("聊天失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void chatStream(String sessionId, String systemPrompt, String userMessage, Map<String, Object> variables,
                          Consumer<String> onNext, Consumer<Response<AiMessage>> onComplete, Consumer<Throwable> onError) {
        try {
            log.debug("开始流式聊天: sessionId={}, messageLength={}", sessionId, userMessage.length());
            
            // 获取简单聊天服务（不带RAG）
            AiAppChatService chatService = chatServiceFactory.getOrCreateService(
                chatModel, streamingChatModel, modelKey, false);
            
            // 构建ChatRequest对象
            ChatRequest request = ChatRequest.builder()
                    .sessionId(sessionId)
                    .systemPrompt(systemPrompt)
                    .userMessage(userMessage)
                    .variables(variables)
                    .includeMemory(true)
                    .streamEnabled(true)
                    .build();
            
            // 创建StreamingHandler
            StreamingHandler handler = StreamingHandler.simple(
                onNext::accept,
                fullResponse -> {
                    log.debug("流式聊天完成: sessionId={}, responseLength={}", sessionId, fullResponse.length());
                    onComplete.accept(Response.from(AiMessage.from(fullResponse)));
                },
                onError::accept
            );
            
            chatService.chatStream(request, handler);
                
        } catch (Exception e) {
            log.error("启动流式聊天失败: sessionId={}, error={}", sessionId, e.getMessage(), e);
            onError.accept(e);
        }
    }

    /**
     * 带RAG的同步聊天
     * 
     * @param sessionId 会话ID
     * @param systemPrompt 系统提示词
     * @param userMessage 用户消息
     * @param variables 模板变量
     * @param knowledgeBaseId 知识库ID
     * @return AI响应
     */
    public String chatWithRag(String sessionId, String systemPrompt, String userMessage, 
                             Map<String, Object> variables, Long knowledgeBaseId) {
        try {
            log.debug("开始RAG同步聊天: sessionId={}, knowledgeBaseId={}, messageLength={}", 
                     sessionId, knowledgeBaseId, userMessage.length());
            
            // 获取RAG聊天服务
            AiAppChatService chatService = chatServiceFactory.getOrCreateService(
                chatModel, streamingChatModel, modelKey, true);
            
            // 构建ChatRequest对象
            ChatRequest request = ChatRequest.builder()
                    .sessionId(sessionId)
                    .systemPrompt(systemPrompt)
                    .userMessage(userMessage)
                    .variables(variables)
                    .knowledgeBaseId(knowledgeBaseId)
                    .includeMemory(true)
                    .streamEnabled(false)
                    .build();
            
            ChatResponse response = chatService.chatWithRag(request);
            String content = response.getContent();
            
            log.debug("RAG同步聊天完成: sessionId={}, responseLength={}", sessionId, content.length());
            return content;
            
        } catch (Exception e) {
            log.error("RAG同步聊天失败: sessionId={}, error={}", sessionId, e.getMessage(), e);
            throw new RuntimeException("RAG聊天失败: " + e.getMessage(), e);
        }
    }

    /**
     * 带RAG的流式聊天
     * 
     * @param sessionId 会话ID
     * @param systemPrompt 系统提示词
     * @param userMessage 用户消息
     * @param variables 模板变量
     * @param knowledgeBaseId 知识库ID
     * @param onNext 接收流式token的回调
     * @param onComplete 完成时的回调
     * @param onError 出错时的回调
     */
    public void chatWithRagStream(String sessionId, String systemPrompt, String userMessage, 
                                 Map<String, Object> variables, Long knowledgeBaseId,
                                 Consumer<String> onNext, Consumer<Response<AiMessage>> onComplete, 
                                 Consumer<Throwable> onError) {
        try {
            log.debug("开始RAG流式聊天: sessionId={}, knowledgeBaseId={}, messageLength={}", 
                     sessionId, knowledgeBaseId, userMessage.length());
            
            // 获取RAG聊天服务
            AiAppChatService chatService = chatServiceFactory.getOrCreateService(
                chatModel, streamingChatModel, modelKey, true);
            
            // 构建ChatRequest对象
            ChatRequest request = ChatRequest.builder()
                    .sessionId(sessionId)
                    .systemPrompt(systemPrompt)
                    .userMessage(userMessage)
                    .variables(variables)
                    .knowledgeBaseId(knowledgeBaseId)
                    .includeMemory(true)
                    .streamEnabled(true)
                    .build();
            
            // 创建StreamingHandler
            StreamingHandler handler = StreamingHandler.simple(
                onNext::accept,
                fullResponse -> {
                    log.debug("RAG流式聊天完成: sessionId={}, responseLength={}", sessionId, fullResponse.length());
                    onComplete.accept(Response.from(AiMessage.from(fullResponse)));
                },
                onError::accept
            );
            
            chatService.chatWithRagStream(request, handler);
                
        } catch (Exception e) {
            log.error("启动RAG流式聊天失败: sessionId={}, error={}", sessionId, e.getMessage(), e);
            onError.accept(e);
        }
    }



    /**
     * 获取模型标识
     */
    public String getModelKey() {
        return modelKey;
    }

    /**
     * 检查是否已初始化
     */
    public boolean isInitialized() {
        return chatModel != null && modelKey != null;
    }
}