package com.leyue.smartcs.knowledge.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 * openai嵌入模型配置
 */
@Data
// @Component
@ConfigurationProperties(prefix = "langchain4j.openai.embedding")
public class OpenAiEmbeddingModelProperties {
    private String apiKey;
    private String baseUrl;
    // 移除SpringAI特有的Options，改为通用配置
    private String model = "text-embedding-ada-002"; // 默认模型
}
