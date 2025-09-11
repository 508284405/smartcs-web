package com.leyue.smartcs.knowledge.chunking.strategy;

import com.leyue.smartcs.dto.knowledge.ChunkDTO;
import com.leyue.smartcs.knowledge.chunking.ChunkingStrategy;
import com.leyue.smartcs.knowledge.enums.DocumentTypeEnum;
import dev.langchain4j.data.document.Document;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 表格处理分块策略
 * 专门处理表格数据的结构化分割
 */
@Component
@Slf4j
public class TableProcessingStrategy implements ChunkingStrategy {
    
    private static final Pattern TABLE_PATTERN = Pattern.compile(
            "<table[^>]*>.*?</table>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
    private static final Pattern CSV_ROW_PATTERN = Pattern.compile("([^,\\r\\n\"]+|\"[^\"]*\")+");
    
    @Override
    public String getName() {
        return "TABLE_PROCESSING";
    }
    
    @Override
    public String getDescription() {
        return "表格数据处理，支持HTML表格和CSV格式的智能分块";
    }
    
    @Override
    public List<DocumentTypeEnum> getSupportedDocumentTypes() {
        return List.of(
                DocumentTypeEnum.CSV,
                DocumentTypeEnum.XLSX,
                DocumentTypeEnum.XLS,
                DocumentTypeEnum.HTML
        );
    }
    
    @Override
    public boolean isCombinable() {
        return true;
    }
    
    @Override
    public int getPriority() {
        return 50; // 高优先级，表格处理应该优先
    }
    
    @Override
    public List<ChunkDTO> chunk(List<Document> documents, DocumentTypeEnum documentType, Map<String, Object> config) {
        log.info("执行表格处理分块策略，文档数量: {}, 文档类型: {}", documents.size(), documentType);
        
        List<ChunkDTO> chunks = new ArrayList<>();
        
        // 获取配置参数
        int maxRowsPerChunk = getConfigValue(config, "maxRowsPerChunk", 50);
        boolean preserveHeaders = getConfigValue(config, "preserveHeaders", true);
        boolean extractTableContext = getConfigValue(config, "extractTableContext", true);
        
        for (int docIndex = 0; docIndex < documents.size(); docIndex++) {
            Document document = documents.get(docIndex);
            
            List<ChunkDTO> docChunks = processDocumentTables(
                    document, docIndex, documentType, maxRowsPerChunk, 
                    preserveHeaders, extractTableContext);
            chunks.addAll(docChunks);
        }
        
        log.info("表格处理分块完成，生成 {} 个分块", chunks.size());
        return chunks;
    }
    
    @Override
    public boolean validateConfig(Map<String, Object> config) {
        if (config == null) {
            return true;
        }
        
        Integer maxRowsPerChunk = getConfigValue(config, "maxRowsPerChunk", null);
        if (maxRowsPerChunk != null && (maxRowsPerChunk < 1 || maxRowsPerChunk > 1000)) {
            log.warn("每个分块的最大行数超出有效范围 [1, 1000]: {}", maxRowsPerChunk);
            return false;
        }
        
        return true;
    }
    
    /**
     * 处理文档中的表格数据
     */
    private List<ChunkDTO> processDocumentTables(Document document, int docIndex, 
                                               DocumentTypeEnum documentType,
                                               int maxRowsPerChunk, boolean preserveHeaders, 
                                               boolean extractTableContext) {
        List<ChunkDTO> chunks = new ArrayList<>();
        String content = document.text();
        
        switch (documentType) {
            case CSV -> chunks.addAll(processCSVContent(content, docIndex, maxRowsPerChunk, preserveHeaders));
            case HTML -> chunks.addAll(processHTMLTables(content, docIndex, maxRowsPerChunk,
                    preserveHeaders, extractTableContext));
            case XLSX, XLS -> chunks.addAll(processExcelContent(document, docIndex, maxRowsPerChunk, preserveHeaders));
            default -> {
                log.warn("表格处理策略不支持文档类型: {}", documentType);
                // 回退到普通文本处理
                chunks.add(createFallbackChunk(document, docIndex, 0));
            }
        }
        
        return chunks;
    }
    
    /**
     * 处理CSV内容
     */
    private List<ChunkDTO> processCSVContent(String content, int docIndex, 
                                           int maxRowsPerChunk, boolean preserveHeaders) {
        List<ChunkDTO> chunks = new ArrayList<>();
        String[] lines = content.split("\\r?\\n");
        
        if (lines.length == 0) {
            return chunks;
        }
        
        String header = preserveHeaders && lines.length > 0 ? lines[0] : null;
        int startRow = header != null ? 1 : 0;
        int chunkIndex = 0;
        
        for (int i = startRow; i < lines.length; i += maxRowsPerChunk) {
            StringBuilder chunkContent = new StringBuilder();
            
            // 添加表头（如果启用）
            if (header != null) {
                chunkContent.append(header).append("\n");
            }
            
            // 添加数据行
            int endRow = Math.min(i + maxRowsPerChunk, lines.length);
            for (int j = i; j < endRow; j++) {
                chunkContent.append(lines[j]).append("\n");
            }
            
            ChunkDTO chunk = createChunkDTO(chunkContent.toString().trim(), 
                    docIndex, chunkIndex++, DocumentTypeEnum.CSV, 
                    Map.of("rowStart", i, "rowEnd", endRow - 1, "hasHeader", header != null));
            chunks.add(chunk);
        }
        
        log.debug("CSV内容分块完成，生成 {} 个分块", chunks.size());
        return chunks;
    }
    
    /**
     * 处理HTML表格
     */
    private List<ChunkDTO> processHTMLTables(String content, int docIndex, 
                                           int maxRowsPerChunk, boolean preserveHeaders, 
                                           boolean extractTableContext) {
        List<ChunkDTO> chunks = new ArrayList<>();
        Matcher tableMatcher = TABLE_PATTERN.matcher(content);
        int tableIndex = 0;
        
        while (tableMatcher.find()) {
            String tableHtml = tableMatcher.group();
            
            // 提取表格上下文（如果启用）
            String context = "";
            if (extractTableContext) {
                context = extractTableContext(content, tableMatcher.start(), tableMatcher.end());
            }
            
            // 处理单个表格
            List<ChunkDTO> tableChunks = processHTMLTable(tableHtml, context, docIndex, 
                    tableIndex++, maxRowsPerChunk, preserveHeaders);
            chunks.addAll(tableChunks);
        }
        
        log.debug("HTML表格处理完成，处理了 {} 个表格，生成 {} 个分块", tableIndex, chunks.size());
        return chunks;
    }
    
    /**
     * 处理Excel内容（简化实现）
     */
    private List<ChunkDTO> processExcelContent(Document document, int docIndex, 
                                             int maxRowsPerChunk, boolean preserveHeaders) {
        List<ChunkDTO> chunks = new ArrayList<>();
        
        // Excel文档通常已经被解析器转换为文本格式
        // 这里假设内容是类CSV格式的文本
        String content = document.text();
        if (content.contains("\t") || content.contains(",")) {
            // 使用CSV处理逻辑
            chunks.addAll(processCSVContent(content, docIndex, maxRowsPerChunk, preserveHeaders));
        } else {
            // 回退到普通分块
            chunks.add(createFallbackChunk(document, docIndex, 0));
        }
        
        return chunks;
    }
    
    /**
     * 处理单个HTML表格
     */
    private List<ChunkDTO> processHTMLTable(String tableHtml, String context, int docIndex, 
                                          int tableIndex, int maxRowsPerChunk, boolean preserveHeaders) {
        List<ChunkDTO> chunks = new ArrayList<>();
        
        // 简化的HTML表格行提取（实际实现可能需要更复杂的HTML解析）
        String[] rows = tableHtml.split("</tr>");
        if (rows.length <= 1) {
            // 表格格式不正确，作为整体处理
            ChunkDTO chunk = createChunkDTO(tableHtml, docIndex, tableIndex, 
                    DocumentTypeEnum.HTML, Map.of("context", context, "tableIndex", tableIndex));
            chunks.add(chunk);
            return chunks;
        }
        
        // 处理表格分块逻辑（简化实现）
        int chunkIndex = 0;
        for (int i = 0; i < rows.length; i += maxRowsPerChunk) {
            StringBuilder chunkContent = new StringBuilder();
            chunkContent.append("<table>");
            
            int endRow = Math.min(i + maxRowsPerChunk, rows.length);
            for (int j = i; j < endRow; j++) {
                chunkContent.append(rows[j]);
                if (j < endRow - 1) {
                    chunkContent.append("</tr>");
                }
            }
            
            chunkContent.append("</table>");
            
            ChunkDTO chunk = createChunkDTO(chunkContent.toString(), docIndex, chunkIndex++, 
                    DocumentTypeEnum.HTML, Map.of("context", context, "tableIndex", tableIndex,
                    "rowStart", i, "rowEnd", endRow - 1));
            chunks.add(chunk);
        }
        
        return chunks;
    }
    
    /**
     * 提取表格上下文
     */
    private String extractTableContext(String fullContent, int tableStart, int tableEnd) {
        int contextLength = 200; // 前后各200字符
        
        int contextStart = Math.max(0, tableStart - contextLength);
        int contextEndBefore = Math.min(fullContent.length(), tableEnd + contextLength);
        
        String beforeContext = fullContent.substring(contextStart, tableStart).trim();
        String afterContext = fullContent.substring(tableEnd, contextEndBefore).trim();
        
        StringBuilder context = new StringBuilder();
        if (!beforeContext.isEmpty()) {
            context.append("前文: ").append(beforeContext);
        }
        if (!afterContext.isEmpty()) {
            if (context.length() > 0) context.append(" ");
            context.append("后文: ").append(afterContext);
        }
        
        return context.toString();
    }
    
    /**
     * 创建回退分块
     */
    private ChunkDTO createFallbackChunk(Document document, int docIndex, int chunkIndex) {
        return createChunkDTO(document.text(), docIndex, chunkIndex, 
                DocumentTypeEnum.UNKNOWN, Map.of("fallback", true));
    }
    
    /**
     * 创建ChunkDTO对象
     */
    private ChunkDTO createChunkDTO(String content, int docIndex, int chunkIndex, 
                                   DocumentTypeEnum documentType, Map<String, Object> additionalMetadata) {
        ChunkDTO chunk = new ChunkDTO();
        chunk.setChunkIndex(String.format("%d-%d", docIndex, chunkIndex));
        chunk.setContent(content);
        
        // 构建元数据
        StringBuilder metadataBuilder = new StringBuilder();
        metadataBuilder.append("{");
        metadataBuilder.append("\"documentType\":\"").append(documentType.name()).append("\",");
        metadataBuilder.append("\"strategy\":\"").append(getName()).append("\",");
        metadataBuilder.append("\"docIndex\":").append(docIndex).append(",");
        metadataBuilder.append("\"chunkIndex\":").append(chunkIndex);
        
        // 添加额外元数据
        additionalMetadata.forEach((key, value) -> {
            metadataBuilder.append(",");
            metadataBuilder.append("\"").append(key).append("\":\"").append(value).append("\"");
        });
        
        metadataBuilder.append("}");
        chunk.setMetadata(metadataBuilder.toString());
        
        return chunk;
    }
    
    @SuppressWarnings("unchecked")
    private <T> T getConfigValue(Map<String, Object> config, String key, T defaultValue) {
        if (config == null || !config.containsKey(key)) {
            return defaultValue;
        }
        
        try {
            return (T) config.get(key);
        } catch (ClassCastException e) {
            log.warn("配置参数 {} 类型转换失败，使用默认值: {}", key, defaultValue);
            return defaultValue;
        }
    }
}