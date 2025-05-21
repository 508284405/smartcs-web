package com.leyue.smartcs.bot.gateway.impl;

import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.domain.bot.gateway.LLMGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * LLM网关实现
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class LLMGatewayImpl implements LLMGateway {
    
    private final OpenAiChatModel openAiChatModel;
    private final OpenAiEmbeddingModel embeddingModel;

    @Override
    public String generateAnswer(String prompt, Map<String, Object> options) {
        try {
            log.info("生成回答, prompt长度: {}", prompt.length());
            
            // 创建用户消息
            UserMessage userMessage = new UserMessage(prompt);
            
            // 创建选项
            ChatOptions.Builder builder = ChatOptions.builder();
            
            // 应用选项
            if (options != null) {
                if (options.containsKey("temperature")) {
                    builder.temperature((Double) options.get("temperature"));
                }
                
                if (options.containsKey("maxTokens")) {
                    builder.maxTokens((Integer) options.get("maxTokens"));
                }
                
                if (options.containsKey("model")) {
                    builder.model((String) options.get("model"));
                }
            }
            
            ChatOptions chatOptions = builder.build();
            
            // 创建提示
            Prompt prompt1 = new Prompt(List.of(userMessage), chatOptions);
            
            // 调用LLM
            ChatResponse response = openAiChatModel.call(prompt1);
            String text = response.getResult().getOutput().getText();
            log.info("生成回答成功: {}", text);
            return text;
        } catch (Exception e) {
            log.error("生成回答失败: {}", e.getMessage(), e);
            throw new BizException("生成回答失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public List<float[]> generateEmbeddings(List<String> texts) {
        try {
            log.info("生成嵌入向量, 文本数量: {}", texts.size());
            
            final int BATCH_SIZE = 25; // Maximum batch size for embedding model
            List<float[]> encodedEmbeddings = new ArrayList<>(texts.size());
            
            for (int i = 0; i < texts.size(); i += BATCH_SIZE) {
                int endIndex = Math.min(i + BATCH_SIZE, texts.size());
                List<String> batch = texts.subList(i, endIndex);
                log.debug("处理嵌入向量批次: {}-{}", i, endIndex);
                EmbeddingResponse response = embeddingModel.embedForResponse(batch);
                
                for (int j = 0; j < response.getResults().size(); j++) {
                    float[] embedding = response.getResults().get(j).getOutput();
                    encodedEmbeddings.add(embedding);
                }
            }
            log.info("生成嵌入向量成功, 嵌入向量数量: {}", encodedEmbeddings);
            return encodedEmbeddings;
        } catch (Exception e) {
            log.error("生成嵌入向量失败: {}", e.getMessage(), e);
            throw new RuntimeException("生成嵌入向量失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public List<String> listAvailableModels() {
        // Spring AI目前不提供模型列表API，返回默认模型
        // 实际应用中，可以根据配置或者从系统属性中获取
        List<String> models = new ArrayList<>();
        models.add("gpt-3.5-turbo");
        models.add("gpt-4");
        return models;
    }
} 