package com.leyue.smartcs.knowledge.factory;

import com.leyue.smartcs.knowledge.enums.DocumentTypeEnum;
import com.leyue.smartcs.knowledge.parser.factory.DocumentParserFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 分块策略工厂
 * 根据文档类型和参数选择合适的策略
 */
@Component
@Slf4j
public class ChunkingStrategyFactory {

    /**
     * 根据文档类型获取默认策略配置
     * 
     * @param documentType 文档类型
     * @return 策略配置
     */
    public Map<String, Object> getDefaultConfigForDocumentType(DocumentTypeEnum documentType) {
        Map<String, Object> config = new HashMap<>();
        
        switch (documentType) {
            case PDF:
                config.put("chunkSize", 800);
                config.put("overlapSize", 150);
                config.put("preserveSentences", true);
                config.put("stripWhitespace", true);
                config.put("removeAllUrls", false);
                break;
                
            case HTML:
                config.put("chunkSize", 600);
                config.put("overlapSize", 100);
                config.put("preserveSentences", true);
                config.put("stripWhitespace", true);
                config.put("removeAllUrls", true);
                break;
                
            case MARKDOWN:
            case MDX:
                config.put("chunkSize", 700);
                config.put("overlapSize", 120);
                config.put("preserveSentences", true);
                config.put("stripWhitespace", true);
                config.put("removeAllUrls", false);
                break;
                
            case DOCX:
                config.put("chunkSize", 750);
                config.put("overlapSize", 130);
                config.put("preserveSentences", true);
                config.put("stripWhitespace", true);
                config.put("removeAllUrls", false);
                break;
                
            case TXT:
            default:
                config.put("chunkSize", 1000);
                config.put("overlapSize", 200);
                config.put("preserveSentences", true);
                config.put("stripWhitespace", true);
                config.put("removeAllUrls", false);
                break;
        }
        
        // 通用配置
        config.put("chunkSeparator", "\n\n");
        config.put("minChunkSize", 10);
        config.put("maxChunkSize", 5000);
        config.put("keepSeparator", true);
        config.put("useQASegmentation", false);
        config.put("qaLanguage", "Chinese");
        
        return config;
    }
    
    /**
     * 合并用户配置和默认配置
     * 
     * @param userConfig 用户配置
     * @param documentType 文档类型
     * @return 合并后的配置
     */
    public Map<String, Object> mergeConfig(Map<String, Object> userConfig, DocumentTypeEnum documentType) {
        Map<String, Object> defaultConfig = getDefaultConfigForDocumentType(documentType);
        Map<String, Object> mergedConfig = new HashMap<>(defaultConfig);
        
        if (userConfig != null) {
            mergedConfig.putAll(userConfig);
        }
        
        return mergedConfig;
    }
    
    /**
     * 验证配置是否适合文档类型
     * 
     * @param config 配置
     * @param documentType 文档类型
     * @return 是否有效
     */
    public boolean validateConfigForDocumentType(Map<String, Object> config, DocumentTypeEnum documentType) {
        if (config == null) {
            return true;
        }
        
        // 基础验证
        Integer chunkSize = (Integer) config.get("chunkSize");
        if (chunkSize != null && (chunkSize < 50 || chunkSize > 10000)) {
            log.warn("分块大小超出有效范围 [50, 10000]: {}", chunkSize);
            return false;
        }
        
        Integer overlapSize = (Integer) config.get("overlapSize");
        if (overlapSize != null && (overlapSize < 0 || overlapSize > 2000)) {
            log.warn("重叠大小超出有效范围 [0, 2000]: {}", overlapSize);
            return false;
        }
        
        // 文档类型特定验证
        switch (documentType) {
            case PDF:
                // PDF文档通常需要较大的分块大小
                if (chunkSize != null && chunkSize < 500) {
                    log.warn("PDF文档建议分块大小不小于500，当前值: {}", chunkSize);
                }
                break;
                
            case HTML:
                // HTML文档通常包含较多标签，需要较小的分块大小
                if (chunkSize != null && chunkSize > 1000) {
                    log.warn("HTML文档建议分块大小不大于1000，当前值: {}", chunkSize);
                }
                break;
                
            case MARKDOWN:
            case MDX:
                // Markdown文档结构清晰，可以使用中等分块大小
                if (chunkSize != null && (chunkSize < 400 || chunkSize > 1200)) {
                    log.warn("Markdown文档建议分块大小在400-1200之间，当前值: {}", chunkSize);
                }
                break;
        }
        
        return true;
    }
    
    /**
     * 获取推荐的分块策略
     * 
     * @param documentType 文档类型
     * @return 推荐策略名称
     */
    public String getRecommendedStrategy(DocumentTypeEnum documentType) {
        switch (documentType) {
            case PDF:
                return "TEXT_CONTENT"; // 可能需要结合图像处理
            case HTML:
                return "TEXT_CONTENT"; // 可能需要结合表格处理
            case MARKDOWN:
            case MDX:
                return "TEXT_CONTENT";
            case DOCX:
                return "TEXT_CONTENT";
            case TXT:
            default:
                return "TEXT_CONTENT";
        }
    }
} 