package com.leyue.smartcs.knowledge.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Set;

/**
 * 文档类型枚举
 */
@Getter
@AllArgsConstructor
public enum DocumentTypeEnum {
    
    // 文本文档
    TXT("txt", "文本文件", Set.of("txt"), "text/plain"),
    
    // Markdown文档
    MARKDOWN("markdown", "Markdown文档", Set.of("md", "markdown"), "text/markdown"),
    MDX("mdx", "MDX文档", Set.of("mdx"), "text/mdx"),
    
    // PDF文档
    PDF("pdf", "PDF文档", Set.of("pdf"), "application/pdf"),
    
    // HTML文档
    HTML("html", "HTML文档", Set.of("html", "htm"), "text/html"),
    
    // Excel文档
    XLSX("xlsx", "Excel文档(新版)", Set.of("xlsx"), "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
    XLS("xls", "Excel文档(旧版)", Set.of("xls"), "application/vnd.ms-excel"),
    
    // Word文档
    DOCX("docx", "Word文档", Set.of("docx"), "application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
    
    // CSV文档
    CSV("csv", "CSV文档", Set.of("csv"), "text/csv"),
    
    // 字幕文档
    VTT("vtt", "WebVTT字幕文件", Set.of("vtt"), "text/vtt"),
    
    // 属性文件
    PROPERTIES("properties", "属性文件", Set.of("properties"), "text/plain"),
    
    // 未知类型
    UNKNOWN("unknown", "未知类型", Set.of(), "application/octet-stream");
    
    private final String code;
    private final String description;
    private final Set<String> extensions;
    private final String mimeType;
    
    /**
     * 根据文件扩展名获取文档类型
     */
    public static DocumentTypeEnum fromExtension(String extension) {
        if (extension == null || extension.trim().isEmpty()) {
            return UNKNOWN;
        }
        
        String normalizedExt = extension.toLowerCase().trim();
        if (normalizedExt.startsWith(".")) {
            normalizedExt = normalizedExt.substring(1);
        }
        
        final String finalExt = normalizedExt;
        return Arrays.stream(values())
                .filter(type -> type.getExtensions().contains(finalExt))
                .findFirst()
                .orElse(UNKNOWN);
    }
    
    /**
     * 根据MIME类型获取文档类型
     */
    public static DocumentTypeEnum fromMimeType(String mimeType) {
        if (mimeType == null || mimeType.trim().isEmpty()) {
            return UNKNOWN;
        }
        
        return Arrays.stream(values())
                .filter(type -> type.getMimeType().equals(mimeType))
                .findFirst()
                .orElse(UNKNOWN);
    }
    
    /**
     * 根据文件名获取文档类型
     */
    public static DocumentTypeEnum fromFileName(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return UNKNOWN;
        }
        
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < fileName.length() - 1) {
            String extension = fileName.substring(lastDotIndex + 1);
            return fromExtension(extension);
        }
        
        return UNKNOWN;
    }
    
    /**
     * 是否为结构化文档（需要保持结构信息的文档）
     */
    public boolean isStructured() {
        return this == PDF || this == XLSX || this == XLS || this == DOCX || 
               this == HTML || this == CSV;
    }
    
    /**
     * 是否为表格类文档
     */
    public boolean isTabular() {
        return this == XLSX || this == XLS || this == CSV;
    }
    
    /**
     * 是否为标记语言文档
     */
    public boolean isMarkup() {
        return this == MARKDOWN || this == MDX || this == HTML;
    }
    
    /**
     * 是否为纯文本文档
     */
    public boolean isPlainText() {
        return this == TXT || this == PROPERTIES || this == VTT;
    }
}