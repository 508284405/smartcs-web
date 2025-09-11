package com.leyue.smartcs.model.ai;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * 模型推理AI服务
 * 基于LangChain4j框架的声明式AI服务接口
 * 自动集成RAG、记忆管理和流式响应
 */
public interface ModelInferenceService {

    /**
     * 同步推理 - 支持RAG增强
     * 
     * @param sessionId 会话ID，用于记忆管理
     * @param message 用户消息
     * @param systemPrompt 系统提示词
     * @return AI响应
     */
    @SystemMessage("{{systemPrompt}}")
    @UserMessage("{{message}}")
    String chat(@MemoryId String sessionId, 
                @V("message") String message, 
                @V("systemPrompt") String systemPrompt);

    /**
     * 流式推理 - 支持RAG增强和实时响应
     * 
     * @param sessionId 会话ID，用于记忆管理
     * @param message 用户消息
     * @param systemPrompt 系统提示词
     * @return TokenStream用于流式处理
     */
    @SystemMessage("{{systemPrompt}}")
    @UserMessage("{{message}}")
    TokenStream chatStream(@MemoryId String sessionId, 
                          @V("message") String message, 
                          @V("systemPrompt") String systemPrompt);

    /**
     * 简化版同步推理 - 使用默认系统提示
     * 
     * @param sessionId 会话ID
     * @param message 用户消息
     * @return AI响应
     */
    @SystemMessage("You are a helpful AI assistant.")
    @UserMessage("{{message}}")
    String simpleChat(@MemoryId String sessionId, @V("message") String message);

    /**
     * 简化版流式推理 - 使用默认系统提示
     * 
     * @param sessionId 会话ID
     * @param message 用户消息
     * @return TokenStream用于流式处理
     */
    @SystemMessage("You are a helpful AI assistant.")
    @UserMessage("{{message}}")
    TokenStream simpleChatStream(@MemoryId String sessionId, @V("message") String message);
}