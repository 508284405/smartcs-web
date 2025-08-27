package com.leyue.smartcs.mcp.executor;

import org.springframework.stereotype.Component;

import com.alibaba.cola.dto.SingleResponse;
import com.leyue.smartcs.domain.order.OrderGateway;
import com.leyue.smartcs.dto.sse.OrderDTO;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OrderGetQryExe {

    private final OrderGateway orderGateway;

    public SingleResponse<OrderDTO> execute(String orderNumber) {
        return orderGateway.getOrder(orderNumber);
    }
} 