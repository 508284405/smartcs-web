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
 * HTML文档解析器
 */
@Component
@Slf4j
public class HtmlDocumentParser implements DocumentParser {

    // HTML标签正则表达式
    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<[^>]+>");
    private static final Pattern SCRIPT_PATTERN = Pattern.compile("<script[^>]*>[\\s\\S]*?</script>", Pattern.CASE_INSENSITIVE);
    private static final Pattern STYLE_PATTERN = Pattern.compile("<style[^>]*>[\\s\\S]*?</style>", Pattern.CASE_INSENSITIVE);
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");

    @Override
    public String parseContent(String fileUrl) throws Exception {
        log.info("开始解析HTML文件: {}", fileUrl);
        
        try (InputStream inputStream = new URL(fileUrl).openStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            
            StringBuilder content = new StringBuilder();
            String line;
            
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            
            String rawHtml = content.toString();
            String text = parseHtmlToText(rawHtml);
            
            log.info("HTML解析完成，原始长度: {}, 解析后长度: {}", rawHtml.length(), text.length());
            return text;
            
        } catch (Exception e) {
            log.error("HTML解析失败: {}", fileUrl, e);
            throw new Exception("HTML解析失败: " + e.getMessage(), e);
        }
    }

    /**
     * 将HTML转换为纯文本
     */
    private String parseHtmlToText(String html) {
        String text = html;
        
        // 移除script标签及内容
        text = SCRIPT_PATTERN.matcher(text).replaceAll("");
        
        // 移除style标签及内容
        text = STYLE_PATTERN.matcher(text).replaceAll("");
        
        // 移除所有HTML标签
        text = HTML_TAG_PATTERN.matcher(text).replaceAll(" ");
        
        // 规范化空白字符
        text = WHITESPACE_PATTERN.matcher(text).replaceAll(" ");
        
        // 解码HTML实体
        text = text.replace("&nbsp;", " ")
                  .replace("&lt;", "<")
                  .replace("&gt;", ">")
                  .replace("&amp;", "&")
                  .replace("&quot;", "\"")
                  .replace("&#39;", "'");
        
        return text.trim();
    }

    @Override
    public boolean supports(String fileType) {
        return "html".equalsIgnoreCase(fileType);
    }
} 