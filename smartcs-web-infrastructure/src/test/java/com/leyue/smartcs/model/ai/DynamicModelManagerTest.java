package com.leyue.smartcs.model.ai;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.leyue.smartcs.domain.model.Model;
import com.leyue.smartcs.domain.model.Provider;
import com.leyue.smartcs.domain.model.enums.ModelStatus;
import com.leyue.smartcs.domain.model.enums.ModelType;
import com.leyue.smartcs.domain.model.enums.ProviderType;
import com.leyue.smartcs.domain.model.gateway.ModelGateway;
import com.leyue.smartcs.domain.model.gateway.ProviderGateway;
import com.leyue.smartcs.model.convertor.ProviderConvertor;
import com.leyue.smartcs.model.dataobject.ProviderDO;
import com.leyue.smartcs.model.mapper.ProviderMapper;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;

/**
 * DynamicModelManager 测试类
 * 重点测试 Ollama 提供商的集成
 */
@ExtendWith(MockitoExtension.class)
class DynamicModelManagerTest {

    @Mock
    private ModelGateway modelGateway;
    
    @Mock
    private ProviderGateway providerGateway;
    
    @Mock
    private ProviderMapper providerMapper;
    
    @Mock
    private ProviderConvertor providerConvertor;
    
    @InjectMocks
    private DynamicModelManager dynamicModelManager;
    
    private Provider ollamaProvider;
    private Model testModel;
    
    @BeforeEach
    void setUp() {
        // 创建 Ollama Provider
        ollamaProvider = new Provider();
        ollamaProvider.setId(1L);
        ollamaProvider.setProviderType(ProviderType.OLLAMA);
        ollamaProvider.setEndpoint("http://localhost:11434");
        ollamaProvider.setLabel("Local Ollama");
        
        // 创建测试模型
        testModel = new Model();
        testModel.setId(100L);
        testModel.setProviderId(1L);
        testModel.setLabel("llama3");
        testModel.setModelType(List.of(ModelType.LLM));
        testModel.setStatus(ModelStatus.ACTIVE);
    }
    
    @Test
    void testGetChatModel_WithOllamaProvider_ShouldReturnOllamaChatModel() {
        // Given
        when(modelGateway.findById(100L)).thenReturn(Optional.of(testModel));
        when(providerGateway.findById(1L)).thenReturn(Optional.of(ollamaProvider));
        
        // When
        ChatModel chatModel = dynamicModelManager.getChatModel(100L);
        
        // Then
        assertNotNull(chatModel, "ChatModel 不应为空");
        assertTrue(chatModel instanceof OllamaChatModel, "应该返回 OllamaChatModel 实例");
        
        verify(modelGateway).findById(100L);
        verify(providerGateway).findById(1L);
        // Ollama 不需要解密操作，所以不会调用 providerMapper 和 convertor
        verify(providerMapper, never()).selectById(anyLong());
        verify(providerConvertor, never()).decryptApiKey(any());
    }
    
    @Test
    void testGetStreamingChatModel_WithOllamaProvider_ShouldReturnOllamaStreamingChatModel() {
        // Given
        when(modelGateway.findById(100L)).thenReturn(Optional.of(testModel));
        when(providerGateway.findById(1L)).thenReturn(Optional.of(ollamaProvider));
        
        // When
        StreamingChatModel streamingChatModel = dynamicModelManager.getStreamingChatModel(100L);
        
        // Then
        assertNotNull(streamingChatModel, "StreamingChatModel 不应为空");
        assertTrue(streamingChatModel instanceof OllamaStreamingChatModel, "应该返回 OllamaStreamingChatModel 实例");
        
        verify(modelGateway).findById(100L);
        verify(providerGateway).findById(1L);
    }
    
    @Test
    void testGetEmbeddingModel_WithOllamaProvider_ShouldReturnOllamaEmbeddingModel() {
        // Given
        testModel.setModelType(List.of(ModelType.TEXT_EMBEDDING));
        testModel.setLabel("nomic-embed-text");
        when(modelGateway.findById(100L)).thenReturn(Optional.of(testModel));
        when(providerGateway.findById(1L)).thenReturn(Optional.of(ollamaProvider));
        
        // When
        EmbeddingModel embeddingModel = dynamicModelManager.getEmbeddingModel(100L);
        
        // Then
        assertNotNull(embeddingModel, "EmbeddingModel 不应为空");
        assertTrue(embeddingModel instanceof OllamaEmbeddingModel, "应该返回 OllamaEmbeddingModel 实例");
        
        verify(modelGateway).findById(100L);
        verify(providerGateway).findById(1L);
    }
    
