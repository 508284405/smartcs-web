package com.leyue.smartcs.domain.moderation.service;

import com.leyue.smartcs.domain.moderation.*;
import com.leyue.smartcs.domain.moderation.gateway.ModerationGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 审核策略领域服务
 * 提供策略选择、维度管理和prompt生成的业务逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ModerationPolicyService {

    private final ModerationGateway moderationGateway;

    /**
     * 根据场景选择最佳审核策略
     */
    public Optional<ModerationPolicy> selectBestPolicyForScenario(String scenario) {
        log.debug("Selecting best policy for scenario: {}", scenario);
        
        List<ModerationPolicy> policies = moderationGateway.findPoliciesByScenario(scenario);
        if (policies.isEmpty()) {
            log.warn("No policies found for scenario: {}", scenario);
            return Optional.empty();
        }
        
        // 返回优先级最高的策略（优先级数值越小，优先级越高）
        ModerationPolicy bestPolicy = policies.get(0);
        log.debug("Selected policy: {} for scenario: {}", bestPolicy.getCode(), scenario);
        
        return Optional.of(bestPolicy);
    }

    /**
     * 根据策略ID获取关联的审核维度列表
     */
    public List<ModerationDimension> getPolicyDimensions(Long policyId) {
        log.debug("Getting dimensions for policy: {}", policyId);
        
        List<ModerationDimension> dimensions = moderationGateway.findDimensionsByPolicyId(policyId);
        log.debug("Found {} dimensions for policy: {}", dimensions.size(), policyId);
        
        return dimensions;
    }

    /**
     * 获取策略的有效维度列表（已启用且有效的维度）
     */
    public List<ModerationDimension> getEffectivePolicyDimensions(Long policyId) {
        List<ModerationDimension> dimensions = getPolicyDimensions(policyId);
        
        return dimensions.stream()
                .filter(dimension -> Boolean.TRUE.equals(dimension.getIsActive()))
                .filter(dimension -> dimension.isValid())
                .sorted(ModerationDimension::compareTo)
                .toList();
    }

    /**
     * 根据策略编码获取策略及其维度
     */
    public Optional<PolicyWithDimensions> getPolicyWithDimensions(String policyCode) {
        log.debug("Getting policy with dimensions: {}", policyCode);
        
        Optional<ModerationPolicy> policyOpt = moderationGateway.findPolicyByCode(policyCode);
        if (policyOpt.isEmpty()) {
            log.warn("Policy not found: {}", policyCode);
            return Optional.empty();
        }
        
        ModerationPolicy policy = policyOpt.get();
        List<ModerationDimension> dimensions = getEffectivePolicyDimensions(policy.getId());
        
        return Optional.of(new PolicyWithDimensions(policy, dimensions));
    }

    /**
     * 验证策略配置的有效性
     */
    public boolean validatePolicyConfiguration(ModerationPolicy policy) {
        if (!policy.isValid()) {
            log.warn("Invalid policy configuration: {}", policy.getCode());
            return false;
        }
        
        // 检查模板是否存在
        if (policy.getTemplateId() != null) {
            Optional<ModerationPolicyTemplate> template = moderationGateway.findTemplateById(policy.getTemplateId());
            if (template.isEmpty() || !Boolean.TRUE.equals(template.get().getIsActive())) {
                log.warn("Invalid template reference in policy: {}", policy.getCode());
                return false;
            }
        }
        
        // 检查关联的维度是否有效
        List<ModerationDimension> dimensions = getEffectivePolicyDimensions(policy.getId());
        if (dimensions.isEmpty()) {
            log.warn("No valid dimensions found for policy: {}", policy.getCode());
            return false;
        }
        
        return true;
    }

    /**
     * 创建或更新策略
     */
    public ModerationPolicy savePolicy(ModerationPolicy policy) {
        log.info("Saving moderation policy: {}", policy.getCode());
        
        if (!policy.isValid()) {
            throw new IllegalArgumentException("Invalid policy configuration");
        }
        
        return moderationGateway.saveModerationPolicy(policy);
    }

    /**
     * 创建或更新维度
     */
    public ModerationDimension saveDimension(ModerationDimension dimension) {
        log.info("Saving moderation dimension: {}", dimension.getCode());
        
        if (!dimension.isValid()) {
            throw new IllegalArgumentException("Invalid dimension configuration");
        }
        
        return moderationGateway.saveModerationDimension(dimension);
    }

    /**
     * 保存策略和维度的关联关系
     */
    public boolean savePolicyDimensionRelation(Long policyId, Long dimensionId, PolicyDimensionConfig config, String updatedBy) {
        log.info("Saving policy-dimension relation: policy={}, dimension={}", policyId, dimensionId);
        
        return moderationGateway.savePolicyDimensionRelation(
                policyId,
                dimensionId,
                config.getIsActive(),
                config.getWeight(),
                config.getCustomThreshold(),
                config.getCustomAction(),
                updatedBy
        );
    }

    /**
     * 批量更新策略维度状态
     */
    public boolean batchUpdatePolicyDimensionStatus(Long policyId, List<Long> dimensionIds, Boolean isActive, String updatedBy) {
        log.info("Batch updating policy dimension status: policy={}, dimensions={}, active={}", policyId, dimensionIds, isActive);
        
        return moderationGateway.batchUpdatePolicyDimensionStatus(policyId, dimensionIds, isActive, updatedBy);
    }

    /**
     * 策略与维度的组合数据类
     */
    public static class PolicyWithDimensions {
        private final ModerationPolicy policy;
        private final List<ModerationDimension> dimensions;

        public PolicyWithDimensions(ModerationPolicy policy, List<ModerationDimension> dimensions) {
            this.policy = policy;
            this.dimensions = dimensions;
        }

        public ModerationPolicy getPolicy() {
            return policy;
        }

        public List<ModerationDimension> getDimensions() {
            return dimensions;
        }

        public boolean hasValidDimensions() {
            return dimensions != null && !dimensions.isEmpty();
        }
    }

    /**
     * 策略维度配置类
     */
    public static class PolicyDimensionConfig {
        private Boolean isActive;
        private Double weight;
        private Double customThreshold;
        private String customAction;

        public PolicyDimensionConfig(Boolean isActive, Double weight, Double customThreshold, String customAction) {
            this.isActive = isActive;
            this.weight = weight;
            this.customThreshold = customThreshold;
            this.customAction = customAction;
        }

        public Boolean getIsActive() {
            return isActive;
        }

        public Double getWeight() {
            return weight;
        }

        public Double getCustomThreshold() {
            return customThreshold;
        }

        public String getCustomAction() {
            return customAction;
        }
    }
}