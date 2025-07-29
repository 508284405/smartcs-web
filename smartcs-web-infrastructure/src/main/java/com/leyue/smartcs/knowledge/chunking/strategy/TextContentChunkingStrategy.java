package com.leyue.smartcs.knowledge.chunking.strategy;

import com.leyue.smartcs.dto.knowledge.ChunkDTO;
import com.leyue.smartcs.knowledge.chunking.ChunkingStrategy;
import com.leyue.smartcs.knowledge.enums.DocumentTypeEnum;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.openai.OpenAiTokenCountEstimator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 文本内容分块策略
 * 处理纯文本内容的语义分割
 */
@Component
@Slf4j
public class TextContentChunkingStrategy implements ChunkingStrategy {
    
    @Override
    public String getName() {
        return "TEXT_CONTENT";
    }
    
    @Override
    public String getDescription() {
        return "文本内容语义分块，支持递归分割和句子边界对齐";
    }
    
    @Override
    public List<DocumentTypeEnum> getSupportedDocumentTypes() {
        return List.of(
                DocumentTypeEnum.TXT,
                DocumentTypeEnum.MARKDOWN,
                DocumentTypeEnum.MDX,
                DocumentTypeEnum.HTML,
                DocumentTypeEnum.DOCX,
                DocumentTypeEnum.PDF
        );
    }
    
    @Override
    public boolean isCombinable() {
        return true;
    }
    
    @Override
    public int getPriority() {
        return 100; // 中等优先级
    }
    
    @Override
    public List<ChunkDTO> chunk(List<Document> documents, DocumentTypeEnum documentType, Map<String, Object> config) {
        log.info("执行文本内容分块策略，文档数量: {}, 文档类型: {}", documents.size(), documentType);
        
        List<ChunkDTO> chunks = new ArrayList<>();
        
        // 获取配置参数
        int chunkSize = getConfigValue(config, "chunkSize", 1000);
        int overlapSize = getConfigValue(config, "overlapSize", 200);
        boolean preserveSentences = getConfigValue(config, "preserveSentences", true);
        String chunkSeparator = getConfigValue(config, "chunkSeparator", "\n\n");
        int minChunkSize = getConfigValue(config, "minChunkSize", 10);
        int maxChunkSize = getConfigValue(config, "maxChunkSize", 5000);
        boolean keepSeparator = getConfigValue(config, "keepSeparator", true);
        boolean stripWhitespace = getConfigValue(config, "stripWhitespace", true);
        boolean removeAllUrls = getConfigValue(config, "removeAllUrls", false);
        
        for (int docIndex = 0; docIndex < documents.size(); docIndex++) {
            Document document = documents.get(docIndex);
            
            // 文本预处理
            String processedText = preprocessText(document.text(), stripWhitespace, removeAllUrls);
            Document processedDocument = Document.from(processedText, document.metadata());
            
            // 根据文档类型选择合适的分割器
            List<TextSegment> segments = createSplitter(chunkSize, overlapSize, preserveSentences)
                    .split(processedDocument);
            
            // 应用大小限制
            segments = applySizeConstraints(segments, minChunkSize, maxChunkSize);
            
            log.debug("文档 {} 分割为 {} 个段落", docIndex, segments.size());
            
            // 转换为ChunkDTO
            for (int segIndex = 0; segIndex < segments.size(); segIndex++) {
                TextSegment segment = segments.get(segIndex);
                ChunkDTO chunk = createChunkDTO(segment, docIndex, segIndex, documentType);
                chunks.add(chunk);
            }
        }
        
        log.info("文本内容分块完成，生成 {} 个分块", chunks.size());
        return chunks;
    }
    
    @Override
    public boolean validateConfig(Map<String, Object> config) {
        if (config == null) {
            return true; // 使用默认配置
        }
        
        // 验证分块大小
        Integer chunkSize = getConfigValue(config, "chunkSize", null);
        if (chunkSize != null && (chunkSize < 50 || chunkSize > 10000)) {
            log.warn("分块大小超出有效范围 [50, 10000]: {}", chunkSize);
            return false;
        }
        
        // 验证重叠大小
        Integer overlapSize = getConfigValue(config, "overlapSize", null);
        if (overlapSize != null && (overlapSize < 0 || overlapSize > 2000)) {
            log.warn("重叠大小超出有效范围 [0, 2000]: {}", overlapSize);
            return false;
        }
        
        return true;
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
            // 简单的URL和邮箱移除逻辑
            processedText = processedText.replaceAll("https?://\\S+", "");
            processedText = processedText.replaceAll("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b", "");
            processedText = processedText.replaceAll("\\s+", " ").trim();
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
                // 如果太小，跳过这个段落
                continue;
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
     * 创建文档分割器
     */
    private DocumentSplitter createSplitter(
            int chunkSize, int overlapSize, boolean preserveSentences) {
        return DocumentSplitters.recursive(chunkSize, overlapSize);
    }
    
    /**
     * 创建ChunkDTO对象
     */
    private ChunkDTO createChunkDTO(TextSegment segment, int docIndex, int segIndex, 
                                   DocumentTypeEnum documentType) {
        ChunkDTO chunk = new ChunkDTO();
        chunk.setChunkIndex(String.format("%d-%d", docIndex, segIndex));
        chunk.setContent(segment.text());
        
        // 构建元数据
        StringBuilder metadataBuilder = new StringBuilder();
        metadataBuilder.append("{");
        metadataBuilder.append("\"documentType\":\"").append(documentType.name()).append("\",");
        metadataBuilder.append("\"strategy\":\"").append(getName()).append("\",");
        metadataBuilder.append("\"docIndex\":").append(docIndex).append(",");
        metadataBuilder.append("\"segIndex\":").append(segIndex);
        
        // 添加原始元数据
        if (segment.metadata() != null && !segment.metadata().toMap().isEmpty()) {
            segment.metadata().toMap().forEach((key, value) -> {
                metadataBuilder.append(",");
                metadataBuilder.append("\"").append(key).append("\":\"").append(value).append("\"");
            });
        }
        
        metadataBuilder.append("}");
        chunk.setMetadata(metadataBuilder.toString());
        
        return chunk;
    }
    
    /**
     * 获取配置值，支持类型转换和默认值
     */
    @SuppressWarnings("unchecked")
    private <T> T getConfigValue(Map<String, Object> config, String key, T defaultValue) {
        if (config == null || !config.containsKey(key)) {
            return defaultValue;
        }
        
        try {
            return (T) config.get(key);
        } catch (ClassCastException e) {
            log.warn("配置参数 {} 类型转换失败，使用默认值: {}", key, defaultValue);
            return defaultValue;
        }
    }
}