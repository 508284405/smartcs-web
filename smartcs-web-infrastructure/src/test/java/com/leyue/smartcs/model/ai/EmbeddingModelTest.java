package com.leyue.smartcs.model.ai;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.http.client.jdk.JdkHttpClientBuilder;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.output.Response;

/**
 * 向量模型测试用例
 * 
 * 测试LangChain4j的EmbeddingModel功能
 */
class EmbeddingModelTest {

    private static final Logger log = LoggerFactory.getLogger(EmbeddingModelTest.class);

    private EmbeddingModel embeddingModel;

    // @BeforeEach
    void setUp() {
        // 从环境变量获取配置
        String apiKey = "";
        String baseUrl = "http://localhost:11434/v1";
        
        assertNotNull(apiKey, "OPENAI_API_KEY环境变量未设置");
        
        // 构建模型实例 - 使用与DynamicModelManager相同的构建方式
        var builder = OpenAiEmbeddingModel.builder()
                .apiKey(apiKey)
                .modelName("dengcao/qwen3-embedding-0.6b:q8_0");
        
        if (baseUrl != null && !baseUrl.trim().isEmpty()) {
            builder.baseUrl(baseUrl);
        }
        
        this.embeddingModel = builder.build();
        log.info("EmbeddingModel初始化完成，baseUrl: {}", baseUrl);
    }

    @Test
    void testEmbedSingleText() {
        // 测试单个文本的向量化
        String text = "这是一个测试文本";
        
        Embedding embedding = embeddingModel.embed(text).content();
        
        assertNotNull(embedding, "嵌入向量不应为空");
        assertNotNull(embedding.vector(), "向量数组不应为空");
        assertTrue(embedding.vector().length > 0, "向量维度应大于0");
        
        log.info("文本: '{}', 向量维度: {}", text, embedding.vector().length);
        log.debug("向量前5个值: {}", java.util.Arrays.toString(
            java.util.Arrays.copyOf(embedding.vector(), Math.min(5, embedding.vector().length))
        ));
    }

    @Test
    void testEmbedMultipleTexts() {
        // 测试多个文本的向量化
        List<TextSegment> textSegments = List.of(
            TextSegment.from("人工智能是未来的发展方向"),
            TextSegment.from("机器学习算法不断进步"),
            TextSegment.from("今天天气很好")
        );
        
        Response<List<Embedding>> response = embeddingModel.embedAll(textSegments);
        List<Embedding> embeddings = response.content();
        
        assertNotNull(embeddings, "嵌入向量列表不应为空");
        assertEquals(textSegments.size(), embeddings.size(), "向量数量应与文本数量一致");
        
        for (int i = 0; i < embeddings.size(); i++) {
            Embedding embedding = embeddings.get(i);
            assertNotNull(embedding, "第" + i + "个嵌入向量不应为空");
            assertTrue(embedding.vector().length > 0, "第" + i + "个向量维度应大于0");
            
            log.info("文本{}: '{}', '{}' 向量维度: {}", i, textSegments.get(i).text(), embedding.vector(),embedding.vector().length);
        }
    }

    @Test
    void testVectorSimilarity() {
        // 测试相似文本的向量相似度
        String text1 = "我喜欢吃苹果";
        String text2 = "我爱吃苹果";
        String text3 = "今天天气很好";
        
        Embedding embedding1 = embeddingModel.embed(text1).content();
        Embedding embedding2 = embeddingModel.embed(text2).content();
        Embedding embedding3 = embeddingModel.embed(text3).content();
        
        // 计算余弦相似度
        double similarity12 = cosineSimilarity(embedding1.vector(), embedding2.vector());
        double similarity13 = cosineSimilarity(embedding1.vector(), embedding3.vector());
        
        log.info("'{}' 和 '{}' 的相似度: {}", text1, text2, similarity12);
        log.info("'{}' 和 '{}' 的相似度: {}", text1, text3, similarity13);
        
        // 相似的文本应该有更高的相似度
        assertTrue(similarity12 > similarity13, "相似文本的相似度应该更高");
        assertTrue(similarity12 > 0.8, "相似文本的相似度应该较高");
    }

    @Test
    void testEmptyText() {
        // 测试空文本的处理
        String emptyText = "";
        
        assertThrows(Exception.class, () -> {
            embeddingModel.embed(emptyText);
        }, "空文本应该抛出异常");
    }

