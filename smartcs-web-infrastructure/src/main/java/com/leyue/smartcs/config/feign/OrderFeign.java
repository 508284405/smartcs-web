package com.leyue.smartcs.config.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.*;

import com.alibaba.cola.dto.PageResponse;
import com.alibaba.cola.dto.Response;
import com.alibaba.cola.dto.SingleResponse;
import com.leyue.smartcs.dto.sse.OrderDTO;
import com.leyue.smartcs.dto.sse.OrderQueryDTO;
import com.leyue.smartcs.dto.sse.OrderCancelRequestDTO;
import com.leyue.smartcs.dto.sse.AddressDTO;

@FeignClient(name = "mathernity-baby-care", contextId = "order",configuration = FeignConfig.class)
public interface OrderFeign {

    @GetMapping("/api/orders/user")
    PageResponse<OrderDTO> listOrders(@SpringQueryMap OrderQueryDTO qry);

    @GetMapping("/api/orders/{orderNumber}")
    SingleResponse<OrderDTO> getOrder(@PathVariable("orderNumber") String orderNumber);

    @PostMapping("/api/orders/{orderNumber}/receive")
    Response confirmReceipt(@PathVariable("orderNumber") String orderNumber);

    @PostMapping("/api/orders/{orderNumber}/cancel")
    Response cancelOrder(@PathVariable("orderNumber") String orderNumber,
            @RequestBody OrderCancelRequestDTO dto);

    @PostMapping("/api/orders/{orderNumber}/address")
    Response updateOrderAddress(@PathVariable("orderNumber") String orderNumber,
            @RequestBody AddressDTO addressDTO);
}
