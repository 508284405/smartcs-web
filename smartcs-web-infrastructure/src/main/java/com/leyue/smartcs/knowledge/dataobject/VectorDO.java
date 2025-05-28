package com.leyue.smartcs.knowledge.dataobject;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.leyue.smartcs.common.dao.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 向量数据对象，对应t_kb_vector表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_kb_vector")
public class VectorDO extends BaseDO {
    
    /**
     * 切片ID
     */
    @TableField("chunk_id")
    private Long chunkId;
    
    /**
     * 向量数据，float[]序列化后存储
     */
    @TableField("embedding")
    private byte[] embedding;
    
    /**
     * 维度大小
     */
    @TableField("dim")
    private Integer dim;
    
    /**
     * embedding提供方，如openai/bge
     */
    @TableField("provider")
    private String provider;
} 