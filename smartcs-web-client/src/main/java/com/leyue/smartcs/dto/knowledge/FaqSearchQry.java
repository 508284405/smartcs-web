package com.leyue.smartcs.dto.knowledge;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FaqSearchQry {
    /**
     * 模型ID（用于嵌入模型）
     */
    @NotNull(message = "模型ID不能为空")
    private Long modelId;
    
    @NotBlank(message = "关键词不能为空")
    private String keyword;
    @Min(value = 1, message = "k不能小于1")
    private Integer k = 5;
}
