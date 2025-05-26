package com.leyue.smartcs.customer.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;

/**
 * 用户中心Feign客户端
 */
@FeignClient(name = "user-center", path = "/api")
public interface UserCenterFeignClient {
    
    /**
     * 获取客服基础信息
     * @param serviceIds 客服ID列表
     * @return 客服基础信息列表
     */
    @GetMapping("/customer-service/batch")
    List<UserCenterCustomerServiceDTO> getCustomerServiceByIds(@RequestParam("serviceIds") List<String> serviceIds);
    
    /**
     * 获取所有客服ID列表
     * @return 客服ID列表
     */
    @GetMapping("/customer-service/ids")
    List<String> getAllCustomerServiceIds();
} 