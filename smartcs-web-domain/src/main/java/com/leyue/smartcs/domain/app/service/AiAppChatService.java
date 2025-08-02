package com.leyue.smartcs.domain.app.service;

import com.leyue.smartcs.domain.app.model.ChatRequest;
import com.leyue.smartcs.domain.app.model.ChatResponse;
import com.leyue.smartcs.domain.app.model.StreamingHandler;

/**
 * AI应用聊天领域服务接口
 * 纯业务接口，不依赖任何技术框架
 * 定义聊天的核心业务能力
 */
public interface AiAppChatService {

    /**
     * 同步聊天
     * 
     * @param request 聊天请求
     * @return 聊天响应
     */
    ChatResponse chat(ChatRequest request);

    /**
     * 流式聊天
     * 
     * @param request 聊天请求
     * @param handler 流式响应处理器
     */
    void chatStream(ChatRequest request, StreamingHandler handler);

    /**
     * 带RAG的同步聊天
     * 系统会自动检索相关知识并注入到上下文中
     * 
     * @param request 聊天请求（需包含知识库ID）
     * @return 聊天响应
     */
    ChatResponse chatWithRag(ChatRequest request);

    /**
     * 带RAG的流式聊天
     * 
     * @param request 聊天请求（需包含知识库ID）
     * @param handler 流式响应处理器
     */
    void chatWithRagStream(ChatRequest request, StreamingHandler handler);

    /**
     * 检查聊天服务是否可用
     * 
     * @return 是否可用
     */
    boolean isAvailable();

    /**
     * 获取服务状态信息
     * 
     * @return 状态描述
     */
    String getStatus();
}