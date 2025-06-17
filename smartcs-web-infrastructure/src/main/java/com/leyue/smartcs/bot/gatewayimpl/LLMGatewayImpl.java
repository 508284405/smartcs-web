package com.leyue.smartcs.bot.gatewayimpl;

import com.alibaba.cola.exception.BizException;
import com.alibaba.fastjson2.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leyue.smartcs.config.ModelBeanManagerService;
import com.leyue.smartcs.config.context.UserContext;
import com.leyue.smartcs.domain.bot.BotProfile;
import com.leyue.smartcs.domain.bot.PromptTemplate;
import com.leyue.smartcs.domain.bot.gateway.BotProfileGateway;
import com.leyue.smartcs.domain.bot.gateway.LLMGateway;
import com.leyue.smartcs.domain.bot.gateway.PromptTemplateGateway;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.spec.McpSchema;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.mcp.client.autoconfigure.NamedClientMcpTransport;
import org.springframework.ai.mcp.client.autoconfigure.configurer.McpSyncClientConfigurer;
import org.springframework.ai.mcp.client.autoconfigure.properties.McpClientCommonProperties;
import org.springframework.ai.mcp.client.autoconfigure.properties.McpSseClientProperties;
import org.springframework.ai.mcp.client.autoconfigure.properties.McpSseClientProperties.SseParameters;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
    private final McpSseClientProperties sseProperties;
    private final ObjectProvider<ObjectMapper> objectMapperProvider;
    private final McpSyncClientConfigurer mcpSyncClientConfigurer;
    private final McpClientCommonProperties commonProperties;

    @Override
    public String generateAnswer(String sessionId, String question, Long botId, boolean isRag) {
        BotProfile botProfile = getBotProfile(botId);
        ChatClient chatClient = buildChatClient(botProfile, sessionId, botId, isRag);
        try {
            log.info("生成回答, sessionId: {}", sessionId);
            // 调用LLM生成
            return chatClient.prompt(question).call().content();
        } catch (Exception e) {
            log.error("生成回答失败: {}", e.getMessage(), e);
            throw new BizException("生成回答失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void generateAnswerStream(String sessionId, String question, Long botId, Consumer<String> chunkConsumer,
            boolean isRag) {
        BotProfile botProfile = getBotProfile(botId);
        log.info("流式生成回答, sessionId: {}", sessionId);

        ChatClient chatClient = buildChatClient(botProfile, sessionId, botId, isRag);
        try {
            // 调用LLM流式生成
            chatClient.prompt(question)
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

    private ChatModel buildChatModel(BotProfile botProfile) {
        return (ChatModel) modelBeanManagerService.getModelBean(botProfile);
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
     * @return ChatClient
     */
    private ChatClient buildChatClient(BotProfile botProfile, String sessionId, Long botId, boolean isRag) {
        // 构建ChatModel
        ChatModel chatModel = buildChatModel(botProfile);
        // 构建ChatOptions
        ChatOptions chatOptions = buildChatOptions(botProfile.getOptions(),chatModel);
        // 系统prompt模板
        PromptTemplate promptTemplate = getPromptTemplate(botProfile);
        // 构建MCP客户端
        List<McpSyncClient> mcpSyncClients = buildMcpSyncClients();
        ChatClient.Builder builder = ChatClient.builder(chatModel)
                .defaultSystem(promptTemplate.getTemplateContent())
                .defaultToolCallbacks(new SyncMcpToolCallbackProvider(mcpSyncClients))
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .defaultAdvisors(a -> a.param(ChatMemory.CONVERSATION_ID, sessionId))
                .defaultAdvisors(a -> a.param("userId", UserContext.getCurrentUser().getId()))
                .defaultAdvisors(a -> a.param("botId", botId));
        if (isRag) {
            builder.defaultAdvisors(QuestionAnswerAdvisor.builder(vectorStore)
                    .searchRequest(SearchRequest.builder()
                            .similarityThreshold(0.8d)
                            .topK(6)
                            .build())
                    .build());
        }
        builder.defaultOptions(chatOptions);
        return builder.build();
    }

    private List<McpSyncClient> buildMcpSyncClients() {
        ObjectMapper objectMapper = objectMapperProvider.getIfAvailable(ObjectMapper::new);

        List<NamedClientMcpTransport> namedTransports = new ArrayList<>();

        for (Map.Entry<String, SseParameters> serverParameters : sseProperties.getConnections().entrySet()) {

            String baseUrl = serverParameters.getValue().url();
            String sseEndpoint = serverParameters.getValue().sseEndpoint() != null
                    ? serverParameters.getValue().sseEndpoint()
                    : "/sse";
            var transport = HttpClientSseClientTransport.builder(baseUrl)
                    .sseEndpoint(sseEndpoint)
                    .clientBuilder(HttpClient.newBuilder())
                    .requestBuilder(HttpRequest.newBuilder().header("Authorization", getAuthorization()))
                    .objectMapper(objectMapper)
                    .build();
            namedTransports.add(new NamedClientMcpTransport(serverParameters.getKey(), transport));
        }
        List<McpSyncClient> mcpSyncClients = new ArrayList<>();

        if (!CollectionUtils.isEmpty(namedTransports)) {
            for (NamedClientMcpTransport namedTransport : namedTransports) {

                McpSchema.Implementation clientInfo = new McpSchema.Implementation(
                        this.connectedClientName(commonProperties.getName(), namedTransport.name()),
                        commonProperties.getVersion());

                McpClient.SyncSpec spec = McpClient.sync(namedTransport.transport())
                        .clientInfo(clientInfo)
                        .requestTimeout(commonProperties.getRequestTimeout());

                spec = mcpSyncClientConfigurer.configure(namedTransport.name(), spec);

                var client = spec.build();
                client.initialize();
                client.ping();
                mcpSyncClients.add(client);
            }
        }

        return mcpSyncClients;
    }

    private String getAuthorization() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes instanceof ServletRequestAttributes servletRequestAttributes) {
            HttpServletRequest request = servletRequestAttributes.getRequest();
            return request.getHeader("Authorization");
        }
        return null;
    }

    private String connectedClientName(String clientName, String serverConnectionName) {
        return clientName + " - " + serverConnectionName;
    }

    /**
     * 构建ChatOptions
     *
     * @param options 选项
     * @return ChatOptions
     */
    private ChatOptions buildChatOptions(String options, ChatModel chatModel) {
        // 根据options构建ChatOptions
        if (chatModel instanceof OpenAiChatModel) {
            // OpenAiChatOptions是工具调用的ChatOptions，会将工具名称发送给LLM
            OpenAiChatOptions.Builder builder = OpenAiChatOptions.builder();
            JSONObject jsonObject = JSONObject.parseObject(options);
            builder.model(jsonObject.getString("model")); // 模型
            builder.temperature(jsonObject.getDouble("temperature")); // 温度
            builder.maxTokens(jsonObject.getInteger("maxTokens")); // 最大token数
            return builder.build();
        }
        ChatOptions.Builder builder = ChatOptions.builder();
        JSONObject jsonObject = JSONObject.parseObject(options);
        builder.model(jsonObject.getString("model"));
        builder.temperature(jsonObject.getDouble("temperature"));
        builder.maxTokens(jsonObject.getInteger("maxTokens"));
        return builder.build();
    }
}