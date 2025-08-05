package com.leyue.smartcs.knowledge.parser.factory;

import com.leyue.smartcs.knowledge.enums.DocumentTypeEnum;
import com.leyue.smartcs.knowledge.parser.DocumentParser;
import com.leyue.smartcs.knowledge.parser.impl.*;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 文档解析器工厂
 * 根据文档类型选择合适的解析器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentParserFactory {
    
    private final List<DocumentParser> documentParsers;
    private final Map<String, DocumentParser> parserMap = new HashMap<>();
    private LangChain4jDocumentParserAdapter langChain4jAdapter;
    
    @PostConstruct
    public void initializeParsers() {
        // 初始化LangChain4j适配器
        langChain4jAdapter = new LangChain4jDocumentParserAdapter();
        
        // 注册所有解析器
        for (DocumentParser parser : documentParsers) {
            for (String type : parser.getSupportedTypes()) {
                parserMap.put(type.toLowerCase(), parser);
                log.info("注册文档解析器: {} -> {}", type, parser.getClass().getSimpleName());
            }
        }
        
        log.info("文档解析器工厂初始化完成，支持的文档类型: {}", parserMap.keySet());
        log.info("LangChain4j适配器已初始化，支持类型: {}", (Object) langChain4jAdapter.getSupportedTypes());
    }
    
    /**
     * 根据文件名获取解析器
     */
    public DocumentParser getParser(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            log.warn("文件名为空，使用默认解析器");
            return getDefaultParser();
        }
        
        DocumentTypeEnum documentType = DocumentTypeEnum.fromFileName(fileName);
        
        // 优先使用LangChain4j适配器
        if (langChain4jAdapter != null && langChain4jAdapter.supports(documentType)) {
            log.debug("使用LangChain4j适配器解析文件: {}, 类型: {}", fileName, documentType);
            return langChain4jAdapter;
        }
        
        // 回退到传统解析器
        DocumentParser parser = getParserByType(documentType);
        
        if (parser == null) {
            log.warn("无法为文件 {} 找到合适的解析器，使用默认解析器", fileName);
            return getDefaultParser();
        }
        
        return parser;
    }
    
    /**
     * 根据文件扩展名获取解析器
     */
    public DocumentParser getParserByExtension(String extension) {
        if (extension == null || extension.trim().isEmpty()) {
            return getDefaultParser();
        }
        
        String normalizedExtension = extension.toLowerCase().trim();
        if (normalizedExtension.startsWith(".")) {
            normalizedExtension = normalizedExtension.substring(1);
        }
        
        // 优先使用LangChain4j适配器
        if (langChain4jAdapter != null && langChain4jAdapter.supports(normalizedExtension)) {
            log.debug("使用LangChain4j适配器解析扩展名: {}", extension);
            return langChain4jAdapter;
        }
        
        // 回退到传统解析器
        DocumentParser parser = parserMap.get(normalizedExtension);
        if (parser == null) {
            log.warn("未找到支持扩展名 {} 的解析器，使用默认解析器", extension);
            return getDefaultParser();
        }
        
        return parser;
    }
    
    /**
     * 根据文档类型获取解析器（简化后的逻辑）
     */
    public DocumentParser getParserByType(DocumentTypeEnum documentType) {
        if (documentType == null || documentType == DocumentTypeEnum.UNKNOWN) {
            return getDefaultParser();
        }
        
        DocumentParser parser = null;
        
        // 只为有特殊价值的文档类型保留专用解析器
        switch (documentType) {
            case PDF:
                // PDF多模态解析器 - 核心优势，必须保留
                parser = getParserByClass(PdfDocumentParser.class);
                break;
                
            case XLSX:
            case XLS:
                // Excel专业处理器 - 表格数据处理优势
                parser = getParserByClass(ExcelDocumentParser.class);
                break;
                
            case DOCX:
                // Word文档解析器 - 可选保留（需要结构化处理时）
                parser = getParserByClass(DocxDocumentParser.class);
                break;
                
            default:
                // 其他所有格式都由LangChain4j适配器（Apache Tika）处理
                parser = null;
                break;
        }
        
        // 如果找不到特定解析器，使用默认解析器
        return parser != null ? parser : getDefaultParser();
    }
    
    /**
     * 根据解析器类获取实例
     */
    private DocumentParser getParserByClass(Class<? extends DocumentParser> parserClass) {
        return documentParsers.stream()
                .filter(parserClass::isInstance)
                .findFirst()
                .orElse(null);
    }
    
    /**
     * 获取默认解析器（优先使用LangChain4j适配器）
     */
    private DocumentParser getDefaultParser() {
        // 默认使用LangChain4j适配器，基于Apache Tika的通用解析能力
        if (langChain4jAdapter != null) {
            log.debug("使用LangChain4j适配器作为默认解析器");
            return langChain4jAdapter;
        }
        
        // 回退：如果LangChain4j适配器不可用，使用第一个可用的解析器
        if (documentParsers == null || documentParsers.isEmpty()) {
            log.error("没有可用的文档解析器，请检查Spring容器配置");
            throw new IllegalStateException("没有可用的文档解析器");
        }
        
        log.warn("LangChain4j适配器不可用，使用第一个可用的解析器: {}",
                documentParsers.get(0).getClass().getSimpleName());
        return documentParsers.get(0);
    }
    
    /**
     * 获取LangChain4j适配器
     */
    public LangChain4jDocumentParserAdapter getLangChain4jAdapter() {
        return langChain4jAdapter;
    }

    /**
     * 检查是否支持指定的文档类型
     */
    public boolean isSupported(String extension) {
        if (extension == null || extension.trim().isEmpty()) {
            return false;
        }
        
        String normalizedExtension = extension.toLowerCase().trim();
        if (normalizedExtension.startsWith(".")) {
            normalizedExtension = normalizedExtension.substring(1);
        }
        
        // 检查LangChain4j适配器是否支持
        if (langChain4jAdapter != null && langChain4jAdapter.supports(normalizedExtension)) {
            return true;
        }
        
        // 检查传统解析器是否支持
        return parserMap.containsKey(normalizedExtension);
    }
    
    /**
     * 获取所有支持的文档类型
     */
    public String[] getSupportedTypes() {
        Set<String> keySet = parserMap.keySet();
        return keySet.toArray(new String[keySet.size()]);
    }
    
    /**
     * 获取文档类型的最佳分块策略建议（简化后的版本）
     * 注意：大部分分块策略现在由LangChain4jChunkingStrategy处理
     */
    public ChunkingStrategy getChunkingStrategy(DocumentTypeEnum documentType) {
        if (documentType == null) {
            return ChunkingStrategy.DEFAULT;
        }
        return switch (documentType) {
            case PDF -> ChunkingStrategy.PAGE_BASED;  // PDF多模态解析器特有
            case XLSX, XLS -> ChunkingStrategy.ROW_BASED;  // Excel专业处理特有
            case DOCX -> ChunkingStrategy.SECTION_BASED;  // Word结构化处理
            default -> ChunkingStrategy.DEFAULT;  // 其他都使用LangChain4j默认策略
        };
    }
    
    /**
     * 分块策略枚举（简化版本，只保留有专用解析器的策略）
     * 注意：大部分分块现在由LangChain4jChunkingStrategy处理
     */
    @Getter
    public enum ChunkingStrategy {
        DEFAULT("默认分块", "使用LangChain4j递归分块策略"),
        PAGE_BASED("页面分块", "PDF多模态解析器专用的页面分块"),
        SECTION_BASED("章节分块", "Word文档结构化分块"),
        ROW_BASED("行分块", "Excel表格数据专用分块");
        
        private final String name;
        private final String description;
        
        ChunkingStrategy(String name, String description) {
            this.name = name;
            this.description = description;
        }
    }
}