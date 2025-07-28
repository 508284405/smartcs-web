package com.leyue.smartcs.dto.knowledge;

import com.alibaba.cola.dto.Command;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文档处理命令
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DocumentProcessCmd extends Command {
    
    /**
     * 知识库ID
     */
    private Long knowledgeBaseId;
    
    /**
     * 文档标题
     */
    private String title;
    
    /**
     * 文件URL
     */
    private String fileUrl;
    
    /**
     * 文件类型
     */
    private String fileType;
    
    /**
     * 文件大小
     */
    private Long fileSize;
    
    /**
     * 分段模式 general/parent_child
     */
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
    private String indexMethod;
    
    /**
     * 检索设置
     */
    private RetrievalSettings retrievalSettings;
    
    /**
     * 是否为编辑模式
     */
    private Boolean editMode = false;
    
    /**
     * 编辑模式下的原始数据
     */
    private ContentDTO editData;
    
    /**
     * 通用分段设置
     */
    @Data
    public static class SegmentSettings {
        private String identifier;
        private Integer maxLength;
        private Integer overlapLength;
        private Boolean replaceConsecutiveSpaces;
        private Boolean removeAllUrls;
        private Boolean useQASegmentation;
        private String qaLanguage;
    }
    
    /**
     * 父子分段设置
     */
    @Data
    public static class ParentChildSettings {
        private String parentIdentifier;
        private Integer parentMaxLength;
        private String childIdentifier;
        private Integer childMaxLength;
        private Boolean replaceConsecutiveSpaces;
        private Boolean removeAllUrls;
    }
    
    /**
     * 检索设置
     */
    @Data
    public static class RetrievalSettings {
        private String method;
        private String rerankModel;
        private Integer topK;
        private Double scoreThreshold;
        private Boolean fullTextSearch;
        private Boolean hybridSearch;
    }
} 