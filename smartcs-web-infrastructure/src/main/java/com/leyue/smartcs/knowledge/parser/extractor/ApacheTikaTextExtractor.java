package com.leyue.smartcs.knowledge.parser.extractor;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;

/**
 * Apache Tika文本提取器
 * 使用Apache Tika框架替代自定义PDFBox文本提取逻辑
 * 支持多种文档格式的统一文本提取
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ApacheTikaTextExtractor {

    private final Tika tika = new Tika();
    private final Parser autoDetectParser = new AutoDetectParser();

    /**
     * 使用Apache Tika提取文档文本
     * 
     * @param inputStream 文档输入流
     * @param sourceFileName 源文件名（用于类型检测）
     * @return 提取的文档对象
     */
    public Document extractText(InputStream inputStream, String sourceFileName) {
        try {
            log.debug("开始使用Apache Tika提取文本: fileName={}", sourceFileName);

            // 使用简单的Tika接口进行文本提取
            String extractedText = tika.parseToString(inputStream);

            // 评估文本质量
            TextQuality textQuality = evaluateTextQuality(extractedText);
            
            log.info("Apache Tika文本提取完成: fileName={}, textLength={}, quality={}", 
                    sourceFileName, extractedText.length(), textQuality.getScore());

            // 构建元数据
            Metadata metadata = Metadata.from("extractor", "apache-tika")
                    .put("sourceFileName", sourceFileName)
                    .put("textLength", String.valueOf(extractedText.length()))
                    .put("textQuality", textQuality.getScore().toString())
                    .put("extractionMethod", "tika-simple");

            return Document.from(extractedText, metadata);

        } catch (Exception e) {
            log.error("Apache Tika文本提取失败: fileName={}", sourceFileName, e);
            throw new RuntimeException("文本提取失败: " + e.getMessage(), e);
        }
    }

    /**
     * 使用Apache Tika详细解析（包含元数据）
     * 
     * @param inputStream 文档输入流
     * @param sourceFileName 源文件名
     * @return 提取的文档对象（包含丰富元数据）
     */
    public Document extractTextWithMetadata(InputStream inputStream, String sourceFileName) {
        try {
            log.debug("开始使用Apache Tika详细解析: fileName={}", sourceFileName);

            // 使用详细解析器
            BodyContentHandler handler = new BodyContentHandler(-1); // 无长度限制
            org.apache.tika.metadata.Metadata tikaMetadata = new org.apache.tika.metadata.Metadata();
            ParseContext parseContext = new ParseContext();

            autoDetectParser.parse(inputStream, handler, tikaMetadata, parseContext);

            String extractedText = handler.toString();
            TextQuality textQuality = evaluateTextQuality(extractedText);

            log.info("Apache Tika详细解析完成: fileName={}, textLength={}, quality={}", 
                    sourceFileName, extractedText.length(), textQuality.getScore());

            // 构建丰富的元数据
            Metadata metadata = buildRichMetadata(tikaMetadata, sourceFileName, textQuality);

            return Document.from(extractedText, metadata);

        } catch (IOException | SAXException | TikaException e) {
            log.error("Apache Tika详细解析失败: fileName={}", sourceFileName, e);
            // 降级到简单提取
            log.warn("降级到简单文本提取: fileName={}", sourceFileName);
            return extractText(inputStream, sourceFileName);
        }
    }

    /**
     * 评估文本质量
     */
    private TextQuality evaluateTextQuality(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new TextQuality(0.0, "文本为空");
        }

        double score = 1.0;
        StringBuilder issues = new StringBuilder();

        // 检查文本长度
        if (text.length() < 100) {
            score -= 0.3;
            issues.append("文本过短;");
        }

        // 检查乱码率
        long invalidChars = text.chars()
                .filter(c -> c == 0xFFFD || (c >= 0x0000 && c <= 0x001F && c != 0x0009 && c != 0x000A && c != 0x000D))
                .count();
        
        double invalidRatio = (double) invalidChars / text.length();
        if (invalidRatio > 0.1) {
            score -= 0.4;
            issues.append("乱码率过高;");
        }

        // 检查空白行比例
        String[] lines = text.split("\n");
        long emptyLines = java.util.Arrays.stream(lines)
                .filter(line -> line.trim().isEmpty())
                .count();
        
        double emptyRatio = (double) emptyLines / lines.length;
        if (emptyRatio > 0.5) {
            score -= 0.2;
            issues.append("空白行过多;");
        }

        // 检查重复内容
        if (hasHighRepetition(text)) {
            score -= 0.1;
            issues.append("重复内容;");
        }

        return new TextQuality(Math.max(0.0, score), issues.toString());
    }

    /**
     * 检查是否有高重复内容
     */
    private boolean hasHighRepetition(String text) {
        String[] sentences = text.split("[.!?]+");
        if (sentences.length < 10) return false;

        long uniqueSentences = java.util.Arrays.stream(sentences)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .count();

        return uniqueSentences < sentences.length * 0.8;
    }

    /**
     * 构建丰富的元数据
     */
    private Metadata buildRichMetadata(org.apache.tika.metadata.Metadata tikaMetadata, 
                                      String sourceFileName, TextQuality textQuality) {
        Metadata metadata = Metadata.from("extractor", "apache-tika-detailed")
                .put("sourceFileName", sourceFileName)
                .put("textQuality", textQuality.getScore().toString())
                .put("qualityIssues", textQuality.getIssues());

        // 从Tika元数据中提取关键信息并构建完整元数据
        metadata = addIfPresent(metadata, "title", tikaMetadata.get("title"));
        metadata = addIfPresent(metadata, "author", tikaMetadata.get("author"));
        metadata = addIfPresent(metadata, "creator", tikaMetadata.get("creator"));
        metadata = addIfPresent(metadata, "subject", tikaMetadata.get("subject"));
        metadata = addIfPresent(metadata, "creationDate", tikaMetadata.get("created"));
        metadata = addIfPresent(metadata, "modificationDate", tikaMetadata.get("modified"));
        metadata = addIfPresent(metadata, "contentType", tikaMetadata.get("Content-Type"));
        metadata = addIfPresent(metadata, "pageCount", tikaMetadata.get("xmpTPg:NPages"));

        // 添加语言检测信息
        String language = detectLanguage(tikaMetadata.get("language"));
        metadata = addIfPresent(metadata, "language", language);

        return metadata;
    }

    /**
     * 安全地添加元数据
     */
    private Metadata addIfPresent(Metadata metadata, String key, String value) {
        if (value != null && !value.trim().isEmpty()) {
            return metadata.put(key, value.trim());
        }
        return metadata;
    }

    /**
     * 检测或推断文档语言
     */
    private String detectLanguage(String tikaLanguage) {
        if (tikaLanguage != null && !tikaLanguage.trim().isEmpty()) {
            return tikaLanguage.trim();
        }
        
        // 可以在这里添加更复杂的语言检测逻辑
        return "unknown";
    }

    /**
     * 文本质量评估结果
     */
    public static class TextQuality {
        private final Double score;
        private final String issues;

        public TextQuality(Double score, String issues) {
            this.score = score;
            this.issues = issues;
        }

        public Double getScore() {
            return score;
        }

        public String getIssues() {
            return issues;
        }

        public boolean isHighQuality() {
            return score >= 0.8;
        }

        public boolean isAcceptable() {
            return score >= 0.5;
        }
    }
}