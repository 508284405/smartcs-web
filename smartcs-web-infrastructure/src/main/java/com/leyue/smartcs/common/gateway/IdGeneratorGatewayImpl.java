package com.leyue.smartcs.common.gateway;

import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.common.feign.IdGeneratorFeignClient;
import com.leyue.smartcs.domain.common.gateway.IdGeneratorGateway;
import com.leyue.smartcs.dto.common.ApiResponse;
import com.leyue.smartcs.dto.common.IdGeneratorRequest;
import com.leyue.smartcs.dto.common.IdGeneratorResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 分布式ID生成器网关实现类
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IdGeneratorGatewayImpl implements IdGeneratorGateway {

    private final IdGeneratorFeignClient idGeneratorFeignClient;

    @Override
    @CircuitBreaker(name = "id-generator-feign", fallbackMethod = "generateIdFallback")
    @Retry(name = "id-generator-feign", fallbackMethod = "generateIdFallback")
    @Bulkhead(name = "id-generator-feign", fallbackMethod = "generateIdFallback")
    @TimeLimiter(name = "id-generator-feign", fallbackMethod = "generateIdFallback")
    public Long generateId() {
        try {
            ApiResponse<IdGeneratorResponse> response = idGeneratorFeignClient.generateId(new IdGeneratorRequest());

            if (!response.getSuccess()) {
                log.error("调用ID生成服务失败: {}", response.getErrMessage());
                throw new BizException("调用ID生成服务失败: " + response.getErrMessage());
            }

            IdGeneratorResponse data = response.getData();
            if (data == null || data.getId() == null) {
                log.error("ID生成服务返回数据为空");
                throw new BizException("ID生成服务返回数据为空");
            }

            return data.getId();
        } catch (Exception e) {
            log.error("生成分布式ID时发生异常", e);
            throw new BizException("生成分布式ID失败", e);
        }
    }

    /**
     * 生成ID的降级方法
     */
    public Long generateIdFallback(Exception e) {
        log.warn("ID生成服务调用失败，使用降级策略: error={}", e.getMessage());
        // 返回一个基于时间戳的ID
        return System.currentTimeMillis();
    }

    @Override
    @CircuitBreaker(name = "id-generator-feign", fallbackMethod = "generateBatchIdsFallback")
    @Retry(name = "id-generator-feign", fallbackMethod = "generateBatchIdsFallback")
    @Bulkhead(name = "id-generator-feign", fallbackMethod = "generateBatchIdsFallback")
    @TimeLimiter(name = "id-generator-feign", fallbackMethod = "generateBatchIdsFallback")
    public Long[] generateBatchIds(int batchSize) {
        if (batchSize <= 0) {
            batchSize = 1;
        }

        try {
            ApiResponse<IdGeneratorResponse> response = idGeneratorFeignClient.generateId(new IdGeneratorRequest(batchSize));

            if (!response.getSuccess()) {
                log.error("调用ID生成服务失败: {}", response.getErrMessage());
                throw new BizException("调用ID生成服务失败: " + response.getErrMessage());
            }

            IdGeneratorResponse data = response.getData();
            if (data == null) {
                log.error("ID生成服务返回数据为空");
                throw new BizException("ID生成服务返回数据为空");
            }

            // 处理单个ID或批量ID列表
            if (batchSize == 1 && data.getId() != null) {
                return new Long[]{data.getId()};
            } else if (data.getIdList() != null && !data.getIdList().isEmpty()) {
                return data.getIdList().toArray(new Long[0]);
            } else {
                log.error("ID生成服务未返回有效的ID");
                throw new BizException("ID生成服务未返回有效的ID");
            }
        } catch (Exception e) {
            log.error("批量生成分布式ID时发生异常", e);
            throw new BizException("批量生成分布式ID失败", e);
        }
    }

    /**
     * 批量生成ID的降级方法
     */
    public Long[] generateBatchIdsFallback(int batchSize, Exception e) {
        log.warn("ID生成服务调用失败，使用降级策略: error={}", e.getMessage());
        // 返回基于时间戳的批量ID
        Long[] fallbackIds = new Long[batchSize];
        long baseTime = System.currentTimeMillis();
        for (int i = 0; i < batchSize; i++) {
            fallbackIds[i] = baseTime + i;
        }
        return fallbackIds;
    }

    @Override
    @CircuitBreaker(name = "id-generator-feign", fallbackMethod = "generateIdStrFallback")
    @Retry(name = "id-generator-feign", fallbackMethod = "generateIdStrFallback")
    @Bulkhead(name = "id-generator-feign", fallbackMethod = "generateIdStrFallback")
    @TimeLimiter(name = "id-generator-feign", fallbackMethod = "generateIdStrFallback")
    public String generateIdStr() {
        Long id = generateId();
        return String.format("%019d", id);
    }

    /**
     * 生成ID字符串的降级方法
     */
    public String generateIdStrFallback(Exception e) {
        log.warn("ID生成服务调用失败，使用降级策略: error={}", e.getMessage());
        // 返回基于时间戳的ID字符串
        return String.format("%019d", System.currentTimeMillis());
    }
}