package com.leyue.smartcs.knowledge.parser.impl;

import com.leyue.smartcs.knowledge.parser.DocumentParser;
import com.leyue.smartcs.knowledge.parser.model.ParserExtendParam;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 通用文档解析器
 * 支持TXT、HTML、CSV、VTT、PROPERTIES等格式
 */
@Slf4j
@Component
public class UniversalDocumentParser implements DocumentParser {
    
    private static final int DEFAULT_CHUNK_SIZE = 1000;
    private static final int DEFAULT_OVERLAP_SIZE = 200;
    
    @Override
    public List<Document> parse(Resource resource, String fileName, ParserExtendParam parserExtendParam) throws IOException {
        String extension = getFileExtension(fileName);
        
        switch (extension.toLowerCase()) {
            case "txt":
                return parsePlainText(resource, fileName);
            case "html":
            case "htm":
                return parseHtml(resource, fileName);
            case "csv":
                return parseCsv(resource, fileName);
            case "vtt":
                return parseVtt(resource, fileName);
            case "properties":
                return parseProperties(resource, fileName);
            default:
                return parsePlainText(resource, fileName);
        }
    }
    
    /**
     * 解析纯文本文件
     */
    private List<Document> parsePlainText(Resource resource, String fileName) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            
            StringBuilder content = new StringBuilder();
            String line;
            int lineNumber = 0;
            
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                content.append(line).append("\n");
            }
            
            String fullText = content.toString();
            
            // 创建主文档
            Metadata metadata = Metadata.from("type", "text_full")
                    .put("fileName", fileName)
                    .put("totalLines", String.valueOf(lineNumber));
            
            Document mainDocument = Document.from(fullText, metadata);
            
            // 使用LangChain4j的DocumentSplitter进行分块
            List<Document> documents = splitDocument(mainDocument, fileName);
            
            log.info("文本文档解析完成，文件: {}，行数: {}，分块数: {}", 
                    fileName, lineNumber, documents.size());
            
            return documents;
                    
        } catch (Exception e) {
            log.error("文本文档解析失败: {}", fileName, e);
            throw new IOException("文本文档解析失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 解析HTML文件
     */
    private List<Document> parseHtml(Resource resource, String fileName) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            
            StringBuilder content = new StringBuilder();
            String line;
            
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            
            String htmlContent = content.toString();
            
            // 简单的HTML标签移除（实际项目中建议使用Jsoup）
            String textContent = htmlContent
                    .replaceAll("<script[^>]*>[\\s\\S]*?</script>", "")
                    .replaceAll("<style[^>]*>[\\s\\S]*?</style>", "")
                    .replaceAll("<[^>]+>", "")
                    .replaceAll("&\\w+;", " ");
            
            Metadata htmlMetadata = Metadata.from("type", "html_content")
                    .put("fileName", fileName)
                    .put("hasScripts", String.valueOf(htmlContent.contains("<script")))
                    .put("hasStyles", String.valueOf(htmlContent.contains("<style")));
            
            Document mainDocument = Document.from(textContent, htmlMetadata);
            
            // 使用LangChain4j的DocumentSplitter进行分块
            List<Document> documents = splitDocument(mainDocument, fileName);
            
            log.info("HTML文档解析完成，文件: {}，分块数: {}", fileName, documents.size());
            
            return documents;
            
        } catch (Exception e) {
            log.error("HTML文档解析失败: {}", fileName, e);
            throw new IOException("HTML文档解析失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 解析CSV文件
     */
    private List<Document> parseCsv(Resource resource, String fileName) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            
            StringBuilder allContent = new StringBuilder();
            String line;
            int rowIndex = 0;
            String headers = null;
            
            while ((line = reader.readLine()) != null) {
                if (rowIndex == 0) {
                    headers = line;
                    allContent.append("表头: ").append(line).append("\n");
                } else {
                    allContent.append("第").append(rowIndex).append("行: ").append(line).append("\n");
                }
                rowIndex++;
            }
            
            Metadata csvMetadata = Metadata.from("type", "csv_content")
                    .put("fileName", fileName)
                    .put("totalRows", String.valueOf(rowIndex))
                    .put("headers", headers != null ? headers : "");
            
            Document mainDocument = Document.from(allContent.toString(), csvMetadata);
            
            // 使用LangChain4j的DocumentSplitter进行分块
            List<Document> documents = splitDocument(mainDocument, fileName);
            
            log.info("CSV文档解析完成，文件: {}，总行数: {}，分块数: {}", fileName, rowIndex, documents.size());
            
            return documents;
            
        } catch (Exception e) {
            log.error("CSV文档解析失败: {}", fileName, e);
            throw new IOException("CSV文档解析失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 解析VTT字幕文件
     */
    private List<Document> parseVtt(Resource resource, String fileName) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            
            StringBuilder content = new StringBuilder();
            StringBuilder cuesText = new StringBuilder();
            StringBuilder currentCue = new StringBuilder();
            String line;
            boolean inCue = false;
            int cueCount = 0;
            
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
                
                if (line.trim().isEmpty()) {
                    if (inCue && currentCue.length() > 0) {
                        cuesText.append(currentCue.toString()).append("\n\n");
                        currentCue = new StringBuilder();
                        cueCount++;
                    }
                    inCue = false;
                } else if (line.contains("-->")) {
                    // 时间戳行
                    inCue = true;
                    currentCue.append("时间: ").append(line).append("\n");
                } else if (inCue) {
                    // 字幕文本
                    currentCue.append("内容: ").append(line).append("\n");
                }
            }
            
            // 处理最后一个字幕
            if (currentCue.length() > 0) {
                cuesText.append(currentCue.toString()).append("\n\n");
                cueCount++;
            }
            
            Metadata vttMetadata = Metadata.from("type", "vtt_content")
                    .put("fileName", fileName)
                    .put("totalCues", String.valueOf(cueCount));
            
            Document mainDocument = Document.from(cuesText.toString(), vttMetadata);
            
            // 使用LangChain4j的DocumentSplitter进行分块
            List<Document> documents = splitDocument(mainDocument, fileName);
            
            log.info("VTT字幕文档解析完成，文件: {}，字幕段数: {}，分块数: {}", fileName, cueCount, documents.size());
            
            return documents;
            
        } catch (Exception e) {
            log.error("VTT文档解析失败: {}", fileName, e);
            throw new IOException("VTT文档解析失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 解析Properties文件
     */
    private List<Document> parseProperties(Resource resource, String fileName) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            
            StringBuilder content = new StringBuilder();
            String line;
            
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            
            Metadata propertiesMetadata = Metadata.from("type", "properties_content")
                    .put("fileName", fileName);
            
            Document mainDocument = Document.from(content.toString(), propertiesMetadata);
            
            // 使用LangChain4j的DocumentSplitter进行分块
            List<Document> documents = splitDocument(mainDocument, fileName);
            
            log.info("Properties文档解析完成，文件: {}，分块数: {}", fileName, documents.size());
            
            return documents;
            
        } catch (Exception e) {
            log.error("Properties文档解析失败: {}", fileName, e);
            throw new IOException("Properties文档解析失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 使用LangChain4j的DocumentSplitter进行文档分块
     */
    private List<Document> splitDocument(Document document, String fileName) {
        try {
            // 创建递归文档分割器
            DocumentSplitter splitter = DocumentSplitters.recursive(
                    DEFAULT_CHUNK_SIZE, 
                    DEFAULT_OVERLAP_SIZE
            );
            
            // 分割文档
            List<TextSegment> segments = splitter.split(document);
            
            // 转换为Document列表
            List<Document> documents = new ArrayList<>();
            for (int i = 0; i < segments.size(); i++) {
                TextSegment segment = segments.get(i);
                
                // 创建新的元数据，保留原始元数据并添加分块信息
                Metadata metadata = Metadata.from(segment.metadata().toMap())
                        .put("chunkIndex", String.valueOf(i))
                        .put("totalChunks", String.valueOf(segments.size()))
                        .put("chunkSize", String.valueOf(segment.text().length()));
                
                // 如果原始元数据中没有fileName，则添加
                if (!segment.metadata().containsKey("fileName")) {
                    metadata = metadata.put("fileName", fileName);
                }
                
                Document chunkDocument = Document.from(segment.text(), metadata);
                documents.add(chunkDocument);
            }
            
            return documents;
            
        } catch (Exception e) {
            log.error("文档分块失败: {}", fileName, e);
            // 如果分块失败，返回原始文档
            return List.of(document);
        }
    }
    
    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            return fileName.substring(lastDotIndex + 1);
        }
        return "";
    }
    
    @Override
    public String[] getSupportedTypes() {
        return new String[]{"txt", "html", "htm", "csv", "vtt", "properties"};
    }
    
    @Override
    public boolean supports(String extension) {
        return Arrays.asList(getSupportedTypes()).contains(extension.toLowerCase());
    }
}