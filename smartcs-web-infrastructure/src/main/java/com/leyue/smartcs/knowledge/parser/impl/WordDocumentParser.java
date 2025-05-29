package com.leyue.smartcs.knowledge.parser.impl;

import com.leyue.smartcs.knowledge.parser.DocumentParser;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.net.URL;

/**
 * Word文档解析器（支持.docx格式）
 */
@Component
@Slf4j
public class WordDocumentParser implements DocumentParser {

    @Override
    public String parseContent(String fileUrl) throws Exception {
        log.info("开始解析Word文件: {}", fileUrl);
        
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
     * 解析.docx格式文件
     */
    private String parseDocxContent(InputStream inputStream) throws Exception {
        try (XWPFDocument document = new XWPFDocument(inputStream);
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            return extractor.getText();
        }
    }

    @Override
    public boolean supports(String fileType) {
        return "docx".equalsIgnoreCase(fileType);
    }
} 