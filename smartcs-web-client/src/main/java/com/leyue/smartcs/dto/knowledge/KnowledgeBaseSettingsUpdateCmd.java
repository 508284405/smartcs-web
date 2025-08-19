package com.leyue.smartcs.dto.knowledge;

import com.alibaba.cola.dto.Command;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 知识库设置更新命令
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class KnowledgeBaseSettingsUpdateCmd extends Command {
    
    /**
     * 知识库ID
     */
    @NotNull(message = "知识库ID不能为空")
    private Long id;
    
    /**
     * 知识库名称
     */
    @Size(max = 128, message = "知识库名称长度不能超过128个字符")
    private String name;
    
    /**
     * 描述信息
     */
    private String description;
    
    /**
     * 可见性 public/private
     */
    @Pattern(regexp = "^(public|private)$", message = "可见性只能是public或private")
    private String visibility;
    
    /**
     * 索引模式：high_quality(高质量)、economy(经济模式)
     */
    @NotNull(message = "索引模式不能为空")
    @Pattern(regexp = "^(high_quality|economy)$", message = "索引模式只能是high_quality或economy")
    private String indexingMode;
    
    /**
     * 嵌入模型名称
     */
    @NotNull(message = "嵌入模型不能为空")
    @Size(max = 128, message = "嵌入模型名称长度不能超过128个字符")
    private String embeddingModel;
    
    /**
     * 检索设置
     */
    @Valid
    @NotNull(message = "检索设置不能为空")
    private RetrievalSettingsUpdateDTO retrievalSettings;
    
    /**
     * 检索设置更新DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RetrievalSettingsUpdateDTO {
        
        /**
         * 向量搜索配置
         */
        @Valid
        @NotNull(message = "向量搜索设置不能为空")
        private VectorSearchUpdateDTO vectorSearch;
        
        /**
         * 全文搜索配置
         */
        @Valid
        @NotNull(message = "全文搜索设置不能为空")
        private FullTextSearchUpdateDTO fullTextSearch;
        
        /**
         * 混合搜索配置
         */
        @Valid
        @NotNull(message = "混合搜索设置不能为空")
        private HybridSearchUpdateDTO hybridSearch;
    }
    
    /**
     * 向量搜索更新DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VectorSearchUpdateDTO {
        
        /**
         * 是否启用向量搜索
         */
        @NotNull(message = "向量搜索启用状态不能为空")
        private Boolean enabled;
        
        /**
         * 向量搜索返回条数
         */
        @NotNull(message = "向量搜索返回条数不能为空")
        private Integer topK;
        
        /**
         * 向量搜索相似度阈值
         */
        @NotNull(message = "向量搜索相似度阈值不能为空")
        private BigDecimal scoreThreshold;
    }
    
    /**
     * 全文搜索更新DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FullTextSearchUpdateDTO {
        
        /**
         * 是否启用全文搜索
         */
        @NotNull(message = "全文搜索启用状态不能为空")
        private Boolean enabled;
    }
    
    /**
     * 混合搜索更新DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HybridSearchUpdateDTO {
        
        /**
         * 是否启用混合搜索
         */
        @NotNull(message = "混合搜索启用状态不能为空")
        private Boolean enabled;
        
        /**
         * 是否启用重排
         */
        @NotNull(message = "混合搜索重排启用状态不能为空")
        private Boolean rerankEnabled;
    }
}