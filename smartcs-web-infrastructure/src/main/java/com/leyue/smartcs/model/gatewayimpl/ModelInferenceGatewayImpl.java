package com.leyue.smartcs.model.gatewayimpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import dev.langchain4j.data.segment.TextSegment;
import org.springframework.stereotype.Component;

import com.alibaba.cola.exception.BizException;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.leyue.smartcs.config.ModelBeanManagerService;
import com.leyue.smartcs.domain.knowledge.gateway.ChunkGateway;
import com.leyue.smartcs.domain.model.Model;
import com.leyue.smartcs.domain.model.ModelContext;
import com.leyue.smartcs.domain.model.Provider;
import com.leyue.smartcs.domain.model.gateway.ModelContextGateway;
import com.leyue.smartcs.domain.model.gateway.ModelGateway;
import com.leyue.smartcs.domain.model.gateway.ModelInferenceGateway;
import com.leyue.smartcs.domain.model.gateway.ProviderGateway;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 模型推理网关实现
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ModelInferenceGatewayImpl implements ModelInferenceGateway {

    private final ModelGateway modelGateway;
    private final ProviderGateway providerGateway;
    private final ModelContextGateway modelContextGateway;
    private final ModelBeanManagerService modelBeanManagerService;
    private final EmbeddingStore<TextSegment> embeddingStore;

    @Override
    public String infer(Long modelId, String message, String sessionId, String systemPrompt,
                       Boolean enableRAG, Long knowledgeId, String inferenceParams, Boolean saveToContext) {
        try {
            log.info("开始同步推理: modelId={}, sessionId={}, enableRAG={}", modelId, sessionId, enableRAG);

            // 获取模型和提供商信息
            Model model = getModel(modelId);
            Provider provider = getProvider(model.getProviderId());

            // 构建完整的Prompt
            String fullPrompt = buildPrompt(message, systemPrompt, enableRAG, knowledgeId);

            // 解析推理参数
            Map<String, Object> params = parseInferenceParams(inferenceParams);

            // 获取ChatModel实例
            ChatModel chatModel = getChatModel(provider);

            // 构建消息列表
            List<ChatMessage> messages = buildMessages(fullPrompt, sessionId);

            // 执行推理
            ChatResponse response = chatModel.chat(messages);
            String result = response.aiMessage().text();

            // 保存到上下文
            if (saveToContext != null && saveToContext) {
                saveToContext(sessionId, modelId, message, result);
            }

            log.info("同步推理完成: modelId={}, sessionId={}, resultLength={}", 
                    modelId, sessionId, result.length());

            return result;

        } catch (Exception e) {
            log.error("同步推理失败: modelId={}, sessionId={}, error={}", 
                    modelId, sessionId, e.getMessage(), e);
            throw new BizException("推理失败: " + e.getMessage());
        }
    }

    @Override
    public void inferStream(Long modelId, String message, String sessionId, String systemPrompt,
                           Boolean enableRAG, Long knowledgeId, String inferenceParams, Boolean saveToContext,
                           Consumer<String> chunkConsumer) {
        try {
            log.info("开始流式推理: modelId={}, sessionId={}, enableRAG={}", modelId, sessionId, enableRAG);

            // 获取模型和提供商信息
            Model model = getModel(modelId);
            Provider provider = getProvider(model.getProviderId());

            // 构建完整的Prompt
            String fullPrompt = buildPrompt(message, systemPrompt, enableRAG, knowledgeId);

            // 解析推理参数
            Map<String, Object> params = parseInferenceParams(inferenceParams);

            // 获取ChatModel实例
            ChatModel chatModel = getChatModel(provider);

            // 构建消息列表
            List<ChatMessage> messages = buildMessages(fullPrompt, sessionId);

            // 执行流式推理（简化实现）
            StringBuilder fullResult = new StringBuilder();
            try {
                // 使用同步推理模拟流式效果
                String result = chatModel.chat(messages).aiMessage().text();
                
                // 模拟流式输出
                String[] tokens = result.split("(?<=\\G.{10})"); // 每10个字符分割
                for (String token : tokens) {
                    fullResult.append(token);
                    chunkConsumer.accept(token);
                    Thread.sleep(50); // 模拟延迟
                }
                
                // 保存到上下文
                if (saveToContext != null && saveToContext) {
                    saveToContext(sessionId, modelId, message, fullResult.toString());
                }
                log.info("流式推理完成: modelId={}, sessionId={}, resultLength={}", 
                        modelId, sessionId, fullResult.length());
                        
            } catch (Exception e) {
                log.error("流式推理错误: modelId={}, sessionId={}, error={}", 
                        modelId, sessionId, e.getMessage(), e);
                throw new BizException("流式推理失败: " + e.getMessage());
            }

        } catch (Exception e) {
            log.error("流式推理失败: modelId={}, sessionId={}, error={}", 
                    modelId, sessionId, e.getMessage(), e);
            throw new BizException("流式推理失败: " + e.getMessage());
        }
    }

    @Override
    public boolean supportsInference(Long modelId) {
        try {
            Model model = getModel(modelId);
            Provider provider = getProvider(model.getProviderId());
            
            // 检查模型类型是否支持推理
            return model.getModelType() != null && 
                   model.getModelType().stream().anyMatch(type -> "llm".equals(type.getCode()));
        } catch (Exception e) {
            log.warn("检查推理支持失败: modelId={}, error={}", modelId, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean supportsStreaming(Long modelId) {
        try {
            Model model = getModel(modelId);
            Provider provider = getProvider(model.getProviderId());
            
            // 检查模型是否支持流式推理（基于模型类型）
            return model.getModelType() != null && 
                   model.getModelType().stream().anyMatch(type -> "llm".equals(type.getCode()));
        } catch (Exception e) {
            log.warn("检查流式推理支持失败: modelId={}, error={}", modelId, e.getMessage());
            return false;
        }
    }

    @Override
    public String getInferenceConfig(Long modelId) {
        try {
            Model model = getModel(modelId);
            
            // 构建推理配置
            Map<String, Object> config = new HashMap<>();
            config.put("modelId", modelId);
            config.put("modelLabel", model.getLabel());
            config.put("modelType", model.getModelType());
            config.put("supportsStreaming", supportsStreaming(modelId));
            config.put("supportsInference", supportsInference(modelId));
            config.put("modelProperties", model.getModelProperties());
            
            return JSON.toJSONString(config);
        } catch (Exception e) {
            log.error("获取推理配置失败: modelId={}, error={}", modelId, e.getMessage());
            return "{}";
        }
    }

    /**
     * 获取模型信息
     */
    private Model getModel(Long modelId) {
        return modelGateway.findById(modelId)
                .orElseThrow(() -> new BizException("模型不存在: " + modelId));
    }

    /**
     * 获取提供商信息
     */
    private Provider getProvider(Long providerId) {
        return providerGateway.findById(providerId)
                .orElseThrow(() -> new BizException("提供商不存在: " + providerId));
    }

    /**
     * 获取ChatModel实例
     */
    private ChatModel getChatModel(Provider provider) {
        Object modelBean = modelBeanManagerService.getModelBean(provider, "chat");
        if (modelBean instanceof ChatModel) {
            return (ChatModel) modelBean;
        }
        throw new BizException("无法获取ChatModel实例");
    }

    /**
     * 获取StreamingChatModel实例
     */
    private StreamingChatModel getStreamingChatModel(Provider provider) {
        Object modelBean = modelBeanManagerService.getModelBean(provider, "chat");
        if (modelBean instanceof StreamingChatModel) {
            return (StreamingChatModel) modelBean;
        }
        throw new BizException("无法获取StreamingChatModel实例");
    }

    /**
     * 构建完整的Prompt
     */
    private String buildPrompt(String message, String systemPrompt, Boolean enableRAG, Long knowledgeId) {
        StringBuilder prompt = new StringBuilder();

        // 添加系统Prompt
        if (systemPrompt != null && !systemPrompt.trim().isEmpty()) {
            prompt.append("系统指令：").append(systemPrompt).append("\n\n");
        }

        // 添加RAG检索的知识
        if (enableRAG != null && enableRAG && knowledgeId != null) {
            String ragContext = retrieveKnowledge(message, knowledgeId);
            if (ragContext != null && !ragContext.trim().isEmpty()) {
                prompt.append("相关知识：\n").append(ragContext).append("\n\n");
            }
        }

        // 添加用户消息
        prompt.append("用户问题：").append(message);

        return prompt.toString();
    }

    /**
     * 检索相关知识
     */
    private String retrieveKnowledge(String query, Long knowledgeId) {
        try {
            // 获取嵌入模型
            EmbeddingModel embeddingModel = getEmbeddingModel();
            if (embeddingModel == null) {
                log.warn("嵌入模型不可用，跳过知识检索");
                return null;
            }

            // 生成查询向量
            dev.langchain4j.data.embedding.Embedding queryEmbedding = embeddingModel.embed(query).content();

            // 执行向量搜索
            List<EmbeddingMatch<TextSegment>> matches = embeddingStore.search(
                    EmbeddingSearchRequest.builder()
                            .queryEmbedding(queryEmbedding)
                            .maxResults(5)
                            .minScore(0.7)
                            .build()
            ).matches();

            if (matches == null || matches.isEmpty()) {
                return null;
            }

            // 获取相关文档内容
            List<String> relevantContents = new ArrayList<>();
            for (EmbeddingMatch<TextSegment> match : matches) {
                if (match.embedded() != null) {
                    // 这里需要根据实际的EmbeddingStore实现来获取文档内容
                    // 暂时返回匹配的相似度信息
                    relevantContents.add("相关内容 (相似度: " + match.score() + ")");
                }
            }

            return String.join("\n", relevantContents);

        } catch (Exception e) {
            log.error("知识检索失败: query={}, knowledgeId={}, error={}", 
                    query, knowledgeId, e.getMessage());
            return null;
        }
    }

    /**
     * 获取嵌入模型
     */
    private EmbeddingModel getEmbeddingModel() {
        try {
            // 获取第一个可用的嵌入模型
            Object modelBean = modelBeanManagerService.getFirstModelBean();
            if (modelBean instanceof EmbeddingModel) {
                return (EmbeddingModel) modelBean;
            }
            return null;
        } catch (Exception e) {
            log.warn("获取嵌入模型失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 构建消息列表
     */
    private List<ChatMessage> buildMessages(String prompt, String sessionId) {
        List<ChatMessage> messages = new ArrayList<>();

        // 添加历史上下文
        if (sessionId != null && !sessionId.trim().isEmpty()) {
            ModelContext context = modelContextGateway.findBySessionId(sessionId);
            if (context != null && context.getMessagesList() != null && !context.getMessagesList().isEmpty()) {
                List<ModelContext.ContextMessage> historyMessages = context.getMessagesList();
                for (ModelContext.ContextMessage msg : historyMessages) {
                    switch (msg.getRole()) {
                        case "user":
                            messages.add(UserMessage.from(msg.getContent()));
                            break;
                        case "assistant":
                            messages.add(AiMessage.from(msg.getContent()));
                            break;
                        case "system":
                            messages.add(SystemMessage.from(msg.getContent()));
                            break;
                        default:
                            messages.add(UserMessage.from(msg.getContent()));
                    }
                }
            }
        }

        // 添加当前用户消息
        messages.add(UserMessage.from(prompt));

        return messages;
    }

    /**
     * 解析推理参数
     */
    private Map<String, Object> parseInferenceParams(String inferenceParams) {
        Map<String, Object> params = new HashMap<>();
        
        if (inferenceParams != null && !inferenceParams.trim().isEmpty()) {
            try {
                JSONObject jsonParams = JSON.parseObject(inferenceParams);
                params.putAll(jsonParams);
            } catch (Exception e) {
                log.warn("解析推理参数失败: {}, error={}", inferenceParams, e.getMessage());
            }
        }

        // 设置默认参数
        params.putIfAbsent("temperature", 0.7);
        params.putIfAbsent("max_tokens", 2000);
        params.putIfAbsent("top_p", 1.0);

        return params;
    }

    /**
     * 保存到上下文
     */
    private void saveToContext(String sessionId, Long modelId, String userMessage, String assistantMessage) {
        try {
            // 查找或创建上下文
            ModelContext context = modelContextGateway.findBySessionId(sessionId);
            if (context == null) {
                context = ModelContext.builder()
                        .sessionId(sessionId)
                        .modelId(modelId)
                        .contextWindow(4000)
                        .currentLength(0)
                        .isDeleted(0)
                        .createdAt(System.currentTimeMillis())
                        .updatedAt(System.currentTimeMillis())
                        .build();
            }

            // 添加用户消息
            context.addUserMessage(userMessage);
            
            // 添加助手消息
            context.addAssistantMessage(assistantMessage);

            // 保存上下文
            modelContextGateway.save(context);

            log.debug("上下文保存成功: sessionId={}, messageCount={}", 
                    sessionId, context.getMessagesList() != null ? context.getMessagesList().size() : 0);

        } catch (Exception e) {
            log.error("保存上下文失败: sessionId={}, error={}", sessionId, e.getMessage());
        }
    }
} 