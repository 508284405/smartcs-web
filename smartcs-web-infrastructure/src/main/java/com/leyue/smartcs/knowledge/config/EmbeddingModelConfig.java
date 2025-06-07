package com.leyue.smartcs.knowledge.config;

import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 嵌入模型配置-openai的自动配置版本太低，自定义自动配置
 */
@Configuration
public class EmbeddingModelConfig {
    @Bean
    public EmbeddingModel embeddingModel(OpenAiEmbeddingModelProperties embeddingModelProperties) {
        return new OpenAiEmbeddingModel(OpenAiApi.builder()
                .apiKey(embeddingModelProperties.getApiKey())
                .baseUrl(embeddingModelProperties.getBaseUrl())
                .build(), MetadataMode.ALL,embeddingModelProperties.getOptions());
    }
}
