package com.leyue.smartcs.config.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import com.alibaba.cola.dto.PageResponse;
import com.leyue.smartcs.dto.sse.OrderDTO;
import com.leyue.smartcs.dto.sse.OrderQueryDTO;

@FeignClient(name = "client-web")
public interface OrderFeign {

    @GetMapping("/api/orders/user")
    PageResponse<OrderDTO> listOrders(OrderQueryDTO qry);
}
