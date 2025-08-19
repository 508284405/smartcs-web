package com.leyue.smartcs.dto.knowledge;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 知识库设置DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeBaseSettingsDTO {
    
    /**
     * 知识库ID
     */
    private Long id;
    
    /**
     * 知识库名称
     */
    private String name;
    
    /**
     * 描述信息
     */
    private String description;
    
    /**
     * 可见性 public/private
     */
    private String visibility;
    
    /**
     * 索引模式：high_quality(高质量)、economy(经济模式)
     */
    private String indexingMode;
    
    /**
     * 嵌入模型名称
     */
    private String embeddingModel;
    
    /**
     * 检索设置
     */
    private RetrievalSettingsDTO retrievalSettings;
    
    /**
     * 检索设置DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RetrievalSettingsDTO {
        
        /**
         * 向量搜索配置
         */
        private VectorSearchDTO vectorSearch;
        
        /**
         * 全文搜索配置
         */
        private FullTextSearchDTO fullTextSearch;
        
        /**
         * 混合搜索配置
         */
        private HybridSearchDTO hybridSearch;
    }
    
    /**
     * 向量搜索DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VectorSearchDTO {
        
        /**
         * 是否启用向量搜索
         */
        private Boolean enabled;
        
        /**
         * 向量搜索返回条数
         */
        private Integer topK;
        
        /**
         * 向量搜索相似度阈值
         */
        private BigDecimal scoreThreshold;
    }
    
    /**
     * 全文搜索DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FullTextSearchDTO {
        
        /**
         * 是否启用全文搜索
         */
        private Boolean enabled;
    }
    
    /**
     * 混合搜索DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HybridSearchDTO {
        
        /**
         * 是否启用混合搜索
         */
        private Boolean enabled;
        
        /**
         * 是否启用重排
         */
        private Boolean rerankEnabled;
    }
}