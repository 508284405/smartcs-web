package com.leyue.smartcs.app.service;

import com.alibaba.cola.exception.BizException;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.leyue.smartcs.app.convertor.AiAppAppConvertor;
import com.leyue.smartcs.domain.app.AiApp;
import com.leyue.smartcs.domain.app.gateway.AiAppGateway;
import com.leyue.smartcs.domain.model.Model;
import com.leyue.smartcs.domain.model.Provider;
import com.leyue.smartcs.domain.model.gateway.ModelGateway;
import com.leyue.smartcs.domain.model.gateway.ProviderGateway;
import com.leyue.smartcs.dto.app.AiAppDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 聊天配置服务
 * 负责应用、模型、提供商等配置信息的获取和管理
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChatConfigurationService {

    private final AiAppGateway aiAppGateway;
    private final ModelGateway modelGateway;
    private final ProviderGateway providerGateway;

    /**
     * 获取应用信息
     */
    public AiAppDTO getAppInfo(Long appId) {
        try {
            if (appId == null) {
                throw new BizException("应用ID不能为空");
            }
            
            AiApp aiApp = aiAppGateway.getById(appId);
            if (aiApp == null) {
                throw new BizException("应用不存在或已删除: " + appId);
            }
            
            return AiAppAppConvertor.INSTANCE.domainToDto(aiApp);
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            throw new BizException("获取应用信息失败: " + e.getMessage());
        }
    }

    /**
     * 获取模型信息
     */
    public Model getModelInfo(Long modelId, AiAppDTO app) {
        try {
            // 如果指定了模型ID，使用指定的模型
            if (modelId != null) {
                return modelGateway.findById(modelId).orElseThrow(
                    () -> new BizException("模型不存在: " + modelId));
            }
            
            // 否则从应用配置中获取默认模型
            if (app.getConfig() != null) {
                JSONObject config = JSON.parseObject(JSON.toJSONString(app.getConfig()));
                Long defaultModelId = config.getLong("modelId");
                if (defaultModelId != null) {
                    return modelGateway.findById(defaultModelId).orElseThrow(
                        () -> new BizException("应用默认模型不存在: " + defaultModelId));
                }
            }
            
            throw new BizException("未找到可用的模型配置");
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            throw new BizException("获取模型信息失败: " + e.getMessage());
        }
    }

    /**
     * 获取提供商信息
     */
    public Provider getProviderInfo(Long providerId) {
        try {
            Provider provider = providerGateway.findById(providerId).orElse(null);
            if (provider == null) {
                throw new BizException("提供商不存在: " + providerId);
            }
            return provider;
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            throw new BizException("获取提供商信息失败: " + e.getMessage());
        }
    }

    /**
     * 构建系统提示词
     */
    public String buildSystemPrompt(AiAppDTO app, Map<String, Object> variables) {
        StringBuilder prompt = new StringBuilder();
        
        // 应用描述
        if (app.getDescription() != null && !app.getDescription().trim().isEmpty()) {
            prompt.append(app.getDescription()).append("\n\n");
        }
        
        // 从应用配置中获取系统提示词
        if (app.getConfig() != null) {
            JSONObject config = JSON.parseObject(JSON.toJSONString(app.getConfig()));
            String systemPrompt = config.getString("systemPrompt");
            if (systemPrompt != null && !systemPrompt.trim().isEmpty()) {
                prompt.append(systemPrompt).append("\n\n");
            }
        }
        
        // 如果没有配置提示词，使用默认的
        if (prompt.isEmpty()) {
            prompt.append("你是一个专业的AI助手，请根据用户的需求提供准确、有用的回答。\n\n");
        }
        
        return prompt.toString().trim();
    }

    /**
     * 验证配置完整性
     */
    public ConfigurationValidationResult validateConfiguration(Long appId, Long modelId) {
        try {
            // 验证应用
            AiAppDTO app = getAppInfo(appId);
            if (app == null) {
                return ConfigurationValidationResult.invalid("应用配置无效");
            }
            
            // 验证模型
            Model model = getModelInfo(modelId, app);
            if (model == null) {
                return ConfigurationValidationResult.invalid("模型配置无效");
            }
            
            // 验证提供商
            Provider provider = getProviderInfo(model.getProviderId());
            if (provider == null) {
                return ConfigurationValidationResult.invalid("提供商配置无效");
            }
            
            return ConfigurationValidationResult.valid(new ChatConfiguration(app, model, provider));
            
        } catch (Exception e) {
            log.error("配置验证失败: appId={}, modelId={}, error={}", appId, modelId, e.getMessage(), e);
            return ConfigurationValidationResult.invalid("配置验证失败: " + e.getMessage());
        }
    }

    /**
     * 聊天配置信息
     */
    public static class ChatConfiguration {
        public final AiAppDTO app;
        public final Model model;
        public final Provider provider;
        
        public ChatConfiguration(AiAppDTO app, Model model, Provider provider) {
            this.app = app;
            this.model = model;
            this.provider = provider;
        }
        
        @Override
        public String toString() {
            return String.format("ChatConfiguration{app=%s, model=%s, provider=%s}", 
                               app.getName(), model.getLabel(), provider.getLabel());
        }
    }

    /**
     * 配置验证结果
     */
    public static class ConfigurationValidationResult {
        private final boolean valid;
        private final String errorMessage;
        private final ChatConfiguration configuration;
        
        private ConfigurationValidationResult(boolean valid, String errorMessage, ChatConfiguration configuration) {
            this.valid = valid;
            this.errorMessage = errorMessage;
            this.configuration = configuration;
        }
        
        public static ConfigurationValidationResult valid(ChatConfiguration configuration) {
            return new ConfigurationValidationResult(true, null, configuration);
        }
        
        public static ConfigurationValidationResult invalid(String errorMessage) {
            return new ConfigurationValidationResult(false, errorMessage, null);
        }
        
        public boolean isValid() { return valid; }
        public String getErrorMessage() { return errorMessage; }
        public ChatConfiguration getConfiguration() { return configuration; }
        
        @Override
        public String toString() {
            return String.format("ConfigurationValidationResult{valid=%s, errorMessage='%s', configuration=%s}", 
                               valid, errorMessage, configuration);
        }
    }
}