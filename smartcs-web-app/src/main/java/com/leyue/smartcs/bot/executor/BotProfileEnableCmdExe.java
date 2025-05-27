package com.leyue.smartcs.bot.executor;

import com.alibaba.cola.dto.SingleResponse;
import com.alibaba.cola.exception.BizException;
import com.leyue.smartcs.config.ModelBeanManagerService;
import com.leyue.smartcs.domain.bot.BotProfile;
import com.leyue.smartcs.domain.bot.gateway.BotProfileGateway;
import com.leyue.smartcs.dto.bot.BotProfileEnableCmd;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 机器人配置启用禁用执行器
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class BotProfileEnableCmdExe {
    
    private final BotProfileGateway botProfileGateway;
    private final ModelBeanManagerService modelBeanManagerService;

    public SingleResponse<Boolean> execute(BotProfileEnableCmd cmd) {
        try {
            log.info("执行机器人配置启用禁用操作，botId: {}, enabled: {}", cmd.getBotId(), cmd.getEnabled());
            
            // 检查机器人配置是否存在
            Optional<BotProfile> botProfileOpt = botProfileGateway.findById(cmd.getBotId());
            if (botProfileOpt.isEmpty()) {
                throw new BizException("机器人配置不存在: " + cmd.getBotId());
            }

            // 执行启用或禁用操作
            boolean success;
            if (cmd.getEnabled()) {
                // 启用：创建SpringAI Bean
                success = botProfileGateway.enableById(cmd.getBotId());
                if (success) {
                    modelBeanManagerService.createModelBean(botProfileOpt.get());
                    log.info("机器人配置启用成功，botId: {}", cmd.getBotId());
                }
            } else {
                // 禁用：销毁SpringAI Bean
                success = botProfileGateway.disableById(cmd.getBotId());
                if (success) {
                    modelBeanManagerService.destroyModelBean(botProfileOpt.get());
                    log.info("机器人配置禁用成功，botId: {}", cmd.getBotId());
                }
            }
            
            if (!success) {
                throw new BizException("机器人配置启用禁用操作失败");
            }
            
            return SingleResponse.of(true);
            
        } catch (Exception e) {
            log.error("机器人配置启用禁用操作失败: {}", e.getMessage(), e);
            throw new BizException("机器人配置启用禁用操作失败: " + e.getMessage());
        }
    }
} 