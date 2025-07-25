package com.leyue.smartcs.knowledge.parser.impl;

import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.config.ModelBeanManagerService;
import com.leyue.smartcs.domain.model.Model;
import com.leyue.smartcs.domain.model.Provider;
import com.leyue.smartcs.domain.model.gateway.ModelGateway;
import com.leyue.smartcs.domain.model.gateway.ProviderGateway;
import com.leyue.smartcs.dto.knowledge.ModelRequest;
import com.leyue.smartcs.knowledge.parser.DocumentParser;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;

public abstract class AbstractDocumentParser implements DocumentParser {
    protected ModelGateway modelGateway;
    protected ProviderGateway providerGateway;
    protected ModelBeanManagerService modelBeanManagerService;

    /**
     * 获取ChatModel
     * 
     * @param modelId 模型ID
     * @return ChatModel
     */
    public ChatModel getChatModel(Long modelId) {
        Model model = modelGateway.findById(modelId).orElse(null);
        if (model == null) {
            throw new BizException("Model not found");
        }
        Provider provider = providerGateway.findById(model.getProviderId()).orElse(null);
        if (provider == null) {
            throw new BizException("Provider not found");
        }
        if (!provider.isValid()) {
            throw new BizException("Provider is not valid");
        }
        Object modelBean = modelBeanManagerService.getModelBean(provider, "chat");
        if (modelBean instanceof ChatModel) {
            return (ChatModel) modelBean;
        }
        throw new BizException("Model bean is not a ChatModel");
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
