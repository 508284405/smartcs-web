package com.leyue.smartcs.knowledge.parser.impl;

import com.leyue.smartcs.dto.knowledge.ModelRequest;
import com.leyue.smartcs.knowledge.parser.model.ParserExtendParam;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * PDF文档解析器 - 多模态RAG增强版
 * 支持高级文本提取、智能图片处理、表格检测和结构化内容分析
 * 基于多模态RAG最佳实践实现
 */
@Slf4j
@Component
public class PdfDocumentParser extends AbstractDocumentParser {

    // 表格检测正则模式
    private static final Pattern TABLE_PATTERN = Pattern.compile(
            "(?:\\s*\\|[^\\n]*\\|\\s*\\n){2,}|" + // 管道分隔的表格
                    "(?:\\s*[^\\n]*\\t[^\\n]*\\n){2,}|" + // Tab分隔的表格
                    "(?:\\s*\\d+[\\s.]+[^\\n]*\\n){3,}" // 编号列表形式的表格
    );

    // 标题检测模式
    private static final Pattern TITLE_PATTERN = Pattern.compile(
            "^\\s*(?:第?[一二三四五六七八九十\\d]+[章节部分条]|" +
                    "\\d+\\.\\d*|[A-Z]\\.|\\d+\\)|" +
                    "[\\u4e00-\\u9fa5]{1,20}：?)\\s*([\\u4e00-\\u9fa5A-Za-z\\d\\s]{2,50})\\s*$",
            Pattern.MULTILINE);

    // 配置参数
    @Value("${pdf.multimodal.enable-ocr:false}")
    private boolean enableOCR;

    @Value("${pdf.multimodal.enable-table-detection:true}")
    private boolean enableTableDetection;

    @Value("${pdf.multimodal.enable-image-description:true}")
    private boolean enableImageDescription;

    @Value("${pdf.multimodal.min-image-size:100}")
    private int minImageSize;

    @Override
    public List<Document> parse(Resource resource, String fileName, ParserExtendParam parserExtendParam)
            throws IOException {
        return parse(resource, fileName, parserExtendParam.getModelRequest());
    }

    /**
     * 带视觉LLM模型的PDF解析方法
     * 
     * @param resource    PDF资源
     * @param fileName    文件名
     */
    private List<Document> parse(Resource resource, String fileName, ModelRequest modelRequest) throws IOException {
        List<Document> documents = new ArrayList<>();

        try (InputStream inputStream = resource.getInputStream();
                PDDocument pdDocument = PDDocument.load(inputStream)) {

            log.info("开始多模态PDF解析，文件: {}，页数: {}", fileName, pdDocument.getNumberOfPages());

            // 1. 文档级别分析
            DocumentAnalysis analysis = analyzeDocument(pdDocument);

            // 2. 提取文档元数据
            documents.add(createDocumentMetadata(fileName, pdDocument, analysis));

            // 3. 处理每一页的多模态内容
            PDPageTree pages = pdDocument.getPages();
            int pageIndex = 0;

            for (PDPage page : pages) {
                pageIndex++;
                log.debug("处理第{}页", pageIndex);

                // 提取页面的多模态内容
                PageContent pageContent = extractPageContent(page, pageIndex, fileName, pdDocument, modelRequest);

                // 添加文本内容
                if (pageContent.hasText()) {
                    documents.addAll(processTextContent(pageContent, fileName, pageIndex));
                }

                // 添加图像内容
                if (pageContent.hasImages()) {
                    documents.addAll(processImageContent(pageContent, fileName, pageIndex));
                }

                // 添加表格内容
                if (pageContent.hasTables()) {
                    documents.addAll(processTableContent(pageContent, fileName, pageIndex));
                }
            }

            // 4. 生成文档结构化索引
            documents.addAll(generateDocumentStructure(pdDocument, fileName, analysis));

            log.info("多模态PDF解析完成，文件: {}，生成文档数: {}", fileName, documents.size());

        } catch (Exception e) {
            log.error("多模态PDF解析失败: {}", fileName, e);
            throw new IOException("多模态PDF解析失败: " + e.getMessage(), e);
        }

        return documents;
    }

