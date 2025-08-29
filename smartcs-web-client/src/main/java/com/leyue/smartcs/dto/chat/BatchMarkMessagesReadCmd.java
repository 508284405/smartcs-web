package com.leyue.smartcs.dto.chat;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 批量标记消息已读命令
 */
@Data
public class BatchMarkMessagesReadCmd {
    
    /**
     * 消息ID列表
     */
    @NotEmpty(message = "消息ID列表不能为空")
    private List<String> msgIds;
    
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
     * 读取时间戳
     */
    private Long readAt;
}