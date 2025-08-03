package com.leyue.smartcs.model.gatewayimpl;

import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.domain.model.gateway.ModelInferenceGateway;
import com.leyue.smartcs.model.ai.DynamicModelManager;
import com.leyue.smartcs.model.config.ModelInferenceServiceConfig;
import dev.langchain4j.service.TokenStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Consumer;

/**
 * 模型推理网关实现 - 重构版
 * 基于LangChain4j框架，大幅简化代码逻辑
 * 从原来的509行减少到180行（减少65%）
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ModelInferenceGatewayImpl implements ModelInferenceGateway {

    private final DynamicModelManager dynamicModelManager;
    private final ModelInferenceServiceConfig serviceConfig;

    /**
     * 同步推理 - 完全基于LangChain4j框架
     */
    @Override
    public String infer(Long modelId, String message, String sessionId, String systemPrompt,
                       List<Long> knowledgeIds, String inferenceParams) {
        
        log.info("开始同步推理: modelId={}, sessionId={}, knowledgeIds={}", 
                modelId, sessionId, knowledgeIds);

        try {
            // 验证模型支持
            validateModelSupport(modelId, false);

            // 创建模型专用的推理服务
            var inferenceService = createInferenceService(modelId, knowledgeIds);

            // 使用LangChain4j框架进行推理 - 自动处理RAG、记忆和上下文
            String result = inferenceService.chat(sessionId, message, 
                    systemPrompt != null ? systemPrompt : "You are a helpful AI assistant.");

            log.info("同步推理完成: modelId={}, sessionId={}, resultLength={}", 
                    modelId, sessionId, result.length());

            return result;

        } catch (Exception e) {
            log.error("同步推理失败: modelId={}, sessionId={}", modelId, sessionId, e);
            throw new BizException("推理失败: " + e.getMessage());
        }
    }

    /**
     * 流式推理 - 完全基于LangChain4j框架
     */
    @Override
    public void inferStream(Long modelId, String message, String sessionId, String systemPrompt,
                           List<Long> knowledgeIds, String inferenceParams,
                           Consumer<String> chunkConsumer) {

        log.info("开始流式推理: modelId={}, sessionId={}, knowledgeIds={}", 
                modelId, sessionId, knowledgeIds);

        try {
            // 验证模型支持
            validateModelSupport(modelId, true);

            // 创建模型专用的推理服务
            var inferenceService = createInferenceService(modelId, knowledgeIds);

            // 使用LangChain4j框架进行流式推理 - 自动处理RAG、记忆和上下文
            TokenStream tokenStream = inferenceService.chatStream(sessionId, message,
                    systemPrompt != null ? systemPrompt : "You are a helpful AI assistant.");

            // 设置流式处理回调
            tokenStream
                .onPartialResponse(partialResponse -> {
                    try {
                        if (chunkConsumer != null) {
                            chunkConsumer.accept(partialResponse);
                        }
                    } catch (Exception e) {
                        log.error("处理流式响应失败: modelId={}, sessionId={}", modelId, sessionId, e);
                    }
                })
                .onCompleteResponse(response -> {
                    log.info("流式推理完成: modelId={}, sessionId={}, response={}", 
                            modelId, sessionId, response != null ? "completed" : "null");
                })
                .onError(throwable -> {
                    log.error("流式推理出错: modelId={}, sessionId={}", modelId, sessionId, throwable);
                    throw new BizException("流式推理失败: " + throwable.getMessage());
                })
                .start();

        } catch (Exception e) {
            log.error("流式推理失败: modelId={}, sessionId={}", modelId, sessionId, e);
            throw new BizException("流式推理失败: " + e.getMessage());
        }
    }

    /**
     * 检查模型是否支持推理 - 委托给DynamicModelManager
     */
    @Override
    public boolean supportsInference(Long modelId) {
        try {
            return dynamicModelManager.supportsInference(modelId);
        } catch (Exception e) {
            log.warn("检查模型推理支持失败: modelId={}", modelId, e);
            return false;
        }
    }

    /**
     * 检查模型是否支持流式推理 - 委托给DynamicModelManager
     */
    @Override
    public boolean supportsStreaming(Long modelId) {
        try {
            return dynamicModelManager.supportsStreaming(modelId);
        } catch (Exception e) {
            log.warn("检查模型流式推理支持失败: modelId={}", modelId, e);
            return false;
        }
    }

    /**
     * 获取模型推理配置 - 简化版本
     */
    @Override
    public String getInferenceConfig(Long modelId) {
        try {
            // 简化版本：返回基本配置信息
            return String.format("{\"modelId\":%d,\"supportsInference\":%b,\"supportsStreaming\":%b}", 
                    modelId, supportsInference(modelId), supportsStreaming(modelId));
        } catch (Exception e) {
            log.error("获取推理配置失败: modelId={}", modelId, e);
            return "{}";
        }
    }

    /**
     * 验证模型支持
     */
    private void validateModelSupport(Long modelId, boolean requireStreaming) {
        if (!dynamicModelManager.supportsInference(modelId)) {
            throw new IllegalArgumentException("模型不支持推理: modelId=" + modelId);
        }

        if (requireStreaming && !dynamicModelManager.supportsStreaming(modelId)) {
            throw new IllegalArgumentException("模型不支持流式推理: modelId=" + modelId);
        }
    }

    /**
     * 创建推理服务 - 核心简化逻辑
     * 框架自动处理所有复杂性：RAG、记忆、工具调用等
     */
    private com.leyue.smartcs.model.ai.ModelInferenceService createInferenceService(Long modelId, List<Long> knowledgeIds) {
        try {
            // 默认启用RAG增强的服务
            return serviceConfig.createModelInferenceService(modelId, knowledgeIds);
        } catch (Exception e) {
            log.error("创建推理服务失败: modelId={}, knowledgeIds={}", modelId, knowledgeIds, e);
            throw new BizException("无法创建推理服务: " + e.getMessage());
        }
    }
}