package com.leyue.smartcs.knowledge.parser.impl;

import com.leyue.smartcs.domain.knowledge.Document;
import com.leyue.smartcs.knowledge.parser.DocumentParser;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;

/**
 * Word文档解析器
 * 使用Apache POI解析Word文件内容，支持.docx格式
 */
@Component
@Slf4j
public class WordDocumentParser implements DocumentParser {
    
    /**
     * 支持的文件类型
     */
    private static final String SUPPORTED_TYPE = "docx";
    
    @Override
    public String parseContent(Document document, File localFile) throws Exception {
        log.info("解析Word文档: {}, 文件路径: {}", document.getTitle(), localFile.getAbsolutePath());
        
        try (FileInputStream fis = new FileInputStream(localFile);
             XWPFDocument wordDoc = new XWPFDocument(fis)) {
            
            XWPFWordExtractor extractor = new XWPFWordExtractor(wordDoc);
            String text = extractor.getText();
            
            log.info("Word解析完成，提取的文本长度: {}", text.length());
            return text;
        } catch (Exception e) {
            log.error("Word解析失败: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    public String getSupportedFileType() {
        return SUPPORTED_TYPE;
    }
} 