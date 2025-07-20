package com.leyue.smartcs.bot.advisor;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * RAG-Fusion问答Advisor
 * 实现query多样化、批量检索、结果融合等功能
 * 作为独立的服务类，供LLMGatewayImpl调用
 */
@Slf4j
public class FusionQuestionAnswerAdvisor {

    private final EmbeddingStore<Document> embeddingStore;
    private final EmbeddingModel embeddingModel;
    private final FusionConfig fusionConfig;
    private final StreamingChatModel streamingChatModel;

    public FusionQuestionAnswerAdvisor(EmbeddingStore<Document> embeddingStore,
                                       EmbeddingModel embeddingModel,
                                       StreamingChatModel streamingChatModel,
                                       FusionConfig fusionConfig) {
        this.embeddingStore = embeddingStore;
        this.embeddingModel = embeddingModel;
        this.streamingChatModel = streamingChatModel;
        this.fusionConfig = fusionConfig;
    }

    /**
     * 执行RAG-Fusion检索
     *
     * @param userQuery 用户查询
     * @return 融合后的上下文
     */
    public String performFusionSearch(String userQuery) {
        if (!StringUtils.hasText(userQuery)) {
            return "";
        }

        try {
            // 0. 翻译query
            String translatedQuery = translateQuery(userQuery);
            log.info("翻译后的query: {}", translatedQuery);

            // 1. query多样化生成
            List<String> queries = generateQueries(userQuery);
            log.info("生成查询变体: {}", queries);

            // 2. 多query批量检索
            List<Document> allDocuments = batchRetrieve(queries);
            log.info("批量检索获得文档数: {}", allDocuments.size());

            // 3. 检索结果融合
            List<Document> fusedDocuments = fuseDocuments(allDocuments);
            log.info("融合后文档数: {}", fusedDocuments.size());

            // 4. 构建上下文
            return buildContext(fusedDocuments);

        } catch (Exception e) {
            log.error("RAG-Fusion处理失败: {}", e.getMessage(), e);
            return "";
        }
    }

    private String translateQuery(String userQuery) {
        // 暂时返回原查询，后续可添加翻译逻辑
        return userQuery;
    }

    /**
     * AI Service接口定义 - 用于生成query变体
     */
    interface QueryGenerator {
        @UserMessage("生成 {{maxQueries}} 个与下列问题相关的检索子查询：\n{{originalQuery}}\n子查询：")
        void generateQueries(String maxQueries, String originalQuery, StreamingChatResponseHandler handler);
    }

    /**
     * 生成query变体
     */
    private List<String> generateQueries(String originalQuery) {
        try {
            // 使用AI Services创建query生成器
            QueryGenerator queryGenerator = AiServices.builder(QueryGenerator.class)
                    .streamingChatModel(streamingChatModel)
                    .build();
            
            // 使用流式模型收集完整回答
            StringBuilder resultBuilder = new StringBuilder();
            CompletableFuture<Void> future = new CompletableFuture<>();
            
            queryGenerator.generateQueries(
                String.valueOf(fusionConfig.getMaxQueries()), 
                originalQuery, 
                new StreamingChatResponseHandler() {
                    @Override
                    public void onPartialResponse(String partialResponse) {
                        resultBuilder.append(partialResponse);
                    }

                    @Override
                    public void onCompleteResponse(dev.langchain4j.model.chat.response.ChatResponse completeResponse) {
                        future.complete(null);
                    }

                    @Override
                    public void onError(Throwable error) {
                        future.completeExceptionally(error);
                    }
                }
            );
            
            future.get();
            return Arrays.asList(resultBuilder.toString().split("\n"));
            
        } catch (Exception e) {
            log.error("生成query变体失败: {}", e.getMessage(), e);
            return Arrays.asList(originalQuery);
        }
    }

    /**
     * 批量检索
     */
    private List<Document> batchRetrieve(List<String> queries) {
        List<CompletableFuture<List<Document>>> futures = queries.stream()
                .map(query -> CompletableFuture.supplyAsync(() -> {
                    try {
                        // 使用LangChain4j的向量检索
                        Embedding queryEmbedding = embeddingModel.embed(query).content();
                        EmbeddingSearchRequest request = EmbeddingSearchRequest.builder()
                                .queryEmbedding(queryEmbedding)
                                .maxResults(fusionConfig.getTopK())
                                .minScore(fusionConfig.getSimilarityThreshold())
                                .build();
                        List<EmbeddingMatch<Document>> matches =
                                embeddingStore.search(request).matches();
                        return matches.stream()
                                .map(EmbeddingMatch::embedded)
                                .collect(Collectors.toList());
                    } catch (Exception e) {
                        log.error("检索失败, query: {}, error: {}", query, e.getMessage());
                        return Collections.<Document>emptyList();
                    }
                }))
                .collect(Collectors.toList());

        return futures.stream()
                .map(CompletableFuture::join)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    /**
     * 融合检索结果
     */
    private List<Document> fuseDocuments(List<Document> allDocuments) {
        // 按内容去重并统计出现频次
        Map<String, DocumentWithScore> documentMap = new ConcurrentHashMap<>();

        for (Document doc : allDocuments) {
            String content = doc.text();
            if (StringUtils.hasText(content)) {
                documentMap.computeIfAbsent(content, k -> new DocumentWithScore(doc, 0))
                        .incrementScore();
            }
        }

        // 按分数排序并限制数量
        return documentMap.values().stream()
                .sorted((a, b) -> Integer.compare(b.getScore(), a.getScore()))
                .limit(fusionConfig.getMaxFusedDocuments())
                .map(DocumentWithScore::getDocument)
                .collect(Collectors.toList());
    }

    /**
     * 构建上下文
     */
    private String buildContext(List<Document> documents) {
        StringBuilder context = new StringBuilder();
        int tokenCount = 0;

        for (Document doc : documents) {
            String content = doc.text();
            if (StringUtils.hasText(content)) {
                // 简单的token估算（实际应用中可用更精确的tokenizer）
                int contentTokens = content.length() / 4;
                if (tokenCount + contentTokens > fusionConfig.getMaxContextTokens()) {
                    break;
                }

                context.append(content).append("\n\n");
                tokenCount += contentTokens;
            }
        }

        return context.toString();
    }

    /**
     * 带分数的文档包装类
     */
    private static class DocumentWithScore {
        private final Document document;
        private int score;

        public DocumentWithScore(Document document, int score) {
            this.document = document;
            this.score = score;
        }

        public Document getDocument() {
            return document;
        }

        public int getScore() {
            return score;
        }

        public void incrementScore() {
            this.score++;
        }
    }

    /**
     * 融合配置类
     */
    @Data
    public static class FusionConfig {
        // 最大查询数
        private int maxQueries = 5;
        // 每个查询返回的文档数量
        private int topK = 3;
        // 相似度阈值
        private double similarityThreshold = 0.7;
        // 最大融合文档数量
        private int maxFusedDocuments = 10;
        // 最大上下文token数量
        private int maxContextTokens = 4000;
    }
}