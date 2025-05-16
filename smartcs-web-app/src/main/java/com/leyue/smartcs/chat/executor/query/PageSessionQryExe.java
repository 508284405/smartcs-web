package com.leyue.smartcs.chat.executor.query;

import com.alibaba.cola.dto.PageResponse;
import com.leyue.smartcs.chat.convertor.SessionConvertor;
import com.leyue.smartcs.domain.chat.Session;
import com.leyue.smartcs.domain.chat.gateway.SessionGateway;
import com.leyue.smartcs.dto.chat.SessionDTO;
import com.leyue.smartcs.dto.chat.SessionPageQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 分页查询会话执行器
 */
@Component
@RequiredArgsConstructor
public class PageSessionQryExe {
    
    private final SessionGateway sessionGateway;
    private final SessionConvertor sessionConvertor;
    
    /**
     * 执行分页查询
     *
     * @param query 查询参数
     * @return 分页会话DTO列表
     */
    public PageResponse<SessionDTO> execute(SessionPageQuery query) {
        // 参数校验
        if (query == null) {
            throw new IllegalArgumentException("查询参数不能为空");
        }
        
        // 调用网关接口
        PageResponse<Session> pageResponse = sessionGateway.pageSessions(query);
        
        // 转换为DTO
        List<SessionDTO> sessionDTOList = pageResponse.getData().stream()
                .map(sessionConvertor::toDTO)
                .collect(Collectors.toList());
        
        // 返回结果
        return PageResponse.of(sessionDTOList, 
                pageResponse.getTotalCount(), 
                pageResponse.getPageSize(), 
                pageResponse.getPageIndex());
    }
} 