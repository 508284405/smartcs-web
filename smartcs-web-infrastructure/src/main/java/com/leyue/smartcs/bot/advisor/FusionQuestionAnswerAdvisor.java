package com.leyue.smartcs.bot.advisor;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.util.StringUtils;

import java.util.*;
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

    private final VectorStore vectorStore;
    private final FusionConfig fusionConfig;
    private final ChatModel chatModel;

    public FusionQuestionAnswerAdvisor(VectorStore vectorStore, ChatModel chatModel, FusionConfig fusionConfig) {
        this.vectorStore = vectorStore;
        this.fusionConfig = fusionConfig;
        this.chatModel = chatModel;
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
//        Query query = new Query("Hvad er Danmarks hovedstad?");
//
//        QueryTransformer queryTransformer = TranslationQueryTransformer.builder()
//                .chatClientBuilder(chatClientBuilder)
//                .targetLanguage("english")
//                .build();
//
//        Query transformedQuery = queryTransformer.transform(query);
        return userQuery;
    }

    /**
     * 生成query变体
     */
    private List<String> generateQueries(String originalQuery) {
        // 使用chatModel生成query变体
        String prompt = "生成 {num_queries} 个与下列问题相关的检索子查询：\n{query}\n子查询：";
        prompt = String.format(prompt, fusionConfig.getMaxQueries(), originalQuery);
        String result = chatModel.call(prompt);
        return Arrays.asList(result.split("\n"));
    }

    /**
     * 批量检索
     */
    private List<Document> batchRetrieve(List<String> queries) {
        List<CompletableFuture<List<Document>>> futures = queries.stream()
                .map(query -> CompletableFuture.supplyAsync(() -> {
                    try {
                        SearchRequest searchRequest = SearchRequest.builder()
                                .query(query)
                                .topK(fusionConfig.getTopK())
                                .similarityThreshold(fusionConfig.getSimilarityThreshold())
                                .build();
                        List<Document> results = vectorStore.similaritySearch(searchRequest);
                        return results != null ? results : Collections.<Document>emptyList();
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
            String content = doc.getText();
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
            String content = doc.getText();
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