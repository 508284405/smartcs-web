package com.leyue.smartcs.app.executor;

import com.alibaba.cola.dto.Response;
import com.alibaba.cola.dto.SingleResponse;
import com.leyue.smartcs.domain.moderation.*;
import com.leyue.smartcs.domain.moderation.enums.ActionType;
import com.leyue.smartcs.domain.moderation.enums.SeverityLevel;
import com.leyue.smartcs.domain.moderation.gateway.ModerationGateway;
import com.leyue.smartcs.domain.moderation.service.ModerationPolicyService;
import com.leyue.smartcs.dto.moderation.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * 审核策略命令执行器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ModerationPolicyCmdExe {

    private final ModerationGateway moderationGateway;
    private final ModerationPolicyService policyService;

    /**
     * 创建审核策略
     */
    public SingleResponse<Long> createPolicy(ModerationPolicyCreateCmd cmd) {
        log.info("Creating moderation policy: {}", cmd.getCode());

        try {
            // 1. 验证策略编码是否已存在
            Optional<ModerationPolicy> existingPolicy = moderationGateway.findPolicyByCode(cmd.getCode());
            if (existingPolicy.isPresent()) {
                return SingleResponse.buildFailure("POLICY_CODE_EXISTS", "策略编码已存在: " + cmd.getCode());
            }

            // 2. 构建领域对象
            ModerationPolicy policy = ModerationPolicy.create(
                    cmd.getName(),
                    cmd.getCode(),
                    cmd.getDescription(),
                    cmd.getScenario(),
                    cmd.getPolicyType(),
                    SeverityLevel.fromCode(cmd.getDefaultRiskLevel()),
                    ActionType.fromCode(cmd.getDefaultAction()),
                    cmd.getPriority(),
                    cmd.getCreatedBy()
            );

            // 3. 设置配置参数和模板
            if (cmd.getConfigParams() != null) {
                cmd.getConfigParams().forEach(policy::setConfigParam);
            }
            if (cmd.getTemplateId() != null) {
                policy.bindTemplate(cmd.getTemplateId(), cmd.getCreatedBy());
            }

            // 4. 保存策略
            ModerationPolicy savedPolicy = policyService.savePolicy(policy);

            log.info("Moderation policy created successfully: id={}, code={}", savedPolicy.getId(), savedPolicy.getCode());
            return SingleResponse.of(savedPolicy.getId());

        } catch (Exception e) {
            log.error("Failed to create moderation policy: {}", cmd.getCode(), e);
            return SingleResponse.buildFailure("CREATE_POLICY_FAILED", "创建策略失败: " + e.getMessage());
        }
    }

    /**
     * 更新审核策略
     */
    public Response updatePolicy(ModerationPolicyUpdateCmd cmd) {
        log.info("Updating moderation policy: id={}", cmd.getId());

        try {
            // 1. 查找现有策略
            Optional<ModerationPolicy> policyOpt = moderationGateway.findPolicyById(cmd.getId());
            if (policyOpt.isEmpty()) {
                return Response.buildFailure("POLICY_NOT_FOUND", "策略不存在: " + cmd.getId());
            }

            // 2. 更新策略信息
            ModerationPolicy policy = policyOpt.get();
            policy.update(
                    cmd.getName(),
                    cmd.getDescription(),
                    cmd.getScenario(),
                    cmd.getPolicyType(),
                    SeverityLevel.fromCode(cmd.getDefaultRiskLevel()),
                    ActionType.fromCode(cmd.getDefaultAction()),
                    cmd.getPriority(),
                    cmd.getConfigParams(),
                    cmd.getUpdatedBy()
            );

            // 3. 更新模板关联
            if (cmd.getTemplateId() != null) {
                policy.bindTemplate(cmd.getTemplateId(), cmd.getUpdatedBy());
            }

            // 4. 保存更新
            policyService.savePolicy(policy);

            log.info("Moderation policy updated successfully: id={}", cmd.getId());
            return Response.buildSuccess();

        } catch (Exception e) {
            log.error("Failed to update moderation policy: id={}", cmd.getId(), e);
            return Response.buildFailure("UPDATE_POLICY_FAILED", "更新策略失败: " + e.getMessage());
        }
    }

    /**
     * 启用策略
     */
    public Response enablePolicy(Long policyId, String updatedBy) {
        log.info("Enabling moderation policy: id={}", policyId);

        try {
            Optional<ModerationPolicy> policyOpt = moderationGateway.findPolicyById(policyId);
            if (policyOpt.isEmpty()) {
                return Response.buildFailure("POLICY_NOT_FOUND", "策略不存在: " + policyId);
            }

            ModerationPolicy policy = policyOpt.get();
            policy.enable(updatedBy);
            policyService.savePolicy(policy);

            log.info("Moderation policy enabled successfully: id={}", policyId);
            return Response.buildSuccess();

        } catch (Exception e) {
            log.error("Failed to enable moderation policy: id={}", policyId, e);
            return Response.buildFailure("ENABLE_POLICY_FAILED", "启用策略失败: " + e.getMessage());
        }
    }

    /**
     * 禁用策略
     */
    public Response disablePolicy(Long policyId, String updatedBy) {
        log.info("Disabling moderation policy: id={}", policyId);

        try {
            Optional<ModerationPolicy> policyOpt = moderationGateway.findPolicyById(policyId);
            if (policyOpt.isEmpty()) {
                return Response.buildFailure("POLICY_NOT_FOUND", "策略不存在: " + policyId);
            }

            ModerationPolicy policy = policyOpt.get();
            policy.disable(updatedBy);
            policyService.savePolicy(policy);

            log.info("Moderation policy disabled successfully: id={}", policyId);
            return Response.buildSuccess();

        } catch (Exception e) {
            log.error("Failed to disable moderation policy: id={}", policyId, e);
            return Response.buildFailure("DISABLE_POLICY_FAILED", "禁用策略失败: " + e.getMessage());
        }
    }

    /**
     * 删除策略
     */
    public Response deletePolicy(Long policyId) {
        log.info("Deleting moderation policy: id={}", policyId);

        try {
            // 1. 检查策略是否存在
            Optional<ModerationPolicy> policyOpt = moderationGateway.findPolicyById(policyId);
            if (policyOpt.isEmpty()) {
                return Response.buildFailure("POLICY_NOT_FOUND", "策略不存在: " + policyId);
            }

            // 2. 检查策略是否正在使用
            // TODO: 检查是否有审核记录正在使用此策略

            // 3. 删除策略
            boolean deleted = moderationGateway.deleteModerationPolicy(policyId);
            if (!deleted) {
                return Response.buildFailure("DELETE_POLICY_FAILED", "删除策略失败");
            }

            log.info("Moderation policy deleted successfully: id={}", policyId);
            return Response.buildSuccess();

        } catch (Exception e) {
            log.error("Failed to delete moderation policy: id={}", policyId, e);
            return Response.buildFailure("DELETE_POLICY_FAILED", "删除策略失败: " + e.getMessage());
        }
    }

    /**
     * 配置策略维度关联
     */
    public Response configurePolicyDimensions(Long policyId, List<PolicyDimensionConfig> configs, String updatedBy) {
        log.info("Configuring policy dimensions: policyId={}, configs={}", policyId, configs.size());

        try {
            // 1. 验证策略存在
            Optional<ModerationPolicy> policyOpt = moderationGateway.findPolicyById(policyId);
            if (policyOpt.isEmpty()) {
                return Response.buildFailure("POLICY_NOT_FOUND", "策略不存在: " + policyId);
            }

            // 2. 配置维度关联
            for (PolicyDimensionConfig config : configs) {
                ModerationPolicyService.PolicyDimensionConfig serviceConfig = 
                    new ModerationPolicyService.PolicyDimensionConfig(
                        config.getIsActive(),
                        config.getWeight(),
                        config.getCustomThreshold(),
                        config.getCustomAction()
                    );

                boolean saved = policyService.savePolicyDimensionRelation(
                    policyId, 
                    config.getDimensionId(), 
                    serviceConfig, 
                    updatedBy
                );

                if (!saved) {
                    log.warn("Failed to save policy dimension relation: policyId={}, dimensionId={}", 
                            policyId, config.getDimensionId());
                }
            }

            log.info("Policy dimensions configured successfully: policyId={}", policyId);
            return Response.buildSuccess();

        } catch (Exception e) {
            log.error("Failed to configure policy dimensions: policyId={}", policyId, e);
            return Response.buildFailure("CONFIGURE_DIMENSIONS_FAILED", "配置策略维度失败: " + e.getMessage());
        }
    }

    /**
     * 策略维度配置DTO
     */
    public static class PolicyDimensionConfig {
        private Long dimensionId;
        private Boolean isActive;
        private Double weight;
        private Double customThreshold;
        private String customAction;

        // Getters and setters
        public Long getDimensionId() { return dimensionId; }
        public void setDimensionId(Long dimensionId) { this.dimensionId = dimensionId; }
        
        public Boolean getIsActive() { return isActive; }
        public void setIsActive(Boolean isActive) { this.isActive = isActive; }
        
        public Double getWeight() { return weight; }
        public void setWeight(Double weight) { this.weight = weight; }
        
        public Double getCustomThreshold() { return customThreshold; }
        public void setCustomThreshold(Double customThreshold) { this.customThreshold = customThreshold; }
        
        public String getCustomAction() { return customAction; }
        public void setCustomAction(String customAction) { this.customAction = customAction; }
    }
}