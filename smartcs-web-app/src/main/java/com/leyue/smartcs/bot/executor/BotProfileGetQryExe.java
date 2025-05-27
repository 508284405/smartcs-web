package com.leyue.smartcs.bot.executor;

import com.alibaba.cola.dto.SingleResponse;
import com.leyue.smartcs.bot.convertor.BotProfileConvertor;
import com.leyue.smartcs.domain.bot.BotProfile;
import com.leyue.smartcs.domain.bot.gateway.BotProfileGateway;
import com.leyue.smartcs.dto.bot.BotProfileGetQry;
import com.leyue.smartcs.dto.bot.BotProfileDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 获取机器人配置查询执行器
 */
@Component
@RequiredArgsConstructor
public class BotProfileGetQryExe {
    
    private final BotProfileGateway botProfileGateway;
    private final BotProfileConvertor botProfileConvertor;
    
    public SingleResponse<BotProfileDTO> execute(BotProfileGetQry qry) {
        // 查询机器人配置
        Optional<BotProfile> botProfileOpt = botProfileGateway.findById(qry.getBotId());
        
        if (!botProfileOpt.isPresent()) {
            return SingleResponse.buildFailure("NOT_FOUND", "机器人配置不存在");
        }
        
        // 转换为DTO
        BotProfile botProfile = botProfileOpt.get();
        BotProfileDTO dto = botProfileConvertor.toDTO(botProfile);
        
        return SingleResponse.of(dto);
    }
} 