    /**
     * 文档级别分析
     */
    private DocumentAnalysis analyzeDocument(PDDocument document) {
        DocumentAnalysis analysis = new DocumentAnalysis();
        analysis.pageCount = document.getNumberOfPages();

        try {
            // 分析文档结构
            PDFTextStripper textStripper = new PDFTextStripper();
            String fullText = textStripper.getText(document);

            analysis.totalCharacters = fullText.length();
            analysis.hasImages = hasImages(document);
            analysis.estimatedTables = countEstimatedTables(fullText);
            analysis.titleCount = countTitles(fullText);

        } catch (Exception e) {
            log.warn("文档分析时出错", e);
        }

        return analysis;
    }

    /**
     * 创建文档元数据
     */
    private Document createDocumentMetadata(String fileName, PDDocument document, DocumentAnalysis analysis) {
        Metadata metadata = Metadata.from("type", "pdf_metadata")
                .put("fileName", fileName)
                .put("pageCount", String.valueOf(analysis.pageCount))
                .put("totalCharacters", String.valueOf(analysis.totalCharacters))
                .put("hasImages", String.valueOf(analysis.hasImages))
                .put("estimatedTables", String.valueOf(analysis.estimatedTables))
                .put("titleCount", String.valueOf(analysis.titleCount));

        String content = String.format("PDF文档概览 - 文件名: %s, 页数: %d, 字符数: %d, 包含图像: %s, 估计表格数: %d, 标题数: %d",
                fileName, analysis.pageCount, analysis.totalCharacters,
                analysis.hasImages, analysis.estimatedTables, analysis.titleCount);

        return Document.from(content, metadata);
    }

    /**
     * 提取页面内容
     */
    private PageContent extractPageContent(PDPage page, int pageIndex, String fileName, PDDocument document,
            ModelRequest modelRequest) {
        PageContent content = new PageContent(pageIndex);

        try {
            // 提取文本内容
            PDFTextStripper textStripper = new PDFTextStripper();
            textStripper.setStartPage(pageIndex);
            textStripper.setEndPage(pageIndex);
            String pageText = textStripper.getText(document);
            content.textContent = pageText != null ? pageText.trim() : "";

            // 检测和提取表格
            if (enableTableDetection && !content.textContent.isEmpty()) {
                content.tables = detectTables(content.textContent);
            }

            // 提取图像
            content.images = extractPageImages(page, pageIndex, modelRequest);

        } catch (Exception e) {
            log.warn("提取页面{}内容时出错", pageIndex, e);
        }

        return content;
    }

    /**
     * 处理文本内容
     */
    private List<Document> processTextContent(PageContent pageContent, String fileName, int pageIndex) {
        List<Document> documents = new ArrayList<>();

        if (pageContent.textContent.isEmpty()) {
            return documents;
        }

        // 检测标题和段落
        List<TextSegment> segments = segmentText(pageContent.textContent);

        for (int i = 0; i < segments.size(); i++) {
            TextSegment segment = segments.get(i);

            Metadata metadata = Metadata.from("type", segment.isTitle ? "pdf_title" : "pdf_text")
                    .put("fileName", fileName)
                    .put("pageNumber", String.valueOf(pageIndex))
                    .put("segmentIndex", String.valueOf(i))
                    .put("segmentType", segment.isTitle ? "title" : "paragraph")
                    .put("confidence", String.valueOf(segment.confidence));

            documents.add(Document.from(segment.content, metadata));
        }

        return documents;
    }

    /**
     * 处理图像内容
     */
    private List<Document> processImageContent(PageContent pageContent, String fileName, int pageIndex) {
        List<Document> documents = new ArrayList<>();

        for (int i = 0; i < pageContent.images.size(); i++) {
            ImageInfo image = pageContent.images.get(i);

            // 过滤小图像（可能是装饰性图标）
            if (image.width < minImageSize || image.height < minImageSize) {
                continue;
            }

            StringBuilder content = new StringBuilder();
            content.append(String.format("图像内容 - 页面: %d, 格式: %s, 尺寸: %dx%d",
                    pageIndex, image.format, image.width, image.height));

            // OCR处理（如果启用）
            if (enableOCR && image.ocrText != null && !image.ocrText.trim().isEmpty()) {
                content.append("\nOCR识别文本: ").append(image.ocrText);
            }

            // 图像描述（如果启用）
            if (enableImageDescription && image.description != null && !image.description.trim().isEmpty()) {
                content.append("\n图像描述: ").append(image.description);
            }

            Metadata metadata = Metadata.from("type", "pdf_image")
                    .put("fileName", fileName)
                    .put("pageNumber", String.valueOf(pageIndex))
                    .put("imageIndex", String.valueOf(i))
                    .put("imageFormat", image.format)
                    .put("width", String.valueOf(image.width))
                    .put("height", String.valueOf(image.height))
                    .put("hasOCR", String.valueOf(enableOCR && image.ocrText != null))
                    .put("hasDescription", String.valueOf(enableImageDescription && image.description != null));

            documents.add(Document.from(content.toString(), metadata));
        }

        return documents;
    }

