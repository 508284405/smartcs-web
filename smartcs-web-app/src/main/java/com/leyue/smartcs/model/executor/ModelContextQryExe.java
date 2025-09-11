package com.leyue.smartcs.model.executor;

import com.alibaba.cola.dto.SingleResponse;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.domain.model.gateway.ModelContextGateway;
import com.leyue.smartcs.dto.model.ModelContextDTO;
import com.leyue.smartcs.dto.common.SingleClientObject;
import com.leyue.smartcs.model.convertor.ModelContextAppConvertor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 模型上下文查询执行器
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ModelContextQryExe {

    private final ModelContextGateway modelContextGateway;
    private final ModelContextAppConvertor modelContextAppConvertor;

    /**
     * 获取模型上下文
     *
     * @param sessionId 会话ID
     * @return 上下文信息
     */
    public SingleResponse<ModelContextDTO> execute(SingleClientObject<String> sessionId) {
        try {
            // 参数校验
            if (sessionId == null || sessionId.getValue() == null || sessionId.getValue().trim().isEmpty()) {
                throw new BizException("会话ID不能为空");
            }

            String session = sessionId.getValue();

            // 查询上下文
            var context = modelContextGateway.findBySessionId(session);
            if (context == null) {
                return SingleResponse.buildFailure("CONTEXT_NOT_FOUND", "上下文不存在");
            }

            // 转换为DTO
            ModelContextDTO dto = modelContextAppConvertor.toDTO(context);

            return SingleResponse.of(dto);

        } catch (Exception e) {
            log.error("获取模型上下文失败: sessionId={}, error={}",
                    sessionId != null ? sessionId.getValue() : null, e.getMessage(), e);
            throw new BizException("获取上下文失败: " + e.getMessage());
        }
    }

    /**
     * 清除模型上下文
     *
     * @param sessionId 会话ID
     * @return 操作结果
     */
    public SingleResponse<Boolean> executeClear(SingleClientObject<String> sessionId) {
        try {
            // 参数校验
            if (sessionId == null || sessionId.getValue() == null || sessionId.getValue().trim().isEmpty()) {
                throw new BizException("会话ID不能为空");
            }

            String session = sessionId.getValue();

            // 清除上下文
            boolean result = modelContextGateway.clearBySessionId(session);

            return SingleResponse.of(result);

        } catch (Exception e) {
            log.error("清除模型上下文失败: sessionId={}, error={}",
                    sessionId != null ? sessionId.getValue() : null, e.getMessage(), e);
            throw new BizException("清除上下文失败: " + e.getMessage());
        }
    }
}