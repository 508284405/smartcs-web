package com.leyue.smartcs.intent.service;

import com.alibaba.cola.dto.Response;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.dto.intent.IntentRuntimeConfigDTO;
import com.leyue.smartcs.dto.intent.IntentRuntimeConfigQry;
import com.leyue.smartcs.intent.executor.query.IntentRuntimeConfigSyncQryExe;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 意图配置同步服务
 * 负责配置的同步、推送和通知
 * 
 * @author Claude
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IntentConfigSyncService {
    
    private final IntentRuntimeConfigSyncQryExe runtimeConfigSyncQryExe;
    private final IntentRuntimeConfigCacheService cacheService;
    
    // 配置变更监听器列表
    private final List<ConfigChangeListener> listeners = new CopyOnWriteArrayList<>();
    
    /**
     * 同步配置到缓存
     * 
     * @param channel 渠道
     * @param tenant 租户
     * @param region 区域
     * @param env 环境
     * @return 同步结果
     */
    public Response syncConfig(String channel, String tenant, String region, String env) {
        try {
            log.info("开始同步意图配置: channel={}, tenant={}, region={}, env={}", 
                    channel, tenant, region, env);
            
            // 构建查询条件
            IntentRuntimeConfigQry qry = new IntentRuntimeConfigQry();
            qry.setChannel(channel);
            qry.setTenant(tenant);
            qry.setRegion(region);
            qry.setEnv(env);
            
            // 获取最新配置
            IntentRuntimeConfigDTO latestConfig = runtimeConfigSyncQryExe.execute(qry).getData();
            
            if (latestConfig == null) {
                throw new BizException("CONFIG_NOT_FOUND", "未找到可用的配置");
            }
            
            // 检查是否需要更新
            IntentRuntimeConfigDTO cachedConfig = cacheService.getConfig(channel, tenant, region, env);
            if (cachedConfig != null && latestConfig.getEtag().equals(cachedConfig.getEtag())) {
                log.debug("配置未发生变化，跳过同步: etag={}", latestConfig.getEtag());
                return Response.buildSuccess();
            }
            
            // 更新缓存
            cacheService.cacheConfig(latestConfig);
            
            // 异步通知配置变更
            notifyConfigChange(latestConfig, cachedConfig);
            
            log.info("意图配置同步成功: channel={}, tenant={}, etag={}", 
                    channel, tenant, latestConfig.getEtag());
            
            return Response.buildSuccess();
            
        } catch (BizException e) {
            log.warn("同步意图配置业务异常: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("同步意图配置失败", e);
            throw new BizException("SYNC_ERROR", "配置同步失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量同步多个作用域的配置
     * 
     * @param scopes 作用域列表
     * @return 同步结果
     */
    @Async
    public CompletableFuture<Response> batchSyncConfigs(List<ConfigScope> scopes) {
        try {
            log.info("开始批量同步配置: scopeCount={}", scopes.size());
            
            int successCount = 0;
            int failureCount = 0;
            
            for (ConfigScope scope : scopes) {
                try {
                    syncConfig(scope.getChannel(), scope.getTenant(), scope.getRegion(), scope.getEnv());
                    successCount++;
                } catch (Exception e) {
                    log.error("同步配置失败: scope={}", scope, e);
                    failureCount++;
                }
            }
            
            log.info("批量同步配置完成: total={}, success={}, failure={}", 
                    scopes.size(), successCount, failureCount);
            
            return CompletableFuture.completedFuture(Response.buildSuccess());
            
        } catch (Exception e) {
            log.error("批量同步配置失败", e);
            return CompletableFuture.completedFuture(
                    Response.buildFailure("BATCH_SYNC_ERROR", "批量同步失败: " + e.getMessage()));
        }
    }
    
    /**
     * 全量同步所有配置
     * 通常在快照发布后调用
     * 
     * @return 同步结果
     */
    @Async
    public CompletableFuture<Response> syncAllConfigs() {
        try {
            log.info("开始全量同步所有配置");
            
            // 清除所有缓存，强制重新加载
            cacheService.evictAllConfigs();
            
            // TODO: 获取所有需要同步的作用域
            // 这里使用一些常见的作用域进行演示
            List<ConfigScope> commonScopes = List.of(
                    new ConfigScope("web", "default", "cn", "prod"),
                    new ConfigScope("api", "default", "cn", "prod"),
                    new ConfigScope("mobile", "default", "cn", "prod")
            );
            
            return batchSyncConfigs(commonScopes);
            
        } catch (Exception e) {
            log.error("全量同步配置失败", e);
            return CompletableFuture.completedFuture(
                    Response.buildFailure("SYNC_ALL_ERROR", "全量同步失败: " + e.getMessage()));
        }
    }
    
    /**
     * 注册配置变更监听器
     * 
     * @param listener 监听器
     */
    public void addConfigChangeListener(ConfigChangeListener listener) {
        if (listener != null) {
            listeners.add(listener);
            log.info("注册配置变更监听器: {}", listener.getClass().getSimpleName());
        }
    }
    
    /**
     * 移除配置变更监听器
     * 
     * @param listener 监听器
     */
    public void removeConfigChangeListener(ConfigChangeListener listener) {
        if (listener != null) {
            listeners.remove(listener);
            log.info("移除配置变更监听器: {}", listener.getClass().getSimpleName());
        }
    }
    
    /**
     * 通知配置变更
     */
    @Async
    protected void notifyConfigChange(IntentRuntimeConfigDTO newConfig, IntentRuntimeConfigDTO oldConfig) {
        if (listeners.isEmpty()) {
            return;
        }
        
        try {
            ConfigChangeEvent event = ConfigChangeEvent.builder()
                    .newConfig(newConfig)
                    .oldConfig(oldConfig)
                    .changeTime(System.currentTimeMillis())
                    .build();
            
            for (ConfigChangeListener listener : listeners) {
                try {
                    listener.onConfigChange(event);
                } catch (Exception e) {
                    log.error("配置变更监听器执行失败: listener={}", listener.getClass().getSimpleName(), e);
                }
            }
            
            log.info("配置变更通知完成: listenerCount={}, etag={}", 
                    listeners.size(), newConfig.getEtag());
                    
        } catch (Exception e) {
            log.error("配置变更通知失败", e);
        }
    }
    
    /**
     * 获取同步状态
     */
    public SyncStatus getSyncStatus() {
        IntentRuntimeConfigCacheService.CacheStats cacheStats = cacheService.getCacheStats();
        
        return SyncStatus.builder()
                .cacheStats(cacheStats)
                .listenerCount(listeners.size())
                .lastSyncTime(System.currentTimeMillis()) // TODO: 记录实际的最后同步时间
                .build();
    }
    
    /**
     * 配置作用域
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class ConfigScope {
        private String channel;
        private String tenant;
        private String region;
        private String env;
    }
    
    /**
     * 配置变更事件
     */
    @lombok.Builder
    @lombok.Data
    public static class ConfigChangeEvent {
        private IntentRuntimeConfigDTO newConfig;
        private IntentRuntimeConfigDTO oldConfig;
        private Long changeTime;
    }
    
    /**
     * 配置变更监听器接口
     */
    @FunctionalInterface
    public interface ConfigChangeListener {
        void onConfigChange(ConfigChangeEvent event);
    }
    
    /**
     * 同步状态
     */
    @lombok.Builder
    @lombok.Data
    public static class SyncStatus {
        private IntentRuntimeConfigCacheService.CacheStats cacheStats;
        private int listenerCount;
        private Long lastSyncTime;
    }
}