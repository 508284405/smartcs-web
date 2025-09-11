package com.leyue.smartcs.knowledge.chunking.strategy;

import com.leyue.smartcs.dto.knowledge.ChunkDTO;
import com.leyue.smartcs.knowledge.chunking.ChunkingStrategy;
import com.leyue.smartcs.knowledge.enums.DocumentTypeEnum;
import dev.langchain4j.data.document.Document;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 图像处理分块策略
 * 专门处理文档中的图片内容，包括OCR识别和图像描述
 */
@Component
@Slf4j
public class ImageProcessingStrategy implements ChunkingStrategy {
    
    // 匹配各种图片标记的正则表达式
    private static final Pattern IMG_TAG_PATTERN = Pattern.compile(
            "<img[^>]+src=[\"']([^\"']+)[\"'][^>]*>", Pattern.CASE_INSENSITIVE);
    private static final Pattern MARKDOWN_IMAGE_PATTERN = Pattern.compile(
            "!\\[([^\\]]*)\\]\\(([^)]+)\\)");
    private static final Pattern BASE64_IMAGE_PATTERN = Pattern.compile(
            "data:image/[^;]+;base64,([A-Za-z0-9+/=]+)");
    
    @Override
    public String getName() {
        return "IMAGE_PROCESSING";
    }
    
    @Override
    public String getDescription() {
        return "图像内容处理，支持图片提取、OCR识别和图像描述生成";
    }
    
    @Override
    public List<DocumentTypeEnum> getSupportedDocumentTypes() {
        return List.of(
                DocumentTypeEnum.PDF,
                DocumentTypeEnum.HTML,
                DocumentTypeEnum.MARKDOWN,
                DocumentTypeEnum.MDX,
                DocumentTypeEnum.DOCX
        );
    }
    
    @Override
    public boolean isCombinable() {
        return true;
    }
    
    @Override
    public int getPriority() {
        return 30; // 较高优先级，图像处理应该在文本处理之前
    }
    
    @Override
    public List<ChunkDTO> chunk(List<Document> documents, DocumentTypeEnum documentType, Map<String, Object> config) {
        log.info("执行图像处理分块策略，文档数量: {}, 文档类型: {}", documents.size(), documentType);
        
        List<ChunkDTO> chunks = new ArrayList<>();
        
        // 获取配置参数
        boolean enableOCR = getConfigValue(config, "enableOCR", false);
        boolean enableImageDescription = getConfigValue(config, "enableImageDescription", false);
        boolean extractImageMetadata = getConfigValue(config, "extractImageMetadata", true);
        int maxImageSize = getConfigValue(config, "maxImageSize", 5 * 1024 * 1024); // 5MB
        
        for (int docIndex = 0; docIndex < documents.size(); docIndex++) {
            Document document = documents.get(docIndex);
            
            List<ChunkDTO> imageChunks = processDocumentImages(
                    document, docIndex, documentType, enableOCR, 
                    enableImageDescription, extractImageMetadata, maxImageSize);
            chunks.addAll(imageChunks);
        }
        
        log.info("图像处理分块完成，生成 {} 个图像分块", chunks.size());
        return chunks;
    }
    
    @Override
    public boolean validateConfig(Map<String, Object> config) {
        if (config == null) {
            return true;
        }
        
        Integer maxImageSize = getConfigValue(config, "maxImageSize", null);
        if (maxImageSize != null && (maxImageSize < 1024 || maxImageSize > 50 * 1024 * 1024)) {
            log.warn("最大图像大小超出有效范围 [1KB, 50MB]: {}", maxImageSize);
            return false;
        }
        
        return true;
    }
    
