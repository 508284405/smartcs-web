package com.leyue.smartcs.web.knowledge;

import com.alibaba.cola.dto.MultiResponse;
import com.alibaba.cola.dto.Response;
import com.alibaba.cola.dto.SingleResponse;
import com.leyue.smartcs.api.IndexCacheService;
import com.leyue.smartcs.dto.knowledge.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 索引管理REST接口
 */
@RestController
@RequestMapping("/api/admin/knowledge/index")
@RequiredArgsConstructor
@Slf4j
public class AdminIndexController {

    private final IndexCacheService indexCacheService;

    // ========== 索引管理相关接口 ==========

    /**
     * 创建索引
     */
    @PostMapping
    public Response createIndex(@RequestBody @Valid CreateIndexCmd cmd) {
        return indexCacheService.createIndex(cmd);
    }

    /**
     * 查询索引信息
     */
    @GetMapping("/info")
    public SingleResponse<IndexInfoDTO> getIndexInfo(GetIndexInfoQry qry) {
        return indexCacheService.getIndexInfo(qry);
    }

    /**
     * 删除索引
     */
    @DeleteMapping
    public Response deleteIndex(DeleteIndexCmd cmd) {
        return indexCacheService.deleteIndex(cmd);
    }

    /**
     * 获取所有索引列表
     */
    @GetMapping
    public MultiResponse<String> listIndexes() {
        log.info("获取索引列表请求");
        return indexCacheService.listIndexes();
    }

    // ========== 索引缓存操作相关接口 ==========

    /**
     * 清空指定索引下的缓存
     */
    @DeleteMapping("/cache")
    public Response clearIndexCache(@RequestBody @Valid ClearIndexCacheCmd cmd) {
        return indexCacheService.clearIndexCache(cmd);
    }

    /**
     * 列出指定索引前缀下的所有缓存键
     */
    @GetMapping("/cache/keys")
    public MultiResponse<String> listIndexCacheKeys(ListIndexCacheKeysQry qry) {
        return indexCacheService.listIndexCacheKeys(qry);
    }

    /**
     * 根据键名获取缓存的详细值
     */
    @GetMapping("/cache")
    public SingleResponse<CacheValueDTO> getCacheValue(GetCacheValueQry qry) {
        return indexCacheService.getCacheValue(qry);
    }
} 