package com.leyue.smartcs.knowledge.executor.command;

import com.alibaba.cola.dto.MultiResponse;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.config.ModelBeanManagerService;
import com.leyue.smartcs.dto.knowledge.ChunkDTO;
import com.leyue.smartcs.dto.knowledge.KnowledgeGeneralChunkCmd;
import com.leyue.smartcs.dto.knowledge.ModelRequest;
import com.leyue.smartcs.domain.model.Provider;
import com.leyue.smartcs.domain.model.Model;
import com.leyue.smartcs.domain.model.gateway.ModelGateway;
import com.leyue.smartcs.domain.model.gateway.ProviderGateway;
import com.leyue.smartcs.knowledge.chunking.ChunkingPipeline;
import com.leyue.smartcs.knowledge.chunking.ChunkingStrategyRegistry;
import com.leyue.smartcs.knowledge.chunking.DocumentTypeChunkingConfig;
import com.leyue.smartcs.knowledge.enums.DocumentTypeEnum;
import com.leyue.smartcs.knowledge.parser.DocumentParser;
import com.leyue.smartcs.knowledge.parser.factory.DocumentParserFactory;
import com.leyue.smartcs.knowledge.parser.impl.PdfDocumentParser;
import com.leyue.smartcs.knowledge.parser.model.ParserExtendParam;
import com.leyue.smartcs.knowledge.util.TextPreprocessor;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 通用文档分块命令执行器
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class KnowledgeGeneralChunkCmdExe {

    private final TextPreprocessor textPreprocessor;
    private final ModelBeanManagerService modelBeanManagerService;
    private final DocumentParserFactory documentParserFactory;
    private final ChunkingStrategyRegistry chunkingStrategyRegistry;
    private final ProviderGateway providerGateway;
    private final ModelGateway modelGateway;

    /**
     * 执行通用文档分块
     * 
     * @param cmd 分块命令
     * @return 分块结果
     */
    public MultiResponse<ChunkDTO> execute(KnowledgeGeneralChunkCmd cmd) {
        if (cmd == null || cmd.getFileUrl() == null) {
            throw new BizException("CHUNK_GENERAL_FAILED", "文件地址不能为空");
        }

        try {
            // 将OSS URL转换为Resource对象
            Resource resource = new UrlResource(cmd.getFileUrl());
            String fileName = extractFileName(cmd.getFileUrl());
            
            log.info("开始执行通用文档分块，文件: {}", fileName);
            
            // 根据文件类型选择合适的解析器
            DocumentParser parser = documentParserFactory.getParser(fileName);
            DocumentTypeEnum documentType = DocumentTypeEnum.fromFileName(fileName);
            
            log.info("选择的解析器: {}，文档类型: {}",
                    parser.getClass().getSimpleName(), documentType);
            
            // 构建ChatModel
            ParserExtendParam parserExtendParam = new ParserExtendParam();
            parserExtendParam.setModelRequest(cmd.getModelRequest());
            
            // 解析文档
            List<Document> parsedDocuments = parser.parse(resource, fileName, parserExtendParam);
            
            log.info("文档解析完成，生成{}个预处理文档", parsedDocuments.size());
            
            // 获取聊天模型（如果需要）
            ChatModel chatModel = null;
            if (cmd.getUseQASegmentation() != null && cmd.getUseQASegmentation()) {
                chatModel = (ChatModel) modelBeanManagerService.getFirstModelBean();
            }

            // 文本预处理（对每个解析后的文档进行预处理）
            List<Document> allPreprocessedTexts = new ArrayList<>();
            for (Document doc : parsedDocuments) {
                List<Document> preprocessed = textPreprocessor.preprocessText(
                        List.of(doc),
                        cmd.getRemoveAllUrls(),
                        cmd.getUseQASegmentation(),
                        cmd.getQaLanguage(),
                        chatModel);
                allPreprocessedTexts.addAll(preprocessed);
            }

            // 使用新的分块策略架构
            List<ChunkDTO> allChunks = executeWithNewStrategy(allPreprocessedTexts, documentType, cmd);

            log.info("通用文档分块完成，文件: {}，生成{}个分块", fileName, allChunks.size());
            return MultiResponse.of(allChunks);

        } catch (Exception e) {
            log.error("通用文档分块失败: {}", e.getMessage(), e);
            throw new BizException("通用文档分块失败: " + e.getMessage());
        }
    }

    /**
     * 使用智能分块策略架构执行分块
     * 根据文档内容和配置自动选择最优分块策略组合
     */
    private List<ChunkDTO> executeWithNewStrategy(List<Document> documents, 
                                                DocumentTypeEnum documentType, 
                                                KnowledgeGeneralChunkCmd cmd) {
        try {
            // 创建或获取文档类型的分块配置
            DocumentTypeChunkingConfig config = createChunkingConfig(documentType, cmd, documents);
            
            // 创建分块管道
            ChunkingPipeline pipeline = chunkingStrategyRegistry.createPipeline(config);
            
            log.info("使用智能分块管道执行，自动选择了 {} 个策略", config.getStrategyConfigs().size());
            
            // 执行分块管道
            List<ChunkDTO> chunks = pipeline.execute(documents);
            
            log.info("智能分块策略完成，生成 {} 个分块", chunks.size());
            return chunks;
            
        } catch (Exception e) {
            log.warn("智能分块策略执行失败，回退到传统方式: {}", e.getMessage());
            // 回退到传统分块方式
            return executeWithLegacyStrategy(documents, documentType, cmd);
        }
    }

    /**
     * 创建智能分块配置
     * 根据文档内容和用户配置自动决定分块策略
     */
    private DocumentTypeChunkingConfig createChunkingConfig(DocumentTypeEnum documentType, 
                                                          KnowledgeGeneralChunkCmd cmd,
                                                          List<Document> documents) {
        // 构建全局配置参数
        Map<String, Object> globalConfig = new HashMap<>();
        globalConfig.put("chunkSize", cmd.getChunkSize() != null ? cmd.getChunkSize() : 1000);
        globalConfig.put("overlapSize", cmd.getOverlapSize() != null ? cmd.getOverlapSize() : 200);
        globalConfig.put("preserveSentences", true);
        globalConfig.put("removeEmptyChunks", true);
        globalConfig.put("minChunkLength", 10);

        // 智能分析文档内容，自动决定分块策略
        List<String> intelligentStrategies = determineStrategiesFromContent(documents, cmd);
        
        log.info("根据文档内容和配置自动选择分块策略: {}", intelligentStrategies);
        
        // 构建策略配置
        List<DocumentTypeChunkingConfig.StrategyConfig> strategyConfigs = intelligentStrategies.stream()
                .map(strategyName -> DocumentTypeChunkingConfig.StrategyConfig.builder()
                        .strategyName(strategyName)
                        .enabled(true)
                        .weight(getStrategyWeight(strategyName))
                        .config(getStrategyConfig(strategyName, cmd))
                        .build())
                .toList();

        return DocumentTypeChunkingConfig.builder()
                .documentType(documentType)
                .strategyConfigs(strategyConfigs)
                .globalConfig(globalConfig)
                .enableParallel(false)
                .description("智能内容分析分块配置: " + String.join("+", intelligentStrategies))
                .build();
    }

    /**
     * 传统分块方式（向后兼容）
     */
    private List<ChunkDTO> executeWithLegacyStrategy(List<Document> documents, 
                                                   DocumentTypeEnum documentType, 
                                                   KnowledgeGeneralChunkCmd cmd) {
        List<ChunkDTO> allChunks = new ArrayList<>();
        
        // 根据文档类型选择最佳分块策略
        DocumentParserFactory.ChunkingStrategy strategy = 
                documentParserFactory.getChunkingStrategy(documentType);
        
        log.info("回退使用传统分块策略: {} - {}", strategy.getName(), strategy.getDescription());
        
        // 应用分块策略
        List<TextSegment> segments = applyChunkingStrategy(documents, strategy, cmd);

        log.info("传统分块策略 {} 生成 {} 个段落", strategy.name(), segments.size());
        
        // 转换为ChunkDTO
        for (int i = 0; i < segments.size(); i++) {
            TextSegment segment = segments.get(i);
            ChunkDTO chunkDTO = convertToChunkDTO(segment, i, "legacy", documentType);
            allChunks.add(chunkDTO);
        }
        
        return allChunks;
    }

    /**
     * 应用分块策略
     */
    private List<TextSegment> applyChunkingStrategy(List<Document> documents, 
                                                   DocumentParserFactory.ChunkingStrategy strategy,
                                                   KnowledgeGeneralChunkCmd cmd) {
        List<TextSegment> allSegments = new ArrayList<>();
        if (documents == null || documents.isEmpty()) {
            return allSegments;
        }
        
        switch (strategy) {
            case PAGE_BASED:
            case SECTION_BASED:
            case ROW_BASED:
            case TAG_BASED:
            case TIME_BASED:
            case GROUP_BASED:
                // 对于特殊策略，文档已经在解析阶段被优化分块了
                // 直接转换为TextSegment
                for (Document doc : documents) {
                    allSegments.add(TextSegment.from(doc.text(), doc.metadata()));
                }
                break;
                
            case DEFAULT:
            default:
                // 使用LangChain4j的默认递归分块器
                for (Document doc : documents) {
                    List<TextSegment> segments = DocumentSplitters.recursive(
                            cmd.getChunkSize() != null ? cmd.getChunkSize() : 1000,
                            cmd.getOverlapSize() != null ? cmd.getOverlapSize() : 200
                    ).split(doc);
                    allSegments.addAll(segments);
                }
                break;
        }
        
        return allSegments;
    }
    
    /**
     * 转换为ChunkDTO
     */
    private ChunkDTO convertToChunkDTO(TextSegment segment, int index, String fileName, DocumentTypeEnum documentType) {
        ChunkDTO chunkDTO = new ChunkDTO();
        chunkDTO.setChunkIndex(String.valueOf(index));
        chunkDTO.setContent(segment.text());
        
        // 合并元数据
        StringBuilder metadataBuilder = new StringBuilder();
        metadataBuilder.append("{");
        metadataBuilder.append("\"fileName\":\"").append(fileName).append("\",");
        metadataBuilder.append("\"documentType\":\"").append(documentType.name()).append("\"");
        
        if (segment.metadata() != null && !segment.metadata().toMap().isEmpty()) {
            metadataBuilder.append(",");
            segment.metadata().toMap().forEach((key, value) ->
                metadataBuilder.append("\"").append(key).append("\":\"").append(value).append("\",")
            );
            // 移除最后一个逗号
            if (metadataBuilder.charAt(metadataBuilder.length() - 1) == ',') {
                metadataBuilder.setLength(metadataBuilder.length() - 1);
            }
        }
        metadataBuilder.append("}");
        
        chunkDTO.setMetadata(metadataBuilder.toString());
        return chunkDTO;
    }
    
    /**
     * 从URL中提取文件名
     */
    private String extractFileName(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return "unknown_file";
        }
        
        // 从URL中提取文件名
        String fileName = fileUrl;
        int lastSlashIndex = fileName.lastIndexOf('/');
        if (lastSlashIndex >= 0 && lastSlashIndex < fileName.length() - 1) {
            fileName = fileName.substring(lastSlashIndex + 1);
        }
        
        // 移除URL参数
        int queryIndex = fileName.indexOf('?');
        if (queryIndex >= 0) {
            fileName = fileName.substring(0, queryIndex);
        }
        
        return fileName;
    }
    
    /**
     * 根据文档内容和用户配置智能决定分块策略
     * @param documents 解析后的文档列表
     * @param cmd 分块命令
     * @return 推荐的策略列表
     */
    private List<String> determineStrategiesFromContent(List<Document> documents, KnowledgeGeneralChunkCmd cmd) {
        List<String> strategies = new ArrayList<>();
        
        // 检查是否指定了视觉模型 - 如果指定则启用图像处理
        if (cmd.getVisionModelId() != null) {
            strategies.add("IMAGE_PROCESSING");
            log.info("检测到视觉模型配置，启用图像处理策略");
        }
        
        // 分析文档内容检测表格和图像
        boolean hasImages = detectImagesInDocuments(documents);
        boolean hasTables = detectTablesInDocuments(documents);
        
        // 如果没有指定视觉模型但检测到图像，添加图像处理（不启用AI描述）
        if (!strategies.contains("IMAGE_PROCESSING") && hasImages) {
            strategies.add("IMAGE_PROCESSING");
            log.info("检测到文档包含图像，启用图像处理策略");
        }
        
        // 如果检测到表格，启用表格处理
        if (hasTables) {
            strategies.add("TABLE_PROCESSING");
            log.info("检测到文档包含表格，启用表格处理策略");
        }
        
        // 始终包含文本内容处理作为基础策略
        strategies.add("TEXT_CONTENT");
        
        log.info("内容分析结果 - 图像: {}, 表格: {}, 最终策略: {}", hasImages, hasTables, strategies);
        
        return strategies;
    }
    
    /**
     * 检测文档中是否包含图像
     * @param documents 文档列表
     * @return 是否包含图像
     */
    private boolean detectImagesInDocuments(List<Document> documents) {
        return documents.stream()
                .anyMatch(doc -> {
                    Map<String, Object> metadata = doc.metadata() != null ? doc.metadata().toMap() : Map.of();
                    // 检查元数据中的图像标识
                    if (metadata.containsKey("hasImages")) {
                        return Boolean.TRUE.equals(metadata.get("hasImages"));
                    }
                    // 检查文档内容中是否包含图像引用
                    String content = doc.text();
                    return content != null && (content.contains("![") || content.contains("<img") || 
                           content.contains("image:") || content.contains(".jpg") || 
                           content.contains(".png") || content.contains(".gif"));
                });
    }
    
    /**
     * 检测文档中是否包含表格
     * @param documents 文档列表
     * @return 是否包含表格
     */
    private boolean detectTablesInDocuments(List<Document> documents) {
        return documents.stream()
                .anyMatch(doc -> {
                    Map<String, Object> metadata = doc.metadata() != null ? doc.metadata().toMap() : Map.of();
                    // 检查元数据中的表格标识
                    if (metadata.containsKey("hasTables") || metadata.containsKey("hasTable")) {
                        return Boolean.TRUE.equals(metadata.get("hasTables")) || 
                               Boolean.TRUE.equals(metadata.get("hasTable"));
                    }
                    // 检查文档内容中是否包含表格标记
                    String content = doc.text();
                    return content != null && (content.contains("<table") || content.contains("</table>") ||
                           content.contains("|") && content.contains("---") || // Markdown表格
                           content.matches(".*\\|.*\\|.*\\n.*\\|.*\\|.*")); // 表格模式匹配
                });
    }
    
    /**
     * 获取策略权重
     * @param strategyName 策略名称
     * @return 权重值
     */
    private double getStrategyWeight(String strategyName) {
        return switch (strategyName) {
            case "TEXT_CONTENT" -> 2.0; // 文本处理权重最高
            case "TABLE_PROCESSING" -> 1.5; // 表格处理中等权重
            case "IMAGE_PROCESSING" -> 1.0; // 图像处理基础权重
            default -> 1.0;
        };
    }
    
    /**
     * 获取策略特定配置
     * @param strategyName 策略名称
     * @param cmd 分块命令
     * @return 策略配置
     */
    private Map<String, Object> getStrategyConfig(String strategyName, KnowledgeGeneralChunkCmd cmd) {
        return switch (strategyName) {
            case "TEXT_CONTENT" -> Map.of(
                    "chunkSize", cmd.getChunkSize() != null ? cmd.getChunkSize() : 800,
                    "overlapSize", cmd.getOverlapSize() != null ? cmd.getOverlapSize() : 150,
                    "preserveSentences", true
            );
            case "TABLE_PROCESSING" -> Map.of(
                    "maxRowsPerChunk", 50,
                    "preserveHeaders", true,
                    "extractTableContext", true
            );
            case "IMAGE_PROCESSING" -> Map.of(
                    "enableOCR", false,
                    "enableImageDescription", cmd.getVisionModelId() != null, // 只有指定视觉模型才启用AI描述
                    "extractImageMetadata", true,
                    "maxImageSize", 10 * 1024 * 1024 // 10MB
            );
            default -> Map.of();
        };
    }
    
    /**
     * 获取视觉模型
     * @param providerId 提供商ID
     * @return ChatModel 视觉模型，如果获取失败返回null
     */
    private ChatModel getVisionModel(ModelRequest modelRequest) {
        Long modelId = modelRequest.getModelId();
        try {
            Model model = modelGateway.findById(modelId).orElse(null);
            if (model == null) {
                log.warn("未找到模型配置，modelId: {}", modelId);
                return null;
            }
            
            // 根据providerId获取提供商配置
            Long providerId = model.getProviderId();
            Provider provider = providerGateway.findById(providerId).orElse(null);
            if (provider == null) {
                log.warn("未找到提供商配置，providerId: {}", providerId);
                return null;
            }
            // 验证提供商配置是否有效
            if (!provider.isValid()) {
                log.warn("提供商配置无效，providerId: {}", providerId);
                return null;
            }
            
            // 检查是否支持chat模型类型（视觉模型通常是chat类型）
            List<String> supportedTypes = provider.getSupportedModelTypesList();
            if (!supportedTypes.contains("chat")) {
                log.warn("指定的提供商不支持chat模型类型，providerId: {}，支持的类型: {}", 
                        providerId, supportedTypes);
                return null;
            }
            
            // 从模型管理服务获取chat模型实例
            Object modelBean = modelBeanManagerService.getModelBean(provider, "chat");
            if (modelBean instanceof ChatModel) {
                log.info("成功获取视觉模型，modelId: {}，providerType: {}",
                        modelId, provider.getProviderType());
                return (ChatModel) modelBean;
            } else {
                log.warn("模型实例类型不匹配，期望ChatModel，实际: {}", 
                        modelBean != null ? modelBean.getClass().getSimpleName() : "null");
                return null;
            }
            
        } catch (Exception e) {
            log.error("获取视觉模型失败，modelId: {}", modelId, e);
            return null;
        }
    }
}