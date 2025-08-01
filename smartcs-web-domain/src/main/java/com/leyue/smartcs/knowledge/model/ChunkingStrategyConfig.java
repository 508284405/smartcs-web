package com.leyue.smartcs.knowledge.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分块策略配置对象
 * 用于替代Map<String, Object>形式的配置参数，提高代码可读性和类型安全性
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChunkingStrategyConfig {
    
    /**
     * 分块大小
     */
    @Builder.Default
    private Integer chunkSize = 1000;
    
    /**
     * 重叠大小
     */
    @Builder.Default
    private Integer overlapSize = 200;
    
    /**
     * 分块分隔符
     */
    @Builder.Default
    private String chunkSeparator = "\n\n";
    
    /**
     * 最小分块大小
     */
    @Builder.Default
    private Integer minChunkSize = 10;
    
    /**
     * 最大分块大小
     */
    @Builder.Default
    private Integer maxChunkSize = 5000;
    
    /**
     * 是否保留分隔符
     */
    @Builder.Default
    private Boolean keepSeparator = true;
    
    /**
     * 是否去除空白字符
     */
    @Builder.Default
    private Boolean stripWhitespace = true;
    
    /**
     * 是否移除所有URL
     */
    @Builder.Default
    private Boolean removeAllUrls = false;
    
    /**
     * 是否使用QA分段
     */
    @Builder.Default
    private Boolean useQASegmentation = false;
    
    /**
     * QA语言
     */
    @Builder.Default
    private String qaLanguage = "Chinese";
    
    /**
     * 是否保留句子完整性
     */
    @Builder.Default
    private Boolean preserveSentences = true;
    
    /**
     * 验证配置参数的有效性
     * 
     * @return 如果配置有效返回true，否则返回false
     */
    public boolean isValid() {
        // 验证分块大小
        if (chunkSize != null && (chunkSize < 50 || chunkSize > 10000)) {
            return false;
        }
        
        // 验证重叠大小
        if (overlapSize != null && (overlapSize < 0 || overlapSize > 2000)) {
            return false;
        }
        
        // 验证最小分块大小
        if (minChunkSize != null && minChunkSize < 10) {
            return false;
        }
        
        // 验证最大分块大小
        if (maxChunkSize != null && maxChunkSize < 100) {
            return false;
        }
        
        // 验证最小和最大分块大小的关系
        if (minChunkSize != null && maxChunkSize != null && minChunkSize > maxChunkSize) {
            return false;
        }
        
        return true;
    }
}