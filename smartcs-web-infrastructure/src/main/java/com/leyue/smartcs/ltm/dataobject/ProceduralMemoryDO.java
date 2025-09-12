package com.leyue.smartcs.ltm.dataobject;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import lombok.Data;

/**
 * DO: 程序性记忆
 */
@Data
@TableName("t_ltm_procedural_memory")
public class ProceduralMemoryDO {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String patternType;
    private String patternName;
    private String patternDescription;
    private String triggerConditionsJson;
    private String actionTemplate;
    private Integer successCount;
    private Integer failureCount;
    private Double successRate;
    private Long lastTriggeredAt;
    private Double learningRate;
    private Boolean isActive;
    private Long createdAt;
    private Long updatedAt;
}

