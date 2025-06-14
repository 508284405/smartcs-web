package com.leyue.smartcs.dto.sse;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

/**
 * 取消订单请求DTO
 */
@Data
public class OrderCancelRequestDTO {
    
    /**
     * 取消原因
     */
    @NotBlank(message = "取消原因不能为空")
    private String reason;
} 