package com.leyue.smartcs.knowledge.parser.impl;

import com.leyue.smartcs.knowledge.parser.DocumentParser;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Word文档解析器（支持.docx格式）
 */
@Component
@Slf4j
public class WordDocumentParser implements DocumentParser {

    @Override
    public List<Document> parse(Resource resource, String fileName) throws IOException {
        log.info("开始解析Word文件: {}", fileName);
        
        List<Document> documents = new ArrayList<>();
        
        try (InputStream inputStream = resource.getInputStream();
             XWPFDocument document = new XWPFDocument(inputStream)) {
            
            // 提取文档属性
            documents.addAll(extractDocumentProperties(document, fileName));
            
            // 提取段落内容
            documents.addAll(extractParagraphs(document, fileName));
            
            // 提取表格内容
            documents.addAll(extractTables(document, fileName));
            
            // 提取页眉页脚
            documents.addAll(extractHeadersFooters(document, fileName));
            
            // 创建完整文档
            String fullText = extractFullText(document);
            Metadata fullMetadata = Metadata.from("type", "full_document")
                    .put("fileName", fileName)
                    .put("totalParagraphs", String.valueOf(document.getParagraphs().size()))
                    .put("totalTables", String.valueOf(document.getTables().size()))
                    .put("textLength", String.valueOf(fullText.length()))
                    .put("hasImages", String.valueOf(hasImages(document)));
            
            documents.add(Document.from(fullText, fullMetadata));
            
            log.info("Word解析完成，文件: {}，生成文档数: {}", fileName, documents.size());
            
        } catch (IOException e) {
            log.error("Word解析失败: {}", fileName, e);
            throw new IOException("Word文件解析失败: " + e.getMessage(), e);
        }
        
        return documents;
    }

    @Override
    public String[] getSupportedTypes() {
        return new String[]{"docx"};
    }

    @Override
    public boolean supports(String extension) {
        return extension != null && "docx".equalsIgnoreCase(extension);
    }

    /**
     * 提取文档属性
     */
    private List<Document> extractDocumentProperties(XWPFDocument document, String fileName) {
        List<Document> properties = new ArrayList<>();
        
        try {
            // 获取核心属性
            String title = document.getProperties().getCoreProperties().getTitle();
            String creator = document.getProperties().getCoreProperties().getCreator();
            String description = document.getProperties().getCoreProperties().getDescription();
            String subject = document.getProperties().getCoreProperties().getSubject();
            
            if (title != null && !title.trim().isEmpty()) {
                Metadata titleMetadata = Metadata.from("type", "document_property")
                        .put("fileName", fileName)
                        .put("propertyType", "title");
                properties.add(Document.from(title.trim(), titleMetadata));
            }
            
            if (creator != null && !creator.trim().isEmpty()) {
                Metadata creatorMetadata = Metadata.from("type", "document_property")
                        .put("fileName", fileName)
                        .put("propertyType", "creator");
                properties.add(Document.from("作者: " + creator.trim(), creatorMetadata));
            }
            
            if (description != null && !description.trim().isEmpty()) {
                Metadata descMetadata = Metadata.from("type", "document_property")
                        .put("fileName", fileName)
                        .put("propertyType", "description");
                properties.add(Document.from("描述: " + description.trim(), descMetadata));
            }
            
            if (subject != null && !subject.trim().isEmpty()) {
                Metadata subjectMetadata = Metadata.from("type", "document_property")
                        .put("fileName", fileName)
                        .put("propertyType", "subject");
                properties.add(Document.from("主题: " + subject.trim(), subjectMetadata));
            }
            
        } catch (Exception e) {
            log.warn("提取Word文档属性时出错: {}", e.getMessage());
        }
        
        return properties;
    }

    /**
     * 提取段落内容
     */
    private List<Document> extractParagraphs(XWPFDocument document, String fileName) {
        List<Document> paragraphs = new ArrayList<>();
        List<XWPFParagraph> docParagraphs = document.getParagraphs();
        
        for (int i = 0; i < docParagraphs.size(); i++) {
            XWPFParagraph paragraph = docParagraphs.get(i);
            String text = paragraph.getText();
            
            if (text != null && !text.trim().isEmpty()) {
                // 判断段落类型（标题或正文）
                String style = paragraph.getStyle();
                boolean isHeading = style != null && style.toLowerCase().contains("heading");
                
                Metadata paragraphMetadata = Metadata.from("type", isHeading ? "heading" : "paragraph")
                        .put("fileName", fileName)
                        .put("paragraphIndex", String.valueOf(i))
                        .put("style", style != null ? style : "")
                        .put("isHeading", String.valueOf(isHeading));
                
                paragraphs.add(Document.from(text.trim(), paragraphMetadata));
            }
        }
        
        return paragraphs;
    }

