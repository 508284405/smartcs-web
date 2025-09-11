package com.leyue.smartcs.dto.knowledge;

import com.alibaba.cola.dto.Command;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.Valid;
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
    @NotNull(message = "知识库ID不能为空")
    private Long knowledgeBaseId;
    
    /**
     * 模型ID（用于嵌入模型）
     */
    @NotNull(message = "模型ID不能为空")
    private Long modelId;
    
    /**
     * 文档标题
     */
    @NotBlank(message = "文档标题不能为空")
    @Size(max = 256, message = "文档标题长度不能超过256个字符")
    private String title;
    
    /**
     * 文件URL
     */
    @NotBlank(message = "文件地址不能为空")
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
     * 原始文件名称
     */
    private String originalFileName;
    
    /**
     * 来源 upload/api/import
     */
    private String source = "upload";
    
    /**
     * 元数据信息（JSON格式）
     */
    private String metadata;
    
    /**
     * 分段模式 general/parent_child
     */
    @NotBlank(message = "分段模式不能为空")
    @Pattern(regexp = "^(general|parent_child)$", message = "分段模式只能是general或parent_child")
    private String segmentMode;
    
    /**
     * 通用分段设置
     */
    @Valid
    private SegmentSettings segmentSettings;
    
    /**
     * 父子分段设置
     */
    @Valid
    private ParentChildSettings parentChildSettings;
    
    /**
     * 索引方式
     */
    private String indexMethod;
    
    /**
     * 检索设置
     */
    @Valid
    private RetrievalSettings retrievalSettings;
    
    /**
     * 是否为编辑模式
     */
    private Boolean editMode = false;
    
    /**
     * 编辑模式下的原始数据
     */
    @Valid
    private ContentDTO editData;
    
    /**
     * 通用分段设置
     */
    @Data
    public static class SegmentSettings {
        private String identifier;
        
        @NotNull(message = "分段最大长度不能为空")
        @Size(min = 50, max = 5000, message = "分段最大长度必须在50-5000之间")
        private Integer maxLength;
        
        @Size(min = 0, max = 1000, message = "分段重叠长度必须在0-1000之间")
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
        
        @NotNull(message = "父块最大长度不能为空")
        @Size(min = 100, max = 10000, message = "父块最大长度必须在100-10000之间")
        private Integer parentMaxLength;
        
        private String childIdentifier;
        
        @NotNull(message = "子块最大长度不能为空")
        @Size(min = 50, max = 5000, message = "子块最大长度必须在50-5000之间")
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
        
        @Size(min = 1, max = 100, message = "topK值必须在1-100之间")
        private Integer topK;
        
        @Size(min = 0, max = 1, message = "分数阈值必须在0-1之间")
        private Double scoreThreshold;
        
        private Boolean fullTextSearch;
        private Boolean hybridSearch;
    }
} 