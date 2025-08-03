package com.leyue.smartcs.knowledge.parser.extractor;

import lombok.extern.slf4j.Slf4j;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.ToXMLContentHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 基于Apache Tika的表格提取器
 * 使用Tika的HTML输出解析表格结构
 * 替代Tabula，提供表格检测和结构化数据提取
 */
@Component
@Slf4j
public class TikaTableExtractor {

    @Value("${pdf.table.enabled:true}")
    private boolean tableExtractionEnabled;

    @Value("${pdf.table.json-format:true}")
    private boolean jsonFormat;

    @Value("${pdf.table.include-headers:true}")
    private boolean includeHeaders;

    // 表格检测模式
    private static final Pattern TABLE_PATTERN = Pattern.compile(
            "<table[^>]*>(.*?)</table>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
    
    private static final Pattern ROW_PATTERN = Pattern.compile(
            "<tr[^>]*>(.*?)</tr>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
    
    private static final Pattern CELL_PATTERN = Pattern.compile(
            "<t[hd][^>]*>(.*?)</t[hd]>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

    /**
     * 从文档中提取表格
     * 
     * @param inputStream 文档输入流
     * @param sourceFileName 源文件名
     * @return 提取的表格列表
     */
    public List<TableData> extractTables(InputStream inputStream, String sourceFileName) {
        if (!tableExtractionEnabled) {
            log.debug("表格提取功能已禁用");
            return new ArrayList<>();
        }

        try {
            log.debug("开始提取表格: fileName={}", sourceFileName);

            // 使用Tika解析文档为HTML格式
            String htmlContent = parseToHtml(inputStream);
            
            // 从HTML中提取表格
            List<TableData> tables = extractTablesFromHtml(htmlContent, sourceFileName);
            
            log.info("表格提取完成: fileName={}, tableCount={}", sourceFileName, tables.size());
            return tables;

        } catch (Exception e) {
            log.error("表格提取失败: fileName={}", sourceFileName, e);
            return new ArrayList<>();
        }
    }

    /**
     * 使用Tika将文档解析为HTML格式
     */
    private String parseToHtml(InputStream inputStream) throws IOException, SAXException, TikaException {
        AutoDetectParser parser = new AutoDetectParser();
        ToXMLContentHandler handler = new ToXMLContentHandler();
        Metadata metadata = new Metadata();
        ParseContext parseContext = new ParseContext();

        parser.parse(inputStream, handler, metadata, parseContext);
        return handler.toString();
    }

    /**
     * 从HTML内容中提取表格
     */
    private List<TableData> extractTablesFromHtml(String htmlContent, String sourceFileName) {
        List<TableData> tables = new ArrayList<>();
        
        Matcher tableMatcher = TABLE_PATTERN.matcher(htmlContent);
        int tableIndex = 0;
        
        while (tableMatcher.find()) {
            try {
                String tableHtml = tableMatcher.group(1);
                TableData tableData = parseTable(tableHtml, tableIndex, sourceFileName);
                
                if (tableData != null && !tableData.isEmpty()) {
                    tables.add(tableData);
                    log.debug("解析表格 {}: 行数={}, 列数={}", 
                            tableIndex, tableData.getRowCount(), tableData.getColumnCount());
                }
                
                tableIndex++;
            } catch (Exception e) {
                log.warn("解析第{}个表格失败: fileName={}", tableIndex, sourceFileName, e);
            }
        }
        
        return tables;
    }

    /**
     * 解析单个表格
     */
    private TableData parseTable(String tableHtml, int tableIndex, String sourceFileName) {
        List<List<String>> rows = new ArrayList<>();
        
        Matcher rowMatcher = ROW_PATTERN.matcher(tableHtml);
        
        while (rowMatcher.find()) {
            String rowHtml = rowMatcher.group(1);
            List<String> cells = parseCells(rowHtml);
            
            if (!cells.isEmpty()) {
                rows.add(cells);
            }
        }
        
        if (rows.isEmpty()) {
            return null;
        }
        
        return new TableData(rows, tableIndex, sourceFileName);
    }

    /**
     * 解析表格行中的单元格
     */
    private List<String> parseCells(String rowHtml) {
        List<String> cells = new ArrayList<>();
        
        Matcher cellMatcher = CELL_PATTERN.matcher(rowHtml);
        
        while (cellMatcher.find()) {
            String cellContent = cellMatcher.group(1);
            // 清理HTML标签并提取纯文本
            String cleanText = cleanHtmlContent(cellContent);
            cells.add(cleanText);
        }
        
        return cells;
    }

    /**
     * 清理HTML内容，提取纯文本
     */
    private String cleanHtmlContent(String htmlContent) {
        if (htmlContent == null) {
            return "";
        }
        
        // 移除HTML标签
        String text = htmlContent.replaceAll("<[^>]+>", "");
        
        // 解码HTML实体
        text = text.replace("&nbsp;", " ")
                  .replace("&amp;", "&")
                  .replace("&lt;", "<")
                  .replace("&gt;", ">")
                  .replace("&quot;", "\"")
                  .replace("&#39;", "'");
        
        return text.trim();
    }

    /**
     * 批量提取多个文档的表格
     */
    public List<TableData> extractTablesFromMultipleFiles(List<InputStream> inputStreams, 
                                                          List<String> fileNames) {
        List<TableData> allTables = new ArrayList<>();
        
        for (int i = 0; i < inputStreams.size() && i < fileNames.size(); i++) {
            try {
                List<TableData> fileTables = extractTables(inputStreams.get(i), fileNames.get(i));
                allTables.addAll(fileTables);
            } catch (Exception e) {
                log.error("批量提取第{}个文件的表格失败: fileName={}", i, fileNames.get(i), e);
            }
        }
        
        return allTables;
    }

    /**
     * 检查配置是否有效
     */
    public boolean isConfigurationValid() {
        log.info("表格提取配置 - 启用:{}, JSON格式:{}, 包含表头:{}", 
                tableExtractionEnabled, jsonFormat, includeHeaders);
        return true; // Tika-based extraction doesn't require external dependencies
    }

    /**
     * 获取配置信息
     */
    public String getConfigurationInfo() {
        return String.format("表格提取配置 - 启用:%s, JSON格式:%s, 包含表头:%s", 
                tableExtractionEnabled, jsonFormat, includeHeaders);
    }

    /**
     * 表格数据类
     */
    public static class TableData {
        private final List<List<String>> rows;
        private final int tableIndex;
        private final String sourceFileName;
        private List<String> headers;

        public TableData(List<List<String>> rows, int tableIndex, String sourceFileName) {
            this.rows = new ArrayList<>(rows);
            this.tableIndex = tableIndex;
            this.sourceFileName = sourceFileName;
            
            // 如果第一行看起来像表头，将其设置为headers
            if (!rows.isEmpty() && looksLikeHeader(rows.get(0))) {
                this.headers = new ArrayList<>(rows.get(0));
            }
        }

        /**
         * 判断某一行是否像表头
         */
        private boolean looksLikeHeader(List<String> row) {
            // 简单的启发式判断：表头通常包含描述性文字，不全是数字
            long numericCells = row.stream()
                    .filter(cell -> cell.matches("\\d+(\\.\\d+)?"))
                    .count();
            
            return numericCells < row.size() * 0.8; // 如果80%以上都是数字，可能不是表头
        }

        /**
         * 转换为JSON格式
         */
        public String toJson() {
            StringBuilder json = new StringBuilder();
            json.append("{\n");
            json.append("  \"tableIndex\": ").append(tableIndex).append(",\n");
            json.append("  \"sourceFile\": \"").append(sourceFileName).append("\",\n");
            
            if (headers != null && !headers.isEmpty()) {
                json.append("  \"headers\": ").append(listToJsonArray(headers)).append(",\n");
            }
            
            json.append("  \"rows\": [\n");
            for (int i = 0; i < rows.size(); i++) {
                if (i > 0) json.append(",\n");
                json.append("    ").append(listToJsonArray(rows.get(i)));
            }
            json.append("\n  ],\n");
            json.append("  \"rowCount\": ").append(rows.size()).append(",\n");
            json.append("  \"columnCount\": ").append(getColumnCount()).append("\n");
            json.append("}");
            
            return json.toString();
        }

        /**
         * 将列表转换为JSON数组
         */
        private String listToJsonArray(List<String> list) {
            return "[" + list.stream()
                    .map(s -> "\"" + s.replace("\"", "\\\"") + "\"")
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("") + "]";
        }

        /**
         * 转换为CSV格式
         */
        public String toCsv() {
            StringBuilder csv = new StringBuilder();
            
            if (headers != null && !headers.isEmpty()) {
                csv.append(String.join(",", headers)).append("\n");
            }
            
            for (List<String> row : rows) {
                csv.append(row.stream()
                        .map(cell -> "\"" + cell.replace("\"", "\"\"") + "\"")
                        .reduce((a, b) -> a + "," + b)
                        .orElse(""))
                   .append("\n");
            }
            
            return csv.toString();
        }

        // Getters
        public List<List<String>> getRows() { return new ArrayList<>(rows); }
        public int getTableIndex() { return tableIndex; }
        public String getSourceFileName() { return sourceFileName; }
        public List<String> getHeaders() { return headers != null ? new ArrayList<>(headers) : null; }
        public int getRowCount() { return rows.size(); }
        public int getColumnCount() { 
            return rows.isEmpty() ? 0 : rows.stream().mapToInt(List::size).max().orElse(0); 
        }
        public boolean isEmpty() { return rows.isEmpty(); }
        public boolean hasHeaders() { return headers != null && !headers.isEmpty(); }
    }
}