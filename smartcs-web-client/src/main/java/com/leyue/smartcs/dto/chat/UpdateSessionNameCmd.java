package com.leyue.smartcs.dto.chat;

import com.alibaba.cola.dto.Command;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 更新会话名称命令
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UpdateSessionNameCmd extends Command {
    
    /**
     * 会话ID
     */
    @NotNull(message = "会话ID不能为空")
    private Long sessionId;
    
    /**
     * 会话名称
     */
    @NotBlank(message = "会话名称不能为空")
    @Size(max = 50, message = "会话名称长度不能超过50个字符")
    private String sessionName;
} 