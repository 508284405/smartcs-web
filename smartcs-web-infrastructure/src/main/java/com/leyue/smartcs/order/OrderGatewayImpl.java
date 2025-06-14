package com.leyue.smartcs.order;

import org.springframework.stereotype.Component;

import com.alibaba.cola.dto.PageResponse;
import com.alibaba.cola.dto.Response;
import com.alibaba.cola.dto.SingleResponse;
import com.leyue.smartcs.domain.order.OrderGateway;
import com.leyue.smartcs.dto.sse.OrderDTO;
import com.leyue.smartcs.dto.sse.OrderQueryDTO;
import com.leyue.smartcs.dto.sse.OrderCancelRequestDTO;
import com.leyue.smartcs.dto.sse.OrderAddressUpdateDTO;
import com.leyue.smartcs.config.feign.OrderFeign;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OrderGatewayImpl implements OrderGateway {

    private final OrderFeign orderFeign;

    @Override
    public PageResponse<OrderDTO> listOrders(OrderQueryDTO qry) {
        return orderFeign.listOrders(qry);
    }
    
    @Override
    public SingleResponse<OrderDTO> getOrder(String orderNumber) {
        return orderFeign.getOrder(orderNumber);
    }
    
    @Override
    public Response confirmReceipt(String orderNumber) {
        return orderFeign.confirmReceipt(orderNumber);
    }
    
    @Override
    public Response cancelOrder(String orderNumber, String reason) {
        OrderCancelRequestDTO dto = new OrderCancelRequestDTO();
        dto.setReason(reason);
        return orderFeign.cancelOrder(orderNumber, dto);
    }
    
    @Override
    public Response updateOrderAddress(OrderAddressUpdateDTO dto) {
        return orderFeign.updateOrderAddress(dto.getOrderNumber(), dto.getAddress());
    }
}
