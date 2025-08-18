package com.leyue.smartcs.web.intent;

import com.alibaba.cola.dto.Response;
import com.alibaba.cola.dto.SingleResponse;
import com.leyue.smartcs.dto.intent.IntentRuntimeConfigDTO;
import com.leyue.smartcs.dto.intent.IntentRuntimeConfigQry;
import com.leyue.smartcs.intent.service.IntentConfigSyncService;
import com.leyue.smartcs.intent.service.IntentRuntimeConfigCacheService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 意图运行时API控制器
 * 提供配置获取、同步和缓存管理等运行时服务
 * 
 * @author Claude
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/intent/runtime")
@RequiredArgsConstructor
@Validated
public class IntentRuntimeController {
    
    private final IntentConfigSyncService configSyncService;
    private final IntentRuntimeConfigCacheService cacheService;
    
    /**
     * 获取运行时配置
     */
    @GetMapping("/config")
    public ResponseEntity<IntentRuntimeConfigDTO> getRuntimeConfig(IntentRuntimeConfigQry qry,
                                                                  HttpServletRequest request) {
        log.info("获取意图运行时配置: channel={}, tenant={}, region={}, env={}", 
                qry.getChannel(), qry.getTenant(), qry.getRegion(), qry.getEnv());
        
        try {
            // 检查If-None-Match头（ETag支持）
            String ifNoneMatch = request.getHeader("If-None-Match");
            if (StringUtils.hasText(ifNoneMatch)) {
                boolean etagMatches = cacheService.checkETag(qry.getChannel(), qry.getTenant(), 
                                                           qry.getRegion(), qry.getEnv(), ifNoneMatch);
                if (etagMatches) {
                    log.debug("ETag匹配，返回304: etag={}", ifNoneMatch);
                    return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
                }
            }
            
            // 尝试从缓存获取
            IntentRuntimeConfigDTO cachedConfig = cacheService.getConfig(qry.getChannel(), qry.getTenant(), 
                                                                        qry.getRegion(), qry.getEnv());
            if (cachedConfig != null) {
                log.info("从缓存返回配置: etag={}, intentCount={}", 
                        cachedConfig.getEtag(), 
                        cachedConfig.getIntents() != null ? cachedConfig.getIntents().size() : 0);
                
                return ResponseEntity.ok()
                        .eTag(cachedConfig.getEtag())
                        .header("Cache-Control", "max-age=300") // 5分钟缓存
                        .body(cachedConfig);
            }
            
            // 缓存未命中，触发同步
            log.info("缓存未命中，触发配置同步");
            Response syncResponse = configSyncService.syncConfig(qry.getChannel(), qry.getTenant(), 
                                                               qry.getRegion(), qry.getEnv());
            
            if (!syncResponse.isSuccess()) {
                log.error("配置同步失败: {}", syncResponse.getErrMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
            
            // 再次从缓存获取
            IntentRuntimeConfigDTO syncedConfig = cacheService.getConfig(qry.getChannel(), qry.getTenant(), 
                                                                        qry.getRegion(), qry.getEnv());
            if (syncedConfig != null) {
                return ResponseEntity.ok()
                        .eTag(syncedConfig.getEtag())
                        .header("Cache-Control", "max-age=300")
                        .body(syncedConfig);
            }
            
            // 如果仍然没有配置，返回404
            return ResponseEntity.notFound().build();
            
        } catch (Exception e) {
            log.error("获取运行时配置失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * 手动同步配置
     */
    @PostMapping("/sync")
    public SingleResponse<String> syncConfig(@RequestParam(required = false) String channel,
                                           @RequestParam(required = false) String tenant,
                                           @RequestParam(required = false) String region,
                                           @RequestParam(required = false) String env) {
        log.info("手动同步意图配置: channel={}, tenant={}, region={}, env={}", 
                channel, tenant, region, env);
        
        try {
            Response syncResponse = configSyncService.syncConfig(channel, tenant, region, env);
            
            if (syncResponse.isSuccess()) {
                return SingleResponse.of("配置同步成功");
            } else {
                return SingleResponse.buildFailure(syncResponse.getErrCode(), syncResponse.getErrMessage());
            }
            
        } catch (Exception e) {
            log.error("手动同步配置失败", e);
            return SingleResponse.buildFailure("SYNC_ERROR", "同步失败: " + e.getMessage());
        }
    }
    
    /**
     * 全量同步所有配置
     */
    @PostMapping("/sync/all")
    public SingleResponse<String> syncAllConfigs() {
        log.info("手动全量同步所有配置");
        
        try {
            // 异步执行全量同步
            configSyncService.syncAllConfigs();
            
            return SingleResponse.of("全量同步已开始，请稍后查看同步状态");
            
        } catch (Exception e) {
            log.error("全量同步配置失败", e);
            return SingleResponse.buildFailure("SYNC_ALL_ERROR", "全量同步失败: " + e.getMessage());
        }
    }
    
    /**
     * 清除配置缓存
     */
    @DeleteMapping("/cache")
    public SingleResponse<String> evictCache(@RequestParam(required = false) String channel,
                                           @RequestParam(required = false) String tenant,
                                           @RequestParam(required = false) String region,
                                           @RequestParam(required = false) String env) {
        log.info("清除意图配置缓存: channel={}, tenant={}, region={}, env={}", 
                channel, tenant, region, env);
        
        try {
            if (channel != null || tenant != null || region != null || env != null) {
                // 清除指定配置的缓存
                cacheService.evictConfig(channel, tenant, region, env);
                return SingleResponse.of("指定配置缓存清除成功");
            } else {
                // 清除所有缓存
                cacheService.evictAllConfigs();
                return SingleResponse.of("所有配置缓存清除成功");
            }
            
        } catch (Exception e) {
            log.error("清除配置缓存失败", e);
            return SingleResponse.buildFailure("CACHE_EVICT_ERROR", "缓存清除失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取同步状态
     */
    @GetMapping("/status")
    public SingleResponse<Map<String, Object>> getSyncStatus() {
        log.debug("获取配置同步状态");
        
        try {
            IntentConfigSyncService.SyncStatus syncStatus = configSyncService.getSyncStatus();
            
            Map<String, Object> status = new HashMap<>();
            status.put("cacheStats", syncStatus.getCacheStats());
            status.put("listenerCount", syncStatus.getListenerCount());
            status.put("lastSyncTime", syncStatus.getLastSyncTime());
            status.put("currentTime", System.currentTimeMillis());
            
            return SingleResponse.of(status);
            
        } catch (Exception e) {
            log.error("获取同步状态失败", e);
            return SingleResponse.buildFailure("STATUS_ERROR", "获取状态失败: " + e.getMessage());
        }
    }
    
    /**
     * 健康检查
     */
    @GetMapping("/health")
    public SingleResponse<Map<String, Object>> healthCheck() {
        log.debug("运行时配置服务健康检查");
        
        try {
            Map<String, Object> health = new HashMap<>();
            health.put("status", "UP");
            health.put("timestamp", System.currentTimeMillis());
            
            // 检查缓存服务状态
            IntentRuntimeConfigCacheService.CacheStats cacheStats = cacheService.getCacheStats();
            health.put("cacheHealthy", cacheStats.getRedisConfigCount() >= 0);
            
            // 检查同步服务状态
            IntentConfigSyncService.SyncStatus syncStatus = configSyncService.getSyncStatus();
            health.put("syncServiceHealthy", syncStatus != null);
            
            return SingleResponse.of(health);
            
        } catch (Exception e) {
            log.error("健康检查失败", e);
            
            Map<String, Object> health = new HashMap<>();
            health.put("status", "DOWN");
            health.put("error", e.getMessage());
            health.put("timestamp", System.currentTimeMillis());
            
            return SingleResponse.of(health);
        }
    }
    
    /**
     * 获取配置版本信息
     */
    @GetMapping("/version")
    public SingleResponse<Map<String, Object>> getVersionInfo() {
        Map<String, Object> version = new HashMap<>();
        version.put("service", "Intent Runtime Service");
        version.put("version", "1.0.0");
        version.put("buildTime", "2024-01-01T00:00:00Z"); // TODO: 从构建信息获取
        version.put("apiVersion", "v1");
        
        return SingleResponse.of(version);
    }
}