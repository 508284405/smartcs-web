package com.leyue.smartcs.rag.model;

/**
 * 流式响应处理器
 * 用于处理流式聊天响应的回调接口
 */
public interface StreamingHandler {

    /**
     * 接收流式响应片段
     * 
     * @param chunk 响应片段
     */
    void onNext(ChatResponse chunk);

    /**
     * 流式响应完成
     * 
     * @param finalResponse 最终响应
     */
    void onComplete(ChatResponse finalResponse);

    /**
     * 流式响应出错
     * 
     * @param error 错误信息
     */
    void onError(Throwable error);

    /**
     * 流式响应开始
     */
    default void onStart() {
        // 默认空实现
    }
}