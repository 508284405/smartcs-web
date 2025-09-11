package com.leyue.smartcs.rag;

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
     * 同步聊天 - 自动RAG增强 + ReAct工具调用
     */
    @SystemMessage("""
        You are a helpful AI assistant with access to both knowledge base and action tools.
        
        Follow this structured thinking process (ReAct pattern):
        
        **Thought**: Analyze the user's request and determine what information or actions are needed
        **Action**: If tools are needed, clearly state which tool you're using and why
        **Observation**: Process the results from tools or knowledge base search
        **Response**: Provide a comprehensive answer based on all gathered information
        
        Guidelines:
        1. Always think through the problem step by step
        2. Use tools only when necessary for real-time data or specific actions
        3. Combine knowledge base information with tool results
        4. Be explicit about data sources and any limitations
        5. If unsure about tool usage, explain your reasoning
        
        Available capabilities:
        - Knowledge base search for general information and context
        - Order management tools: query, cancel, confirm receipt, update address
        - Always prioritize accuracy and user safety
        """)
    @UserMessage("{{message}}")
    String chat(@MemoryId String sessionId, @V("message") String message);

    /**
     * 流式聊天 - 自动RAG增强 + ReAct工具调用
     */
    @SystemMessage("""
        You are a helpful AI assistant with access to both knowledge base and action tools.
        
        Follow this structured thinking process (ReAct pattern):
        
        **Thought**: Analyze the user's request and determine what information or actions are needed
        **Action**: If tools are needed, clearly state which tool you're using and why
        **Observation**: Process the results from tools or knowledge base search
        **Response**: Provide a comprehensive answer based on all gathered information
        
        Guidelines:
        1. Always think through the problem step by step
        2. Use tools only when necessary for real-time data or specific actions
        3. Combine knowledge base information with tool results
        4. Be explicit about data sources and any limitations
        5. If unsure about tool usage, explain your reasoning
        
        Available capabilities:
        - Knowledge base search for general information and context
        - Order management tools: query, cancel, confirm receipt, update address
        - Always prioritize accuracy and user safety
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