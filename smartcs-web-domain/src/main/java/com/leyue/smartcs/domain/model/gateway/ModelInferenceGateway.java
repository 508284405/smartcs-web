package com.leyue.smartcs.domain.model.gateway;

import java.util.function.Consumer;

/**
 * 模型推理网关接口
 */
public interface ModelInferenceGateway {
    
    /**
     * 同步推理
     * 
     * @param modelId 模型ID
     * @param message 输入消息
     * @param sessionId 会话ID
     * @param systemPrompt 系统Prompt
     * @param enableRAG 是否启用RAG
     * @param knowledgeId 知识库ID
     * @param inferenceParams 推理参数（JSON格式）
     * @param saveToContext 是否保存到上下文
     * @return 推理结果
     */
    String infer(Long modelId, String message, String sessionId, String systemPrompt, 
                Boolean enableRAG, Long knowledgeId, String inferenceParams, Boolean saveToContext);
    
    /**
     * 流式推理
     * 
     * @param modelId 模型ID
     * @param message 输入消息
     * @param sessionId 会话ID
     * @param systemPrompt 系统Prompt
     * @param enableRAG 是否启用RAG
     * @param knowledgeId 知识库ID
     * @param inferenceParams 推理参数（JSON格式）
     * @param saveToContext 是否保存到上下文
     * @param chunkConsumer 流式数据处理器
     */
    void inferStream(Long modelId, String message, String sessionId, String systemPrompt,
                    Boolean enableRAG, Long knowledgeId, String inferenceParams, Boolean saveToContext,
                    Consumer<String> chunkConsumer);
    
    /**
     * 检查模型是否支持推理
     * 
     * @param modelId 模型ID
     * @return 是否支持
     */
    boolean supportsInference(Long modelId);
    
    /**
     * 检查模型是否支持流式推理
     * 
     * @param modelId 模型ID
     * @return 是否支持
     */
    boolean supportsStreaming(Long modelId);
    
    /**
     * 获取模型推理配置
     * 
     * @param modelId 模型ID
     * @return 推理配置（JSON格式）
     */
    String getInferenceConfig(Long modelId);
}