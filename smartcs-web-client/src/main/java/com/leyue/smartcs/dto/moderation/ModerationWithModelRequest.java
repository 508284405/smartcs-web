package com.leyue.smartcs.dto.moderation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 带模型参数的审核请求DTO
 * 支持指定使用特定的AI模型进行内容审核
 *
 * @author Claude
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ModerationWithModelRequest {

    /**
     * 待审核内容
     */
    @NotBlank(message = "审核内容不能为空")
    private String content;

    /**
     * 使用的AI模型ID
     */
    @NotNull(message = "模型ID不能为空")
    private Long modelId;

    /**
     * 内容类型（可选）
     */
    private String contentType;

    /**
     * 来源类型（可选）
     */
    private String sourceType;

    /**
     * 源ID（可选）
     */
    private String sourceId;

    /**
     * 用户ID（可选）
     */
    private String userId;

    /**
     * 会话ID（可选）
     */
    private String sessionId;
}
