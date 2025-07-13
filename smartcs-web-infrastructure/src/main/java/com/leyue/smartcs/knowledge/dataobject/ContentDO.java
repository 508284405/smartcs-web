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
     * 状态 uploaded/parsed/vectorized/enabled/disabled
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
} 