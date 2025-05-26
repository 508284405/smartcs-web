package com.leyue.smartcs.bot.executor;

import com.alibaba.cola.dto.SingleResponse;
import com.leyue.smartcs.domain.bot.BotProfile;
import com.leyue.smartcs.domain.bot.domainservice.BotProfileDomainService;
import com.leyue.smartcs.dto.bot.BotProfileUpdateCmd;
import com.leyue.smartcs.dto.bot.BotProfileDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

/**
 * 更新机器人配置执行器
 */
@Component
@RequiredArgsConstructor
public class BotProfileUpdateCmdExe {
    
    private final BotProfileDomainService botProfileDomainService;
    
    public SingleResponse<BotProfileDTO> execute(BotProfileUpdateCmd cmd) {
        // 转换为领域对象
        BotProfile botProfile = new BotProfile();
        BeanUtils.copyProperties(cmd, botProfile);
        
        // 执行更新
        BotProfile updatedBotProfile = botProfileDomainService.updateBotProfile(botProfile);
        
        // 转换为DTO
        BotProfileDTO dto = new BotProfileDTO();
        BeanUtils.copyProperties(updatedBotProfile, dto);
        
        return SingleResponse.of(dto);
    }
} 