    /**
     * 处理文档中的图像内容
     */
    private List<ChunkDTO> processDocumentImages(Document document, int docIndex, 
                                               DocumentTypeEnum documentType,
                                               boolean enableOCR, boolean enableImageDescription,
                                               boolean extractImageMetadata, int maxImageSize) {
        List<ChunkDTO> chunks = new ArrayList<>();
        String content = document.text();
        
        switch (documentType) {
            case HTML -> chunks.addAll(processHTMLImages(content, docIndex, enableOCR,
                    enableImageDescription, extractImageMetadata, maxImageSize));
            case MARKDOWN, MDX -> chunks.addAll(processMarkdownImages(content, docIndex, enableOCR,
                    enableImageDescription, extractImageMetadata, maxImageSize));
            case PDF -> chunks.addAll(processPDFImages(document, docIndex, enableOCR,
                    enableImageDescription, extractImageMetadata));
            case DOCX -> chunks.addAll(processDocxImages(document, docIndex, enableOCR,
                    enableImageDescription, extractImageMetadata));
            default -> log.debug("文档类型 {} 不包含可处理的图像", documentType);
        }
        
        return chunks;
    }
    
    /**
     * 处理HTML中的图像
     */
    private List<ChunkDTO> processHTMLImages(String content, int docIndex, boolean enableOCR,
                                           boolean enableImageDescription, boolean extractImageMetadata,
                                           int maxImageSize) {
        List<ChunkDTO> chunks = new ArrayList<>();
        Matcher imgMatcher = IMG_TAG_PATTERN.matcher(content);
        int imageIndex = 0;
        
        while (imgMatcher.find()) {
            String imgTag = imgMatcher.group();
            String src = imgMatcher.group(1);
            
            // 提取图像周围的上下文
            String context = extractImageContext(content, imgMatcher.start(), imgMatcher.end());
            
            // 处理图像
            ChunkDTO imageChunk = processImage(src, imgTag, context, docIndex, imageIndex++,
                    DocumentTypeEnum.HTML, enableOCR, enableImageDescription, 
                    extractImageMetadata, maxImageSize);
            
            if (imageChunk != null) {
                chunks.add(imageChunk);
            }
        }
        
        log.debug("HTML图像处理完成，处理了 {} 个图像", imageIndex);
        return chunks;
    }
    
    /**
     * 处理Markdown中的图像
     */
    private List<ChunkDTO> processMarkdownImages(String content, int docIndex, boolean enableOCR,
                                               boolean enableImageDescription, boolean extractImageMetadata,
                                               int maxImageSize) {
        List<ChunkDTO> chunks = new ArrayList<>();
        Matcher imgMatcher = MARKDOWN_IMAGE_PATTERN.matcher(content);
        int imageIndex = 0;
        
        while (imgMatcher.find()) {
            String altText = imgMatcher.group(1);
            String src = imgMatcher.group(2);
            String imgMarkdown = imgMatcher.group();
            
            // 提取图像周围的上下文
            String context = extractImageContext(content, imgMatcher.start(), imgMatcher.end());
            
            // 处理图像
            ChunkDTO imageChunk = processImage(src, imgMarkdown, context, docIndex, imageIndex++,
                    DocumentTypeEnum.MARKDOWN, enableOCR, enableImageDescription,
                    extractImageMetadata, maxImageSize);
            
            if (imageChunk != null) {
                // 添加alt文本到元数据
                addToMetadata(imageChunk, "altText", altText);
                chunks.add(imageChunk);
            }
        }
        
        log.debug("Markdown图像处理完成，处理了 {} 个图像", imageIndex);
        return chunks;
    }
    
    /**
     * 处理PDF中的图像
     */
    private List<ChunkDTO> processPDFImages(Document document, int docIndex, boolean enableOCR,
                                          boolean enableImageDescription, boolean extractImageMetadata) {
        List<ChunkDTO> chunks = new ArrayList<>();
        
        // PDF图像处理需要专门的图像提取库
        // 这里提供框架代码，实际实现需要集成如PDFBox等库
        log.debug("PDF图像提取需要专门的处理逻辑，当前为占位实现");
        
        // 检查文档元数据中是否包含图像信息
        Map<String, Object> metadata = document.metadata().toMap();
        if (metadata.containsKey("hasImages") && Boolean.TRUE.equals(metadata.get("hasImages"))) {
            // 创建一个占位分块，表示文档包含图像
            ChunkDTO placeholder = createImagePlaceholder(docIndex, 0, DocumentTypeEnum.PDF);
            chunks.add(placeholder);
        }
        
        return chunks;
    }
    
