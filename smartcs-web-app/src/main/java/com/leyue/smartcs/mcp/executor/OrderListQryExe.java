package com.leyue.smartcs.mcp.executor;

import org.springframework.stereotype.Component;

import com.alibaba.cola.dto.PageResponse;
import com.leyue.smartcs.domain.order.OrderGateway;
import com.leyue.smartcs.dto.sse.OrderDTO;
import com.leyue.smartcs.dto.sse.OrderQueryDTO;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OrderListQryExe {

    private final OrderGateway orderGateway;

    public PageResponse<OrderDTO> execute(OrderQueryDTO qry) {
        return orderGateway.listOrders(qry);
    }
}