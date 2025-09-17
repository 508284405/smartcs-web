package com.leyue.smartcs.common.gateway;

import org.springframework.stereotype.Component;

import com.alibaba.cola.exception.BizException;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.leyue.smartcs.common.feign.IdGeneratorFeignClient;
import com.leyue.smartcs.domain.common.gateway.IdGeneratorGateway;
import com.leyue.smartcs.dto.common.ApiResponse;
import com.leyue.smartcs.dto.common.IdGeneratorRequest;
import com.leyue.smartcs.dto.common.IdGeneratorResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 分布式ID生成器网关实现类
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IdGeneratorGatewayImpl implements IdGeneratorGateway {

    private final IdGeneratorFeignClient idGeneratorFeignClient;

    @Override
    @SentinelResource(value = "gateway:id-generator:generateId",
            blockHandler = "generateIdBlockHandler",
            fallback = "generateIdFallback")
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
    public Long generateIdFallback(Throwable e) {
        log.warn("ID生成服务调用失败，使用降级策略", e);
        // 返回一个基于时间戳的ID
        return System.currentTimeMillis();
    }

    /**
     * Sentinel限流处理
     */
    public Long generateIdBlockHandler(BlockException ex) {
        log.warn("ID生成服务触发限流/降级: {}", ex.getMessage());
        return System.currentTimeMillis();
    }

    @Override
    @SentinelResource(value = "gateway:id-generator:generateBatchIds",
            blockHandler = "generateBatchIdsBlockHandler",
            fallback = "generateBatchIdsFallback")
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
    public Long[] generateBatchIdsFallback(int batchSize, Throwable e) {
        log.warn("批量ID生成失败，使用降级策略", e);
        // 返回基于时间戳的批量ID
        Long[] fallbackIds = new Long[batchSize];
        long baseTime = System.currentTimeMillis();
        for (int i = 0; i < batchSize; i++) {
            fallbackIds[i] = baseTime + i;
        }
        return fallbackIds;
    }

    /**
     * 批量生成ID的限流/降级处理
     */
    public Long[] generateBatchIdsBlockHandler(int batchSize, BlockException ex) {
        log.warn("批量生成ID触发限流/降级: {}", ex.getMessage());
        return generateBatchIdsFallback(batchSize, ex);
    }

    @Override
    @SentinelResource(value = "gateway:id-generator:generateIdStr",
            blockHandler = "generateIdStrBlockHandler",
            fallback = "generateIdStrFallback")
    public String generateIdStr() {
        Long id = generateId();
        return String.format("%019d", id);
    }

    /**
     * 生成ID字符串的降级方法
     */
    public String generateIdStrFallback(Throwable e) {
        log.warn("生成ID字符串失败，使用降级策略", e);
        // 返回基于时间戳的ID字符串
        return String.format("%019d", System.currentTimeMillis());
    }

    /**
     * 生成ID字符串的限流/降级处理
     */
    public String generateIdStrBlockHandler(BlockException ex) {
        log.warn("生成ID字符串触发限流/降级: {}", ex.getMessage());
        return String.format("%019d", System.currentTimeMillis());
    }
}
