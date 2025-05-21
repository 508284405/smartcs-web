package com.leyue.smartcs.knowledge.executor;

import com.leyue.smartcs.dto.knowledge.GetIndexInfoQry;
import com.leyue.smartcs.dto.knowledge.IndexInfoDTO;
import com.leyue.smartcs.knowledge.mapper.RediSearchMapper;
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
    
    private final RediSearchMapper rediSearchMapper;
    
    /**
     * 执行索引信息查询
     * @param qry 查询参数
     * @return 索引信息
     */
    public IndexInfoDTO execute(GetIndexInfoQry qry) {
        return rediSearchMapper.getIndexInfo(qry.getIndexName());
    }
} 