package com.leyue.smartcs.dto.knowledge;

import com.alibaba.cola.dto.Command;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 内容状态更新命令
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ContentStatusUpdateCmd extends Command {
    
    /**
     * 内容ID
     */
    @NotNull(message = "内容ID不能为空")
    private Long contentId;
    
    /**
     * 新状态 enabled/disabled
     */
    @NotNull(message = "状态不能为空")
    @Pattern(regexp = "^(enabled|disabled)$", message = "状态只能是enabled或disabled")
    private String status;
} 