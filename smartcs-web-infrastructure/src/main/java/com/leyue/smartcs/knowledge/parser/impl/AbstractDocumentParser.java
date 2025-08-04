package com.leyue.smartcs.knowledge.parser.impl;

import com.leyue.smartcs.domain.model.gateway.ModelGateway;
import com.leyue.smartcs.domain.model.gateway.ProviderGateway;
import com.leyue.smartcs.dto.knowledge.ModelRequest;
import com.leyue.smartcs.knowledge.parser.DocumentParser;
import com.leyue.smartcs.model.ai.DynamicModelManager;

import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class AbstractDocumentParser implements DocumentParser {
    @Autowired
    protected ModelGateway modelGateway;
    @Autowired
    protected ProviderGateway providerGateway;
    @Autowired
    protected DynamicModelManager dynamicModelManager;

    /**
     * 获取ChatModel
     * 
     * @param modelId 模型ID
     * @return ChatModel
     */
    public StreamingChatModel getChatModel(Long modelId) {
        return dynamicModelManager.getStreamingChatModel(modelId);
    }

    /**
     * 获取ChatRequest
     * 
     * @param modelRequest 模型请求
     * @return ChatRequest
     */
    public ChatRequest.Builder getChatRequest(ModelRequest modelRequest) {
        return ChatRequest.builder()
                .modelName(modelRequest.getModelName())
                .temperature(modelRequest.getTemperature())
                .topP(modelRequest.getTopP())
                .topK(modelRequest.getTopK())
                .frequencyPenalty(modelRequest.getFrequencyPenalty())
                .presencePenalty(modelRequest.getPresencePenalty())
                .maxOutputTokens(modelRequest.getMaxOutputTokens());
    }
}
