package com.leyue.smartcs.knowledge.parser.impl;

import com.leyue.smartcs.knowledge.parser.DocumentParser;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * DOCX文档解析器
 * 支持段落、表格、标题等结构化内容
 */
@Slf4j
@Component
public class DocxDocumentParser implements DocumentParser {
    
    @Override
    public List<Document> parse(Resource resource, String fileName) throws IOException {
        List<Document> documents = new ArrayList<>();
        
        try (InputStream inputStream = resource.getInputStream()) {
            XWPFDocument document = new XWPFDocument(inputStream);
            
            // 解析段落内容
            List<XWPFParagraph> paragraphs = document.getParagraphs();
            parseParagraphs(paragraphs, fileName, documents);
            
            // 解析表格内容
            List<XWPFTable> tables = document.getTables();
            parseTables(tables, fileName, documents);
            
            // 创建完整文档
            createFullDocument(document, fileName, documents);
            
            document.close();
            log.info("DOCX解析完成，文件: {}，生成文档数: {}", fileName, documents.size());
            
        } catch (Exception e) {
            log.error("DOCX文档解析失败: {}", fileName, e);
            throw new IOException("DOCX文档解析失败: " + e.getMessage(), e);
        }
        
        return documents;
    }
    
    /**
     * 解析段落
     */
    private void parseParagraphs(List<XWPFParagraph> paragraphs, String fileName, List<Document> documents) {
        StringBuilder currentSection = new StringBuilder();
        String currentHeading = "";
        int sectionIndex = 0;
        
        for (int i = 0; i < paragraphs.size(); i++) {
            XWPFParagraph paragraph = paragraphs.get(i);
            String text = paragraph.getText().trim();
            
            if (text.isEmpty()) {
                continue;
            }
            
            // 检查是否为标题（基于样式或格式）
            boolean isHeading = isHeadingParagraph(paragraph);
            
            if (isHeading) {
                // 保存之前的章节
                if (currentSection.length() > 0) {
                    createSectionDocument(currentSection.toString(), currentHeading, 
                                         fileName, sectionIndex++, documents);
                }
                
                // 开始新章节
                currentHeading = text;
                currentSection = new StringBuilder();
                currentSection.append(text).append("\n\n");
            } else {
                // 添加到当前章节
                currentSection.append(text).append("\n");
                
                // 每10个段落创建一个分块
                if (currentSection.length() > 2000) {
                    createSectionDocument(currentSection.toString(), currentHeading, 
                                         fileName, sectionIndex++, documents);
                    currentSection = new StringBuilder();
                    if (!currentHeading.isEmpty()) {
                        currentSection.append("章节: ").append(currentHeading).append("\n\n");
                    }
                }
            }
        }
        
        // 处理最后一个章节
        if (currentSection.length() > 0) {
            createSectionDocument(currentSection.toString(), currentHeading, 
                                 fileName, sectionIndex, documents);
        }
    }
    
    /**
     * 解析表格
     */
    private void parseTables(List<XWPFTable> tables, String fileName, List<Document> documents) {
        for (int tableIndex = 0; tableIndex < tables.size(); tableIndex++) {
            XWPFTable table = tables.get(tableIndex);
            
            StringBuilder tableContent = new StringBuilder();
            tableContent.append("表格 ").append(tableIndex + 1).append(":\n");
            
            List<XWPFTableRow> rows = table.getRows();
            if (rows.isEmpty()) {
                continue;
            }
            
            // 提取表头
            XWPFTableRow headerRow = rows.get(0);
            List<String> headers = new ArrayList<>();
            for (XWPFTableCell cell : headerRow.getTableCells()) {
                headers.add(cell.getText().trim());
            }
            
            if (!headers.isEmpty()) {
                tableContent.append("表头: ").append(String.join(" | ", headers)).append("\n");
            }
            
            // 处理数据行
            for (int rowIndex = 1; rowIndex < rows.size(); rowIndex++) {
                XWPFTableRow row = rows.get(rowIndex);
                List<String> rowData = new ArrayList<>();
                
                for (XWPFTableCell cell : row.getTableCells()) {
                    rowData.add(cell.getText().trim());
                }
                
                if (!rowData.isEmpty()) {
                    tableContent.append("第").append(rowIndex).append("行: ")
                              .append(String.join(" | ", rowData)).append("\n");
                }
            }
            
            // 创建表格文档
            Metadata tableMetadata = Metadata.from("type", "docx_table")
                    .add("fileName", fileName)
                    .add("tableIndex", String.valueOf(tableIndex))
                    .add("rowCount", String.valueOf(rows.size()))
                    .add("columnCount", String.valueOf(headers.size()))
                    .add("headers", String.join(",", headers));
            
            documents.add(Document.from(tableContent.toString(), tableMetadata));
        }
    }
    
