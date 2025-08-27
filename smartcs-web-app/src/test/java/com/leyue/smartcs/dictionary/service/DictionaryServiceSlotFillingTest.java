package com.leyue.smartcs.dictionary.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leyue.smartcs.domain.dictionary.gateway.DictionaryGateway;
import com.leyue.smartcs.dto.intent.IntentDictionaryDTO;
import com.leyue.smartcs.dto.intent.IntentRuntimeConfigDTO;
import com.leyue.smartcs.dto.intent.SlotTemplateDTO;
import com.leyue.smartcs.intent.service.IntentRuntimeConfigCacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * DictionaryServiceImpl槽位填充功能单元测试
 * 
 * @author Claude
 */
@ExtendWith(MockitoExtension.class)
class DictionaryServiceSlotFillingTest {
    
    @Mock
    private DictionaryGateway dictionaryGateway;
    
    @Mock
    private IntentRuntimeConfigCacheService intentRuntimeConfigCacheService;
    
    private ObjectMapper objectMapper;
    private DictionaryServiceImpl dictionaryService;
    
    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        dictionaryService = new DictionaryServiceImpl(dictionaryGateway, objectMapper, intentRuntimeConfigCacheService);
    }
    
    @Test
    void testGetIntentSlotTemplates_WithValidRuntimeConfig_ShouldReturnTemplates() {
        // Given
        String tenant = "test_tenant";
        String channel = "test_channel";
        String domain = "test_domain";
        
        SlotTemplateDTO orderTemplate = SlotTemplateDTO.builder()
                .templateId("order_template")
                .intentCode("ORDER_QUERY")
                .slotFillingEnabled(true)
                .build();
        
        Map<String, SlotTemplateDTO> slotTemplates = new HashMap<>();
        slotTemplates.put("ORDER_QUERY", orderTemplate);
        
        IntentRuntimeConfigDTO runtimeConfig = new IntentRuntimeConfigDTO();
        runtimeConfig.setSlotTemplates(slotTemplates);
        
        when(intentRuntimeConfigCacheService.getConfig(channel, tenant, domain, "prod"))
                .thenReturn(runtimeConfig);
        
        // When
        Map<String, SlotTemplateDTO> result = dictionaryService.getIntentSlotTemplates(tenant, channel, domain);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.containsKey("ORDER_QUERY"));
        assertEquals("order_template", result.get("ORDER_QUERY").getTemplateId());
    }
    
    @Test
    void testGetIntentSlotTemplates_WithNullRuntimeConfig_ShouldReturnEmptyMap() {
        // Given
        String tenant = "test_tenant";
        String channel = "test_channel";
        String domain = "test_domain";
        
        when(intentRuntimeConfigCacheService.getConfig(channel, tenant, domain, "prod"))
                .thenReturn(null);
        
        // When
        Map<String, SlotTemplateDTO> result = dictionaryService.getIntentSlotTemplates(tenant, channel, domain);
        
        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
    
    @Test
    void testGetIntentSlotTemplates_WithExceptionThrown_ShouldReturnEmptyMap() {
        // Given
        String tenant = "test_tenant";
        String channel = "test_channel";
        String domain = "test_domain";
        
        when(intentRuntimeConfigCacheService.getConfig(channel, tenant, domain, "prod"))
                .thenThrow(new RuntimeException("Service unavailable"));
        
        // When
        Map<String, SlotTemplateDTO> result = dictionaryService.getIntentSlotTemplates(tenant, channel, domain);
        
        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
    
    @Test
    void testGetSlotTemplateByIntent_WithExistingIntent_ShouldReturnTemplate() {
        // Given
        String intentCode = "ORDER_QUERY";
        String tenant = "test_tenant";
        String channel = "test_channel";
        String domain = "test_domain";
        
        SlotTemplateDTO orderTemplate = SlotTemplateDTO.builder()
                .templateId("order_template")
                .intentCode(intentCode)
                .slotFillingEnabled(true)
                .build();
        
        Map<String, SlotTemplateDTO> slotTemplates = new HashMap<>();
        slotTemplates.put(intentCode, orderTemplate);
        
        IntentRuntimeConfigDTO runtimeConfig = new IntentRuntimeConfigDTO();
        runtimeConfig.setSlotTemplates(slotTemplates);
        
        when(intentRuntimeConfigCacheService.getConfig(channel, tenant, domain, "prod"))
                .thenReturn(runtimeConfig);
        
        // When
        SlotTemplateDTO result = dictionaryService.getSlotTemplateByIntent(intentCode, tenant, channel, domain);
        
        // Then
        assertNotNull(result);
        assertEquals("order_template", result.getTemplateId());
        assertEquals(intentCode, result.getIntentCode());
    }
    
    @Test
    void testGetSlotTemplateByIntent_WithNonExistingIntent_ShouldReturnNull() {
        // Given
        String intentCode = "NON_EXISTING_INTENT";
        String tenant = "test_tenant";
        String channel = "test_channel";
        String domain = "test_domain";
        
        Map<String, SlotTemplateDTO> slotTemplates = new HashMap<>();
        // 不包含所请求的intentCode
        
        IntentRuntimeConfigDTO runtimeConfig = new IntentRuntimeConfigDTO();
        runtimeConfig.setSlotTemplates(slotTemplates);
        
        when(intentRuntimeConfigCacheService.getConfig(channel, tenant, domain, "prod"))
                .thenReturn(runtimeConfig);
        
        // When
        SlotTemplateDTO result = dictionaryService.getSlotTemplateByIntent(intentCode, tenant, channel, domain);
        
        // Then
        assertNull(result);
    }
    
    @Test
    void testGetSlotTemplateByIntent_WithNullIntentCode_ShouldReturnNull() {
        // Given
        String intentCode = null;
        String tenant = "test_tenant";
        String channel = "test_channel";
        String domain = "test_domain";
        
        // When
        SlotTemplateDTO result = dictionaryService.getSlotTemplateByIntent(intentCode, tenant, channel, domain);
        
        // Then
        assertNull(result);
    }
    
    @Test
    void testGetSlotTemplateByIntent_WithEmptyIntentCode_ShouldReturnNull() {
        // Given
        String intentCode = "  ";
        String tenant = "test_tenant";
        String channel = "test_channel";
        String domain = "test_domain";
        
        // When
        SlotTemplateDTO result = dictionaryService.getSlotTemplateByIntent(intentCode, tenant, channel, domain);
        
        // Then
        assertNull(result);
    }
    
    @Test
    void testGetIntentDictionaries_WithValidRuntimeConfig_ShouldReturnDictionaries() {
        // Given
        String tenant = "test_tenant";
        String channel = "test_channel";
        String domain = "test_domain";
        
        IntentDictionaryDTO orderDictionary = IntentDictionaryDTO.builder()
                .intentCode("ORDER_QUERY")
                .language("zh_CN")
                .build();
        
        Map<String, IntentDictionaryDTO> intentDictionaries = new HashMap<>();
        intentDictionaries.put("ORDER_QUERY", orderDictionary);
        
        IntentRuntimeConfigDTO runtimeConfig = new IntentRuntimeConfigDTO();
        runtimeConfig.setIntentDictionaries(intentDictionaries);
        
        when(intentRuntimeConfigCacheService.getConfig(channel, tenant, domain, "prod"))
                .thenReturn(runtimeConfig);
        
        // When
        Map<String, IntentDictionaryDTO> result = dictionaryService.getIntentDictionaries(tenant, channel, domain);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.containsKey("ORDER_QUERY"));
        assertEquals("ORDER_QUERY", result.get("ORDER_QUERY").getIntentCode());
    }
    
    @Test
    void testGetIntentDictionary_WithExistingIntent_ShouldReturnDictionary() {
        // Given
        String intentCode = "ORDER_QUERY";
        String tenant = "test_tenant";
        String channel = "test_channel";
        String domain = "test_domain";
        
        IntentDictionaryDTO orderDictionary = IntentDictionaryDTO.builder()
                .intentCode(intentCode)
                .language("zh_CN")
                .build();
        
        Map<String, IntentDictionaryDTO> intentDictionaries = new HashMap<>();
        intentDictionaries.put(intentCode, orderDictionary);
        
        IntentRuntimeConfigDTO runtimeConfig = new IntentRuntimeConfigDTO();
        runtimeConfig.setIntentDictionaries(intentDictionaries);
        
        when(intentRuntimeConfigCacheService.getConfig(channel, tenant, domain, "prod"))
                .thenReturn(runtimeConfig);
        
        // When
        IntentDictionaryDTO result = dictionaryService.getIntentDictionary(intentCode, tenant, channel, domain);
        
        // Then
        assertNotNull(result);
        assertEquals(intentCode, result.getIntentCode());
        assertEquals("zh_CN", result.getLanguage());
    }
    
    @Test
    void testGetIntentDictionary_WithNonExistingIntent_ShouldReturnNull() {
        // Given
        String intentCode = "NON_EXISTING_INTENT";
        String tenant = "test_tenant";
        String channel = "test_channel";
        String domain = "test_domain";
        
        Map<String, IntentDictionaryDTO> intentDictionaries = new HashMap<>();
        // 不包含所请求的intentCode
        
        IntentRuntimeConfigDTO runtimeConfig = new IntentRuntimeConfigDTO();
        runtimeConfig.setIntentDictionaries(intentDictionaries);
        
        when(intentRuntimeConfigCacheService.getConfig(channel, tenant, domain, "prod"))
                .thenReturn(runtimeConfig);
        
        // When
        IntentDictionaryDTO result = dictionaryService.getIntentDictionary(intentCode, tenant, channel, domain);
        
        // Then
        assertNull(result);
    }
}