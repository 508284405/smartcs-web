package com.leyue.smartcs.knowledge.dataobject;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.leyue.smartcs.common.dao.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 知识内容数据对象，对应t_kb_content表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_kb_content")
public class ContentDO extends BaseDO {
    
    /**
     * 所属知识库ID
     */
    @TableField("knowledge_base_id")
    private Long knowledgeBaseId;
    
    /**
     * 标题
     */
    @TableField("title")
    private String title;
    
    /**
     * 内容类型 document/audio/video
     */
    @TableField("content_type")
    private String contentType;
    
    /**
     * 原始文件地址
     */
    @TableField("file_url")
    private String fileUrl;

    /**
     * 文件扩展名
     */
    @TableField("file_type")
    private String fileType;

    /**
     * 提取后的原始文本
     */
    @TableField("text_extracted")
    private String textExtracted;

    /**
     * 状态 /enabled/disabled
     */
    @TableField("status")
    private String status;

    /**
     * 分段模式 general/parent_child
     */
    @TableField("segment_mode")
    private String segmentMode;

    /**
     * 字符数
     */
    @TableField("char_count")
    private Long charCount;

    /**
     * 召回次数
     */
    @TableField("recall_count")
    private Long recallCount;

    /**
     * 元数据信息（JSON格式）
     */
    @TableField("metadata")
    private String metadata;

    /**
     * 原始文件名称
     */
    @TableField("original_file_name")
    private String originalFileName;

    /**
     * 文件大小（字节）
     */
    @TableField("file_size")
    private Long fileSize;

    /**
     * 来源 upload/api/import
     */
    @TableField("source")
    private String source;

    /**
     * 处理时间（毫秒）
     */
    @TableField("processing_time")
    private Long processingTime;

    /**
     * 向量化时间（毫秒）
     */
    @TableField("embedding_time")
    private Long embeddingTime;

    /**
     * 嵌入成本（tokens）
     */
    @TableField("embedding_cost")
    private Long embeddingCost;

    /**
     * 平均段落长度
     */
    @TableField("average_chunk_length")
    private Integer averageChunkLength;

    /**
     * 段落数量
     */
    @TableField("chunk_count")
    private Integer chunkCount;

    /**
     * 处理状态 processing/success/failed
     */
    @TableField("processing_status")
    private String processingStatus;

    /**
     * 处理错误信息
     */
    @TableField("processing_error_message")
    private String processingErrorMessage;
} 