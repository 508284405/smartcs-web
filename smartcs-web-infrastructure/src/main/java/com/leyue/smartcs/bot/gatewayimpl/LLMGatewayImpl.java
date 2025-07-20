package com.leyue.smartcs.bot.gatewayimpl;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.concurrent.CompletableFuture;

import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import org.springframework.stereotype.Component;

import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.bot.advisor.FusionQuestionAnswerAdvisor;
import com.leyue.smartcs.config.ModelBeanManagerService;
import com.leyue.smartcs.domain.bot.BotProfile;
import com.leyue.smartcs.domain.bot.PromptTemplate;
import com.leyue.smartcs.domain.bot.gateway.BotProfileGateway;
import com.leyue.smartcs.domain.bot.gateway.LLMGateway;
import com.leyue.smartcs.domain.bot.gateway.PromptTemplateGateway;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * LLM网关实现 - 使用LangChain4j AI Services
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class LLMGatewayImpl implements LLMGateway {
    private final BotProfileGateway botProfileGateway;
    private final ModelBeanManagerService modelBeanManagerService;
    private final ChatMemory chatMemory;
    private final PromptTemplateGateway promptTemplateGateway;
    private final EmbeddingStore<Embedding> embeddingStore;
    private final FusionQuestionAnswerAdvisor fusionQuestionAnswerAdvisor;



    /**
     * AI Service接口定义 - 用于流式聊天
     */
    interface StreamingChatAssistant {
        @SystemMessage("{{systemPrompt}}")
        void chat(@UserMessage String userMessage, @MemoryId String sessionId, StreamingChatResponseHandler handler);
    }

    @Override
    public String generateAnswer(String sessionId, String question, Long botId, boolean isRag) {
        // 非流式方法已移除，统一使用流式方法
        throw new UnsupportedOperationException("非流式方法已移除，请使用generateAnswerStream方法");
    }

    @Override
    public void generateAnswerStream(String sessionId, String question, Long botId, Consumer<String> chunkConsumer,
                                     boolean isRag) {
        BotProfile botProfile = getBotProfile(botId);
        StreamingChatModel streamingChatModel = buildStreamingChatModel(botProfile);
        
        log.info("流式生成回答, sessionId: {}, isRag: {}", sessionId, isRag);

        try {
            // 获取系统提示词
            PromptTemplate promptTemplate = getPromptTemplate(botProfile);
            String systemPrompt = promptTemplate.getTemplateContent();
            
            // 如果启用RAG，先进行向量检索
            String userMessage = question;
            if (isRag) {
                String context = fusionQuestionAnswerAdvisor.performFusionSearch(question);
                if (context != null && !context.isEmpty()) {
                    userMessage = "上下文信息：\n" + context + "\n\n用户问题：" + question;
                }
            }
            
            // 使用AI Services创建流式聊天助手
            StreamingChatAssistant assistant = AiServices.builder(StreamingChatAssistant.class)
                    .streamingChatModel(streamingChatModel)
                    .chatMemoryProvider(memoryId -> MessageWindowChatMemory.withMaxMessages(10))
                    .systemMessageProvider(memoryId -> systemPrompt)
                    .build();
            
            // 创建CompletableFuture用于异步处理
            CompletableFuture<Void> future = new CompletableFuture<>();
            
            // 调用流式AI Service
            assistant.chat(userMessage, sessionId, new StreamingChatResponseHandler() {
                @Override
                public void onPartialResponse(String partialResponse) {
                    try {
                        chunkConsumer.accept(partialResponse);
                    } catch (Exception e) {
                        log.error("处理流式响应失败: {}", e.getMessage(), e);
                        future.completeExceptionally(e);
                    }
                }

                @Override
                public void onCompleteResponse(ChatResponse completeResponse) {
                    try {
                        log.info("流式生成回答完成, sessionId: {}", sessionId);
                        future.complete(null);
                    } catch (Exception e) {
                        log.error("处理完成响应失败: {}", e.getMessage(), e);
                        future.completeExceptionally(e);
                    }
                }

                @Override
                public void onError(Throwable error) {
                    log.error("流式生成回答失败: {}", error.getMessage(), error);
                    future.completeExceptionally(error);
                }
            });
            
            // 等待流式处理完成
            future.get();
            
        } catch (Exception e) {
            log.error("流式生成回答失败: {}", e.getMessage(), e);
            throw new BizException("流式生成回答失败: " + e.getMessage(), e);
        }
    }



    private StreamingChatModel buildStreamingChatModel(BotProfile botProfile) {
        Object modelBean = modelBeanManagerService.getModelBean(botProfile);
        if (modelBean instanceof StreamingChatModel) {
            return (StreamingChatModel) modelBean;
        } else {
            throw new BizException("不支持的模型类型: " + modelBean.getClass().getSimpleName());
        }
    }

    private BotProfile getBotProfile(Long botId) {
        if (botId == null) {
            BotProfile botProfile = botProfileGateway.findAllActive().stream()
                    .findFirst()
                    .orElseThrow(() -> new BizException("没有可用的机器人"));
            log.info("使用默认机器人: {}", botProfile.getBotName());
            return botProfile;
        } else {
            Optional<BotProfile> botProfileOptional = botProfileGateway.findById(botId);
            BotProfile botProfile = botProfileOptional.orElseThrow(() -> new BizException("机器人配置不存在"));
            log.info("使用机器人: {}", botProfile.getBotName());
            return botProfile;
        }
    }

    /**
     * 获取Prompt模板
     *
     * @param botProfile 机器人配置
     * @return Prompt模板
     */
    private PromptTemplate getPromptTemplate(BotProfile botProfile) {
        return promptTemplateGateway.findByTemplateKey(botProfile.getPromptKey())
                .orElseThrow(() -> new BizException("Prompt模板不存在: " + botProfile.getPromptKey()));
    }
}