package com.leyue.smartcs.config.feign;

import com.leyue.smartcs.dto.common.ApiResponse;
import com.leyue.smartcs.dto.sse.OrderAddressUpdateDTO;
import com.leyue.smartcs.dto.sse.OrderCancelRequestDTO;
import com.leyue.smartcs.dto.sse.OrderDTO;
import com.leyue.smartcs.dto.sse.OrderQueryDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 订单服务Feign客户端
 */
@FeignClient(name = "mathernity-baby-care", contextId = "order",configuration = FeignConfig.class)
public interface OrderFeign {

    @GetMapping("/api/orders")
    ApiResponse<OrderDTO> listOrders(@SpringQueryMap OrderQueryDTO qry);

    @GetMapping("/api/orders/{orderNumber}")
    ApiResponse<OrderDTO> getOrder(@PathVariable("orderNumber") String orderNumber);

    @PostMapping("/api/orders/{orderNumber}/confirm-receipt")
    ApiResponse<OrderDTO> confirmReceipt(@PathVariable("orderNumber") String orderNumber);

    @PostMapping("/api/orders/{orderNumber}/cancel")
    ApiResponse<OrderDTO> cancelOrder(@PathVariable("orderNumber") String orderNumber, @RequestBody OrderCancelRequestDTO dto);

    @PutMapping("/api/orders/{orderNumber}/address")
    ApiResponse<OrderDTO> updateOrderAddress(@PathVariable("orderNumber") String orderNumber, @RequestBody OrderAddressUpdateDTO dto);
}
