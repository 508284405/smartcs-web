package com.leyue.smartcs.dto.knowledge;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 内容处理命令对象
 * 用于文档的完整处理流程，包括分块和向量化
 */
@Data
public class ContentProcessCmd {
    
    /**
     * 知识库ID
     */
    @NotNull(message = "知识库ID不能为空")
    private Long knowledgeBaseId;
    
    /**
     * 模型ID（用于嵌入模型）
     */
    @NotNull(message = "模型ID不能为空")
    private Long modelId;
    
    /**
     * 文件URL列表
     */
    @NotNull(message = "文件列表不能为空")
    private List<String> files;
    
    /**
     * 分段模式：general-通用分段，parent_child-父子分段
     */
    @NotNull(message = "分段模式不能为空")
    private String segmentMode;
    
    /**
     * 通用分段设置
     */
    private SegmentSettings segmentSettings;
    
    /**
     * 父子分段设置
     */
    private ParentChildSettings parentChildSettings;
    
    /**
     * 索引方式
     */
    private String indexMethod = "high_quality";
    
    /**
     * 检索设置
     */
    private RetrievalSettings retrievalSettings;
    
    /**
     * 是否编辑模式
     */
    private Boolean editMode = false;
    
    /**
     * 编辑数据（编辑模式时使用）
     */
    private Object editData;
    
    /**
     * 通用分段设置
     */
    @Data
    public static class SegmentSettings {
        /**
         * 分段标识符
         */
        private String identifier = "\n\n";
        
        /**
         * 分段最大长度
         */
        private Integer maxLength = 500;
        
        /**
         * 分段重叠长度
         */
        private Integer overlapLength = 50;
        
        /**
         * 替换连续空格
         */
        private Boolean replaceConsecutiveSpaces = true;
        
        /**
         * 删除所有URL
         */
        private Boolean removeAllUrls = false;
        
        /**
         * 使用Q&A分段
         */
        private Boolean useQASegmentation = false;
        
        /**
         * Q&A语言
         */
        private String qaLanguage = "Chinese";
    }
    
    /**
     * 父子分段设置
     */
    @Data
    public static class ParentChildSettings {
        /**
         * 父块分段标识符
         */
        private String parentIdentifier = "\n\n";
        
        /**
         * 父块最大长度
         */
        private Integer parentMaxLength = 500;
        
        /**
         * 子块分段标识符
         */
        private String childIdentifier = "\n";
        
        /**
         * 子块最大长度
         */
        private Integer childMaxLength = 200;
        
        /**
         * 替换连续空格
         */
        private Boolean replaceConsecutiveSpaces = true;
        
        /**
         * 删除所有URL
         */
        private Boolean removeAllUrls = false;
    }
    
    /**
     * 检索设置
     */
    @Data
    public static class RetrievalSettings {
        /**
         * 检索方法
         */
        private String method = "vector_search";
        
        /**
         * 重排模型
         */
        private String rerankModel = "gte-rerank";
        
        /**
         * Top K
         */
        private Integer topK = 3;
        
        /**
         * 分数阈值
         */
        private Double scoreThreshold = 0.5;
        
        /**
         * 全文检索
         */
        private Boolean fullTextSearch = false;
        
        /**
         * 混合检索
         */
        private Boolean hybridSearch = false;
    }
}