    @Test
    void testOllamaEmbeddingModel() {
        // 测试Ollama部署的向量模型
        String ollamaBaseUrl = "http://localhost:11434";
        String modelName = "dengcao/qwen3-embedding-0.6b:q8_0"; // Ollama支持的嵌入模型
        
        // 显式指定 JDK HTTP Client，避免类路径上存在多个 HTTP 客户端实现时的冲突
        OllamaEmbeddingModel ollamaModel = OllamaEmbeddingModel.builder()
                .httpClientBuilder(new JdkHttpClientBuilder())
                .baseUrl(ollamaBaseUrl)
                .modelName(modelName)
                .timeout(java.time.Duration.ofMinutes(2))
                .build();
        
        String testText = "这是测试Ollama嵌入模型的文本";
        
        try {
            Embedding embedding = ollamaModel.embed(testText).content();
            
            assertNotNull(embedding, "Ollama嵌入向量不应为空");
            assertNotNull(embedding.vector(), "Ollama向量数组不应为空");
            assertTrue(embedding.vector().length > 0, "Ollama向量维度应大于0");
            
            log.info("Ollama模型: {}, 文本: '{}', 向量维度: {}", 
                    modelName, testText, embedding.vector().length);
            log.debug("Ollama向量前5个值: {}", java.util.Arrays.toString(
                java.util.Arrays.copyOf(embedding.vector(), Math.min(5, embedding.vector().length))
            ));
        } catch (Exception e) {
            log.warn("Ollama服务可能未启动或模型未下载: {}", e.getMessage());
            // 如果Ollama服务不可用，跳过测试而不是失败
            org.junit.jupiter.api.Assumptions.assumeTrue(false, 
                    "跳过Ollama测试，服务不可用: " + e.getMessage());
        }
    }

    @Test 
    void testOllamaMultipleEmbeddings() {
        // 测试Ollama多文本嵌入
        String ollamaBaseUrl = "http://localhost:11434";
        String modelName = "nomic-embed-text";
        
        // 显式指定 JDK HTTP Client，避免类路径冲突
        OllamaEmbeddingModel ollamaModel = OllamaEmbeddingModel.builder()
                .httpClientBuilder(new JdkHttpClientBuilder())
                .baseUrl(ollamaBaseUrl)
                .modelName(modelName)
                .timeout(java.time.Duration.ofMinutes(2))
                .build();
        
        List<TextSegment> testTexts = List.of(
            TextSegment.from("自然语言处理技术发展迅速"),
            TextSegment.from("机器学习在各个领域都有应用"),
            TextSegment.from("今天是晴朗的一天")
        );
        
        try {
            Response<List<Embedding>> response = ollamaModel.embedAll(testTexts);
            List<Embedding> embeddings = response.content();
            
            assertNotNull(embeddings, "Ollama嵌入向量列表不应为空");
            assertEquals(testTexts.size(), embeddings.size(), "Ollama向量数量应与文本数量一致");
            
            for (int i = 0; i < embeddings.size(); i++) {
                Embedding embedding = embeddings.get(i);
                assertNotNull(embedding, "Ollama第" + i + "个嵌入向量不应为空");
                assertTrue(embedding.vector().length > 0, "Ollama第" + i + "个向量维度应大于0");
                
                log.info("Ollama文本{}: '{}', 向量维度: {}", 
                        i, testTexts.get(i).text(), embedding.vector().length);
            }
            
            // 测试相似文本的相似度
            if (embeddings.size() >= 3) {
                double similarity01 = cosineSimilarity(embeddings.get(0).vector(), embeddings.get(1).vector());
                double similarity02 = cosineSimilarity(embeddings.get(0).vector(), embeddings.get(2).vector());
                
                log.info("Ollama相似文本相似度: {}, 不相似文本相似度: {}", similarity01, similarity02);
                assertTrue(similarity01 > similarity02, "Ollama模型应能区分相似和不相似文本");
            }
        } catch (Exception e) {
            log.warn("Ollama服务可能未启动或模型未下载: {}", e.getMessage());
            org.junit.jupiter.api.Assumptions.assumeTrue(false, 
                    "跳过Ollama批量测试，服务不可用: " + e.getMessage());
        }
    }

    @Test
    void testOllamaWithDifferentModels() {
        // 测试不同Ollama嵌入模型的性能对比
        String ollamaBaseUrl = "http://localhost:11434";
        String[] modelNames = {
            "nomic-embed-text",     // 通用嵌入模型
            "mxbai-embed-large",    // 大型嵌入模型
            "all-minilm"            // MiniLM嵌入模型
        };
        
        String testText = "测试不同Ollama模型的嵌入效果";
        
        for (String modelName : modelNames) {
            try {
                OllamaEmbeddingModel model = OllamaEmbeddingModel.builder()
                        .httpClientBuilder(new JdkHttpClientBuilder())
                        .baseUrl(ollamaBaseUrl)
                        .modelName(modelName)
                        .timeout(java.time.Duration.ofMinutes(2))
                        .build();
                
                long startTime = System.currentTimeMillis();
                Embedding embedding = model.embed(testText).content();
                long endTime = System.currentTimeMillis();
                
                assertNotNull(embedding, modelName + " 嵌入向量不应为空");
                assertTrue(embedding.vector().length > 0, modelName + " 向量维度应大于0");
                
                log.info("Ollama模型: {}, 向量维度: {}, 耗时: {}ms", 
                        modelName, embedding.vector().length, (endTime - startTime));
                
            } catch (Exception e) {
                log.warn("Ollama模型 {} 不可用: {}", modelName, e.getMessage());
            }
        }
    }

    /**
     * 计算两个向量的余弦相似度
     */
    private double cosineSimilarity(float[] vectorA, float[] vectorB) {
        if (vectorA.length != vectorB.length) {
            throw new IllegalArgumentException("向量维度不匹配");
        }
        
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        
        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            normA += Math.pow(vectorA[i], 2);
            normB += Math.pow(vectorB[i], 2);
        }
        
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
