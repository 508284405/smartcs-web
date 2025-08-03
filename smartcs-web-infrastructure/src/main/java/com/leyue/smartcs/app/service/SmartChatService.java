package com.leyue.smartcs.app.service;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.TokenStream;

/**
 * 智能聊天服务 - 纯注解驱动的AI Service
 * 完全基于LangChain4j框架，避免自定义抽象
 */
public interface SmartChatService {

    /**
     * 同步聊天 - 自动RAG增强
     */
    @SystemMessage("""
        You are a helpful AI assistant with access to knowledge base.
        Provide accurate and helpful responses based on the available knowledge.
        """)
    @UserMessage("{{message}}")
    String chat(@MemoryId String sessionId, @V("message") String message);

    /**
     * 流式聊天 - 自动RAG增强
     */
    @SystemMessage("""
        You are a helpful AI assistant with access to knowledge base.
        Provide accurate and helpful responses based on the available knowledge.
        """)
    @UserMessage("{{message}}")
    TokenStream chatStream(@MemoryId String sessionId, @V("message") String message);

    /**
     * 带自定义系统提示的聊天
     */
    @SystemMessage("{{systemPrompt}}")
    @UserMessage("{{message}}")
    String chatWithCustomPrompt(@MemoryId String sessionId, 
                               @V("systemPrompt") String systemPrompt,
                               @V("message") String message);

    /**
     * 带自定义系统提示的流式聊天
     */
    @SystemMessage("{{systemPrompt}}")
    @UserMessage("{{message}}")
    TokenStream chatStreamWithCustomPrompt(@MemoryId String sessionId,
                                          @V("systemPrompt") String systemPrompt, 
                                          @V("message") String message);
}