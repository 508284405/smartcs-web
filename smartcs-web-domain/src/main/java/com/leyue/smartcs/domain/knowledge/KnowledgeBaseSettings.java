package com.leyue.smartcs.domain.knowledge;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 知识库设置领域模型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeBaseSettings {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 知识库ID
     */
    private Long knowledgeBaseId;
    
    /**
     * 索引模式：high_quality(高质量)、economy(经济模式)
     */
    private String indexingMode;
    
    /**
     * 嵌入模型名称
     */
    private String embeddingModel;
    
    /**
     * 向量搜索配置
     */
    private VectorSearchSettings vectorSearch;
    
    /**
     * 全文搜索配置
     */
    private FullTextSearchSettings fullTextSearch;
    
    /**
     * 混合搜索配置
     */
    private HybridSearchSettings hybridSearch;
    
    /**
     * 创建者
     */
    private String createdBy;
    
    /**
     * 更新者
     */
    private String updatedBy;
    
    /**
     * 创建时间（毫秒时间戳）
     */
    private Long createdAt;
    
    /**
     * 更新时间（毫秒时间戳）
     */
    private Long updatedAt;
    
    /**
     * 向量搜索设置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VectorSearchSettings {
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
     * 全文搜索设置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FullTextSearchSettings {
        /**
         * 是否启用全文搜索
         */
        private Boolean enabled;
    }
    
    /**
     * 混合搜索设置
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HybridSearchSettings {
        /**
         * 是否启用混合搜索
         */
        private Boolean enabled;
        
        /**
         * 是否启用重排
         */
        private Boolean rerankEnabled;
    }
    
    /**
     * 检查索引模式是否有效
     * @return 是否有效
     */
    public boolean isValidIndexingMode() {
        return "high_quality".equals(indexingMode) || "economy".equals(indexingMode);
    }
    
    /**
     * 检查配置是否有效
     * @return 是否有效
     */
    public boolean isValid() {
        return knowledgeBaseId != null && knowledgeBaseId > 0 
            && isValidIndexingMode()
            && embeddingModel != null && !embeddingModel.trim().isEmpty();
    }
    
    /**
     * 创建默认设置
     * @param knowledgeBaseId 知识库ID
     * @return 默认设置
     */
    public static KnowledgeBaseSettings createDefault(Long knowledgeBaseId) {
        return KnowledgeBaseSettings.builder()
            .knowledgeBaseId(knowledgeBaseId)
            .indexingMode("high_quality")
            .embeddingModel("")
            .vectorSearch(VectorSearchSettings.builder()
                .enabled(true)
                .topK(10)
                .scoreThreshold(BigDecimal.ZERO)
                .build())
            .fullTextSearch(FullTextSearchSettings.builder()
                .enabled(false)
                .build())
            .hybridSearch(HybridSearchSettings.builder()
                .enabled(false)
                .rerankEnabled(false)
                .build())
            .build();
    }
}