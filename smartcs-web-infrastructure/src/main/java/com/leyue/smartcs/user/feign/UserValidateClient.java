package com.leyue.smartcs.user.feign;


import com.leyue.smartcs.config.feign.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "user-center",contextId = "user-center",configuration = FeignConfig.class)
public interface UserValidateClient {
    
    @GetMapping("/api/users/current/info")
    String validateUserToken(@RequestHeader("Authorization") String token);
} 