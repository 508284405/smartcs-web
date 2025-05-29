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
     * 状态 uploaded/parsed/vectorized
     */
    @TableField("status")
    private String status;
} 