package com.leyue.smartcs.bot.executor;


import com.alibaba.cola.dto.SingleResponse;
import com.leyue.smartcs.domain.bot.BotProfile;
import com.leyue.smartcs.domain.bot.domainservice.BotProfileDomainService;
import com.leyue.smartcs.dto.bot.BotProfileCreateCmd;
import com.leyue.smartcs.dto.bot.BotProfileDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import com.leyue.smartcs.bot.convertor.BotProfileConvertor;

/**
 * 机器人资料创建命令执行器。
 */
@Component
public class BotProfileCreateCmdExe {

    @Resource
    private BotProfileDomainService botProfileDomainService;

    @Resource
    private BotProfileConvertor botProfileConvertor;

    public SingleResponse<BotProfileDTO> execute(BotProfileCreateCmd cmd) {
        // 转换为领域对象
        BotProfile botProfile = botProfileConvertor.toDomain(cmd);

        // 执行创建
        BotProfile createdBotProfile = botProfileDomainService.createBotProfile(botProfile);

        BotProfileDTO dto = botProfileConvertor.toDTO(createdBotProfile);

        return SingleResponse.of(dto);
    }
} 