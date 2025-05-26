package com.leyue.smartcs.dto.bot;

import com.alibaba.cola.dto.Query;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 获取机器人配置查询
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BotProfileGetQry extends Query {
    
    /**
     * 机器人ID
     */
    private Long botId;
} 