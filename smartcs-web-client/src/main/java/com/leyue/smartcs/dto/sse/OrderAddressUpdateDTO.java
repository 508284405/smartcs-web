package com.leyue.smartcs.dto.sse;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 订单地址更新DTO
 */
@Data
public class OrderAddressUpdateDTO {
    
    /**
     * 订单编号
     */
    @NotBlank(message = "订单编号不能为空")
    private String orderNumber;
    
    /**
     * 收货地址信息
     */
    @NotNull(message = "收货地址不能为空")
    private AddressDTO address;
} 