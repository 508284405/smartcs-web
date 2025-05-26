package com.leyue.smartcs.dto.bot;

import com.alibaba.cola.dto.Command;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 更新机器人配置命令
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BotProfileUpdateCmd extends Command {
    
    /**
     * 机器人ID
     */
    @NotNull(message = "botId cannot be null")
    private Long botId;
    
    /**
     * 机器人名称
     */
    private String botName;
    
    /**
     * 使用的 LLM / 模型标识，如 gpt-4o、bge-large
     */
    private String modelName;
    
    /**
     * 默认 Prompt 模板 key，关联 bot_prompt_template
     */
    private String promptKey;
    
    /**
     * 该 Bot 对外允许的最大 QPS
     */
    private Integer maxQps;
    
    /**
     * LLM 采样温度
     */
    private BigDecimal temperature;
    
    /**
     * 额外配置（如系统指令、插件开关等）
     */
    private String extraConfig;
} 