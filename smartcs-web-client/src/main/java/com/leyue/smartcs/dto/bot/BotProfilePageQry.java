package com.leyue.smartcs.dto.bot;

import com.alibaba.cola.dto.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 机器人配置分页查询
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BotProfilePageQry extends PageQuery {
    
    /**
     * 机器人名称（模糊查询）
     */
    private String botName;
    
    /**
     * 模型名称（模糊查询）
     */
    private String modelName;
    
    /**
     * 创建时间开始
     */
    private Long startTime;
    
    /**
     * 创建时间结束
     */
    private Long endTime;
} 