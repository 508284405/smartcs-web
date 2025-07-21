package com.leyue.smartcs.knowledge.executor.command;

import com.alibaba.cola.dto.MultiResponse;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.config.ModelBeanManagerService;
import com.leyue.smartcs.dto.knowledge.ChunkDTO;
import com.leyue.smartcs.dto.knowledge.KnowledgeGeneralChunkCmd;
import com.leyue.smartcs.knowledge.enums.DocumentTypeEnum;
import com.leyue.smartcs.knowledge.parser.DocumentParser;
import com.leyue.smartcs.knowledge.parser.factory.DocumentParserFactory;
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
import java.util.List;

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

    /**
     * 执行通用文档分块
     * 
     * @param cmd 分块命令
     * @return 分块结果
     */
    public MultiResponse<ChunkDTO> execute(KnowledgeGeneralChunkCmd cmd) {
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
            List<Document> parsedDocuments = parser.parse(resource, fileName);
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

            List<ChunkDTO> allChunks = new ArrayList<>();
            
            // 根据文档类型选择最佳分块策略
            DocumentParserFactory.ChunkingStrategy strategy = 
                    documentParserFactory.getChunkingStrategy(documentType);
            
            log.info("使用分块策略: {} - {}", strategy.getName(), strategy.getDescription());
            
            // 应用分块策略
            List<TextSegment> segments = applyChunkingStrategy(
                    allPreprocessedTexts, strategy, cmd);
            
            // 转换为ChunkDTO
            for (int i = 0; i < segments.size(); i++) {
                TextSegment segment = segments.get(i);
                ChunkDTO chunkDTO = convertToChunkDTO(segment, i, fileName, documentType);
                allChunks.add(chunkDTO);
            }

            log.info("通用文档分块完成，文件: {}，生成{}个分块", fileName, allChunks.size());
            return MultiResponse.of(allChunks);

        } catch (Exception e) {
            log.error("通用文档分块失败: {}", e.getMessage(), e);
            throw new BizException("通用文档分块失败: " + e.getMessage());
        }
    }

    /**
     * 应用分块策略
     */
    private List<TextSegment> applyChunkingStrategy(List<Document> documents, 
                                                   DocumentParserFactory.ChunkingStrategy strategy,
                                                   KnowledgeGeneralChunkCmd cmd) {
        List<TextSegment> allSegments = new ArrayList<>();
        
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
                            cmd.getChunkOverlap() != null ? cmd.getChunkOverlap() : 200
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
        
        if (segment.metadata() != null && !segment.metadata().asMap().isEmpty()) {
            metadataBuilder.append(",");
            segment.metadata().asMap().forEach((key, value) -> 
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
}