package com.leyue.smartcs.bot.executor;

import com.alibaba.cola.dto.SingleResponse;
import com.leyue.smartcs.bot.convertor.BotProfileConvertor;
import com.leyue.smartcs.config.ModelBeanManagerService;
import com.leyue.smartcs.domain.bot.BotProfile;
import com.leyue.smartcs.domain.bot.domainservice.BotProfileDomainService;
import com.leyue.smartcs.dto.bot.BotProfileUpdateCmd;
import com.leyue.smartcs.dto.bot.BotProfileDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 更新机器人配置执行器
 */
@Component
@RequiredArgsConstructor
public class BotProfileUpdateCmdExe {
    
    private final BotProfileDomainService botProfileDomainService;
    private final BotProfileConvertor botProfileConvertor;
    private final ModelBeanManagerService modelBeanManagerService;
    
    public SingleResponse<BotProfileDTO> execute(BotProfileUpdateCmd cmd) {
        // 转换为领域对象
        BotProfile botProfile = new BotProfile();
        botProfile.setBotId(cmd.getBotId());
        botProfile.setBotName(cmd.getBotName());
        botProfile.setModelName(cmd.getModelName());
        botProfile.setPromptKey(cmd.getPromptKey());
        botProfile.setRemark(cmd.getRemark());
        botProfile.setVendor(cmd.getVendor() != null ? 
            com.leyue.smartcs.domain.bot.enums.VendorTypeEnum.fromCode(cmd.getVendor()) : null);
        botProfile.setModelType(cmd.getModelType() != null ? 
            com.leyue.smartcs.domain.bot.enums.ModelTypeEnum.fromCode(cmd.getModelType()) : null);
        botProfile.setApiKey(cmd.getApiKey());
        botProfile.setBaseUrl(cmd.getBaseUrl());
        botProfile.setOptions(cmd.getOptions());
        botProfile.setEnabled(cmd.getEnabled());
        
        // 执行更新
        BotProfile updatedBotProfile = botProfileDomainService.updateBotProfile(botProfile);

        // 重启ModelBean
        modelBeanManagerService.restartModelBean(updatedBotProfile);
        
        // 转换为DTO
        BotProfileDTO dto = botProfileConvertor.toDTO(updatedBotProfile);
        
        return SingleResponse.of(dto);
    }
} 