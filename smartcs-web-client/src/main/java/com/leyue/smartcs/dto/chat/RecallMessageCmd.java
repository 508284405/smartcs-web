package com.leyue.smartcs.dto.chat;

import com.alibaba.cola.dto.Command;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 消息撤回命令
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RecallMessageCmd extends Command {
    
    /**
     * 消息ID
     */
    @NotBlank(message = "消息ID不能为空")
    private String msgId;
    
    /**
     * 会话ID
     */
    @NotNull(message = "会话ID不能为空")
    private Long sessionId;
    
    /**
     * 撤回原因（可选）
     */
    private String reason;
    
    /**
     * 操作用户ID
     */
    @NotBlank(message = "用户ID不能为空")
    private String userId;
}