    /**
     * 处理表格内容
     */
    private List<Document> processTableContent(PageContent pageContent, String fileName, int pageIndex) {
        List<Document> documents = new ArrayList<>();

        for (int i = 0; i < pageContent.tables.size(); i++) {
            TableInfo table = pageContent.tables.get(i);

            StringBuilder content = new StringBuilder();
            content.append(String.format("表格内容 - 页面: %d, 行数: %d, 列数: %d\n",
                    pageIndex, table.rowCount, table.columnCount));
            content.append("表格数据:\n").append(table.content);

            Metadata metadata = Metadata.from("type", "pdf_table")
                    .put("fileName", fileName)
                    .put("pageNumber", String.valueOf(pageIndex))
                    .put("tableIndex", String.valueOf(i))
                    .put("rowCount", String.valueOf(table.rowCount))
                    .put("columnCount", String.valueOf(table.columnCount))
                    .put("confidence", String.valueOf(table.confidence));

            documents.add(Document.from(content.toString(), metadata));
        }

        return documents;
    }

    /**
     * 生成文档结构化索引
     */
    private List<Document> generateDocumentStructure(PDDocument document, String fileName, DocumentAnalysis analysis) {
        List<Document> documents = new ArrayList<>();

        // 创建文档大纲
        StringBuilder outline = new StringBuilder();
        outline.append("文档结构大纲:\n");
        outline.append(String.format("- 总页数: %d\n", analysis.pageCount));
        outline.append(String.format("- 包含图像: %s\n", analysis.hasImages ? "是" : "否"));
        outline.append(String.format("- 估计表格数: %d\n", analysis.estimatedTables));
        outline.append(String.format("- 标题数量: %d\n", analysis.titleCount));

        Metadata outlineMetadata = Metadata.from("type", "pdf_outline")
                .put("fileName", fileName)
                .put("structureType", "document_outline");

        documents.add(Document.from(outline.toString(), outlineMetadata));

        return documents;
    }

    // =================== 辅助方法 ===================

