package com.leyue.smartcs.bot.dto;

import com.alibaba.cola.dto.Command;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.NotBlank;

/**
 * Bot Prompt模板创建命令
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BotPromptTemplateCreateCmd extends Command {
    
    /**
     * 模板标识
     */
    @NotBlank(message = "模板标识不能为空")
    private String templateKey;
    
    /**
     * 模板内容
     */
    @NotBlank(message = "模板内容不能为空")
    private String templateContent;
} 