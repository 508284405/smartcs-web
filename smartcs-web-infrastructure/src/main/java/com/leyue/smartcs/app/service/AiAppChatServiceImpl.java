package com.leyue.smartcs.app.service;

import com.leyue.smartcs.app.ai.AppChatAssistant;
import com.leyue.smartcs.app.ai.AppChatAssistantFactory;
import com.leyue.smartcs.app.ai.EnhancedAppChatAssistant;
import com.leyue.smartcs.config.ModelBeanManagerService;
import com.leyue.smartcs.domain.app.model.ChatRequest;
import com.leyue.smartcs.domain.app.model.ChatResponse;
import com.leyue.smartcs.domain.app.model.StreamingHandler;
import com.leyue.smartcs.domain.app.service.AiAppChatService;
import com.leyue.smartcs.domain.model.Model;
import com.leyue.smartcs.domain.model.Provider;
import com.leyue.smartcs.domain.model.gateway.ModelGateway;
import com.leyue.smartcs.domain.model.gateway.ProviderGateway;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * AI应用聊天服务的LangChain4j技术实现
 * 实现Domain层定义的业务接口，封装LangChain4j技术细节
 */
@Service("domainAiAppChatService")
@RequiredArgsConstructor
@Slf4j
public class AiAppChatServiceImpl implements AiAppChatService {

    private final AppChatAssistantFactory assistantFactory;
    private final ModelBeanManagerService modelBeanManagerService;
    private final ApplicationContext applicationContext;
    private final ModelGateway modelGateway;
    private final ProviderGateway providerGateway;

    @Override
    public ChatResponse chat(ChatRequest request) {
        try {
            validateRequest(request);
            
            AppChatAssistant assistant = createAssistant(request);
            String messageId = generateMessageId();
            long startTime = System.currentTimeMillis();
            
            String response;
            if (request.isRagEnabled()) {
                response = executeChatWithRag(assistant, request);
            } else {
                response = executeChat(assistant, request);
            }
            
            long processTime = System.currentTimeMillis() - startTime;
            
            return ChatResponse.builder()
                    .sessionId(request.getSessionId())
                    .messageId(messageId)
                    .content(response)
                    .finished(true)
                    .timestamp(System.currentTimeMillis())
                    .processTime(processTime)
                    .responseType(request.isRagEnabled() ? 
                        ChatResponse.ResponseType.KNOWLEDGE_BASED : 
                        ChatResponse.ResponseType.ANSWER)
                    .build();
                    
        } catch (Exception e) {
            log.error("同步聊天失败: sessionId={}, error={}", request.getSessionId(), e.getMessage(), e);
            return ChatResponse.error(request.getSessionId(), "聊天处理失败: " + e.getMessage());
        }
    }

    @Override
    public void chatStream(ChatRequest request, StreamingHandler handler) {
        try {
            validateRequest(request);
            validateHandler(handler);
            
            AppChatAssistant assistant = createAssistant(request);
            StringBuilder fullResponse = new StringBuilder();
            AtomicBoolean hasError = new AtomicBoolean(false);
            
            if (request.isRagEnabled()) {
                executeChatWithRagStream(assistant, request, handler, fullResponse, hasError);
            } else {
                executeChatStream(assistant, request, handler, fullResponse, hasError);
            }
            
        } catch (Exception e) {
            log.error("流式聊天失败: sessionId={}, error={}", request.getSessionId(), e.getMessage(), e);
            handler.onError(e);
        }
    }

    @Override
    public ChatResponse chatWithRag(ChatRequest request) {
        if (!request.isRagEnabled()) {
            throw new IllegalArgumentException("RAG聊天请求必须包含知识库ID");
        }
        return chat(request);
    }

    @Override
    public void chatWithRagStream(ChatRequest request, StreamingHandler handler) {
        if (!request.isRagEnabled()) {
            throw new IllegalArgumentException("RAG流式聊天请求必须包含知识库ID");
        }
        chatStream(request, handler);
    }

    @Override
    public boolean isAvailable() {
        try {
            return assistantFactory != null && modelBeanManagerService != null;
        } catch (Exception e) {
            log.warn("检查服务可用性失败", e);
            return false;
        }
    }

    @Override
    public String getStatus() {
        if (isAvailable()) {
            return "AI聊天服务运行正常";
        } else {
            return "AI聊天服务不可用";
        }
    }

    /**
     * 创建AI助手
     */
    private AppChatAssistant createAssistant(ChatRequest request) {
        try {
            // 这里需要从请求中获取模型信息，实际实现时需要补充
            // 为了演示，使用默认值
            Long modelId = extractModelId(request);
            Model model = modelGateway.findById(modelId)
                .orElseThrow(() -> new RuntimeException("模型不存在: " + modelId));
            
            Provider provider = providerGateway.findById(model.getProviderId())
                .orElseThrow(() -> new RuntimeException("提供商不存在: " + model.getProviderId()));
            
            // 获取聊天模型
            String chatModelBeanName = modelBeanManagerService.createModelBean(provider, "chat");
            ChatModel chatModel = (ChatModel) applicationContext.getBean(chatModelBeanName);
            
            // 获取流式聊天模型
            StreamingChatModel streamingModel = null;
            try {
                String streamingModelBeanName = modelBeanManagerService.createModelBean(provider, "streaming");
                streamingModel = (StreamingChatModel) applicationContext.getBean(streamingModelBeanName);
            } catch (Exception e) {
                log.warn("未找到流式模型，将使用同步模式: {}", e.getMessage());
            }
            
            String modelKey = provider.getId() + "_" + model.getId();
            return assistantFactory.getOrCreateAssistant(chatModel, streamingModel, modelKey);
            
        } catch (Exception e) {
            throw new RuntimeException("创建AI助手失败: " + e.getMessage(), e);
        }
    }

