package com.leyue.smartcs.knowledge.infrastructure.startup;

import com.leyue.smartcs.domain.common.Constants;
import com.leyue.smartcs.knowledge.gateway.impl.TextSearchGatewayImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.search.index.FieldIndex;
import org.redisson.api.search.index.VectorDistParam;
import org.redisson.api.search.index.VectorTypeParam;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import static com.leyue.smartcs.domain.common.Constants.UMBEDDING_INDEX_REDISEARCH;

/**
 * 应用启动时初始化 RediSearch 索引
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RediSearchIndexInitializer implements CommandLineRunner {

    private final TextSearchGatewayImpl textSearchGatewayImpl;

    @Override
    public void run(String... args) throws Exception {
        log.info("开始初始化 RediSearch 索引...");


        // 创建 FAQ 索引,将Faq作为其索引字段
        textSearchGatewayImpl.createIndex(Constants.FAQ_INDEX_REDISEARCH, FieldIndex.text("question").as("question"), FieldIndex.text("answer").as("answer"));

        // 创建文档段落 embedding 索引
        // 假设文档段落内容存储在 'content' 字段
        textSearchGatewayImpl.createIndex(UMBEDDING_INDEX_REDISEARCH, FieldIndex.text("id").as("id"),
                 FieldIndex.hnswVector("embedding")
                         .type(VectorTypeParam.Type.FLOAT32)
                         .dim(1536)
                         .distance(VectorDistParam.DistanceMetric.L2)
        );

        log.info("RediSearch 索引初始化完成.");
    }
} 