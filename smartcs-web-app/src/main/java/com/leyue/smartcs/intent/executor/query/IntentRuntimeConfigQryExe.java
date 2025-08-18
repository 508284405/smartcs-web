package com.leyue.smartcs.intent.executor.query;

import com.alibaba.cola.dto.SingleResponse;
import com.leyue.smartcs.domain.intent.entity.IntentSnapshot;
import com.leyue.smartcs.domain.intent.gateway.IntentSnapshotGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 意图运行时配置查询执行器
 * 
 * @author Claude
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class IntentRuntimeConfigQryExe {
    
    private final IntentSnapshotGateway snapshotGateway;
    
    /**
     * 获取运行时配置
     */
    public SingleResponse<Map<String, Object>> execute(String tenant, String channel, String region, String env) {
        
        // 获取当前激活的快照
        IntentSnapshot activeSnapshot = snapshotGateway.getCurrentActiveSnapshot();
        
        Map<String, Object> config = new HashMap<>();
        
        if (activeSnapshot != null) {
            config.put("snapshot_id", activeSnapshot.getCode());
            config.put("etag", activeSnapshot.getEtag());
            config.put("generated_at", System.currentTimeMillis());
            config.put("scope", activeSnapshot.getScope());
            config.put("scope_selector", activeSnapshot.getScopeSelector());
            
            // TODO: 根据快照构建具体的配置内容
            // 包括意图列表、策略配置、路由配置等
            config.put("intents", buildIntentConfig(activeSnapshot));
            
            log.info("获取运行时配置成功，快照ID: {}", activeSnapshot.getCode());
        } else {
            log.warn("未找到激活的配置快照");
            config.put("snapshot_id", null);
            config.put("intents", new HashMap<>());
        }
        
        config.put("request_context", Map.of(
                "tenant", tenant,
                "channel", channel,
                "region", region,
                "env", env
        ));
        
        return SingleResponse.of(config);
    }
    
    private Map<String, Object> buildIntentConfig(IntentSnapshot snapshot) {
        // TODO: 根据快照项构建意图配置
        // 这里返回模拟数据
        return Map.of(
                "total_count", 0,
                "active_count", 0,
                "intent_list", new HashMap<>()
        );
    }
}