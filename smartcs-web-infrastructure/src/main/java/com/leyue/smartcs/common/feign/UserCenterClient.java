package com.leyue.smartcs.common.feign;


import com.leyue.smartcs.common.dao.UserCenterCustomerServiceDTO;
import com.leyue.smartcs.config.feign.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "user-center",contextId = "user-center",configuration = FeignConfig.class)
public interface UserCenterClient {
    
    @GetMapping("/api/users/current/info")
    String validateUserToken(@RequestHeader("Authorization") String token);

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