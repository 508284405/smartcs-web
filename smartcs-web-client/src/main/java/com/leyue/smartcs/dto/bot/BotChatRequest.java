package com.leyue.smartcs.dto.bot;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Bot聊天请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BotChatRequest {

    /**
     * 会话ID
     */
    @NotEmpty(message = "会话ID不能为空")
    private String sessionId;

    /**
     * 机器人ID
     */
    @NotNull(message = "机器人ID不能为空")
    private Long botId;

    /**
     * 用户问题
     */
    @NotEmpty(message = "问题不能为空")
    private String question;
}