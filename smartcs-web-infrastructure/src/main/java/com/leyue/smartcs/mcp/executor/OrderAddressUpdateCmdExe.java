package com.leyue.smartcs.mcp.executor;

import org.springframework.stereotype.Component;

import com.alibaba.cola.dto.Response;
import com.leyue.smartcs.domain.order.OrderGateway;
import com.leyue.smartcs.dto.sse.AddressDTO;
import com.leyue.smartcs.dto.sse.OrderAddressUpdateDTO;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OrderAddressUpdateCmdExe {

    private final OrderGateway orderGateway;

    public Response execute(String orderNumber, AddressDTO addressDTO) {
        OrderAddressUpdateDTO dto = new OrderAddressUpdateDTO();
        dto.setOrderNumber(orderNumber);
        dto.setAddress(addressDTO);
        return orderGateway.updateOrderAddress(dto);
    }
} 