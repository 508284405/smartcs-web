package com.leyue.smartcs.knowledge.config;

import com.leyue.smartcs.knowledge.chunking.DocumentTypeChunkingConfig;
import com.leyue.smartcs.knowledge.enums.DocumentTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 分块策略配置类
 * 提供预定义的分块策略配置和自定义配置示例
 */
@Configuration
@Slf4j
public class ChunkingStrategyConfiguration {

    /**
     * PDF文档的分块配置示例
     * 组合使用图像处理和文本内容分块策略
     */
    @Bean("pdfChunkingConfig")
    public DocumentTypeChunkingConfig pdfChunkingConfig() {
        // 图像处理策略配置
        DocumentTypeChunkingConfig.StrategyConfig imageConfig = DocumentTypeChunkingConfig.StrategyConfig.builder()
                .strategyName("IMAGE_PROCESSING")
                .enabled(true)
                .weight(1.0)
                .config(Map.of(
                        "enableOCR", true,
                        "enableImageDescription", false,
                        "extractImageMetadata", true,
                        "maxImageSize", 10 * 1024 * 1024 // 10MB
                ))
                .condition("hasImages") // 只有包含图像时才执行
                .build();

        // 文本内容分块策略配置
        DocumentTypeChunkingConfig.StrategyConfig textConfig = DocumentTypeChunkingConfig.StrategyConfig.builder()
                .strategyName("TEXT_CONTENT")
                .enabled(true)
                .weight(2.0) // 更高权重
                .config(Map.of(
                        "chunkSize", 800,
                        "overlapSize", 150,
                        "preserveSentences", true
                ))
                .build();

        // 全局配置
        Map<String, Object> globalConfig = new HashMap<>();
        globalConfig.put("preserveMetadata", true);
        globalConfig.put("removeEmptyChunks", true);
        globalConfig.put("minChunkLength", 20);

        return DocumentTypeChunkingConfig.builder()
                .documentType(DocumentTypeEnum.PDF)
                .strategyConfigs(Arrays.asList(imageConfig, textConfig))
                .globalConfig(globalConfig)
                .enableParallel(false)
                .description("PDF文档专用分块配置：优先处理图像，然后进行文本分块")
                .build();
    }

    /**
     * HTML文档的分块配置示例
     * 组合使用图像、表格和文本处理策略
     */
    @Bean("htmlChunkingConfig")
    public DocumentTypeChunkingConfig htmlChunkingConfig() {
        // 图像处理策略
        DocumentTypeChunkingConfig.StrategyConfig imageConfig = DocumentTypeChunkingConfig.StrategyConfig.builder()
                .strategyName("IMAGE_PROCESSING")
                .enabled(true)
                .weight(1.0)
                .config(Map.of(
                        "enableOCR", false,
                        "enableImageDescription", true,
                        "extractImageMetadata", true
                ))
                .build();

        // 表格处理策略
        DocumentTypeChunkingConfig.StrategyConfig tableConfig = DocumentTypeChunkingConfig.StrategyConfig.builder()
                .strategyName("TABLE_PROCESSING")
                .enabled(true)
                .weight(1.5)
                .config(Map.of(
                        "maxRowsPerChunk", 20,
                        "preserveHeaders", true,
                        "extractTableContext", true
                ))
                .build();

        // 文本内容策略
        DocumentTypeChunkingConfig.StrategyConfig textConfig = DocumentTypeChunkingConfig.StrategyConfig.builder()
                .strategyName("TEXT_CONTENT")
                .enabled(true)
                .weight(2.0)
                .config(Map.of(
                        "chunkSize", 1200,
                        "overlapSize", 200,
                        "preserveSentences", true
                ))
                .build();

        return DocumentTypeChunkingConfig.builder()
                .documentType(DocumentTypeEnum.HTML)
                .strategyConfigs(Arrays.asList(imageConfig, tableConfig, textConfig))
                .globalConfig(Map.of(
                        "preserveMetadata", true,
                        "removeEmptyChunks", true,
                        "minChunkLength", 15
                ))
                .enableParallel(true) // HTML处理可以并行执行
                .description("HTML文档全面处理配置：图像+表格+文本三重策略")
                .build();
    }

