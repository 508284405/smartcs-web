package com.leyue.smartcs.domain.app.service;

import com.leyue.smartcs.domain.app.model.StructuredChatResponse;

import java.util.Map;

/**
 * 结构化聊天领域服务接口
 * 纯业务接口，不依赖任何技术框架
 * 提供结构化响应生成能力
 */
public interface StructuredChatService {

    /**
     * 生成结构化响应
     * 
     * @param sessionId 会话ID
     * @param systemPrompt 系统提示词
     * @param userMessage 用户消息
     * @param variables 模板变量
     * @return 结构化响应
     */
    StructuredChatResponse generateStructuredResponse(String sessionId, String systemPrompt, 
                                                    String userMessage, Map<String, Object> variables);

    /**
     * 生成知识问答的结构化响应
     * 
     * @param sessionId 会话ID
     * @param userMessage 用户消息
     * @param knowledgeBaseId 知识库ID
     * @return 结构化响应
     */
    StructuredChatResponse generateKnowledgeBasedResponse(String sessionId, String userMessage, 
                                                        Long knowledgeBaseId);

    /**
     * 分析用户意图并生成结构化响应
     * 
     * @param userMessage 用户消息
     * @param context 上下文信息
     * @return 结构化响应
     */
    StructuredChatResponse analyzeIntentAndRespond(String userMessage, String context);

    /**
     * 生成错误处理的结构化响应
     * 
     * @param errorMessage 错误信息
     * @param userMessage 用户原始消息
     * @return 结构化错误响应
     */
    StructuredChatResponse generateErrorResponse(String errorMessage, String userMessage);

    /**
     * 检查结构化聊天服务是否可用
     * 
     * @return 是否可用
     */
    boolean isAvailable();

    /**
     * 验证结构化响应的完整性
     * 
     * @param response 响应对象
     * @return 是否有效
     */
    boolean validateResponse(StructuredChatResponse response);
}