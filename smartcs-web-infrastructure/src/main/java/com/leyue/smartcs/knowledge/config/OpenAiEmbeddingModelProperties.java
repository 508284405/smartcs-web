package com.leyue.smartcs.knowledge.config;

import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 * openai嵌入模型配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "spring.ai.openai.embedding")
public class OpenAiEmbeddingModelProperties {
    private String apiKey;
    private String baseUrl;
    private OpenAiEmbeddingOptions options;
}
