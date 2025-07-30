package com.leyue.smartcs.dto.app;

import lombok.Data;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

/**
 * 更新AI应用状态命令
 */
@Data
public class AiAppStatusUpdateCmd {
    
    /**
     * 应用ID
     */
    @NotNull(message = "应用ID不能为空")
    private Long id;
    
    /**
     * 应用状态
     */
    @NotBlank(message = "应用状态不能为空")
    private String status;
}