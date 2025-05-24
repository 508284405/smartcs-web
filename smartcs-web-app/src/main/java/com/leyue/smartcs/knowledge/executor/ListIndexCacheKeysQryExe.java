package com.leyue.smartcs.knowledge.executor;

import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.dto.knowledge.ListIndexCacheKeysQry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 列出索引缓存键查询执行器
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ListIndexCacheKeysQryExe {
    
    private final RedissonClient redissonClient;
    
    /**
     * 执行列出索引缓存键查询
     * @param qry 查询条件
     * @return 缓存键列表
     */
    public List<String> execute(ListIndexCacheKeysQry qry) {
        log.info("执行列出索引缓存键查询: {}", qry);
        
        // 参数校验
        if (qry.getIndexName() == null || qry.getIndexName().trim().isEmpty()) {
            throw new BizException("索引名称不能为空");
        }
        
        try {
            String pattern = qry.getIndexName() + ":*";
            
            // 使用pattern扫描匹配的键
            Iterable<String> keys = redissonClient.getKeys().getKeysByPattern(pattern);
            
            List<String> result = new ArrayList<>();
            for (String key : keys) {
                result.add(key);
            }
            
            log.info("成功列出索引缓存键: 索引={}, 键数量={}", qry.getIndexName(), result.size());
            return result;
        } catch (Exception e) {
            log.error("列出索引缓存键失败: 索引={}", qry.getIndexName(), e);
            throw new BizException("列出索引缓存键失败: " + e.getMessage());
        }
    }
} 