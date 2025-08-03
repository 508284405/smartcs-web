package com.leyue.smartcs.config.feign;

import com.leyue.smartcs.dto.common.ApiResponse;
import com.leyue.smartcs.dto.sse.OrderDTO;
import com.leyue.smartcs.dto.sse.OrderQueryDTO;
import com.leyue.smartcs.dto.sse.OrderCancelRequestDTO;
import com.leyue.smartcs.dto.sse.OrderAddressUpdateDTO;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.*;

/**
 * 订单服务Feign客户端
 */
@FeignClient(name = "mathernity-baby-care", contextId = "order",configuration = FeignConfig.class)
public interface OrderFeign {

    @GetMapping("/api/orders")
    @CircuitBreaker(name = "order-feign", fallbackMethod = "listOrdersFallback")
    @Retry(name = "order-feign", fallbackMethod = "listOrdersFallback")
    @Bulkhead(name = "order-feign", fallbackMethod = "listOrdersFallback")
    @TimeLimiter(name = "order-feign", fallbackMethod = "listOrdersFallback")
    ApiResponse<OrderDTO> listOrders(@SpringQueryMap OrderQueryDTO qry);

    /**
     * 获取订单列表的降级方法
     */
    default ApiResponse<OrderDTO> listOrdersFallback(OrderQueryDTO qry, Exception e) {
        // 返回一个空的订单列表
        return ApiResponse.success(null);
    }

    @GetMapping("/api/orders/{orderNumber}")
    @CircuitBreaker(name = "order-feign", fallbackMethod = "getOrderFallback")
    @Retry(name = "order-feign", fallbackMethod = "getOrderFallback")
    @Bulkhead(name = "order-feign", fallbackMethod = "getOrderFallback")
    @TimeLimiter(name = "order-feign", fallbackMethod = "getOrderFallback")
    ApiResponse<OrderDTO> getOrder(@PathVariable("orderNumber") String orderNumber);

    /**
     * 获取订单详情的降级方法
     */
    default ApiResponse<OrderDTO> getOrderFallback(String orderNumber, Exception e) {
        // 返回一个默认的订单信息
        OrderDTO fallbackOrder = new OrderDTO();
        fallbackOrder.setOrderNumber(orderNumber);
        fallbackOrder.setOrderStatus("UNKNOWN");
        return ApiResponse.success(fallbackOrder);
    }

    @PostMapping("/api/orders/{orderNumber}/confirm-receipt")
    @CircuitBreaker(name = "order-feign", fallbackMethod = "confirmReceiptFallback")
    @Retry(name = "order-feign", fallbackMethod = "confirmReceiptFallback")
    @Bulkhead(name = "order-feign", fallbackMethod = "confirmReceiptFallback")
    @TimeLimiter(name = "order-feign", fallbackMethod = "confirmReceiptFallback")
    ApiResponse<OrderDTO> confirmReceipt(@PathVariable("orderNumber") String orderNumber);

    /**
     * 确认收货的降级方法
     */
    default ApiResponse<OrderDTO> confirmReceiptFallback(String orderNumber, Exception e) {
        // 返回一个默认的订单信息
        OrderDTO fallbackOrder = new OrderDTO();
        fallbackOrder.setOrderNumber(orderNumber);
        fallbackOrder.setOrderStatus("CONFIRMED");
        return ApiResponse.success(fallbackOrder);
    }

    @PostMapping("/api/orders/{orderNumber}/cancel")
    @CircuitBreaker(name = "order-feign", fallbackMethod = "cancelOrderFallback")
    @Retry(name = "order-feign", fallbackMethod = "cancelOrderFallback")
    @Bulkhead(name = "order-feign", fallbackMethod = "cancelOrderFallback")
    @TimeLimiter(name = "order-feign", fallbackMethod = "cancelOrderFallback")
    ApiResponse<OrderDTO> cancelOrder(@PathVariable("orderNumber") String orderNumber, @RequestBody OrderCancelRequestDTO dto);

    /**
     * 取消订单的降级方法
     */
    default ApiResponse<OrderDTO> cancelOrderFallback(String orderNumber, OrderCancelRequestDTO dto, Exception e) {
        // 返回一个默认的订单信息
        OrderDTO fallbackOrder = new OrderDTO();
        fallbackOrder.setOrderNumber(orderNumber);
        fallbackOrder.setOrderStatus("CANCELLED");
        return ApiResponse.success(fallbackOrder);
    }

    @PutMapping("/api/orders/{orderNumber}/address")
    @CircuitBreaker(name = "order-feign", fallbackMethod = "updateOrderAddressFallback")
    @Retry(name = "order-feign", fallbackMethod = "updateOrderAddressFallback")
    @Bulkhead(name = "order-feign", fallbackMethod = "updateOrderAddressFallback")
    @TimeLimiter(name = "order-feign", fallbackMethod = "updateOrderAddressFallback")
    ApiResponse<OrderDTO> updateOrderAddress(@PathVariable("orderNumber") String orderNumber, @RequestBody OrderAddressUpdateDTO dto);

    /**
     * 更新订单地址的降级方法
     */
    default ApiResponse<OrderDTO> updateOrderAddressFallback(String orderNumber, OrderAddressUpdateDTO dto, Exception e) {
        // 返回一个默认的订单信息
        OrderDTO fallbackOrder = new OrderDTO();
        fallbackOrder.setOrderNumber(orderNumber);
        fallbackOrder.setOrderStatus("UPDATED");
        return ApiResponse.success(fallbackOrder);
    }
}