    @Test
    void testGetChatModel_WithInvalidModelId_ShouldThrowException() {
        // Given
        Long invalidModelId = 999L;
        when(modelGateway.findById(invalidModelId)).thenReturn(Optional.empty());
        
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            dynamicModelManager.getChatModel(invalidModelId);
        });
        
        assertEquals("模型不存在: modelId=999", exception.getMessage());
        verify(modelGateway).findById(invalidModelId);
    }
    
    @Test
    void testGetChatModel_WithInvalidProviderId_ShouldThrowException() {
        // Given
        testModel.setProviderId(999L);
        when(modelGateway.findById(100L)).thenReturn(Optional.of(testModel));
        when(providerGateway.findById(999L)).thenReturn(Optional.empty());
        
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            dynamicModelManager.getChatModel(100L);
        });
        
        assertEquals("提供商不存在: providerId=999", exception.getMessage());
        verify(modelGateway).findById(100L);
        verify(providerGateway).findById(999L);
    }
    
    @Test
    void testClearModelCache_ShouldClearSpecificModelCache() {
        // Given
        when(modelGateway.findById(100L)).thenReturn(Optional.of(testModel));
        when(providerGateway.findById(1L)).thenReturn(Optional.of(ollamaProvider));
        
        // 先创建一个模型实例以确保缓存中有数据
        ChatModel chatModel = dynamicModelManager.getChatModel(100L);
        assertNotNull(chatModel);
        
        // When
        dynamicModelManager.clearModelCache(100L);
        
        // 再次获取应该重新创建实例
        ChatModel newChatModel = dynamicModelManager.getChatModel(100L);
        assertNotNull(newChatModel);
        
        // 验证调用了两次构建方法（说明缓存被清除）
        verify(modelGateway, times(2)).findById(100L);
        verify(providerGateway, times(2)).findById(1L);
    }
    
    @Test
    void testClearAllCache_ShouldClearAllModelCaches() {
        // Given
        when(modelGateway.findById(100L)).thenReturn(Optional.of(testModel));
        when(providerGateway.findById(1L)).thenReturn(Optional.of(ollamaProvider));
        
        // 创建多种类型的模型实例
        ChatModel chatModel = dynamicModelManager.getChatModel(100L);
        StreamingChatModel streamingChatModel = dynamicModelManager.getStreamingChatModel(100L);
        EmbeddingModel embeddingModel = dynamicModelManager.getEmbeddingModel(100L);
        
        assertNotNull(chatModel);
        assertNotNull(streamingChatModel);
        assertNotNull(embeddingModel);
        
        // When
        dynamicModelManager.clearAllCache();
        
        // 验证缓存统计信息显示缓存已清空（这个方法检查实际的缓存大小）
        var cacheStats = dynamicModelManager.getCacheStats();
        assertEquals(0, cacheStats.get("chatModelCache"));
        assertEquals(0, cacheStats.get("streamingChatModelCache"));
        assertEquals(0, cacheStats.get("embeddingModelCache"));
    }
    
    @Test
    void testSupportsInference_WithValidModel_ShouldReturnTrue() {
        // Given
        testModel.setStatus(ModelStatus.ACTIVE);
        
        when(modelGateway.findById(100L)).thenReturn(Optional.of(testModel));
        
        // When
        boolean supportsInference = dynamicModelManager.supportsInference(100L);
        
        // Then
        assertTrue(supportsInference, "活跃状态的模型应该支持推理");
        verify(modelGateway).findById(100L);
    }
    
    @Test
    void testSupportsStreaming_WithOllamaModel_ShouldReturnTrue() {
        // Given
        when(modelGateway.findById(100L)).thenReturn(Optional.of(testModel));
        when(providerGateway.findById(1L)).thenReturn(Optional.of(ollamaProvider));
        
        // When
        boolean supportsStreaming = dynamicModelManager.supportsStreaming(100L);
        
        // Then
        assertTrue(supportsStreaming, "Ollama 模型应该支持流式推理");
        verify(modelGateway).findById(100L);
        verify(providerGateway).findById(1L);
    }
}