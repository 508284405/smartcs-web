package com.leyue.smartcs.knowledge.executor.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;

import com.alibaba.cola.dto.MultiResponse;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.config.ModelBeanManagerService;
import com.leyue.smartcs.domain.model.gateway.ModelGateway;
import com.leyue.smartcs.domain.model.gateway.ProviderGateway;
import com.leyue.smartcs.dto.knowledge.ChunkDTO;
import com.leyue.smartcs.dto.knowledge.KnowledgeGeneralChunkCmd;

import com.leyue.smartcs.knowledge.enums.DocumentTypeEnum;
import com.leyue.smartcs.knowledge.parser.DocumentParser;
import com.leyue.smartcs.knowledge.parser.factory.DocumentParserFactory;
import com.leyue.smartcs.knowledge.parser.model.ParserExtendParam;
import com.leyue.smartcs.knowledge.util.TextPreprocessor;
import com.leyue.smartcs.knowledge.util.ChunkingParameterConverter;
import com.leyue.smartcs.knowledge.model.ChunkingStrategyConfig;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 通用文档分块命令执行器
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class KnowledgeGeneralChunkCmdExe {

    private final TextPreprocessor textPreprocessor;
    private final DocumentParserFactory documentParserFactory;

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

//            // 先进行文本预处理
//            List<Document> preprocessedDocuments = textPreprocessor.preprocessText(
//                    List.of(Document.from("", dev.langchain4j.data.document.Metadata.from("type", "temp"))), // 创建临时文档用于预处理
//                    cmd.getRemoveAllUrls(),
//                    cmd.getUseQASegmentation(),
//                    cmd.getQaLanguage(),
//                    chatModel);
            
//            log.info("文本预处理完成，生成{}个预处理文档", preprocessedDocuments.size());
            
            // 解析文档
            List<Document> parsedDocuments = parser.parse(resource, fileName, parserExtendParam);
            
            log.info("文档解析完成，生成{}个解析文档", parsedDocuments.size());

            // 转换命令参数为策略配置
            ChunkingStrategyConfig strategyConfig = ChunkingParameterConverter.convertToStrategyConfig(cmd);
            
            // 验证配置参数
            if (!ChunkingParameterConverter.validateConfig(strategyConfig)) {
                throw new BizException("分块参数配置无效");
            }

            // 使用改进的分块策略
            List<ChunkDTO> allChunks = executeWithImprovedStrategy(parsedDocuments, documentType, strategyConfig, cmd);

            log.info("通用文档分块完成，文件: {}，生成{}个分块", fileName, allChunks.size());
            return MultiResponse.of(allChunks);

        } catch (Exception e) {
            log.error("通用文档分块失败: {}", e.getMessage(), e);
            throw new BizException("通用文档分块失败: " + e.getMessage());
        }
    }



    /**
     * 改进的分块方式（使用所有参数）
     */
    private List<ChunkDTO> executeWithImprovedStrategy(List<Document> documents, 
                                                      DocumentTypeEnum documentType, 
                                                      ChunkingStrategyConfig strategyConfig,
                                                      KnowledgeGeneralChunkCmd cmd) {
        List<ChunkDTO> allChunks = new ArrayList<>();
        
        // 根据文档类型选择最佳分块策略
        DocumentParserFactory.ChunkingStrategy strategy = 
                documentParserFactory.getChunkingStrategy(documentType);
        
        log.info("使用改进分块策略: {} - {}", strategy.getName(), strategy.getDescription());
        
        // 应用改进的分块策略
        List<TextSegment> segments = applyImprovedChunkingStrategy(documents, strategy, strategyConfig, cmd);

        log.info("改进分块策略 {} 生成 {} 个段落", strategy.name(), segments.size());
        
        // 转换为ChunkDTO
        for (int i = 0; i < segments.size(); i++) {
            TextSegment segment = segments.get(i);
            ChunkDTO chunkDTO = convertToChunkDTO(segment, i, "improved", documentType);
            allChunks.add(chunkDTO);
        }
        
        return allChunks;
    }

    /**
     * 应用改进的分块策略（使用所有参数）
     */
    private List<TextSegment> applyImprovedChunkingStrategy(List<Document> documents, 
                                                           DocumentParserFactory.ChunkingStrategy strategy,
                                                           ChunkingStrategyConfig strategyConfig,
                                                           KnowledgeGeneralChunkCmd cmd) {
        List<TextSegment> allSegments = new ArrayList<>();
        if (documents == null || documents.isEmpty()) {
            return allSegments;
        }
        
        // 获取配置参数
        int chunkSize = strategyConfig.getChunkSize();
        int overlapSize = strategyConfig.getOverlapSize();
        String chunkSeparator = strategyConfig.getChunkSeparator();
        int minChunkSize = strategyConfig.getMinChunkSize();
        int maxChunkSize = strategyConfig.getMaxChunkSize();
        boolean keepSeparator = strategyConfig.getKeepSeparator();
        boolean stripWhitespace = strategyConfig.getStripWhitespace();
        boolean removeAllUrls = strategyConfig.getRemoveAllUrls();
        
        switch (strategy) {
            case PAGE_BASED:
            case SECTION_BASED:
            case ROW_BASED:
            case TAG_BASED:
            case TIME_BASED:
            case GROUP_BASED:
                // 对于特殊策略，文档已经在解析阶段被优化分块了
                // 应用文本预处理
                for (Document doc : documents) {
                    String processedText = preprocessText(doc.text(), stripWhitespace, removeAllUrls);
                    Document processedDoc = Document.from(processedText, doc.metadata());
                    allSegments.add(TextSegment.from(processedDoc.text(), processedDoc.metadata()));
                }
                break;
                
            case DEFAULT:
            default:
                // 使用改进的递归分块器，支持所有参数
                for (Document doc : documents) {
                    // 文本预处理
                    String processedText = preprocessText(doc.text(), stripWhitespace, removeAllUrls);
                    Document processedDoc = Document.from(processedText, doc.metadata());
                    
                    // 使用自定义参数进行分块
                    List<TextSegment> segments = DocumentSplitters.recursive(chunkSize, overlapSize)
                            .split(processedDoc);
                    
                    // 应用大小限制
                    segments = applySizeConstraints(segments, minChunkSize, maxChunkSize);
                    
                    allSegments.addAll(segments);
                }
                break;
        }
        
        return allSegments;
    }
    
    /**
     * 应用分块策略（传统方式，向后兼容）
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
     * 文本预处理
     */
    private String preprocessText(String text, boolean stripWhitespace, boolean removeAllUrls) {
        if (text == null) {
            return "";
        }
        
        String processedText = text;
        
        // 替换连续的空格、换行符和制表符
        if (stripWhitespace) {
            processedText = processedText.replaceAll("\\s+", " ");
            processedText = processedText.trim();
        }
        
        // 删除所有URL和电子邮件地址
        if (removeAllUrls) {
            processedText = textPreprocessor.removeUrlsAndEmails(processedText);
        }
        
        return processedText;
    }
    
    /**
     * 应用大小限制
     */
    private List<TextSegment> applySizeConstraints(List<TextSegment> segments, int minChunkSize, int maxChunkSize) {
        List<TextSegment> filteredSegments = new ArrayList<>();
        
        for (TextSegment segment : segments) {
            String text = segment.text();
            int textLength = text.length();
            
            // 检查最小大小限制
            if (textLength < minChunkSize) {
                // 如果太小，尝试与下一个段落合并
                continue; // 这里简化处理，实际可以更复杂的合并逻辑
            }
            
            // 检查最大大小限制
            if (textLength > maxChunkSize) {
                // 如果太大，进行进一步分割
                List<TextSegment> subSegments = splitLargeSegment(segment, maxChunkSize);
                filteredSegments.addAll(subSegments);
            } else {
                filteredSegments.add(segment);
            }
        }
        
        return filteredSegments;
    }
    
    /**
     * 分割大段落
     */
    private List<TextSegment> splitLargeSegment(TextSegment segment, int maxChunkSize) {
        List<TextSegment> subSegments = new ArrayList<>();
        String text = segment.text();
        
        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + maxChunkSize, text.length());
            
            // 尝试在句子边界分割
            if (end < text.length()) {
                int lastSentenceEnd = findLastSentenceEnd(text, start, end);
                if (lastSentenceEnd > start) {
                    end = lastSentenceEnd;
                }
            }
            
            String subText = text.substring(start, end);
            TextSegment subSegment = TextSegment.from(subText, segment.metadata());
            subSegments.add(subSegment);
            
            start = end;
        }
        
        return subSegments;
    }
    
    /**
     * 查找最后一个句子结束位置
     */
    private int findLastSentenceEnd(String text, int start, int end) {
        for (int i = end - 1; i >= start; i--) {
            char c = text.charAt(i);
            if (c == '.' || c == '!' || c == '?' || c == '\n') {
                return i + 1;
            }
        }
        return end;
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
}