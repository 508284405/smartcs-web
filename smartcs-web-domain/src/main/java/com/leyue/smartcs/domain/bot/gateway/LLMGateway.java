package com.leyue.smartcs.domain.bot.gateway;

import java.util.List;
import java.util.Map;
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
    String generateAnswer(String prompt, Map<String, Object> options);
    
    /**
     * 流式生成回答
     * @param prompt 提示语
     * @param options 选项参数
     * @param chunkConsumer 流式数据消费者
     */
    void generateAnswerStream(String prompt, Map<String, Object> options, Consumer<String> chunkConsumer);
    
    /**
     * 生成嵌入向量
     * @param texts 文本列表
     * @return 嵌入向量列表（Base64编码）
     */
    List<float[]> generateEmbeddings(List<String> texts);
    
    /**
     * 获取可用模型列表
     * @return 模型ID列表
     */
    List<String> listAvailableModels();
} 