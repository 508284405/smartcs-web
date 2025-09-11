package com.leyue.smartcs.dto.model;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 模型流式推理请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModelInferStreamRequest {

    /**
     * 模型ID
     */
    @NotNull(message = "模型ID不能为空")
    private Long modelId;

    /**
     * 用户输入消息
     */
    @NotEmpty(message = "输入消息不能为空")
    private String message;

    /**
     * 会话ID（可选，用于上下文管理）
     */
    private String sessionId;

    /**
     * 系统Prompt（可选）
     */
    private String systemPrompt;

    /**
     * 知识库ID列表（支持多知识库查询）
     */
    private List<Long> knowledgeIds;

    /**
     * 推理参数（JSON格式，包含temperature、max_tokens等）
     */
    private String inferenceParams;
}