package com.leyue.smartcs.knowledge.parser.impl;

import com.leyue.smartcs.knowledge.parser.DocumentParser;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

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
    public String parseContent(String fileUrl) throws Exception {
        log.info("开始解析PDF文件: {}", fileUrl);
        
        try (InputStream inputStream = new URL(fileUrl).openStream();
             PDDocument document = PDDocument.load(inputStream)) {
            
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            
            log.info("PDF解析完成，提取文本长度: {}", text.length());
            return text;
            
        } catch (IOException e) {
            log.error("PDF解析失败: {}", fileUrl, e);
            throw new Exception("PDF解析失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public boolean supports(String fileType) {
        return "pdf".equalsIgnoreCase(fileType);
    }
} 