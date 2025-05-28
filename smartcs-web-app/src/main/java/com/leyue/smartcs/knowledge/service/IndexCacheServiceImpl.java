package com.leyue.smartcs.knowledge.service;

import com.alibaba.cola.dto.MultiResponse;
import com.alibaba.cola.dto.Response;
import com.alibaba.cola.dto.SingleResponse;
import com.leyue.smartcs.api.IndexCacheService;
import com.leyue.smartcs.domain.knowledge.gateway.SearchGateway;
import com.leyue.smartcs.dto.knowledge.*;
import com.leyue.smartcs.knowledge.executor.command.IndexClearCacheCmdExe;
import com.leyue.smartcs.knowledge.executor.command.IndexCreateCmdExe;
import com.leyue.smartcs.knowledge.executor.command.IndexDeleteCmdExe;
import com.leyue.smartcs.knowledge.executor.query.GetCacheValueQryExe;
import com.leyue.smartcs.knowledge.executor.query.IndexInfoQryExe;
import com.leyue.smartcs.knowledge.executor.query.IndexListCacheKeysQryExe;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 索引缓存服务实现
 */
@Service
@RequiredArgsConstructor
public class IndexCacheServiceImpl implements IndexCacheService {

    private final IndexCreateCmdExe indexCreateCmdExe;
    private final IndexInfoQryExe indexInfoQryExe;
    private final IndexDeleteCmdExe indexDeleteCmdExe;
    private final SearchGateway searchGateway;
    private final IndexClearCacheCmdExe indexClearCacheCmdExe;
    private final IndexListCacheKeysQryExe indexListCacheKeysQryExe;
    private final GetCacheValueQryExe getCacheValueQryExe;

    // ========== 索引管理相关方法 ==========
    
    @Override
    public Response createIndex(CreateIndexCmd cmd) {
        return indexCreateCmdExe.execute(cmd);
    }

    @Override
    public SingleResponse<IndexInfoDTO> getIndexInfo(GetIndexInfoQry qry) {
        return SingleResponse.of(indexInfoQryExe.execute(qry));
    }

    @Override
    public Response deleteIndex(DeleteIndexCmd cmd) {
        return indexDeleteCmdExe.execute(cmd);
    }

    @Override
    public MultiResponse<String> listIndexes() {
        return MultiResponse.of(searchGateway.listIndexes());
    }

    // ========== 索引缓存操作相关方法 ==========
    
    @Override
    public Response clearIndexCache(ClearIndexCacheCmd cmd) {
        return indexClearCacheCmdExe.execute(cmd);
    }

    @Override
    public MultiResponse<String> listIndexCacheKeys(ListIndexCacheKeysQry qry) {
        return MultiResponse.of(indexListCacheKeysQryExe.execute(qry));
    }

    @Override
    public SingleResponse<CacheValueDTO> getCacheValue(GetCacheValueQry qry) {
        return SingleResponse.of(getCacheValueQryExe.execute(qry));
    }
} 