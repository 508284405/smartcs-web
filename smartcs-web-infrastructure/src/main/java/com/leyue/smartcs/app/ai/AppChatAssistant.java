package com.leyue.smartcs.app.ai;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.output.Response;
import java.util.Map;
import java.util.function.Consumer;

/**
 * AI应用聊天助手接口
 * 简化的聊天助手接口，支持同步和流式聊天
 */
public interface AppChatAssistant {
    
    /**
     * 同步聊天
     * @param sessionId 会话ID
     * @param systemPrompt 系统提示词
     * @param userMessage 用户消息
     * @param variables 模板变量
     * @return AI响应
     */
    String chat(String sessionId, String systemPrompt, String userMessage, Map<String, Object> variables);
    
    /**
     * 流式聊天
     * @param sessionId 会话ID
     * @param systemPrompt 系统提示词
     * @param userMessage 用户消息
     * @param variables 模板变量
     * @param onNext 接收流式token的回调
     * @param onComplete 完成时的回调
     * @param onError 出错时的回调
     */
    void chatStream(String sessionId, String systemPrompt, String userMessage, Map<String, Object> variables,
                   Consumer<String> onNext, Consumer<Response<AiMessage>> onComplete, Consumer<Throwable> onError);
}