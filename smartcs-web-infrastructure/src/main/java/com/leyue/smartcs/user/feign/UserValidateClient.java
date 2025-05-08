package com.leyue.smartcs.user.feign;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "user-center",contextId = "user-center")
public interface UserValidateClient {
    
    @GetMapping("/api/users/current/info")
    String validateUserToken(@RequestHeader("Authorization") String token);
} 