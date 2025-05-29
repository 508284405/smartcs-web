package com.leyue.smartcs.knowledge.parser.impl;

import com.leyue.smartcs.knowledge.parser.DocumentParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

/**
 * Markdown文档解析器
 */
@Component
@Slf4j
public class MarkdownDocumentParser implements DocumentParser {

    // Markdown语法正则表达式
    private static final Pattern HEADER_PATTERN = Pattern.compile("^#{1,6}\\s+");
    private static final Pattern BOLD_PATTERN = Pattern.compile("\\*\\*(.*?)\\*\\*");
    private static final Pattern ITALIC_PATTERN = Pattern.compile("\\*(.*?)\\*");
    private static final Pattern CODE_BLOCK_PATTERN = Pattern.compile("```[\\s\\S]*?```");
    private static final Pattern INLINE_CODE_PATTERN = Pattern.compile("`(.*?)`");
    private static final Pattern LINK_PATTERN = Pattern.compile("\\[([^\\]]+)\\]\\([^\\)]+\\)");
    private static final Pattern IMAGE_PATTERN = Pattern.compile("!\\[([^\\]]*)\\]\\([^\\)]+\\)");

    @Override
    public String parseContent(String fileUrl) throws Exception {
        log.info("开始解析Markdown文件: {}", fileUrl);
        
        try (InputStream inputStream = new URL(fileUrl).openStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            
            StringBuilder content = new StringBuilder();
            String line;
            
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            
            String rawText = content.toString();
            String parsedText = parseMarkdownToText(rawText);
            
            log.info("Markdown解析完成，原始长度: {}, 解析后长度: {}", rawText.length(), parsedText.length());
            return parsedText;
            
        } catch (Exception e) {
            log.error("Markdown解析失败: {}", fileUrl, e);
            throw new Exception("Markdown解析失败: " + e.getMessage(), e);
        }
    }

    /**
     * 将Markdown格式转换为纯文本
     */
    private String parseMarkdownToText(String markdown) {
        String text = markdown;
        
        // 移除代码块
        text = CODE_BLOCK_PATTERN.matcher(text).replaceAll("");
        
        // 移除内联代码
        text = INLINE_CODE_PATTERN.matcher(text).replaceAll("$1");
        
        // 移除标题格式，保留文本
        text = HEADER_PATTERN.matcher(text).replaceAll("");
        
        // 移除粗体格式，保留文本
        text = BOLD_PATTERN.matcher(text).replaceAll("$1");
        
        // 移除斜体格式，保留文本
        text = ITALIC_PATTERN.matcher(text).replaceAll("$1");
        
        // 移除链接格式，保留链接文本
        text = LINK_PATTERN.matcher(text).replaceAll("$1");
        
        // 移除图片格式，保留alt文本
        text = IMAGE_PATTERN.matcher(text).replaceAll("$1");
        
        // 清理多余的空行
        text = text.replaceAll("\n{3,}", "\n\n");
        
        return text.trim();
    }

    @Override
    public boolean supports(String fileType) {
        return "md".equalsIgnoreCase(fileType);
    }
} 