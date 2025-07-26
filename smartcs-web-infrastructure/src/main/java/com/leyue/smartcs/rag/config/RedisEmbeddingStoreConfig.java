package com.leyue.smartcs.rag.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.redis.RedisEmbeddingStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

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

    /**
     * 创建基于Redis的嵌入向量存储
     * 支持高性能向量相似性搜索
     */
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

    /**
     * 创建专用于知识库的嵌入向量存储
     * 使用独立的索引和前缀以避免数据冲突
     */
    @Bean("knowledgeEmbeddingStore")
    public EmbeddingStore<TextSegment> knowledgeEmbeddingStore() {
        log.info("初始化知识库专用Redis嵌入向量存储");
        
        RedisEmbeddingStore.Builder builder = RedisEmbeddingStore.builder()
                .host(redisHost)
                .port(redisPort)
                .user("default")
                .password(redisPassword)
                .indexName("knowledge_" + indexName)
                .prefix("knowledge:" + keyPrefix)
                .dimension(dimension);

        EmbeddingStore<TextSegment> store = builder.build();
        log.info("知识库专用Redis嵌入向量存储初始化完成");
        return store;
    }

    /**
     * 创建专用于对话上下文的嵌入向量存储
     * 用于存储和检索对话历史的向量表示
     */
    @Bean("contextEmbeddingStore")
    public EmbeddingStore<TextSegment> contextEmbeddingStore() {
        log.info("初始化对话上下文专用Redis嵌入向量存储");
        
        RedisEmbeddingStore.Builder builder = RedisEmbeddingStore.builder()
                .host(redisHost)
                .port(redisPort)
                .indexName("context_" + indexName)
                .prefix("context:" + keyPrefix)
                .user("default")
                .password(redisPassword)
                .dimension(dimension);

        EmbeddingStore<TextSegment> store = builder.build();
        log.info("对话上下文专用Redis嵌入向量存储初始化完成");
        return store;
    }
}