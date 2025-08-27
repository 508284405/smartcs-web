package com.leyue.smartcs.app.executor;

import com.alibaba.cola.dto.PageResponse;
import com.alibaba.cola.dto.SingleResponse;
import com.leyue.smartcs.domain.moderation.*;
import com.leyue.smartcs.domain.moderation.gateway.ModerationGateway;
import com.leyue.smartcs.domain.moderation.service.ModerationPolicyService;
import com.leyue.smartcs.dto.moderation.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 审核策略查询执行器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ModerationPolicyQryExe {

    private final ModerationGateway moderationGateway;
    private final ModerationPolicyService policyService;

    /**
     * 根据ID查询策略详情
     */
    public SingleResponse<ModerationPolicyDTO> getPolicyById(Long policyId) {
        log.debug("Querying moderation policy by id: {}", policyId);

        try {
            Optional<ModerationPolicy> policyOpt = moderationGateway.findPolicyById(policyId);
            if (policyOpt.isEmpty()) {
                return SingleResponse.buildFailure("POLICY_NOT_FOUND", "策略不存在: " + policyId);
            }

            ModerationPolicy policy = policyOpt.get();
            ModerationPolicyDTO policyDTO = convertToPolicyDTO(policy);

            // 加载关联的模板信息
            if (policy.getTemplateId() != null) {
                Optional<ModerationPolicyTemplate> templateOpt = moderationGateway.findTemplateById(policy.getTemplateId());
                templateOpt.ifPresent(template -> policyDTO.setTemplate(convertToTemplateDTO(template)));
            }

            // 加载关联的维度信息
            List<ModerationDimension> dimensions = policyService.getEffectivePolicyDimensions(policyId);
            policyDTO.setDimensions(dimensions.stream()
                    .map(this::convertToDimensionDTO)
                    .collect(Collectors.toList()));

            return SingleResponse.of(policyDTO);

        } catch (Exception e) {
            log.error("Failed to query policy by id: {}", policyId, e);
            return SingleResponse.buildFailure("QUERY_POLICY_FAILED", "查询策略失败: " + e.getMessage());
        }
    }

    /**
     * 根据编码查询策略
     */
    public SingleResponse<ModerationPolicyDTO> getPolicyByCode(String code) {
        log.debug("Querying moderation policy by code: {}", code);

        try {
            Optional<ModerationPolicy> policyOpt = moderationGateway.findPolicyByCode(code);
            if (policyOpt.isEmpty()) {
                return SingleResponse.buildFailure("POLICY_NOT_FOUND", "策略不存在: " + code);
            }

            ModerationPolicy policy = policyOpt.get();
            ModerationPolicyDTO policyDTO = convertToPolicyDTO(policy);

            // 加载关联的维度信息
            List<ModerationDimension> dimensions = policyService.getEffectivePolicyDimensions(policy.getId());
            policyDTO.setDimensions(dimensions.stream()
                    .map(this::convertToDimensionDTO)
                    .collect(Collectors.toList()));

            return SingleResponse.of(policyDTO);

        } catch (Exception e) {
            log.error("Failed to query policy by code: {}", code, e);
            return SingleResponse.buildFailure("QUERY_POLICY_FAILED", "查询策略失败: " + e.getMessage());
        }
    }

    /**
     * 分页查询策略列表
     */
    public PageResponse<ModerationPolicyDTO> queryPolicies(ModerationPolicyPageQry qry) {
        log.debug("Querying moderation policies with criteria: {}", qry);

        try {
            // 这里简化实现，实际应该支持复杂查询条件和分页
            List<ModerationPolicy> policies;
            
            // 修复：正确处理空字符串参数，空字符串应该视为无场景限制
            if (qry.getScenario() != null && !qry.getScenario().trim().isEmpty()) {
                policies = moderationGateway.findPoliciesByScenario(qry.getScenario().trim());
            } else if (qry.getPolicyType() != null && !qry.getPolicyType().trim().isEmpty()) {
                policies = moderationGateway.findPoliciesByType(qry.getPolicyType().trim());
            } else {
                policies = moderationGateway.findAllActivePolicies();
            }

            // 过滤条件
            List<ModerationPolicy> filteredPolicies = policies.stream()
                    .filter(policy -> qry.getIsActive() == null || qry.getIsActive().equals(policy.getIsActive()))
                    .filter(policy -> qry.getName() == null || qry.getName().trim().isEmpty() || policy.getName().contains(qry.getName().trim()))
                    .filter(policy -> qry.getCode() == null || qry.getCode().trim().isEmpty() || policy.getCode().equals(qry.getCode().trim()))
                    .filter(policy -> qry.getTemplateId() == null || qry.getTemplateId().equals(policy.getTemplateId()))
                    .collect(Collectors.toList());

            // 排序处理
            if ("priority".equals(qry.getSortBy())) {
                if ("ASC".equalsIgnoreCase(qry.getSortOrder())) {
                    filteredPolicies.sort((p1, p2) -> {
                        Integer priority1 = p1.getPriority() != null ? p1.getPriority() : Integer.MAX_VALUE;
                        Integer priority2 = p2.getPriority() != null ? p2.getPriority() : Integer.MAX_VALUE;
                        return priority1.compareTo(priority2);
                    });
                } else {
                    filteredPolicies.sort((p1, p2) -> {
                        Integer priority1 = p1.getPriority() != null ? p1.getPriority() : Integer.MAX_VALUE;
                        Integer priority2 = p2.getPriority() != null ? p2.getPriority() : Integer.MAX_VALUE;
                        return priority2.compareTo(priority1);
                    });
                }
            }

            // 简单分页处理
            int total = filteredPolicies.size();
            int start = (qry.getPageNum() - 1) * qry.getPageSize();
            int end = Math.min(start + qry.getPageSize(), total);
            
            List<ModerationPolicy> pagedPolicies = start < total ? 
                    filteredPolicies.subList(start, end) : new ArrayList<>();

            // 转换为DTO
            List<ModerationPolicyDTO> policyDTOs = pagedPolicies.stream()
                    .map(this::convertToPolicyDTO)
                    .collect(Collectors.toList());

            // 修复：使用正确的参数顺序和字段名
            PageResponse<ModerationPolicyDTO> pageResponse = PageResponse.of(policyDTOs, total, qry.getPageSize(), qry.getPageNum());
            return pageResponse;

        } catch (Exception e) {
            log.error("Failed to query policies", e);
            return PageResponse.buildFailure("QUERY_POLICIES_FAILED", "查询策略列表失败: " + e.getMessage());
        }
    }

    /**
     * 查询指定场景的可用策略
     */
    public SingleResponse<List<ModerationPolicyDTO>> getPoliciesByScenario(String scenario) {
        log.debug("Querying policies by scenario: {}", scenario);

        try {
            // 修复：空字符串或null应该返回所有策略
            List<ModerationPolicy> policies;
            if (scenario == null || scenario.trim().isEmpty()) {
                policies = moderationGateway.findAllActivePolicies();
            } else {
                policies = moderationGateway.findPoliciesByScenario(scenario.trim());
            }
            
            List<ModerationPolicyDTO> policyDTOs = policies.stream()
                    .map(this::convertToPolicyDTO)
                    .collect(Collectors.toList());

            return SingleResponse.of(policyDTOs);

        } catch (Exception e) {
            log.error("Failed to query policies by scenario: {}", scenario, e);
            return SingleResponse.buildFailure("QUERY_POLICIES_FAILED", "查询场景策略失败: " + e.getMessage());
        }
    }

    /**
     * 查询策略关联的维度
     */
    public SingleResponse<List<ModerationDimensionDTO>> getPolicyDimensions(Long policyId) {
        log.debug("Querying policy dimensions: policyId={}", policyId);

        try {
            List<ModerationDimension> dimensions = policyService.getPolicyDimensions(policyId);
            List<ModerationDimensionDTO> dimensionDTOs = dimensions.stream()
                    .map(this::convertToDimensionDTO)
                    .collect(Collectors.toList());

            return SingleResponse.of(dimensionDTOs);

        } catch (Exception e) {
            log.error("Failed to query policy dimensions: policyId={}", policyId, e);
            return SingleResponse.buildFailure("QUERY_DIMENSIONS_FAILED", "查询策略维度失败: " + e.getMessage());
        }
    }

    /**
     * 查询所有可用的审核维度
     */
    public SingleResponse<List<ModerationDimensionDTO>> getAllActiveDimensions() {
        log.debug("Querying all active dimensions");

        try {
            List<ModerationDimension> dimensions = moderationGateway.findAllActiveDimensions();
            List<ModerationDimensionDTO> dimensionDTOs = dimensions.stream()
                    .map(this::convertToDimensionDTO)
                    .collect(Collectors.toList());

            return SingleResponse.of(dimensionDTOs);

        } catch (Exception e) {
            log.error("Failed to query all active dimensions", e);
            return SingleResponse.buildFailure("QUERY_DIMENSIONS_FAILED", "查询维度列表失败: " + e.getMessage());
        }
    }

    /**
     * 查询所有可用的策略模板
     */
    public SingleResponse<List<ModerationPolicyTemplateDTO>> getAllActiveTemplates() {
        log.debug("Querying all active templates");

        try {
            List<ModerationPolicyTemplate> templates = moderationGateway.findAllActiveTemplates();
            List<ModerationPolicyTemplateDTO> templateDTOs = templates.stream()
                    .map(this::convertToTemplateDTO)
                    .collect(Collectors.toList());

            return SingleResponse.of(templateDTOs);

        } catch (Exception e) {
            log.error("Failed to query all active templates", e);
            return SingleResponse.buildFailure("QUERY_TEMPLATES_FAILED", "查询模板列表失败: " + e.getMessage());
        }
    }

    /**
     * 转换策略领域对象为DTO
     */
    private ModerationPolicyDTO convertToPolicyDTO(ModerationPolicy policy) {
        return ModerationPolicyDTO.builder()
                .id(policy.getId())
                .name(policy.getName())
                .code(policy.getCode())
                .description(policy.getDescription())
                .scenario(policy.getScenario())
                .policyType(policy.getPolicyType())
                .defaultRiskLevel(policy.getDefaultRiskLevel() != null ? policy.getDefaultRiskLevel().getCode() : null)
                .defaultAction(policy.getDefaultAction() != null ? policy.getDefaultAction().getCode() : null)
                .isActive(policy.getIsActive())
                .priority(policy.getPriority())
                .configParams(policy.getConfigParams())
                .templateId(policy.getTemplateId())
                .createdBy(policy.getCreatedBy())
                .updatedBy(policy.getUpdatedBy())
                .createdAt(policy.getCreatedAt())
                .updatedAt(policy.getUpdatedAt())
                .build();
    }

    /**
     * 转换维度领域对象为DTO
     */
    private ModerationDimensionDTO convertToDimensionDTO(ModerationDimension dimension) {
        return ModerationDimensionDTO.builder()
                .id(dimension.getId())
                .name(dimension.getName())
                .code(dimension.getCode())
                .description(dimension.getDescription())
                .checkGuideline(dimension.getCheckGuideline())
                .severityLevel(dimension.getSeverityLevel() != null ? dimension.getSeverityLevel().getCode() : null)
                .actionType(dimension.getActionType() != null ? dimension.getActionType().getCode() : null)
                .confidenceThreshold(dimension.getConfidenceThreshold())
                .isActive(dimension.getIsActive())
                .sortOrder(dimension.getSortOrder())
                .category(dimension.getCategory())
                .configParams(dimension.getConfigParams())
                .categoryId(dimension.getCategoryId())
                .createdBy(dimension.getCreatedBy())
                .updatedBy(dimension.getUpdatedBy())
                .createdAt(dimension.getCreatedAt())
                .updatedAt(dimension.getUpdatedAt())
                .build();
    }

    /**
     * 转换模板领域对象为DTO
     */
    private ModerationPolicyTemplateDTO convertToTemplateDTO(ModerationPolicyTemplate template) {
        return ModerationPolicyTemplateDTO.builder()
                .id(template.getId())
                .name(template.getName())
                .code(template.getCode())
                .description(template.getDescription())
                .templateType(template.getTemplateType())
                .promptTemplate(template.getPromptTemplate())
                .dimensionTemplate(template.getDimensionTemplate())
                .responseTemplate(template.getResponseTemplate())
                .language(template.getLanguage())
                .variables(template.getVariables())
                .defaultValues(template.getDefaultValues())
                .isActive(template.getIsActive())
                .version(template.getVersion())
                .createdBy(template.getCreatedBy())
                .updatedBy(template.getUpdatedBy())
                .createdAt(template.getCreatedAt())
                .updatedAt(template.getUpdatedAt())
                .build();
    }
}