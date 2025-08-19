package com.leyue.smartcs.rag.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.redis.RedisEmbeddingStore;
import lombok.extern.slf4j.Slf4j;

/**
 * Redis嵌入向量存储配置
 * 基于LangChain4j Redis向量存储实现
 */
@Slf4j
@Configuration
public class RedisEmbeddingStoreConfig {

    @Value("${spring.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.redis.port:6379}")
    private Integer redisPort;

    @Value("${spring.redis.password:}")
    private String redisPassword;

    @Value("${spring.redis.database:0}")
    private Integer redisDatabase;

    @Value("${langchain4j.embedding-store.redis.index-name:embedding_index}")
    private String indexName;

    @Value("${langchain4j.embedding-store.redis.prefix:embedding:}")
    private String keyPrefix;

    @Value("${langchain4j.embedding-store.dimension:1536}")
    private Integer dimension;

    @Bean
    @Primary
    public EmbeddingStore<TextSegment> embeddingStore() {
        log.info("初始化Redis嵌入向量存储 - Host: {}, Port: {}, Database: {}, IndexName: {}", 
                redisHost, redisPort, redisDatabase, indexName);
        
        RedisEmbeddingStore.Builder builder = RedisEmbeddingStore.builder()
                .host(redisHost)
                .port(6379)
                .indexName(indexName)
                .prefix(keyPrefix)
                .user("default")
                .password(redisPassword)
                .dimension(dimension);
        RedisEmbeddingStore store = builder.build();
        log.info("Redis嵌入向量存储初始化完成");
        return store;
    }
}