package com.leyue.smartcs.ltm.dataobject;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import lombok.Data;

/**
 * DO: 情景记忆
 */
@Data
@TableName("t_ltm_episodic_memory")
public class EpisodicMemoryDO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long sessionId;
    private String episodeId;
    private String content;
    private byte[] embeddingVector;
    private String contextJson;
    private Long timestamp;
    private Double importanceScore;
    private Integer accessCount;
    private Long lastAccessedAt;
    private Integer consolidationStatus;
    private Long createdAt;
    private Long updatedAt;
}

