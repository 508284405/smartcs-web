package com.leyue.smartcs.knowledge.config;

import dev.langchain4j.store.embedding.EmbeddingStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class VectorStoreConfig {
    
    @Value("${spring.redis.host:localhost}")
    private String redisHost;
    
    @Value("${spring.redis.port:6379}")
    private Integer redisPort;
    
    @Value("${spring.redis.password:}")
    private String redisPassword;
    
    // 暂时注释掉具体实现，避免导入错误
    // 后续需要引入正确的LangChain4j Redis向量存储依赖
    /*
    @Bean
    public EmbeddingStore<dev.langchain4j.data.embedding.Embedding> embeddingStore() {
        RedisEmbeddingStore.Builder builder = RedisEmbeddingStore.builder()
                .host(redisHost)
                .port(redisPort);
        
        if (redisPassword != null && !redisPassword.trim().isEmpty()) {
            builder.password(redisPassword);
        }
        
        return builder.build();
    }
    */
}
