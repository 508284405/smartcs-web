package com.leyue.smartcs.common.feign;

import com.leyue.smartcs.dto.common.ApiResponse;
import com.leyue.smartcs.dto.common.IdGeneratorRequest;
import com.leyue.smartcs.dto.common.IdGeneratorResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * ID生成器服务Feign客户端
 */
@FeignClient(name = "id-generator", path = "/api/generator")
public interface IdGeneratorFeignClient {

    @PostMapping("/generate")
    @CircuitBreaker(name = "id-generator-feign", fallbackMethod = "generateIdFallback")
    @Retry(name = "id-generator-feign", fallbackMethod = "generateIdFallback")
    @Bulkhead(name = "id-generator-feign", fallbackMethod = "generateIdFallback")
    ApiResponse<IdGeneratorResponse> generateId(@RequestBody IdGeneratorRequest request);

    /**
     * 生成ID的降级方法
     */
    default ApiResponse<IdGeneratorResponse> generateIdFallback(IdGeneratorRequest request, Exception e) {
        // 返回一个默认的ID响应
        IdGeneratorResponse fallbackResponse = new IdGeneratorResponse();
        fallbackResponse.setId(System.currentTimeMillis());
        return ApiResponse.success(fallbackResponse);
    }

    @PostMapping("/generate-batch")
    @CircuitBreaker(name = "id-generator-feign", fallbackMethod = "generateBatchIdFallback")
    @Retry(name = "id-generator-feign", fallbackMethod = "generateBatchIdFallback")
    @Bulkhead(name = "id-generator-feign", fallbackMethod = "generateBatchIdFallback")
    ApiResponse<IdGeneratorResponse> generateBatchId(@RequestBody IdGeneratorRequest request);

    /**
     * 批量生成ID的降级方法
     */
    default ApiResponse<IdGeneratorResponse> generateBatchIdFallback(IdGeneratorRequest request, Exception e) {
        // 返回一个默认的批量ID响应
        IdGeneratorResponse fallbackResponse = new IdGeneratorResponse();
        fallbackResponse.setId(System.currentTimeMillis());
        return ApiResponse.success(fallbackResponse);
    }
} 