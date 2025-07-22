package com.leyue.smartcs.dto.model;

import com.alibaba.cola.dto.Command;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 启用/禁用模型实例命令
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ModelEnableCmd extends Command {
    
    /**
     * 模型ID
     */
    @NotNull(message = "模型ID不能为空")
    private Long id;
    
    /**
     * 状态（active/inactive）
     */
    @NotNull(message = "状态不能为空")
    private String status;
}