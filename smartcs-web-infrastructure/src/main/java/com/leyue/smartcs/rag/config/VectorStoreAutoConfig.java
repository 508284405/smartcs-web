package com.leyue.smartcs.rag.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * 向量存储自动配置
 */
@Configuration
public class VectorStoreAutoConfig {
    
    @Value("${langchain4j.vectorstore.type:memory}")
    private String vectorStoreType;
} 