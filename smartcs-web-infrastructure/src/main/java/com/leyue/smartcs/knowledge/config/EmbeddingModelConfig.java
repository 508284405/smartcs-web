package com.leyue.smartcs.knowledge.config;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 嵌入模型配置-使用LangChain4j的OpenAI嵌入模型
 */
// @Configuration
public class EmbeddingModelConfig {
    @Bean
    public EmbeddingModel embeddingModel(OpenAiEmbeddingModelProperties embeddingModelProperties) {
        return OpenAiEmbeddingModel.builder()
                .apiKey(embeddingModelProperties.getApiKey())
                .baseUrl(embeddingModelProperties.getBaseUrl())
                .build();
    }
}
