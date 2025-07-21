package com.leyue.smartcs.mcp.executor;

import org.springframework.stereotype.Component;

import com.alibaba.cola.dto.Response;
import com.leyue.smartcs.domain.order.OrderGateway;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OrderConfirmReceiptCmdExe {

    private final OrderGateway orderGateway;

    public Response execute(String orderNumber) {
        return orderGateway.confirmReceipt(orderNumber);
    }
} 