    /**
     * 处理DOCX中的图像
     */
    private List<ChunkDTO> processDocxImages(Document document, int docIndex, boolean enableOCR,
                                           boolean enableImageDescription, boolean extractImageMetadata) {
        List<ChunkDTO> chunks = new ArrayList<>();
        
        // DOCX图像处理需要专门的处理逻辑
        log.debug("DOCX图像提取需要专门的处理逻辑，当前为占位实现");
        
        // 检查文档内容中是否有图像标记
        String content = document.text();
        if (content.contains("[图片]") || content.contains("[图像]") || content.contains("[IMAGE]")) {
            ChunkDTO placeholder = createImagePlaceholder(docIndex, 0, DocumentTypeEnum.DOCX);
            chunks.add(placeholder);
        }
        
        return chunks;
    }
    
    /**
     * 处理单个图像
     */
    private ChunkDTO processImage(String src, String originalMarkup, String context,
                                 int docIndex, int imageIndex, DocumentTypeEnum documentType,
                                 boolean enableOCR, boolean enableImageDescription,
                                 boolean extractImageMetadata, int maxImageSize) {
        
        try {
            // 分析图像源
            ImageInfo imageInfo = analyzeImageSource(src, maxImageSize);

            // 创建图像分块
            StringBuilder chunkContent = new StringBuilder();
            chunkContent.append("图像内容:\n");
            chunkContent.append("原始标记: ").append(originalMarkup).append("\n");
            
            if (context != null && !context.trim().isEmpty()) {
                chunkContent.append("上下文: ").append(context.trim()).append("\n");
            }
            
            // OCR处理（如果启用）
            if (enableOCR && imageInfo.isProcessable) {
                String ocrText = performOCR(imageInfo);
                if (ocrText != null && !ocrText.trim().isEmpty()) {
                    chunkContent.append("OCR识别文本: ").append(ocrText).append("\n");
                }
            }
            
            // 图像描述生成（如果启用）
            if (enableImageDescription && imageInfo.isProcessable) {
                String description = generateImageDescription(imageInfo);
                if (description != null && !description.trim().isEmpty()) {
                    chunkContent.append("图像描述: ").append(description).append("\n");
                }
            }
            
            // 创建ChunkDTO

            return createChunkDTO(chunkContent.toString(), docIndex, imageIndex,
                    documentType, Map.of(
                            "imageType", "image",
                            "imageSrc", src,
                            "imageFormat", imageInfo.format,
                            "imageSize", imageInfo.size,
                            "processable", imageInfo.isProcessable
                    ));
            
        } catch (Exception e) {
            log.warn("处理图像时发生错误: {}, 错误: {}", src, e.getMessage());
            return null;
        }
    }
    
    /**
     * 分析图像源信息
     */
    private ImageInfo analyzeImageSource(String src, int maxImageSize) {
        ImageInfo info = new ImageInfo();
        info.src = src;
        
        // 检查是否是Base64图像
        if (src.startsWith("data:image/")) {
            Matcher base64Matcher = BASE64_IMAGE_PATTERN.matcher(src);
            if (base64Matcher.find()) {
                info.isBase64 = true;
                info.format = src.substring(11, src.indexOf(';'));
                
                // 估算Base64图像大小
                String base64Data = base64Matcher.group(1);
                info.size = (int) (base64Data.length() * 0.75); // Base64编码后大小约为原始大小的4/3
                
                info.isProcessable = info.size <= maxImageSize;
            }
        } else {
            // 外部图像URL
            info.isBase64 = false;
            info.format = extractFileExtension(src);
            info.size = -1; // 无法确定大小
            info.isProcessable = true; // 假设可处理，实际需要下载验证
        }
        
        return info;
    }
    
    /**
     * 执行OCR识别（占位实现）
     */
    private String performOCR(ImageInfo imageInfo) {
        // 这里应该集成实际的OCR服务，如Tesseract、云OCR等
        log.debug("OCR处理占位实现，图像: {}", imageInfo.src);
        return null;
    }
    
