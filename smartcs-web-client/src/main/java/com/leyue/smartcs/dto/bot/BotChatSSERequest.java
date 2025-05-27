package com.leyue.smartcs.dto.bot;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Bot SSE聊天请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BotChatSSERequest {

    /**
     * 会话ID
     */
    @NotNull(message = "会话ID不能为空")
    private Long sessionId;

    /**
     * 用户问题
     */
    @NotNull(message =  "用户问题不能为空")
    private String question;

    /**
     * 是否包含历史消息
     */
    @NotNull(message = "是否包含历史消息不能为空")
    private Boolean includeHistory = true;

    /**
     * 目标机器人
     */
    @NotNull(message = "目标机器人不能为空")
    private Long targetBotId;
} 