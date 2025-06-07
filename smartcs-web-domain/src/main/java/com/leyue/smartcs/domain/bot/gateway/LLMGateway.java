package com.leyue.smartcs.domain.bot.gateway;

import java.util.function.Consumer;

/**
 * LLM网关接口
 */
public interface LLMGateway {
    
    /**
     * 生成回答
     * @param prompt 提示语
     * @param options 选项参数
     * @return 生成的回答
     */
    String generateAnswer(String sessionId, String question, Long botId);
    
    /**
     * 流式生成回答
     * @param sessionId 会话ID
     * @param question 问题
     * @param botId 机器人ID
     * @param chunkConsumer 流式数据消费者
     */
    void generateAnswerStream(String sessionId, String question, Long botId, Consumer<String> chunkConsumer);
} 