    /**
     * 提取表格内容
     */
    private List<Document> extractTables(XWPFDocument document, String fileName) {
        List<Document> tables = new ArrayList<>();
        List<XWPFTable> docTables = document.getTables();
        
        for (int tableIndex = 0; tableIndex < docTables.size(); tableIndex++) {
            XWPFTable table = docTables.get(tableIndex);
            StringBuilder tableText = new StringBuilder();
            
            List<XWPFTableRow> rows = table.getRows();
            for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
                XWPFTableRow row = rows.get(rowIndex);
                List<XWPFTableCell> cells = row.getTableCells();
                
                StringBuilder rowText = new StringBuilder();
                for (XWPFTableCell cell : cells) {
                    String cellText = cell.getText();
                    if (cellText != null && !cellText.trim().isEmpty()) {
                        if (rowText.length() > 0) {
                            rowText.append(" | ");
                        }
                        rowText.append(cellText.trim());
                    }
                }
                
                if (rowText.length() > 0) {
                    if (rowIndex == 0) {
                        tableText.append("表头: ");
                    } else {
                        tableText.append("第").append(rowIndex).append("行: ");
                    }
                    tableText.append(rowText).append("\n");
                }
            }
            
            if (tableText.length() > 0) {
                Metadata tableMetadata = Metadata.from("type", "table")
                        .put("fileName", fileName)
                        .put("tableIndex", String.valueOf(tableIndex))
                        .put("rowCount", String.valueOf(rows.size()))
                        .put("columnCount", String.valueOf(rows.isEmpty() ? 0 : rows.get(0).getTableCells().size()));
                
                tables.add(Document.from(tableText.toString().trim(), tableMetadata));
            }
        }
        
        return tables;
    }

    /**
     * 提取页眉页脚
     */
    private List<Document> extractHeadersFooters(XWPFDocument document, String fileName) {
        List<Document> headersFooters = new ArrayList<>();
        
        // 提取页眉
        List<XWPFHeader> headers = document.getHeaderList();
        for (int i = 0; i < headers.size(); i++) {
            XWPFHeader header = headers.get(i);
            String headerText = header.getText();
            if (headerText != null && !headerText.trim().isEmpty()) {
                Metadata headerMetadata = Metadata.from("type", "header")
                        .put("fileName", fileName)
                        .put("headerIndex", String.valueOf(i));
                headersFooters.add(Document.from("页眉: " + headerText.trim(), headerMetadata));
            }
        }
        
        // 提取页脚
        List<XWPFFooter> footers = document.getFooterList();
        for (int i = 0; i < footers.size(); i++) {
            XWPFFooter footer = footers.get(i);
            String footerText = footer.getText();
            if (footerText != null && !footerText.trim().isEmpty()) {
                Metadata footerMetadata = Metadata.from("type", "footer")
                        .put("fileName", fileName)
                        .put("footerIndex", String.valueOf(i));
                headersFooters.add(Document.from("页脚: " + footerText.trim(), footerMetadata));
            }
        }
        
        return headersFooters;
    }

    /**
     * 提取完整文本
     */
    private String extractFullText(XWPFDocument document) {
        try (XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            return extractor.getText();
        } catch (IOException e) {
            log.warn("提取Word完整文本时出错: {}", e.getMessage());
            return "";
        }
    }

    /**
     * 检查文档是否包含图片
     */
    private boolean hasImages(XWPFDocument document) {
        try {
            List<XWPFPictureData> pictures = document.getAllPictures();
            return pictures != null && !pictures.isEmpty();
        } catch (Exception e) {
            log.warn("检查Word文档图片时出错: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 保持向后兼容的方法（已弃用）
     * @deprecated 使用 parse(Resource resource, String fileName) 方法替代
     */
    @Deprecated
    public String parseContent(String fileUrl) throws Exception {
        log.warn("使用了已弃用的 parseContent 方法，建议使用 parse 方法");
        
        try (InputStream inputStream = new URL(fileUrl).openStream()) {
            String text;
            
            // 目前只支持.docx格式
            if (fileUrl.toLowerCase().endsWith(".docx")) {
                text = parseDocxContent(inputStream);
            } else {
                throw new Exception("目前只支持.docx格式的Word文件");
            }
            
            log.info("Word解析完成，提取文本长度: {}", text.length());
            return text;
            
        } catch (Exception e) {
            log.error("Word解析失败: {}", fileUrl, e);
            throw new Exception("Word解析失败: " + e.getMessage(), e);
        }
    }

    /**
     * 解析.docx格式文件（已弃用）
     * @deprecated 内部方法，由新的parse方法替代
     */
    @Deprecated
    private String parseDocxContent(InputStream inputStream) throws Exception {
        try (XWPFDocument document = new XWPFDocument(inputStream);
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            return extractor.getText();
        }
    }
} 