    /**
     * Excel文档的分块配置示例
     * 专注于表格数据处理
     */
    @Bean("excelChunkingConfig")
    public DocumentTypeChunkingConfig excelChunkingConfig() {
        DocumentTypeChunkingConfig.StrategyConfig tableConfig = DocumentTypeChunkingConfig.StrategyConfig.builder()
                .strategyName("TABLE_PROCESSING")
                .enabled(true)
                .weight(1.0)
                .config(Map.of(
                        "maxRowsPerChunk", 100, // Excel可以处理更多行
                        "preserveHeaders", true,
                        "extractTableContext", false, // Excel不需要上下文
                        "groupBySheet", true // Excel特有：按工作表分组
                ))
                .build();

        return DocumentTypeChunkingConfig.builder()
                .documentType(DocumentTypeEnum.XLSX)
                .strategyConfigs(List.of(tableConfig))
                .globalConfig(Map.of(
                        "preserveMetadata", true,
                        "includeSheetInfo", true,
                        "minChunkLength", 5
                ))
                .enableParallel(false)
                .description("Excel专用配置：按工作表和行数智能分块")
                .build();
    }

    /**
     * 通用文本文档的轻量级配置
     */
    @Bean("lightweightTextConfig")
    public DocumentTypeChunkingConfig lightweightTextConfig() {
        DocumentTypeChunkingConfig.StrategyConfig textConfig = DocumentTypeChunkingConfig.StrategyConfig.builder()
                .strategyName("TEXT_CONTENT")
                .enabled(true)
                .weight(1.0)
                .config(Map.of(
                        "chunkSize", 500,
                        "overlapSize", 50,
                        "preserveSentences", true
                ))
                .build();

        return DocumentTypeChunkingConfig.builder()
                .documentType(DocumentTypeEnum.TXT)
                .strategyConfigs(List.of(textConfig))
                .globalConfig(Map.of(
                        "preserveMetadata", false, // 轻量级处理
                        "removeEmptyChunks", true,
                        "minChunkLength", 10
                ))
                .enableParallel(false)
                .description("轻量级文本处理配置：快速简单分块")
                .build();
    }

    /**
     * 创建自定义配置的工厂方法示例
     */
    public DocumentTypeChunkingConfig createCustomConfig(DocumentTypeEnum documentType, 
                                                       List<String> strategyNames,
                                                       Map<String, Object> customGlobalConfig) {
        log.info("创建自定义分块配置，文档类型: {}, 策略: {}", documentType, strategyNames);

        List<DocumentTypeChunkingConfig.StrategyConfig> strategyConfigs = strategyNames.stream()
                .map(strategyName -> DocumentTypeChunkingConfig.StrategyConfig.builder()
                        .strategyName(strategyName)
                        .enabled(true)
                        .weight(1.0)
                        .config(getDefaultStrategyConfig(strategyName))
                        .build())
                .toList();

        return DocumentTypeChunkingConfig.builder()
                .documentType(documentType)
                .strategyConfigs(strategyConfigs)
                .globalConfig(customGlobalConfig != null ? customGlobalConfig : getDefaultGlobalConfig())
                .enableParallel(false)
                .description("自定义配置 - " + String.join("+", strategyNames))
                .build();
    }

    /**
     * 获取策略的默认配置
     */
    private Map<String, Object> getDefaultStrategyConfig(String strategyName) {
        return switch (strategyName) {
            case "TEXT_CONTENT" -> Map.of(
                    "chunkSize", 1000,
                    "overlapSize", 200,
                    "preserveSentences", true
            );
            case "TABLE_PROCESSING" -> Map.of(
                    "maxRowsPerChunk", 50,
                    "preserveHeaders", true,
                    "extractTableContext", true
            );
            case "IMAGE_PROCESSING" -> Map.of(
                    "enableOCR", false,
                    "enableImageDescription", false,
                    "extractImageMetadata", true,
                    "maxImageSize", 5 * 1024 * 1024
            );
            default -> Map.of();
        };
    }

    /**
     * 获取默认全局配置
     */
    private Map<String, Object> getDefaultGlobalConfig() {
        return Map.of(
                "preserveMetadata", true,
                "removeEmptyChunks", true,
                "minChunkLength", 10
        );
    }
}