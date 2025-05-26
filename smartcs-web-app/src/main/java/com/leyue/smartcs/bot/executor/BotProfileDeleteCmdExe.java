package com.leyue.smartcs.bot.executor;

import com.alibaba.cola.dto.SingleResponse;
import com.leyue.smartcs.domain.bot.domainservice.BotProfileDomainService;
import com.leyue.smartcs.dto.bot.BotProfileDeleteCmd;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 删除机器人配置执行器
 */
@Component
@RequiredArgsConstructor
public class BotProfileDeleteCmdExe {
    
    private final BotProfileDomainService botProfileDomainService;
    
    public SingleResponse<Boolean> execute(BotProfileDeleteCmd cmd) {
        // 执行删除
        botProfileDomainService.deleteBotProfile(cmd.getBotId());
        
        return SingleResponse.of(true);
    }
} 