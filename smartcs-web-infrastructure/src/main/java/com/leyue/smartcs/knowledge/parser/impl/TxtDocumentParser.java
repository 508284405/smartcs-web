package com.leyue.smartcs.knowledge.parser.impl;

import com.leyue.smartcs.domain.knowledge.model.Document;
import com.leyue.smartcs.knowledge.parser.DocumentParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * TXT文档解析器
 * 直接读取文本文件内容
 */
@Component
@Slf4j
public class TxtDocumentParser implements DocumentParser {
    
    /**
     * 支持的文件类型
     */
    private static final String SUPPORTED_TYPE = "txt";
    
    @Override
    public String parseContent(Document document, File localFile) throws Exception {
        log.info("解析TXT文档: {}, 文件路径: {}", document.getTitle(), localFile.getAbsolutePath());
        
        try {
            // 读取文件内容
            String content = Files.readString(Path.of(localFile.getAbsolutePath()), StandardCharsets.UTF_8);
            log.info("TXT解析完成，文本长度: {}", content.length());
            return content;
        } catch (Exception e) {
            log.error("TXT解析失败: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    @Override
    public String getSupportedFileType() {
        return SUPPORTED_TYPE;
    }
} 