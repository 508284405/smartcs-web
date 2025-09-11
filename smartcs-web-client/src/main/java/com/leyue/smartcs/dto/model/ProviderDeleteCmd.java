package com.leyue.smartcs.dto.model;

import com.alibaba.cola.dto.Command;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 删除模型提供商命令
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProviderDeleteCmd extends Command {
    
    /**
     * 提供商ID
     */
    @NotNull(message = "提供商ID不能为空")
    private Long id;
}