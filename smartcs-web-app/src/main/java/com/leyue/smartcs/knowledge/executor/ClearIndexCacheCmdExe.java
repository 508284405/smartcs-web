package com.leyue.smartcs.knowledge.executor;

import com.alibaba.cola.dto.Response;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.dto.knowledge.ClearIndexCacheCmd;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

/**
 * 清空索引缓存命令执行器
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ClearIndexCacheCmdExe {
    
    private final RedissonClient redissonClient;
    
    /**
     * 执行清空索引缓存命令
     * @param cmd 清空缓存命令
     * @return 操作结果
     */
    public Response execute(ClearIndexCacheCmd cmd) {
        log.info("执行清空索引缓存命令: {}", cmd);
        
        // 参数校验
        if (cmd.getIndexName() == null || cmd.getIndexName().trim().isEmpty()) {
            throw new BizException("索引名称不能为空");
        }
        
        try {
            String pattern = cmd.getIndexName() + ":*";
            
            // 使用pattern扫描匹配的键
            Iterable<String> keys = redissonClient.getKeys().getKeysByPattern(pattern);
            
            int deletedCount = 0;
            for (String key : keys) {
                if (redissonClient.getBucket(key).delete()) {
                    deletedCount++;
                }
            }
            
            log.info("成功清空索引缓存: 索引={}, 删除键数量={}", cmd.getIndexName(), deletedCount);
            return Response.buildSuccess();
        } catch (Exception e) {
            log.error("清空索引缓存失败: 索引={}", cmd.getIndexName(), e);
            throw new BizException("清空索引缓存失败: " + e.getMessage());
        }
    }
} 