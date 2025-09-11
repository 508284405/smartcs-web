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
import com.leyue.smartcs.dto.common.ApiResponse;

import lombok.RequiredArgsConstructor;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderGatewayImpl implements OrderGateway {

    private final OrderFeign orderFeign;

    @Override
    public PageResponse<OrderDTO> listOrders(OrderQueryDTO qry) {
        ApiResponse<OrderDTO> apiResponse = orderFeign.listOrders(qry);
        if (apiResponse.getSuccess()) {
            // 由于Feign返回的是单个OrderDTO，我们需要将其包装为列表
            List<OrderDTO> orderList = apiResponse.getData() != null ? 
                List.of(apiResponse.getData()) : List.of();
            return PageResponse.of(orderList, 1, 1, 1);
        } else {
            return PageResponse.buildFailure(apiResponse.getErrCode(), apiResponse.getErrMessage());
        }
    }
    
    @Override
    public SingleResponse<OrderDTO> getOrder(String orderNumber) {
        ApiResponse<OrderDTO> apiResponse = orderFeign.getOrder(orderNumber);
        if (apiResponse.getSuccess()) {
            return SingleResponse.of(apiResponse.getData());
        } else {
            return SingleResponse.buildFailure(apiResponse.getErrCode(), apiResponse.getErrMessage());
        }
    }
    
    @Override
    public Response confirmReceipt(String orderNumber) {
        ApiResponse<OrderDTO> apiResponse = orderFeign.confirmReceipt(orderNumber);
        if (apiResponse.getSuccess()) {
            return Response.buildSuccess();
        } else {
            return Response.buildFailure(apiResponse.getErrCode(), apiResponse.getErrMessage());
        }
    }
    
    @Override
    public Response cancelOrder(String orderNumber, String reason) {
        OrderCancelRequestDTO dto = new OrderCancelRequestDTO();
        dto.setReason(reason);
        ApiResponse<OrderDTO> apiResponse = orderFeign.cancelOrder(orderNumber, dto);
        if (apiResponse.getSuccess()) {
            return Response.buildSuccess();
        } else {
            return Response.buildFailure(apiResponse.getErrCode(), apiResponse.getErrMessage());
        }
    }
    
    @Override
    public Response updateOrderAddress(OrderAddressUpdateDTO dto) {
        ApiResponse<OrderDTO> apiResponse = orderFeign.updateOrderAddress(dto.getOrderNumber(), dto);
        if (apiResponse.getSuccess()) {
            return Response.buildSuccess();
        } else {
            return Response.buildFailure(apiResponse.getErrCode(), apiResponse.getErrMessage());
        }
    }
}
