package com.leyue.smartcs.dto.chat;

import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import lombok.Data;

/**
 * 批量删除消息命令
 */
@Data
public class BatchDeleteMessagesCmd {
    
    /**
     * 消息ID列表
     */
    @NotEmpty(message = "消息ID列表不能为空")
    private List<String> msgIds;
    
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