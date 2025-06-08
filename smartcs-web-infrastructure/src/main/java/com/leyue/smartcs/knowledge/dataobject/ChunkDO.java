package com.leyue.smartcs.knowledge.dataobject;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.leyue.smartcs.common.dao.BaseDO;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 内容切片数据对象，对应t_kb_chunk表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_kb_chunk")
public class ChunkDO extends BaseDO {
    
    /**
     * 内容ID
     */
    @TableField("content_id")
    private Long contentId;
    
    /**
     * 段落序号
     */
    @TableField("chunk_index")
    private String chunkIndex;
    
    /**
     * 切片token数
     */
    @TableField("token_size")
    private Integer tokenSize;
    
    /**
     * 切片内容文本
     */
    @TableField("content")
    private String content;
    
    /**
     * 附加元信息，如页码、起止时间、原始位置等
     */
    @TableField("metadata")
    private String metadata;
} 