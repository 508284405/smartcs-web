package com.leyue.smartcs.knowledge.executor.query;

import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.dto.knowledge.CacheValueDTO;
import com.leyue.smartcs.dto.knowledge.GetCacheValueQry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.redisson.codec.CompositeCodec;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 获取缓存值查询执行器
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class GetCacheValueQryExe {
    
    private final RedissonClient redissonClient;
    
    /**
     * 执行获取缓存值查询
     * @param qry 查询条件
     * @return 缓存值详情
     */
    public CacheValueDTO execute(GetCacheValueQry qry) {
        log.info("执行获取缓存值查询: {}", qry);
        
        // 参数校验
        if (qry.getCacheKey() == null || qry.getCacheKey().trim().isEmpty()) {
            throw new BizException("缓存键名不能为空");
        }
        
        try {
            String cacheKey = qry.getCacheKey();
            
            // 检查键是否存在
            if (redissonClient.getKeys().countExists(cacheKey) == 0) {
                throw new BizException("缓存键不存在: " + cacheKey);
            }
            
            // 获取RMap对象（假设缓存值是Hash结构）
            RMap<String, Object> rMap = redissonClient.getMap(cacheKey,new CompositeCodec(StringCodec.INSTANCE, redissonClient.getConfig().getCodec()));
            Map<String, Object> value = rMap.readAllMap();
            
            // 获取TTL
            long ttl = rMap.remainTimeToLive();
            
            CacheValueDTO result = new CacheValueDTO();
            result.setCacheKey(cacheKey);
            result.setValue(value);
            result.setTtl(ttl > 0 ? ttl / 1000 : -1); // 转换为秒
            
            log.info("成功获取缓存值: 键={}, 值大小={}", cacheKey, value.size());
            return result;
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取缓存值失败: 键={}", qry.getCacheKey(), e);
            throw new BizException("获取缓存值失败: " + e.getMessage());
        }
    }
} 