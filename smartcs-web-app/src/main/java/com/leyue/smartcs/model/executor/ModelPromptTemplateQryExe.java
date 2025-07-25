package com.leyue.smartcs.model.executor;

import com.alibaba.cola.dto.SingleResponse;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.domain.model.ModelPromptTemplate;
import com.leyue.smartcs.domain.model.gateway.ModelPromptTemplateGateway;
import com.leyue.smartcs.dto.model.ModelPromptTemplateDTO;
import com.leyue.smartcs.model.convertor.ModelPromptTemplateAppConvertor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 模型Prompt模板查询执行器
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ModelPromptTemplateQryExe {

    private final ModelPromptTemplateGateway modelPromptTemplateGateway;
    private final ModelPromptTemplateAppConvertor modelPromptTemplateAppConvertor;

    /**
     * 根据ID获取Prompt模板
     *
     * @param id 模板ID
     * @return 模板DTO
     */
    public SingleResponse<ModelPromptTemplateDTO> execute(Long id) {
        try {
            // 参数校验
            if (id == null) {
                throw new BizException("模板ID不能为空");
            }

            // 查询模板
            Optional<ModelPromptTemplate> templateOpt = modelPromptTemplateGateway.findById(id);
            if (!templateOpt.isPresent()) {
                return SingleResponse.buildFailure("TEMPLATE_NOT_FOUND", "模板不存在");
            }

            // 转换为DTO
            ModelPromptTemplateDTO dto = modelPromptTemplateAppConvertor.toDTO(templateOpt.get());

            return SingleResponse.of(dto);

        } catch (Exception e) {
            log.error("获取Prompt模板失败: id={}, error={}", id, e.getMessage(), e);
            throw new BizException("获取模板失败: " + e.getMessage());
        }
    }

    /**
     * 根据模板键获取Prompt模板
     *
     * @param templateKey 模板键
     * @return 模板DTO
     */
    public SingleResponse<ModelPromptTemplateDTO> executeByKey(String templateKey) {
        try {
            // 参数校验
            if (templateKey == null || templateKey.trim().isEmpty()) {
                throw new BizException("模板键不能为空");
            }

            // 查询模板
            Optional<ModelPromptTemplate> templateOpt = modelPromptTemplateGateway.findByTemplateKey(templateKey);
            if (!templateOpt.isPresent()) {
                return SingleResponse.buildFailure("TEMPLATE_NOT_FOUND", "模板不存在");
            }

            // 转换为DTO
            ModelPromptTemplateDTO dto = modelPromptTemplateAppConvertor.toDTO(templateOpt.get());

            return SingleResponse.of(dto);

        } catch (Exception e) {
            log.error("根据键获取Prompt模板失败: templateKey={}, error={}", templateKey, e.getMessage(), e);
            throw new BizException("获取模板失败: " + e.getMessage());
        }
    }
}