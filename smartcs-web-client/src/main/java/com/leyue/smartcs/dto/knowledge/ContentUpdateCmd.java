package com.leyue.smartcs.dto.knowledge;

import com.alibaba.cola.dto.Command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 内容更新命令
*/
@Data
@EqualsAndHashCode(callSuper = true)
public class ContentUpdateCmd extends Command {
    
    /**
     * 内容ID
     */
    @NotNull(message = "内容ID不能为空")
    private Long id;
    
    /**
     * 内容标题
     */
    @NotBlank(message = "内容标题不能为空")
    private String title;
} 