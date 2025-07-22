package com.leyue.smartcs.knowledge.parser.impl;

import com.leyue.smartcs.knowledge.parser.DocumentParser;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * TXT文档解析器
 */
@Component
@Slf4j
public class TxtDocumentParser implements DocumentParser {

    @Override
    public List<Document> parse(Resource resource, String fileName) throws IOException {
        log.info("开始解析TXT文件: {}", fileName);
        
        List<Document> documents = new ArrayList<>();
        StringBuilder fullContent = new StringBuilder();
        int lineNumber = 0;
        
        try (InputStream inputStream = resource.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            
            String line;
            StringBuilder paragraphContent = new StringBuilder();
            int paragraphIndex = 0;
            
            while ((line = reader.readLine()) != null) {
                fullContent.append(line).append("\n");
                lineNumber++;
                
                if (line.trim().isEmpty()) {
                    // 空行表示段落结束
                    if (paragraphContent.length() > 0) {
                        String paragraph = paragraphContent.toString().trim();
                        if (!paragraph.isEmpty()) {
                            Metadata paragraphMetadata = Metadata.from("type", "paragraph")
                                    .put("fileName", fileName)
                                    .put("paragraphIndex", String.valueOf(paragraphIndex))
                                    .put("startLine", String.valueOf(lineNumber - paragraphContent.toString().split("\n").length))
                                    .put("endLine", String.valueOf(lineNumber - 1));
                            
                            documents.add(Document.from(paragraph, paragraphMetadata));
                            paragraphIndex++;
                        }
                        paragraphContent = new StringBuilder();
                    }
                } else {
                    paragraphContent.append(line).append("\n");
                }
            }
            
            // 处理最后一个段落（如果文件不以空行结束）
            if (paragraphContent.length() > 0) {
                String paragraph = paragraphContent.toString().trim();
                if (!paragraph.isEmpty()) {
                    Metadata paragraphMetadata = Metadata.from("type", "paragraph")
                            .put("fileName", fileName)
                            .put("paragraphIndex", String.valueOf(paragraphIndex))
                            .put("startLine", String.valueOf(lineNumber - paragraphContent.toString().split("\n").length))
                            .put("endLine", String.valueOf(lineNumber));
                    
                    documents.add(Document.from(paragraph, paragraphMetadata));
                }
            }
            
            // 创建完整文档（如果内容不为空）
            String fullText = fullContent.toString();
            if (!fullText.trim().isEmpty()) {
                Metadata fullMetadata = Metadata.from("type", "full_document")
                        .put("fileName", fileName)
                        .put("totalLines", String.valueOf(lineNumber))
                        .put("totalParagraphs", String.valueOf(documents.size()))
                        .put("fileSize", String.valueOf(fullContent.length()));
                
                documents.add(Document.from(fullText, fullMetadata));
            } else {
                // 如果文件为空，创建一个占位文档
                Metadata emptyMetadata = Metadata.from("type", "empty_document")
                        .put("fileName", fileName)
                        .put("totalLines", "0")
                        .put("totalParagraphs", "0")
                        .put("fileSize", "0");
                
                documents.add(Document.from("空文档", emptyMetadata));
            }
            
            log.info("TXT解析完成，文件: {}，总行数: {}，段落数: {}", fileName, lineNumber, documents.size() - 1);
            
        } catch (IOException e) {
            log.error("TXT解析失败: {}", fileName, e);
            throw new IOException("TXT文件解析失败: " + e.getMessage(), e);
        }
        
        return documents;
    }

    @Override
    public String[] getSupportedTypes() {
        return new String[]{"txt", "text"};
    }

    @Override
    public boolean supports(String extension) {
        if (extension == null) {
            return false;
        }
        String lowerExt = extension.toLowerCase();
        return "txt".equals(lowerExt) || "text".equals(lowerExt);
    }

    /**
     * 保持向后兼容的方法（已弃用）
     * @deprecated 使用 parse(Resource resource, String fileName) 方法替代
     */
    @Deprecated
    public String parseContent(String fileUrl) throws Exception {
        log.warn("使用了已弃用的 parseContent 方法，建议使用 parse 方法");
        
        try (InputStream inputStream = new URL(fileUrl).openStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            
            StringBuilder content = new StringBuilder();
            String line;
            
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            
            String text = content.toString();
            log.info("TXT解析完成，提取文本长度: {}", text.length());
            return text;
            
        } catch (Exception e) {
            log.error("TXT解析失败: {}", fileUrl, e);
            throw new Exception("TXT解析失败: " + e.getMessage(), e);
        }
    }
} 