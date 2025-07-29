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
    
    @PostConstruct
    public void initializeParsers() {
        // 注册所有解析器
        for (DocumentParser parser : documentParsers) {
            for (String type : parser.getSupportedTypes()) {
                parserMap.put(type.toLowerCase(), parser);
                log.info("注册文档解析器: {} -> {}", type, parser.getClass().getSimpleName());
            }
        }
        
        log.info("文档解析器工厂初始化完成，支持的文档类型: {}", parserMap.keySet());
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
        
        DocumentParser parser = parserMap.get(normalizedExtension);
        if (parser == null) {
            log.warn("未找到支持扩展名 {} 的解析器，使用默认解析器", extension);
            return getDefaultParser();
        }
        
        return parser;
    }
    
    /**
     * 根据文档类型获取解析器
     */
    public DocumentParser getParserByType(DocumentTypeEnum documentType) {
        if (documentType == null || documentType == DocumentTypeEnum.UNKNOWN) {
            return getDefaultParser();
        }
        
        DocumentParser parser = null;
        
        // 根据文档类型选择最佳解析器
        switch (documentType) {
            case PDF:
                parser = getParserByClass(PdfDocumentParser.class);
                break;
                
            case XLSX:
            case XLS:
                parser = getParserByClass(ExcelDocumentParser.class);
                break;
                
            case DOCX:
                parser = getParserByClass(DocxDocumentParser.class);
                break;
                
            case MARKDOWN:
            case MDX:
                parser = getParserByClass(MarkdownDocumentParser.class);
                break;
                
            case TXT:
            case HTML:
            case CSV:
            case VTT:
            case PROPERTIES:
                parser = getParserByClass(UniversalDocumentParser.class);
                break;
                
            default:
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
     * 获取默认解析器（通用解析器）
     */
    private DocumentParser getDefaultParser() {
        if (documentParsers == null || documentParsers.isEmpty()) {
            log.error("没有可用的文档解析器，请检查Spring容器配置");
            throw new IllegalStateException("没有可用的文档解析器");
        }
        
        // 直接查找通用解析器，避免递归调用
        DocumentParser universalParser = documentParsers.stream()
                .filter(UniversalDocumentParser.class::isInstance)
                .findFirst()
                .orElse(null);
        
        if (universalParser == null) {
            // 如果找不到通用解析器，返回第一个可用的解析器
            log.warn("未找到通用解析器(UniversalDocumentParser)，使用第一个可用的解析器: {}",
                    documentParsers.get(0).getClass().getSimpleName());
            return documentParsers.get(0);
        }
        
        return universalParser;
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
        
        return parserMap.containsKey(normalizedExtension);
    }
    
    /**
     * 获取所有支持的文档类型
     */
    public String[] getSupportedTypes() {
        return parserMap.keySet().toArray(new String[0]);
    }
    
    /**
     * 获取文档类型的最佳分块策略建议
     */
    public ChunkingStrategy getChunkingStrategy(DocumentTypeEnum documentType) {
        if (documentType == null) {
            return ChunkingStrategy.DEFAULT;
        }
        return switch (documentType) {
            case PDF -> ChunkingStrategy.PAGE_BASED;
            case XLSX, XLS, CSV -> ChunkingStrategy.ROW_BASED;
            case DOCX -> ChunkingStrategy.SECTION_BASED;
            case MARKDOWN, MDX -> ChunkingStrategy.SECTION_BASED;
            case HTML -> ChunkingStrategy.TAG_BASED;
            case VTT -> ChunkingStrategy.TIME_BASED;
            case PROPERTIES -> ChunkingStrategy.GROUP_BASED;
            default -> ChunkingStrategy.DEFAULT;
        };
    }
    
    /**
     * 分块策略枚举
     */
    @Getter
    public enum ChunkingStrategy {
        DEFAULT("默认分块", "基于文本长度的递归分块"),
        PAGE_BASED("页面分块", "按页面进行分块，适合PDF"),
        SECTION_BASED("章节分块", "按标题层次进行分块，适合Markdown"),
        ROW_BASED("行分块", "按行进行分块，适合表格类文档"),
        TAG_BASED("标签分块", "按HTML标签进行分块"),
        TIME_BASED("时间分块", "按时间戳进行分块，适合字幕文件"),
        GROUP_BASED("分组分块", "按逻辑分组进行分块，适合配置文件");
        
        private final String name;
        private final String description;
        
        ChunkingStrategy(String name, String description) {
            this.name = name;
            this.description = description;
        }
    }
}