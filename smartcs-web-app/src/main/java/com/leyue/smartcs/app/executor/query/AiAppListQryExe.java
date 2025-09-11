package com.leyue.smartcs.app.executor.query;

import com.alibaba.cola.dto.PageResponse;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.app.convertor.AiAppAppConvertor;
import com.leyue.smartcs.domain.app.AiApp;
import com.leyue.smartcs.domain.app.enums.AppStatus;
import com.leyue.smartcs.domain.app.enums.AppType;
import com.leyue.smartcs.domain.app.gateway.AiAppGateway;
import com.leyue.smartcs.dto.app.AiAppDTO;
import com.leyue.smartcs.dto.app.AiAppListQry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * AI应用列表查询执行器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AiAppListQryExe {

    private final AiAppGateway aiAppGateway;
    
    /**
     * 分页查询AI应用列表
     */
    public PageResponse<AiAppDTO> execute(AiAppListQry qry) {
        log.info("分页查询AI应用列表, 页码: {}, 每页: {}, 关键词: {}", 
            qry.getPageIndex(), qry.getPageSize(), qry.getKeyword());
        
        try {
            // 参数转换
            AppType type = null;
            if (qry.getType() != null && !qry.getType().trim().isEmpty()) {
                try {
                    type = AppType.valueOf(qry.getType());
                } catch (IllegalArgumentException e) {
                    throw new BizException("INVALID_APP_TYPE", "无效的应用类型: " + qry.getType());
                }
            }
            
            AppStatus status = null;
            if (qry.getStatus() != null && !qry.getStatus().trim().isEmpty()) {
                try {
                    status = AppStatus.valueOf(qry.getStatus());
                } catch (IllegalArgumentException e) {
                    throw new BizException("INVALID_APP_STATUS", "无效的应用状态: " + qry.getStatus());
                }
            }
            
            // 计算偏移量
            int offset = (qry.getPageIndex() - 1) * qry.getPageSize();
            
            // 查询列表
            List<AiApp> apps = aiAppGateway.listByPage(
                qry.getCreatorId(), 
                type, 
                status, 
                qry.getKeyword(), 
                offset, 
                qry.getPageSize()
            );
            
            // 查询总数
            long total = aiAppGateway.count(
                qry.getCreatorId(),
                type,
                status,
                qry.getKeyword()
            );
            
            // 转换为DTO
            List<AiAppDTO> dtoList = apps.stream()
                    .map(AiAppAppConvertor.INSTANCE::domainToDto)
                    .collect(Collectors.toList());
            
            // 构建分页响应
            PageResponse<AiAppDTO> result = PageResponse.of(dtoList, (int) total, qry.getPageSize(), qry.getPageIndex());
            
            log.info("AI应用列表查询成功, 总数: {}, 当前页: {}", total, dtoList.size());
            
            return result;
            
        } catch (BizException e) {
            log.warn("AI应用列表查询失败: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("AI应用列表查询失败", e);
            throw new BizException("APP_LIST_QUERY_ERROR", "AI应用列表查询失败: " + e.getMessage());
        }
    }
}