package com.leyue.smartcs.knowledge.parser.impl;

import com.leyue.smartcs.domain.knowledge.Document;
import com.leyue.smartcs.knowledge.parser.DocumentParser;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * PDF文档解析器
 * 使用PDFBox解析PDF文件内容
 */
@Component
@Slf4j
public class PdfDocumentParser implements DocumentParser {
    
    /**
     * 支持的文件类型
     */
    private static final String SUPPORTED_TYPE = "pdf";
    
    @Override
    public String parseContent(Document document, File localFile) throws Exception {
        log.info("解析PDF文档: {}, 文件路径: {}", document.getTitle(), localFile.getAbsolutePath());
        
        try (PDDocument pdfDocument = PDDocument.load(localFile)) {
            PDFTextStripper textStripper = new PDFTextStripper();
            
            // 设置按顺序提取文本
            textStripper.setSortByPosition(true);
            
            // 提取所有页面的文本
            String text = textStripper.getText(pdfDocument);
            
            log.info("PDF解析完成，文档页数: {}, 提取的文本长度: {}", pdfDocument.getNumberOfPages(), text.length());
            return text;
        } catch (Exception e) {
            log.error("PDF解析失败: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    public String getSupportedFileType() {
        return SUPPORTED_TYPE;
    }
} 