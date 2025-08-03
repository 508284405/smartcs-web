package com.leyue.smartcs.app.service;

import com.leyue.smartcs.domain.app.model.StructuredChatResponse;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.TokenStream;

import java.util.Map;

/**
 * 结构化聊天服务 - 基于AiServices的声明式实现
 * 完全基于LangChain4j框架，自动集成记忆管理和结构化输出
 */
public interface StructuredChatServiceAi {

    /**
     * 生成结构化响应 - 自动记忆管理
     * 
     * @param sessionId 会话ID
     * @param systemPrompt 系统提示词
     * @param userMessage 用户消息
     * @param variables 模板变量
     * @return 结构化响应
     */
    @SystemMessage("""
        {{systemPrompt}}
        
        You are a helpful AI assistant that provides structured responses.
        Analyze the user's message and provide a well-structured response.
        """)
    @UserMessage("{{userMessage}}")
    StructuredChatResponse generateStructuredResponse(@MemoryId String sessionId, 
                                                    @V("systemPrompt") String systemPrompt,
                                                    @V("userMessage") String userMessage, 
                                                    @V("variables") Map<String, Object> variables);

    /**
     * 生成知识问答的结构化响应 - 自动RAG增强
     * 
     * @param sessionId 会话ID
     * @param userMessage 用户消息
     * @param knowledgeBaseId 知识库ID
     * @return 结构化响应
     */
    @SystemMessage("""
        You are a helpful AI assistant with access to knowledge base.
        Provide accurate and structured responses based on the available knowledge.
        When answering questions, always cite your sources and provide structured information.
        """)
    @UserMessage("{{userMessage}}")
    StructuredChatResponse generateKnowledgeBasedResponse(@MemoryId String sessionId, 
                                                        @V("userMessage") String userMessage, 
                                                        @V("knowledgeBaseId") Long knowledgeBaseId);

    /**
     * 分析用户意图并生成结构化响应
     * 
     * @param userMessage 用户消息
     * @param context 上下文信息
     * @return 结构化响应
     */
    @SystemMessage("""
        You are an AI assistant that analyzes user intent and provides structured responses.
        Analyze the user's message and context to understand their intent.
        Provide a structured response with clear categorization and suggestions.
        """)
    @UserMessage("User message: {{userMessage}}\nContext: {{context}}")
    StructuredChatResponse analyzeIntentAndRespond(@V("userMessage") String userMessage, 
                                                  @V("context") String context);

    /**
     * 生成错误处理的结构化响应
     * 
     * @param errorMessage 错误信息
     * @param userMessage 用户原始消息
     * @return 结构化错误响应
     */
    @SystemMessage("""
        You are an AI assistant that handles errors gracefully.
        When an error occurs, provide a structured error response that explains the issue
        and suggests possible solutions or alternatives.
        """)
    @UserMessage("Error: {{errorMessage}}\nOriginal user message: {{userMessage}}")
    StructuredChatResponse generateErrorResponse(@V("errorMessage") String errorMessage, 
                                               @V("userMessage") String userMessage);

    /**
     * 简化版结构化聊天 - 使用默认系统提示
     * 
     * @param sessionId 会话ID
     * @param userMessage 用户消息
     * @return 结构化响应
     */
    @SystemMessage("""
        You are a helpful AI assistant that provides structured responses.
        Analyze the user's message and provide a well-structured response with clear organization.
        """)
    @UserMessage("{{userMessage}}")
    StructuredChatResponse simpleStructuredChat(@MemoryId String sessionId, 
                                               @V("userMessage") String userMessage);

    /**
     * 流式结构化聊天 - 自动记忆管理
     * 
     * @param sessionId 会话ID
     * @param systemPrompt 系统提示词
     * @param userMessage 用户消息
     * @param variables 模板变量
     * @return TokenStream用于流式处理
     */
    @SystemMessage("{{systemPrompt}}")
    @UserMessage("{{userMessage}}")
    TokenStream structuredChatStream(@MemoryId String sessionId, 
                                    @V("systemPrompt") String systemPrompt,
                                    @V("userMessage") String userMessage, 
                                    @V("variables") Map<String, Object> variables);
} 