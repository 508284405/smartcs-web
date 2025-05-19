package com.leyue.smartcs.knowledge.executor;

import com.alibaba.cola.dto.SingleResponse;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.domain.knowledge.gateway.RedisearchGateway;
import com.leyue.smartcs.dto.knowledge.GetIndexInfoQry;
import com.leyue.smartcs.dto.knowledge.IndexInfoDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 索引信息查询执行器
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class IndexInfoQryExe {
    
    private final RedisearchGateway redisearchGateway;
    
    /**
     * 执行索引信息查询
     * @param qry 查询参数
     * @return 索引信息
     */
    public IndexInfoDTO execute(GetIndexInfoQry qry) {
        return redisearchGateway.getIndexInfo(qry.getIndexName());
    }
} 