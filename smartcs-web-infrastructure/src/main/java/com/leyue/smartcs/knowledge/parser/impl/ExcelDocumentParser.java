package com.leyue.smartcs.knowledge.parser.impl;

import com.leyue.smartcs.knowledge.parser.DocumentParser;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Excel文档解析器
 * 支持XLSX和XLS格式，保持表头结构和工作表信息
 */
@Slf4j
@Component
public class ExcelDocumentParser implements DocumentParser {
    
    @Override
    public List<Document> parse(Resource resource, String fileName) throws IOException {
        List<Document> documents = new ArrayList<>();
        
        try (InputStream inputStream = resource.getInputStream()) {
            Workbook workbook = createWorkbook(inputStream, fileName);
            
            // 遍历所有工作表
            for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
                Sheet sheet = workbook.getSheetAt(sheetIndex);
                String sheetName = sheet.getSheetName();
                
                log.info("解析Excel工作表: {} - {}", fileName, sheetName);
                
                // 解析工作表内容
                parseSheet(sheet, sheetName, fileName, documents);
            }
            
            workbook.close();
            log.info("Excel解析完成，文件: {}，生成文档数: {}", fileName, documents.size());
            
        } catch (Exception e) {
            log.error("Excel文档解析失败: {}", fileName, e);
            throw new IOException("Excel文档解析失败: " + e.getMessage(), e);
        }
        
        return documents;
    }
    
    /**
     * 创建工作簿对象
     */
    private Workbook createWorkbook(InputStream inputStream, String fileName) throws IOException {
        if (fileName.toLowerCase().endsWith(".xlsx")) {
            return new XSSFWorkbook(inputStream);
        } else if (fileName.toLowerCase().endsWith(".xls")) {
            return new HSSFWorkbook(inputStream);
        } else {
            throw new IllegalArgumentException("不支持的Excel文件格式: " + fileName);
        }
    }
    
    /**
     * 解析工作表
     */
    private void parseSheet(Sheet sheet, String sheetName, String fileName, List<Document> documents) {
        if (sheet.getPhysicalNumberOfRows() == 0) {
            return;
        }
        
        // 1. 提取表头信息
        Row headerRow = sheet.getRow(sheet.getFirstRowNum());
        List<String> headers = extractHeaders(headerRow);
        
        // 2. 创建表头文档
        if (!headers.isEmpty()) {
            String headerContent = "表头：" + String.join(" | ", headers);
            Metadata headerMetadata = Metadata.from("type", "excel_header")
                    .add("fileName", fileName)
                    .add("sheetName", sheetName)
                    .add("columnCount", String.valueOf(headers.size()));
            
            documents.add(Document.from(headerContent, headerMetadata));
        }
        
        // 3. 按行提取数据
        StringBuilder fullContent = new StringBuilder();
        fullContent.append("工作表：").append(sheetName).append("\n");
        if (!headers.isEmpty()) {
            fullContent.append("表头：").append(String.join(" | ", headers)).append("\n");
        }
        fullContent.append("数据：\n");
        
        int dataRowCount = 0;
        for (int rowIndex = sheet.getFirstRowNum() + (headers.isEmpty() ? 0 : 1); 
             rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            
            Row row = sheet.getRow(rowIndex);
            if (row == null) continue;
            
            List<String> rowData = extractRowData(row, headers.size());
            if (!rowData.isEmpty() && !isEmptyRow(rowData)) {
                String rowContent = String.join(" | ", rowData);
                fullContent.append(rowContent).append("\n");
                dataRowCount++;
                
                // 每100行创建一个分块
                if (dataRowCount % 100 == 0) {
                    createDataChunk(fullContent.toString(), fileName, sheetName, 
                                   dataRowCount - 99, dataRowCount, headers, documents);
                    
                    // 重置内容，保留表头
                    fullContent = new StringBuilder();
                    fullContent.append("工作表：").append(sheetName).append("\n");
                    if (!headers.isEmpty()) {
                        fullContent.append("表头：").append(String.join(" | ", headers)).append("\n");
                    }
                    fullContent.append("数据：\n");
                }
            }
        }
        
        // 处理剩余数据
        if (dataRowCount % 100 != 0) {
            int startRow = (dataRowCount / 100) * 100 + 1;
            createDataChunk(fullContent.toString(), fileName, sheetName, 
                           startRow, dataRowCount, headers, documents);
        }
        
        // 4. 创建工作表摘要文档
        String summaryContent = String.format("工作表摘要：\n名称：%s\n总行数：%d\n列数：%d\n表头：%s",
                sheetName, dataRowCount, headers.size(), 
                headers.isEmpty() ? "无" : String.join(", ", headers));
        
        Metadata summaryMetadata = Metadata.from("type", "excel_summary")
                .add("fileName", fileName)
                .add("sheetName", sheetName)
                .add("totalRows", String.valueOf(dataRowCount))
                .add("columnCount", String.valueOf(headers.size()));
        
        documents.add(Document.from(summaryContent, summaryMetadata));
    }
    
    /**
     * 提取表头
     */
    private List<String> extractHeaders(Row headerRow) {
        List<String> headers = new ArrayList<>();
        if (headerRow == null) return headers;
        
        for (int cellIndex = 0; cellIndex < headerRow.getLastCellNum(); cellIndex++) {
            Cell cell = headerRow.getCell(cellIndex);
            String headerValue = getCellValueAsString(cell);
            headers.add(headerValue.isEmpty() ? "列" + (cellIndex + 1) : headerValue);
        }
        
        return headers;
    }
    
    /**
     * 提取行数据
     */
    private List<String> extractRowData(Row row, int expectedColumnCount) {
        List<String> rowData = new ArrayList<>();
        
        int maxCells = Math.max(expectedColumnCount, row.getLastCellNum());
        for (int cellIndex = 0; cellIndex < maxCells; cellIndex++) {
            Cell cell = row.getCell(cellIndex);
            rowData.add(getCellValueAsString(cell));
        }
        
        return rowData;
    }
    
    /**
     * 获取单元格值作为字符串
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    // 避免科学计数法
                    double numericValue = cell.getNumericCellValue();
                    if (numericValue == Math.floor(numericValue)) {
                        return String.valueOf((long) numericValue);
                    } else {
                        return String.valueOf(numericValue);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return String.valueOf(cell.getNumericCellValue());
                } catch (Exception e) {
                    return cell.getStringCellValue();
                }
            default:
                return "";
        }
    }
    
    /**
     * 检查是否为空行
     */
    private boolean isEmptyRow(List<String> rowData) {
        return rowData.stream().allMatch(String::isEmpty);
    }
    
    /**
     * 创建数据分块
     */
    private void createDataChunk(String content, String fileName, String sheetName, 
                                int startRow, int endRow, List<String> headers, 
                                List<Document> documents) {
        
        Metadata dataMetadata = Metadata.from("type", "excel_data")
                .add("fileName", fileName)
                .add("sheetName", sheetName)
                .add("startRow", String.valueOf(startRow))
                .add("endRow", String.valueOf(endRow))
                .add("headers", String.join(",", headers));
        
        documents.add(Document.from(content, dataMetadata));
    }
    
    @Override
    public String[] getSupportedTypes() {
        return new String[]{"xlsx", "xls"};
    }
    
    @Override
    public boolean supports(String extension) {
        return Arrays.asList(getSupportedTypes()).contains(extension.toLowerCase());
    }
}