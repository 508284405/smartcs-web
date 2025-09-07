package com.leyue.smartcs.model.ai;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.leyue.smartcs.domain.model.Model;
import com.leyue.smartcs.domain.model.Provider;
import com.leyue.smartcs.domain.model.enums.ProviderType;
import com.leyue.smartcs.domain.model.gateway.ModelGateway;
import com.leyue.smartcs.domain.model.gateway.ProviderGateway;
import com.leyue.smartcs.model.gateway.ModelProvider;
import com.leyue.smartcs.dto.app.RagComponentConfig;
import com.leyue.smartcs.model.convertor.ProviderConvertor;
import com.leyue.smartcs.model.dataobject.ProviderDO;
import com.leyue.smartcs.model.mapper.ProviderMapper;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.input.PromptTemplate;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import dev.langchain4j.http.client.jdk.JdkHttpClientBuilder;
import dev.langchain4j.model.scoring.ScoringModel;
// import dev.langchain4j.model.openai.OpenAiScoringModel; // 1.1.0版本暂未提供
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 动态模型管理器
 * 负责根据modelId动态获取和缓存ChatModel和StreamingChatModel实例
 * 实现ModelProvider端口接口，提供统一的模型实例获取能力
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DynamicModelManager implements ModelProvider {

    private final ModelGateway modelGateway;
    private final ProviderGateway providerGateway;
    private final ProviderMapper providerMapper;
    private final ProviderConvertor providerConvertor;
    
    // 缓存模型实例，避免重复创建
    private final Map<Long, ChatModel> chatModelCache = new ConcurrentHashMap<>();
    private final Map<Long, StreamingChatModel> streamingChatModelCache = new ConcurrentHashMap<>();
    private final Map<Long, EmbeddingModel> embeddingModelCache = new ConcurrentHashMap<>();
    private final Map<Long, ScoringModel> scoringModelCache = new ConcurrentHashMap<>();
    

    /**
     * 根据模型ID获取ChatModel
     * 
     * @param modelId 模型ID
     * @return ChatModel实例
     */
    public ChatModel getChatModel(Long modelId) {
        return chatModelCache.computeIfAbsent(modelId, id -> {
            log.debug("创建ChatModel实例: modelId={}", id);
            Model model = getModel(id);
            Provider provider = getProvider(model.getProviderId());
            return buildChatModel(provider, model);
        });
    }

    /**
     * 根据模型ID获取StreamingChatModel
     * 
     * @param modelId 模型ID
     * @return StreamingChatModel实例
     */
    public StreamingChatModel getStreamingChatModel(Long modelId) {
        return streamingChatModelCache.computeIfAbsent(modelId, id -> {
            log.debug("创建StreamingChatModel实例: modelId={}", id);
            Model model = getModel(id);
            Provider provider = getProvider(model.getProviderId());
            return buildStreamingChatModel(provider, model);
        });
    }

    /**
     * 根据模型ID获取EmbeddingModel
     * 
     * @param modelId 模型ID
     * @return EmbeddingModel实例
     */
    public EmbeddingModel getEmbeddingModel(Long modelId) {
        return embeddingModelCache.computeIfAbsent(modelId, id -> {
            log.debug("创建EmbeddingModel实例: modelId={}", id);
            Model model = getModel(id);
            Provider provider = getProvider(model.getProviderId());
            return buildEmbeddingModel(provider, model);
        });
    }

    /**
     * 根据模型ID获取ScoringModel
     * 
     * @param modelId 模型ID
     * @return ScoringModel实例
     */
    public ScoringModel getScoringModel(Long modelId) {
        return scoringModelCache.computeIfAbsent(modelId, id -> {
            log.debug("创建ScoringModel实例: modelId={}", id);
            Model model = getModel(id);
            Provider provider = getProvider(model.getProviderId());
            return buildScoringModel(provider, model);
        });
    }

    /**
     * 检查模型是否支持推理
     * 
     * @param modelId 模型ID
     * @return 是否支持推理
     */
    public boolean supportsInference(Long modelId) {
        try {
            Model model = getModel(modelId);
            return model != null && model.getStatus() != null 
                   && model.getStatus().getCode().equals("active");
        } catch (Exception e) {
            log.warn("检查模型推理支持失败: modelId={}", modelId, e);
            return false;
        }
    }

    /**
     * 检查模型是否支持流式推理
     * 
     * @param modelId 模型ID
     * @return 是否支持流式推理
     */
    public boolean supportsStreaming(Long modelId) {
        try {
            StreamingChatModel streamingChatModel = getStreamingChatModel(modelId);
            return streamingChatModel != null;
        } catch (Exception e) {
            log.warn("检查模型流式推理支持失败: modelId={}", modelId, e);
            return false;
        }
    }

    /**
     * 清除指定模型的缓存
     * 
     * @param modelId 模型ID
     */
    public void clearModelCache(Long modelId) {
        log.info("清除模型缓存: modelId={}", modelId);
        chatModelCache.remove(modelId);
        streamingChatModelCache.remove(modelId);
        embeddingModelCache.remove(modelId);
        scoringModelCache.remove(modelId);
    }

    /**
     * 清除所有模型缓存
     */
    public void clearAllCache() {
        log.info("清除所有模型缓存");
        chatModelCache.clear();
        streamingChatModelCache.clear();
        embeddingModelCache.clear();
        scoringModelCache.clear();
        
    }

    /**
     * 获取缓存统计信息
     * 
     * @return 缓存大小映射
     */
    public Map<String, Integer> getCacheStats() {
        Map<String, Integer> stats = new ConcurrentHashMap<>();
        stats.put("chatModelCache", chatModelCache.size());
        stats.put("streamingChatModelCache", streamingChatModelCache.size());
        stats.put("embeddingModelCache", embeddingModelCache.size());
        stats.put("scoringModelCache", scoringModelCache.size());
        return stats;
    }

    /**
     * 获取模型信息
     */
    private Model getModel(Long modelId) {
        return modelGateway.findById(modelId)
                .orElseThrow(() -> new IllegalArgumentException("模型不存在: modelId=" + modelId));
    }

    /**
     * 获取提供商信息
     */
    private Provider getProvider(Long providerId) {
        return providerGateway.findById(providerId)
                .orElseThrow(() -> new IllegalArgumentException("提供商不存在: providerId=" + providerId));
    }

    /**
     * 构建ChatModel实例
     * 使用即时解密获取API Key，使用后立即释放引用
     */
    private ChatModel buildChatModel(Provider provider, Model model) {
        if (provider.getProviderType().isOpenAiCompatible()) {
            // 获取Provider DO用于解密
            ProviderDO providerDO = providerMapper.selectById(provider.getId());
            if (providerDO == null) {
                throw new IllegalStateException("提供商数据不存在: " + provider.getId());
            }
            
            // 即时解密API Key
            String apiKey = providerConvertor.decryptApiKey(providerDO);
            if (apiKey == null || apiKey.trim().isEmpty()) {
                throw new IllegalStateException("API Key未设置或解密失败: " + provider.getId());
            }
            
            try {
                // 构建模型实例
                ChatModel chatModel = OpenAiChatModel.builder()
                        .baseUrl(provider.getEndpoint())
                        .apiKey(apiKey)
                        .modelName(model.getLabel())
                        .build();
                
                log.debug("ChatModel构建完成，Provider ID: {}, Model: {}", provider.getId(), model.getLabel());
                return chatModel;
                
            } finally {
                // 立即清空明文API Key引用
                apiKey = null;
            }
        } else if (provider.getProviderType() == ProviderType.OLLAMA) {
            return buildOllamaChatModel(provider, model);
        }
        throw new IllegalStateException("不支持的提供商类型: " + provider.getProviderType().getKey());
    }

    /**
     * 构建StreamingChatModel实例
     * 使用即时解密获取API Key，使用后立即释放引用
     */
    private StreamingChatModel buildStreamingChatModel(Provider provider, Model model) {
        if (provider.getProviderType().isOpenAiCompatible()) {
            // 获取Provider DO用于解密
            ProviderDO providerDO = providerMapper.selectById(provider.getId());
            if (providerDO == null) {
                throw new IllegalStateException("提供商数据不存在: " + provider.getId());
            }
            
            // 即时解密API Key
            String apiKey = providerConvertor.decryptApiKey(providerDO);
            if (apiKey == null || apiKey.trim().isEmpty()) {
                throw new IllegalStateException("API Key未设置或解密失败: " + provider.getId());
            }
            
            try {
                // 构建模型实例
                StreamingChatModel streamingChatModel = OpenAiStreamingChatModel.builder()
                        .baseUrl(provider.getEndpoint())
                        .apiKey(apiKey)
                        .modelName(model.getLabel())
                        .build();
                
                log.debug("StreamingChatModel构建完成，Provider ID: {}, Model: {}", provider.getId(), model.getLabel());
                return streamingChatModel;
                
            } finally {
                // 立即清空明文API Key引用
                apiKey = null;
            }
        } else if (provider.getProviderType() == ProviderType.OLLAMA) {
            return buildOllamaStreamingChatModel(provider, model);
        }
        throw new IllegalStateException("不支持的提供商类型: " + provider.getProviderType().getKey());
    }

    /**
     * 构建EmbeddingModel实例
     * 使用即时解密获取API Key，使用后立即释放引用
     */
    private EmbeddingModel buildEmbeddingModel(Provider provider, Model model) {
        if (provider.getProviderType().isOpenAiCompatible()) {
            // 获取Provider DO用于解密
            ProviderDO providerDO = providerMapper.selectById(provider.getId());
            if (providerDO == null) {
                throw new IllegalStateException("提供商数据不存在: " + provider.getId());
            }
            
            // 即时解密API Key
            String apiKey = providerConvertor.decryptApiKey(providerDO);
            if (apiKey == null || apiKey.trim().isEmpty()) {
                throw new IllegalStateException("API Key未设置或解密失败: " + provider.getId());
            }
            
            try {
                // 构建模型实例
                EmbeddingModel embeddingModel = OpenAiEmbeddingModel.builder()
                        .baseUrl(provider.getEndpoint())
                        .apiKey(apiKey)
                        .modelName(model.getLabel())
                        .build();
                
                log.debug("EmbeddingModel构建完成，Provider ID: {}, Model: {}", provider.getId(), model.getLabel());
                return embeddingModel;
                
            } finally {
                // 立即清空明文API Key引用
                apiKey = null;
            }
        } else if (provider.getProviderType() == ProviderType.OLLAMA) {
            return buildOllamaEmbeddingModel(provider, model);
        }
        throw new IllegalStateException("不支持的提供商类型: " + provider.getProviderType().getKey());
    }

    /**
     * 构建ScoringModel实例
     * 使用基于LLM的自定义ScoringModel实现，通过ChatModel进行相关性打分
     */
    private ScoringModel buildScoringModel(Provider provider, Model model) {
        if (provider.getProviderType().isOpenAiCompatible()) {
            try {
                ChatModel chatModel = buildChatModel(provider, model);
                return new LlmBasedScoringModel(chatModel);
            } catch (Exception e) {
                log.warn("创建LlmBasedScoringModel失败，返回null: {}", e.getMessage());
                return null;
            }
        }
        log.warn("不支持的提供商类型用于ScoringModel: {}", provider.getProviderType().getKey());
        return null;
    }

    /**
     * 构建Ollama ChatModel实例
     */
    private ChatModel buildOllamaChatModel(Provider provider, Model model) {
        try {
            log.info("构建Ollama ChatModel: providerId={}, endpoint={}, model={}", 
                    provider.getId(), provider.getEndpoint(), model.getLabel());
                    
            OllamaChatModel chatModel = OllamaChatModel.builder()
                    .baseUrl(provider.getEndpoint())
                    .modelName(model.getLabel())
                    .httpClientBuilder(new JdkHttpClientBuilder())
                    .timeout(Duration.ofMinutes(2))
                    .build();
                    
            log.debug("Ollama ChatModel构建成功");
            return chatModel;
            
        } catch (Exception e) {
            log.error("Ollama ChatModel构建失败: providerId={}, endpoint={}, model={}, error={}", 
                    provider.getId(), provider.getEndpoint(), model.getLabel(), e.getMessage(), e);
            throw new IllegalStateException(
                String.format("Ollama服务不可用或模型未下载: %s at %s (检查服务状态: ollama list)", 
                             model.getLabel(), provider.getEndpoint()), e);
        }
    }

    /**
     * 构建Ollama StreamingChatModel实例
     */
    private StreamingChatModel buildOllamaStreamingChatModel(Provider provider, Model model) {
        try {
            log.info("构建Ollama StreamingChatModel: providerId={}, endpoint={}, model={}", 
                    provider.getId(), provider.getEndpoint(), model.getLabel());
                    
            OllamaStreamingChatModel streamingChatModel = OllamaStreamingChatModel.builder()
                    .baseUrl(provider.getEndpoint())
                    .modelName(model.getLabel())
                    .httpClientBuilder(new JdkHttpClientBuilder())
                    .timeout(Duration.ofMinutes(2))
                    .build();
                    
            log.debug("Ollama StreamingChatModel构建成功");
            return streamingChatModel;
            
        } catch (Exception e) {
            log.error("Ollama StreamingChatModel构建失败: providerId={}, endpoint={}, model={}, error={}", 
                    provider.getId(), provider.getEndpoint(), model.getLabel(), e.getMessage(), e);
            throw new IllegalStateException(
                String.format("Ollama服务不可用或模型未下载: %s at %s (检查服务状态: ollama list)", 
                             model.getLabel(), provider.getEndpoint()), e);
        }
    }

    /**
     * 构建Ollama EmbeddingModel实例
     */
    private EmbeddingModel buildOllamaEmbeddingModel(Provider provider, Model model) {
        try {
            log.info("构建Ollama EmbeddingModel: providerId={}, endpoint={}, model={}", 
                    provider.getId(), provider.getEndpoint(), model.getLabel());
                    
            OllamaEmbeddingModel embeddingModel = OllamaEmbeddingModel.builder()
                    .baseUrl(provider.getEndpoint())
                    .modelName(model.getLabel())
                    .httpClientBuilder(new JdkHttpClientBuilder())
                    .timeout(Duration.ofMinutes(2))
                    .build();
                    
            log.debug("Ollama EmbeddingModel构建成功");
            return embeddingModel;
            
        } catch (Exception e) {
            log.error("Ollama EmbeddingModel构建失败: providerId={}, endpoint={}, model={}, error={}", 
                    provider.getId(), provider.getEndpoint(), model.getLabel(), e.getMessage(), e);
            throw new IllegalStateException(
                String.format("Ollama服务不可用或模型未下载: %s at %s (检查服务状态: ollama list)", 
                             model.getLabel(), provider.getEndpoint()), e);
        }
    }

}
