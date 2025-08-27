package com.leyue.smartcs.dictionary.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.leyue.smartcs.api.DictionaryAdminService;
import com.leyue.smartcs.api.DictionaryService;
import com.leyue.smartcs.dictionary.gateway.DictionaryGatewayImpl;
import com.leyue.smartcs.dictionary.mapper.DictionaryEntryMapper;
import com.leyue.smartcs.dictionary.provider.FallbackDictionaryProvider;
import com.leyue.smartcs.dictionary.service.DictionaryAdminServiceImpl;
import com.leyue.smartcs.dictionary.service.DictionaryServiceImpl;
import com.leyue.smartcs.domain.dictionary.gateway.DictionaryGateway;
import com.leyue.smartcs.domain.intent.gateway.SlotTemplateGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * 字典模块配置类
 * 负责字典模块相关Bean的装配和配置
 * 
 * @author Claude
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class DictionaryConfiguration {
    
    private final DictionaryEntryMapper dictionaryEntryMapper;
    
    /**
     * 配置字典领域网关
     */
    @Bean
    @ConditionalOnMissingBean
    public DictionaryGateway dictionaryGateway() {
        return new DictionaryGatewayImpl(dictionaryEntryMapper);
    }
    
    /**
     * 配置回退字典数据提供者
     */
    @Bean
    @ConditionalOnMissingBean
    public FallbackDictionaryProvider fallbackDictionaryProvider() {
        return new FallbackDictionaryProvider();
    }
    
    /**
     * 配置字典服务实现
     * 支持缓存和回退机制
     */
    @Bean
    @Primary
    public DictionaryService dictionaryService(DictionaryGateway dictionaryGateway,
                                             ObjectMapper objectMapper,
                                             SlotTemplateGateway slotTemplateGateway,
                                             FallbackDictionaryProvider fallbackProvider) {
        DictionaryServiceImpl service = new DictionaryServiceImpl(dictionaryGateway, objectMapper, slotTemplateGateway);
        
        // 如果需要，可以在这里配置回退逻辑
        log.info("字典服务配置完成，支持缓存和回退机制");
        
        return service;
    }
    
    /**
     * 配置字典管理服务实现
     */
    @Bean
    @ConditionalOnMissingBean
    public DictionaryAdminService dictionaryAdminService(DictionaryGateway dictionaryGateway) {
        return new DictionaryAdminServiceImpl(dictionaryGateway);
    }
}