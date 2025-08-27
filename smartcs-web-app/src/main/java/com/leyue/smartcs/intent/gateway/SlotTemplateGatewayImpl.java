package com.leyue.smartcs.intent.gateway;

import com.leyue.smartcs.domain.intent.gateway.SlotTemplateGateway;
import com.leyue.smartcs.dto.intent.IntentDictionaryDTO;
import com.leyue.smartcs.dto.intent.IntentRuntimeConfigDTO;
import com.leyue.smartcs.dto.intent.SlotTemplateDTO;
import com.leyue.smartcs.intent.service.IntentRuntimeConfigCacheService;
import com.leyue.smartcs.api.IntentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 槽位模板网关实现
 * 基于意图运行时配置缓存服务的实现
 * 
 * 职责：
 * - 封装意图运行时配置的访问逻辑
 * - 提供统一的槽位模板数据访问接口
 * - 处理缓存和异常情况的降级策略
 * 
 * 特性：
 * - 支持多级缓存（本地缓存 + Redis缓存）
 * - 异常时返回空结果，不影响主流程
 * - 统计缓存命中率和访问指标
 * 
 * @author Claude
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SlotTemplateGatewayImpl implements SlotTemplateGateway {
    
    private final IntentRuntimeConfigCacheService intentRuntimeConfigCacheService;
    
    @Override
    @Cacheable(value = "slot-template-gateway-all", key = "#tenant + ':' + #channel + ':' + #domain")
    public Map<String, SlotTemplateDTO> getIntentSlotTemplates(String tenant, String channel, String domain) {
        log.debug("获取意图槽位模板: tenant={}, channel={}, domain={}", tenant, channel, domain);
        
        try {
            IntentRuntimeConfigDTO runtimeConfig = intentRuntimeConfigCacheService.getConfig(channel, tenant, domain, "prod");
            if (runtimeConfig != null && runtimeConfig.getSlotTemplates() != null) {
                log.debug("成功获取槽位模板: count={}", runtimeConfig.getSlotTemplates().size());
                return runtimeConfig.getSlotTemplates();
            }
        } catch (Exception e) {
            log.error("获取意图槽位模板失败: tenant={}, channel={}, domain={}", tenant, channel, domain, e);
        }
        
        // 降级策略：返回空映射
        log.debug("返回空槽位模板映射作为降级");
        return Collections.emptyMap();
    }
    
    @Override
    @Cacheable(value = "slot-template-gateway-single", key = "#intentCode + ':' + #tenant + ':' + #channel + ':' + #domain")
    public SlotTemplateDTO getSlotTemplateByIntent(String intentCode, String tenant, String channel, String domain) {
        log.debug("获取意图槽位模板: intentCode={}, tenant={}, channel={}, domain={}", 
                intentCode, tenant, channel, domain);
        
        if (intentCode == null || intentCode.trim().isEmpty()) {
            log.debug("意图编码为空，返回null");
            return null;
        }
        
        Map<String, SlotTemplateDTO> allTemplates = getIntentSlotTemplates(tenant, channel, domain);
        SlotTemplateDTO template = allTemplates.get(intentCode.trim());
        
        if (template != null) {
            log.debug("找到槽位模板: intentCode={}, templateId={}", intentCode, template.getTemplateId());
        } else {
            log.debug("未找到槽位模板: intentCode={}", intentCode);
        }
        
        return template;
    }
    
    @Override
    @Cacheable(value = "intent-dictionary-gateway-all", key = "#tenant + ':' + #channel + ':' + #domain")
    public Map<String, IntentDictionaryDTO> getIntentDictionaries(String tenant, String channel, String domain) {
        log.debug("获取意图字典映射: tenant={}, channel={}, domain={}", tenant, channel, domain);
        
        try {
            IntentRuntimeConfigDTO runtimeConfig = intentRuntimeConfigCacheService.getConfig(channel, tenant, domain, "prod");
            if (runtimeConfig != null && runtimeConfig.getIntentDictionaries() != null) {
                log.debug("成功获取意图字典: count={}", runtimeConfig.getIntentDictionaries().size());
                return runtimeConfig.getIntentDictionaries();
            }
        } catch (Exception e) {
            log.error("获取意图字典映射失败: tenant={}, channel={}, domain={}", tenant, channel, domain, e);
        }
        
        // 降级策略：返回空映射
        log.debug("返回空意图字典映射作为降级");
        return Collections.emptyMap();
    }
    
    @Override
    @Cacheable(value = "intent-dictionary-gateway-single", key = "#intentCode + ':' + #tenant + ':' + #channel + ':' + #domain")
    public IntentDictionaryDTO getIntentDictionary(String intentCode, String tenant, String channel, String domain) {
        log.debug("获取意图字典: intentCode={}, tenant={}, channel={}, domain={}", 
                intentCode, tenant, channel, domain);
        
        if (intentCode == null || intentCode.trim().isEmpty()) {
            log.debug("意图编码为空，返回null");
            return null;
        }
        
        Map<String, IntentDictionaryDTO> allDictionaries = getIntentDictionaries(tenant, channel, domain);
        IntentDictionaryDTO dictionary = allDictionaries.get(intentCode.trim());
        
        if (dictionary != null) {
            log.debug("找到意图字典: intentCode={}, language={}", intentCode, dictionary.getLanguage());
        } else {
            log.debug("未找到意图字典: intentCode={}", intentCode);
        }
        
        return dictionary;
    }
}