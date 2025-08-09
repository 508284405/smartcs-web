package com.leyue.smartcs.common.feign;

import com.leyue.smartcs.config.feign.FeignConfig;
import com.leyue.smartcs.common.dao.UserCenterCustomerServiceDTO;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 用户中心服务Feign客户端
 */
@FeignClient(name = "user-center",contextId = "user-center",configuration = FeignConfig.class)
public interface UserCenterClient {

    @GetMapping("/api/users/current/info")
    @CircuitBreaker(name = "user-center-feign", fallbackMethod = "validateUserTokenFallback")
    @Retry(name = "user-center-feign", fallbackMethod = "validateUserTokenFallback")
    @Bulkhead(name = "user-center-feign", fallbackMethod = "validateUserTokenFallback")
    String validateUserToken(@RequestHeader("Authorization") String token);

    /**
     * 验证用户token的降级方法
     */
    default String validateUserTokenFallback(String token, Exception e) {
        // 返回一个默认的用户信息
        return "{\"id\":0,\"username\":\"fallback-user\",\"nickname\":\"降级用户\"}";
    }

    /**
     * 获取客服基础信息
     * @param serviceIds 客服ID列表
     * @return 客服基础信息列表
     */
    @GetMapping("/customer-service/batch")
    @CircuitBreaker(name = "user-center-feign", fallbackMethod = "getCustomerServiceByIdsFallback")
    @Retry(name = "user-center-feign", fallbackMethod = "getCustomerServiceByIdsFallback")
    @Bulkhead(name = "user-center-feign", fallbackMethod = "getCustomerServiceByIdsFallback")
    List<UserCenterCustomerServiceDTO> getCustomerServiceByIds(@RequestParam("serviceIds") List<String> serviceIds);

    /**
     * 获取客服信息的降级方法
     */
    default List<UserCenterCustomerServiceDTO> getCustomerServiceByIdsFallback(List<String> serviceIds, Exception e) {
        // 返回一个默认的客服信息
        return List.of();
    }

    /**
     * 获取所有客服ID列表
     * @return 客服ID列表
     */
    @GetMapping("/customer-service/ids")
    @CircuitBreaker(name = "user-center-feign", fallbackMethod = "getAllCustomerServiceIdsFallback")
    @Retry(name = "user-center-feign", fallbackMethod = "getAllCustomerServiceIdsFallback")
    @Bulkhead(name = "user-center-feign", fallbackMethod = "getAllCustomerServiceIdsFallback")
    List<String> getAllCustomerServiceIds();

    /**
     * 获取所有客服ID的降级方法
     */
    default List<String> getAllCustomerServiceIdsFallback(Exception e) {
        // 返回一个空的客服ID列表
        return List.of();
    }
} 