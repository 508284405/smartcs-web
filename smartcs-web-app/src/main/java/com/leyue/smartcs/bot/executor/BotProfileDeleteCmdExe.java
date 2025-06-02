package com.leyue.smartcs.bot.executor;

import com.alibaba.cola.dto.SingleResponse;
import com.leyue.smartcs.domain.bot.BotProfile;
import com.leyue.smartcs.domain.bot.domainservice.BotProfileDomainService;
import com.leyue.smartcs.dto.bot.BotProfileDeleteCmd;
import com.leyue.smartcs.config.ModelBeanManagerService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 删除机器人配置执行器
 */
@Component
@RequiredArgsConstructor
public class BotProfileDeleteCmdExe {
    
    private final BotProfileDomainService botProfileDomainService;
    private final ModelBeanManagerService modelBeanManagerService;
    public SingleResponse<Boolean> execute(BotProfileDeleteCmd cmd) {
        // 执行删除
        BotProfile botProfile = botProfileDomainService.deleteBotProfile(cmd.getBotId());

        // 销毁SpringAI Bean
        modelBeanManagerService.destroyModelBean(botProfile);

        return SingleResponse.of(true);
    }
} 