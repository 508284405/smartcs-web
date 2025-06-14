package com.leyue.smartcs.dto.bot;

import com.alibaba.cola.dto.Command;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Bot Prompt模板更新命令
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BotPromptTemplateUpdateCmd extends Command {
    
    /**
     * 模板ID
     */
    @NotNull(message = "模板ID不能为空")
    private Long id;
    
    /**
     * 模板内容
     */
    @NotBlank(message = "模板内容不能为空")
    private String templateContent;
} 