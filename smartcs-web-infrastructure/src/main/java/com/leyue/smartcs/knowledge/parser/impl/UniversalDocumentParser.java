package com.leyue.smartcs.knowledge.parser.impl;

import com.leyue.smartcs.knowledge.parser.DocumentParser;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
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
    
    @Override
    public List<Document> parse(Resource resource, String fileName) throws IOException {
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
        List<Document> documents = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            
            StringBuilder content = new StringBuilder();
            String line;
            int lineNumber = 0;
            
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                content.append(line).append("\n");
            }
            
            // 按段落分块
            String fullText = content.toString();
            String[] paragraphs = fullText.split("\n\n+");
            
            for (int i = 0; i < paragraphs.length; i++) {
                String paragraph = paragraphs[i].trim();
                if (!paragraph.isEmpty()) {
                    Metadata metadata = Metadata.from("type", "text_paragraph")
                            .add("fileName", fileName)
                            .add("paragraphIndex", String.valueOf(i))
                            .add("totalParagraphs", String.valueOf(paragraphs.length));
                    
                    documents.add(Document.from(paragraph, metadata));
                }
            }
            
            // 创建完整文档
            Metadata fullMetadata = Metadata.from("type", "text_full")
                    .add("fileName", fileName)
                    .add("totalLines", String.valueOf(lineNumber))
                    .add("totalParagraphs", String.valueOf(paragraphs.length));
            
            documents.add(Document.from(fullText, fullMetadata));
            
            log.info("文本文档解析完成，文件: {}，行数: {}，段落数: {}", 
                    fileName, lineNumber, paragraphs.length);
                    
        } catch (Exception e) {
            log.error("文本文档解析失败: {}", fileName, e);
            throw new IOException("文本文档解析失败: " + e.getMessage(), e);
        }
        
        return documents;
    }
    
    /**
     * 解析HTML文件
     */
    private List<Document> parseHtml(Resource resource, String fileName) throws IOException {
        List<Document> documents = new ArrayList<>();
        
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
                    .add("fileName", fileName)
                    .add("hasScripts", String.valueOf(htmlContent.contains("<script")))
                    .add("hasStyles", String.valueOf(htmlContent.contains("<style")));
            
            documents.add(Document.from(textContent, htmlMetadata));
            
            log.info("HTML文档解析完成，文件: {}", fileName);
            
        } catch (Exception e) {
            log.error("HTML文档解析失败: {}", fileName, e);
            throw new IOException("HTML文档解析失败: " + e.getMessage(), e);
        }
        
        return documents;
    }
    
    /**
     * 解析CSV文件
     */
    private List<Document> parseCsv(Resource resource, String fileName) throws IOException {
        List<Document> documents = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            
            String line;
            int rowIndex = 0;
            String headers = null;
            StringBuilder allContent = new StringBuilder();
            
            while ((line = reader.readLine()) != null) {
                if (rowIndex == 0) {
                    headers = line;
                    allContent.append("表头: ").append(line).append("\n");
                } else {
                    allContent.append("第").append(rowIndex).append("行: ").append(line).append("\n");
                    
                    // 每50行创建一个分块
                    if (rowIndex % 50 == 0) {
                        Metadata chunkMetadata = Metadata.from("type", "csv_chunk")
                                .add("fileName", fileName)
                                .add("startRow", String.valueOf(rowIndex - 49))
                                .add("endRow", String.valueOf(rowIndex))
                                .add("headers", headers != null ? headers : "");
                        
                        documents.add(Document.from(allContent.toString(), chunkMetadata));
                        allContent = new StringBuilder();
                        allContent.append("表头: ").append(headers).append("\n");
                    }
                }
                rowIndex++;
            }
            
            // 处理剩余内容
            if (allContent.length() > 0) {
                Metadata chunkMetadata = Metadata.from("type", "csv_chunk")
                        .add("fileName", fileName)
                        .add("startRow", String.valueOf((rowIndex / 50) * 50 + 1))
                        .add("endRow", String.valueOf(rowIndex - 1))
                        .add("headers", headers != null ? headers : "");
                
                documents.add(Document.from(allContent.toString(), chunkMetadata));
            }
            
            log.info("CSV文档解析完成，文件: {}，总行数: {}", fileName, rowIndex);
            
        } catch (Exception e) {
            log.error("CSV文档解析失败: {}", fileName, e);
            throw new IOException("CSV文档解析失败: " + e.getMessage(), e);
        }
        
        return documents;
    }
    
    /**
     * 解析VTT字幕文件
     */
    private List<Document> parseVtt(Resource resource, String fileName) throws IOException {
        List<Document> documents = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            
            StringBuilder content = new StringBuilder();
            StringBuilder currentCue = new StringBuilder();
            String line;
            boolean inCue = false;
            
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
                
                if (line.trim().isEmpty()) {
                    if (inCue && currentCue.length() > 0) {
                        // 创建字幕段落文档
                        Metadata cueMetadata = Metadata.from("type", "vtt_cue")
                                .add("fileName", fileName);
                        
                        documents.add(Document.from(currentCue.toString(), cueMetadata));
                        currentCue = new StringBuilder();
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
                Metadata cueMetadata = Metadata.from("type", "vtt_cue")
                        .add("fileName", fileName);
                
                documents.add(Document.from(currentCue.toString(), cueMetadata));
            }
            
            // 创建完整字幕文档
            Metadata fullMetadata = Metadata.from("type", "vtt_full")
                    .add("fileName", fileName);
            
            documents.add(Document.from(content.toString(), fullMetadata));
            
            log.info("VTT字幕文档解析完成，文件: {}，字幕段数: {}", fileName, documents.size() - 1);
            
        } catch (Exception e) {
            log.error("VTT文档解析失败: {}", fileName, e);
            throw new IOException("VTT文档解析失败: " + e.getMessage(), e);
        }
        
        return documents;
    }
    
    /**
     * 解析Properties文件
     */
    private List<Document> parseProperties(Resource resource, String fileName) throws IOException {
        List<Document> documents = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            
            StringBuilder content = new StringBuilder();
            StringBuilder currentGroup = new StringBuilder();
            String groupName = "default";
            String line;
            
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
                
                // 跳过注释和空行
                if (line.trim().isEmpty() || line.trim().startsWith("#")) {
                    if (line.trim().startsWith("#") && !line.trim().startsWith("##")) {
                        // 检查是否为分组注释
                        if (currentGroup.length() > 0) {
                            // 保存之前的分组
                            Metadata groupMetadata = Metadata.from("type", "properties_group")
                                    .add("fileName", fileName)
                                    .add("groupName", groupName);
                            
                            documents.add(Document.from(currentGroup.toString(), groupMetadata));
                            currentGroup = new StringBuilder();
                        }
                        groupName = line.trim().substring(1).trim();
                    }
                    continue;
                }
                
                currentGroup.append(line).append("\n");
            }
            
            // 处理最后一个分组
            if (currentGroup.length() > 0) {
                Metadata groupMetadata = Metadata.from("type", "properties_group")
                        .add("fileName", fileName)
                        .add("groupName", groupName);
                
                documents.add(Document.from(currentGroup.toString(), groupMetadata));
            }
            
            // 创建完整配置文档
            Metadata fullMetadata = Metadata.from("type", "properties_full")
                    .add("fileName", fileName);
            
            documents.add(Document.from(content.toString(), fullMetadata));
            
            log.info("Properties文档解析完成，文件: {}，分组数: {}", fileName, documents.size() - 1);
            
        } catch (Exception e) {
            log.error("Properties文档解析失败: {}", fileName, e);
            throw new IOException("Properties文档解析失败: " + e.getMessage(), e);
        }
        
        return documents;
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