    /**
     * 执行普通聊天
     */
    private String executeChat(AppChatAssistant assistant, ChatRequest request) {
        String sessionId = request.hasMemory() ? request.getSessionId() : null;
        Map<String, Object> variables = buildTemplateVariables(request.getVariables());
        
        return assistant.chat(sessionId, request.getSystemPrompt(), request.getUserMessage(), variables);
    }

    /**
     * 执行RAG聊天
     */
    private String executeChatWithRag(AppChatAssistant assistant, ChatRequest request) {
        if (!(assistant instanceof EnhancedAppChatAssistant enhancedAssistant)) {
            log.warn("助手不支持RAG，降级到普通聊天");
            return executeChat(assistant, request);
        }
        
        String sessionId = request.hasMemory() ? request.getSessionId() : null;
        Map<String, Object> variables = buildTemplateVariables(request.getVariables());
        
        return enhancedAssistant.chatWithRag(sessionId, request.getSystemPrompt(), 
            request.getUserMessage(), variables, request.getKnowledgeBaseId());
    }

    /**
     * 执行流式聊天
     */
    private void executeChatStream(AppChatAssistant assistant, ChatRequest request, 
                                 StreamingHandler handler, StringBuilder fullResponse, AtomicBoolean hasError) {
        String sessionId = request.hasMemory() ? request.getSessionId() : null;
        Map<String, Object> variables = buildTemplateVariables(request.getVariables());
        
        assistant.chatStream(sessionId, request.getSystemPrompt(), request.getUserMessage(), variables,
            token -> {
                if (!hasError.get()) {
                    fullResponse.append(token);
                    handler.onToken(token);
                }
            },
            response -> {
                if (!hasError.get()) {
                    handler.onComplete(fullResponse.toString());
                }
            },
            error -> {
                hasError.set(true);
                handler.onError(error);
            });
    }

    /**
     * 执行RAG流式聊天
     */
    private void executeChatWithRagStream(AppChatAssistant assistant, ChatRequest request, 
                                        StreamingHandler handler, StringBuilder fullResponse, AtomicBoolean hasError) {
        if (!(assistant instanceof EnhancedAppChatAssistant enhancedAssistant)) {
            log.warn("助手不支持RAG，降级到普通流式聊天");
            executeChatStream(assistant, request, handler, fullResponse, hasError);
            return;
        }
        
        String sessionId = request.hasMemory() ? request.getSessionId() : null;
        Map<String, Object> variables = buildTemplateVariables(request.getVariables());
        
        enhancedAssistant.chatWithRagStream(sessionId, request.getSystemPrompt(), 
            request.getUserMessage(), variables, request.getKnowledgeBaseId(),
            token -> {
                if (!hasError.get()) {
                    fullResponse.append(token);
                    handler.onToken(token);
                }
            },
            response -> {
                if (!hasError.get()) {
                    handler.onComplete(fullResponse.toString());
                }
            },
            error -> {
                hasError.set(true);
                handler.onError(error);
            });
    }

    /**
     * 构建模板变量
     */
    private Map<String, Object> buildTemplateVariables(Map<String, Object> variables) {
        Map<String, Object> templateVariables = new HashMap<>();
        if (variables != null) {
            templateVariables.putAll(variables);
        }
        templateVariables.put("context", buildContextFromVariables(variables));
        return templateVariables;
    }

    /**
     * 从变量中构建上下文
     */
    private String buildContextFromVariables(Map<String, Object> variables) {
        if (variables == null || variables.isEmpty()) {
            return "";
        }
        
        StringBuilder context = new StringBuilder();
        variables.forEach((key, value) -> {
            if (!"target".equals(key)) {
                context.append(key).append(": ").append(value).append("\n");
            }
        });
        
        return context.toString().trim();
    }

    /**
     * 验证请求
     */
    private void validateRequest(ChatRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("聊天请求不能为空");
        }
        if (request.getUserMessage() == null || request.getUserMessage().trim().isEmpty()) {
            throw new IllegalArgumentException("用户消息不能为空");
        }
    }

    /**
     * 验证处理器
     */
    private void validateHandler(StreamingHandler handler) {
        if (handler == null) {
            throw new IllegalArgumentException("流式处理器不能为空");
        }
    }

    /**
     * 生成消息ID
     */
    private String generateMessageId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 从请求中提取模型ID
     * 实际实现时需要根据具体业务逻辑调整
     */
    private Long extractModelId(ChatRequest request) {
        // 这里需要根据实际的业务逻辑来获取模型ID
        // 可能从变量中获取，或者从其他途径
        if (request.getVariables() != null && request.getVariables().containsKey("modelId")) {
            return (Long) request.getVariables().get("modelId");
        }
        // 返回默认模型ID，实际应用中需要有更好的策略
        return 1L;
    }
}