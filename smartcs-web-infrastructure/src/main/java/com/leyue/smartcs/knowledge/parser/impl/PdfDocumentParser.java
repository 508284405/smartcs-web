package com.leyue.smartcs.knowledge.parser.impl;

import com.leyue.smartcs.knowledge.parser.DocumentParser;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

/**
 * PDF文档解析器
 * 支持文本提取、图片提取和表格识别
 */
@Slf4j
@Component
public class PdfDocumentParser implements DocumentParser {
    
    @Override
    public List<Document> parse(Resource resource, String fileName) throws IOException {
        List<Document> documents = new ArrayList<>();
        
        try (InputStream inputStream = resource.getInputStream();
             PDDocument document = PDDocument.load(inputStream)) {
            
            // 1. 提取全文内容
            PDFTextStripper textStripper = new PDFTextStripper();
            String fullText = textStripper.getText(document);
            
            // 创建主文档
            Metadata mainMetadata = Metadata.from("type", "pdf_main")
                    .add("fileName", fileName)
                    .add("pageCount", String.valueOf(document.getNumberOfPages()))
                    .add("hasImages", String.valueOf(hasImages(document)));
                    
            documents.add(Document.from(fullText, mainMetadata));
            
            // 2. 按页面分块提取
            PDPageTree pages = document.getPages();
            int pageIndex = 0;
            
            for (PDPage page : pages) {
                pageIndex++;
                
                // 提取页面文本
                textStripper.setStartPage(pageIndex);
                textStripper.setEndPage(pageIndex);
                String pageText = textStripper.getText(document);
                
                if (pageText != null && !pageText.trim().isEmpty()) {
                    Metadata pageMetadata = Metadata.from("type", "pdf_page")
                            .add("fileName", fileName)
                            .add("pageNumber", String.valueOf(pageIndex))
                            .add("totalPages", String.valueOf(document.getNumberOfPages()));
                    
                    documents.add(Document.from(pageText, pageMetadata));
                }
                
                // 3. 提取页面中的图片（如果有）
                extractPageImages(page, pageIndex, fileName, documents);
            }
            
            log.info("PDF解析完成，文件: {}，总页数: {}，生成文档数: {}", 
                    fileName, document.getNumberOfPages(), documents.size());
                    
        } catch (Exception e) {
            log.error("PDF文档解析失败: {}", fileName, e);
            throw new IOException("PDF文档解析失败: " + e.getMessage(), e);
        }
        
        return documents;
    }
    
    /**
     * 提取页面中的图片
     */
    private void extractPageImages(PDPage page, int pageIndex, String fileName, List<Document> documents) {
        try {
            // 简化的图片提取实现
            // 在实际应用中可以使用更复杂的图片提取算法
            var resources = page.getResources();
            if (resources != null && resources.getXObjectNames() != null) {
                for (var name : resources.getXObjectNames()) {
                    try {
                        var xObject = resources.getXObject(name);
                        if (xObject instanceof PDImageXObject) {
                            PDImageXObject image = (PDImageXObject) xObject;
                            
                            // 转换图片为Base64（用于存储）
                            BufferedImage bufferedImage = image.getImage();
                            String imageBase64 = imageToBase64(bufferedImage);
                            
                            // 创建图片文档
                            Metadata imageMetadata = Metadata.from("type", "pdf_image")
                                    .add("fileName", fileName)
                                    .add("pageNumber", String.valueOf(pageIndex))
                                    .add("imageFormat", image.getSuffix())
                                    .add("width", String.valueOf(image.getWidth()))
                                    .add("height", String.valueOf(image.getHeight()));
                            
                            String imageContent = String.format("[图片：第%d页，格式：%s，尺寸：%dx%d]\n图片数据：%s", 
                                    pageIndex, image.getSuffix(), 
                                    image.getWidth(), image.getHeight(), 
                                    imageBase64.substring(0, Math.min(100, imageBase64.length())) + "...");
                            
                            documents.add(Document.from(imageContent, imageMetadata));
                        }
                    } catch (Exception e) {
                        log.warn("提取页面图片失败，页面: {}，图片: {}", pageIndex, name, e);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("提取页面{}的图片时出错", pageIndex, e);
        }
    }
    
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
    
    @Override
    public String[] getSupportedTypes() {
        return new String[]{"pdf"};
    }
    
    @Override
    public boolean supports(String extension) {
        return Arrays.asList(getSupportedTypes()).contains(extension.toLowerCase());
    }
}