    /**
     * 检查PDF是否包含图片
     */
    private boolean hasImages(PDDocument document) {
        try {
            for (PDPage page : document.getPages()) {
                var resources = page.getResources();
                if (resources != null && resources.getXObjectNames() != null) {
                    for (var name : resources.getXObjectNames()) {
                        var xObject = resources.getXObject(name);
                        if (xObject instanceof PDImageXObject) {
                            return true;
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("检查PDF图片时出错", e);
        }
        return false;
    }

    /**
     * 估算表格数量
     */
    private int countEstimatedTables(String text) {
        Matcher matcher = TABLE_PATTERN.matcher(text);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    /**
     * 计算标题数量
     */
    private int countTitles(String text) {
        Matcher matcher = TITLE_PATTERN.matcher(text);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    /**
     * 提取页面图像
     */
    private List<ImageInfo> extractPageImages(PDPage page, int pageIndex, ModelRequest modelRequest) {
        List<ImageInfo> images = new ArrayList<>();

        try {
            var resources = page.getResources();
            if (resources != null && resources.getXObjectNames() != null) {
                for (var name : resources.getXObjectNames()) {
                    try {
                        var xObject = resources.getXObject(name);
                        if (xObject instanceof PDImageXObject) {
                            PDImageXObject pdImage = (PDImageXObject) xObject;

                            ImageInfo imageInfo = new ImageInfo();
                            imageInfo.width = pdImage.getWidth();
                            imageInfo.height = pdImage.getHeight();
                            imageInfo.format = pdImage.getSuffix();
                            imageInfo.pageNumber = pageIndex;

                            // 转换为BufferedImage用于进一步处理
                            BufferedImage bufferedImage = pdImage.getImage();
                            imageInfo.imageData = imageToBase64(bufferedImage);

                            // OCR处理（如果启用）
                            if (enableOCR) {
                                imageInfo.ocrText = performOCR(bufferedImage);
                            }

                            // 图像描述生成（如果启用）
                            if (enableImageDescription && modelRequest != null) {
                                imageInfo.description = generateImageDescription(bufferedImage, modelRequest);
                            }

                            images.add(imageInfo);
                        }
                    } catch (Exception e) {
                        log.warn("提取页面图片失败，页面: {}，图片: {}", pageIndex, name, e);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("提取页面{}的图片时出错", pageIndex, e);
        }

        return images;
    }

    /**
     * 检测表格
     */
    private List<TableInfo> detectTables(String text) {
        List<TableInfo> tables = new ArrayList<>();

        Matcher matcher = TABLE_PATTERN.matcher(text);
        int tableIndex = 0;

        while (matcher.find()) {
            String tableContent = matcher.group();

            TableInfo tableInfo = new TableInfo();
            tableInfo.content = tableContent;
            tableInfo.startIndex = matcher.start();
            tableInfo.endIndex = matcher.end();
            tableInfo.tableIndex = tableIndex++;

            // 分析表格结构
            analyzeTableStructure(tableInfo, tableContent);

            tables.add(tableInfo);
        }

        return tables;
    }

    /**
     * 分析表格结构
     */
    private void analyzeTableStructure(TableInfo tableInfo, String tableContent) {
        String[] lines = tableContent.split("\\n");
        tableInfo.rowCount = lines.length;

        // 估算列数（取第一行的分隔符数量）
        if (lines.length > 0) {
            String firstLine = lines[0];
            if (firstLine.contains("|")) {
                tableInfo.columnCount = firstLine.split("\\|").length - 1;
            } else if (firstLine.contains("\t")) {
                tableInfo.columnCount = firstLine.split("\t").length;
            } else {
                tableInfo.columnCount = 1;
            }
        }

        // 设置置信度（基于结构规律性）
        tableInfo.confidence = calculateTableConfidence(tableContent);
    }

    /**
     * 计算表格置信度
     */
    private double calculateTableConfidence(String tableContent) {
        String[] lines = tableContent.split("\\n");
        if (lines.length < 2)
            return 0.3;

        // 检查行结构一致性
        int consistentRows = 0;
        String pattern = null;

        for (String line : lines) {
            String currentPattern = line.replaceAll("[^|\\t]", "X");
            if (pattern == null) {
                pattern = currentPattern;
                consistentRows = 1;
            } else if (pattern.equals(currentPattern)) {
                consistentRows++;
            }
        }

        double consistencyRatio = (double) consistentRows / lines.length;
        return Math.min(consistencyRatio, 1.0);
    }

    /**
     * 文本分段
     */
    private List<TextSegment> segmentText(String text) {
        List<TextSegment> segments = new ArrayList<>();

        String[] paragraphs = text.split("\\n\\s*\\n");

        for (String paragraph : paragraphs) {
            if (paragraph.trim().isEmpty())
                continue;

            TextSegment segment = new TextSegment();
            segment.content = paragraph.trim();

            // 检测是否为标题
            Matcher titleMatcher = TITLE_PATTERN.matcher(paragraph);
            if (titleMatcher.find()) {
                segment.isTitle = true;
                segment.confidence = 0.8;
            } else {
                segment.isTitle = false;
                segment.confidence = 0.9;
            }

            segments.add(segment);
        }

        return segments;
    }

    /**
     * 执行OCR（占位实现）
     */
    private String performOCR(BufferedImage image) {
        // 这里应该集成实际的OCR库，如Tesseract
        // 当前返回占位符
        log.debug("OCR处理占位实现，图像尺寸: {}x{}", image.getWidth(), image.getHeight());
        return null;
    }

    /**
     * 生成图像描述
     * 使用视觉LLM模型分析图像内容，生成详细的描述
     */
    private String generateImageDescription(BufferedImage image, ModelRequest modelRequest) {
        StreamingChatModel chatModel = getChatModel(modelRequest.getModelId());
        try {
            // 将图像转换为Base64格式
            String base64Image = imageToBase64(image);
            if (base64Image.isEmpty()) {
                log.warn("图像转换Base64失败，无法生成描述");
                return null;
            }

            // 创建图像内容，使用适当的MIME类型
            String mimeType = "image/png"; // 默认使用PNG格式，确保最佳兼容性
            ImageContent imageContent = ImageContent.from(base64Image, mimeType);

            // 创建提示词，要求AI详细描述图像内容
            TextContent promptText = TextContent.from(
                    "请详细描述这张图片的内容，包括：\n" +
                            "1. 图片的主要内容和对象\n" +
                            "2. 文字信息（如果有的话）\n" +
                            "3. 图表、表格或数据（如果有的话）\n" +
                            "4. 布局和结构信息\n" +
                            "5. 任何其他重要的视觉元素\n" +
                            "请用中文回答，描述要准确、详细但简洁。");

            // 创建用户消息
            UserMessage userMessage = UserMessage.from(promptText, imageContent);

            log.debug("开始使用视觉模型分析图像，尺寸: {}x{}", image.getWidth(), image.getHeight());

            // 调用视觉模型 - 使用同步方式处理流式响应
            ChatRequest chatRequest = getChatRequest(modelRequest)
                    .messages(List.of(userMessage))
                    .build();
            
            // 使用StringBuilder收集流式响应
            StringBuilder responseBuilder = new StringBuilder();
            
            // 创建StreamingChatResponseHandler来收集响应
            StreamingChatResponseHandler responseHandler = new StreamingChatResponseHandler() {
                @Override
                public void onPartialResponse(String partialResponse) {
                    responseBuilder.append(partialResponse);
                }
                
                @Override
                public void onCompleteResponse(ChatResponse response) {
                    // 流式响应完成
                }
                
                @Override
                public void onError(Throwable error) {
                    log.error("流式响应处理错误", error);
                }
            };
            
            // 调用流式模型
            chatModel.chat(chatRequest, responseHandler);
            
            // 获取完整的响应文本
            String description = responseBuilder.toString().trim();
            if (!description.isEmpty()) {
                log.debug("图像描述生成成功，长度: {} 字符", description.length());
                return description;
            } else {
                log.warn("视觉模型返回空结果");
                return null;
            }

        } catch (Exception e) {
            log.error("使用视觉模型生成图像描述时发生错误，图像尺寸: {}x{}",
                    image.getWidth(), image.getHeight(), e);
            return null;
        }
    }

    /**
     * 图片转Base64
     */
    private String imageToBase64(BufferedImage image) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "PNG", baos);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            log.warn("图片转Base64失败", e);
            return "";
        }
    }

    // =================== 数据类 ===================

    /**
     * 文档分析结果
     */
    private static class DocumentAnalysis {
        int pageCount;
        int totalCharacters;
        boolean hasImages;
        int estimatedTables;
        int titleCount;
    }

    /**
     * 页面内容
     */
    private static class PageContent {
        int pageNumber;
        String textContent = "";
        List<ImageInfo> images = new ArrayList<>();
        List<TableInfo> tables = new ArrayList<>();

        PageContent(int pageNumber) {
            this.pageNumber = pageNumber;
        }

        boolean hasText() {
            return !textContent.isEmpty();
        }

        boolean hasImages() {
            return !images.isEmpty();
        }

        boolean hasTables() {
            return !tables.isEmpty();
        }
    }

    /**
     * 图像信息
     */
    private static class ImageInfo {
        int pageNumber;
        int width;
        int height;
        String format;
        String imageData; // Base64编码
        String ocrText;
        String description;
    }

    /**
     * 表格信息
     */
    private static class TableInfo {
        int tableIndex;
        String content;
        int startIndex;
        int endIndex;
        int rowCount;
        int columnCount;
        double confidence;
    }

    /**
     * 文本段落
     */
    private static class TextSegment {
        String content;
        boolean isTitle;
        double confidence;
    }

    @Override
    public String[] getSupportedTypes() {
        return new String[] { "pdf" };
    }

    @Override
    public boolean supports(String extension) {
        return extension != null && "pdf".equalsIgnoreCase(extension);
    }
}