    /**
     * 创建章节文档
     */
    private void createSectionDocument(String content, String heading, String fileName, 
                                      int sectionIndex, List<Document> documents) {
        if (content.trim().isEmpty()) {
            return;
        }
        
        Metadata sectionMetadata = Metadata.from("type", "docx_section")
                .add("fileName", fileName)
                .add("heading", heading)
                .add("sectionIndex", String.valueOf(sectionIndex))
                .add("contentType", getContentType(content));
        
        documents.add(Document.from(content, sectionMetadata));
    }
    
    /**
     * 创建完整文档
     */
    private void createFullDocument(XWPFDocument document, String fileName, List<Document> documents) {
        StringBuilder fullContent = new StringBuilder();
        
        // 添加所有段落
        for (XWPFParagraph paragraph : document.getParagraphs()) {
            String text = paragraph.getText().trim();
            if (!text.isEmpty()) {
                fullContent.append(text).append("\n");
            }
        }
        
        // 添加所有表格（简化文本形式）
        for (int i = 0; i < document.getTables().size(); i++) {
            fullContent.append("\n[表格 ").append(i + 1).append("]\n");
        }
        
        Metadata fullMetadata = Metadata.from("type", "docx_full")
                .add("fileName", fileName)
                .add("paragraphCount", String.valueOf(document.getParagraphs().size()))
                .add("tableCount", String.valueOf(document.getTables().size()));
        
        documents.add(Document.from(fullContent.toString(), fullMetadata));
    }
    
    /**
     * 判断段落是否为标题
     */
    private boolean isHeadingParagraph(XWPFParagraph paragraph) {
        // 检查样式名称
        String styleName = paragraph.getStyle();
        if (styleName != null) {
            String lowerStyle = styleName.toLowerCase();
            if (lowerStyle.contains("heading") || lowerStyle.contains("title") || 
                lowerStyle.startsWith("h") || lowerStyle.contains("标题")) {
                return true;
            }
        }
        
        // 检查文本格式（粗体、大字体等）
        String text = paragraph.getText();
        if (text.length() < 100 && // 标题通常较短
            (paragraph.getRuns().stream().anyMatch(run -> run.isBold()) || // 粗体
             text.matches("^第[一二三四五六七八九十\\d]+章.*") || // 中文章节
             text.matches("^\\d+\\.\\s*.*") || // 数字编号
             text.matches("^[A-Z][^.!?]*$"))) { // 全大写无标点
            return true;
        }
        
        return false;
    }
    
    /**
     * 判断内容类型
     */
    private String getContentType(String content) {
        if (content.contains("表格") || content.contains("|")) {
            return "table_content";
        } else if (content.contains("第") && content.contains("章")) {
            return "chapter_content";
        } else if (content.length() < 200) {
            return "short_content";
        } else {
            return "text_content";
        }
    }
    
    @Override
    public String[] getSupportedTypes() {
        return new String[]{"docx"};
    }
    
    @Override
    public boolean supports(String extension) {
        return Arrays.asList(getSupportedTypes()).contains(extension.toLowerCase());
    }
}