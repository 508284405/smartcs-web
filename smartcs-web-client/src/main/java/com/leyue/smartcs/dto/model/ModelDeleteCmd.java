package com.leyue.smartcs.dto.model;

import com.alibaba.cola.dto.Command;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 删除模型实例命令
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ModelDeleteCmd extends Command {
    
    /**
     * 模型ID
     */
    @NotNull(message = "模型ID不能为空")
    private Long id;
}