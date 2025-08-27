package com.leyue.smartcs.moderation.service;

import com.leyue.smartcs.domain.moderation.*;
import com.leyue.smartcs.domain.moderation.gateway.ModerationGateway;
import com.leyue.smartcs.domain.moderation.service.ModerationPolicyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 审核prompt动态生成服务
 * 基于策略配置和模板动态生成审核prompt
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ModerationPromptGenerator {

    private final ModerationGateway moderationGateway;
    private final ModerationPolicyService policyService;

    /**
     * 根据场景生成审核prompt
     */
    public GeneratedPrompt generatePromptForScenario(String scenario, String content) {
        log.debug("Generating prompt for scenario: {}", scenario);
        
        // 1. 选择最佳策略
        Optional<ModerationPolicy> policyOpt = policyService.selectBestPolicyForScenario(scenario);
        if (policyOpt.isEmpty()) {
            log.warn("No policy found for scenario: {}, using fallback", scenario);
            return generateFallbackPrompt(content);
        }
        
        ModerationPolicy policy = policyOpt.get();
        return generatePromptForPolicy(policy, content);
    }

    /**
     * 根据策略生成审核prompt
     */
    public GeneratedPrompt generatePromptForPolicy(ModerationPolicy policy, String content) {
        log.debug("Generating prompt for policy: {}", policy.getCode());
        
        // 1. 获取策略关联的模板
        Optional<ModerationPolicyTemplate> templateOpt = Optional.empty();
        if (policy.getTemplateId() != null) {
            templateOpt = moderationGateway.findTemplateById(policy.getTemplateId());
        }
        
        if (templateOpt.isEmpty()) {
            log.warn("No template found for policy: {}, using default template", policy.getCode());
            return generatePromptWithDefaultTemplate(policy, content);
        }
        
        // 2. 获取策略的有效维度
        List<ModerationDimension> dimensions = policyService.getEffectivePolicyDimensions(policy.getId());
        if (dimensions.isEmpty()) {
            log.warn("No valid dimensions found for policy: {}", policy.getCode());
            return GeneratedPrompt.error("No valid dimensions configured for policy: " + policy.getCode());
        }
        
        // 3. 使用模板生成prompt
        ModerationPolicyTemplate template = templateOpt.get();
        return generatePromptWithTemplate(template, policy, dimensions, content);
    }

    /**
     * 根据策略编码生成审核prompt
     */
    public GeneratedPrompt generatePromptForPolicyCode(String policyCode, String content) {
        log.debug("Generating prompt for policy code: {}", policyCode);
        
        Optional<ModerationPolicy> policyOpt = moderationGateway.findPolicyByCode(policyCode);
        if (policyOpt.isEmpty()) {
            log.warn("Policy not found: {}", policyCode);
            return GeneratedPrompt.error("Policy not found: " + policyCode);
        }
        
        return generatePromptForPolicy(policyOpt.get(), content);
    }

    /**
     * 使用模板生成prompt
     */
    private GeneratedPrompt generatePromptWithTemplate(ModerationPolicyTemplate template, 
                                                     ModerationPolicy policy,
                                                     List<ModerationDimension> dimensions, 
                                                     String content) {
        try {
            // 1. 构建上下文变量
            Map<String, Object> contextVariables = new HashMap<>();
            contextVariables.put("content", content);
            contextVariables.put("policyName", policy.getName());
            contextVariables.put("policyType", policy.getPolicyType());
            contextVariables.put("scenario", policy.getScenario());
            
            // 2. 生成维度列表
            String dimensionsText = template.renderDimensions(dimensions);
            contextVariables.put("dimensions", dimensionsText);
            
            // 3. 添加策略特定的配置参数
            if (policy.getConfigParams() != null) {
                contextVariables.putAll(policy.getConfigParams());
            }
            
            // 4. 渲染完整prompt
            String finalPrompt = template.renderPrompt(contextVariables);
            
            log.debug("Generated prompt for policy {} using template {}", policy.getCode(), template.getCode());
            
            return GeneratedPrompt.success(finalPrompt, policy, template, dimensions);
            
        } catch (Exception e) {
            log.error("Failed to generate prompt with template: {}", template.getCode(), e);
            return GeneratedPrompt.error("Failed to generate prompt: " + e.getMessage());
        }
    }

    /**
     * 使用默认模板生成prompt
     */
    private GeneratedPrompt generatePromptWithDefaultTemplate(ModerationPolicy policy, String content) {
        log.debug("Generating prompt with default template for policy: {}", policy.getCode());
        
        // 获取策略的有效维度
        List<ModerationDimension> dimensions = policyService.getEffectivePolicyDimensions(policy.getId());
        if (dimensions.isEmpty()) {
            return GeneratedPrompt.error("No valid dimensions configured for policy: " + policy.getCode());
        }
        
        // 使用内置的默认模板
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("请对以下内容进行安全审核，评估其是否存在违规风险。\n\n");
        promptBuilder.append("审核内容：\n").append(content).append("\n\n");
        promptBuilder.append("请从以下维度进行评估：\n");
        
        int index = 1;
        for (ModerationDimension dimension : dimensions) {
            promptBuilder.append(index++).append(". ");
            promptBuilder.append(dimension.generatePromptDescription()).append("\n");
        }
        
        promptBuilder.append("\n请返回JSON格式的审核结果，包含：\n");
        promptBuilder.append("{\n");
        promptBuilder.append("  \"result\": \"APPROVED/REJECTED/NEEDS_REVIEW\",\n");
        promptBuilder.append("  \"riskLevel\": \"LOW/MEDIUM/HIGH/CRITICAL\",\n");
        promptBuilder.append("  \"confidence\": 0.85,\n");
        promptBuilder.append("  \"violations\": [\n");
        promptBuilder.append("    {\n");
        promptBuilder.append("      \"category\": \"违规分类\",\n");
        promptBuilder.append("      \"confidence\": 0.9,\n");
        promptBuilder.append("      \"reason\": \"具体原因\"\n");
        promptBuilder.append("    }\n");
        promptBuilder.append("  ],\n");
        promptBuilder.append("  \"reasoning\": \"详细分析说明\"\n");
        promptBuilder.append("}");
        
        return GeneratedPrompt.success(promptBuilder.toString(), policy, null, dimensions);
    }

    /**
     * 生成备用prompt（当没有可用策略时）
     */
    private GeneratedPrompt generateFallbackPrompt(String content) {
        log.debug("Generating fallback prompt");
        
        String fallbackPrompt = "请对以下内容进行基础安全审核：\n\n" +
                content + "\n\n" +
                "请检查是否包含以下类型的问题：\n" +
                "1. 仇恨言论或歧视性内容\n" +
                "2. 骚扰、威胁或恶意攻击\n" +
                "3. 暴力或危险行为描述\n" +
                "4. 个人隐私信息泄露\n\n" +
                "请返回简单的审核结果：SAFE、UNSAFE 或 BLOCKED";
        
        return GeneratedPrompt.fallback(fallbackPrompt);
    }

    /**
     * 根据模板类型和语言获取快速审核prompt
     */
    public GeneratedPrompt generateQuickModerationPrompt(String content, String language) {
        log.debug("Generating quick moderation prompt for language: {}", language);
        
        // 查找快速审核模板
        Optional<ModerationPolicyTemplate> templateOpt = moderationGateway.findTemplateByTypeAndLanguage("QUICK", language);
        if (templateOpt.isEmpty()) {
            templateOpt = moderationGateway.findTemplateByTypeAndLanguage("QUICK", "zh-CN"); // 默认中文
        }
        
        if (templateOpt.isEmpty()) {
            log.warn("No quick moderation template found, using fallback");
            return generateFallbackPrompt(content);
        }
        
        ModerationPolicyTemplate template = templateOpt.get();
        Map<String, Object> contextVariables = new HashMap<>();
        contextVariables.put("content", content);
        
        String finalPrompt = template.renderPrompt(contextVariables);
        
        return GeneratedPrompt.success(finalPrompt, null, template, null);
    }

    /**
     * 生成的prompt结果封装类
     */
    public static class GeneratedPrompt {
        private final boolean success;
        private final String prompt;
        private final String errorMessage;
        private final ModerationPolicy policy;
        private final ModerationPolicyTemplate template;
        private final List<ModerationDimension> dimensions;
        private final boolean isFallback;

        private GeneratedPrompt(boolean success, String prompt, String errorMessage, 
                              ModerationPolicy policy, ModerationPolicyTemplate template,
                              List<ModerationDimension> dimensions, boolean isFallback) {
            this.success = success;
            this.prompt = prompt;
            this.errorMessage = errorMessage;
            this.policy = policy;
            this.template = template;
            this.dimensions = dimensions;
            this.isFallback = isFallback;
        }

        public static GeneratedPrompt success(String prompt, ModerationPolicy policy, 
                                            ModerationPolicyTemplate template, List<ModerationDimension> dimensions) {
            return new GeneratedPrompt(true, prompt, null, policy, template, dimensions, false);
        }

        public static GeneratedPrompt fallback(String prompt) {
            return new GeneratedPrompt(true, prompt, null, null, null, null, true);
        }

        public static GeneratedPrompt error(String errorMessage) {
            return new GeneratedPrompt(false, null, errorMessage, null, null, null, false);
        }

        // Getters
        public boolean isSuccess() {
            return success;
        }

        public String getPrompt() {
            return prompt;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public ModerationPolicy getPolicy() {
            return policy;
        }

        public ModerationPolicyTemplate getTemplate() {
            return template;
        }

        public List<ModerationDimension> getDimensions() {
            return dimensions;
        }

        public boolean isFallback() {
            return isFallback;
        }

        public boolean hasTemplate() {
            return template != null;
        }

        public boolean hasDimensions() {
            return dimensions != null && !dimensions.isEmpty();
        }
    }
}