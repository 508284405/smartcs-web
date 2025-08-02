package com.leyue.smartcs.domain.app.model;

/**
 * 流式响应处理器接口
 * 定义流式聊天的回调机制，技术无关的纯业务接口
 */
public interface StreamingHandler {
    
    /**
     * 处理流式数据片段
     * 
     * @param token 数据片段
     */
    void onToken(String token);
    
    /**
     * 处理完成事件
     * 
     * @param fullResponse 完整响应内容
     */
    void onComplete(String fullResponse);
    
    /**
     * 处理错误事件
     * 
     * @param error 错误信息
     */
    void onError(Throwable error);
    
    /**
     * 创建简单的流式处理器
     */
    static StreamingHandler simple(TokenConsumer onToken, CompletionConsumer onComplete, ErrorConsumer onError) {
        return new StreamingHandler() {
            @Override
            public void onToken(String token) {
                if (onToken != null) {
                    onToken.accept(token);
                }
            }
            
            @Override
            public void onComplete(String fullResponse) {
                if (onComplete != null) {
                    onComplete.accept(fullResponse);
                }
            }
            
            @Override
            public void onError(Throwable error) {
                if (onError != null) {
                    onError.accept(error);
                }
            }
        };
    }
    
    /**
     * Token消费者函数式接口
     */
    @FunctionalInterface
    interface TokenConsumer {
        void accept(String token);
    }
    
    /**
     * 完成消费者函数式接口
     */
    @FunctionalInterface
    interface CompletionConsumer {
        void accept(String fullResponse);
    }
    
    /**
     * 错误消费者函数式接口
     */
    @FunctionalInterface
    interface ErrorConsumer {
        void accept(Throwable error);
    }
}