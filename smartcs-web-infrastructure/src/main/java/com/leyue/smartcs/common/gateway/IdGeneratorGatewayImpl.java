package com.leyue.smartcs.common.gateway;

import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.common.feign.IdGeneratorFeignClient;
import com.leyue.smartcs.domain.common.gateway.IdGeneratorGateway;
import com.leyue.smartcs.dto.common.ApiResponse;
import com.leyue.smartcs.dto.common.IdGeneratorRequest;
import com.leyue.smartcs.dto.common.IdGeneratorResponse;
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

    @Override
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

    @Override
    public String generateIdStr() {
        Long id = generateId();
        return String.format("%019d", id);
    }
}