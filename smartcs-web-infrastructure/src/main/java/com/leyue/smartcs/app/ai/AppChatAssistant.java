package com.leyue.smartcs.app.ai;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.TokenStream;

import java.util.Map;
import java.util.function.Consumer;

/**
 * AI应用聊天助手接口 - 基于AiServices的声明式实现
 * 完全基于LangChain4j框架，自动集成记忆管理和模板变量处理
 */
public interface AppChatAssistant {
    
    /**
     * 同步聊天 - 自动记忆管理
     * @param sessionId 会话ID
     * @param systemPrompt 系统提示词
     * @param userMessage 用户消息
     * @param variables 模板变量
     * @return AI响应
     */
    @SystemMessage("{{systemPrompt}}")
    @UserMessage("{{userMessage}}")
    String chat(@MemoryId String sessionId, 
                @V("systemPrompt") String systemPrompt, 
                @V("userMessage") String userMessage, 
                @V("variables") Map<String, Object> variables);
    
    /**
     * 流式聊天 - 自动记忆管理
     * @param sessionId 会话ID
     * @param systemPrompt 系统提示词
     * @param userMessage 用户消息
     * @param variables 模板变量
     * @return TokenStream用于流式处理
     */
    @SystemMessage("{{systemPrompt}}")
    @UserMessage("{{userMessage}}")
    TokenStream chatStream(@MemoryId String sessionId, 
                          @V("systemPrompt") String systemPrompt, 
                          @V("userMessage") String userMessage, 
                          @V("variables") Map<String, Object> variables);

    /**
     * 简化版同步聊天 - 使用默认系统提示
     * @param sessionId 会话ID
     * @param userMessage 用户消息
     * @return AI响应
     */
    @SystemMessage("You are a helpful AI assistant.")
    @UserMessage("{{userMessage}}")
    String simpleChat(@MemoryId String sessionId, @V("userMessage") String userMessage);

    /**
     * 简化版流式聊天 - 使用默认系统提示
     * @param sessionId 会话ID
     * @param userMessage 用户消息
     * @return TokenStream用于流式处理
     */
    @SystemMessage("You are a helpful AI assistant.")
    @UserMessage("{{userMessage}}")
    TokenStream simpleChatStream(@MemoryId String sessionId, @V("userMessage") String userMessage);
}