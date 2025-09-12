package com.leyue.smartcs.ltm.dataobject;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import lombok.Data;

/**
 * DO: 语义记忆
 */
@Data
@TableName("t_ltm_semantic_memory")
public class SemanticMemoryDO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String concept;
    private String knowledge;
    private byte[] embeddingVector;
    private Double confidence;
    private String sourceEpisodesJson;
    private Integer evidenceCount;
    private Integer contradictionCount;
    private Long lastReinforcedAt;
    private Double decayRate;
    private Long createdAt;
    private Long updatedAt;
}

