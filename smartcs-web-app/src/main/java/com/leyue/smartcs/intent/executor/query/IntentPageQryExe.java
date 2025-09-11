package com.leyue.smartcs.intent.executor.query;

import com.alibaba.cola.dto.PageResponse;
import com.leyue.smartcs.domain.intent.entity.Intent;
import com.leyue.smartcs.domain.intent.enums.IntentStatus;
import com.leyue.smartcs.domain.intent.gateway.IntentGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 意图分页查询执行器
 * 
 * @author Claude
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class IntentPageQryExe {
    
    private final IntentGateway intentGateway;
    
    /**
     * 执行意图分页查询
     */
    public PageResponse<Intent> execute(Long catalogId, String status, String keyword, int pageNum, int pageSize) {
        
        log.info("执行意图分页查询 - 目录ID: {}, 状态: {}, 关键词: {}, 页码: {}, 页大小: {}", 
                catalogId, status, keyword, pageNum, pageSize);
        
        IntentStatus intentStatus = null;
        if (status != null) {
            try {
                intentStatus = IntentStatus.fromCode(status);
                log.debug("解析意图状态成功: {} -> {}", status, intentStatus);
            } catch (IllegalArgumentException e) {
                log.warn("无效的意图状态: {}", status);
            }
        }
        
        PageResponse<Intent> result = intentGateway.findByPage(catalogId, intentStatus, keyword, pageNum, pageSize);
        
        log.info("意图分页查询完成 - 查询到数量: {}, 总数: {}, 页码: {}, 每页大小: {}", 
                result.getData().size(), result.getTotalCount(), result.getPageIndex(), result.getPageSize());
        
        return result;
    }
}