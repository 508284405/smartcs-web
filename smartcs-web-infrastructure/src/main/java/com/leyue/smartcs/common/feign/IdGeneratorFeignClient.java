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

    @PostMapping("/generate")
    ApiResponse<IdGeneratorResponse> generateId(@RequestBody IdGeneratorRequest request);

    @PostMapping("/generate-batch")
    ApiResponse<IdGeneratorResponse> generateBatchId(@RequestBody IdGeneratorRequest request);
}
