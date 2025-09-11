package com.leyue.smartcs.dto.app;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * AI应用Prompt优化响应
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AiAppPromptOptimizeResponse {
    
    /**
     * 原始prompt
     */
    private String originalPrompt;
    
    /**
     * 优化后的prompt
     */
    private String optimizedPrompt;
    
    /**
     * 优化说明
     */
    private String optimizeExplanation;
    
    /**
     * 使用的模型名称
     */
    private String modelName;
    
    /**
     * 优化任务ID
     */
    private String taskId;
}