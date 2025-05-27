package com.leyue.smartcs.dto.bot;

import com.alibaba.cola.dto.Command;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 机器人配置启用禁用命令
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BotProfileEnableCmd extends Command {
    
    /**
     * 机器人ID
     */
    @NotNull(message = "机器人ID不能为空")
    private Long botId;
    
    /**
     * 是否启用
     */
    @NotNull(message = "启用状态不能为空")
    private Boolean enabled;
} 