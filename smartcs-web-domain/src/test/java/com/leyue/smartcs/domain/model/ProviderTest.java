package com.leyue.smartcs.domain.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.leyue.smartcs.domain.model.enums.ProviderType;

/**
 * Provider 领域模型测试类
 * 重点测试 Ollama 提供商的验证逻辑
 */
class ProviderTest {

    @Test
    void testIsValid_WithOllamaProvider_ShouldNotRequireApiKey() {
        // Given
        Provider ollamaProvider = new Provider();
        ollamaProvider.setProviderType(ProviderType.OLLAMA);
        ollamaProvider.setEndpoint("http://localhost:11434");
        ollamaProvider.setLabel("Local Ollama");
        // 注意：Ollama 不需要设置 API Key
        
        // When
        boolean isValid = ollamaProvider.isValid();
        
        // Then
        assertTrue(isValid, "Ollama 提供商不需要 API Key 也应该有效");
    }
    
    @Test
    void testIsValid_WithOllamaProvider_EmptyEndpoint_ShouldBeInvalid() {
        // Given
        Provider ollamaProvider = new Provider();
        ollamaProvider.setProviderType(ProviderType.OLLAMA);
        ollamaProvider.setEndpoint("");
        ollamaProvider.setLabel("Local Ollama");
        
        // When
        boolean isValid = ollamaProvider.isValid();
        
        // Then
        assertFalse(isValid, "Ollama 提供商没有端点应该无效");
    }
    
    @Test
    void testIsValid_WithOllamaProvider_NullEndpoint_ShouldBeInvalid() {
        // Given
        Provider ollamaProvider = new Provider();
        ollamaProvider.setProviderType(ProviderType.OLLAMA);
        ollamaProvider.setEndpoint(null);
        ollamaProvider.setLabel("Local Ollama");
        
        // When
        boolean isValid = ollamaProvider.isValid();
        
        // Then
        assertFalse(isValid, "Ollama 提供商端点为 null 应该无效");
    }
    
    @Test
    void testIsValid_WithOpenAiProvider_RequiresApiKey() {
        // Given
        Provider openaiProvider = new Provider();
        openaiProvider.setProviderType(ProviderType.OPENAI);
        openaiProvider.setEndpoint("https://api.openai.com/v1");
        openaiProvider.setLabel("OpenAI");
        openaiProvider.setHasApiKey(Boolean.TRUE);
        
        // When
        boolean isValid = openaiProvider.isValid();
        
        // Then
        assertTrue(isValid, "OpenAI 提供商设置了 API Key 应该有效");
    }
    
    @Test
    void testIsValid_WithOpenAiProvider_NoApiKey_ShouldBeInvalid() {
        // Given
        Provider openaiProvider = new Provider();
        openaiProvider.setProviderType(ProviderType.OPENAI);
        openaiProvider.setEndpoint("https://api.openai.com/v1");
        openaiProvider.setLabel("OpenAI");
        // 没有设置 API Key
        
        // When
        boolean isValid = openaiProvider.isValid();
        
        // Then
        assertFalse(isValid, "OpenAI 提供商没有 API Key 应该无效");
    }
    
    @Test
    void testIsValid_WithOpenAiProvider_ApiKeyString_ShouldBeValid() {
        // Given
        Provider openaiProvider = new Provider();
        openaiProvider.setProviderType(ProviderType.OPENAI);
        openaiProvider.setEndpoint("https://api.openai.com/v1");
        openaiProvider.setLabel("OpenAI");
        openaiProvider.setApiKey("sk-test-key");
        
        // When
        boolean isValid = openaiProvider.isValid();
        
        // Then
        assertTrue(isValid, "OpenAI 提供商直接设置 API Key 字符串应该有效");
    }
    
    @Test
    void testIsValid_WithNullProviderType_ShouldBeInvalid() {
        // Given
        Provider provider = new Provider();
        provider.setProviderType(null);
        provider.setEndpoint("http://localhost:11434");
        
        // When
        boolean isValid = provider.isValid();
        
        // Then
        assertFalse(isValid, "提供商类型为 null 应该无效");
    }
}