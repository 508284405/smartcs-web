package com.leyue.smartcs.dto.knowledge;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FaqSearchQry {
    @NotBlank(message = "关键词不能为空")
    private String keyword;
    @Min(value = 1, message = "k不能小于1")
    private Integer k = 5;
}
