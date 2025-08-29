package com.leyue.smartcs.dto.chat;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.Data;

/**
 * 删除消息命令
 */
@Data
public class DeleteMessageCmd {
    
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
     * 删除类型 0-仅自己可见删除 1-双方删除
     */
    private Integer deleteType = 0;
    
    /**
     * 删除原因
     */
    private String reason;
    
    /**
     * 操作用户ID
     */
    @NotBlank(message = "操作用户ID不能为空")
    private String userId;
}