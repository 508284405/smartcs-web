package com.leyue.smartcs.knowledge.executor.query;

import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.dto.knowledge.ListIndexCacheKeysQry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.redisson.api.options.KeysScanOptions;
import org.redisson.api.search.index.IndexInfo;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 列出索引缓存键查询执行器
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class IndexListCacheKeysQryExe {

    private final RedissonClient redissonClient;

    /**
     * 执行列出索引缓存键查询
     *
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
            IndexInfo indexInfo = redissonClient.getSearch().info(qry.getIndexName());
            if (indexInfo == null) {
                log.warn("索引不存在: {}", qry.getIndexName());
                return new ArrayList<>();
            }

            String[] prefixes = (String[]) indexInfo.getDefinition().get("prefixes");
            List<String> result = new ArrayList<>();

            if (prefixes != null) {
                for (String prefix : prefixes) {
                    // 使用SCAN命令安全遍历匹配前缀的键，避免阻塞Redis
                    String pattern = prefix + "*";
                    KeysScanOptions scanOptions = KeysScanOptions.defaults()
                            .pattern(pattern)
                            .limit(Integer.MAX_VALUE);
                    Iterable<String> iterable = redissonClient.getKeys().getKeys(scanOptions);
                    for (String s : iterable) {
                        result.add(s);
                    }
                }
            }

            log.info("成功列出索引缓存键: 索引={}, 键数量={}", qry.getIndexName(), result.size());
            return result;
        } catch (Exception e) {
            log.error("列出索引缓存键失败: 索引={}", qry.getIndexName(), e);
            throw new BizException("列出索引缓存键失败: " + e.getMessage());
        }
    }
} 