    /**
     * 生成图像描述（占位实现）
     */
    private String generateImageDescription(ImageInfo imageInfo) {
        // 这里应该集成视觉AI模型，如GPT-4 Vision、Google Vision等
        log.debug("图像描述生成占位实现，图像: {}", imageInfo.src);
        return null;
    }
    
    /**
     * 提取图像周围的上下文
     */
    private String extractImageContext(String content, int imageStart, int imageEnd) {
        int contextLength = 100;
        int contextStart = Math.max(0, imageStart - contextLength);
        int contextEndPos = Math.min(content.length(), imageEnd + contextLength);
        
        String beforeContext = content.substring(contextStart, imageStart).trim();
        String afterContext = content.substring(imageEnd, contextEndPos).trim();
        
        StringBuilder context = new StringBuilder();
        if (!beforeContext.isEmpty()) {
            context.append(beforeContext);
        }
        if (!afterContext.isEmpty()) {
            if (context.length() > 0) context.append(" ");
            context.append(afterContext);
        }
        
        return context.toString();
    }
    
    /**
     * 创建图像占位符分块
     */
    private ChunkDTO createImagePlaceholder(int docIndex, int imageIndex, DocumentTypeEnum documentType) {
        return createChunkDTO("此文档包含图像内容，需要专门的图像处理工具。", 
                docIndex, imageIndex, documentType, Map.of(
                        "imageType", "placeholder",
                        "requiresSpecialProcessing", true
                ));
    }
    
    /**
     * 提取文件扩展名
     */
    private String extractFileExtension(String src) {
        int lastDot = src.lastIndexOf('.');
        int lastSlash = src.lastIndexOf('/');
        int lastQuery = src.lastIndexOf('?');
        
        if (lastDot > lastSlash && (lastQuery == -1 || lastDot < lastQuery)) {
            return src.substring(lastDot + 1, lastQuery == -1 ? src.length() : lastQuery).toLowerCase();
        }
        
        return "unknown";
    }
    
    /**
     * 向元数据中添加信息
     */
    private void addToMetadata(ChunkDTO chunk, String key, String value) {
        // 简化实现，实际应该解析JSON并重新构建
        String metadata = chunk.getMetadata();
        if (metadata.endsWith("}")) {
            metadata = metadata.substring(0, metadata.length() - 1) +
                    ",\"" + key + "\":\"" + value + "\"}";
            chunk.setMetadata(metadata);
        }
    }
    
    /**
     * 创建ChunkDTO对象
     */
    private ChunkDTO createChunkDTO(String content, int docIndex, int imageIndex,
                                   DocumentTypeEnum documentType, Map<String, Object> additionalMetadata) {
        ChunkDTO chunk = new ChunkDTO();
        chunk.setChunkIndex(String.format("%d-img-%d", docIndex, imageIndex));
        chunk.setContent(content);
        
        // 构建元数据
        StringBuilder metadataBuilder = new StringBuilder();
        metadataBuilder.append("{");
        metadataBuilder.append("\"documentType\":\"").append(documentType.name()).append("\",");
        metadataBuilder.append("\"strategy\":\"").append(getName()).append("\",");
        metadataBuilder.append("\"docIndex\":").append(docIndex).append(",");
        metadataBuilder.append("\"imageIndex\":").append(imageIndex);
        
        // 添加额外元数据
        additionalMetadata.forEach((key, value) -> {
            metadataBuilder.append(",");
            metadataBuilder.append("\"").append(key).append("\":\"").append(value).append("\"");
        });
        
        metadataBuilder.append("}");
        chunk.setMetadata(metadataBuilder.toString());
        
        return chunk;
    }
    
    @SuppressWarnings("unchecked")
    private <T> T getConfigValue(Map<String, Object> config, String key, T defaultValue) {
        if (config == null || !config.containsKey(key)) {
            return defaultValue;
        }
        
        try {
            return (T) config.get(key);
        } catch (ClassCastException e) {
            log.warn("配置参数 {} 类型转换失败，使用默认值: {}", key, defaultValue);
            return defaultValue;
        }
    }
    
    /**
     * 图像信息类
     */
    private static class ImageInfo {
        String src;
        String format;
        int size;
        boolean isBase64;
        boolean isProcessable;
    }
}