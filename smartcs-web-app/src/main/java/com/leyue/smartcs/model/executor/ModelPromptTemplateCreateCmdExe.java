package com.leyue.smartcs.model.executor;

import com.alibaba.cola.dto.SingleResponse;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.domain.model.ModelPromptTemplate;
import com.leyue.smartcs.domain.model.gateway.ModelPromptTemplateGateway;
import com.leyue.smartcs.dto.model.ModelPromptTemplateCreateCmd;
import com.leyue.smartcs.dto.model.ModelPromptTemplateDTO;
import com.leyue.smartcs.model.convertor.ModelPromptTemplateAppConvertor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 创建模型Prompt模板命令执行器
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ModelPromptTemplateCreateCmdExe {

    private final ModelPromptTemplateGateway modelPromptTemplateGateway;
    private final ModelPromptTemplateAppConvertor modelPromptTemplateAppConvertor;

    /**
     * 创建Prompt模板
     *
     * @param cmd 创建命令
     * @return 模板DTO
     */
    public SingleResponse<ModelPromptTemplateDTO> execute(ModelPromptTemplateCreateCmd cmd) {
        try {
            // 参数校验
            if (cmd.getTemplateKey() == null || cmd.getTemplateKey().trim().isEmpty()) {
                throw new BizException("模板键不能为空");
            }
            if (cmd.getTemplateName() == null || cmd.getTemplateName().trim().isEmpty()) {
                throw new BizException("模板名称不能为空");
            }
            if (cmd.getTemplateContent() == null || cmd.getTemplateContent().trim().isEmpty()) {
                throw new BizException("模板内容不能为空");
            }

            // 检查模板键是否已存在
            if (modelPromptTemplateGateway.existsByTemplateKey(cmd.getTemplateKey())) {
                throw new BizException("模板键已存在");
            }

            // 创建域对象
            ModelPromptTemplate template = ModelPromptTemplate.builder()
                    .templateKey(cmd.getTemplateKey())
                    .templateName(cmd.getTemplateName())
                    .templateContent(cmd.getTemplateContent())
                    .description(cmd.getDescription())
                    .modelTypes(cmd.getModelTypes())
                    .variables(cmd.getVariables())
                    .isSystem(false)
                    .status("ACTIVE")
                    .build();

            // 设置创建时间
            long now = System.currentTimeMillis();
            template.setCreatedAt(now);
            template.setUpdatedAt(now);

            // 保存模板
            ModelPromptTemplate savedTemplate = modelPromptTemplateGateway.save(template);

            // 转换为DTO
            ModelPromptTemplateDTO dto = modelPromptTemplateAppConvertor.toDTO(savedTemplate);

            return SingleResponse.of(dto);

        } catch (Exception e) {
            log.error("创建Prompt模板失败: templateKey={}, error={}", cmd.getTemplateKey(), e.getMessage(), e);
            throw new BizException("创建模板失败: " + e.getMessage());
        }
    }
}