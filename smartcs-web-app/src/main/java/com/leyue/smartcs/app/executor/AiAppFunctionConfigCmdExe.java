package com.leyue.smartcs.app.executor;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.alibaba.cola.dto.Response;
import com.alibaba.cola.dto.SingleResponse;
import com.leyue.smartcs.domain.app.AiApp;
import com.leyue.smartcs.domain.app.gateway.AiAppGateway;
import com.leyue.smartcs.dto.app.AiAppFunctionConfigCmd;
import com.leyue.smartcs.dto.app.AiAppFunctionConfigResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * AI应用功能配置命令执行器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AiAppFunctionConfigCmdExe {
    
    private final AiAppGateway aiAppGateway;
    
    public Response updateFunctionConfig(AiAppFunctionConfigCmd cmd) {
        try {
            log.info("开始更新AI应用功能配置: appId={}", cmd.getAppId());
            
            // 查询AI应用
            AiApp aiApp = aiAppGateway.getById(cmd.getAppId());
            if (aiApp == null) {
                return Response.buildFailure("APP_NOT_FOUND", "AI应用不存在");
            }
            
            // 构建功能配置
            Map<String, Object> functionConfig = buildFunctionConfig(cmd);
            
            // 更新配置（这里假设AiApp有setConfig方法）
            Map<String, Object> config = aiApp.getConfig();
            if (config == null) {
                config = new HashMap<>();
            }
            config.put("functionConfig", functionConfig);
            aiApp.setConfig(config);
            
            // 保存到数据库
            aiAppGateway.save(aiApp);
            
            log.info("AI应用功能配置更新成功: appId={}", cmd.getAppId());
            return Response.buildSuccess();
            
        } catch (Exception e) {
            log.error("更新AI应用功能配置异常: appId={}", cmd.getAppId(), e);
            return Response.buildFailure("UPDATE_CONFIG_ERROR", "更新功能配置异常: " + e.getMessage());
        }
    }
    
    public SingleResponse<AiAppFunctionConfigResponse> getFunctionConfig(Long appId) {
        try {
            log.info("开始获取AI应用功能配置: appId={}", appId);
            
            // 查询AI应用
            AiApp aiApp = aiAppGateway.getById(appId);
            if (aiApp == null) {
                return SingleResponse.buildFailure("APP_NOT_FOUND", "AI应用不存在");
            }
            
            // 获取功能配置
            Map<String, Object> config = aiApp.getConfig();
            Map<String, Object> functionConfig = new HashMap<>();
            if (config != null && config.containsKey("functionConfig")) {
                functionConfig = (Map<String, Object>) config.get("functionConfig");
            }
            
            // 构建响应
            AiAppFunctionConfigResponse response = buildFunctionConfigResponse(appId, functionConfig);
            
            log.info("获取AI应用功能配置成功: appId={}", appId);
            return SingleResponse.of(response);
            
        } catch (Exception e) {
            log.error("获取AI应用功能配置异常: appId={}", appId, e);
            return SingleResponse.buildFailure("GET_CONFIG_ERROR", "获取功能配置异常: " + e.getMessage());
        }
    }
    
    private Map<String, Object> buildFunctionConfig(AiAppFunctionConfigCmd cmd) {
        Map<String, Object> functionConfig = new HashMap<>();
        
        if (cmd.getConversationOpenerEnabled() != null) {
            functionConfig.put("conversationOpenerEnabled", cmd.getConversationOpenerEnabled());
        }
        if (cmd.getNextQuestionSuggestionEnabled() != null) {
            functionConfig.put("nextQuestionSuggestionEnabled", cmd.getNextQuestionSuggestionEnabled());
        }
        if (cmd.getTextToSpeechEnabled() != null) {
            functionConfig.put("textToSpeechEnabled", cmd.getTextToSpeechEnabled());
        }
        if (cmd.getSpeechToTextEnabled() != null) {
            functionConfig.put("speechToTextEnabled", cmd.getSpeechToTextEnabled());
        }
        if (cmd.getCitationEnabled() != null) {
            functionConfig.put("citationEnabled", cmd.getCitationEnabled());
        }
        if (cmd.getContentModerationEnabled() != null) {
            functionConfig.put("contentModerationEnabled", cmd.getContentModerationEnabled());
        }
        if (cmd.getStandardReplyEnabled() != null) {
            functionConfig.put("standardReplyEnabled", cmd.getStandardReplyEnabled());
        }
        
        // 如果有自定义配置，也加入
        if (cmd.getFunctionConfig() != null) {
            functionConfig.putAll(cmd.getFunctionConfig());
        }
        
        return functionConfig;
    }
    
    private AiAppFunctionConfigResponse buildFunctionConfigResponse(Long appId, Map<String, Object> functionConfig) {
        return AiAppFunctionConfigResponse.builder()
                .appId(appId)
                .functionConfig(functionConfig)
                .conversationOpenerEnabled(getBooleanValue(functionConfig, "conversationOpenerEnabled", false))
                .nextQuestionSuggestionEnabled(getBooleanValue(functionConfig, "nextQuestionSuggestionEnabled", false))
                .textToSpeechEnabled(getBooleanValue(functionConfig, "textToSpeechEnabled", false))
                .speechToTextEnabled(getBooleanValue(functionConfig, "speechToTextEnabled", false))
                .citationEnabled(getBooleanValue(functionConfig, "citationEnabled", true))
                .contentModerationEnabled(getBooleanValue(functionConfig, "contentModerationEnabled", false))
                .standardReplyEnabled(getBooleanValue(functionConfig, "standardReplyEnabled", false))
                .build();
    }
    
    private Boolean getBooleanValue(Map<String, Object> config, String key, Boolean defaultValue) {
        Object value = config.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return defaultValue;
    }
}