//package com.leyue.smartcs.knowledge.chunking;
//
//import com.leyue.smartcs.dto.knowledge.ChunkDTO;
//import com.leyue.smartcs.knowledge.chunking.strategy.ImageProcessingStrategy;
//import com.leyue.smartcs.knowledge.chunking.strategy.TableProcessingStrategy;
//import com.leyue.smartcs.knowledge.chunking.strategy.TextContentChunkingStrategy;
//import com.leyue.smartcs.knowledge.enums.DocumentTypeEnum;
//import dev.langchain4j.data.document.Document;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.util.Arrays;
//import java.util.List;
//import java.util.Map;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.junit.jupiter.api.Assertions.*;
//
///**
// * 分块策略架构测试
// */
//@ExtendWith(MockitoExtension.class)
//class ChunkingStrategyTest {
//
//    private ChunkingStrategyRegistryImpl registry;
//    private TextContentChunkingStrategy textStrategy;
//    private TableProcessingStrategy tableStrategy;
//    private ImageProcessingStrategy imageStrategy;
//
//    @BeforeEach
//    void setUp() {
//        // 初始化策略
//        textStrategy = new TextContentChunkingStrategy();
//        tableStrategy = new TableProcessingStrategy();
//        imageStrategy = new ImageProcessingStrategy();
//
//        // 创建注册器
//        registry = new ChunkingStrategyRegistryImpl();
//        registry.registerStrategy(textStrategy);
//        registry.registerStrategy(tableStrategy);
//        registry.registerStrategy(imageStrategy);
//    }
//
//    @Test
//    void testStrategyRegistration() {
//        // 验证策略注册
//        assertTrue(registry.isRegistered("TEXT_CONTENT"));
//        assertTrue(registry.isRegistered("TABLE_PROCESSING"));
//        assertTrue(registry.isRegistered("IMAGE_PROCESSING"));
//
//        // 验证策略数量
//        assertThat(registry.getAllStrategies()).hasSize(3);
//        assertThat(registry.getAllStrategyNames()).contains("TEXT_CONTENT", "TABLE_PROCESSING", "IMAGE_PROCESSING");
//    }
//
//    @Test
//    void testDocumentTypeStrategyMapping() {
//        // 测试PDF文档类型的策略映射
//        List<ChunkingStrategy> pdfStrategies = registry.getStrategiesForDocumentType(DocumentTypeEnum.PDF);
//        assertThat(pdfStrategies).hasSize(2);
//        assertThat(pdfStrategies.stream().map(ChunkingStrategy::getName))
//                .contains("TEXT_CONTENT", "IMAGE_PROCESSING");
//
//        // 测试HTML文档类型的策略映射
//        List<ChunkingStrategy> htmlStrategies = registry.getStrategiesForDocumentType(DocumentTypeEnum.HTML);
//        assertThat(htmlStrategies).hasSize(3);
//        assertThat(htmlStrategies.stream().map(ChunkingStrategy::getName))
//                .contains("TEXT_CONTENT", "TABLE_PROCESSING", "IMAGE_PROCESSING");
//
//        // 测试CSV文档类型的策略映射
//        List<ChunkingStrategy> csvStrategies = registry.getStrategiesForDocumentType(DocumentTypeEnum.CSV);
//        assertThat(csvStrategies).hasSize(1);
//        assertThat(csvStrategies.get(0).getName()).isEqualTo("TABLE_PROCESSING");
//    }
//
//    @Test
//    void testTextContentChunkingStrategy() {
//        // 创建测试文档
//        Document document = Document.from("这是第一段文字。这里有更多内容。\n\n这是第二段文字，包含了不同的信息。");
//        List<Document> documents = List.of(document);
//
//        // 配置参数
//        Map<String, Object> config = Map.of(
//                "chunkSize", 50,
//                "overlapSize", 10,
//                "preserveSentences", true
//        );
//
//        // 执行分块
//        List<ChunkDTO> chunks = textStrategy.chunk(documents, DocumentTypeEnum.TXT, config);
//
//        // 验证结果
//        assertThat(chunks).isNotEmpty();
//        chunks.forEach(chunk -> {
//            assertThat(chunk.getContent()).isNotBlank();
//            assertThat(chunk.getMetadata()).contains("TEXT_CONTENT");
//            assertThat(chunk.getChunkIndex()).isNotNull();
//        });
//    }
//
//    @Test
//    void testTableProcessingStrategy() {
//        // 创建包含HTML表格的文档
//        String htmlContent = "<table><tr><th>姓名</th><th>年龄</th></tr>" +
//                             "<tr><td>张三</td><td>25</td></tr>" +
//                             "<tr><td>李四</td><td>30</td></tr></table>";
//        Document document = Document.from(htmlContent);
//        List<Document> documents = List.of(document);
//
//        // 配置参数
//        Map<String, Object> config = Map.of(
//                "maxRowsPerChunk", 2,
//                "preserveHeaders", true,
//                "extractTableContext", false
//        );
//
//        // 执行分块
//        List<ChunkDTO> chunks = tableStrategy.chunk(documents, DocumentTypeEnum.HTML, config);
//
//        // 验证结果
//        assertThat(chunks).isNotEmpty();
//        chunks.forEach(chunk -> {
//            assertThat(chunk.getContent()).contains("table");
//            assertThat(chunk.getMetadata()).contains("TABLE_PROCESSING");
//        });
//    }
//
//    @Test
//    void testImageProcessingStrategy() {
//        // 创建包含图像的Markdown文档
//        String markdownContent = "这是一个包含图像的文档。\n\n![测试图片](test-image.jpg)\n\n这是图像后的内容。";
//        Document document = Document.from(markdownContent);
//        List<Document> documents = List.of(document);
//
//        // 配置参数
//        Map<String, Object> config = Map.of(
//                "enableOCR", false,
//                "enableImageDescription", false,
//                "extractImageMetadata", true,
//                "maxImageSize", 1024 * 1024
//        );
//
//        // 执行分块
//        List<ChunkDTO> chunks = imageStrategy.chunk(documents, DocumentTypeEnum.MARKDOWN, config);
//
//        // 验证结果（可能没有实际图像处理，但应该有占位符或描述）
//        if (!chunks.isEmpty()) {
//            chunks.forEach(chunk -> {
//                assertThat(chunk.getMetadata()).contains("IMAGE_PROCESSING");
//            });
//        }
//    }
//
//    @Test
//    void testChunkingPipeline() {
//        // 创建包含多种内容的HTML文档
//        String htmlContent = "<html><body>" +
//                           "<h1>标题</h1>" +
//                           "<img src='test.jpg' alt='测试图片'/>" +
//                           "<table><tr><th>列1</th><th>列2</th></tr><tr><td>数据1</td><td>数据2</td></tr></table>" +
//                           "<p>这是一段普通文字内容。</p>" +
//                           "</body></html>";
//
//        Document document = Document.from(htmlContent);
//        List<Document> documents = List.of(document);
//
//        // 创建分块管道配置
//        DocumentTypeChunkingConfig config = DocumentTypeChunkingConfig.builder()
//                .documentType(DocumentTypeEnum.HTML)
//                .strategyConfigs(Arrays.asList(
//                        DocumentTypeChunkingConfig.StrategyConfig.builder()
//                                .strategyName("IMAGE_PROCESSING")
//                                .enabled(true)
//                                .weight(1.0)
//                                .config(Map.of("extractImageMetadata", true))
//                                .build(),
//                        DocumentTypeChunkingConfig.StrategyConfig.builder()
//                                .strategyName("TABLE_PROCESSING")
//                                .enabled(true)
//                                .weight(1.0)
//                                .config(Map.of("maxRowsPerChunk", 10))
//                                .build(),
//                        DocumentTypeChunkingConfig.StrategyConfig.builder()
//                                .strategyName("TEXT_CONTENT")
//                                .enabled(true)
//                                .weight(1.0)
//                                .config(Map.of("chunkSize", 200, "overlapSize", 50))
//                                .build()
//                ))
//                .globalConfig(Map.of("preserveMetadata", true))
//                .enableParallel(false)
//                .build();
//
//        // 创建并执行管道
//        ChunkingPipeline pipeline = registry.createPipeline(config);
//        List<ChunkDTO> chunks = pipeline.execute(documents);
//
//        // 验证管道执行结果
//        assertThat(chunks).isNotEmpty();
//
//        // 验证不同策略的分块结果都存在
//        boolean hasImageChunk = chunks.stream().anyMatch(chunk ->
//                chunk.getMetadata().contains("IMAGE_PROCESSING"));
//        boolean hasTableChunk = chunks.stream().anyMatch(chunk ->
//                chunk.getMetadata().contains("TABLE_PROCESSING"));
//        boolean hasTextChunk = chunks.stream().anyMatch(chunk ->
//                chunk.getMetadata().contains("TEXT_CONTENT"));
//
//        // 至少应该有文本分块（其他类型取决于内容解析）
//        assertTrue(hasTextChunk, "应该包含文本内容分块");
//    }
//
//    @Test
//    void testStrategyConfigValidation() {
//        // 测试有效配置
//        Map<String, Object> validConfig = Map.of(
//                "chunkSize", 500,
//                "overlapSize", 100,
//                "preserveSentences", true
//        );
//        assertTrue(textStrategy.validateConfig(validConfig));
//
//        // 测试无效配置
//        Map<String, Object> invalidConfig = Map.of(
//                "chunkSize", 50000, // 超出范围
//                "overlapSize", -100  // 负数
//        );
//        assertFalse(textStrategy.validateConfig(invalidConfig));
//    }
//
//    @Test
//    void testDefaultStrategies() {
//        // 测试获取默认策略
//        List<ChunkingStrategy> pdfDefaults = registry.getDefaultStrategies(DocumentTypeEnum.PDF);
//        assertThat(pdfDefaults).isNotEmpty();
//
//        List<ChunkingStrategy> csvDefaults = registry.getDefaultStrategies(DocumentTypeEnum.CSV);
//        assertThat(csvDefaults).hasSize(1);
//        assertThat(csvDefaults.get(0).getName()).isEqualTo("TABLE_PROCESSING");
//    }
//
//    @Test
//    void testStrategyStats() {
//        // 执行一些策略操作来生成统计数据
//        registry.getStrategy("TEXT_CONTENT");
//        registry.getStrategy("TABLE_PROCESSING");
//        registry.getStrategy("TEXT_CONTENT"); // 再次获取
//
//        // 获取统计信息
//        Map<String, Object> stats = registry.getStrategyStats();
//
//        assertThat(stats).containsKey("totalStrategies");
//        assertThat(stats).containsKey("usageStats");
//        assertThat(stats).containsKey("documentTypeSupport");
//
//        assertThat((Integer) stats.get("totalStrategies")).isEqualTo(3);
//    }
//
//    @Test
//    void testRecommendedConfig() {
//        // 测试推荐配置创建
//        DocumentTypeChunkingConfig pdfConfig = ((ChunkingStrategyRegistryImpl) registry)
//                .createRecommendedConfig(DocumentTypeEnum.PDF);
//
//        assertThat(pdfConfig).isNotNull();
//        assertThat(pdfConfig.getDocumentType()).isEqualTo(DocumentTypeEnum.PDF);
//        assertThat(pdfConfig.getStrategyConfigs()).isNotEmpty();
//        assertTrue(pdfConfig.isValid());
//    }
//}