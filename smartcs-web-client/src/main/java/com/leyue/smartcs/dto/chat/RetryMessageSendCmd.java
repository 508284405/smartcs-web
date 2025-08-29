package com.leyue.smartcs.dto.chat;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 重试发送消息命令
 */
@Data
public class RetryMessageSendCmd {
    
    /**
     * 消息ID
     */
    @NotBlank(message = "消息ID不能为空")
    private String msgId;
    
    /**
     * 会话ID
     */
    @NotNull(message = "会话ID不能为空")
    private String sessionId;
    
    /**
     * 用户ID
     */
    @NotBlank(message = "用户ID不能为空")
    private String userId;
    
    /**
     * 重试时间戳
     */
    private Long retryAt;
}