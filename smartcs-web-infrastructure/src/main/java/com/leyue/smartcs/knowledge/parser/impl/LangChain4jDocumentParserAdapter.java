package com.leyue.smartcs.knowledge.parser.impl;

import com.leyue.smartcs.knowledge.enums.DocumentTypeEnum;
import com.leyue.smartcs.knowledge.parser.DocumentParser;
import com.leyue.smartcs.knowledge.parser.model.ParserExtendParam;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser;
import dev.langchain4j.data.document.parser.apache.poi.ApachePoiDocumentParser;
import dev.langchain4j.data.document.parser.apache.tika.ApacheTikaDocumentParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * LangChain4j文档解析器适配器
 * 统一使用LangChain4j内置解析器，减少自定义实现
 */
@Slf4j
@Component
public class LangChain4jDocumentParserAdapter implements DocumentParser {

    private final ApachePdfBoxDocumentParser pdfParser;
    private final ApachePoiDocumentParser poiParser;
    private final ApacheTikaDocumentParser tikaParser;

    public LangChain4jDocumentParserAdapter() {
        this.pdfParser = new ApachePdfBoxDocumentParser();
        this.poiParser = new ApachePoiDocumentParser();
        this.tikaParser = new ApacheTikaDocumentParser();
        log.info("LangChain4j文档解析器适配器初始化完成");
    }

    @Override
    public List<Document> parse(Resource resource, String fileName, ParserExtendParam parserExtendParam) throws IOException {
        if (resource == null || fileName == null) {
            throw new IllegalArgumentException("资源和文件名不能为空");
        }

        DocumentTypeEnum documentType = DocumentTypeEnum.fromFileName(fileName);
        List<Document> documents = new ArrayList<>();

        try (InputStream inputStream = resource.getInputStream()) {
            Document parsedDocument = parseByType(inputStream, documentType, fileName);
            if (parsedDocument != null) {
                // 添加文件相关元数据
                Metadata enhancedMetadata = enhanceMetadata(parsedDocument.metadata(), fileName, documentType);
                Document enhancedDocument = Document.from(parsedDocument.text(), enhancedMetadata);
                documents.add(enhancedDocument);
                
                log.info("使用LangChain4j解析器解析文档完成: {}, 类型: {}, 内容长度: {}", 
                        fileName, documentType, parsedDocument.text().length());
            } else {
                log.warn("解析文档失败，返回空结果: {}", fileName);
            }
        } catch (Exception e) {
            log.error("解析文档时发生错误: {}, 错误: {}", fileName, e.getMessage(), e);
            throw new IOException("解析文档失败: " + e.getMessage(), e);
        }

        return documents;
    }

    /**
     * 根据文档类型选择合适的解析器
     */
    private Document parseByType(InputStream inputStream, DocumentTypeEnum documentType, String fileName) throws IOException {
        return switch (documentType) {
            case PDF -> {
                log.debug("使用ApachePdfBoxDocumentParser解析PDF文档: {}", fileName);
                yield pdfParser.parse(inputStream);
            }
            case DOCX, DOC -> {
                log.debug("使用ApachePoiDocumentParser解析Word文档: {}", fileName);
                yield poiParser.parse(inputStream);
            }
            case XLSX, XLS -> {
                log.debug("使用ApachePoiDocumentParser解析Excel文档: {}", fileName);
                yield poiParser.parse(inputStream);
            }
            case PPTX, PPT -> {
                log.debug("使用ApachePoiDocumentParser解析PowerPoint文档: {}", fileName);
                yield poiParser.parse(inputStream);
            }
            case TXT, HTML, HTM, CSV, MARKDOWN, MDX, RTF, XML -> {
                log.debug("使用ApacheTikaDocumentParser解析文本类文档: {}", fileName);
                yield tikaParser.parse(inputStream);
            }
            default -> {
                log.debug("使用ApacheTikaDocumentParser作为默认解析器: {}", fileName);
                yield tikaParser.parse(inputStream);
            }
        };
    }

    /**
     * 增强文档元数据，添加文件和类型信息
     */
    private Metadata enhanceMetadata(Metadata originalMetadata, String fileName, DocumentTypeEnum documentType) {
        // 创建新的元数据，从原有元数据开始
        Metadata metadata;
        if (originalMetadata != null) {
            // 如果有原有元数据，先复制所有键值对
            metadata = Metadata.from(originalMetadata.toMap());
        } else {
            // 如果没有原有元数据，创建一个空的
            metadata = Metadata.from("type", "langchain4j_document");
        }
        
        // 添加文件相关元数据
        metadata.put("fileName", fileName);
        metadata.put("documentType", documentType.name());
        metadata.put("fileExtension", getFileExtension(fileName));
        metadata.put("parser", "LangChain4j");
        metadata.put("parseTime", String.valueOf(System.currentTimeMillis()));
        
        return metadata;
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        int lastDotIndex = fileName.lastIndexOf('.');
        return lastDotIndex >= 0 ? fileName.substring(lastDotIndex + 1).toLowerCase() : "";
    }

    @Override
    public String[] getSupportedTypes() {
        return new String[]{
                "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx",
                "txt", "html", "htm", "csv", "md", "markdown", "mdx",
                "rtf", "xml", "json", "properties", "vtt"
        };
    }

    /**
     * 检查是否支持指定的文档类型
     */
    public boolean supports(DocumentTypeEnum documentType) {
        return documentType != null && documentType != DocumentTypeEnum.UNKNOWN;
    }

    /**
     * 检查是否支持指定的文件扩展名
     */
    public boolean supports(String fileExtension) {
        String[] supportedTypes = getSupportedTypes();
        for (String type : supportedTypes) {
            if (type.equalsIgnoreCase(fileExtension)) {
                return true;
            }
        }
        return false;
    }
}