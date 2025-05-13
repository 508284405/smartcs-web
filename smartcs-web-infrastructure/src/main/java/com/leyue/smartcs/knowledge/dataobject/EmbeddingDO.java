package com.leyue.smartcs.knowledge.dataobject;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.leyue.smartcs.common.dao.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 向量数据对象，对应cs_doc_embedding表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_cs_doc_embedding")
public class EmbeddingDO extends BaseDO {
    
    /**
     * 文档ID
     */
    @TableField("doc_id")
    private Long docId;
    
    /**
     * 段落序号
     */
    @TableField("section_idx")
    private Integer sectionIdx;
    
    /**
     * 文本片段
     */
    @TableField("content_snip")
    private String contentSnip;
    
    /**
     * 向量数据
     */
    @TableField("vector")
    private byte[] vector;
    
    /**
     * 模型类型
     */
    @TableField("model_type")
    private String modelType;
} 