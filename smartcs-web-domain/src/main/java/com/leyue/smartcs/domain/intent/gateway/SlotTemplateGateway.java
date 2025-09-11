package com.leyue.smartcs.domain.intent.gateway;

import com.leyue.smartcs.dto.intent.IntentDictionaryDTO;
import com.leyue.smartcs.dto.intent.SlotTemplateDTO;

import java.util.Map;

/**
 * 槽位模板网关接口
 * 定义槽位模板和意图字典的数据访问契约
 * 
 * 职责：
 * - 提供槽位模板数据访问抽象
 * - 提供意图字典数据访问抽象
 * - 隔离具体的数据源实现细节
 * 
 * 设计原则：
 * - 遵循依赖倒置原则，App层依赖此抽象而非具体实现
 * - 统一槽位模板相关数据的访问入口
 * - 支持多种数据源的灵活切换
 * 
 * @author Claude
 */
public interface SlotTemplateGateway {
    
    /**
     * 获取意图槽位模板映射
     * 
     * @param tenant 租户标识
     * @param channel 渠道标识
     * @param domain 领域标识
     * @return 意图编码到槽位模板的映射
     */
    Map<String, SlotTemplateDTO> getIntentSlotTemplates(String tenant, String channel, String domain);
    
    /**
     * 获取指定意图的槽位模板
     * 
     * @param intentCode 意图编码
     * @param tenant 租户标识
     * @param channel 渠道标识
     * @param domain 领域标识
     * @return 槽位模板，如果不存在则返回null
     */
    SlotTemplateDTO getSlotTemplateByIntent(String intentCode, String tenant, String channel, String domain);
    
    /**
     * 获取意图字典映射
     * 
     * @param tenant 租户标识
     * @param channel 渠道标识
     * @param domain 领域标识
     * @return 意图编码到意图字典的映射
     */
    Map<String, IntentDictionaryDTO> getIntentDictionaries(String tenant, String channel, String domain);
    
    /**
     * 获取指定意图的字典信息
     * 
     * @param intentCode 意图编码
     * @param tenant 租户标识
     * @param channel 渠道标识
     * @param domain 领域标识
     * @return 意图字典，如果不存在则返回null
     */
    IntentDictionaryDTO getIntentDictionary(String intentCode, String tenant, String channel, String domain);
}