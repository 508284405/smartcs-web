package com.leyue.smartcs.dto.bot;

import com.alibaba.cola.dto.Command;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 删除机器人配置命令
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BotProfileDeleteCmd extends Command {
    
    /**
     * 机器人ID
     */
    private Long botId;
} 