package com.leyue.smartcs.dto.chat;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 添加表情反应命令
 */
@Data
public class AddReactionCmd {
    
    /**
     * 消息ID
     */
    @NotBlank(message = "消息ID不能为空")
    private String msgId;
    
    /**
     * 会话ID
     */
    @NotBlank(message = "会话ID不能为空")
    private String sessionId;
    
    /**
     * 表情符号
     */
    @NotBlank(message = "表情符号不能为空")
    @Size(max = 10, message = "表情符号长度不能超过10个字符")
    private String emoji;
    
    /**
     * 表情名称
     */
    @NotBlank(message = "表情名称不能为空")
    @Size(max = 32, message = "表情名称长度不能超过32个字符")
    private String name;
    
    /**
     * 操作类型（toggle/add/remove）
     */
    private String action = "toggle";
}