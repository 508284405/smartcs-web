package com.leyue.smartcs.knowledge.chunking;

import com.leyue.smartcs.knowledge.model.ChunkingStrategyConfig;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.*;
import dev.langchain4j.data.segment.TextSegment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 基于LangChain4j的分块策略实现
 * 使用LangChain4j内置的分块器，减少自定义实现
 */
@Slf4j
@Component
public class LangChain4jChunkingStrategy {

    /**
     * 分块策略类型枚举
     */
    public enum ChunkingType {
        RECURSIVE,      // 递归分块
        BY_PARAGRAPH,   // 按段落分块
        BY_LINE,        // 按行分块
        BY_SENTENCE,    // 按句子分块
        BY_WORD,        // 按词分块
        BY_CHARACTER,   // 按字符分块
        BY_REGEX        // 按正则表达式分块
    }

    /**
     * 执行文档分块
     */
    public List<TextSegment> chunkDocuments(List<Document> documents, 
                                           ChunkingType chunkingType,
                                           ChunkingStrategyConfig config) {
        if (documents == null || documents.isEmpty()) {
            return new ArrayList<>();
        }

        DocumentSplitter splitter = createSplitter(chunkingType, config);
        List<TextSegment> allSegments = new ArrayList<>();

        for (Document document : documents) {
            try {
                // 预处理文档文本
                String processedText = preprocessText(document.text(), config);
                Document processedDocument = Document.from(processedText, document.metadata());
                
                // 执行分块
                List<TextSegment> segments = splitter.split(processedDocument);
                
                // 应用大小限制
                segments = applySizeConstraints(segments, config);
                
                allSegments.addAll(segments);
                
                log.debug("文档分块完成: 原始文档长度={}, 分块数={}, 分块类型={}", 
                         document.text().length(), segments.size(), chunkingType);
                         
            } catch (Exception e) {
                log.error("文档分块失败: {}", e.getMessage(), e);
                // 发生错误时，将整个文档作为一个分块
                allSegments.add(TextSegment.from(document.text(), document.metadata()));
            }
        }

        log.info("总分块完成: 文档数={}, 总分块数={}, 分块类型={}", 
                documents.size(), allSegments.size(), chunkingType);
                
        return allSegments;
    }

    /**
     * 创建指定类型的分块器
     */
    private DocumentSplitter createSplitter(ChunkingType chunkingType, ChunkingStrategyConfig config) {
        return switch (chunkingType) {
            case RECURSIVE -> {
                int chunkSize = config.getChunkSize() != null ? config.getChunkSize() : 1000;
                int overlapSize = config.getOverlapSize() != null ? config.getOverlapSize() : 200;
                yield DocumentSplitters.recursive(chunkSize, overlapSize);
            }
            case BY_PARAGRAPH -> {
                int chunkSize = config.getChunkSize() != null ? config.getChunkSize() : 1000;
                int overlapSize = config.getOverlapSize() != null ? config.getOverlapSize() : 200;
                yield new DocumentByParagraphSplitter(chunkSize, overlapSize);
            }
            case BY_LINE -> {
                int chunkSize = config.getChunkSize() != null ? config.getChunkSize() : 1000;
                int overlapSize = config.getOverlapSize() != null ? config.getOverlapSize() : 200;
                yield new DocumentByLineSplitter(chunkSize, overlapSize);
            }
            case BY_SENTENCE -> {
                int chunkSize = config.getChunkSize() != null ? config.getChunkSize() : 1000;
                int overlapSize = config.getOverlapSize() != null ? config.getOverlapSize() : 200;
                yield new DocumentBySentenceSplitter(chunkSize, overlapSize);
            }
            case BY_WORD -> {
                int chunkSize = config.getChunkSize() != null ? config.getChunkSize() : 1000;
                int overlapSize = config.getOverlapSize() != null ? config.getOverlapSize() : 200;
                yield new DocumentByWordSplitter(chunkSize, overlapSize);
            }
            case BY_CHARACTER -> {
                int chunkSize = config.getChunkSize() != null ? config.getChunkSize() : 1000;
                int overlapSize = config.getOverlapSize() != null ? config.getOverlapSize() : 200;
                yield new DocumentByCharacterSplitter(chunkSize, overlapSize);
            }
            case BY_REGEX -> {
                int chunkSize = config.getChunkSize() != null ? config.getChunkSize() : 1000;
                int overlapSize = config.getOverlapSize() != null ? config.getOverlapSize() : 200;
                String separator = config.getChunkSeparator() != null ? config.getChunkSeparator() : "\\n\\n";
                yield new DocumentByRegexSplitter(separator, ".*", chunkSize, overlapSize);
            }
        };
    }

