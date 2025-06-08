package com.leyue.smartcs.bot.gatewayimpl;

import com.alibaba.cola.exception.BizException;
import com.alibaba.fastjson2.JSONObject;
import com.leyue.smartcs.config.ModelBeanManagerService;
import com.leyue.smartcs.config.context.UserContext;
import com.leyue.smartcs.domain.bot.BotProfile;
import com.leyue.smartcs.domain.bot.PromptTemplate;
import com.leyue.smartcs.domain.bot.gateway.BotProfileGateway;
import com.leyue.smartcs.domain.bot.gateway.LLMGateway;
import com.leyue.smartcs.domain.bot.gateway.PromptTemplateGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * LLM网关实现
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class LLMGatewayImpl implements LLMGateway {
    private final BotProfileGateway botProfileGateway;
    private final ModelBeanManagerService modelBeanManagerService;
    private final ChatMemory chatMemory;
    private final PromptTemplateGateway promptTemplateGateway;
    private final VectorStore vectorStore;

    @Override
    public String generateAnswer(String sessionId, String question, Long botId) {
        BotProfile botProfile = getBotProfile(botId);
        PromptTemplate promptTemplate = getPromptTemplate(botProfile);
        ChatClient chatClient = buildChatClient(botProfile);

        try {
            log.info("生成回答, sessionId: {}", sessionId);
            ChatOptions chatOptions = buildChatOptions(botProfile.getOptions());

            // 调用LLM生成
            return buildBasePrompt(chatClient, sessionId, botId, question, promptTemplate, chatOptions)
                    .call()
                    .content();
        } catch (Exception e) {
            log.error("生成回答失败: {}", e.getMessage(), e);
            throw new BizException("生成回答失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void generateAnswerStream(String sessionId, String question, Long botId, Consumer<String> chunkConsumer) {
        BotProfile botProfile = getBotProfile(botId);
        PromptTemplate promptTemplate = getPromptTemplate(botProfile);
        ChatClient chatClient = buildChatClient(botProfile);

        try {
            log.info("流式生成回答, sessionId: {}", sessionId);
            ChatOptions chatOptions = buildChatOptions(botProfile.getOptions());

            // 调用LLM流式生成
            buildBasePrompt(chatClient, sessionId, botId,question, promptTemplate, chatOptions)
                    .stream()
                    .content()
                    .doOnNext(content -> {
                        if (content != null && !content.isEmpty()) {
                            chunkConsumer.accept(content);
                        }
                    })
                    .doOnComplete(() -> log.info("流式生成回答完成"))
                    .doOnError(error -> log.error("流式生成回答失败: {}", error.getMessage()))
                    .blockLast(); // 等待流完成

        } catch (Exception e) {
            log.error("流式生成回答失败: {}", e.getMessage(), e);
            throw new BizException("流式生成回答失败: " + e.getMessage(), e);
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

    /**
     * 构建ChatClient
     *
     * @param botProfile 机器人配置
     * @return ChatClient
     */
    private ChatClient buildChatClient(BotProfile botProfile) {
        ChatModel chatModel = (ChatModel) modelBeanManagerService.getModelBean(botProfile);
        return ChatClient.builder(chatModel)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build()).build();
    }

    /**
     * 构建基础Prompt
     *
     * @param chatClient     ChatClient
     * @param sessionId      会话ID
     * @param botId
     * @param question       问题
     * @param promptTemplate Prompt模板
     * @param chatOptions    ChatOptions
     * @return ChatClientRequestSpec
     */
    private ChatClient.ChatClientRequestSpec buildBasePrompt(ChatClient chatClient, String sessionId, Long botId, String question,
                                                             PromptTemplate promptTemplate, ChatOptions chatOptions) {
        return chatClient.prompt()
                .system(promptTemplate.getTemplateContent())
                .user(question)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, sessionId)) // 会话ID
                .advisors(a -> a.param("userId", UserContext.getCurrentUser().getId()))
                .advisors(a -> a.param("botId", botId))
                .advisors(QuestionAnswerAdvisor.builder(vectorStore)
                        .searchRequest(SearchRequest.builder()
                                .similarityThreshold(0.8d)
                                .topK(6).build())
                        .build()) // RAG
                .options(chatOptions);
    }

    /**
     * 构建ChatOptions
     *
     * @param options 选项
     * @return ChatOptions
     */
    private ChatOptions buildChatOptions(String options) {
        ChatOptions.Builder builder = ChatOptions.builder();
        JSONObject jsonObject = JSONObject.parseObject(options);
        builder.model(jsonObject.getString("model"));
        builder.temperature(jsonObject.getDouble("temperature"));
        builder.maxTokens(jsonObject.getInteger("maxTokens"));
        return builder.build();
    }
}