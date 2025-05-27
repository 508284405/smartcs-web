package com.leyue.smartcs.bot.executor;

import com.alibaba.cola.dto.PageResponse;
import com.leyue.smartcs.bot.convertor.BotProfileConvertor;
import com.leyue.smartcs.domain.bot.BotProfile;
import com.leyue.smartcs.domain.bot.gateway.BotProfileGateway;
import com.leyue.smartcs.dto.bot.BotProfilePageQry;
import com.leyue.smartcs.dto.bot.BotProfileDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 分页查询机器人配置执行器
 */
@Component
@RequiredArgsConstructor
public class BotProfilePageQryExe {
    
    private final BotProfileGateway botProfileGateway;
    private final BotProfileConvertor botProfileConvertor;
    
    public PageResponse<BotProfileDTO> execute(BotProfilePageQry qry) {
        // 分页查询
        PageResponse<BotProfile> pageResult = botProfileGateway.pageQuery(
                qry.getPageIndex(), 
                qry.getPageSize(), 
                qry.getBotName(), 
                qry.getModelName()
        );
        
        // 转换为DTO
        List<BotProfileDTO> dtoList = pageResult.getData().stream()
                .map(botProfileConvertor::toDTO)
                .collect(Collectors.toList());
        
        return PageResponse.of(dtoList, pageResult.getTotalCount(), qry.getPageSize(), qry.getPageIndex());
    }
} 