package com.leyue.smartcs.knowledge.dataobject;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.leyue.smartcs.common.dao.BaseDO;
import com.leyue.smartcs.domain.knowledge.enums.StrategyNameEnum;

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
    private Integer chunkIndex;
    
    /**
     * 该段文本内容
     */
    @TableField("text")
    private String text;
    
    /**
     * 切片token数
     */
    @TableField("token_size")
    private Integer tokenSize;
    
    /**
     * 向量数据库中的ID（如Milvus主键）
     */
    @TableField("vector_id")
    private String vectorId;
    
    /**
     * 附加元信息，如页码、起止时间、原始位置等
     */
    @TableField("metadata")
    private String metadata;

    /**
     * 解析策略名称，用于指定文档解析方式
     */
    @TableField("strategy_name")
    private StrategyNameEnum strategyName;
} 