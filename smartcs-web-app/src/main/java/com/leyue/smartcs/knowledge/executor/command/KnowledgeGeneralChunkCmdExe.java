package com.leyue.smartcs.knowledge.executor.command;

import com.alibaba.cola.dto.MultiResponse;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.config.ModelBeanManagerService;
import com.leyue.smartcs.dto.knowledge.ChunkDTO;
import com.leyue.smartcs.dto.knowledge.KnowledgeGeneralChunkCmd;
import com.leyue.smartcs.domain.bot.BotProfile;
import com.leyue.smartcs.domain.bot.enums.ModelTypeEnum;
import com.leyue.smartcs.domain.bot.gateway.BotProfileGateway;
import com.leyue.smartcs.knowledge.chunking.ChunkingPipeline;
import com.leyue.smartcs.knowledge.chunking.ChunkingStrategyRegistry;
import com.leyue.smartcs.knowledge.chunking.DocumentTypeChunkingConfig;
import com.leyue.smartcs.knowledge.enums.DocumentTypeEnum;
import com.leyue.smartcs.knowledge.parser.DocumentParser;
import com.leyue.smartcs.knowledge.parser.factory.DocumentParserFactory;
import com.leyue.smartcs.knowledge.parser.impl.PdfDocumentParser;
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
    private final BotProfileGateway botProfileGateway;

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
            
            // 解析文档
            List<Document> parsedDocuments;
            
            // 如果是PDF文档且指定了视觉模型，则使用多模态解析
            if (parser instanceof PdfDocumentParser && cmd.getVisionModelBotId() != null) {
                ChatModel visionModel = getVisionModel(cmd.getVisionModelBotId());
                if (visionModel != null) {
                    log.info("使用视觉模型进行PDF多模态解析，botId: {}", cmd.getVisionModelBotId());
                    parsedDocuments = ((PdfDocumentParser) parser).parse(resource, fileName, visionModel);
                } else {
                    log.warn("无法获取视觉模型，botId: {}，回退到标准解析", cmd.getVisionModelBotId());
                    parsedDocuments = parser.parse(resource, fileName);
                }
            } else {
                parsedDocuments = parser.parse(resource, fileName);
            }
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
     * 使用新的分块策略架构执行分块
     */
    private List<ChunkDTO> executeWithNewStrategy(List<Document> documents, 
                                                DocumentTypeEnum documentType, 
                                                KnowledgeGeneralChunkCmd cmd) {
        try {
            // 创建或获取文档类型的分块配置
            DocumentTypeChunkingConfig config = createChunkingConfig(documentType, cmd);
            
            // 创建分块管道
            ChunkingPipeline pipeline = chunkingStrategyRegistry.createPipeline(config);
            
            log.info("使用分块管道执行，包含 {} 个策略", config.getStrategyConfigs().size());
            
            // 执行分块管道
            List<ChunkDTO> chunks = pipeline.execute(documents);
            
            log.info("新分块策略架构完成，生成 {} 个分块", chunks.size());
            return chunks;
            
        } catch (Exception e) {
            log.warn("新分块策略执行失败，回退到传统方式: {}", e.getMessage());
            // 回退到传统分块方式
            return executeWithLegacyStrategy(documents, documentType, cmd);
        }
    }

    /**
     * 创建分块配置
     */
    private DocumentTypeChunkingConfig createChunkingConfig(DocumentTypeEnum documentType, 
                                                          KnowledgeGeneralChunkCmd cmd) {
        // 构建全局配置参数
        Map<String, Object> globalConfig = new HashMap<>();
        globalConfig.put("chunkSize", cmd.getChunkSize() != null ? cmd.getChunkSize() : 1000);
        globalConfig.put("overlapSize", cmd.getOverlapSize() != null ? cmd.getOverlapSize() : 200);
        globalConfig.put("preserveSentences", true);
        globalConfig.put("removeEmptyChunks", true);
        globalConfig.put("minChunkLength", 10);

        // 检查用户是否指定了特定的策略组合
        if (cmd.getChunkingStrategies() != null && !cmd.getChunkingStrategies().isEmpty()) {
            // 使用用户指定的策略组合
            List<DocumentTypeChunkingConfig.StrategyConfig> strategyConfigs = cmd.getChunkingStrategies().stream()
                    .map(strategyName -> DocumentTypeChunkingConfig.StrategyConfig.builder()
                            .strategyName(strategyName)
                            .enabled(true)
                            .weight(1.0)
                            .config(Map.of())
                            .build())
                    .toList();

            return DocumentTypeChunkingConfig.builder()
                    .documentType(documentType)
                    .strategyConfigs(strategyConfigs)
                    .globalConfig(globalConfig)
                    .enableParallel(false)
                    .description("用户自定义分块配置")
                    .build();
        } else {
            // 使用推荐的默认配置
            return ((com.leyue.smartcs.knowledge.chunking.ChunkingStrategyRegistryImpl) chunkingStrategyRegistry)
                    .createRecommendedConfig(documentType);
        }
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
     * 获取视觉模型
     * @param botId 机器人ID
     * @return ChatModel 视觉模型，如果获取失败返回null
     */
    private ChatModel getVisionModel(Long botId) {
        try {
            // 根据botId获取机器人配置
            BotProfile botProfile = botProfileGateway.findById(botId).orElse(null);
            if (botProfile == null) {
                log.warn("未找到机器人配置，botId: {}", botId);
                return null;
            }
            
            // 验证是否为图像模型
            if (botProfile.getModelType() != ModelTypeEnum.IMAGE && 
                botProfile.getModelType() != ModelTypeEnum.CHAT) {
                log.warn("指定的机器人不是视觉模型，botId: {}，modelType: {}", 
                        botId, botProfile.getModelType());
                return null;
            }
            
            // 从模型管理服务获取模型实例
            Object modelBean = modelBeanManagerService.getModelBean(botProfile);
            if (modelBean instanceof ChatModel) {
                log.info("成功获取视觉模型，botId: {}，modelType: {}", 
                        botId, botProfile.getModelType());
                return (ChatModel) modelBean;
            } else {
                log.warn("模型实例类型不匹配，期望ChatModel，实际: {}", 
                        modelBean != null ? modelBean.getClass().getSimpleName() : "null");
                return null;
            }
            
        } catch (Exception e) {
            log.error("获取视觉模型失败，botId: {}", botId, e);
            return null;
        }
    }
}