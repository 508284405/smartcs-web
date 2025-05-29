package com.leyue.smartcs.knowledge.parser.impl;

import com.leyue.smartcs.knowledge.parser.DocumentParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * TXT文档解析器
 */
@Component
@Slf4j
public class TxtDocumentParser implements DocumentParser {

    @Override
    public String parseContent(String fileUrl) throws Exception {
        log.info("开始解析TXT文件: {}", fileUrl);
        
        try (InputStream inputStream = new URL(fileUrl).openStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            
            StringBuilder content = new StringBuilder();
            String line;
            
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            
            String text = content.toString();
            log.info("TXT解析完成，提取文本长度: {}", text.length());
            return text;
            
        } catch (Exception e) {
            log.error("TXT解析失败: {}", fileUrl, e);
            throw new Exception("TXT解析失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean supports(String fileType) {
        return "txt".equalsIgnoreCase(fileType);
    }
} 