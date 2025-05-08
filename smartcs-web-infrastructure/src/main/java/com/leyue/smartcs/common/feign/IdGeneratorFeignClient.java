package com.leyue.smartcs.common.feign;

import com.leyue.smartcs.dto.common.ApiResponse;
import com.leyue.smartcs.dto.common.IdGeneratorRequest;
import com.leyue.smartcs.dto.common.IdGeneratorResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * ID生成器服务Feign客户端
 */
@FeignClient(name = "id-generator", path = "/api/generator")
public interface IdGeneratorFeignClient {
    
    /**
     * 获取分布式唯一ID
     * 
     * @param request 请求参数
     * @return ID生成响应
     */
    @PostMapping("/id")
    ApiResponse<IdGeneratorResponse> generateId(@RequestBody IdGeneratorRequest request);
} 