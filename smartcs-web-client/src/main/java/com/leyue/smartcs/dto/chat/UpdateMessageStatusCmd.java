package com.leyue.smartcs.dto.chat;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 更新消息状态命令
 */
@Data
public class UpdateMessageStatusCmd {
    
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
     * 新的发送状态 0-发送中 1-已送达 2-发送失败 3-已读
     */
    @NotNull(message = "发送状态不能为空")
    private Integer sendStatus;
    
    /**
     * 失败原因（状态为发送失败时需要）
     */
    private String failReason;
    
    /**
     * 更新时间戳
     */
    private Long updatedAt;
}