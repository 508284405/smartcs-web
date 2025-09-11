package com.leyue.smartcs.intent.executor.query;

import com.alibaba.cola.dto.SingleResponse;
import com.alibaba.cola.exception.BizException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leyue.smartcs.domain.intent.entity.Intent;
import com.leyue.smartcs.domain.intent.entity.IntentSnapshot;
import com.leyue.smartcs.domain.intent.entity.IntentSnapshotItem;
import com.leyue.smartcs.domain.intent.gateway.IntentGateway;
import com.leyue.smartcs.domain.intent.gateway.IntentPolicyGateway;
import com.leyue.smartcs.domain.intent.gateway.IntentRouteGateway;
import com.leyue.smartcs.domain.intent.gateway.IntentSnapshotGateway;
import com.leyue.smartcs.dto.intent.IntentRuntimeConfigDTO;
import com.leyue.smartcs.dto.intent.IntentRuntimeConfigQry;
import com.leyue.smartcs.intent.gateway.IntentSnapshotGatewayImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 意图运行时配置同步查询执行器
 * 负责生成和同步意图分类的运行时配置
 * 
 * @author Claude
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class IntentRuntimeConfigSyncQryExe {
    
    private final IntentSnapshotGateway intentSnapshotGateway;
    private final IntentSnapshotGatewayImpl intentSnapshotGatewayImpl;
    private final IntentGateway intentGateway;
    private final IntentPolicyGateway intentPolicyGateway;
    private final IntentRouteGateway intentRouteGateway;
    private final ObjectMapper objectMapper;
    
    /**
     * 获取运行时配置
     * 
     * @param qry 查询条件
     * @return 运行时配置
     */
    public SingleResponse<IntentRuntimeConfigDTO> execute(IntentRuntimeConfigQry qry) {
        try {
            log.info("获取意图运行时配置: channel={}, tenant={}, region={}, env={}", 
                    qry.getChannel(), qry.getTenant(), qry.getRegion(), qry.getEnv());
            
            // 获取适用的快照
            IntentSnapshot activeSnapshot = getApplicableSnapshot(qry.getChannel(), qry.getTenant());
            
            if (activeSnapshot == null) {
                log.warn("没有找到适用的激活快照: channel={}, tenant={}", qry.getChannel(), qry.getTenant());
                return SingleResponse.of(buildEmptyConfig(qry));
            }
            
            // 构建运行时配置
            IntentRuntimeConfigDTO config = buildRuntimeConfig(activeSnapshot, qry);
            
            log.info("运行时配置生成成功: snapshotId={}, intentCount={}, etag={}", 
                    activeSnapshot.getId(), 
                    config.getIntents() != null ? config.getIntents().size() : 0,
                    config.getEtag());
            
            return SingleResponse.of(config);
            
        } catch (Exception e) {
            log.error("获取意图运行时配置失败", e);
            throw new BizException("CONFIG_ERROR", "获取运行时配置失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取适用的快照
     */
    private IntentSnapshot getApplicableSnapshot(String channel, String tenant) {
        // 优先获取特定作用域的快照
        IntentSnapshot scopedSnapshot = intentSnapshotGatewayImpl.getActiveSnapshotByScope(channel, tenant);
        if (scopedSnapshot != null) {
            log.debug("找到作用域特定快照: snapshotId={}, channel={}, tenant={}", 
                    scopedSnapshot.getId(), channel, tenant);
            return scopedSnapshot;
        }
        
        // 回退到全局快照
        IntentSnapshot globalSnapshot = intentSnapshotGateway.getCurrentActiveSnapshot();
        if (globalSnapshot != null) {
            log.debug("使用全局快照: snapshotId={}", globalSnapshot.getId());
            return globalSnapshot;
        }
        
        return null;
    }
    
    /**
     * 构建运行时配置
     */
    private IntentRuntimeConfigDTO buildRuntimeConfig(IntentSnapshot snapshot, IntentRuntimeConfigQry qry) {
        try {
            IntentRuntimeConfigDTO config = new IntentRuntimeConfigDTO();
            
            // 基本信息
            config.setSnapshotId(snapshot.getCode());
            config.setEtag(snapshot.getEtag());
            config.setGeneratedAt(System.currentTimeMillis());
            config.setScope(snapshot.getScope());
            config.setScopeSelector(snapshot.getScopeSelector());
            
            // 请求上下文
            Map<String, Object> requestContext = new HashMap<>();
            requestContext.put("channel", qry.getChannel());
            requestContext.put("tenant", qry.getTenant());
            requestContext.put("region", qry.getRegion());
            requestContext.put("env", qry.getEnv());
            requestContext.put("requestTime", System.currentTimeMillis());
            config.setRequestContext(requestContext);
            
            // 构建意图配置
            Map<String, Object> intents = buildIntentConfigs(snapshot);
            config.setIntents(intents);
            
            // 扩展字段
            config.setChannel(qry.getChannel());
            config.setTenant(qry.getTenant());
            config.setRegion(qry.getRegion());
            config.setEnv(qry.getEnv());
            config.setConfigVersion("1.0.0");
            config.setLastUpdateTime(snapshot.getUpdatedAt());
            
            // 默认配置
            config.setDefaultThreshold(0.6);
            config.setMaxRetries(3);
            config.setTimeout(5000);
            
            // 构建意图阈值配置
            Map<String, Object> thresholds = buildIntentThresholds(snapshot, qry.getChannel(), qry.getTenant());
            config.setIntentThresholds(thresholds);
            
            return config;
            
        } catch (Exception e) {
            log.error("构建运行时配置失败: snapshotId={}", snapshot.getId(), e);
            throw new RuntimeException("构建运行时配置失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 构建意图配置映射
     */
    private Map<String, Object> buildIntentConfigs(IntentSnapshot snapshot) {
        Map<String, Object> intentConfigs = new HashMap<>();
        
        if (snapshot.getItems() == null || snapshot.getItems().isEmpty()) {
            return intentConfigs;
        }
        
        for (IntentSnapshotItem item : snapshot.getItems()) {
            Map<String, Object> intentConfig = new HashMap<>();
            
            // 基本信息
            intentConfig.put("intentId", item.getIntentId());
            intentConfig.put("intentCode", item.getIntentCode());
            intentConfig.put("intentName", item.getIntentName());
            intentConfig.put("version", item.getVersion());
            intentConfig.put("versionId", item.getVersionId());
            
            // 意图定义
            intentConfig.put("labels", item.getLabels() != null ? item.getLabels() : Collections.emptyList());
            intentConfig.put("boundaries", item.getBoundaries() != null ? item.getBoundaries() : Collections.emptyList());
            
            // 示例文本（用于LLM分类时的few-shot learning）
            List<String> examples = buildIntentExamples(item);
            intentConfig.put("examples", examples);
            
            intentConfigs.put(item.getIntentCode(), intentConfig);
        }
        
        return intentConfigs;
    }
    
    /**
     * 构建意图示例
     */
    private List<String> buildIntentExamples(IntentSnapshotItem item) {
        // TODO: 从样本数据中获取代表性示例
        // 这里返回基于标签生成的示例
        List<String> examples = new ArrayList<>();
        
        if (item.getLabels() != null && !item.getLabels().isEmpty()) {
            for (String label : item.getLabels()) {
                examples.add("示例: " + label);
                if (examples.size() >= 3) { // 限制示例数量
                    break;
                }
            }
        }
        
        return examples;
    }
    
    /**
     * 构建意图阈值配置
     */
    private Map<String, Object> buildIntentThresholds(IntentSnapshot snapshot, String channel, String tenant) {
        Map<String, Object> thresholds = new HashMap<>();
        
        if (snapshot.getItems() == null || snapshot.getItems().isEmpty()) {
            return thresholds;
        }
        
        // TODO: 从IntentPolicyGateway获取具体的阈值配置
        // 这里先使用默认配置
        for (IntentSnapshotItem item : snapshot.getItems()) {
            // 根据意图类型设置不同的默认阈值
            double threshold = getDefaultThresholdByIntent(item.getIntentCode());
            thresholds.put(item.getIntentCode(), threshold);
        }
        
        return thresholds;
    }
    
    /**
     * 根据意图获取默认阈值
     */
    private double getDefaultThresholdByIntent(String intentCode) {
        // 根据意图类型返回不同的默认阈值
        switch (intentCode.toLowerCase()) {
            case "greeting":
            case "goodbye":
                return 0.8; // 问候类意图要求更高的置信度
            case "question":
            case "inquiry":
                return 0.6; // 询问类意图使用标准阈值
            case "complaint":
            case "urgent":
                return 0.7; // 投诉类意图需要较高置信度
            default:
                return 0.6; // 默认阈值
        }
    }
    
    /**
     * 构建空配置
     */
    private IntentRuntimeConfigDTO buildEmptyConfig(IntentRuntimeConfigQry qry) {
        IntentRuntimeConfigDTO config = new IntentRuntimeConfigDTO();
        
        config.setSnapshotId("empty");
        config.setEtag("W/\"empty-" + System.currentTimeMillis() + "\"");
        config.setGeneratedAt(System.currentTimeMillis());
        config.setScope("global");
        config.setScopeSelector(Collections.emptyMap());
        config.setIntents(Collections.emptyMap());
        
        // 请求上下文
        Map<String, Object> requestContext = new HashMap<>();
        requestContext.put("channel", qry.getChannel());
        requestContext.put("tenant", qry.getTenant());
        requestContext.put("region", qry.getRegion());
        requestContext.put("env", qry.getEnv());
        requestContext.put("requestTime", System.currentTimeMillis());
        config.setRequestContext(requestContext);
        
        // 扩展字段
        config.setChannel(qry.getChannel());
        config.setTenant(qry.getTenant());
        config.setRegion(qry.getRegion());
        config.setEnv(qry.getEnv());
        config.setConfigVersion("1.0.0");
        config.setLastUpdateTime(System.currentTimeMillis());
        config.setDefaultThreshold(0.6);
        config.setMaxRetries(3);
        config.setTimeout(5000);
        config.setIntentThresholds(Collections.emptyMap());
        
        return config;
    }
}