package com.leyue.smartcs.knowledge.parser.impl;

import com.leyue.smartcs.knowledge.parser.DocumentParser;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
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
    
    // 内容提取正则
    private static final Pattern TITLE_PATTERN = Pattern.compile("<title[^>]*>([^<]*)</title>", Pattern.CASE_INSENSITIVE);
    private static final Pattern HEADING_PATTERN = Pattern.compile("<h[1-6][^>]*>([^<]*)</h[1-6]>", Pattern.CASE_INSENSITIVE);
    private static final Pattern PARAGRAPH_PATTERN = Pattern.compile("<p[^>]*>([\\s\\S]*?)</p>", Pattern.CASE_INSENSITIVE);
    private static final Pattern TABLE_PATTERN = Pattern.compile("<table[^>]*>[\\s\\S]*?</table>", Pattern.CASE_INSENSITIVE);
    private static final Pattern IMAGE_PATTERN = Pattern.compile("<img[^>]+src=[\"']([^\"']+)[\"'][^>]*>", Pattern.CASE_INSENSITIVE);

    @Override
    public List<Document> parse(Resource resource, String fileName) throws IOException {
        log.info("开始解析HTML文件: {}", fileName);
        
        List<Document> documents = new ArrayList<>();
        StringBuilder rawHtml = new StringBuilder();
        
        try (InputStream inputStream = resource.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            
            String line;
            while ((line = reader.readLine()) != null) {
                rawHtml.append(line).append("\n");
            }
            
            String htmlContent = rawHtml.toString();
            
            // 提取文档标题
            String title = extractTitle(htmlContent);
            if (title != null && !title.trim().isEmpty()) {
                Metadata titleMetadata = Metadata.from("type", "title")
                        .put("fileName", fileName)
                        .put("elementType", "title");
                documents.add(Document.from(title.trim(), titleMetadata));
            }
            
            // 提取标题层次结构
            documents.addAll(extractHeadings(htmlContent, fileName));
            
            // 提取段落内容
            documents.addAll(extractParagraphs(htmlContent, fileName));
            
            // 提取表格内容
            documents.addAll(extractTables(htmlContent, fileName));
            
            // 提取图片信息
            documents.addAll(extractImages(htmlContent, fileName));
            
            // 创建完整HTML文档（清理后的文本版本）
            String cleanText = parseHtmlToText(htmlContent);
            Metadata fullMetadata = Metadata.from("type", "full_document")
                    .put("fileName", fileName)
                    .put("originalSize", String.valueOf(htmlContent.length()))
                    .put("cleanSize", String.valueOf(cleanText.length()))
                    .put("hasTitle", String.valueOf(title != null && !title.trim().isEmpty()))
                    .put("tableCount", String.valueOf(countMatches(htmlContent, TABLE_PATTERN)))
                    .put("imageCount", String.valueOf(countMatches(htmlContent, IMAGE_PATTERN)));
            
            documents.add(Document.from(cleanText, fullMetadata));
            
            log.info("HTML解析完成，文件: {}，原始大小: {}，生成文档数: {}", 
                    fileName, htmlContent.length(), documents.size());
            
        } catch (IOException e) {
            log.error("HTML解析失败: {}", fileName, e);
            throw new IOException("HTML文件解析失败: " + e.getMessage(), e);
        }
        
        return documents;
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
    public String[] getSupportedTypes() {
        return new String[]{"html", "htm", "xhtml"};
    }

    @Override
    public boolean supports(String extension) {
        if (extension == null) {
            return false;
        }
        String lowerExt = extension.toLowerCase();
        return "html".equals(lowerExt) || "htm".equals(lowerExt) || "xhtml".equals(lowerExt);
    }

    /**
     * 提取HTML标题
     */
    private String extractTitle(String htmlContent) {
        Matcher matcher = TITLE_PATTERN.matcher(htmlContent);
        if (matcher.find()) {
            return cleanHtmlText(matcher.group(1));
        }
        return null;
    }

    /**
     * 提取标题层次结构
     */
    private List<Document> extractHeadings(String htmlContent, String fileName) {
        List<Document> headings = new ArrayList<>();
        Matcher matcher = HEADING_PATTERN.matcher(htmlContent);
        int headingIndex = 0;
        
        while (matcher.find()) {
            String headingText = cleanHtmlText(matcher.group(1));
            if (!headingText.trim().isEmpty()) {
                // 提取标题级别
                String fullTag = matcher.group();
                String level = fullTag.substring(2, 3); // h1 -> 1, h2 -> 2, etc.
                
                Metadata headingMetadata = Metadata.from("type", "heading")
                        .put("fileName", fileName)
                        .put("elementType", "h" + level)
                        .put("level", level)
                        .put("headingIndex", String.valueOf(headingIndex++));
                
                headings.add(Document.from(headingText, headingMetadata));
            }
        }
        
        return headings;
    }

    /**
     * 提取段落内容
     */
    private List<Document> extractParagraphs(String htmlContent, String fileName) {
        List<Document> paragraphs = new ArrayList<>();
        Matcher matcher = PARAGRAPH_PATTERN.matcher(htmlContent);
        int paragraphIndex = 0;
        
        while (matcher.find()) {
            String paragraphText = cleanHtmlText(matcher.group(1));
            if (!paragraphText.trim().isEmpty()) {
                Metadata paragraphMetadata = Metadata.from("type", "paragraph")
                        .put("fileName", fileName)
                        .put("elementType", "p")
                        .put("paragraphIndex", String.valueOf(paragraphIndex++));
                
                paragraphs.add(Document.from(paragraphText, paragraphMetadata));
            }
        }
        
        return paragraphs;
    }

    /**
     * 提取表格内容
     */
    private List<Document> extractTables(String htmlContent, String fileName) {
        List<Document> tables = new ArrayList<>();
        Matcher matcher = TABLE_PATTERN.matcher(htmlContent);
        int tableIndex = 0;
        
        while (matcher.find()) {
            String tableHtml = matcher.group();
            String tableText = parseHtmlToText(tableHtml);
            
            if (!tableText.trim().isEmpty()) {
                Metadata tableMetadata = Metadata.from("type", "table")
                        .put("fileName", fileName)
                        .put("elementType", "table")
                        .put("tableIndex", String.valueOf(tableIndex++))
                        .put("originalHtml", tableHtml);
                
                tables.add(Document.from(tableText, tableMetadata));
            }
        }
        
        return tables;
    }

    /**
     * 提取图片信息
     */
    private List<Document> extractImages(String htmlContent, String fileName) {
        List<Document> images = new ArrayList<>();
        Matcher matcher = IMAGE_PATTERN.matcher(htmlContent);
        int imageIndex = 0;
        
        while (matcher.find()) {
            String src = matcher.group(1);
            String fullTag = matcher.group();
            
            // 提取alt属性
            String alt = extractAttribute(fullTag, "alt");
            String title = extractAttribute(fullTag, "title");
            
            StringBuilder imageText = new StringBuilder();
            imageText.append("图像: ").append(src);
            if (alt != null && !alt.isEmpty()) {
                imageText.append(", 描述: ").append(alt);
            }
            if (title != null && !title.isEmpty()) {
                imageText.append(", 标题: ").append(title);
            }
            
            Metadata imageMetadata = Metadata.from("type", "image")
                    .put("fileName", fileName)
                    .put("elementType", "img")
                    .put("imageIndex", String.valueOf(imageIndex++))
                    .put("src", src)
                    .put("alt", alt != null ? alt : "")
                    .put("title", title != null ? title : "");
            
            images.add(Document.from(imageText.toString(), imageMetadata));
        }
        
        return images;
    }

    /**
     * 提取HTML属性值
     */
    private String extractAttribute(String tag, String attributeName) {
        Pattern pattern = Pattern.compile(attributeName + "=[\"']([^\"']*)[\"']", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(tag);
        return matcher.find() ? matcher.group(1) : null;
    }

    /**
     * 清理HTML文本
     */
    private String cleanHtmlText(String text) {
        if (text == null) return "";
        
        // 移除HTML标签
        String cleaned = HTML_TAG_PATTERN.matcher(text).replaceAll(" ");
        
        // 解码HTML实体
        cleaned = cleaned.replace("&nbsp;", " ")
                        .replace("&lt;", "<")
                        .replace("&gt;", ">")
                        .replace("&amp;", "&")
                        .replace("&quot;", "\"")
                        .replace("&#39;", "'");
        
        // 规范化空白字符
        cleaned = WHITESPACE_PATTERN.matcher(cleaned).replaceAll(" ");
        
        return cleaned.trim();
    }

    /**
     * 计算正则匹配的数量
     */
    private int countMatches(String text, Pattern pattern) {
        Matcher matcher = pattern.matcher(text);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    /**
     * 保持向后兼容的方法（已弃用）
     * @deprecated 使用 parse(Resource resource, String fileName) 方法替代
     */
    @Deprecated
    public String parseContent(String fileUrl) throws Exception {
        log.warn("使用了已弃用的 parseContent 方法，建议使用 parse 方法");
        
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
} 