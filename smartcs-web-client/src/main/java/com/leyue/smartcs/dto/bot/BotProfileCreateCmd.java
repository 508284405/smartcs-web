package com.leyue.smartcs.dto.bot;

import com.alibaba.cola.dto.Command;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 创建机器人配置命令
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BotProfileCreateCmd extends Command {
    
    /**
     * 机器人名称
     */
    @NotEmpty(message = "机器人名称不能为空")
    private String botName;
    
    /**
     * 使用的 LLM / 模型标识，如 gpt-4o、bge-large
     */
    @NotEmpty(message = "LLM/模型标识不能为空")
    private String modelName;
    
    /**
     * 默认 Prompt 模板 key，关联 bot_prompt_template
     */
    @NotEmpty(message = "默认 Prompt 模板 key 不能为空")
    private String promptKey;
    
    /**
     * 该 Bot 对外允许的最大 QPS
     */
    private Integer maxQps;
    
    /**
     * LLM 采样温度
     */
    private BigDecimal temperature = BigDecimal.valueOf(0.7);
    
    /**
     * 额外配置（如系统指令、插件开关等）
     */
    private String extraConfig;
} 