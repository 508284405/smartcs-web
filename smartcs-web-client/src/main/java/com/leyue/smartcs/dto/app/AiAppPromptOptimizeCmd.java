package com.leyue.smartcs.dto.app;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * AI应用Prompt优化命令
 */
@Data
public class AiAppPromptOptimizeCmd {
    
    /**
     * 应用ID
     */
    @NotNull(message = "应用ID不能为空")
    private Long appId;
    
    /**
     * 原始prompt内容
     */
    @NotBlank(message = "原始prompt内容不能为空")
    @Size(max = 4000, message = "prompt内容长度不能超过4000个字符")
    private String originalPrompt;
    
    /**
     * 优化目标描述
     */
    @Size(max = 500, message = "优化目标描述长度不能超过500个字符")
    private String optimizeGoal;
    
    /**
     * 使用的模型ID（可选，不指定则使用默认模型）
     */
    private Long modelId;
}