package com.leyue.smartcs.mcp.executor;

import org.springframework.stereotype.Component;

import com.alibaba.cola.dto.Response;
import com.leyue.smartcs.domain.order.OrderGateway;
import com.leyue.smartcs.dto.sse.OrderCancelRequestDTO;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OrderCancelCmdExe {

    private final OrderGateway orderGateway;

    public Response execute(String orderNumber, OrderCancelRequestDTO dto) {
        return orderGateway.cancelOrder(orderNumber, dto.getReason());
    }
} 