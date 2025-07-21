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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Markdown文档解析器
 * 支持标题层次、代码块、表格等Markdown结构
 */
@Slf4j
@Component
public class MarkdownDocumentParser implements DocumentParser {
    
    private static final Pattern HEADER_PATTERN = Pattern.compile("^(#{1,6})\\s+(.+)$");
    private static final Pattern CODE_BLOCK_PATTERN = Pattern.compile("^```(\\w*)$");
    private static final Pattern TABLE_PATTERN = Pattern.compile("^\\|(.+\\|)+$");
    
    @Override
    public List<Document> parse(Resource resource, String fileName) throws IOException {
        List<Document> documents = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
            StringBuilder fullContent = new StringBuilder();
            StringBuilder currentSection = new StringBuilder();
            String currentHeader = "";
            int headerLevel = 0;
            boolean inCodeBlock = false;
            String codeLanguage = "";
            StringBuilder codeContent = new StringBuilder();
            List<String> tableRows = new ArrayList<>();
            
            String line;
            int lineNumber = 0;
            
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                fullContent.append(line).append("\n");
                
                // 检查代码块
                Matcher codeBlockMatcher = CODE_BLOCK_PATTERN.matcher(line.trim());
                if (codeBlockMatcher.matches()) {
                    if (!inCodeBlock) {
                        // 开始代码块
                        inCodeBlock = true;
                        codeLanguage = codeBlockMatcher.group(1);
                        codeContent = new StringBuilder();
                    } else {
                        // 结束代码块
                        inCodeBlock = false;
                        createCodeBlockDocument(codeContent.toString(), codeLanguage, 
                                               fileName, currentHeader, documents);
                        codeContent = new StringBuilder();
                        codeLanguage = "";
                    }
                    continue;
                }
                
                // 在代码块中，直接添加内容
                if (inCodeBlock) {
                    codeContent.append(line).append("\n");
                    continue;
                }
                
                // 检查表格行
                if (TABLE_PATTERN.matcher(line.trim()).matches()) {
                    tableRows.add(line.trim());
                    continue;
                } else if (!tableRows.isEmpty()) {
                    // 表格结束，处理表格
                    createTableDocument(tableRows, fileName, currentHeader, documents);
                    tableRows.clear();
                }
                
                // 检查标题
                Matcher headerMatcher = HEADER_PATTERN.matcher(line);
                if (headerMatcher.matches()) {
                    // 保存之前的章节
                    if (currentSection.length() > 0) {
                        createSectionDocument(currentSection.toString(), currentHeader, 
                                             headerLevel, fileName, documents);
                    }
                    
                    // 开始新章节
                    headerLevel = headerMatcher.group(1).length();
                    currentHeader = headerMatcher.group(2);
                    currentSection = new StringBuilder();
                    currentSection.append(line).append("\n");
                } else {
                    // 添加到当前章节
                    currentSection.append(line).append("\n");
                }
            }
            
            // 处理最后一个章节
            if (currentSection.length() > 0) {
                createSectionDocument(currentSection.toString(), currentHeader, 
                                     headerLevel, fileName, documents);
            }
            
            // 处理最后一个表格
            if (!tableRows.isEmpty()) {
                createTableDocument(tableRows, fileName, currentHeader, documents);
            }
            
            // 创建完整文档
            Metadata fullMetadata = Metadata.from("type", "markdown_full")
                    .add("fileName", fileName)
                    .add("totalLines", String.valueOf(lineNumber));
            
            documents.add(Document.from(fullContent.toString(), fullMetadata));
            
            log.info("Markdown解析完成，文件: {}，行数: {}，生成文档数: {}", 
                    fileName, lineNumber, documents.size());
                    
        } catch (Exception e) {
            log.error("Markdown文档解析失败: {}", fileName, e);
            throw new IOException("Markdown文档解析失败: " + e.getMessage(), e);
        }
        
        return documents;
    }
    
    /**
     * 创建章节文档
     */
    private void createSectionDocument(String content, String header, int level, 
                                      String fileName, List<Document> documents) {
        if (content.trim().isEmpty()) return;
        
        Metadata sectionMetadata = Metadata.from("type", "markdown_section")
                .add("fileName", fileName)
                .add("header", header)
                .add("headerLevel", String.valueOf(level))
                .add("sectionType", getSectionType(content));
        
        documents.add(Document.from(content, sectionMetadata));
    }
    
    /**
     * 创建代码块文档
     */
    private void createCodeBlockDocument(String code, String language, 
                                        String fileName, String section, 
                                        List<Document> documents) {
        if (code.trim().isEmpty()) return;
        
        String content = String.format("```%s\n%s\n```", language, code);
        
        Metadata codeMetadata = Metadata.from("type", "markdown_code")
                .add("fileName", fileName)
                .add("language", language)
                .add("section", section)
                .add("lineCount", String.valueOf(code.split("\n").length));
        
        documents.add(Document.from(content, codeMetadata));
    }
    
    /**
     * 创建表格文档
     */
    private void createTableDocument(List<String> tableRows, String fileName, 
                                    String section, List<Document> documents) {
        if (tableRows.isEmpty()) return;
        
        String content = String.join("\n", tableRows);
        
        // 提取表头
        String headers = "";
        if (tableRows.size() > 0) {
            headers = tableRows.get(0).replaceAll("\\|", "").trim();
        }
        
        Metadata tableMetadata = Metadata.from("type", "markdown_table")
                .add("fileName", fileName)
                .add("section", section)
                .add("rowCount", String.valueOf(tableRows.size()))
                .add("headers", headers);
        
        documents.add(Document.from(content, tableMetadata));
    }
    
    /**
     * 判断章节类型
     */
    private String getSectionType(String content) {
        if (content.contains("```")) {
            return "code_section";
        } else if (content.contains("![")) {
            return "image_section";
        } else if (content.contains("|") && content.contains("---")) {
            return "table_section";
        } else if (content.contains("- ") || content.contains("* ") || content.contains("1. ")) {
            return "list_section";
        } else {
            return "text_section";
        }
    }
    
    @Override
    public String[] getSupportedTypes() {
        return new String[]{"md", "markdown", "mdx"};
    }
    
    @Override
    public boolean supports(String extension) {
        return Arrays.asList(getSupportedTypes()).contains(extension.toLowerCase());
    }
}