    /**
     * 文本预处理
     */
    private String preprocessText(String text, ChunkingStrategyConfig config) {
        if (text == null) {
            return "";
        }

        String processedText = text;

        // 去除多余空白字符
        if (config.getStripWhitespace() != null && config.getStripWhitespace()) {
            processedText = processedText.replaceAll("\\s+", " ").trim();
        }

        // 移除URL（如果配置了的话）
        if (config.getRemoveAllUrls() != null && config.getRemoveAllUrls()) {
            processedText = removeUrls(processedText);
        }

        return processedText;
    }

    /**
     * 移除URL
     */
    private String removeUrls(String text) {
        return text.replaceAll("https?://[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=%]+", "")
                  .replaceAll("ftp://[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=%]+", "")
                  .replaceAll("www\\.[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=%]+", "")
                  .replaceAll("\\s+", " ")
                  .trim();
    }

    /**
     * 应用大小限制
     */
    private List<TextSegment> applySizeConstraints(List<TextSegment> segments, ChunkingStrategyConfig config) {
        if (config.getMinChunkSize() == null && config.getMaxChunkSize() == null) {
            return segments;
        }

        List<TextSegment> filteredSegments = new ArrayList<>();
        int minSize = config.getMinChunkSize() != null ? config.getMinChunkSize() : 0;
        int maxSize = config.getMaxChunkSize() != null ? config.getMaxChunkSize() : Integer.MAX_VALUE;

        for (TextSegment segment : segments) {
            String text = segment.text();
            int textLength = text.length();

            // 检查最小大小限制
            if (textLength < minSize) {
                log.debug("跳过过小的分块: 长度={}, 最小长度={}", textLength, minSize);
                continue;
            }

            // 检查最大大小限制
            if (textLength > maxSize) {
                log.debug("分割过大的分块: 长度={}, 最大长度={}", textLength, maxSize);
                List<TextSegment> subSegments = splitLargeSegment(segment, maxSize);
                for (TextSegment subSegment : subSegments) {
                    if (subSegment.text().length() >= minSize) {
                        filteredSegments.add(subSegment);
                    }
                }
            } else {
                filteredSegments.add(segment);
            }
        }

        return filteredSegments;
    }

    /**
     * 分割过大的分块
     */
    private List<TextSegment> splitLargeSegment(TextSegment segment, int maxSize) {
        List<TextSegment> subSegments = new ArrayList<>();
        String text = segment.text();

        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + maxSize, text.length());

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
            if (c == '.' || c == '!' || c == '?' || c == '\n' || c == '。' || c == '！' || c == '？') {
                return i + 1;
            }
        }
        return end;
    }

    /**
     * 根据文档类型推荐最佳分块策略
     */
    public ChunkingType recommendChunkingType(String fileExtension) {
        if (fileExtension == null) {
            return ChunkingType.RECURSIVE;
        }

        return switch (fileExtension.toLowerCase()) {
            case "md", "markdown", "mdx" -> ChunkingType.BY_PARAGRAPH;
            case "txt" -> ChunkingType.BY_PARAGRAPH;
            case "html", "htm" -> ChunkingType.BY_PARAGRAPH;
            case "csv" -> ChunkingType.BY_LINE;
            case "json", "xml" -> ChunkingType.BY_REGEX;
            case "properties" -> ChunkingType.BY_LINE;
            default -> ChunkingType.RECURSIVE;
        };
    }
}